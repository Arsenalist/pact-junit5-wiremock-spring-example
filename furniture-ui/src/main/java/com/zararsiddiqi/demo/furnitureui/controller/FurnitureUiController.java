package com.zararsiddiqi.demo.furnitureui.controller;

import com.zararsiddiqi.demo.furnitureui.service.FurnitureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FurnitureUiController {

    @Autowired
    private FurnitureService furnitureService;

    @GetMapping("/display-furniture-types")
    public String displayFurnitureTypes(Model model) {
        model.addAttribute("furnitureTypes", furnitureService.getFurnitureTypes());
        return "furnitureTypes";
    }

}
