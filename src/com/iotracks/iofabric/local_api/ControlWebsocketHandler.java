package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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
	private final String MODULE_NAME = "Local API";

	private static final Byte OPCODE_PING = 0x9; 
	private static final Byte OPCODE_PONG = 0xA; 
	private static final Byte OPCODE_ACK = 0xB; 
	private static final Byte OPCODE_CONTROL_SIGNAL = 0xC;
	private static int intiateCount = 0;

	private static final String WEBSOCKET_PATH = "/v2/control/socket";

	private WebSocketServerHandshaker handshaker;

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{
		LoggingService.logInfo(MODULE_NAME,"In ControlWebsocketHandler : handle");
		LoggingService.logInfo(MODULE_NAME,"Handshake start.... ");

		String uri = req.getUri();
		uri = uri.substring(1);
		String[] tokens = uri.split("/");
		String publisherId = tokens[4].trim();
		LoggingService.logInfo(MODULE_NAME,"Publisher Id: "+ publisherId);

		synchronized (this) {
			Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
			controlMap.put(publisherId, ctx);
		}

		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			LoggingService.logInfo(MODULE_NAME,"In handshake = null...");
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			LoggingService.logInfo(MODULE_NAME,"In handshake else.....");
			handshaker.handshake(ctx.channel(), req);
		}

		LoggingService.logInfo(MODULE_NAME,"Handshake end....");

		//Code for testing - To be removed later - start
		if(publisherId.equals("viewer")){
			LoggingService.logInfo(MODULE_NAME,"Initiating the control signal...");
			initiateControlSignal();
		}
		//Code for testing - end
	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		if (frame instanceof PingWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handleWebSocketFrame.....  PongWebSocketFrame... " );
			ByteBuf buffer = frame.content();
			Byte opcode = buffer.readByte(); 
			if(opcode == OPCODE_PING.intValue()){
				if(hasContextInMap(ctx)){
					ByteBuf buffer1 = Unpooled.buffer(126);
					buffer1.writeByte(OPCODE_PONG.intValue());
					ctx.channel().write(new PongWebSocketFrame(buffer1));
				}
			}		
		}

		if (frame instanceof TextWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handleWebSocketFrame.....  TextWebSocketFrame... " );
			ByteBuf buffer2 = frame.content();
			Byte opcode = buffer2.readByte(); 
			LoggingService.logInfo(MODULE_NAME,"OPCODE Acknowledgment: " + opcode);
			if(opcode != OPCODE_ACK.intValue()){
				if(intiateCount < 10){
					initiateControlSignal();
				}else{
					removeContextFromMap(ctx);
				}
			}else{
				LoggingService.logInfo(MODULE_NAME,"Acknowledgement received...");
				intiateCount = 0;
			}
			return;
		}

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handleWebSocketFrame..... CloseWebSocketFrame... " + ctx);
			ctx.channel().close();
			removeContextFromMap(ctx);
			return;
		}
	}

	private void removeContextFromMap(ChannelHandlerContext ctx){
		Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
		for (Iterator<Map.Entry<String,ChannelHandlerContext>> it = controlMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String,ChannelHandlerContext> e = it.next();
			if (ctx.equals(e.getValue())) {
				it.remove();
			}
		}
	}

	private boolean hasContextInMap(ChannelHandlerContext ctx){
		Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
		for (Iterator<Map.Entry<String,ChannelHandlerContext>> it = controlMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String,ChannelHandlerContext> e = it.next();
			if (ctx.equals(e.getValue())) {
				LoggingService.logInfo(MODULE_NAME,"Context found in map...");
				return true;
			}
		}
		LoggingService.logInfo(MODULE_NAME,"Context not found in map...");
		return false;
	}

	public void initiateControlSignal(){
		LoggingService.logInfo(MODULE_NAME,"In ControlWebsocketHandler : initiateControlSignal");
		intiateCount++;
		LoggingService.logInfo(MODULE_NAME,"Count   " + intiateCount);
		//Receive control signals from field agent module
		ChannelHandlerContext ctx = null;
		String containerChangedId = "viewer";
		Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
		
		if(controlMap.containsKey(containerChangedId)){
			LoggingService.logInfo(MODULE_NAME,"Found container id in map...");
			ctx = controlMap.get(containerChangedId);
			ByteBuf buffer1 = Unpooled.buffer(126);
			buffer1.writeByte(OPCODE_CONTROL_SIGNAL);
			ctx.channel().write(new TextWebSocketFrame(buffer1));
		}

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
