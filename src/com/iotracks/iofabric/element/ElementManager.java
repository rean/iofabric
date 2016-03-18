package com.iotracks.iofabric.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import com.iotracks.iofabric.utils.JSON;
import com.iotracks.iofabric.utils.configuration.Configuration;

public class ElementManager {

	private List<Element> elements;
	private Map<String, Route> routes;
	private Map<String, String> configs;
	private List<Registry> registries;
	private static ElementManager instance = null;
	
	private ElementManager() {
		elements = new ArrayList<>();
		routes = new HashMap<>();
		configs = new HashMap<>();
		registries = new ArrayList<>();
	}
	
	public static ElementManager getInstance() {
		if (instance == null) {
			synchronized (ElementManager.class) {
				if (instance == null)
					instance = new ElementManager();
			}
		}
		return instance;
	}
	
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

	public Map<String, Route> getRoutes() {
		synchronized (ElementManager.class) {
			return routes;
		}
	}

	public Map<String, String> getConfigs() {
		synchronized (ElementManager.class) {
			return configs;
		}
	}

	public List<Registry> getRegistries() {
		synchronized (ElementManager.class) {
			return registries;
		}
	}
	
	public Registry getRegistry(String name) {
		for (Registry registry : registries) {
			if (registry.getUrl().equals(name))
				return registry;
		}
		return null;
	}
	
	public void loadRegistries() {
		try {
			JsonObject registriesObjs =  
					JSON.getJSON(
					"https://iotracks.com/api/v1/instance/registries/id/" + Configuration.getInstanceId() + "/token/" + Configuration.getAccessToken());
			JsonArray registriesList = registriesObjs.getJsonArray("registries"); 

			for (int i = 0; i < registriesList.size(); i++) {
				JsonObject registry = registriesList.getJsonObject(i);
				Registry r = new Registry();
				r.setUrl(registry.getString("url"));
				r.setSecure(registry.getString("secure").equals("true"));
				r.setCertificate(registry.getString("certificate"));
				r.setRequiersCertificate(registry.getString("requierscert").equals("true"));
				r.setUserName(registry.getString("username"));
				r.setPassword(registry.getString("password"));
				r.setUserEmail(registry.getString("useremail"));
				this.registries.add(r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadElementsConfig() {
		try {
			JsonObject configObjs = JSON.getJSON(
					"https://iotracks.com/api/v1/instance/containerconfig/id/" + Configuration.getInstanceId() + "/token/" + Configuration.getAccessToken());
			JsonArray configs = configObjs.getJsonArray("containerconfig");
			for (int i = 0; i < configs.size(); i++) {
				JsonObject config = configs.getJsonObject(i);
				String id = config.getString("id");
				String configString = config.getString("config");
				long lastUpdated = config.getJsonNumber("lastupdatedtimestamp").longValue();
				this.configs.put(id, configString);
				for (Element element : elements)
					if (element.getElementId().equals(id)) {
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
			JsonObject routeObjs = JSON
					.getJSON("https://iotracks.com/api/v1/instance/routing/id/" + Configuration.getInstanceId() + "/token/" + Configuration.getAccessToken());
			JsonArray routes = routeObjs.getJsonArray("routing");
			for (int i = 0; i < routes.size(); i++) {
				JsonObject route = routes.getJsonObject(i);
				Route elementRoute = new Route();
				String container = route.getString("container");

				JsonObject receiversObj = route.getJsonObject("receivers");
				JsonArray receivers = receiversObj.getJsonArray("internal");
				if (receivers.size() == 0)
					continue;
				for (int j = 0; j < receivers.size(); j++) {
					String receiver = receivers.getString(j);
					elementRoute.getReceivers().add(receiver);
				}
				this.routes.put(container, elementRoute);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void loadElementsList() {
		try {
			JsonObject containerObjects = JSON
					.getJSON("https://iotracks.com/api/v1/instance/containerlist/id/" + Configuration.getInstanceId() + "/token/" + Configuration.getAccessToken());
			JsonArray containers = containerObjects.getJsonArray("containerlist");
			if (containers.size() > 0)
				elements = new ArrayList<>();
			for (int i = 0; i < containers.size(); i++) {
				JsonObject container = containers.getJsonObject(i);

				Element element = new Element(container.getString("id"), container.getString("imageid"));

				element.setLastModified(container.getJsonNumber("lastmodified").longValue());

				JsonArray portMappingObjs = container.getJsonArray("portmappings");
				List<PortMapping> pms = null;
				if (portMappingObjs.size() > 0) {
					pms = new ArrayList<>();
					for (int j = 0; j < portMappingObjs.size(); j++) {
						JsonObject portMapping = portMappingObjs.getJsonObject(j);
						PortMapping pm = new PortMapping(portMapping.getString("outsidecontainer"),
								portMapping.getString("insidecontainer"));
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

	public void setRegistries(List<Registry> registries) {
		synchronized (ElementManager.class) {
			this.registries = registries;
		}
	}

	public void setConfigs(Map<String, String> configs) {
		synchronized (ElementManager.class) {
			this.configs = configs;
		}
	}

	public void setElements(List<Element> elements) {
		synchronized (ElementManager.class) {
			this.elements = elements;
		}
	}

	public void setRoutes(Map<String, Route> routes) {
		synchronized (ElementManager.class) {
			this.routes = routes;
		}
	}

	public boolean elementExists(String elementId) {
		for (Element element : elements)
			if (element.getElementId().equals(elementId))
				return true;
				
		return false;
	}

	public void clearData() {
		synchronized (ElementManager.class) {
			elements.clear();
			routes.clear();
			configs.clear();
			registries.clear();
		}
	}

	public Element getElementById(String elementId) {
		for (Element element : elements)
			if (element.getElementId().equals(elementId))
				return element;
				
		return null;
	}

}
