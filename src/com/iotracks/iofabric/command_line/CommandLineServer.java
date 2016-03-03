package com.iotracks.iofabric.command_line;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Set;
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

	@Override
	public void run() {
		EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
		EventLoopGroup workerGroup = new EpollEventLoopGroup(1);
		try {
			ServerBootstrap b = new ServerBootstrap().group(bossGroup, workerGroup)
					.channel(EpollServerDomainSocketChannel.class).childHandler(new ChannelInitializer<UnixChannel>() {
						@Override
						public void initChannel(UnixChannel ch) throws Exception {
							ch.pipeline().addFirst(new ServerHandler());
						}
					});

			File socketFile = new File("/var/run/iofabric/iofabric.sock");
			if (socketFile.exists())
				socketFile.delete();
			ChannelFuture f = b.bind(new DomainSocketAddress(socketFile)).sync();
			socketFile.deleteOnExit();

			UserPrincipalLookupService lookupservice = FileSystems.getDefault().getUserPrincipalLookupService();
			final GroupPrincipal group = lookupservice.lookupPrincipalByGroupName("iofabric");
			Files.getFileAttributeView(socketFile.toPath(), PosixFileAttributeView.class,
					LinkOption.NOFOLLOW_LINKS).setGroup(group);
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwx---");
			Files.setPosixFilePermissions(socketFile.toPath(),
					perms);

			
			LoggingService.logInfo("UNIX", "SERVER STARTED");

			f.channel().closeFuture().sync();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
