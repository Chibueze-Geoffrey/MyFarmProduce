package com.myfarmproduce.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/catalog";
    }

    @GetMapping("/home/privacy")
    public String privacy() {
        return "home/privacy";
    }
}
