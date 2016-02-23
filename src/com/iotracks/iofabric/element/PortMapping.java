package com.iotracks.iofabric.element;

public class PortMapping {
    final String external;
    final String internal;

	public PortMapping(String out, String in) {
        this.external = out;
        this.internal = in;
    }

    public String getExternal() {
		return external;
	}

	public String getInternal() {
		return internal;
	}

    @Override
    public String toString() {
        return "{" +
                "external='" + external + '\'' +
                ", internal='" + internal + '\'' +
                '}';
    }
}
