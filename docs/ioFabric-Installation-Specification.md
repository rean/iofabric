# Installation Specification

One of the most important aspects of the ioFabric product is its ease of installation. Unfortunately, it is difficult to produce an easy installation experience across a variety of Linux machines. It is even harder to make such an installation completely reliable.

That's why we are putting the challenge of creating a great installation experience right at the front. Adding an installation package after a product is fully built seems like the logical treatment, but I believe it is backwards. If installation is important, put it up front.

The packaging mechanisms and install scripts can grow and change, but the requirements of a great product installation experience don't waver. If we follow these requirements from the start, we will be able to overcome the challenges.

####Officially Supported Linux Versions

* CentOS 7
* RHEL (Red Hat Enterprise Linux) 7.0
* RHEL (Red Hat Enterprise Linux) 7.1
* RHEL (Red Hat Enterprise Linux) 7.2
* Debian 7.7
* Debian 8
* Ubuntu 12.04
* Ubuntu 14.04
* Ubuntu 15.10

####Package Installation Requirements

* Set up the ioFabric daemon to run as a service on system boot
	* For Ubuntu - define the service as /etc/init.d/iofabric and register it using update-rc.d
	* For Debian - define the service as /etc/init.d/iofabric and register it using update-rc.d
	* For CentOS - define the service as /etc/init.d/iofabric and register it using chkconfig
	* For RHEL - define the service as /etc/init.d/iofabric and register it using chkconfig
* Place all program files in the standard locations for each Linux version
	* For all Linux versions the directory for the executable file is /usr/bin/
	* For all Linux versions the directory for static configuration files is /etc/iofabric/
	* For all Linux versions the default directory for log files is /var/log/iofabric/
	* For all Linux versions the directory for dynamic files used during runtime is /var/lib/iofabric/
	* For all Linux versions the directory for files associated with the running daemon is /var/run/iofabric/
* Create the proper groups, users, and permissions
	* Create a group called "iofabric"
	* Create a user called "iofabric"
	* Make the iofabric user a member of the iofabric group
	* Give ownership of the installed files and directories to both the iofabric user and group
	* Give the proper permissions to the installed files and directories
* Register the executable path so the command line functionality works from anywhere
* Minimize the amount of installation text the user sees
* Clearly report the cause of installation errors on the screen if they are encountered
* Install the software as a native package for each Linux version (as a PPA for Ubuntu Linux, for example)

####Convenience Installation Script Requirements

* Focus on Ubuntu 14.04 first, then produce convenience scripts for the other Linux versions
* Create a shell script that can be downloaded and run by a root user or by anyone who can use "sudo"
* Install Docker 1.5+ as a dependency
* Install Java 8+ as a dependency
* Register the iofabric package with the installer service
* Update the installer service
* Run the package installation of iofabric

