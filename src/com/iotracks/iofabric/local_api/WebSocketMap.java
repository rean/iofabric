package com.iotracks.iofabric.local_api;

import java.util.Hashtable;

public class WebSocketMap {
	static Hashtable<String, Object> controlWebsocketMap;
	static Hashtable<String, Object> messageWebsocketMap;

	private static WebSocketMap instance = null;

	private WebSocketMap(){

	}

	public static WebSocketMap getInstance(){
		if(instance == null){
			instance = new WebSocketMap();
			controlWebsocketMap = new Hashtable<String, Object>();
			messageWebsocketMap = new Hashtable<String, Object>();
		}
		return instance;
	}
}
