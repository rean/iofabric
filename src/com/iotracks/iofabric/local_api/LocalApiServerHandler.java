package com.iotracks.iofabric.local_api;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class LocalApiServerHandler extends SimpleChannelInboundHandler<Object>{

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof FullHttpRequest) {
			handleHttpRequest(ctx, (FullHttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			ControlWebsocketHandler controlSocket = new ControlWebsocketHandler();
			controlSocket.handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

		System.out.println("In LocalApiHandler: handleHttpRequest" );

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
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
