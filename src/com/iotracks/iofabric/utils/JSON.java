package com.iotracks.iofabric.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonObject;

public class JSON {
	
	public static JsonObject getJSON(String surl) throws Exception {
		URL url = new URL(surl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        JsonObject result = Json.createReader(in).readObject();
        
		conn.disconnect();
		
		return result;
	}

}
