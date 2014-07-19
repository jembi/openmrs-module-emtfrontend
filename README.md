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