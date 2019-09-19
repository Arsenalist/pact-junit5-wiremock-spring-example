Contact-based testing allows service consumers and service providers to stay in continuous sync so that any breaking changes are immediately visible.  In general, there are two types of contract-based testing. The first is where the consumer specifies what the producer service should do, and the other is where the producer defines how the consumer should expect to create it. This example is about consumer-based contract testing which is more relevant in organizations where delivery teams are dependent on internal (and often legacy) systems to consumer system-of-record services. An excellent intro to the concept can be found [here](https://kreuzwerker.de/post/introduction-to-consumer-driven-contract-testing).

We'll implement a CDC using an example of a Furniture UI application which depends on a Furniture Service application. Basically, the Furniture UI just displays the data returned by the Furniture Service.

The tools we're using are:

1. [Pact](https://docs.pact.io/) for defining the contracts
2. [WireMock](http://wiremock.org/) for mocking out back-ends (and as you'll see, help defining our contracts)
3. [Spring](https://spring.io/) to run the UI and service applications
4. [JUnit 5](https://junit.org/junit5/) as a testing tool
5. [This library](https://bitbucket.org/atlassian/wiremock-pact-generator/src/master/) which generates Pact contracts based on WireMock stubs.

## Defining a Contract
We define the contract using WireMock in our `UiFurnitureServiceContractTest` class:

First, we tell WireMock to generate Pact contracts based on any stubbing that we'll do. 
```java
@BeforeEach  
public void setup() {  
    wireMockServer = new WireMockServer(8082);  
  wireMockServer.addMockServiceRequestListener(  
            WireMockPactGenerator  
                    .builder("furniture-ui-consumer", "furniture-service-producer")  
                    .build()  
    );    
}
```
Second, we'll define a WireMock stub and then execute some expectations against it.
```java
@Test  
public void furnitureTypes() {  
    // given  
  wireMockServer.stubFor(get(  
            urlEqualTo("/furniture-types"))  
            .willReturn(aResponse()  
                    .withStatus(200)  
                    .withHeader("Content-Type", "application/json")  
                    .withBody("[\"Tables\",\"Chairs\"]")  
    ));  
  
  // when  
  List<String> furnitureTypes = furnitureService.getFurnitureTypes();  
  
  // then  
  Assertions.assertEquals("Tables", furnitureTypes.get(0));  
  Assertions.assertEquals("Chairs", furnitureTypes.get(1));  
}
```
This will generate a Pact file which looks like the following. You'll notice that this looks very similar to WireMock JSON stubs. The reason we're using WireMock to generate these instead of Pact's own DSL is the possible duplication in creating these contracts. If you're already invested in WireMock there may be little reason to expand the toolset to do something which we can already do with ease.  This is made possible by the `wiremock-pact-generator` library.
```json
{  
  "consumer": {  
    "name": "furniture-ui-consumer"  
  },  
  "provider": {  
    "name": "furniture-service-producer"  
  },  
  "interactions": [  
    {  
      "description": "GET /furniture-types -> 200",  
  "request": {  
        "method": "GET",  
  "path": "/furniture-types",  
  "headers": {  
          "connection": "keep-alive",  
  "user-agent": "Java/1.8.0_131",  
  "accept": "application/json, application/*+json"  
  }  
      },  
  "response": {  
        "status": 200,  
  "headers": {  
          "content-type": "application/json"  
  },  
  "body": [  
          "Tables",  
  "Chairs"  
  ]  
      }  
    }  
  ]  
}
```
## How will this contract be used by the provider?
Before we get to the service code, it's important to consider how this contract can be used by the service provider.

Conceptually, the service provider will want to do a few things:
1. Understand what the consumer expects from it
2. Execute the tests described in the contract against the provider application to ensure they pass (e.g., respond correctly to the specified requests)
3. It may choose to do #2 by executing it against the real application, or against a mocked back-end, however it sees fit. 

#3 is an important point of distinction as depending on the contract, the provider may choose the back-end at its own discretion. If the service is idempotent or doesn't have data implications, it may execute against a real service. If it is not, it may just run its own WireMock server and test against that. 

Either is fine, but either way it must startup some sort of HTTP or HTTPS server to validate the contract.

In the example below, we are using a WireMocked back-end in the provider service.

## Satisfying the Contract

The key annotations on the contract class are as follows. Let's examine both of them:
```java
@Provider("furniture-service-producer")  
@PactFolder("../furniture-ui/target/pacts")  
public class FurnitureServiceContractTest
```
1. The `@Provider` refers to the provider specified in the `UiFurnitureServiceContractTest` and will be used to query the contract file. 
2. The `@PactFolder` simply refers to the filesystem path of where the contracts were generated by the consumer.

There are other ways to define where the contracts are stored but that is beyond the scope of this demo.

We next setup a WireMock server to validate the contract. Remember, we could use the real application as well but we just chose the WireMock instead.

```java
private static final int WIREMOCK_PORT = 8082;  
private WireMockServer wireMockServer;

@BeforeEach  
public void setup() {  
    wireMockServer = new WireMockServer(WIREMOCK_PORT);  
  wireMockServer.stubFor(get(  
            urlEqualTo("/furniture-types"))  
            .willReturn(aResponse()  
                    .withStatus(200)  
                    .withHeader("Content-Type", "application/json")  
                    .withBody("[\"Tables\",\"Chairs\"]")  
            ));  
  wireMockServer.start();  
}
```
We must now make sure that when the contract is executed it goes against the WireMocked backend (by default it'll hit localhost:8080) but as WireMock starts on a different port, we need the following:
```java
@BeforeEach  
void setTarget(PactVerificationContext context) {  
    HttpTestTarget target = new HttpTestTarget("localhost", WIREMOCK_PORT);  
  context.setTarget(target);  
}
```
Then it's a matter of reading the contract and executing the tests:
```java
@TestTemplate  
@ExtendWith(PactVerificationInvocationContextProvider.class)  
void testTemplate(Pact pact, Interaction interaction, HttpRequest request, PactVerificationContext context) {  
    context.verifyInteraction();  
}
```

That's it, if you run the tests on the provider, you'll notice the following in the logs:

```
  GET /furniture-types -> 200
testTemplate called: furniture-service-producer, GET /furniture-types -> 200
2019-09-19 12:10:41.850  INFO 82307 --- [tp1947681232-38] o.e.j.s.handler.ContextHandler.ROOT      : RequestHandlerClass from context returned com.github.tomakehurst.wiremock.http.StubRequestHandler. Normalized mapped under returned 'null'
    returns a response which
      has status code 200 (OK)
      has a matching body (OK)
```

