package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{
		LoggingService.logInfo(MODULE_NAME,"In control websocket Handler : handle");
		LoggingService.logInfo(MODULE_NAME,"Handshake start.... ");

		String uri = req.getUri();
		uri = uri.substring(1);
		String[] tokens = uri.split("/");

		if(tokens.length < 5){
			LoggingService.logWarning(MODULE_NAME, " Missing ID or ID value in URL " );
			return;
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
			LoggingService.logInfo(MODULE_NAME,"In handshake initation");
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			LoggingService.logInfo(MODULE_NAME,"In handshake retrieval");
			handshaker.handshake(ctx.channel(), req);
		}

		LoggingService.logInfo(MODULE_NAME,"Handshake end....");

		//Code for testing - To be removed later - start
		LoggingService.logInfo(MODULE_NAME,"Initiating the control signal...");
		initiateControlSignal(ctx);
		//Code for testing - end

		return;
	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception{

		if (frame instanceof PingWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handle - Websocket frame: Ping frame" );
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
			LoggingService.logInfo(MODULE_NAME,"In websocket handle - websocket frame: binary acknowledgment frame " );
			ByteBuf buffer2 = frame.content();
			if (buffer2.readableBytes() == 1) {
				Byte opcode = buffer2.readByte(); 
				LoggingService.logInfo(MODULE_NAME,"OPCODE Acknowledgment: " + opcode);
				if(opcode == OPCODE_ACK.intValue()){
					WebSocketMap.controlSignalSendContextMap.remove(ctx);
					LoggingService.logInfo(MODULE_NAME,"Acknowledgement received");
					LoggingService.logInfo(MODULE_NAME,"Control signals send successfully");
					return;
				}
			}
		}

		if (frame instanceof CloseWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handle websocket frame : Close websocket frame ");
			ctx.channel().close();
			removeContextFromMap(ctx);
			return;
		}
	}

	private void initiateUnacknowledgedSignals() throws Exception{
		//TODO: do it on timely basis
		
		for(Map.Entry<ChannelHandlerContext, Integer> contextEntry : WebSocketMap.controlSignalSendContextMap.entrySet()){
			LoggingService.logInfo(MODULE_NAME,"Initiating control signal for unacknowledged signals");
			ChannelHandlerContext ctx = contextEntry.getKey();
			int tryCount = contextEntry.getValue();
			if(tryCount < 10){
				initiateControlSignal(ctx);
			}else{
				LoggingService.logInfo(MODULE_NAME," Initiating control signal expires");
				removeContextFromMap(ctx);
				return;
			}
			LoggingService.logInfo(MODULE_NAME,"Acknowledgement opcode not found" );		

			return;
		}
	}

	private void removeContextFromMap(ChannelHandlerContext ctx) throws Exception{
		Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
		for (Iterator<Map.Entry<String,ChannelHandlerContext>> it = controlMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String,ChannelHandlerContext> e = it.next();
			if (ctx.equals(e.getValue())) {
				LoggingService.logInfo(MODULE_NAME,"Removing real-time control context for the id: " + e.getKey());
				it.remove();
			}
		}
	}

	private boolean hasContextInMap(ChannelHandlerContext ctx) throws Exception{
		Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
		for (Iterator<Map.Entry<String,ChannelHandlerContext>> it = controlMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String,ChannelHandlerContext> e = it.next();
			if (ctx.equals(e.getValue())) {
				LoggingService.logInfo(MODULE_NAME,"Context found as real-time control socket");
				return true;
			}
		}
		LoggingService.logInfo(MODULE_NAME,"Context found as real-time control socket");
		return false;
	}

	public void initiateControlSignal(Map<String, String> oldConfigMap, Map<String, String> newConfigMap) throws Exception{
		LoggingService.logInfo(MODULE_NAME,"In control websocket handler : initiating control signals");
		ChannelHandlerContext ctx = null;

		//Compare the old and new config map
		Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
		ArrayList<String> changedConfigElmtsList = new ArrayList<String>();

		for (Map.Entry<String, String> newEntry : newConfigMap.entrySet()) {
			String newMapKey = newEntry.getKey();
			if(!oldConfigMap.containsKey(newMapKey)){
				changedConfigElmtsList.add(newMapKey);
			}else{

				String newConfigValue = newEntry.getValue();
				String oldConfigValue = oldConfigMap.get(newMapKey);
				if(!newConfigValue.equals(oldConfigValue)){
					changedConfigElmtsList.add(newMapKey);
				}
			}
		}

		for(String changedConfigElmtId : changedConfigElmtsList){
			if(controlMap.containsKey(changedConfigElmtId)){
				LoggingService.logInfo(MODULE_NAME,"Found container id in map ");
				ctx = controlMap.get(changedConfigElmtId);
				WebSocketMap.controlSignalSendContextMap.put(ctx, 1);

				ByteBuf buffer1 = ctx.alloc().buffer();
				buffer1.writeByte(OPCODE_CONTROL_SIGNAL);
				ctx.channel().write(new BinaryWebSocketFrame(buffer1));
			}
		}

	}

	private void initiateControlSignal(ChannelHandlerContext ctx) throws Exception{

		int tryCount = WebSocketMap.controlSignalSendContextMap.get(ctx);
		tryCount = tryCount+1;
		WebSocketMap.controlSignalSendContextMap.put(ctx, tryCount);

		ByteBuf buffer1 = ctx.alloc().buffer();
		buffer1.writeByte(OPCODE_CONTROL_SIGNAL);
		ctx.channel().write(new BinaryWebSocketFrame(buffer1));
	}

	private static String getWebSocketLocation(FullHttpRequest req) throws Exception{
		String location =  req.headers().get(HOST) + WEBSOCKET_PATH;
		if (LocalApiServer.SSL) {
			return "wss://" + location;
		} else {
			return "ws://" + location;
		}
	}
}