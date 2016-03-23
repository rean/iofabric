package com.iotracks.iofabric.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.client.config.RequestConfig;

import com.iotracks.iofabric.utils.configuration.Configuration;

public class Orchestrator {
	private String controllerUrl; // = "http://127.0.0.1:12345/api/v2/";
	private String instanceId;
	private String accessToken;
	private String cert;
	private String eth;
	
	public Orchestrator() {
		this.update();
	}

	public boolean ping() {
		try {
			JsonObject result = getJSON(controllerUrl + "status");
			return result.getString("status").equals("ok");
		} catch (Exception e) {
			return false;
		} 
	}

	public JsonObject provision(String key) throws Exception {
		JsonObject result = null;
		try {
			result = getJSON(controllerUrl + "instance/provision/key/" + key);
		} catch (Exception e) {
			throw e;
		} 
		return result;
	}
	
	public HttpURLConnection getConnection(String url, boolean secure) throws Exception {
		InetAddress address = null;
		boolean found = false;
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
		    while (networkInterfaces.hasMoreElements()) {
		        NetworkInterface networkInterface = networkInterfaces.nextElement();
		        if (networkInterface.getName().equals(eth)) {
		        	Enumeration<InetAddress> ipAddresses = networkInterface.getInetAddresses();
		        	while (ipAddresses.hasMoreElements()) {
		        		address = ipAddresses.nextElement();
		        		if (address instanceof Inet4Address) {
		        			found = true;
		        			break;
		        		}
		        	}
		        	if (found)
		        		break;
		        }
		    }
		} catch (Exception e) {}
		
		if (!found)
			throw new Exception(String.format("unable to bind network interface \"%s\"", eth));
		
		try {
			RequestConfig config = RequestConfig.custom().setLocalAddress(address).build();

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
	
	public JsonObject getJSON(String surl) throws Exception {
		HttpURLConnection conn = getConnection(surl, false);
		conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        JsonObject result = Json.createReader(in).readObject();
        
		conn.disconnect();
		
		return result;
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
		cert = Configuration.getControllerCert();
		eth = Configuration.getNetworkInterface();
	}

}
