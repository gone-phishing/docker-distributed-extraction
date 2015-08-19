# Docker distributed extraction on AWS (Amazon Web Services)

The directory contains two methods for building and running the Docker image on aws. First one is through aws-ec2 setup. These are basically virtual servers in cloud. Script has been written to automate the complete task and instructions have been elaborated below. Second method is via aws-beanstalk servers. They have pre-configured docker setup part and so we just need to add our Dockerfile and a configuration file for setting up the server to deploy our image on them. All the files are listed in this directory.

**Pre-requesites for a aws ec2 setup :**
 - Install aws-cli on your pc
 - Add your ssh public key for an independent IAM user on aws settings
 - Download the credentials (access key, secret key) and set them up for your aws-cli
 - Ensure atleast java 1.7 is installed

**Running the ec2 setup :**

Update the automate.sh script with correct arguments for execution. After that just run the script using :

`./automate.sh`

This will first compile the Spark_aws.java file using the jars put in the lib/ folder. Then it executes the file that does the following :
 - Launch an aws-ec2 instance of your specified type
 - Get the public dns for the instance
 - Add tags to this instance
 - Executes all the desired shell commands on this instance
 - Stop / Terminate the instance
