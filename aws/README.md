# Docker distributed extraction on AWS (Amazon Web Services)

The directory contains two methods for building and running the Docker image on aws. First one is through the aws-ec2 setup. aws-ec2 instances are basically virtual servers in cloud. Scripts have been written to automate the complete task and instructions are mentioned below. The second method is via the aws-beanstalk servers. They already have pre-configured the docker setup part and so all we need to do is add our Dockerfile and a configuration file(for setting up the server) to deploy our image on them. All the required files are listed in this directory.

**Pre-requesites for a aws ec2 setup :**
 - Python 2 version 2.6.5+ or Python 3 version 3.3+
 - Install aws-cli on your system

`sudo pip install awscli`

 - Add your ssh public key for an independent IAM user on aws settings
 - Download the credentials (access key, secret key) and set them up for your aws-cli
 - Ensure atleast java 1.7 is installed

**Running the ec2 setup :**

Update the automate.sh script with correct parameters for execution. After that just run the script using :

`./automate.sh`

This will first compile the Spark_aws.java file using the jars put in the lib/ folder. Then it executes the file that does the following :
 - Launch an aws-ec2 instance of your specified type
 - Get the public dns of the instance
 - Add tags to this instance
 - Executes all the desired shell commands on this instance
 - Stop / Terminate the instance
