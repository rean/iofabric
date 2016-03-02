package com.iotracks.iofabric.local_api;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class LocalApiServer {

	private final int PORT = 54321;
	public LocalApiServer(){

	}

	public static void main(String[] args) throws Exception{
		LocalApiServer server = new LocalApiServer();
		server.start();
	}

	public void start() throws Exception{
		System.out.println("Server start...");
		 // Configure the server.
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));
        
        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new LocalApiServerPipelineFactory());

        // Bind and start to accept incoming connections.
        bootstrap.bind(new InetSocketAddress(PORT));
        System.out.println("Server end...");
	}
}
