#!/bin/bash

if [ "$#" -ne 3 ]; then
echo "Illegal number of parameters"
echo "Usage:  improved-installation.sh <OpenMRS APPName> <OpenMRS Data Directory> <Tomcat Root URL>"
echo ""
echo "Usage example: improved-installation.sh openmrs167 ~/.OpenMRS http://localhost:8080"
echo ""
exit 1
fi

# Important variables
EMT_DIR=$HOME/EmrMonitoringTool
OPENMRS_APP_NAME=$1
OPENMRS_DATA_DIR=$2
TOMCAT_ROOT_URL=$3
OPENMRS_URL=$TOMCAT_ROOT_URL/$OPENMRS_APP_NAME
EMT_MAIN_CONFIG=$EMT_DIR/.emt-config.properties

if [ ! -d $EMT_DIR ]; then
mkdir $EMT_DIR
fi

if [ ! -f $EMT_MAIN_CONFIG ]; then
cat <<EOF > $EMT_MAIN_CONFIG
openmrs_app_name=$OPENMRS_APP_NAME
openmrs_data_directory=$OPENMRS_DATA_DIR
openmrs_backups_directory=$OPENMRS_DATA_DIR/backups
openmrs_url=$OPENMRS_URL
EOF
echo ""
echo "INFO: You can change the provided configurations by editing: $EMT_MAIN_CONFIG";
echo ""
fi

OMRS_APP_NAME=`sed '/^\#/d' "$EMT_MAIN_CONFIG" | grep 'openmrs_app_name' | tail -n 1 | cut -d "=" -f2-`
OMRS_DATA_DIR=`sed '/^\#/d' "$EMT_MAIN_CONFIG" | grep 'openmrs_data_directory' | tail -n 1 | cut -d "=" -f2-`
OMRS_BACKUP_DIR=`sed '/^\#/d' "$EMT_MAIN_CONFIG" | grep 'openmrs_backups_directory' | tail -n 1 | cut -d "=" -f2-`
OMRS_URL=`sed '/^\#/d' "$EMT_MAIN_CONFIG" | grep 'openmrs_url' | tail -n 1 | cut -d "=" -f2-`

cp -r shell-backend/lib $EMT_DIR
cp shell-backend/*.sh $EMT_DIR
if [ ! -d $OMRS_DATA_DIR ]; then
echo "ERROR: $OMRS_DATA_DIR must exist to proceed, confirm that it is the right OpenMRS Data Directory/folder"
exit 1
fi
if [ ! -d $OMRS_DATA_DIR/EmrMonitoringTool ]; then
mkdir $OMRS_DATA_DIR/EmrMonitoringTool
fi
bash $EMT_DIR/configure.sh $EMT_DIR $OMRS_DATA_DIR

