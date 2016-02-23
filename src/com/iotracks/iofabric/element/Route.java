package com.iotracks.iofabric.element;

import java.util.List;

public class Route {
	private List<Element> internalReceivers;
	private List<Element> externalReceivers;
	
	public Route() {
		
	}

	public List<Element> getInternalReceivers() {
		return internalReceivers;
	}

	public void setInternalReceivers(List<Element> internalReceivers) {
		this.internalReceivers = internalReceivers;
	}

	public List<Element> getExternalReceivers() {
		return externalReceivers;
	}

	public void setExternalReceivers(List<Element> externalReceivers) {
		this.externalReceivers = externalReceivers;
	}

	@Override
	public String toString() {
		String in = "\"internal\" : [";
		if (internalReceivers != null)
			for (Element e : internalReceivers)
				in += "\"" + e.getElementID() + "\",";
		in += "]";
		String ex = "\"external\" : [";
		if (externalReceivers != null)
			for (Element e : externalReceivers)
				ex += "\"" + e.getElementID() + "\",";
		ex += "]";
		return "{" + in + "," + ex + "}";
	}
}
