package com.zararsiddiqi.demo.furnitureservice.controller;

import com.zararsiddiqi.demo.furnitureservice.service.FurnitureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class FurnitureController {

    @Autowired
    private FurnitureService furnitureService;

    @RequestMapping("/furniture-types")
    @ResponseBody
    public List<String> furnitureTypes() {
        return furnitureService.getFurnitureTypes();
    }
}
