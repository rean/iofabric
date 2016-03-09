package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.bouncycastle.util.Arrays;

import com.iotracks.iofabric.message_bus.Message;
import com.iotracks.iofabric.utils.BytesUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;

public class MessageWebsocketHandler {

	private static final Byte OPCODE_PING = 0x9; 
	private static final Byte OPCODE_PONG = 0xA; 
	private static final Byte OPCODE_ACK = 0xB; 
	private static final Byte OPCODE_MSG = 0xD;
	private static final Byte OPCODE_RECEIPT = 0xE;

	private int tryCount = 0;

	private static final String WEBSOCKET_PATH = "/v2/message/socket";

	private WebSocketServerHandshaker handshaker;

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{
		System.out.println("In MessageWebsocketHandler : handle");
		System.out.println("Handshake start.... ");

		String uri = req.getUri();
		uri = uri.substring(1);
		String[] tokens = uri.split("/");
		String publisherId = tokens[4].trim();
		System.out.println("Publisher Id: "+ publisherId);

		synchronized (this) {
			Hashtable<String, ChannelHandlerContext> controlMap = WebSocketMap.controlWebsocketMap;
			controlMap.put(publisherId, ctx);
		}

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

		//Code for testing
		if(publisherId.equals("viewer")){
			System.out.println("Initiating the control signal...");
			sendRealTimeMessage();
		}
	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		if (frame instanceof PingWebSocketFrame) {
			System.out.println("In websocket handleWebSocketFrame.....  PongWebSocketFrame... " );
			ByteBuf buffer = frame.content();
			Byte opcode = buffer.getByte(0); 
			System.out.println("OPCPDE: " + opcode);
			if(opcode == OPCODE_PING){
				if(hasContextInMap(ctx)){
					ByteBuf buffer1 = Unpooled.buffer(126);
					buffer1.writeByte(OPCODE_PONG);
					ctx.channel().write(new PongWebSocketFrame(buffer1));
				}
			}		
		}

		if (!(frame instanceof TextWebSocketFrame)) {
			System.out.println("In websocket handleWebSocketFrame.....  TextWebSocketFrame... " );
			ByteBuf input = frame.content();
			if (!input.isReadable()) {
				return;
			}

			byte[] byteArray = input.array();
			int opcode = BytesUtil.bytesToInteger(Arrays.copyOfRange(byteArray, 0, 0));
			int length = BytesUtil.bytesToInteger(Arrays.copyOfRange(byteArray, 1, 4));
			//Only as find in the length
			//To check for if length is greater then what to do.

			Message message = new Message(Arrays.copyOfRange(byteArray, 5, length));

			System.out.println("OPCODE: " + opcode);
			System.out.println(OPCODE_MSG.intValue());
			if(opcode == OPCODE_MSG.intValue()){
				if(hasContextInMap(ctx)){
					//put message on message bus
					System.out.println("Message sent on message bus..");
					//get id and timestamp from bus 
					String messageId = message.getId();
					Long msgTimestamp = message.getTimestamp();
					ByteBuf buffer1 = Unpooled.buffer(126);
					buffer1.writeByte(OPCODE_RECEIPT);
					buffer1.writeBytes(messageId.getBytes());
					buffer1.writeLong(msgTimestamp);
					ctx.channel().write(new TextWebSocketFrame(buffer1));
				}
			}

			return;
		}

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			System.out.println("In websocket handleWebSocketFrame..... CloseWebSocketFrame... " + ctx);
			ctx.channel().close();
			removeContextFromMap(ctx);
			return;
		}
	}

	private void removeContextFromMap(ChannelHandlerContext ctx){
		Hashtable<String, ChannelHandlerContext> messageMap = WebSocketMap.messageWebsocketMap;
		for (Iterator<Map.Entry<String,ChannelHandlerContext>> it = messageMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String,ChannelHandlerContext> e = it.next();
			if (ctx.equals(e.getValue())) {
				it.remove();
			}
		}
	}

	private boolean hasContextInMap(ChannelHandlerContext ctx){
		Hashtable<String, ChannelHandlerContext> messageMap = WebSocketMap.controlWebsocketMap;
		for (Iterator<Map.Entry<String,ChannelHandlerContext>> it = messageMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String,ChannelHandlerContext> e = it.next();
			if (ctx.equals(e.getValue())) {
				System.out.println("Context found in map...");
				return true;
			}
		}
		System.out.println("Context not found in map...");
		return false;
	}

	public void sendRealTimeMessage(){
		System.out.println("In MessageWebsocketHandler : sendRealTimeMessage");
		tryCount++;
		System.out.println("Count" + tryCount);
		//Receive control signals from field agent module
		ChannelHandlerContext ctx = null;
		String containerChangedId = "viewer";
		Hashtable<String, ChannelHandlerContext> messageMap = WebSocketMap.messageWebsocketMap;

		if(messageMap.containsKey(containerChangedId)){
			System.out.println("Found container id in map...");
			ctx = messageMap.get(containerChangedId);
			ByteBuf buffer1 = Unpooled.buffer(126);
			//buffer1.writeByte(OPCODE_CONTROL_SIGNAL);
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
