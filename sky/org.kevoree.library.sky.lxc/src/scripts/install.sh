#!/bin/bash
#
# jedartois@gmail.com
#
# script install and configure lxc for ubuntu
# Tested with Ubuntu Server 13.04 kernel 3.8

idNode=`cat /etc/hostname`
kevoreeVersion="2.0.0-SNAPSHOT"
watchdogVersion="0.12"

echo "Can you please tell me the version of kevoree ?"
read kevoreeVersion


echo "Updating /etc/apt/sources.list"
sudo cp /etc/apt/sources.list /etc/apt/sources.list.back
sudo echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" | tee -a /etc/apt/sources.list
sudo echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" | tee -a /etc/apt/sources.list# Update local software list
sudo apt-get update
 
echo "Accept Oracle software license (only required once)"
sudo echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true |   /usr/bin/debconf-set-selections
 
echo "Install Oracle JDK7"
sudo apt-get install oracle-java7-installer --force-yes -y

echo "Install LXC"
sudo apt-get install lxc  --force-yes -y

echo "Install bridge-utils debootstrap"
sudo apt-get install  bridge-utils debootstrap --force-yes -y

echo "Checking lxc-checkconfig"
#check lxc config
checklxc = `lxc-checkconfig | grep disabled`
if [ "$checklxc" -eq 1 ]
        then
    	echo "Your kernel configure is not supporting LXC"
	exit -1
    fi

echo "Configure network"
#configure bridge
sudo cat >> "/etc/network/interfaces" << EOF


auto br0
iface br0 inet dhcp
    bridge_ports eth0
    bridge_stp off
    bridge_fd 0
    bridge_maxwait 0
EOF

echo "Configure LXC"
#configure lxc bridge
sudo cat > "/etc/lxc/lxc.conf" << EOF
lxc.network.type=veth
lxc.network.link=br0
lxc.network.flags=up
EOF

# br0
sudo cat > "/etc/default/lxc" << EOF
# MIRROR to be used by ubuntu template at container creation:
# Leaving it undefined is fine
#MIRROR="http://archive.ubuntu.com/ubuntu"
# or 
#MIRROR="http://<host-ip-addr>:3142/archive.ubuntu.com/ubuntu"

# LXC_AUTO - whether or not to start containers symlinked under
# /etc/lxc/auto
LXC_AUTO="true"

# Leave USE_LXC_BRIDGE as "true" if you want to use lxcbr0 for your
# containers.  Set to "false" if you'll use virbr0 or another existing
# bridge, or mavlan to your host's NIC.
USE_LXC_BRIDGE="true"

# If you change the LXC_BRIDGE to something other than lxcbr0, then
# you will also need to update your /etc/lxc/default.conf as well as the
# configuration (/var/lib/lxc/<container>/config) for any containers
# already created using the default config to reflect the new bridge
# name.
# If you have the dnsmasq daemon installed, you'll also have to update
# /etc/dnsmasq.d/lxc and restart the system wide dnsmasq daemon.
LXC_BRIDGE="br0"
LXC_SHUTDOWN_TIMEOUT=120

EOF

sudo cat > "/etc/lxc/default.conf" << EOF
lxc.network.type=veth
lxc.network.link=br0
lxc.network.flags=up
EOF


echo "Configure Kevoree Watchdog"
sudo apt-get install wget
sudo wget http://oss.sonatype.org/content/repositories/releases/org/kevoree/watchdog/org.kevoree.watchdog/$watchdogVersion/org.kevoree.watchdog-$watchdogVersion.deb
sudo dpkg -i org.kevoree.watchdog-$watchdogVersion.deb
sudo rm org.kevoree.watchdog-$watchdogVersion.deb*

echo "configure NAT"
sudo echo 1 > /proc/sys/net/ipv4/ip_forward

echo "Configure kevoree watchdog"
sudo cat > "/etc/kevoree/config" << EOF
KEVOREE_VERSION=$kevoreeVersion
NODE_NAME=$(hostname)
PING_PORT=9999
PING_TIMEOUT=3000
EOF

#create model
echo "Configure kevoree bootstrapmodel"
sudo touch /etc/kevoree/bootmodel
sudo cat > "/etc/kevoree/bootmodel" << EOF
{
merge 'mvn:org.kevoree.corelibrary.sky/org.kevoree.library.sky.lxc/$kevoreeVersion'
merge 'mvn:org.kevoree.corelibrary.javase/org.kevoree.library.javase.jexxus/$kevoreeVersion'
addNode $idNode:LxcHostNode
addGroup sync:BasicGroup
addToGroup sync $idNode
}
EOF

# TODO /etc/init.d/kevoree watchdog user to be root ( lxc must be run as root)
echo "Reboot"
sudo sleep 5

# reboot 
sudo reboot
