# Advanced Internet Computing WS 2016/2017; Group 3; Topic 2: Cloud-based RAID Storage

To compile, run and test use maven. You can use your local maven (`mvn`) or the included maven wrapper (`./mvnw`).

### Configuration
Before you start you need to setup the configuration. Do so by copying `credentials.properties.template` to `credentials.properties`:
```sh
cp src/main/resources/credentials.properties.template src/main/resources/credentials.properties
```
and edit `credentials.properties` to your needs.

### Compile
```sh
mvn compile
```

### Run tests
```sh
mvn test
```

### Run app
```sh
mvn spring-boot:run
```

To access the REST interface you can use curl:
```sh
curl http://localhost:8080/listFiles
```

Some examples:
```sh
curl http://localhost:8080/listFiles
curl http://localhost:8080/file/filename
curl http://localhost:8080/file/filename/locations
curl -T filename http://localhost:8080/file/
curl -X DELETE http://localhost:8080/file/filename
```


For further commands see `src/main/java/at/ac/tuwien/infosys/aic2016/g3t2/rest/RestController.java`