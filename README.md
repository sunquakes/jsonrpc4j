# jsonrpc4j
## Installing
- maven
```xml
<!-- https://mvnrepository.com/artifact/com.sunquakes/jsonrpc4j -->
<dependency>
    <groupId>com.sunquakes</groupId>
    <artifactId>jsonrpc4j</artifactId>
    <version>1.0.0</version>
</dependency>
```
- gradle
```groovy
// https://mvnrepository.com/artifact/com.sunquakes/jsonrpc4j
implementation group: 'com.sunquakes', name: 'jsonrpc4j', version: '1.0.0'
```


## Getting started
### step1. Configuring base packages with @JsonRpcScan to scan service and client class
- method 1. Used with the @Configuration
```java
@Configuration
@JsonRpcScan({"com.sunquakes"})
public class JsonRpcConfig {
}
```
- method 2. Used in the Application class
```java
@SpringBootApplication
@JsonRpcScan({"com.sunquakes.jsonrpc4j.spring.boot"})
public class JsonRpcApplication {
    public static void main(String[] args) {
        SpringApplication.run(JsonRpcApplication.class, args);
    }
}
```
### step2. Configuring server
- Add configuration items to application.properties
```properties
# The protocol of the server, Value can be tcp or http.
jsonrpc.server.protocol=tcp
# The port of the server listening.
jsonrpc.server.port=3200

# If the protocol is http, the next two property can be ignore.
# Symbol for the end of data receive once.
jsonrpc.server.package-eof=\r\n
# Buffer size of the server receive data.
jsonrpc.server.package-max-length=2097152
```
### step3. Create service interface and class
- Create a new interface file named IJsonRpcService with @JsonRpcService
```java
@JsonRpcService("JsonRpc")
public interface IJsonRpcService {
    int add(int a, int b);
}
```
- Create a new class file name JsonRpcServiceImpl to implements interface IJsonRpcService
```java
public class JsonRpcServiceImpl implements IJsonRpcService {
    @Override
    public Integer add(int a, int b) {
        return a + b;
    }
}
```
### step4. Configuring client
- Add configuration items to application.properties
```properties
# Symbol for the end of data request once.
jsonrpc.client.package-eof=\r\n
```
### step5. Create client interface
- Create a new interface file named IJsonRpcClient with @JsonRpcClient, protocol value can be tcp or http or https.
```java
@JsonRpcClient(value = "JsonRpc", protocol = "tcp", url = "localhost:3200")
public interface IJsonRpcClient {
    int add(int a, int b);
}
```
### step6. Use the client request the server
```java
public class JsonRpcTest {
    
    @Autowired
    private IJsonRpcClient jsonRpcClient;

    public int add() {
        // data sent: {"id":"691bbf41-e992-4f45-9c0d-1eeb0b088698","jsonrpc":"2.0","method":"json_rpc/add","params":{"a":3,"b":4}}
        // data received: {"id":"691bbf41-e992-4f45-9c0d-1eeb0b088698","jsonrpc":"2.0","result":7}
        return jsonRpcClient.add(1, 2); // the result is 3
    }
}
```



