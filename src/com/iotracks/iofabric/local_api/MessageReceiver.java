package com.iotracks.iofabric.local_api;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;

public class MessageReceiver {
	public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		System.out.println("in receive Message");
	}
}
