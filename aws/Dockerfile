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

# Changing to home directory
WORKDIR /

# Deleting the extraction framework zip and extracted files
RUN rm -rf extraction-framework-master/
RUN rm master.zip

# Downloading and extracting the distributed extracted framework zip from github
RUN wget https://github.com/gone-phishing/distributed-extraction-framework/archive/spark_1.3.0-update.zip
RUN unzip spark_1.3.0-update.zip

# Removing the zip from drive
RUN rm spark_1.3.0-update.zip

# Defining the work directory for dist-extraction framework
WORKDIR /distributed-extraction-framework-spark_1.3.0-update

# Cleaning previous builds
RUN ["mvn", "clean"]

# Building the framework (skipping the tests)
RUN ["mvn", "install", "-Dmaven.test.skip=true"]

# Make directory for main jar
RUN mkdir -p distributed/target/

# Copy and rename the extraction jar to distribution jar
RUN cp extraction/target/extraction-4.0-SNAPSHOT.jar distributed/target/
RUN mv distributed/target/extraction-4.0-SNAPSHOT.jar distributed/target/distributed-4.0-SNAPSHOT.jar 

# Exposing port for host to use
EXPOSE 12000

# Executing download module to get zip files for li and bn languages
RUN ["./run", "seq-download", "config=download/src/test/resources/download.properties"]
