#!/bin/bash

PATH=$PATH:/sbin

LOG=$HOME/emt.log

SYSTEM_ID=`hostname`-`ifconfig eth0 | grep HWaddr | awk '{ print $NF}' | sed 's/://g'`
NOW=`date +%Y%m%d-%H%M%S`
#SYS_LOAD=`uptime | awk -F":" '{print $4}'`
#UPTIME=`uptime`
#SYS_LOAD_TMP="`echo $UPTIME | awk -F"," '{print $3}' | awk -F":" '{print $2}'` ;`echo $UPTIME | awk -F"," '{print $4}'` ;`echo $UPTIME | awk -F"," '{print $5}'`"
#SYS_LOAD=`echo $SYS_LOAD_TMP | tr -d " "`
SYS_LOAD=`uptime | sed "s/^.*load averages: //g" | sed "s/^.*load average: //g" | tr -d " " | sed "s/,/;/g"`
#SYS_LOAD2=`uptime`
NUMBER_PROCESSORS=`grep processor /proc/cpuinfo | wc -l`
MEM_TOTAL=`free -m | grep Mem | awk -F" " '{print $2}'`
MEM_FREE=`free -m | grep "-" | awk -F" " '{print $4}'`
SDA1_DISK_TOTAL=`df --total / | grep sda1 | awk -F" " '{ print $2 }'`
SDA1_DISK_FREE=`df --total / | grep sda1 | awk -F" " '{ print $4 }'`
SDA1_DISK_USE=`df --total / | grep sda1 | awk -F" " '{ print $5 }'`
DISK_TOTAL=`df --total / | grep total | awk -F" " '{ print $2 }'`
DISK_FREE=`df --total / | grep total | awk -F" " '{ print $4 }'`
DISK_USE=`df --total / | grep total | awk -F" " '{ print $5 }'`

echo "$NOW;$SYSTEM_ID;HEARTBEAT;$SYS_LOAD;$NUMBER_PROCESSORS;$MEM_TOTAL;$MEM_FREE;$SDA1_DISK_TOTAL;$SDA1_DISK_FREE;$SDA1_DISK_USE;$DISK_TOTAL;$DISK_FREE;$DISK_USE" >> $LOG
