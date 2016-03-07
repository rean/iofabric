package com.iotracks.iofabric.local_api;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.PortMapping;
import com.iotracks.iofabric.element.Route;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.JSON;

public class LocalApi {

	private  List<Element> containersList;
	private final String MODULE_NAME = "Local API";
	private final String instanceId = "qk7PnPVpDTGmx3zWNR8zNP34";
	private final String token = "0b51a84b066a049228ea3e0b14f424720842c132153b2fa7091c21a1129534d4";
	

	public LocalApi() {
		containersList = new ArrayList<>();	
	}


	public static void main(String[] args) throws Exception {
		LocalApi api = new LocalApi();
		api.start();
	}

	public void start() throws Exception {
		readContainerConfig();
		LocalApiServer server = new LocalApiServer();
		server.start();
		StatusReporter.setLocalApiStatus().setCurrentIpAddress("127.0.0.1");
		WebSocketMap socketMap = WebSocketMap.getInstance();
	}

	public List<Element> readContainerConfig() throws Exception{
		JSONParser parser = new JSONParser();

		Object obj = parser.parse(new FileReader("/home/ashita/Documents/config.json"));
		JSONObject jsonObject = (JSONObject) obj;
		JSONArray containersConfig = (JSONArray) jsonObject.get("containerconfig");

		for (Object containerObject : containersConfig) {
			JSONObject container = (JSONObject) containerObject;
			Element element = new Element(container.get("id").toString(), "imageidtobehere");

			if(container.get("id") != null)
				element.setContainerID(container.get("id").toString());

			if(container.get("lastupdatedtimestamp") != null)
				element.setLastModified(Long.parseLong(container.get("lastupdatedtimestamp").toString()));

			if(container.get("starttime") != null)
				element.setLastModified(Long.parseLong(container.get("starttime").toString()));

			if(container.get("config") != null)
				element.setElementConfig(container.get("config").toString());

			if(container.get("route") != null)
				element.setRoute((Route)container.get("route"));

			JSONArray portMappingObjs = (JSONArray) container.get("portmappings");
			if(portMappingObjs != null){
				List<PortMapping> pms = null;
				if (portMappingObjs.size() > 0) {
					pms = new ArrayList<>();
					for (Object portMappingObj : portMappingObjs) {
						JSONObject portMapping = (JSONObject) portMappingObj;
						PortMapping pm =				 new PortMapping(portMapping.get("outsidecontainer").toString(), portMapping.get("insidecontainer").toString());
						pms.add(pm);
					}
				}

				element.setPortMappings(pms);
			}

			this.containersList.add(element);
		}	  

		return containersList;
	}

	public List<Element> getContainersList(){
		return containersList;
	}

	//To be remove later - for testing purpose
	public void writeContainerConfigToFile() throws Exception{
		FileWriter file = new FileWriter("/home/ashita/Documents/config.json");
		try{
			JSONObject configObjs = JSON.getJSON("https://iotracks.com/api/v1/instance/containerconfig/id/"
					+ instanceId + "/token/" + token);
			JSONArray configs = (JSONArray) configObjs.get("containerconfig");

			for (Object configObj : configs) {
				JSONObject config = (JSONObject) configObj;
				file.write(config.toJSONString());
			}
			System.out.println("Successfully write JSON Object to File...");
		}finally{
			file.flush();
			file.close();
		}
	}


	public void updateContainerConfig(String containerId, String property, Object value) throws Exception{
		File jsonFile = new File("/home/ashita/Documents/config.json");
		String jsonString = FileUtils.readFileToString(jsonFile);

		JsonElement jelement = new JsonParser().parse(jsonString);
		JsonObject jobject = jelement.getAsJsonObject();
		JsonElement containersConfig = jobject.get("containerlist");

		JsonArray JsonContainerArray = containersConfig.getAsJsonArray();

		for (Object containerObject : JsonContainerArray) {
			JsonObject container = (JsonObject) containerObject;
			if((container.get("id").getAsString()).equals(containerId)){
				if(container.get(property) != null){
					container.remove(property);
				}

				container.addProperty(property, value.toString());
			}
		}

		for(Element current:containersList){
			if(current.getContainerID().toString().equals(containerId)){
				if(property.equals("id"))
					current.setContainerID(value.toString());

				if(property.equals("lastupdatedtimestamp"))
					current.setLastModified(Long.parseLong(value.toString()));

				if(property.equals("starttime"))
					current.setLastModified(Long.parseLong(value.toString()));

				if(property.equals("config"))
					current.setElementConfig(value.toString());

				if(property.equals("route"))
					current.setRoute((Route)value);

				if(property.equals("outsidecontainer") || property.equals("insidecontainer")){
					List<PortMapping> pms = current.getPortMappings();
					PortMapping mapping = pms.get(0);

					if(property.equals("outsidecontainer"))
						mapping = new PortMapping(value.toString(), mapping.getInternal().toString());

					if(property.equals("insidecontainer"))
						mapping = new PortMapping(mapping.getExternal().toString(), value.toString());

					current.setPortMappings(pms);
				}
			}
		}

		Gson gson = new Gson();

		String resultingJson = gson.toJson(jelement);
		FileUtils.writeStringToFile(jsonFile, resultingJson);
	}
}
