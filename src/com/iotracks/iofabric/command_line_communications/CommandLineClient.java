package com.iotracks.iofabric.command_line_communications;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.UnixChannel;

public class CommandLineClient implements Runnable {
	private final ClientHandler clientHandler = new ClientHandler();
	private boolean isRunning = false;
	private ExecutorService executor = null;
	private volatile Boolean connected;
	private volatile boolean done;

	public synchronized boolean startClient() {
		if (!isRunning) {
			done = false;
			connected = false;
			isRunning = true;
			executor = Executors.newFixedThreadPool(1);
			executor.execute(this);
			while (!done)
				;
		}
		return connected;
	}

	public synchronized boolean stopClient() {
		boolean bReturn = true;
		if (isRunning) {
			if (executor != null) {
				executor.shutdown();
				try {
					executor.shutdownNow();
					if (executor.awaitTermination(calcTime(10, 0.66667), TimeUnit.SECONDS)) {
						if (!executor.awaitTermination(calcTime(10, 0.33334), TimeUnit.SECONDS)) {
							bReturn = false;
						}
					}
				} catch (InterruptedException ie) {
					executor.shutdownNow();
					Thread.currentThread().interrupt();
				} finally {
					executor = null;
				}
			}
			isRunning = false;
			connected = false;
			done = false;
		}
		return bReturn;
	}

	private long calcTime(int nTime, double dValue) {
		return (long) ((double) nTime * dValue);
	}

	public boolean sendMessage(String msg) {
		return clientHandler.sendMessage(msg);
	}

	@Override
	public void run() {
		EventLoopGroup workerGroup = new EpollEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap().group(workerGroup).channel(EpollDomainSocketChannel.class)
					.handler(new ChannelInitializer<UnixChannel>() {
						@Override
						public void initChannel(UnixChannel ch) throws Exception {
							ch.pipeline().addFirst(clientHandler);
						}
					});

			ChannelFuture f = b.connect(new DomainSocketAddress("/var/run/iofabric/iofabric.sock")).sync();
			connected = true;
			done = true;

			f.channel().closeFuture().sync();

		} catch (Exception e) {
			connected = false;
			done = true;
		} finally {
			workerGroup.shutdownGracefully();
		}
		done = true;
	}
}
