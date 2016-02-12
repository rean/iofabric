#!/bin/bash

DEBIAN_NAME=$(lsb_release -sc)

DOCKERINSTALLED=$(dpkg -l | grep -w "docker.io")
if [ "$DOCKERINSTALLED" != "" ]; then
    echo "Docker installed, checking version..."

	DOCKERVERSION=$(docker -v | perl -pe '($_)=/([0-9]+([.][0-9]+)+)/')
	DOCKERNUMBER=$(echo $DOCKERVERSION | sed 's/\.//' )
	DOCKERBIGVERSION=$(echo "$DOCKERNUMBER * 100/1" | bc)

	if [ $DOCKERBIGVERSION -gt 1299 ]
	then
        	echo "Docker is usable at version $DOCKERVERSION"
	else
        	echo "Docker version is too low"
        	echo "Installing latest version of Docker..."
		
		sudo apt-get update
		sudo apt-get install -q -y apt-transport-https ca-certificates
		sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
		sudo echo "TMP" >> /etc/apt/sources.list.d/docker.list
		sudo rm /etc/apt/sources.list.d/docker.list
		sudo echo "deb https://apt.dockerproject.org/repo debian-$DEBIAN_NAME main" >> /etc/apt/sources.list.d/docker.list
		sudo apt-get update
		sudo apt-cache policy docker-engine
		sudo apt-get update
		sudo apt-get install -q -y docker-engine

        	NEWDOCKERVERSION=$(docker -v | perl -pe '($_)=/([0-9]+([.][0-9]+)+)/')
        	echo "Now Docker is version $NEWDOCKERVERSION"
	fi
else
	echo "Docker not installed."
	echo "Installing latest version of Docker..."

	sudo apt-get update
	sudo apt-get install -q -y apt-transport-https ca-certificates
	sudo apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
	sudo echo "TMP" >> /etc/apt/sources.list.d/docker.list
	sudo rm /etc/apt/sources.list.d/docker.list
	sudo echo "deb https://apt.dockerproject.org/repo debian-$DEBIAN_NAME main" >> /etc/apt/sources.list.d/docker.list
	sudo apt-get update
	sudo apt-cache policy docker-engine
	sudo apt-get update
	sudo apt-get install -q -y docker-engine

        NEWDOCKERVERSION=$(docker -v | perl -pe '($_)=/([0-9]+([.][0-9]+)+)/')
        echo "Now Docker is version $NEWDOCKERVERSION"
fi

#echo "Ensuring Docker runs in the background as a service on boot..."
#sudo ln -sf /usr/bin/docker.io /usr/local/bin/docker
#sudo sed -i '$acomplete -f _docker docker' /etc/bash_completion.d/docker.io
#sudo update-rc.d docker.io defaults

echo "Docker setup is complete\n"

JAVAINSTALLED=$(dpkg -l | grep -w "java")
if [ "$JAVAINSTALLED" != "" ]; then
    echo "Java installed, checking version..."

	JAVAVERSION=$(java -version 2>&1 | perl -pe 'if(($v)=/([0-9]+([.][0-9]+)+)/){print"$v";exit}$_=""')
	JAVANUMBER=$(echo $JAVAVERSION | sed 's/\.//' )
	JAVABIGVERSION=$(echo "$JAVANUMBER * 100/1" | bc)

        if [ $JAVABIGVERSION -gt 1790 ]
        then
                echo "Java is usable at version $JAVAVERSION"
        else
                echo "Java version is too low"
                echo "Installing Java 8 from Oracle..."

		sudo echo "TMP" >> tee /etc/apt/sources.list.d/webupd8team-java.list
		sudo rm /etc/apt/sources.list.d/webupd8team-java.list
		sudo echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
		sudo echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
		sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
		sudo apt-get update
		sudo apt-get install -q -y oracle-java8-installer
		sudo sudo apt-get install -q -y oracle-java8-set-default

                NEWJAVAVERSION=$(java -version 2>&1 | perl -pe 'if(($v)=/([0-9]+([.][0-9]+)+)/){print"$v";exit}$_=""')
                echo "Now Java is version $NEWJAVAVERSION"
        fi
else
	echo "Java not installed."
        echo "Installing Java 8 from Oracle..."

	sudo echo "TMP" >> tee /etc/apt/sources.list.d/webupd8team-java.list
	sudo rm /etc/apt/sources.list.d/webupd8team-java.list
	sudo echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee /etc/apt/sources.list.d/webupd8team-java.list
	sudo echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu trusty main" | tee -a /etc/apt/sources.list.d/webupd8team-java.list
	sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys EEA14886
	sudo apt-get update
	sudo apt-get install -q -y oracle-java8-installer
	sudo sudo apt-get install -q -y oracle-java8-set-default

        NEWJAVAVERSION=$(java -version 2>&1 | perl -pe 'if(($v)=/([0-9]+([.][0-9]+)+)/){print"$v";exit}$_=""')
        echo "Now Java is version $NEWJAVAVERSION"
fi

echo "Java setup is complete\n" 

echo "Setting up iofabric to run as a service on boot..."

SOURCE="$0"
while [ -h "$SOURCE" ]; do 
	DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
	SOURCE="$(readlink "$SOURCE")"
	[[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" 
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

SERVICE_NAME=iofabric
SERVICE_DIR=/etc/init.d
BIN_DIR=/usr/bin

apt-get install acl

LOG_PATH=$DIR/log
GROUP_NAME=iofabric
if grep -q "^${GROUP_NAME}:" /etc/group
	then
		delgroup $GROUP_NAME
fi
groupadd $GROUP_NAME

mkdir $LOG_PATH
# SET LOG DIRECTORY PERMISSIONS
chmod 774 -R $LOG_PATH
chown -R :$GROUP_NAME $LOG_PATH
chmod g+s -R $LOG_PATH
setfacl -d -m g::rwx -R $LOG_PATH

# SET APP DIRECTORY PERMISSIONS
chmod 774 -R $DIR
chown -R :$GROUP_NAME $DIR
chmod g+s -R $DIR
setfacl -d -m g::rwx -R $DIR

cd $DIR >> /tmp/$SERVICE_NAME-install

echo "BLAH BLAH BLAH" >> $SERVICE_DIR/$SERVICE_NAME
rm $SERVICE_DIR/$SERVICE_NAME >> /tmp/$SERVICE_NAME-install
sed "11 a SERVICE_NAME=$SERVICE_NAME\nPATH_TO_JAR=$DIR" $SERVICE_NAME-service >> $SERVICE_DIR/$SERVICE_NAME
chmod 774 $SERVICE_DIR/$SERVICE_NAME >> /tmp/$SERVICE_NAME-install
update-rc.d $SERVICE_NAME defaults >> /tmp/$SERVICE_NAME-install

echo "BLAH BLAH BLAH" >> $BIN_DIR/$SERVICE_NAME
rm $BIN_DIR/$SERVICE_NAME >> /tmp/$SERVICE_NAME-install
sed "2 a SERVICE_NAME=$SERVICE_NAME\nPATH_TO_JAR=$DIR" $SERVICE_NAME-console >> $BIN_DIR/$SERVICE_NAME
chmod 774 $BIN_DIR/$SERVICE_NAME >> /tmp/$SERVICE_NAME-install

echo "Done!"

