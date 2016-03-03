package com.iotracks.iofabric.element;

import java.util.List;

public class Route {
	private List<Element> receivers;
	
	public Route() {
		
	}

	public List<Element> getInternalReceivers() {
		return receivers;
	}

	public void setInternalReceivers(List<Element> internalReceivers) {
		this.receivers = internalReceivers;
	}

	@Override
	public String toString() {
		String in = "\"receivers\" : [";
		if (receivers != null)
			for (Element e : receivers)
				in += "\"" + e.getElementId() + "\",";
		in += "]";
		return "{" + in + "}";
	}
}
