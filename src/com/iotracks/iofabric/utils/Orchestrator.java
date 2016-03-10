package com.iotracks.iofabric.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.iotracks.iofabric.utils.configuration.Configuration;

public class Orchestrator {
	private String apiUrl = "https://iotracks.com/api/v1/";
	
	public Orchestrator() {
	}

	public boolean ping() {
		try {
			JsonObject data = JSON.getJSON(apiUrl + "status");
			return data.get("status").equals("ok");
		} catch (Exception e) {
			return false;
		} 
	}

	public JsonObject provision(String key) throws Exception {
		JsonObject result = null;
		try {
			result = JSON.getJSON(apiUrl + "instance/provision/key/" + key);
		} catch (Exception e) {
			throw e;
		} 
		return result;
	}
	
	public JsonObject doCommand(String command, Map<String, Object> queryParams, Map<String, Object> postParams) throws Exception {
		JsonObject result = null;
		
		StringBuilder uri = new StringBuilder(apiUrl);
		
		uri.append("instance/")
			.append(command)
			.append("/id/").append(Configuration.getInstanceId())
			.append("/token/").append(Configuration.getAccessToken());
		
		if (queryParams != null)
			queryParams.entrySet().forEach(entry -> {
				uri.append("/").append(entry.getKey())
					.append("/").append(entry.getValue());
			});

		StringBuilder postData = new StringBuilder();
		if (postParams != null)
			postParams.entrySet().forEach(entry -> {
				if (postData.length() > 0)
					postData.append("&");
				try {
					postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
						.append("=")
						.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
				} catch (Exception e) {
				}
			});
		byte[] postDataBytes = postData.toString().getBytes();
		
		HttpURLConnection httpRequest;
		try {
			httpRequest = (HttpURLConnection) new URL(uri.toString()).openConnection();
			httpRequest.setRequestMethod("POST");
			httpRequest.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpRequest.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
			httpRequest.setDoOutput(true);
			httpRequest.getOutputStream().write(postDataBytes);
			BufferedReader in = new BufferedReader(new InputStreamReader(httpRequest.getInputStream(), "UTF-8"));
			result = Json.createReader(in).readObject();
		} catch (Exception e) {
			throw e;
		}
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		Orchestrator orchestrator = new Orchestrator();
		Configuration.loadConfig();
		
		Map<String, Object> query = new HashMap<>();
		query.put("id", "qk7PnPVpDTGmx3zWNR8zNP34");
		query.put("token", "0b51a84b066a049228ea3e0b14f424720842c132153b2fa7091c21a1129534d4");
		
		Map<String, Object> post = new HashMap<>();
		post.put("daemonstatus", "running");
		post.put("daemonlaststart", 1234567890);
		post.put("cpuusage", 24.71);
		
		JsonObject result = orchestrator.doCommand("containerlist", query, null);
		JsonArray listArray = result.getJsonArray("containerlist");
		for (int i = 0; i < listArray.size(); i++) {
			System.out.println(i);
			JsonObject listItem = listArray.getJsonObject(i);
			listItem.forEach((key, value) -> {
				System.out.println("\t" + key + " : " + value.toString());
			});
			System.out.println();
		}
		
	}
}
