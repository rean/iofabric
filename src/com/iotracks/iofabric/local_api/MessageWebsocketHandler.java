package com.iotracks.iofabric.local_api;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;

import java.util.Hashtable;

import com.iotracks.iofabric.message_bus.Message;
import com.iotracks.iofabric.message_bus.MessageBus;
import com.iotracks.iofabric.utils.BytesUtil;
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

public class MessageWebsocketHandler {

	private static final Byte OPCODE_PING = 0x9; 
	private static final Byte OPCODE_PONG = 0xA; 
	private static final Byte OPCODE_ACK = 0xB; 
	private static final Byte OPCODE_MSG = 0xD;
	private static final Byte OPCODE_RECEIPT = 0xE;

	private final String MODULE_NAME = "Local API";
	private static int userCount = 0;
	private static final String WEBSOCKET_PATH = "/v2/message/socket";

	private WebSocketServerHandshaker handshaker;

	public void handle(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception{

		LoggingService.logInfo(MODULE_NAME,"In message websocket handler : handle");
		LoggingService.logInfo(MODULE_NAME,"Handshake start.... ");

		String uri = req.getUri();
		uri = uri.substring(1);
		String[] tokens = uri.split("/");

		if(tokens.length < 5){
			LoggingService.logWarning(MODULE_NAME, " Missing ID or ID value in URL " );
			return;
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
			LoggingService.logInfo(MODULE_NAME,"In handshake initation");
			WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
		} else {
			LoggingService.logInfo(MODULE_NAME,"In handshake retrieval");
			handshaker.handshake(ctx.channel(), req);
		}

		LoggingService.logInfo(MODULE_NAME,"Handshake end....");
		return;
	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

		if (frame instanceof PingWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handle - webSocket frame: Ping frame" );
			ByteBuf buffer = frame.content();
			if (buffer.readableBytes() == 1) {
				Byte opcode = buffer.readByte();  
				if(opcode == OPCODE_PING.intValue()){
					if(WebsocketUtil.hasContextInMap(ctx, WebSocketMap.messageWebsocketMap)){
						ByteBuf buffer1 = ctx.alloc().buffer();
						buffer1.writeByte(OPCODE_PONG.intValue());
						LoggingService.logInfo(MODULE_NAME,"Pong frame send in response to ping" );
						ctx.channel().write(new PongWebSocketFrame(buffer1));
					}
				}
			}else{
				LoggingService.logInfo(MODULE_NAME,"Ping opcode not found" );		
			}

			return;
		}

		if (frame instanceof BinaryWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handle websocket frame: message frame" );
			ByteBuf input = frame.content();
			if (!input.isReadable()) {
				return;
			}

			byte[] byteArray = new byte[input.readableBytes()];
			int readerIndex = input.readerIndex();
			input.getBytes(readerIndex, byteArray);

			if(byteArray.length >= 2){
				Byte opcode = byteArray[0];
				if(opcode == OPCODE_MSG.intValue()){
					Message message = null;

					if(WebsocketUtil.hasContextInMap(ctx, WebSocketMap.messageWebsocketMap)){

						int totalMsgLength = BytesUtil.bytesToInteger(BytesUtil.copyOfRange(byteArray, 1, 5)); 
						try {
							message = new Message(BytesUtil.copyOfRange(byteArray, 5, totalMsgLength));
							LoggingService.logInfo(MODULE_NAME,message.toString());
						} catch (Exception e) {
							LoggingService.logInfo(MODULE_NAME,"wrong message format  " + e.getMessage());
							LoggingService.logInfo(MODULE_NAME,"Validation fail");
						}

						LoggingService.logInfo(MODULE_NAME,"Right message format");
						LoggingService.logInfo(MODULE_NAME,"Validation successful");

						//To be removed - For testing
//						System.out.println("usercount" + userCount);
						if(userCount == 0){
//							System.out.println("Sending message back to the container viewer1 start.......");
							sendRealTimeMessage( "viewer",  message);
//							userCount++;
							return;
					}

//						userCount++;
						//To be removed - For testing

						MessageBus messageBus = MessageBus.getInstance();
						Message messageWithId = messageBus.publishMessage(message);

						String messageId = messageWithId.getId();
						Long msgTimestamp = messageWithId.getTimestamp();
						ByteBuf buffer1 = ctx.alloc().buffer();

						buffer1.writeByte(OPCODE_RECEIPT.intValue());

						//send Length
						int msgIdLength = messageId.length();
						buffer1.writeByte(msgIdLength); 
						buffer1.writeByte(Long.BYTES); 

						//Send opcode, id and timestamp
						buffer1.writeBytes(messageId.getBytes()); 
						buffer1.writeBytes(BytesUtil.longToBytes(msgTimestamp));
						LoggingService.logInfo(MODULE_NAME,"Message Sent complete");
						LoggingService.logInfo(MODULE_NAME,"Sending Receipt....");

						ctx.channel().write(new BinaryWebSocketFrame(buffer1));
					}
					return;
				}
			}
		}

		if (frame instanceof BinaryWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handle - websocket frame: binary acknowledgment frame" );
			ByteBuf input = frame.content();
			Byte opcode = input.readByte(); 
			if(opcode == OPCODE_ACK.intValue()){
				LoggingService.logInfo(MODULE_NAME,"Received acknowledgment for the message sent successfully");
				WebSocketMap.unackMessageSendingMap.remove(ctx);
			}

			return;
		}

		// Check for closing frame
		if (frame instanceof CloseWebSocketFrame) {
			LoggingService.logInfo(MODULE_NAME,"In websocket handle websocket frame : Close websocket frame ");
			ctx.channel().close();
			WebsocketUtil.removeWebsocketContextFromMap(ctx, WebSocketMap.messageWebsocketMap);
			return;
		}
	}

	public void sendRealTimeMessage(String receiverId, Message message){
		LoggingService.logInfo(MODULE_NAME,"In Message Websocket : sending real-time message");
		ChannelHandlerContext ctx = null;
		Hashtable<String, ChannelHandlerContext> messageSocketMap = WebSocketMap.messageWebsocketMap;

		if(messageSocketMap!=null && messageSocketMap.containsKey(receiverId)){
			LoggingService.logInfo(MODULE_NAME,"Active real-time websocket found");
			ctx = messageSocketMap.get(receiverId);
			WebSocketMap.unackMessageSendingMap.put(ctx, new MessageSendContextCount(message, 1));
			ByteBuf buffer1 = ctx.alloc().buffer();

			//Send Opcode
			buffer1.writeByte(OPCODE_MSG);
			int totalMsgLength = 0;

			byte[] bytesMsg = null;
			try {
				bytesMsg = message.getBytes();
			} catch (Exception e) {
				LoggingService.logInfo(MODULE_NAME, "Problem in retrieving the message");
			}
			
			totalMsgLength = bytesMsg.length;
			//Total Length
			System.out.println("Total message length: "+ totalMsgLength);
			buffer1.writeBytes(BytesUtil.integerToBytes(totalMsgLength));
			//Message
			buffer1.writeBytes(bytesMsg);
			ctx.channel().write(new BinaryWebSocketFrame(buffer1));
		}else{
			LoggingService.logInfo(MODULE_NAME, "No active real-time websocket found for "+ receiverId);
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