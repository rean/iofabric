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
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object>{

	private static final Byte OPCODE_PING = 0x9; 
	private static final Byte OPCODE_PONG = 0xA; 
	private static final Byte OPCODE_ACK = 0xB; 
	private static final Byte OPCODE_MSG = 0xD;
	private static final Byte OPCODE_CONTROL_SIGNAL = 0xC;

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
			if(frame instanceof TextWebSocketFrame){
				handleWebSocketFrame(ctx,  frame);
			}
			return;
		}
		//	sendRealTimeMessageTest(ctx);
		return;

	}

	public void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
		System.out.println("In client handleWebSocketFrame.....");
		if (frame instanceof TextWebSocketFrame) {
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
			if(opcode == OPCODE_MSG){
				int length = BytesUtil.bytesToInteger(Arrays.copyOfRange(byteArray, 1, 5));
				String id = BytesUtil.bytesToString(Arrays.copyOfRange(byteArray, 5, byteArray.length));
				System.out.println("Opcode: " + opcode + "  id: " + id + "  length: " + length) ;
				ByteBuf buffer1 = Unpooled.buffer(126);
				buffer1.writeByte(88);
				ctx.channel().writeAndFlush(new TextWebSocketFrame(buffer1));			}
		}
		return;

	}

	public void sendRealTimeMessageTest(ChannelHandlerContext ctx){
		System.out.println("In clienttest : sendRealTimeMessageTest");

		int length = 50;
		ByteBuf buffer1 = Unpooled.buffer(126);
		buffer1.writeByte(OPCODE_MSG);
		buffer1.writeBytes(BytesUtil.integerToBytes(length));

		byte[] msgArray = new byte[256];
		msgArray[5] = 22;
		msgArray[6] = 100;
		msgArray[7] = 111;
		msgArray[8] = 1;
		msgArray[23] = Byte.parseByte("11");
		msgArray[24] = Byte.parseByte("22");
		msgArray[25] = Byte.parseByte("13");
		msgArray[26] = Byte.parseByte("23");
		msgArray[27] = Byte.parseByte("33");
		msgArray[28] = Byte.parseByte("43");
		msgArray[29] = Byte.parseByte("14");
		msgArray[30] = Byte.parseByte("24");
		msgArray[31] = Byte.parseByte("34");
		msgArray[32] = Byte.parseByte("44");

		buffer1.writeBytes(msgArray);

		ctx.channel().writeAndFlush(new TextWebSocketFrame(buffer1));
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
