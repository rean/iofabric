package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

public class ControlWebsocketHandler {
	private final String MODULE_NAME = "Local API";

	private static final Byte OPCODE_PING = 0x9; 
	private static final Byte OPCODE_PONG = 0xA; 
	private static final Byte OPCODE_ACK = 0xB; 
	private static final Byte OPCODE_CONTROL_SIGNAL = 0xC;

	private static final String WEBSOCKET_PATH = "/v2/control/socket";

	private WebSocketServerHandshaker handshaker;

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req){
		LoggingService.logInfo(MODULE_NAME,"In ControlWebsocketHandler : handle");
		LoggingService.logInfo(MODULE_NAME,"Handshake start.... ");

		String uri = req.getUri();
		uri = uri.substring(1);
		String[] tokens = uri.split("/");

		if(tokens.length < 5){
			LoggingService.logInfo(MODULE_NAME, " Id or Id value not found in the URL " );
		}else {
			String id = tokens[4].trim();
			LoggingService.logInfo(MODULE_NAME,"Receiver Id: "+ id);
			Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
			controlMap.put(id, ctx);
		}

		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			LoggingService.logInfo(MODULE_NAME,"In handshake = null...");
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			LoggingService.logInfo(MODULE_NAME,"In handshake else...");
			handshaker.handshake(ctx.channel(), req);
		}

		LoggingService.logInfo(MODULE_NAME,"Handshake end....");

		//Code for testing - To be removed later - start
		//LoggingService.logInfo(MODULE_NAME,"Initiating the control signal...");
		//initiateControlSignal();
		//Code for testing - end
	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		if (frame instanceof PingWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handleWebSocketFrame: Ping Frame" );
			ByteBuf buffer = frame.content();
			if (buffer.readableBytes() == 1) {
				Byte opcode = buffer.readByte(); 
				if(opcode == OPCODE_PING.intValue()){
					if(hasContextInMap(ctx)){
						ByteBuf buffer1 = ctx.alloc().buffer();
						buffer1.writeByte(OPCODE_PONG.intValue());
						LoggingService.logInfo(MODULE_NAME,"Pong frame send to the container" );
						ctx.channel().write(new PongWebSocketFrame(buffer1));
					}
				}
			}else{
				LoggingService.logInfo(MODULE_NAME,"Ping opcode not found" );		
			}

			return;
		}

		if (frame instanceof BinaryWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handleWebSocketFrame: Text Frame " );
			ByteBuf buffer2 = frame.content();
			if (buffer2.readableBytes() == 1) {
				Byte opcode = buffer2.readByte(); 
				LoggingService.logInfo(MODULE_NAME,"OPCODE Acknowledgment: " + opcode);
				if(opcode == OPCODE_ACK.intValue()){
					WebSocketMap.controlSignalSendContextMap.remove(ctx);
					LoggingService.logInfo(MODULE_NAME,"Acknowledgement received... Done");
					return;
				}
			}
			
			int tryCount = WebSocketMap.controlSignalSendContextMap.get(ctx);
			if(tryCount < 10){
				LoggingService.logInfo(MODULE_NAME,"Acknowledgment not received : Initiating control signal");
				initiateControlSignal();
			}else{
				LoggingService.logInfo(MODULE_NAME,"Acknowledgment not received :  Initiating control signal expires");
				LoggingService.logInfo(MODULE_NAME,"Removed stored context for real time messaging");
				removeContextFromMap(ctx);
				return;
			}
			LoggingService.logInfo(MODULE_NAME,"Acknowledgement opcode not found" );		

			return;
		}

		if (frame instanceof CloseWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handleWebSocketFrame : CloseWebSocketFrame " + ctx);
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
		//Receive control signals from field agent module
		ChannelHandlerContext ctx = null;
		String containerChangedId = "viewer";
		Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;

		if(controlMap.containsKey(containerChangedId)){
			LoggingService.logInfo(MODULE_NAME,"Found container id in map...");
			if(WebSocketMap.controlSignalSendContextMap.contains(ctx)){
				int tryCount = WebSocketMap.controlSignalSendContextMap.get(ctx);
				tryCount = tryCount+1;
				WebSocketMap.controlSignalSendContextMap.put(ctx, tryCount);
			}else{
				WebSocketMap.controlSignalSendContextMap.put(ctx, 1);
			}
			ctx = controlMap.get(containerChangedId);
			ByteBuf buffer1 = ctx.alloc().buffer();
			buffer1.writeByte(OPCODE_CONTROL_SIGNAL);
			ctx.channel().write(new BinaryWebSocketFrame(buffer1));
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