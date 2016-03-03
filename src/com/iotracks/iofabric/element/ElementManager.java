package com.iotracks.iofabric.element;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.iotracks.iofabric.utils.JSON;
import com.iotracks.iofabric.utils.configuration.Configuration;

public class ElementManager {

	private static List<Element> elements = new ArrayList<>();
	private static List<Registry> registries = new ArrayList<>();

	public void loadFromApi() {
		synchronized (ElementManager.class) {
			while (!elements.isEmpty())
				elements.remove(0);
			loadElementsList();
			loadElementsConfig();
			loadRoutes();
		}
	}

	public List<Element> getElements() {
		synchronized (ElementManager.class) {
			return elements;
		}
	}

	public static List<Registry> getRegistries() {
		synchronized (ElementManager.class) {
			return registries;
		}
	}
	
	public void loadRegistries() {
		try {
			JSONObject registriesObjs = JSON.getJSON(
					"https://iotracks.com/api/v1/instance/registries/id/" + Configuration.getInstanceId() + "/token/" + Configuration.getAccessToken());
			JSONArray registriesList = (JSONArray) registriesObjs.get("registries");
			for (Object registryObj : registriesList) {
				JSONObject registry = (JSONObject) registryObj;
				Registry r = new Registry();
				r.setUrl(registry.get("url").toString());
				r.setSecure(registry.get("secure").toString().equals("true"));
				r.setCertificate(registry.get("certificate").toString());
				r.setRequiersCertificate(registry.get("requierscert").toString().equals("true"));
				r.setUserName(registry.get("username").toString());
				r.setPassword(registry.get("password").toString());
				r.setUserEmail(registry.get("useremail").toString());
				registries.add(r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadElementsConfig() {
		try {
			JSONObject configObjs = JSON.getJSON(
					"https://iotracks.com/api/v1/instance/containerconfig/id/" + Configuration.getInstanceId() + "/token/" + Configuration.getAccessToken());
			JSONArray configs = (JSONArray) configObjs.get("containerconfig");
			for (Object configObj : configs) {
				JSONObject config = (JSONObject) configObj;
				String id = config.get("id").toString();
				String configString = config.get("config").toString();
				long lastUpdated = Long.parseLong(config.get("lastupdatedtimestamp").toString());
				for (Element element : elements)
					if (element.getElementId().equals(id)) {
						element.setElementConfig(configString);
						element.setLastUpdated(lastUpdated);
						break;
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadRoutes() {
		try {
			JSONObject routeObjs = JSON
					.getJSON("https://iotracks.com/api/v1/instance/routing/id/" + Configuration.getInstanceId() + "/token/" + Configuration.getAccessToken());
			JSONArray routes = (JSONArray) routeObjs.get("routing");
			for (Object routeObj : routes) {
				JSONObject route = (JSONObject) routeObj;
				Route elementRoute = new Route();
				String container = route.get("container").toString();

				JSONObject receivers = (JSONObject) route.get("receivers");

				JSONArray internals = (JSONArray) receivers.get("internal");
				List<Element> internalElements = null;
				if (internals.size() > 0)
					internalElements = new ArrayList<>();
				for (Object internalObj : internals) {
					String internal = internalObj.toString();
					for (Element element : elements)
						if (element.getElementId().equals(internal)) {
							internalElements.add(element);
							break;
						}
				}

				elementRoute.setInternalReceivers(internalElements);
				for (Element element : elements)
					if (element.getElementId().equals(container)) {
						element.setRoute(elementRoute);
						break;
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void loadElementsList() {
		try {
			JSONObject containerObjects = JSON
					.getJSON("https://iotracks.com/api/v1/instance/containerlist/id/" + Configuration.getInstanceId() + "/token/" + Configuration.getAccessToken());
			JSONArray containers = (JSONArray) containerObjects.get("containerlist");
			if (containers.size() > 0)
				elements = new ArrayList<>();
			for (Object containerObj : containers) {
				JSONObject container = (JSONObject) containerObj;

				Element element = new Element(container.get("id").toString(), container.get("imageid").toString());

				element.setLastModified(Long.parseLong(container.get("lastmodified").toString()));

				JSONArray portMappingObjs = (JSONArray) container.get("portmappings");
				List<PortMapping> pms = null;
				if (portMappingObjs.size() > 0) {
					pms = new ArrayList<>();
					for (Object portMappingObj : portMappingObjs) {
						JSONObject portMapping = (JSONObject) portMappingObj;
						PortMapping pm = new PortMapping(portMapping.get("outsidecontainer").toString(),
								portMapping.get("insidecontainer").toString());
						pms.add(pm);
					}
				}
				element.setPortMappings(pms);
				elements.add(element);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
