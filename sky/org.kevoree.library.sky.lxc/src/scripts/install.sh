#!/bin/bash
su -
idNode=`cat /etc/hostname`
kevoreeVersion="2.0.0-SNAPSHOT"
watchdogVersion="0.11"

echo "Writing sources.list"
echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" | tee -a /etc/apt/sources.list
echo "deb-src http://ppa.launchpad.net/webupd8team/java/ubuntu precise main" | tee -a /etc/apt/sources.list# Update local software list
apt-get update
 
echo "Accept Oracle software license (only required once)"
echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true |   /usr/bin/debconf-set-selections
 
echo "Install Oracle JDK7"
apt-get install oracle-java7-installer --force-yes -y

apt-get install lxc  --force-yes -y

apt-get install  bridge-utils debootstrap --force-yes -y

#check lxc config
checklxc = `lxc-checkconfig | grep disabled`
if [ "$checklxc" -eq 1 ]
        then
    	echo "Your kernel configure is not supporting LXC"
	exit -1
    fi

#configure bridge
 cat >> "/etc/network/interfaces" << EOF
auto br0
iface br0 inet dhcp
    bridge_ports eth0
    bridge_stp off
    bridge_fd 0
    bridge_maxwait 0
EOF

#configure lxc bridge
 cat > "/etc/lxc/lxc.conf" << EOF
lxc.network.type=veth
lxc.network.link=br0
lxc.network.flags=up
EOF

# br0
cat > "/etc/default/lxc" << EOF
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

 cat > "/etc/lxc/default.conf" << EOF
lxc.network.type=veth
lxc.network.link=br0
lxc.network.flags=up
EOF


#create model 
#TODO CHANGE NODENAME
touch /etc/kevoree/bootmodel
 cat > "/etc/kevoree/bootmodel" << EOF
{
merge 'mvn:org.kevoree.corelibrary.sky/org.kevoree.library.sky.lxc/$kevoreeVersion'
merge 'mvn:org.kevoree.corelibrary.javase/org.kevoree.library.javase.jexxus/$kevoreeVersion'
addNode $idNode:LxcHostNode
addGroup sync:BasicGroup
addToGroup sync $idNode
}
EOF
echo "Configure Kevoree Watchdog"
apt-get install wget 
wget http://oss.sonatype.org/content/repositories/releases/org/kevoree/watchdog/org.kevoree.watchdog/$watchdogVersion/org.kevoree.watchdog-$watchdogVersion.deb
dpkg -i org.kevoree.watchdog-$watchdogVersion.deb
rm org.kevoree.watchdog-$watchdogVersion.deb*

echo "NAT"
echo 1 > /proc/sys/net/ipv4/ip_forward

# TODO change watchdog user to be root ( lxc must be run as root)
# reboot 
reboot
