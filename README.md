Cassandra project about distributed factories that are using machines to create parts of products.
The product is something that each client request from time to time.

To run this project you must have cassandra running for example in docker.
If you are using windows you can use this instruction to install docker:
https://www.youtube.com/watch?v=cMyoSkQZ41E

Then you can run docker container with cassandra using this command in terminal:
docker pull cassandra
docker run --name cassandra -p 127.0.0.1:9042:9042 -p 127.0.0.1:9160:9160 -d cassandra
docker ps
docker exec -it <id of container> bash
cqlsh

and here you need to copy content of file create_schema.cql(change "replication_factor" to 1) and restart_schema.cql (or run this as files before typing cqlsh).


Now in an another terminal use this command from project location:
gradle build
gradle run

Have fun!