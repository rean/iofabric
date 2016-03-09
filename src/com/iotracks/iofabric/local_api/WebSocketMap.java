package com.iotracks.iofabric.local_api;

import java.util.Hashtable;

import io.netty.channel.ChannelHandlerContext;

public class WebSocketMap {
	static Hashtable<String, ChannelHandlerContext> controlWebsocketMap;
	static Hashtable<String, ChannelHandlerContext> messageWebsocketMap;

	private static WebSocketMap instance = null;

	private WebSocketMap(){

	}

	public static WebSocketMap getInstance(){
		if(instance == null){
			instance = new WebSocketMap();
			controlWebsocketMap = new Hashtable<String, ChannelHandlerContext>();
			messageWebsocketMap = new Hashtable<String, ChannelHandlerContext>();
		}
		return instance;
	}
}
