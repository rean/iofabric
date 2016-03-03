package com.iotracks.iofabric.element;

public class PortMapping {
    final String outside;
    final String inside;

	public PortMapping(String outside, String inside) {
        this.outside = outside;
        this.inside = inside;
    }

    public String getOutside() {
		return outside;
	}

	public String getInside() {
		return inside;
	}

    @Override
    public String toString() {
        return "{" +
                "outside='" + outside + '\'' +
                ", inside='" + inside + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object other) {
    	if (other.getClass() != PortMapping.class)
    		return false;
    	return this.outside.equals(((PortMapping) other).outside) 
    			&& this.inside.equals(((PortMapping) other).inside); 
    }
    
}
