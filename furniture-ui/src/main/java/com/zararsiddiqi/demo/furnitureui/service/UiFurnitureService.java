package com.zararsiddiqi.demo.furnitureui.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class UiFurnitureService {

    @Value("${furnitureService.base}")
    private String furnitureServiceBase;


    public List<String> getFurnitureTypes() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String[]> responseEntity = restTemplate.getForEntity(furnitureServiceBase +
                "/furniture-types", String[].class);
        return Arrays.asList(responseEntity.getBody());
    }
}
