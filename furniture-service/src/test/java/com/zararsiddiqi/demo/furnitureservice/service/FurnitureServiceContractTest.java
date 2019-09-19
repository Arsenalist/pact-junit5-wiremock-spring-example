package com.zararsiddiqi.demo.furnitureservice.service;

import au.com.dius.pact.core.model.Interaction;
import au.com.dius.pact.core.model.Pact;
import au.com.dius.pact.provider.junit.Provider;
import au.com.dius.pact.provider.junit.loader.PactFolder;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.apache.http.HttpRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * This spring environment is not needed as we're using WireMock to mock the service. However,
 * if we were using a live Spring app to run our contract tests again, then we'd use it (and presumably
 * not use WireMock) - it's really upto you.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@Provider("furniture-service-producer")
@PactFolder("../furniture-ui/target/pacts")
public class FurnitureServiceContractTest {

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

    @BeforeEach
    void setTarget(PactVerificationContext context) {
        HttpTestTarget target = new HttpTestTarget("localhost", WIREMOCK_PORT);
        context.setTarget(target);
    }


    @AfterEach
    public void tearDown() {

        wireMockServer.stop();
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void testTemplate(Pact pact, Interaction interaction, HttpRequest request, PactVerificationContext context) {
        System.out.println("testTemplate called: " + pact.getProvider().getName() + ", " + interaction.getDescription());
        context.verifyInteraction();
    }

}
