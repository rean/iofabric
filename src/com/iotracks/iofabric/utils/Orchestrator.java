package com.iotracks.iofabric.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.iotracks.iofabric.utils.configuration.Configuration;

public class Orchestrator {
	private String apiUrl = "https://iotracks.com/api/v1/";
	
	public Orchestrator() {
	}

	public boolean ping() {
		try {
			JSONObject data = JSON.getJSON(apiUrl + "status");
			return data.get("status").equals("ok");
		} catch (Exception e) {
			return false;
		} 
	}

	public JSONObject provision(String key) throws Exception {
		JSONObject result = null;
		try {
			result = JSON.getJSON(apiUrl + "instance/provision/key/" + key);
		} catch (Exception e) {
			throw e;
		} 
		return result;
	}
	
	public JSONObject doCommand(String command, Map<String, Object> queryParams, Map<String, Object> postParams) throws Exception {
		JSONObject result = null;
		
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
			JSONParser parser = new JSONParser();
			result = (JSONObject) parser.parse(in);
		} catch (Exception e) {
			throw e;
		}
		
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		Orchestrator orchestrator = new Orchestrator();
		
		Map<String, Object> query = new HashMap<>();
		query.put("id", "qk7PnPVpDTGmx3zWNR8zNP34");
		query.put("token", "0b51a84b066a049228ea3e0b14f424720842c132153b2fa7091c21a1129534d4");
		
		Map<String, Object> post = new HashMap<>();
		post.put("daemonstatus", "running");
		post.put("daemonlaststart", 1234567890);
		post.put("cpuusage", 24.71);
		
		Map<String, Object> result = orchestrator.doCommand("containerlist", query, null);
		JSONArray listArray = (JSONArray) result.get("containerlist");
		for (Object o : listArray) {
			((Map<String, Object>) o).entrySet().forEach(entry -> {
				System.out.println(entry.getKey() + " : " + entry.getValue());
			});
			System.out.println();
		}
		
	}
}
