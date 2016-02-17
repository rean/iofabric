package com.iotracks.iofabric.command_line_communications;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.logging.Level;

import com.iotracks.iofabric.utils.logging.LoggingService;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerDomainSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.channel.unix.UnixChannel;

public class CommandLineServer implements Runnable {

	private final LoggingService logger;

	public CommandLineServer(LoggingService logger) {
		this.logger = logger;
	}

	@Override
	public void run() {
		EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
		EventLoopGroup workerGroup = new EpollEventLoopGroup(1);
		try {
			ServerBootstrap b = new ServerBootstrap().group(bossGroup, workerGroup)
					.channel(EpollServerDomainSocketChannel.class).childHandler(new ChannelInitializer<UnixChannel>() {
						@Override
						public void initChannel(UnixChannel ch) throws Exception {
							ch.pipeline().addFirst(new ServerHandler(logger));
						}
					});

			File socketFile = new File("/var/run/iofabric/iofabric.sock");
			if (socketFile.exists())
				socketFile.delete();
			ChannelFuture f = b.bind(new DomainSocketAddress("/var/run/iofabric/iofabric.sock")).sync();
			socketFile.deleteOnExit();
			Files.setPosixFilePermissions(socketFile.toPath(),
					EnumSet.of(PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.GROUP_READ,
							PosixFilePermission.GROUP_WRITE, PosixFilePermission.OWNER_EXECUTE,
							PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));

			logger.log(Level.INFO, "UNIX", "SERVER STARTED");

			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
