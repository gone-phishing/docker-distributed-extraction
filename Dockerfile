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

#Download extraction framework zip :
RUN wget https://github.com/dbpedia/extraction-framework/archive/master.zip
RUN unzip master.zip
#RUN ["cd", "/extraction-framework-master/"]
WORKDIR /extraction-framework-master
RUN ["mvn", "clean", "install"]
#RUN ["cd", "/"]
WORKDIR /
RUN rm -rf extraction-framework-master/
# Adding sequential extraction framework jars to the maven directory
#RUN mkdir -p ~/.m2/repository/org/dbpedia
#ADD extraction /root/.m2/repository/org/dbpedia/extraction

# Defining work directory on the image being used
WORKDIR /code

# Adding the current code base
ADD distributed-extraction-framework /code

# Cleaning previous builds
RUN ["mvn", "clean"]

# Building the framework
RUN ["mvn", "install", "-Dmaven.test.skip=true"]
