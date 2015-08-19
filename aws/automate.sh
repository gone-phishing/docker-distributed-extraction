#!/bin/bash

javac -cp ".:./lib/jsch.jar" spark_aws.java

# $? holds the exit code for last executed command
if [[ $? == 0 ]]; then
	java -cp ".:./lib/jsch.jar" spark_aws single ec2-user ec2-52-27-159-45.us-west-2.compute.amazonaws.com /home/gonephishing/Downloads/gsoc1.pem i-9d0b9a58
else
	echo "[ERROR] Compilation failed"
fi
exit