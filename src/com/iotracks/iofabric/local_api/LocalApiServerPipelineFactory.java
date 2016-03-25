package com.iotracks.iofabric.local_api;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

public class LocalApiServerPipelineFactory extends ChannelInitializer<SocketChannel>{
	private final SslContext sslCtx;
	private final EventExecutorGroup executor;
	
	public LocalApiServerPipelineFactory(SslContext sslCtx) {
		this.sslCtx = sslCtx;
		this.executor = new DefaultEventExecutorGroup(10);
	}

	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new LocalApiServerHandler(executor));	
	}
}	