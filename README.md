# DBpedia Dockerized Distributed extraction framework

The project aims at creating Docker images for setting up and working with the distributed extraction framework. Distributing the workload is expected to provide performance enhancements over the current sequential approach. Docker does this by combining a lightweight container virtualization platform with workflows and tooling that help speedup the complete deployment process.

Scripts have been added to deploy the same on Amazon's aws and updates have been pushed for Google cloud instances. The scripts can be found in their respective directories. Also, a few utility scripts have been provided for benchmarking, performing checks on frameworks, and installing docker on a ubuntu linux system. They can be found in the uti directory of the project.

**Steps:**
- Add your download and extraction properties files in their respective directories under config folder
- Install Docker using the docker_installer script
- Build the docker image using the Dockerfile provided
- Run the image in containers to carry the download and extraction of wiki data
 

**Building an image from Dockerfile :**

`sudo docker build -t gonephishing/dbpedia .`

**Running the image built :**

`sudo docker run -i -t gonephishing/dbpedia /bin/bash`

**Pulling the image directly from the docker hub**

`sudo docker pull gonephishing/dbpedia`
