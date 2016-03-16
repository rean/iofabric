package com.iotracks.iofabric.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;

import com.iotracks.iofabric.utils.configuration.Configuration;

public class Orchestrator {
	public String controllerUrl; // = "http://127.0.0.1:12345/api/v2/";
	public String instanceId;
	public String accessToken;
	
	public Orchestrator() {
		this.update();
	}

	public String getControllerUrl() {
		return controllerUrl;
	}

	public void setControllerUrl(String controllerUrl) {
		this.controllerUrl = controllerUrl;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public boolean ping() {
		try {
			JsonObject result = JSON.getJSON(controllerUrl + "status");
			return result.getString("status").equals("ok");
		} catch (Exception e) {
			return false;
		} 
	}

	public JsonObject provision(String key) throws Exception {
		JsonObject result = null;
		try {
			result = JSON.getJSON(controllerUrl + "instance/provision/key/" + key);
		} catch (Exception e) {
			throw e;
		} 
		return result;
	}
	
	private HttpURLConnection getConnection(String url, boolean secure) {
		try {
			HttpURLConnection httpRequest;
			if (secure) 
				httpRequest = (HttpsURLConnection) new URL(url).openConnection();
			else
				httpRequest = (HttpURLConnection) new URL(url).openConnection();
			return httpRequest;
		} catch (Exception e) {
			return null;
		}
	}
	
	public JsonObject doCommand(String command, Map<String, Object> queryParams, Map<String, Object> postParams) throws Exception {
		JsonObject result = null;
		
		StringBuilder uri = new StringBuilder(controllerUrl);
		
		uri.append("instance/")
			.append(command)
			.append("/id/").append(instanceId)
			.append("/token/").append(accessToken);
		
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
		
		try {
			HttpURLConnection httpRequest = getConnection(uri.toString(), false);
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

	public void update() {
		instanceId = Configuration.getInstanceId();
		accessToken = Configuration.getAccessToken();
		controllerUrl = Configuration.getControllerUrl();
	}

}
