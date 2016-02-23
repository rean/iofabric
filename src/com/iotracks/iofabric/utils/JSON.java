package com.iotracks.iofabric.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSON {
	
	public static JSONObject getJSON(String surl) throws Exception {
		URL url = new URL(surl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        JSONParser parser = new JSONParser();
		JSONObject result = (JSONObject) parser.parse(in);
		
		conn.disconnect();
		
		return result;
	}

}
