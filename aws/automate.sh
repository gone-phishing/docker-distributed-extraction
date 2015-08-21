#!/bin/bash

 # Usage (for single instance):
 # args[0] = "single" | "cluster"
 # args[1] = "username"
 # args[2] = "hostname"
 # args[3] = "privateKeyFile"
 # args[4] = "key_name"
 # args[5] = "image_id"
 # args[6] = "instance_count"
 # args[7] = "instance_type"
 # args[8] = "security_groups"
 # args[9] = "instance_id1"
 # args[10] = "setup-timeout"
 #
 # Usage (for cluster setup):
 # args[0] = "single" | "cluster"
 # args[1] = "cluster_name"
 # args[2] = "ami_version"
 # args[3] = "application_name"
 # args[4] = "key_name"
 # args[5] = "instance_type"
 # args[6] = "instance_count"
 # args[7] = "username"
 # args[8] = "hostname"
 # args[9] = "privatekeyfile"
 # args[10] = "setup-timeout"

javac -cp ".:./lib/jsch.jar:./lib/json.jar" Spark_aws.java

# $? holds the exit code for last executed command
if [[ $? == 0 ]]; then
	#java -cp ".:./lib/jsch.jar:./lib/json.jar" Spark_aws single ec2-user ec2-54-187-245-90.us-west-2.compute.amazonaws.com $HOME/Downloads/gsoc1.pem gsoc1 ami-5189a661 1 t2.micro "" "" 40000
	java -cp ".:./lib/jsch.jar:./lib/json.jar" Spark_aws cluster Spark_Cluster11 3.8 Spark gsoc1 m3.xlarge 3 hadoop "" $HOME/Downloads/gsoc1.pem 40000
else
	echo "[ERROR] Compilation failed"
fi
exit