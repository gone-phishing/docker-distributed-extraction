###################################################################
### Installing the distributed extraction framework		###
### on spark:1.3.0-ubuntu image					###
###################################################################
FROM sequenceiq/spark:1.3.0-ubuntu
MAINTAINER gonephishing <riteshoneinamillion@gmail.com>
RUN apt-get update
RUN apt-get install -y maven

RUN mkdir -p ~/.m2/repository/org/dbpedia
ADD extraction /root/.m2/repository/org/dbpedia/extraction
WORKDIR /code
ADD distributed-extraction-framework /code
RUN ["mvn", "clean"]
RUN ["mvn", "install", "-Dmaven.test.skip=true"]
