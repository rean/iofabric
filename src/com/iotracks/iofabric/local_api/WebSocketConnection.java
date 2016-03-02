package com.iotracks.iofabric.local_api;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.*;


public class WebSocketConnection {

	private WebSocketServerHandshaker handshaker;

	public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
		
		
		// Handle a bad request.
		if (!req.getDecoderResult().isSuccess()) {
			System.out.println("In handleHttpRequest: bad status");
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
			return;
		}

		// Allow only GET methods.
		if (req.getMethod() != GET) {
			System.out.println("In handleHttpRequest: forbidden status");
			sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
			return;
		}

		// Send the demo page and favicon.ico
		if ("/socket".equals(req.getUri())) {
			System.out.println("URL hit : " + req.getUri());
			System.out.println(req.getDecoderResult());
			System.out.println("In OK: SOCKET Server...");
			ByteBuf content = Unpooled.copiedBuffer("Hello world", CharsetUtil.US_ASCII);;
			
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
			res.headers().set(CONTENT_TYPE, "charset=UTF-8");
			HttpHeaders.setContentLength(res, content.readableBytes());
			sendHttpResponse(ctx, req, res);
			return;
		}


		if ("/favicon.ico".equals(req.getUri())) {
			System.out.println("In handleHttpRequest: not found status");
			FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
			sendHttpResponse(ctx, req, res);
			return;
		}

		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
				getWebSocketLocation(req), null, true);
		handshaker = wsFactory.newHandshaker(req);

		if (handshaker == null) {
			System.out.println("handleHttpRequest: handshaker == null");

			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			System.out.println("handleHttpRequest: handshaker == else");

			handshaker.handshake(ctx.channel(), req);
		}
		System.out.println("enddd method handleHttpRequest");
	}

	private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
		// Generate an error page if response getStatus code is not OK (200).
		
		if (res.getStatus().code() != 200) {
			System.out.println("In sendHttpResponse : OK");
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpHeaders.setContentLength(res, res.content().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
	

	private static String getWebSocketLocation(FullHttpRequest req) {
		String location =  req.headers().get(HOST) + "/msg/send";
		System.out.println(location);
		
			return "ws://" + location;
	}

}
