####################################################################
####  Installing the distributed extraction framework		####
####  on sequenceiq/spark:1.3.0-ubuntu image		 	####
####################################################################

FROM sequenceiq/spark:1.3.0-ubuntu
MAINTAINER gonephishing <riteshoneinamillion@gmail.com>

# Updating repositories
RUN apt-get update

# Installing maven v3.2.1
RUN apt-get install -y maven

# Download extraction framework zip 
RUN wget https://github.com/dbpedia/extraction-framework/archive/master.zip

# Unzip the extraction framework files
RUN unzip master.zip

# Define workdir for installing the extraction framework
WORKDIR /extraction-framework-master

# Install the extraction framework jars
RUN ["mvn", "clean", "install"]

# Remove the extraction-framework-master folder to free space on the image
WORKDIR /
RUN rm -rf extraction-framework-master/

# Defining work directory for the distributed extraction framework
WORKDIR /code

# Adding the current code base
ADD distributed-extraction-framework /code

# Cleaning previous builds
RUN ["mvn", "clean"]

# Building the framework
RUN ["mvn", "install", "-Dmaven.test.skip=true"]

# Success install message
RUN ["echo", "Image has been successfully built"]
