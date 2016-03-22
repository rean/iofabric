package com.iotracks.iofabric;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.core.client.ClientConsumer;
import org.hornetq.api.core.client.ClientMessage;
import org.hornetq.api.core.client.ClientProducer;
import org.hornetq.api.core.client.ClientSession;
import org.hornetq.api.core.client.ClientSessionFactory;
import org.hornetq.api.core.client.HornetQClient;
import org.hornetq.api.core.client.ServerLocator;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;

import com.iotracks.iofabric.command_line.CommandLineParser;
import com.iotracks.iofabric.message_bus.MessageBusServer;
import com.iotracks.iofabric.supervisor.Supervisor;
import com.iotracks.iofabric.utils.Constants;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.configuration.ConfigurationItemException;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class Start {

	private static void sendCommand(String... args) {
		Map<String, Object> connectionParams = new HashMap<>();
		connectionParams.put("port", 55555);
		connectionParams.put("host", "localhost");

        ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(
        		new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams));
        ClientSessionFactory sf = null;
        try {
            sf = serverLocator.createSessionFactory();
        } catch (Exception e) {
        	return;
        }
        
		if (args.length > 0 && args[0].equals("start")) {
			System.out.println("iofabric is already running.");
			sf.close();
			System.exit(1);
		}
		String command = "";
		for (String str : args)
			command += str + " ";

		if (command.trim().equals(""))
			command = "help";
		if (command.trim().startsWith("stop")) {
			System.out.println("Stopping iofabric service...");
			System.out.flush();
		}
        
		ClientSession session = null;
		try {
			session = sf.createSession();
			ClientConsumer consumer = session.createConsumer(MessageBusServer.commandlineAddress,
					"receiver = 'iofabric.commandline.response'");
			ClientProducer producer = session.createProducer(MessageBusServer.commandlineAddress);
			session.start();

			ClientMessage received = consumer.receiveImmediate();
			while (received != null) {
				received.acknowledge();
				received = consumer.receiveImmediate();
			}
			
			ClientMessage message = session.createMessage(false);
			message.putStringProperty("command", command);
			message.putObjectProperty("receiver", "iofabric.commandline.command");
			producer.send(message);
			if (args[0].equals("stop"))
				System.exit(0);
			received = consumer.receive();
			received.acknowledge();
			String response = received.getStringProperty("response");
			System.out.println(response);

			producer.close();
			consumer.close();
		} catch (Exception e) {
			// DO NOTHING
		} finally {
			if (sf != null) {
				sf.close();
			}
		}
		System.exit(0);
	}

	private static void setupEnvironment() {
		final File daemonFilePath = new File("/var/run/iofabric");
		if (!daemonFilePath.exists()) {
			try {
				daemonFilePath.mkdirs();

				UserPrincipalLookupService lookupservice = FileSystems.getDefault().getUserPrincipalLookupService();
				final GroupPrincipal group = lookupservice.lookupPrincipalByGroupName("iofabric");
				Files.getFileAttributeView(daemonFilePath.toPath(), PosixFileAttributeView.class,
						LinkOption.NOFOLLOW_LINKS).setGroup(group);
				Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrwx---");
				Files.setPosixFilePermissions(daemonFilePath.toPath(), perms);
			} catch (Exception e) {
			}
		}

	}
	
	public static void main(String[] args) throws ParseException {
		try {
			Configuration.loadConfig();
		} catch (ConfigurationItemException e) {
			System.out.println("invalid configuration item(s).");
			System.out.println(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.out.println("error accessing /etc/iofabric/config.xml");
			System.exit(1);
		}

		setupEnvironment();
		
		sendCommand(args);

		if (args.length > 0) {
			if (args[0].equals("help") || args[0].equals("--help") || args[0].equals("-h") || args[0].equals("-?") || 
					args[0].equals("version") || args[0].equals("--version") || args[0].equals("-v")) {
				System.out.println(CommandLineParser.parse(args[0]));
				System.out.flush();
				System.exit(0);
			} else if (!args[0].equals("start")) {
				System.out.println("iofabric is not running.");
				System.out.flush();
				System.exit(1);
			}
		}

		try {
			LoggingService.setupLogger();
		} catch (IOException e) {
			System.out.println("Error starting logging service\n" + e.getMessage());
			System.exit(1);
		}
		LoggingService.logInfo("Main", "configuration loaded.");

		// port System.out to null
		Constants.systemOut = System.out;
		if (!Configuration.debugging) {
			System.setOut(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) {
					// DO NOTHING
				}
			}));
	
			System.setErr(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) {
					// DO NOTHING
				}
			}));
		}
		
		
		LoggingService.logInfo("Main", "starting supervisor");
		Supervisor supervisor = new Supervisor();
		supervisor.start();

		// port System.out to standard
		System.setOut(Constants.systemOut);
	}

}