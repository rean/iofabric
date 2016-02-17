package com.iotracks.iofabric.command_line_communications;

import java.util.logging.Level;

import com.iotracks.iofabric.utils.CommandLineParser;
import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {
	
	private final LoggingService logger;
	
	public ServerHandler(LoggingService logger) {
		this.logger = logger;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        String command = "";
        while (in.isReadable()) {
            command += (char) in.readByte();
        }
        
		logger.log(Level.INFO, "UNIX", "COMMAND RECIEVED: " + command);
        String result = CommandLineParser.parse(command);

        ByteBuf response = ctx.alloc().buffer();
		response.writeBytes(result.getBytes());
		final ChannelFuture f = ctx.writeAndFlush(response);
        f.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                logger.log(Level.INFO, "UNIX", "CLOSING CHANNEL");
                ctx.close();
            }
        });
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		logger.log(Level.INFO, "UNIX", "CLIENT CONNECTED");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
