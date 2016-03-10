package com.iotracks.iofabric.local_api;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;


public class LocalApiServerHandler extends SimpleChannelInboundHandler<Object>{

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println(ctx);
		System.out.println("In LocalApiServerHandler:channelRead0");
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			String mapName = findContextMapName(ctx);
			if(mapName.equals("control")){
				ControlWebsocketHandler controlSocket = new ControlWebsocketHandler();
				controlSocket.handleWebSocketFrame(ctx, (WebSocketFrame)msg);
			}else{
				MessageWebsocketHandler messageSocket = new MessageWebsocketHandler();
				messageSocket.handleWebSocketFrame(ctx, (WebSocketFrame)msg);
			}
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		System.out.println("In LocalApiServerHandler: channelReadComplete");
		ctx.flush();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

		System.out.println("In LocalApiHandler: handleHttpRequest");

		if (req.getUri().equals("/v2/config/get")) {
			System.out.println("In LocalApiHandler: calling configuration..." );
			GetConfigurationHandler config = new GetConfigurationHandler();
			config.handle( ctx,  req);
			return;
		}

		if (req.getUri().equals("/v2/messages/next")) {
			MessageReceiverHandler receiver = new MessageReceiverHandler();
			receiver.handle(ctx, req);
			return;
		}


		if (req.getUri().equals("/v2/messages/new")) {
			MessageSenderHandler send = new MessageSenderHandler();
			send.handle(ctx, req);
			return;
		}

		String uri = req.getUri();
		uri = uri.substring(1);
		String[] tokens = uri.split("/");
		String url = "/"+tokens[0]+"/"+tokens[1]+"/"+tokens[2];

		if (url.equals("/v2/control/socket")) {
			ControlWebsocketHandler controlSocket = new ControlWebsocketHandler();
			controlSocket.handle(ctx, req);
		}

		if (url.equals("/v2/message/socket")) {
			System.out.println("In message websocket fullhttprequest..... ");
			MessageWebsocketHandler messageSocket = new MessageWebsocketHandler();
			messageSocket.handle(ctx, req);
		}

	}

	private String findContextMapName(ChannelHandlerContext ctx){
		String mapName = null;

		Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
		for (Iterator<Map.Entry<String,ChannelHandlerContext>> it = controlMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String,ChannelHandlerContext> e = it.next();
			if (ctx.equals(e.getValue())) {
				mapName = "control";
				return mapName;
			}
		}

		Hashtable<String, ChannelHandlerContext> messageMap = WebSocketMap.messageWebsocketMap;
		for (Iterator<Map.Entry<String,ChannelHandlerContext>> it = messageMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String,ChannelHandlerContext> e = it.next();
			if (ctx.equals(e.getValue())) {
				mapName = "message";
				return mapName;
			}
		}

		return mapName;
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
