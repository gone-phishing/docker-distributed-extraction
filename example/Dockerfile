FROM sequenceiq/spark:1.3.0-ubuntu

# Install maven
RUN apt-get update
RUN apt-get install -y maven

# Change working directory to /code
WORKDIR /code

# Add pom file for the project
ADD pom.xml /code/pom.xml
ADD dependency-reduced-pom.xml /code/dependency-reduced-pom.xml
# Add the data file for which the number of characters is to be counted
ADD data.txt /code/data.txt

# Add java source files
ADD src /code/src

# Clean existing jars
RUN ["mvn", "clean"]
RUN ["mvn", "dependency:resolve"]
#RUN ["mvn", "verify"]

# Make the compiled jar for the project
#RUN ["mvn", "package"]

# Expose port for host to use
#EXPOSE 4567

# Run the jar file made to count the number of characters
#CMD ["/usr/lib/jvm/java-7-openjdk-amd64/bin/java", "-jar", "target/testDocker-1.0-SNAPSHOT.jar"]

