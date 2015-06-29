# Spark on Docker test

*The project basically reads the number of characters present in the data.txt file using map and reduce operations on top of Apache Spark.*

Install docker using the docker_installer script<br>
Start the docker daemon using the following :<br>
    `sudo docker -d`

Edit the data.txt file and add any number of characters to it.<br>
Build the image using this :<br>
    `sudo docker build -t <username>/sparkdockertest .`
    
Alternatively, the image can be directly pulled from the main docker hub using :<br>
    `sudo docker pull gonephishing/sparkdockertest`
