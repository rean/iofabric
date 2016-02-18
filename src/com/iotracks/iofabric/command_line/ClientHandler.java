package com.iotracks.iofabric.command_line;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {
	private ChannelHandlerContext ctx;

	public ClientHandler() {
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		ByteBuf in = (ByteBuf) msg;

		String text = "";
		while (in.isReadable()) {
			text += (char) in.readByte();
		}

		System.out.print(text);
		System.out.flush();
		in.release();
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

	public boolean sendMessage(String message) {
		if (ctx != null) {
            ChannelFuture cf = ctx.write(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8));
            ctx.flush();
            if (!cf.isSuccess()) {
                return false;
            }
        } else {
        	return false;
        }
		return true;
	}
}
