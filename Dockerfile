FROM sequenceiq/spark:latest
MAINTAINER gonephishing <riteshoneinamillion@gmail.com>
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /code
ADD distributed-extraction-framework /code/distributed-extraction-framework
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]
RUN ["mvn", "clean install"]
