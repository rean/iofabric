package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.bouncycastle.util.Arrays;

import com.iotracks.iofabric.message_bus.Message;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.utils.BytesUtil;
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

public class MessageWebsocketHandler {

	private static final Byte OPCODE_PING = 0x9; 
	private static final Byte OPCODE_PONG = 0xA; 
	private static final Byte OPCODE_ACK = 0xB; 
	private static final Byte OPCODE_MSG = 0xD;
	private static final Byte OPCODE_RECEIPT = 0xE;

	private final String MODULE_NAME = "Local API";
	private static int tryCount = 0;
	private static Map<ChannelHandlerContext, Message> messageContextMap = new HashMap<ChannelHandlerContext, Message>();
	private static final String WEBSOCKET_PATH = "/v2/message/socket";

	private WebSocketServerHandshaker handshaker;

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{
		LoggingService.logInfo(MODULE_NAME,"In MessageWebsocketHandler : handle");
		LoggingService.logInfo(MODULE_NAME,"Handshake start.... ");

		String uri = req.getUri();
		uri = uri.substring(1);
		String[] tokens = uri.split("/");

		if(tokens.length < 5){
			LoggingService.logInfo(MODULE_NAME, " Id or Id value not found in the URL " );
		}else {
			String publisherId = tokens[4].trim();
			LoggingService.logInfo(MODULE_NAME,"Publisher Id: "+ publisherId);
			Hashtable<String, ChannelHandlerContext> messageMap = WebSocketMap.messageWebsocketMap;
			messageMap.put(publisherId, ctx);
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

		//For testing - start
		//sendRealTimeMessage(ctx);
		//For testing - end
		return;
	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

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

		if (frame instanceof TextWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handleWebSocketFrame: Text Frame" );
			ByteBuf input = frame.content();
			if (!input.isReadable()) {
				return;
			}

			byte[] byteArray = new byte[input.readableBytes()];
			int readerIndex = input.readerIndex();
			input.getBytes(readerIndex, byteArray);

			if(byteArray.length >= 5){
				Byte opcode = byteArray[0];
				if(opcode == OPCODE_MSG.intValue()){
					LoggingService.logInfo(MODULE_NAME,"Opcode: " + opcode);
					
					Message message = null;
					
					try {
						message = new Message(Arrays.copyOfRange(byteArray, 1, byteArray.length));
					} catch (Exception e) {
						LoggingService.logInfo(MODULE_NAME,"wrong message format." + e.getMessage());
					}
					LoggingService.logInfo(MODULE_NAME,"Right message format.");

					if(hasContextInMap(ctx)){
						System.out.println("In context true");

						MessageBus messageBus = MessageBus.getInstance();
						Message messageWithId = messageBus.publishMessage(message);
						LoggingService.logInfo(MODULE_NAME,"Message id: " + messageWithId.getId() + "Message timestamp: " + messageWithId.getTimestamp());

						String messageId = messageWithId.getId();
						Long msgTimestamp = messageWithId.getTimestamp();
						ByteBuf buffer1 = Unpooled.buffer(256);
						buffer1.writeByte(OPCODE_RECEIPT);
						buffer1.writeBytes(BytesUtil.longToBytes(msgTimestamp));
						buffer1.writeBytes(BytesUtil.stringToBytes(messageId));
						ctx.channel().write(new TextWebSocketFrame(buffer1));
					}

					return;
				}
			}else{
				return;
			}
		}

		if (frame instanceof TextWebSocketFrame) {
			ByteBuf input = frame.content();
			Byte opcode = input.readByte(); 
			if(opcode == OPCODE_ACK.intValue()){
				tryCount = 0;
				messageContextMap.remove(ctx);
				LoggingService.logInfo(MODULE_NAME,"Received acknowledment for message sent");
			}else{
				LoggingService.logInfo(MODULE_NAME,"Received no acknowledment... send again");
				if(tryCount < 10){
					sendRealTimeMessage(ctx);
				}else{
					messageContextMap.remove(ctx);
					removeContextFromMap(ctx);
				}
			}
		}

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handleWebSocketFrame..... CloseWebSocketFrame... " + ctx);
			ctx.channel().close();
			removeContextFromMap(ctx);
			return;
		}
	}

	private void saveNotAckMessage(ChannelHandlerContext ctx, Message message){
		messageContextMap.put(ctx, message);
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
		Hashtable<String, ChannelHandlerContext> messageMap = WebSocketMap.messageWebsocketMap;
		for (Iterator<Map.Entry<String,ChannelHandlerContext>> it = messageMap.entrySet().iterator(); it.hasNext();) {
			Map.Entry<String,ChannelHandlerContext> e = it.next();
			if (ctx.equals(e.getValue())) {
				LoggingService.logInfo(MODULE_NAME,"Context found in map...");
				return true;
			}
		}
		LoggingService.logInfo(MODULE_NAME,"Context not found in map...");
		return false;
	}

	public void sendRealTimeMessage(ChannelHandlerContext ctx){
		System.out.println("trycount:  "+ tryCount);
		tryCount++;
		Message message = messageContextMap.get(ctx);
		ByteBuf buffer1 = Unpooled.buffer(126);
		int length = 50;
		buffer1.writeByte(OPCODE_MSG);
		buffer1.writeBytes(BytesUtil.integerToBytes(length));
		buffer1.writeBytes(BytesUtil.stringToBytes("This is my message to send"));
		ctx.channel().write(new TextWebSocketFrame(buffer1));
	}

	public void sendRealTimeMessage(String receiverId, Message message){
		LoggingService.logInfo(MODULE_NAME,"In MessageWebsocketHandler : sendRealTimeMessage");
		tryCount++;
		LoggingService.logInfo(MODULE_NAME,"Count" + tryCount);
		ChannelHandlerContext ctx = null;
		Hashtable<String, ChannelHandlerContext> messageMap = WebSocketMap.messageWebsocketMap;
		int length = 50;
		if(messageMap.containsKey(receiverId)){
			saveNotAckMessage(ctx, message);
			LoggingService.logInfo(MODULE_NAME,"Found container id in map...");
			ctx = messageMap.get(receiverId);
			messageContextMap.put(ctx, message);
			ByteBuf buffer1 = Unpooled.buffer(126);
			buffer1.writeByte(OPCODE_MSG);
			buffer1.writeBytes(BytesUtil.integerToBytes(length));
			buffer1.writeBytes(BytesUtil.stringToBytes("This is my message to send"));
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
