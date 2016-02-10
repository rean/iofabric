#ioFabric Architecture

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

To handle the new challenges, we have created an input/output compute layer that sits between data sources and the end systems and applications that use them. Our I/O compute layer is the opposite of a cloud. The layer is a fabric composed of completely separate and independent nodes performing individual tasks but ultimately working together. Typical cloud infrastructure turns compute instances into interchangeable commodities where the location does not matter, while the iotracks I/O compute fabric enforces the individual identity and location of each compute instance. With iotracks, specific processes are directed to take place on particular compute instances through orchestration. This is very advantageous for connecting to sensors and processing information on the edge instead of in the cloud. The orchestration happens outside of the nodes themselves, using a toolset product called ioAuthoring to model and move the streams of information.

To make the fabric work properly, it needs to span across many different processing platforms. Some examples are Linux servers, Windows servers, iPhone and iPad devices, Android phone and tablet devices, and ARM processor Linux machines.

For each computing platform, a version of ioFabric must be built to fit the native system. It communicates with the fabric controller, manages the instantiation of dynamic processing elements, and exposes resources to those elements. When it is running on a device or a server, it makes sure that the fabric controller knows the health and status of the computing instance. It receives instructions for allocating or deallocating containers and must carry out those instructions. It hosts a local API that the containers use to send and receive information messages, communicate with each other, receive their configuration information, and perform other tasks. ioFabric also hosts the high-performance local message bus that moves information securely between containers.

<img src="ioFabric-Architecture-Diagram.png" />

##Module Details

The following breakdown of functional modules gives detailed descriptions and functional requirements. Even though the functionality of ioFabric has been split into modules, that does not mean that the actual code should contain separate libraries or separately compiled components. In some cases it might, but this is not necessary. The goal is to keep the duties of the application clearly categorized so repeat code is minimized and so coding tasks are grouped.

Implement the functional requirements for each module and across modules in a way that fits the underlying compute platform (such as x86 Linux) in the best way possible.

###Supervisor

The supervisor module is the root thread of the ioFabric application. It is repsonsible for launching the other modules and monitoring them to make sure they are always running. The supervisor module should never stop running unless the user stops the ioFabric application service. It should start when the system boots unless the user manually removes the automatic starting of the ioFabric service.

The supervisor doesn't provide much of the actual ioFabric functionality but it does provide the key application features that are exposed to the user. Each ioFabric instance is tied to a particular fabric controller and user account through a provisioning process. The provisioning functionality is handled by the supervisor module, which then passes the information down to the field agent. Each ioFabric instance is also manually configurable through its configuration file located in the installation directory. The supervisor module is responsible for parsing the configuration file and passing the different pieces of configuration information to the other modules.

The supervisor module exposes several command-line interactions that the Linux system user can use to set up, monitor, and control ioFabric.

####Functional Requirements

* Be the main executable process of the ioFabric product (the main thread)
* Parse the product's configuration XML file
* Store the product's configuration in memory for use while the software is operational
* Pass configuration information into the other modules of the software where needed
* Write configuration changes to the configuration XML file as they occur during software operation
* Pass updated configuration changes into the other modules of the software as needed as they occur
* Store configuration changes in memory as they occur
* Provide command-line interface functionality according to the command-line interface specification document
* Parse and handle the configuration according to the configuration specification document
* Monitor the status of the other modules
* Make the status of itself (the supervisor module) and the other modules available via command-line and available to the status reporting module
* Start the other modules and manage multi-threading as needed
* Restart the other modules on a decreasing frequency basis when they fail (immediately, then after 10 seconds, then 30 seconds, then 1 minute, and so on)
* Provide logging functionality to all other modules
* Initiate and manage the software's log files
* Log start-up and shut-down sequences
* Log module starts, stops, and restarts
* Log configuration XML file parsing
* Log configuration changes


####Performance Requirements

* Start immediately (as fast as possible)
* Use as little memory, disk space, and CPU time as possible
* Monitor modules frequently enough to be performant but also keep CPU consumption to a minimum


###Resource Consumption Manager

The Resource Consumption Manager is in charge of monitoring the usage behavior of the whole application. Timeliness and efficiency are more important than precision. It is easy to monitor resources with heavy code. The problem is, this precise monitoring drags on system performance and consumes significant resources itself.

The Resource Consumption Manager in ioFabric should be checking frequently enough to be effective, but should only cause minimal drain on CPU time and other resources.

In some cases, the Resource Consumption Manager needs to tell another module to "curb its behavior" and use less memory or curtail processing because the CPU usage is too high. In other cases, the modules themselves are supposed to keep their usage to a set limit (such as logging) and the job of Resource Consumption Manager is simply to monitor and report violations.

In production systems, users will be expecting ioFabric to stay within certain resource consumption boundaries. The ioFabric product needs to be reliable in its self-management so it will operate peacefully with other software.

####Functional Requirements

* 1sdfasd


####Performance Requirements

* sfsd


