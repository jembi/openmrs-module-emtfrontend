#!/bin/bash

PATH=$PATH:/sbin
EMT_DIR=$HOME/EmrMonitoringTool
EMT_MAIN_CONFIG=$EMT_DIR/.emt-config.properties
OMRS_DATA_DIR=`sed '/^\#/d' "$EMT_MAIN_CONFIG" | grep 'openmrs_data_directory' | tail -n 1 | cut -d "=" -f2-`
LOG=$OMRS_DATA_DIR/EmrMonitoringTool/emt.log

if [ ! -f $LOG ]; then
echo "ERROR: $LOG must exist to proceed, make sure you successfully run improved-installation.sh first"
exit 1
fi

SYSTEM_ID=`hostname`-`ifconfig eth0 | grep HWaddr | awk '{ print $NF}' | sed 's/://g'`
NOW=`date +%Y%m%d-%H%M%S`

# if possible add previous shutdown line based on system status
# based on /var/log/syslog (only admin users have access rights)
#LAST_SHUTDOWN_LOG=`grep "logging (proc) stopped" /var/log/syslog | tail -1`
#LAST_SHUTDOWN_DATE=`echo $LAST_SHUTDOWN_LOG | cut -c 1-15`
# based on output of command last
LAST_SHUTDOWN_LOG=`last -x | grep shutdown | head -1`
LAST_SHUTDOWN_DATE=`echo $LAST_SHUTDOWN_LOG | cut -c 39-54`
# check if already in log, if not then add
LAST_SHUTDOWN_DATE_CONV=`date --date="$LAST_SHUTDOWN_DATE" +%Y%m%d-%H%M%S`
SHUTDOWN_LOG_LINE="$LAST_SHUTDOWN_DATE_CONV;$SYSTEM_ID;SHUTDOWN"
grep "$SHUTDOWN_LOG_LINE" $LOG
if [ $? -ne 0 ]; then
  # shutdown not yet registered, do so
  echo $SHUTDOWN_LOG_LINE >> $LOG
fi

# check if last line is a shutdown and assume it was a clean shutdown
# if there is no shutdown line, then most likely the system wasn't shut down properly
tail -1 $LOG | grep SHUTDOWN
if [ $? -ne 0 ]; then
  echo "$NOW;$SYSTEM_ID;STARTUP;DIRTY" >> $LOG
else
  echo "$NOW;$SYSTEM_ID;STARTUP;CLEAN" >> $LOG
fi

