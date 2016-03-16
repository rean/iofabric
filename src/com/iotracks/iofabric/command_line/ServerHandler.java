package com.iotracks.iofabric.command_line;

import java.nio.charset.StandardCharsets;

import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	
	public ServerHandler() {
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        String command = "";
        while (in.isReadable()) {
            command += (char) in.readByte();
        }
        
        LoggingService.logInfo("UNIX", "COMMAND RECIEVED: " + command);
        String result = CommandLineParser.parse(command);

		ByteBuf response = ctx.alloc().buffer();
		response.writeBytes(result.getBytes(StandardCharsets.US_ASCII));
		final ChannelFuture f = ctx.writeAndFlush(response);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                LoggingService.logInfo("UNIX", "CLOSING CHANNEL");
                ctx.close();
            }
        });
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		LoggingService.logInfo("UNIX", "CLIENT CONNECTED");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
