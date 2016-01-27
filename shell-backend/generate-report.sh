#!/bin/bash

# Generates a report

if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters"
    echo "Usage example: generate-report.sh 20140501 20140531 emt.pdf"
    exit 1
fi

EMT_MAIN_CONFIG=$HOME/EmrMonitoringTool/.emt-config.properties

if [ ! -f $EMT_MAIN_CONFIG ]; then
echo "ERROR: $EMT_MAIN_CONFIG must exist to proceed, make sure you successfully run improved-installation.sh first"
exit 1
fi

OMRS_DATA_DIR=`sed '/^\#/d' "$EMT_MAIN_CONFIG" | grep 'openmrs_data_directory' | tail -n 1 | cut -d "=" -f2-`
LOGFILE=$OMRS_DATA_DIR/EmrMonitoringTool/emt.log
STARTDATE=$1
ENDDATE=$2
OUTPUTPDF=$3

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

java -cp "$BASEDIR/lib/*" org.openmrs.module.emtfrontend.Emt $1 $2 $LOGFILE $OUTPUTPDF
