# ioFabric Architecture

The ioFabric application is a background service that runs on x86 Linux machines. The application is written using Java Enterprise Edition. The base of the application is a .jar file that is turned into a service on the host Linux machine via an install script. This .jar file runs the *supervisor module* when started.

The *supervisor module* is responsible for providing the base stability of the application. It is the root thread of the application. All of the other modules are started and monitored by the *supervisor module*.