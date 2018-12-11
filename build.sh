export JAVA_HOME=/usr/lib/jvm/java-8-oracle

git pull

mvn -Dmaven.test.skip=true -Dspring.profiles.active=prod clean package

rm -rf /opt/tomcat/bmw/*
cp -r target/bmw-3.0/. /opt/tomcat/bmw/

# systemctl restart tomcat



