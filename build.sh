#!/bin/sh

mvn -f /home/git/bmw/pom.xml -Dmaven.test.skip=true -Dspring.profiles.active=prod clean package

rm -rf /opt/tomcat/bmw/*

cp -r /home/git/bmw/target/bmw-3.0/. /opt/tomcat/webapps/bmw/
