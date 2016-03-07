package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

import java.util.Hashtable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

public class ControlWebsocketHandler {
	private static final String WEBSOCKET_PATH = "/v2/control/socket";

	private WebSocketServerHandshaker handshaker;

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{
		System.out.println("In ControlWebsocketHandler : handle");
		System.out.println("Handshake start.... ");

		String uri = req.getUri();
		uri = uri.substring(1);
		String[] tokens = uri.split("/");
		String publisherId = tokens[4].trim();
		System.out.println("Publisher Id: "+ publisherId);

		WebSocketMap map = WebSocketMap.getInstance();
		Hashtable<String, Object> controlMap = map.controlWebsocketMap;
		controlMap.put(publisherId, ctx);

		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			System.out.println("In handshake = null...");
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			System.out.println("In handshake else.....");
			handshaker.handshake(ctx.channel(), req);
		}

		System.out.println("Handshake end....");
	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
		System.out.println("In ControlWebsocketHandler : handleWebSocketFrame");

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			System.out.println("In websocket handleWebSocketFrame..... CloseWebSocketFrame... " );
			handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
			return;
		}

		if (frame instanceof PingWebSocketFrame) {
			System.out.println("In websocket handleWebSocketFrame..... PingWebSocketFrame... " );
			ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
			return;
		}

		if (!(frame instanceof TextWebSocketFrame)) {
			System.out.println("In websocket handleWebSocketFrame..... TextWebSocketFrame... " );
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
					.getName()));
		}

		// Send the uppercase string back.
		String request = ((TextWebSocketFrame) frame).text();
		System.err.printf("%s received %s%n", ctx.channel(), request);
		ctx.channel().write(new TextWebSocketFrame(request.toUpperCase()));
	}

	private static String getWebSocketLocation(FullHttpRequest req) {
		String location =  req.headers().get(HOST) + WEBSOCKET_PATH;
		if (LocalApiServer.SSL) {
			return "wss://" + location;
		} else {
			return "ws://" + location;
		}
	}
}
