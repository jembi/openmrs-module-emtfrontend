EMR-Monitoring-Tool
===================


0,15,30,45 * * * * /home/xian/heartbeat.sh 
10,25,40,55 * * * * /openmrs-heartbeat.sh 
@reboot /home/xian/emt/startup-hook.sh

