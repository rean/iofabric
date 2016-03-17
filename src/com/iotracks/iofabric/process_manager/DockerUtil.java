package com.iotracks.iofabric.process_manager;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.iotracks.iofabric.element.Registry;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class DockerUtil {
	private final String MODULE_NAME = "Docker Util";
	
	private static DockerUtil instance;
	public DockerClient getDockerClient() {
		return dockerClient;
	}

	private DockerClient dockerClient;
	
	private DockerUtil() {
	}
	
	public static DockerUtil getInstance() {
		if (instance == null) {
			synchronized (DockerUtil.class) {
				if (instance == null) 
					instance = new DockerUtil();
			}
		}
		return instance;
	}
	
	public void connect() throws Exception {
		DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder()
				.withUri(Configuration.getDockerUrl())
				.withDockerCertPath(Configuration.getControllerCert())
				.build();
		dockerClient = DockerClientBuilder.getInstance(config).build();

		try {
			Info info = dockerClient.infoCmd().exec();
			LoggingService.logInfo(MODULE_NAME, "connected to docker daemon: " + info.getName());
		} catch (Exception e) {
			LoggingService.logInfo(MODULE_NAME, "connecting to docker failed: " + e.getMessage());
			throw e;
		}
	}
	
	private String getAuth(Registry registry) {
		JsonObject auth = Json.createObjectBuilder()
				.add("username", registry.getUserName())
				.add("password", registry.getPassword())
				.add("email", registry.getUserEmail())
				.add("auth", "")
				.build();
		return Base64.getEncoder().encodeToString(auth.toString().getBytes(StandardCharsets.US_ASCII));
	}
	
	public void login(Registry registry) throws Exception {
		if (!isConnected()) {
			try {
				connect();
			} catch (Exception e) {
				throw e;
			}
		}
		LoggingService.logInfo(MODULE_NAME, "logging in to registry");
		try {
			DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder()
					.withUri(Configuration.getDockerUrl())
					.withDockerCertPath(Configuration.getControllerCert())
					.withUsername(registry.getUserName())
					.withPassword(registry.getPassword())
					.withEmail(registry.getUserEmail())
					.withServerAddress(registry.getUrl())
					.build();

			AuthConfig authConfig = new AuthConfig();
			authConfig.setUsername(registry.getUserName());
			authConfig.setPassword(registry.getPassword());
			authConfig.setEmail(registry.getUserEmail());
			authConfig.setAuth(getAuth(registry));
			authConfig.setServerAddress(registry.getUrl());

			dockerClient = DockerClientBuilder.getInstance(config).build();
			dockerClient.authCmd().exec();
			
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "login failed - " + e.getMessage());
			throw e;
		}
	}
	
	public void close() {
		try {
			dockerClient.close();
		} catch (Exception e) {}
	}
	
	public boolean isConnected() {
		try {
			dockerClient.infoCmd().exec();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public void startContainer(String id) throws Exception {
		dockerClient.startContainerCmd(id).exec();
	}
	
	public void stopContainer(String id) throws Exception {
		dockerClient.stopContainerCmd(id).exec();
	}
	
	public void removeContainer(String id) throws Exception {
		dockerClient.removeContainerCmd(id).withForce(true).exec();
	}
	
	public String getContainerIpAddress(String id) throws Exception {
		try {
			InspectContainerResponse inspect =  dockerClient.inspectContainerCmd(id).exec();
			return inspect.getNetworkSettings().getIpAddress();
		} catch (Exception e) {
			throw e;
		}
	}
	
	public ContainerState getContainerStatus(String id) throws Exception {
		try {
			InspectContainerResponse inspect =  dockerClient.inspectContainerCmd(id).exec();
			return inspect.getState();
		} catch (Exception e) {
			throw e;
		}
	}
	
	public List<Container> getContainers() {
		if (!isConnected()) {
			try {
				connect();
			} catch (Exception e) {
				return null;
			}
		}
		return dockerClient.listContainersCmd().withShowAll(true).exec();
	}
}
