package com.iotracks.iofabric.local_api;

import org.bouncycastle.util.Arrays;

import com.iotracks.iofabric.utils.BytesUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object>{

	private static final Byte OPCODE_PING = 0x9; 
	private static final Byte OPCODE_PONG = 0xA; 
	private static final Byte OPCODE_ACK = 0xB; 
	private static final Byte OPCODE_MSG = 0xD;
	private static final Byte OPCODE_CONTROL_SIGNAL = 0xC;
	private static final Byte OPCODE_RECEIPT = 0xE;

	private final WebSocketClientHandshaker handshaker;
	private ChannelPromise handshakeFuture;

	public WebSocketClientHandler(WebSocketClientHandshaker handshaker) {
		this.handshaker = handshaker;
	}

	public ChannelFuture handshakeFuture() {
		return handshakeFuture;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		handshakeFuture = ctx.newPromise();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		handshaker.handshake(ctx.channel());
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		System.out.println("WebSocket Client disconnected!");
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("client channelRead0 "+ctx);
		Channel ch = ctx.channel();
		if (!handshaker.isHandshakeComplete()) {
			handshaker.finishHandshake(ch, (FullHttpResponse) msg);
			System.out.println("WebSocket Client connected!");
			handshakeFuture.setSuccess();
		}

		if(msg instanceof WebSocketFrame){
			WebSocketFrame frame = (WebSocketFrame)msg;
			if(frame instanceof BinaryWebSocketFrame){
				handleWebSocketFrame(ctx,  frame);
			}
			return;
		}
		sendRealTimeMessageTest(ctx);
		return;

	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		System.out.println("In client handleWebSocketFrame.....");
		if (frame instanceof BinaryWebSocketFrame) {
			System.out.println("In websocket client.....  TextWebSocketFrame... " );
			ByteBuf input = frame.content();
			if (!input.isReadable()) {
				return;
			}

			byte[] byteArray = new byte[input.readableBytes()];
			int readerIndex = input.readerIndex();
			input.getBytes(readerIndex, byteArray);

			Byte opcode = byteArray[0];
			System.out.println("Opcode: " + opcode);
			if(opcode.intValue() == OPCODE_RECEIPT){
				int size = byteArray[1];
				int pos = 3;
				if (size > 0) {
					String messageId = BytesUtil.bytesToString(Arrays.copyOfRange(byteArray, pos, pos + size));
					System.out.println("Message Id: " + messageId + "\n");
					pos += size;
				}

				size = byteArray[2];
				if (size > 0) {
					long timeStamp = BytesUtil.bytesToLong(Arrays.copyOfRange(byteArray, pos, pos + size));
					System.out.println("Timestamp: " + timeStamp + "\n");
					pos += size;
				}
				
				System.out.println("Receipt received.. Message send complete");
			}
		}
		return;

	}

	private void readReceipt(){

	}

	public void sendRealTimeMessageTest(ChannelHandlerContext ctx){
		System.out.println("In clienttest : sendRealTimeMessageTest");

		ByteBuf buffer1 = Unpooled.buffer(126);
		buffer1.writeByte(OPCODE_MSG);

		//Actual Message
		short version = 4;//version
		String id = " "; //id
		String tag = "Bosch Camera 8798797"; //tag
		String messageGroupId = "group1"; //messageGroupId
		Integer seqNum = 1; //sequence number
		Integer seqTot = 1; //sequence total
		Integer priority = 5; //priority 
		Long timestamp = (long)0; //timestamp
		String publisher = "viewer"; //publisher
		String authid = "auth"; //authid
		String authGroup = "authgrp"; //auth group
		Integer chainPos = 10; //chain position
		String hash = "";  //hash
		String prevHash = ""; //previous hash
		String nounce = "";  //nounce
		Integer diffTarget = 30;//difficultytarget
		String infotype = "image/jpeg"; //infotype
		String infoformat = "base64"; //infoformat
		String contextData = "gghh";
		String contentData = "sdkjhwrtiy8wrtgSDFOiuhsrgowh4touwsdhsDFDSKJhsdkljasjklweklfjwhefiauhw98p328";

		/*****************************************************************************************************/
		//Version
		buffer1.writeBytes(BytesUtil.shortToBytes(version)); //version

		//Length
		buffer1.writeByte(id.getBytes().length); //id
		buffer1.writeBytes(BytesUtil.shortToBytes((short)tag.getBytes().length)); //tag
		buffer1.writeByte(messageGroupId.getBytes().length); //messageGroupId
		buffer1.writeByte(BytesUtil.integerToBytes(seqNum).length); //sequence number
		buffer1.writeByte(BytesUtil.integerToBytes(seqTot).length); //sequence total
		buffer1.writeByte(BytesUtil.integerToBytes(priority).length); //priority
		buffer1.writeByte(BytesUtil.longToBytes(timestamp).length); //timestamp
		buffer1.writeByte(publisher.getBytes().length); //publisher
		buffer1.writeBytes(BytesUtil.shortToBytes((short)authid.getBytes().length)); //auth id
		buffer1.writeBytes(BytesUtil.shortToBytes((short)authGroup.getBytes().length)); //auth group
		buffer1.writeByte(BytesUtil.integerToBytes(chainPos).length); //chain position
		buffer1.writeBytes(BytesUtil.shortToBytes((short)hash.getBytes().length)); //hash
		buffer1.writeBytes(BytesUtil.shortToBytes((short)prevHash.getBytes().length)); //hash previous
		buffer1.writeBytes(BytesUtil.shortToBytes((short)nounce.getBytes().length)); //nounce
		buffer1.writeByte(BytesUtil.integerToBytes(diffTarget).length); //difficultytarget
		buffer1.writeByte(infotype.getBytes().length); //infotype
		buffer1.writeByte(infoformat.getBytes().length); //infoformat
		buffer1.writeBytes(BytesUtil.integerToBytes(contextData.getBytes().length)); //contextData
		buffer1.writeBytes(BytesUtil.integerToBytes(contentData.getBytes().length)); //contentData

		/********************************************************************/
		// Message to bytes conversion
		buffer1.writeBytes(BytesUtil.stringToBytes(id)); //id
		buffer1.writeBytes(BytesUtil.stringToBytes(tag)); //tag
		buffer1.writeBytes(BytesUtil.stringToBytes(messageGroupId)); //messageGroupId
		buffer1.writeBytes(BytesUtil.integerToBytes(seqNum)); //sequence number
		buffer1.writeBytes(BytesUtil.integerToBytes(seqTot)); //sequence total
		buffer1.writeBytes(BytesUtil.integerToBytes(priority)); //priority
		buffer1.writeBytes(BytesUtil.longToBytes(timestamp)); //timestamp
		buffer1.writeBytes(BytesUtil.stringToBytes(publisher)); //publisher
		buffer1.writeBytes(BytesUtil.stringToBytes(authid)); //authid
		buffer1.writeBytes(BytesUtil.stringToBytes(authGroup)); //auth group
		buffer1.writeBytes(BytesUtil.integerToBytes(chainPos)); //chain position
		buffer1.writeBytes(BytesUtil.stringToBytes(hash)); //hash
		buffer1.writeBytes(BytesUtil.stringToBytes(prevHash)); //previous hash
		buffer1.writeBytes(BytesUtil.stringToBytes(nounce)); //nounce
		buffer1.writeBytes(BytesUtil.integerToBytes(diffTarget)); //difficultytarget
		buffer1.writeBytes(BytesUtil.stringToBytes(infotype)); //infotype
		buffer1.writeBytes(BytesUtil.stringToBytes(infoformat)); //infoformat
		buffer1.writeBytes(BytesUtil.stringToBytes(contextData)); //contextdata
		buffer1.writeBytes(BytesUtil.stringToBytes(contentData)); //contentdata

		ctx.channel().writeAndFlush(new BinaryWebSocketFrame(buffer1));
		System.out.println("sendRealTimeMessageTest : done");
		return;
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		if (!handshakeFuture.isDone()) {
			handshakeFuture.setFailure(cause);
		}
		ctx.close();
	}
}