FROM java:7
MAINTAINER gonephishing <riteshoneinamillion@gmail.com>

# Install maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /code

# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
# RUN ["mvn", "dependency:resolve"]
# RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
ADD src /code/src
RUN ["mvn", "package"]

# EXPOSE 4567
# CMD ["/usr/lib/jvm/java-7-openjdk-amd64/bin/java", "-jar", "target/sparktest-jar-with-dependencies.jar"]

RUN $SPARK_HOME/bin/spark-submit --class "Test" --master local[4] target/sparktest-jar-with-dependencies.jar
