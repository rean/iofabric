package com.iotracks.iofabric.process_manager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import javax.json.Json;
import javax.json.JsonObject;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.model.AuthConfig;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.RestartPolicy;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.Registry;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants.LinkStatus;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class DockerUtil {
	private final String MODULE_NAME = "Docker Util";
	
	private static DockerUtil instance;
//	public DockerClient getDockerClient() {
//		return dockerClient;
//	}

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
	
	public float getMemoryUsage(String containerId) {
		if (!hasContainer(containerId)) 
			return 0;
		
		StatsCallback statsCallback = new StatsCallback(); 
		dockerClient.statsCmd().withContainerId(containerId).exec(statsCallback);
		while (!statsCallback.gotStats()) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {}
		}
		Map<String, Object> memoryUsage = statsCallback.getStats().getMemoryStats();
		return Float.parseFloat(memoryUsage.get("usage").toString());
	}
	
	@SuppressWarnings("unchecked")
	public float getCpuUsage(String containerId) {
		if (!hasContainer(containerId)) 
			return 0;
		
		StatsCallback statsCallback = new StatsCallback(); 
		dockerClient.statsCmd().withContainerId(containerId).exec(statsCallback);
		while (!statsCallback.gotStats()) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {}
		}
		Map<String, Object> usageBefore = statsCallback.getStats().getCpuStats();
		float totalBefore = Long.parseLong(((Map<String, Object>) usageBefore.get("cpu_usage")).get("total_usage").toString());;
		float systemBefore = Long.parseLong((usageBefore.get("system_cpu_usage")).toString());
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}

		statsCallback.reset();
		dockerClient.statsCmd().withContainerId(containerId).exec(statsCallback);
		while (!statsCallback.gotStats()) {
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {}
		}
		Map<String, Object> usageAfter = statsCallback.getStats().getCpuStats();
		float totalAfter = Long.parseLong(((Map<String, Object>) usageAfter.get("cpu_usage")).get("total_usage").toString());
		float systemAfter = Long.parseLong((usageAfter.get("system_cpu_usage")).toString());
		
		
		return Math.abs(1000f * ((totalAfter - totalBefore) / (systemAfter - systemBefore)));
	}
	
	public Image getImage(String imageName) {
		List<Image> images = dockerClient.listImagesCmd().exec();
		Optional<Image> result = images.stream()
				.filter(image -> image.getRepoTags()[0].equals(imageName)).findFirst();

		if (result.isPresent())
			return result.get();
		else
			return null;
	}
	
	public void connect() throws Exception {
		dockerClient = DockerClientBuilder.getInstance(Configuration.getDockerUrl()).build();

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
			StatusReporter.setProcessManagerStatus().setRegistriesStatus(registry, LinkStatus.CONNECTED);
		} catch (Exception e) {
			LoggingService.logWarning(MODULE_NAME, "login failed - " + e.getMessage());
			StatusReporter.setProcessManagerStatus().setRegistriesStatus(registry, LinkStatus.FAILED_LOGIN);
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
	
	public Container getContainer(String elementId) {
		List<Container> containers = getContainers();
		Optional<Container> result = containers.stream()
				.filter(c -> c.getNames()[0].trim().substring(1).equals(elementId)).findFirst();
		if (result.isPresent())
			return result.get();
		else 
			return null;
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

	public void removeImage(String imageName) throws Exception {
		Image image = getImage(imageName);
		if (image == null)
			return;
		dockerClient.removeImageCmd(image.getId()).withForce(true).exec();
	}
	
	public boolean hasContainer(String containerId) {
		try {
			getContainerStatus(containerId);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void pullImage(String imageName) throws Exception {
		String tag = null;
		String registry = imageName;
		if (registry.contains(":")) {
			String[] sp = registry.split(":");
			registry = sp[0];
			tag = sp[1];
		}
		PullImageCmd req = dockerClient.pullImageCmd(registry).withAuthConfig(dockerClient.authConfig());
		if (tag != null)
			req.withTag(tag);
		PullImageResultCallback res = new PullImageResultCallback();
		res = req.exec(res);
		res.awaitSuccess();
	}

	public String createContainer(Element element, String host) throws Exception {
		RestartPolicy restartPolicy = RestartPolicy.onFailureRestart(10);

		Ports portBindings = new Ports();
		List<ExposedPort> exposedPorts = new ArrayList<>();
		if (element.getPortMappings() != null)
			element.getPortMappings().forEach(mapping -> {
				ExposedPort internal = ExposedPort.tcp(Integer.parseInt(mapping.getInside()));
				Ports.Binding external = Ports.Binding(Integer.parseInt(mapping.getOutside()));
				portBindings.bind(internal, external);
				exposedPorts.add(internal);
			});
		String[] extraHosts = { host };
		CreateContainerResponse resp = dockerClient.createContainerCmd(element.getImageName())
				.withCpuset("0")
				.withExtraHosts(extraHosts)
				.withExposedPorts(exposedPorts.toArray(new ExposedPort[0]))
				.withPortBindings(portBindings)
				.withEnv("SELFNAME=" + element.getElementId())
				.withName(element.getElementId())
				.withRestartPolicy(restartPolicy)
				.exec();
		return resp.getId();
	}
	
}
