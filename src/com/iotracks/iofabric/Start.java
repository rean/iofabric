package com.iotracks.iofabric;

import com.iotracks.iofabric.supervisor.Supervisor;

public class Start {
	
	public static void main(String[] args) throws Exception {

		Thread supervisorThread = new Thread(new Supervisor());
		supervisorThread.start();
		
	}

}
