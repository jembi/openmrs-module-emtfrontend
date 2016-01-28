#!/bin/bash
if [ "$#" -ne 2 ]; then
echo "Illegal number of parameters"
echo ""
echo "Usage example: configure.sh $HOME/EmrMonitoringTool /val/lib/OpenMRS"
echo ""
exit 1
fi

#$1 contains emt backend directory and $2 openmrs data directory
INSTALL_DIR=$1
LOG=$2/EmrMonitoringTool/emt.log
CONFIG=$2/EmrMonitoringTool/emt.properties
SYSTEM_ID=`hostname`-`ifconfig eth0 | grep HWaddr | awk '{ print $NF}' | sed 's/://g'`
NOW=`date +%Y%m%d-%H%M%S`

# remove old cronjobs
crontab -l | grep -v heartbeat.sh | crontab -
crontab -l | grep -v openmrs-heartbeat.sh | crontab -
crontab -l | grep -v startup-hook.sh | crontab -

(crontab -l ; echo "1,16,31,46 * * * * $INSTALL_DIR/heartbeat.sh") | crontab -
(crontab -l ; echo "2,17,32,47 * * * * $INSTALL_DIR/openmrs-heartbeat.sh") | crontab -
(crontab -l ; echo "@reboot $INSTALL_DIR/startup-hook.sh") | crontab -

echo "$NOW;$SYSTEM_ID;EMT-INSTALL;0.5" >> $LOG

# create properties file if necessary
if [[ ! -f $CONFIG ]]; then
  echo ""
  echo "Creating default config file for clinic times"
  echo "clinicStart=800" >> $CONFIG
  echo "clinicEnd=1700" >> $CONFIG
  echo "clinicDays=Mo,Tu,We,Th,Fr" >> $CONFIG
fi
chmod 666 $CONFIG

# Check system time
echo ""
echo "ATTENTION: Please check the date and time of this system!"
echo "           Current date and time are: `date`"
echo ""
echo "           If this does NOT match the current real time, please report this!"
echo "           (Any difference of more than 5 minutes)"

# Check write permission for tomcat6 in modules directory
# TODO remove this check if proved un-necessary
MODULES_OWNER=`stat -c '%U' $2/modules | tail`
if [ "$MODULES_OWNER" != "tomcat6" ]; then
  echo ""
  echo "WARNING: OpenMRS modules most likely can NOT be uploaded with OpenMRS!" 
fi
