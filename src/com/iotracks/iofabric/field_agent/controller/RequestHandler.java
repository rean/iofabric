package com.iotracks.iofabric.field_agent.controller;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.iotracks.iofabric.utils.logging.LoggingService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RequestHandler implements HttpHandler {
	private final String filename;

	public RequestHandler(String filename) {
		this.filename = filename;
	}

	public String inputStreamToString(InputStream input) {
		StringBuilder result = new StringBuilder();
		int data = -1;
		try {
			data = input.read();
		} catch (IOException e) {
			return "";
		}
		while (data != -1) {
			result.append((char) data);
			try {
				data = input.read();
			} catch (IOException e) {
				return "";
			}
		}
		return result.toString();
	}

	private void sendString(String response, OutputStream responseBody) throws Exception {
		BufferedOutputStream out = new BufferedOutputStream(responseBody);
		ByteArrayInputStream bis = new ByteArrayInputStream(response.getBytes(StandardCharsets.US_ASCII));
		byte[] buffer = new byte[1024];
        int count ;
        while ((count = bis.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
        out.close();
        bis.close();
	}
	
	private String responseFromFile() {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get("/etc/iofabric/controller/" + filename));
			return new String(encoded, StandardCharsets.US_ASCII);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	@Override
	public void handle(HttpExchange ex) throws IOException {
		LoggingService.logInfo("CONTROLLER", ex.getRequestURI().toString());
		String response = responseFromFile(); 
		ex.sendResponseHeaders(200, response.length());
		try {
			sendString(response, ex.getResponseBody());
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

}
