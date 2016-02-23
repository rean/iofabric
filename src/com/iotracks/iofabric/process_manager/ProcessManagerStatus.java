package com.iotracks.iofabric.process_manager;

import java.util.HashMap;
import java.util.Map;

import com.iotracks.iofabric.docker.Registry;
import com.iotracks.iofabric.element.Element;
import com.iotracks.iofabric.utils.Constants.DockerStatus;
import com.iotracks.iofabric.utils.Constants.ElementStatus;
import com.iotracks.iofabric.utils.Constants.LinkStatus;

public class ProcessManagerStatus {
	private int runningElementsCount;
	private DockerStatus dockerStatus;
	private Map<Element, ElementStatus> elementsStatus;
	private int registriesCount;
	private Map<Registry, LinkStatus> registriesStatus;

	public ProcessManagerStatus() {
		elementsStatus = new HashMap<>();
		registriesStatus = new HashMap<>();
		runningElementsCount = 0;
		dockerStatus = DockerStatus.RUNNING;
		registriesCount = 0;
	}

	public int getRunningElementsCount() {
		return runningElementsCount;
	}

	public ProcessManagerStatus increaseRunningElementsCount() {
		this.runningElementsCount++;
		return this;
	}

	public ProcessManagerStatus decreaseRunningElementsCount() {
		this.runningElementsCount--;
		return this;
	}

	public DockerStatus getDockerStatus() {
		return dockerStatus;
	}

	public ProcessManagerStatus setDockerStatus(DockerStatus dockerStatus) {
		this.dockerStatus = dockerStatus;
		return this;
	}

	public ElementStatus getElementsStatus(Element element) {
		return elementsStatus.get(element);
	}

	public ProcessManagerStatus setElementsStatus(Element element, ElementStatus status) {
		this.elementsStatus.put(element, status);
		return this;
	}

	public int getRegistriesCount() {
		return registriesCount;
	}

	public ProcessManagerStatus increaseRegistriesCount() {
		this.registriesCount++;
		return this;
	}

	public ProcessManagerStatus decreaseRegistriesCount() {
		this.registriesCount--;
		return this;
	}

	public LinkStatus getRegistriesStatus(Registry registry) {
		return registriesStatus.get(registry);
	}

	public ProcessManagerStatus setRegistriesStatus(Registry registry, LinkStatus status) {
		this.registriesStatus.put(registry, status);
		return this;
	}

}
