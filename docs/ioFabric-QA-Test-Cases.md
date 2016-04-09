# ioFabric QA Test Cases

To perform quality assurance testing on the ioFabric product, the same test steps need to be executed again and again on different Linux versions, different hardware machines, and after each update to the ioFabric product itself. Use these steps to perform proper testing. The tests validate the product functionality, the installation instructions, and the hosted installation packages.

####Installation

* Choose one of the following supported Linux versions

	* Ubuntu 12.04, 14.04, or 15.10
	* Fedora 22 or 23
	* Debian 7.7 or 8
	* CentOS 7
	* Red Hat Enterprise Linux 7

* Create a new Linux installation or use an existing one

* Use the installation instructions on the correct iotracks.com Web page

	* Ubuntu - <a href="https://iotracks.com/products/iofabric/installation/linux/ubuntu">https://iotracks.com/products/iofabric/installation/linux/ubuntu</a>

	* Debian - <a href="https://iotracks.com/products/iofabric/installation/linux/debian">https://iotracks.com/products/iofabric/installation/linux/debian</a>

	* Fedora - <a href="https://iotracks.com/products/iofabric/installation/linux/fedora">https://iotracks.com/products/iofabric/installation/linux/fedora</a>

	* Red Hat - <a href="https://iotracks.com/products/iofabric/installation/linux/rhel">https://iotracks.com/products/iofabric/installation/linux/rhel</a>

	* CentOS - <a href="https://iotracks.com/products/iofabric/installation/linux/centos">https://iotracks.com/products/iofabric/installation/linux/centos</a>

* Make sure that your Linux installation meets the minimum system requirements

	* Install Java runtime (JRE) 8 or 9 if it is not already installed (both OpenJDK and Oracle Java are suitable)

	* Install Docker 1.5 or higher if it is not already installed

* Follow the installation steps on the Web page

* After installation, type 'iofabric status' and verify that it produces information results on the screen

* Type 'iofabric start' and see that it runs the ioFabric service in your command prompt (your cursor will be "locked up" during this)

* Type the "ctrl" and "c" keys together to end the ioFabric service and see that you are returned to a fresh command prompt

* Use the instructions on the Web page to start the ioFabric daemon in the background (the command may be different for different Linux versions)

* Type 'iofabric status' to verify that the daemon is running

* Reboot your Linux machine to test the auto-starting of the ioFabric daemon

* Type 'iofabric status' after reboot to verify that it is running

* Check the information provided by the 'iofabric status' command

	* The 'iofabric daemon' value should be 'running'

	* The 'system time' value should match the Linux machine

	* The 'connection to controller' value should be 'not provisioned' at this time

* abc