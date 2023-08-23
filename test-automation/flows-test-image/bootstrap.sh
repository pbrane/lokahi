#!/bin/bash

set -u
UDPGEN=""
SNMP=""

snmpd -f $@

# Exit immidiately in case of any errors or when we have interactive terminal
if [[ $? != 0 ]] || test -t 0; then exit $?; fi
