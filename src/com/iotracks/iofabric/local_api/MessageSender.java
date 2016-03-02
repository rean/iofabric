package com.iotracks.iofabric.local_api;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class MessageSender {
	
	public void handle(ChannelHandlerContext ctx, HttpRequest req, Object message){
		System.out.println("In send message..." );
		System.out.println(message.toString());
	}
}
