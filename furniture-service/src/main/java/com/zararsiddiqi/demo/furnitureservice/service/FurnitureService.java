package com.zararsiddiqi.demo.furnitureservice.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FurnitureService {

    public List<String> getFurnitureTypes() {
        List<String> list = new ArrayList<String>();
        list.add("Tables");
        list.add("Chairs");
        return list;
    }
}
