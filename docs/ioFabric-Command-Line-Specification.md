# Command Line Specification

As a service intended to run constantly in the background (also known as a daemon), the ioFabric software needs to respond to shell commands from the user. This document defines all of the commands that the software needs to accept and the exact structure of the commands and responses.

We will follow the guidelines set forth in the <a href="http://www.gnu.org/prep/standards/standards.html#Command_002dLine-Interfaces">GNU Coding Standards document regarding command line interfaces</a>.

All command line outputs are sent to the "standard out" stream.

The root command is the executable keyword. When using a text editor such as "nano" you simply type "nano xyz.xml" if you want to edit an XML file in the current directory. The executable keyword is "nano" and the parameter that follows is the file to open in the nano editor.

The root command keyword for the ioFabric product is "iofabric" in all lowercase letters. If a user only types "iofabric" they should be presented with the help options displayed as if they typed "iofabric -h" or "iofabric --help" or "iofabric -?" to access the help menu.

####Help Menu

#####Accepted Inputs

<pre>
iofabric
iofabric help
iofabric --help
iofabric -h
iofabric -?
</pre>

#####Output

<pre>
Usage: iofabric [OPTIONS] COMMAND [arg...]

Option                   GNU long option              Meaning
======                   ===============              =======
-h, -?                   --help                       Show this message
-v                       --version                    Display the software version and license information


Command                  Arguments                    Meaning
=======                  =========                    =======
help                                                  Show this message
version                                               Display the software version and license information
status                                                Display current status information about the software
start                                                 Start the ioFabric daemon which runs in the background
stop                                                  Stop the ioFabric daemon
restart                                               Stop and then start the ioFabric daemon
provision                &lt;provisioning key&gt;           Attach this software to the configured ioFabric controller
deprovision                                           Detach this software from all ioFabric controllers
info                                                  Display the current configuration and other information about the software
config                   [OPTION] [VALUE]             Change the software configuration according to the options provided
                         -d &lt;#GB Limit&gt;               Set the limit, in GiB, of disk space that the software is allowed to use
                         -dl &lt;dir&gt;                    Set the directory to use for disk storage
                         -m &lt;#MB Limit&gt;               Set the limit, in MiB, of memory that the software is allowed to use
                         -p &lt;#cpu % Limit&gt;            Set the limit, in percentage, of CPU time that the software is allowed to use
                         -a &lt;uri&gt;                     Set the uri of the fabric controller to which this software connects
                         -ac &lt;filepath&gt;               Set the file path of the SSL/TLS certificate for validating the fabric controller identity
                         -c &lt;uri&gt;                     Set the UNIX socket or network address that the Docker daemon is using
                         -n &lt;network adapter&gt;         Set the name of the network adapter that holds the correct IP address of this machine


Report bugs to: kilton@iotracks.com
ioFabric home page: http://iotracks.com
</pre>



####Display ioFabric Version

#####Accepted Inputs

<pre>
iofabric version
iofabric --version
iofabric -v
</pre>

#####Output

<pre>
TBD
</pre>



####Display ioFabric Status

#####Accepted Inputs

<pre>
iofabric status
</pre>

#####Output

<pre>
TBD
</pre>



####Start ioFabric

#####Accepted Inputs

<pre>
iofabric start
</pre>

#####Output

<pre>
TBD
</pre>



####Stop ioFabric

#####Accepted Inputs

<pre>
iofabric stop
</pre>

#####Output

<pre>
TBD
</pre>



####Restart ioFabric

#####Accepted Inputs

<pre>
iofabric restart
</pre>

#####Output

<pre>
TBD
</pre>



####Provision this ioFabric instance to a controller 

#####Accepted Inputs

<pre>
iofabric provision D98we4sd

* The provisioning key entered by the user takes the place of the D98we4sd above
</pre>

#####Output

<pre>
TBD
</pre>



####De-provision this ioFabric instance (removed from any controller)

#####Accepted Inputs

<pre>
iofabric deprovision
</pre>

#####Output

<pre>
TBD
</pre>



####Show ioFabric information

#####Accepted Inputs

<pre>
iofabric info
</pre>

#####Output

<pre>
TBD
</pre>



####Change ioFabric configuration

#####Accepted Inputs

<pre>
iofabric config -d 17.5
iofabric config -dl ~/temp/spool/
iofabric config -m 568
iofabric config -p 82.0
iofabric config -a https://250.17.0.200/controllers/7/
iofabric config -ac ~/temp/certs/controller_identity_proof.crt
iofabric config -c unix:///var/run/docker.sock
iofabric config -n eth0

* Any combination of parameters listed here can be entered on the command line simultaneously
* for example, iofabric config -m 2048 -p 80.0 -n wlan0
</pre>

#####Output

<pre>
TBD
</pre>

