# Advanced Internet Computing WS 2016/2017; Group 3; Topic 2: Cloud-based RAID Storage

To compile, run and test use maven. You can use your local maven (`mvn`) or the included maven wrapper (`./mvnw`).

### Configuration
Before you start you need to setup the configuration. Do so by copying `credentials.properties.template` to `credentials.properties`:
```sh
cp src/main/resources/credentials.properties.template src/main/resources/credentials.properties
```
and edit `credentials.properties` to your needs.

#### Box

For Box authentication you need to do the following:
```sh
openssl genrsa -out private_key.pem 2048
openssl rsa -pubout -in private_key.pem -out public_key.pem
openssl pkcs8 -topk8 -inform PEM -outform DER -in private_key.pem -out private_key.pkcs8 -nocrypt
```

Then go to https://app.box.com/developers/services/ and create a new app.
To be able to use the public key authentication, you also need to enable two factor authentification for your account.
You will see an error message with a link if this is not yet enabled.

With two factor authentication enabled, you can add a public key to the app.
Click the button and then copy in the content of the public_key.pem file created earlier.

Next you need to copy the values of the following fields from the app settings page and put them in the credentials file:
 - client_id
 - client_secret
 - the key id of the key you just added

You will also need to copy the api key which is at the bottom of the page.
Then go to https://app.box.com/master/settings/openbox and authorize the app.
This will ask you for the api key you just copied previously.

Then you have to go to https://app.box.com/master/settings/account and copy the enterprise id.

You also need to add the private key in base64 encoded PKCS8 format.
You can get this by running the following command:

```
sh
base64 -w0 private_key.pkcs8
```

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

To enable DEBUG logging add `--debug`. (See [Spring doc](http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-logging.html#boot-features-logging-console-output).)

To access the REST interface you can use curl:
```sh
curl http://localhost:8080/file
```

Some examples:
```sh
curl http://localhost:8080/file
curl http://localhost:8080/file/filename
curl http://localhost:8080/file/filename/locations
curl -T filename http://localhost:8080/file/
curl -X DELETE http://localhost:8080/file/filename
```


For further commands see [RestController](src/main/java/at/ac/tuwien/infosys/aic2016/g3t2/rest/RestController.java).