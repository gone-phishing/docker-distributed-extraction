#!/bin/bash

# Usage :
# args[0] = "single" | "cluster"
# args[1] = "username"
# args[2] = "hostname"
# args[3] = "privateKeyFile"
# args[4] = "key_name"
# args[5] = "image_id"
# args[6] = "count"
# args[7] = "instance_type"
# args[8] = "security_groups"
# args[9] = "instance_id1"

javac -cp ".:./lib/jsch.jar:./lib/json.jar" Spark_aws.java

# $? holds the exit code for last executed command
if [[ $? == 0 ]]; then
	java -cp ".:./lib/jsch.jar:./lib/json.jar" Spark_aws single ec2-user ec2-54-187-245-90.us-west-2.compute.amazonaws.com /home/gonephishing/Downloads/gsoc1.pem gsoc1 ami-e7527ed7 1 t2.micro "" i-73fc63b6
else
	echo "[ERROR] Compilation failed"
fi
exit