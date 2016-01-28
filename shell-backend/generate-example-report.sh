#!/bin/bash

# Convenience script to generate a test report

BASEDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

$BASEDIR/generate-report.sh 20140501 20160131 emt.pdf
