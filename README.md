openmrs-module-emtfrontend
==========================

The EMT (EMR Monitoring Tool) contains two components:
1. A shell backend to collect data points and store them in a dedicated log file
2. An OpenMRS module to run reports based on the log file from the shell backend

It is designed to run on top of the standard OpenMRS installation of MOH in Rwanda (OpenMRS 1.6 on Ubuntu Linux).

Unlike typical OpenMRS module projects this repository does not only contain the OpenMRS module itself, but also another package (shell-backend) for installation and invocation from a terminal session. 

The OpenMRS module is a 'mavenized' package and thus can be created through the default OpenMRS Maven conventions (via pom.xml).

The shell-backend package is created by a bash script and therefore requires a bash environment on the development system (currently only tested with MacOS, but Linux or even a cygwin environment should work with minimal adjustments) (via create-shell-backend-installation-package.sh)

Additional documentation can be found on the PIH Confluence Wiki: http://wiki.pih-emr.org/pages/viewpage.action?pageId=6127786


###Installation:

* Compile the module, must succeed

* Execute improved-installation.sh on command line like: bash improved-installation.sh openmrs167 ~/.OpenMRS http://localhost:8080
 	
	-> openmrs167 is the OpenMRS Application name (name of openmrs war file), it is normally openmrs
	-> ~/.OpenMRS should be replaced with your OpenMRS Data Directory where the modules and runtime properties file are found
	-> http://localhost:8080 should be replaced with the url to your Root tomcat
	-> Make sure the your last message reads; "You have successfully installed EMT"

* You can always execute the scripts installed under your $HOME/EmrMonitoringTool
	
	-> To generate a report on command line, run generate-example-report.sh or see an example on how to in it
	-> startup-hook.sh, heartbeat.sh and openmrs-heartbeat.sh can be run any time to log more data to show up in the next reports
	
* The included .omod file is the backend openmrs module which provides a user interface to interact with the tool, you can always use the module 
	
	-> without setting up the back end by Copying emt.log from & into your ~/.OpenMRS/EmrMonitoringTool($OpenMRS_DATA_DIR/EmrMonitoringTool) and then using the front end module
	
* You can always change the settings provided by editing your $HOME/EmrMonitoringTool/.emt-config.properties by replacing the values for each line and not the name
