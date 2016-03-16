package com.iotracks.iofabric.local_api;

import org.jboss.netty.handler.codec.http.websocketx.WebSocket00FrameDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;

public class LocalApiServerPipelineFactory extends ChannelInitializer<SocketChannel>{
	private final SslContext sslCtx;

	public LocalApiServerPipelineFactory(SslContext sslCtx) {
		this.sslCtx = sslCtx;
	}

	public void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}
		pipeline.addLast(new HttpServerCodec());
		pipeline.addLast(new HttpObjectAggregator(65536));
		pipeline.addLast(new LocalApiServerHandler());	
	}
}	
