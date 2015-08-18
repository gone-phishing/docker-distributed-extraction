#!/bin/bash

javac -cp ".:./lib/jsch.jar" spark_aws.java
java -cp ".:./lib/jsch.jar" spark_aws single ec2-user ec2-52-27-159-45.us-west-2.compute.amazonaws.com /home/gonephishing/Downloads/gsoc1.pem