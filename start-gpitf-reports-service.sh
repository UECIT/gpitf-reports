#!/usr/bin/env sh
echo "Starting a Spring Boot GPITF Reports Service on $HOSTNAME"
exec java -jar gpitf-reports-service.war
