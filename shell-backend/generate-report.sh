#!/bin/bash

STARTDATE=$1
ENDDATE=$2
OUTPUTPDF=$3

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
LOGFILE=$HOME/emt.log

java -cp "$BASEDIR/lib/*" org.openmrs.module.emtfrontend.Emt $1 $2 $LOGFILE $OUTPUTPDF
