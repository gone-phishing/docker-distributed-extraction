# DBpedia Dockerized Distributed extraction framework

The project aims at creating Docker images for setting up and working with the distributed extraction framework. Distributing the workload is expected to provide performance enhancements over the current sequential approach. Docker does this by combining a lightweight container virtualization platform with workflows and tooling that help speedup the complete deployment process.

**Building an image from Dockerfile :**
`sudo docker build -t sparkexample .`

**Running the image built :**
`sudo docker run -d -p 4567:4567 sparkexample`
