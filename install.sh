#!/bin/bash

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
        	sudo add-apt-repository ppa:docker-maint/testing
        	sudo apt-get update
        	sudo apt-get install -q -y -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" docker.io
        	NEWDOCKERVERSION=$(docker -v | perl -pe '($_)=/([0-9]+([.][0-9]+)+)/')
        	echo "Now Docker is version $NEWDOCKERVERSION"
	fi
else
	echo "Docker not installed."
	echo "Installing latest version of Docker..."
	sudo add-apt-repository ppa:docker-maint/testing
        sudo apt-get update
        sudo apt-get install -q -y -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" docker.io
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

		sudo add-apt-repository ppa:webupd8team/java
		sudo apt-get update
		echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
		sudo apt-get install -q -y -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" oracle-java8-installer
                NEWJAVAVERSION=$(java -version 2>&1 | perl -pe 'if(($v)=/([0-9]+([.][0-9]+)+)/){print"$v";exit}$_=""')
                echo "Now Java is version $NEWJAVAVERSION"
        fi
else
	echo "Java not installed."
        echo "Installing Java 8 from Oracle..."

	sudo add-apt-repository ppa:webupd8team/java
        sudo apt-get update
        echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
        sudo apt-get install -q -y -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" oracle-java8-installer

        NEWJAVAVERSION=$(java -version 2>&1 | perl -pe 'if(($v)=/([0-9]+([.][0-9]+)+)/){print"$v";exit}$_=""')
        echo "Now Java is version $NEWJAVAVERSION"
fi

echo "Java setup is complete\n" 

apt-get install acl >> /tmp/$SERVICE_NAME-install

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
CONFIG_DIR=/etc/$SERVICE_NAME
LOG_DIR=/var/log/$SERVICE_NAME
WORKING_DIR=/var/lib/$SERVICE_NAME
DAEMON_DIR=/var/run/$SERVICE_NAME

GROUP_NAME=iofabric
if grep -q "^${GROUP_NAME}:" /etc/group
	then
		delgroup $GROUP_NAME
fi
groupadd $GROUP_NAME

createDir() {
	mkdir $1  >> /tmp/$SERVICE_NAME-install
	# SET DIRECTORY PERMISSIONS
	chmod 774 -R $1 >> /tmp/$SERVICE_NAME-install
	chown -R :$GROUP_NAME $1 >> /tmp/$SERVICE_NAME-install
	chmod g+s -R $1 >> /tmp/$SERVICE_NAME-install
	setfacl -d -m g::rwx -R $1 >> /tmp/$SERVICE_NAME-install
}

createDir $CONFIG_DIR
createDir $LOG_DIR
createDir $WORKING_DIR
createDir $DAEMON_DIR


cd $DIR >> /tmp/$SERVICE_NAME-install
cp config/config.xml $CONFIG_DIR >> /tmp/$SERVICE_NAME-install
cp iofabric.jar $BIN_DIR >> /tmp/$SERVICE_NAME-install

mv /dev/random /dev/random.real
ln -s /dev/urandom /dev/random

echo "BLAH BLAH BLAH" >> $SERVICE_DIR/$SERVICE_NAME
rm $SERVICE_DIR/$SERVICE_NAME >> /tmp/$SERVICE_NAME-install
sed "11 a SERVICE_NAME=$SERVICE_NAME\nPATH_TO_JAR=$BIN_DIR" $SERVICE_NAME-service >> $SERVICE_DIR/$SERVICE_NAME
chmod 774 $SERVICE_DIR/$SERVICE_NAME >> /tmp/$SERVICE_NAME-install
update-rc.d $SERVICE_NAME defaults >> /tmp/$SERVICE_NAME-install

echo "BLAH BLAH BLAH" >> $BIN_DIR/$SERVICE_NAME
rm $BIN_DIR/$SERVICE_NAME >> /tmp/$SERVICE_NAME-install
sed "2 a SERVICE_NAME=$SERVICE_NAME\nPATH_TO_JAR=$BIN_DIR" $SERVICE_NAME-console >> $BIN_DIR/$SERVICE_NAME
chmod 774 $BIN_DIR/$SERVICE_NAME >> /tmp/$SERVICE_NAME-install
ln -sf $BIN_DIR/$SERVICE_NAME /usr/local/bin/$SERVICE_NAME >> /tmp/$SERVICE_NAME-install

echo "Done!"

