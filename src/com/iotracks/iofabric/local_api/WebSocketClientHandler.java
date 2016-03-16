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

		ByteBuf buffer1 = Unpooled.buffer(126);
		buffer1.writeByte(OPCODE_MSG);

		//Actual Message
		short version = 4;//version
		String id = " "; //id
		String tag = "Bosch Camera"; //tag
		String messageGroupId = ""; //messageGroupId
		Integer seqNum = 1; //sequence number
		Integer seqTot = 1; //sequence total
		Integer priority = 0; //priority 
		Long timestamp = (long)0; //timestamp
		String publisher = "viewer"; //publisher
		String authid = ""; //authid
		String authGroup = ""; //auth group
		Integer chainPos = 0; //chain position
		String hash = "";  //hash
		String prevHash = ""; //previous hash
		String nounce = "";  //nounce
		Integer diffTarget = 0;//difficultytarget
		String infotype = "image/jpeg"; //infotype
		String infoformat = "base64"; //infoformat
		String contextData = "";
		String contentData = "sdkjhwrtiy8wrtgSDFOiuhsrgowh4touwsdhsDFDSKJhsdkljasjklweklfjwhefiauhw98p328";

		/*****************************************************************************************************/
		//Version
		buffer1.writeBytes(BytesUtil.shortToBytes(version)); //version

		//Length
		buffer1.writeByte(id.getBytes().length); //id

		byte[] tagLength = new byte[2];	//tag
		for (int i = 0; i < 2; ++i) {
			tagLength[i] = (byte) (tag.getBytes().length >> (2 - i - 1 << 3));
		}
		buffer1.writeBytes(tagLength);
		buffer1.writeByte(messageGroupId.getBytes().length); //messageGroupId
		buffer1.writeByte(seqNum.toString().getBytes().length); //sequence number
		buffer1.writeByte(seqTot.toString().getBytes().length); //sequence total
		buffer1.writeByte(priority.toString().getBytes().length); //priority
		buffer1.writeByte(timestamp.toString().getBytes().length); //timestamp
		buffer1.writeByte(publisher.getBytes().length); //publisher

		byte[] authIdLength = new byte[2];	//authid
		for (int i = 0; i < 2; ++i) {
			authIdLength[i] = (byte) (authid.toString().getBytes().length >> (2 - i - 1 << 3));
		}
		buffer1.writeBytes(authIdLength);

		byte[] authGrpLength = new byte[2];	//auth group
		for (int i = 0; i < 2; ++i) {
			authGrpLength[i] = (byte) (authGroup.toString().getBytes().length >> (2 - i - 1 << 3));
		}
		buffer1.writeBytes(authGrpLength);

		buffer1.writeByte(chainPos.toString().getBytes().length); //chain position

		byte[] hashLength = new byte[2];	//hash
		for (int i = 0; i < 2; ++i) {
			hashLength[i] = (byte) (hash.toString().getBytes().length >> (2 - i - 1 << 3));
		}
		buffer1.writeBytes(hashLength);

		byte[] prevHashLength = new byte[2];	//hash previous
		for (int i = 0; i < 2; ++i) {
			prevHashLength[i] = (byte) (prevHash.toString().getBytes().length >> (2 - i - 1 << 3));
		}
		buffer1.writeBytes(prevHashLength);

		byte[] nounceLength = new byte[2];	//nounce
		for (int i = 0; i < 2; ++i) {
			nounceLength[i] = (byte) (nounce.toString().getBytes().length >> (2 - i - 1 << 3));
		}
		buffer1.writeBytes(nounceLength);

		buffer1.writeByte(diffTarget.toString().getBytes().length); //difficultytarget
		buffer1.writeByte(infotype.getBytes().length); //infotype
		buffer1.writeByte(infoformat.getBytes().length); //infoformat

		byte[] contextdataLength = new byte[4];	//contextdata
		for (int i = 0; i < 4; ++i) {
			contextdataLength[i] = (byte) (contextData.getBytes().length >> (4 - i - 1 << 3));
		}
		buffer1.writeBytes(contextdataLength);

		byte[] contentdataLength = new byte[4];	//contentdata
		for (int i = 0; i < 4; ++i) {
			contentdataLength[i] = (byte) (contentData.getBytes().length >> (4 - i - 1 << 3));
		}
		buffer1.writeBytes(contentdataLength);

		/********************************************************************/
		// Message to byets conversion
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