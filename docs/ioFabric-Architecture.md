# ioFabric Architecture

The ioFabric application is a background service that runs on x86 Linux machines. The application is written using Java Enterprise Edition. The base of the application is a .jar file that is turned into a service on the host Linux machine via an install script. This .jar file runs the *supervisor module* when started.

The *supervisor module* is responsible for providing the base stability of the application. It is the root thread of the application. All of the other modules are started and monitored by the *supervisor module*.

A list of the modules in the ioFabric application can be found immediately below. A detailed description of all modules can be found later in this document. Each section contains a discussion of the module's purpose and functional requirements.

###ioFabric Modules (functional sections of the application)

* Supervisor
* Resource Consumption Manager
* Process Manager
* Status Reporter
* Local API
* Message Bus
* Field Agent

###Application Purpose

ioFabric exists to turn static Linux compute instances into independently controllable nodes of a dynamic processing fabric. Iotracks, inc. provides several software products that work in tandem to create this fabric and the tools needed to orchestrate it. The Linux ioFabric product is a major piece. It's the part that runs the local processing on the Linux instance and stands up and takes down the containers that do the actual processing of information.

At iotracks, we believe that one of the next great challenges in computing technology is handling the rapidly increasing number of data sources, their widening variety, and the need to add and remove them dynamically without rebuilding solution code. Along with these voluminous data sources comes massive amounts of actual data. We believe that processing should be moved to where the data is, instead of moving large amounts of data all the way to a central backend for processing.

To handle the new challenges, we have created an input/output compute layer that sits between data sources and the end systems and applications that use them. The I/O compute layer is the opposite of a cloud. The layer is a fabric composed of completely separate and independent nodes working together. Typical cloud infrastructure turns compute instances into interchangeable commodities where the location does not matter, while the iotracks I/O compute fabric enforces the individual identity of each compute instance. With iotracks, specific processes are directed to take place on particular compute instances through orchestration. This is very important for connecting to sensors and processing information on the edge instead of in the cloud. The orchestration happens outside of the nodes themselves, using a toolset product called ioAuthoring to model and move the streams of information.

