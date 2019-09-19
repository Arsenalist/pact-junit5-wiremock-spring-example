package com.zararsiddiqi.demo.furnitureui.service;

import com.atlassian.ta.wiremockpactgenerator.WireMockPactGenerator;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(
        locations = "classpath:test-application.properties")
public class UiFurnitureServiceContractTest {

    private WireMockServer wireMockServer;

    @Autowired
    private UiFurnitureService furnitureService;

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(8082);

        wireMockServer.addMockServiceRequestListener(
                WireMockPactGenerator
                        .builder("furniture-ui-consumer", "furniture-service-producer")
                        .build()
        );

        wireMockServer.start();
    }

    @AfterEach
    public void tearDown() {
        wireMockServer.stop();
    }

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

}
