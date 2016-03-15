package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;


public class LocalApiServerHandler extends SimpleChannelInboundHandler<Object>{
	private final String MODULE_NAME = "Local API";

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		LoggingService.logInfo(MODULE_NAME,
				String.format("\"%s\": channel context", ctx));

		LoggingService.logInfo(MODULE_NAME, "In LocalApiServerHandler: Channel read start");
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
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception{
		LoggingService.logInfo(MODULE_NAME, "In LocalApiServerHandler: Channel read complete");
		ctx.flush();
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {

		LoggingService.logInfo(MODULE_NAME, "In Local Api Handler: handshaking start");

		if (req.getUri().equals("/v2/config/get")) {
			LoggingService.logInfo(MODULE_NAME, "In Local Api Handler: Get configuration" );
			GetConfigurationHandler config = new GetConfigurationHandler();
			config.handle( ctx,  req);
			return;
		}

		if (req.getUri().equals("/v2/messages/next")) {
			LoggingService.logInfo(MODULE_NAME, "In Local Api Handler: Get next messages" );
			MessageReceiverHandler receiver = new MessageReceiverHandler();
			receiver.handle(ctx, req);
			return;
		}


		if (req.getUri().equals("/v2/messages/new")) {
			LoggingService.logInfo(MODULE_NAME, "In Local Api Handler: Send new message" );
			MessageSenderHandler send = new MessageSenderHandler();
			send.handle(ctx, req);
			return;
		}
		
		if (req.getUri().equals("/v2/messages/query")) {
			LoggingService.logInfo(MODULE_NAME, "In Local Api Handler: Get queried messages" );
			QueryMessageReceiverHandler queryReceiver = new QueryMessageReceiverHandler();
			queryReceiver.handle(ctx, req);
			return;
		}

		String uri = req.getUri();
		uri = uri.substring(1);
		String[] tokens = uri.split("/");
		String url = "/"+tokens[0]+"/"+tokens[1]+"/"+tokens[2];

		if (url.equals("/v2/control/socket")) {
			LoggingService.logInfo(MODULE_NAME, "In Local Api Handler: Open control websocket" );
			ControlWebsocketHandler controlSocket = new ControlWebsocketHandler();
			controlSocket.handle(ctx, req);
			return;
		}

		if (url.equals("/v2/message/socket")) {
			LoggingService.logInfo(MODULE_NAME, "In Local Api Handler: Open message websocket" );
			MessageWebsocketHandler messageSocket = new MessageWebsocketHandler();
			messageSocket.handle(ctx, req);
			return;
		}
		
		ByteBuf	errorMsgBytes = ctx.alloc().buffer();
		String errorMsg = " Request not found ";
		errorMsgBytes.writeBytes(errorMsg.getBytes());
		sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.NOT_FOUND, errorMsgBytes));
		return;
	}

	private String findContextMapName(ChannelHandlerContext ctx) throws Exception{
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
	
	private static void sendHttpResponse(
			ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) throws Exception{
		if (res.getStatus().code() != 200) {
			ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
			res.content().writeBytes(buf);
			buf.release();
			HttpHeaders.setContentLength(res, res.content().readableBytes());
		}

		ChannelFuture f = ctx.channel().writeAndFlush(res);
		if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}
}
