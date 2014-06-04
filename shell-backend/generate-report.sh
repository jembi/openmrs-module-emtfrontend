#!/bin/bash

# Generates a report

if [ "$#" -ne 3 ]; then
    echo "Illegal number of parameters"
    echo "Usage example: generate-report.sh 20140501 20140531 emt.pdf"
    exit 1
fi

STARTDATE=$1
ENDDATE=$2
OUTPUTPDF=$3

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LOGFILE=$HOME/emt.log

java -cp "$BASEDIR/lib/*" org.openmrs.module.emtfrontend.Emt $1 $2 $LOGFILE $OUTPUTPDF
