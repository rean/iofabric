package com.iotracks.iofabric.process_manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.model.AuthResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Info;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.api.model.RestartPolicy;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.command.PullImageResultCallback;
import com.iotracks.iofabric.docker.Registry;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.element.PortMapping;
import com.iotracks.iofabric.element.Route;
import com.iotracks.iofabric.status_reporter.StatusReporter;
import com.iotracks.iofabric.utils.Constants.ElementStatus;
import com.iotracks.iofabric.utils.JSON;
import com.iotracks.iofabric.utils.configuration.Configuration;
import com.iotracks.iofabric.utils.logging.LoggingService;

public class ProcessManager {
	private final String instanceId = "qk7PnPVpDTGmx3zWNR8zNP34";
	private final String token = "0b51a84b066a049228ea3e0b14f424720842c132153b2fa7091c21a1129534d4";
	
	private String MODULE_NAME = "Process Manager";
	private int MONITORING_FREQ_SECONDS = 10;

	private DockerClient dockerClient;

	private List<Element> elements;
	private List<Registry> registries;

	private final Runnable monitoring = () -> {
		LoggingService.log(Level.INFO, MODULE_NAME, "monitoring elements");
	};

	public ProcessManager() {
		elements = new ArrayList<>();
		registries = new ArrayList<>();
	}

	public void update() {
		// TODO
	}

	private void addElement(Element e) throws AddElementException {
		// TODO
		StatusReporter.setProcessManagerStatus().setElementsStatus(e, ElementStatus.BUILDING);
		String tag = null;
		String registry = e.getImageName();
		if (registry.contains(":")) {
			String[] sp = registry.split(":");
			registry = sp[0];
			tag = sp[1];
		}
		PullImageCmd req = dockerClient.pullImageCmd(registry).withAuthConfig(dockerClient.authConfig());
		if (tag != null)
			req.withTag(tag);

		try {
			req.exec(new PullImageResultCallback()).awaitSuccess();
			RestartPolicy restartPolicy = RestartPolicy.onFailureRestart(10);

			Ports portBindings = new Ports();
			List<ExposedPort> exposedPorts = new ArrayList<>();
			if (e.getPortMappings() != null)
				e.getPortMappings().forEach(mapping -> {
					ExposedPort internal = ExposedPort.tcp(Integer.parseInt(mapping.getInternal()));
					Ports.Binding external = Ports.Binding(Integer.parseInt(mapping.getExternal()));
					portBindings.bind(internal, external);
					exposedPorts.add(internal);
				});
			String[] extraHosts = { "iofabric:127.0.0.1" };

			CreateContainerResponse resp = dockerClient.createContainerCmd(e.getImageName()).withCpuset("0")
					.withExtraHosts(extraHosts).withExposedPorts(exposedPorts.toArray(new ExposedPort[0]))
					.withPortBindings(portBindings).withEnv("SELFNAME=" + e.getElementID()).withName(e.getElementID())
					.withRestartPolicy(restartPolicy).exec();
			e.setContainerID(resp.getId());
			StatusReporter.setProcessManagerStatus().setElementsStatus(e, ElementStatus.STOPPED);
		} catch (Exception ex) {
			StatusReporter.setProcessManagerStatus().setElementsStatus(e, ElementStatus.FAILED_VERIFICATION);
			System.out.println(ex.getMessage());
			LoggingService.log(Level.WARNING, MODULE_NAME, ex.getMessage());
			throw new AddElementException(ex.getMessage());
		}
	}

	private void startElement(Element e) throws StartElementException {
		StatusReporter.setProcessManagerStatus().setElementsStatus(e, ElementStatus.STARTING);
		// TODO
		try {
			dockerClient.startContainerCmd(e.getContainerID()).exec();
			StatusReporter.setProcessManagerStatus().setElementsStatus(e, ElementStatus.RUNNING);
		} catch (Exception ex) {
			StatusReporter.setProcessManagerStatus().setElementsStatus(e, ElementStatus.FAILED_VERIFICATION);
			LoggingService.log(Level.WARNING, MODULE_NAME, ex.getMessage());
			throw new StartElementException(ex.getMessage());
		}
	}

	private void stopElement() {
		// TODO
	}

	private void removeElement() {
		// TODO
	}

	private void updateElement() {
		// TODO
	}

	private void loadConfig() {
		try {
			JSONObject configObjs = JSON.getJSON("https://iotracks.com/api/v1/instance/containerconfig/id/"
					+ instanceId + "/token/" + token);
//			JSONObject configObjs = (JSONObject) parser.parse(new FileReader("elements/config.json"));
			JSONArray configs = (JSONArray) configObjs.get("containerconfig");
			for (Object configObj : configs) {
				JSONObject config = (JSONObject) configObj;
				String id = config.get("id").toString();
				String configString = config.get("config").toString();
				for (Element element : this.elements)
					if (element.getElementID().equals(id)) {
						element.setElementConfig(configString);
						break;
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// LoggingService.log(Level.INFO, MODULE_NAME, "unable to load
			// elements config\n" + e.getMessage());
		}
	}

	private void loadRoutes() {
		try {
			JSONObject routeObjs = JSON.getJSON("https://iotracks.com/api/v1/instance/routing/id/"
					+ instanceId + "/token/" + token);
//			JSONObject routeObjs = (JSONObject) parser.parse(new FileReader("elements/routes.json"));
			JSONArray routes = (JSONArray) routeObjs.get("routing");
			for (Object routeObj : routes) {
				JSONObject route = (JSONObject) routeObj;
				Route elementRoute = new Route();
				String container = route.get("container").toString();

				JSONObject receivers = (JSONObject) route.get("receivers");

				JSONArray internals = (JSONArray) receivers.get("internal");
				List<Element> internalElements = null;
				if (internals.size() > 0)
					internalElements = new ArrayList<>();
				for (Object internalObj : internals) {
					String internal = internalObj.toString();
					for (Element element : this.elements)
						if (element.getElementID().equals(internal)) {
							internalElements.add(element);
							break;
						}
				}

				JSONArray externals = (JSONArray) receivers.get("external");
				List<Element> externalElements = null;
				if (externals.size() > 0)
					externalElements = new ArrayList<>();
				for (Object externalObj : externals) {
					String external = externalObj.toString();
					for (Element element : this.elements)
						if (element.getElementID().equals(external)) {
							externalElements.add(element);
							break;
						}
				}

				elementRoute.setExternalReceivers(externalElements);
				elementRoute.setInternalReceivers(internalElements);
				for (Element element : this.elements)
					if (element.getElementID().equals(container)) {
						element.setRoute(elementRoute);
						break;
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// LoggingService.log(Level.INFO, MODULE_NAME, "unable to load
			// elements route\n" + e.getMessage());
		}

	}

	private void loadList() {
		try {
			JSONObject containerObjects = JSON.getJSON("https://iotracks.com/api/v1/instance/containerlist/id/"
					+ instanceId + "/token/" + token);
//			JSONObject containerObjects = (JSONObject) parser.parse(new FileReader("elements/list.json"));
			JSONArray containers = (JSONArray) containerObjects.get("containerlist");
			if (containers.size() > 0)
				this.elements = new ArrayList<>();
			for (Object containerObj : containers) {
				JSONObject container = (JSONObject) containerObj;

				Element element = new Element(container.get("id").toString(), container.get("imageid").toString());

				element.setLastModified(Long.parseLong(container.get("lastmodified").toString()));

				JSONArray portMappingObjs = (JSONArray) container.get("portmappings");
				List<PortMapping> pms = null;
				if (portMappingObjs.size() > 0) {
					pms = new ArrayList<>();
					for (Object portMappingObj : portMappingObjs) {
						JSONObject portMapping = (JSONObject) portMappingObj;
						PortMapping pm = new PortMapping(portMapping.get("outsidecontainer").toString(),
								portMapping.get("insidecontainer").toString());
						pms.add(pm);
					}
				}
				element.setPortMappings(pms);
				this.elements.add(element);
			}
		} catch (Exception e) {
			LoggingService.log(Level.INFO, MODULE_NAME, "unable to load elements list\n" + e.getMessage());
		}
	}

	private void dockerConnect() {
		DockerClientConfig config = DockerClientConfig.createDefaultConfigBuilder().withUri("http://127.0.0.1:2375")
				.withDockerCertPath(Configuration.getControllerCert()).withUsername("iointegrator")
				.withPassword("0nTh3Edge2015").withEmail("admin@iotracks.com")
				.withServerAddress("https://index.docker.io/v1/").build();

		dockerClient = DockerClientBuilder.getInstance(config).build();
		try {
			Info info = dockerClient.infoCmd().exec();
			LoggingService.log(Level.INFO, MODULE_NAME, "connected to docker : " + info.getName());
			LoggingService.log(Level.INFO, MODULE_NAME, "containers count : " + info.getContainers());
			LoggingService.log(Level.INFO, MODULE_NAME, "images count : " + info.getImages());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void dockerLogin() throws Exception {
		try {
			AuthResponse resp = dockerClient.authCmd().exec();
			System.out.println(resp.getStatus());

			System.out.println("\nList images :");
			List<Image> imagesList = dockerClient.listImagesCmd().exec();
			imagesList.forEach(image -> {
				System.out.println("\t" + image.getId() + " : " + image.getRepoTags()[0]);
			});

			System.out.println("\nSearch images :");
			List<SearchItem> images = dockerClient.searchImagesCmd("iotrack").exec();
			images.forEach(image -> {
				System.out.println(image.getName());
			});
		} catch (Exception e) {
			LoggingService.log(Level.WARNING, MODULE_NAME, e.getMessage());
			throw new Exception();
		}
	}

	private void dockerLogout() {
		dockerClient.authCmd().close();
	}

	private Container getContainerByName(String name) {
		List<Container> containers = dockerClient.listContainersCmd().exec();
		Optional<Container> result = containers.stream().filter(c -> c.getNames()[0].trim().substring(1).equals(name))
				.findFirst();
		if (result.isPresent())
			return result.get();
		else
			return null;
	}

	private Container getContainerById(String Id) {
		List<Container> containers = dockerClient.listContainersCmd().exec();
		Optional<Container> result = containers.stream().filter(c -> c.getId().trim().equals(Id)).findFirst();
		if (result.isPresent())
			return result.get();
		else
			return null;
	}

	private List<Container> getAllContainers() {
		return dockerClient.listContainersCmd().withShowAll(true).exec();
	}

	private List<Container> getAllRunningContainers() {
		return dockerClient.listContainersCmd().withShowAll(false).exec();
	}

	private void compareContainers() {
		List<Container> containers = getAllContainers();
		this.elements.forEach(e -> {
			Optional<Container> result = containers.stream()
					.filter(c -> c.getNames()[0].trim().substring(1).equals(e.getElementID())).findFirst();
			if (result.isPresent()) {
				// TODO : container for element installed. check for
				// configuration and
				// status
				Container container = result.get();
				e.setContainerID(container.getId());
				String status = container.getStatus();
				if (status.contains("Up"))
					StatusReporter.setProcessManagerStatus().setElementsStatus(e, ElementStatus.RUNNING);
				else
					try {
						startElement(e);
					} catch (StartElementException startException) {
						startException.printStackTrace();
					}
			} else {
				// TODO : container not present. create one!
				try {
					addElement(e);
					startElement(e);
				} catch (AddElementException addException) {
					addException.printStackTrace();
				} catch (StartElementException startException) {
					startException.printStackTrace();
				}
			}
		});
	}

	public void start() {
		// TODO

		dockerConnect();

		loadList();
		loadConfig();
		loadRoutes();

		compareContainers();

		System.out.println(this.elements.toString());
		System.exit(0);

		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(monitoring, 0, MONITORING_FREQ_SECONDS, TimeUnit.SECONDS);
		LoggingService.log(Level.INFO, MODULE_NAME, "started");

	}
}
