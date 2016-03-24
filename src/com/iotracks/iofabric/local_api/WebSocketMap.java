package com.iotracks.iofabric.local_api;

import java.util.Hashtable;

import io.netty.channel.ChannelHandlerContext;

public class WebSocketMap {
	static Hashtable<String, ChannelHandlerContext> controlWebsocketMap;
	static Hashtable<String, ChannelHandlerContext> messageWebsocketMap;
	
	static Hashtable<ChannelHandlerContext, MessageSendContextCount> unackMessageSendingMap;
	static Hashtable<ChannelHandlerContext, Integer> unackControlSignalsMap;

	private static WebSocketMap instance = null;

	private WebSocketMap(){

	}

	public static WebSocketMap getInstance(){
		if (instance == null) {
			synchronized (WebSocketMap.class) {
				if(instance == null){
					instance = new WebSocketMap();
					controlWebsocketMap = new Hashtable<String, ChannelHandlerContext>();
					messageWebsocketMap = new Hashtable<String, ChannelHandlerContext>();
					unackMessageSendingMap = new Hashtable<ChannelHandlerContext, MessageSendContextCount>();
					unackControlSignalsMap = new Hashtable<ChannelHandlerContext, Integer>();
				}
			}
		}
		return instance;
	}
}