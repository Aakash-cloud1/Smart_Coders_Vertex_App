package com.example.quizapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/","/welcome"})
    public String welcome() {
        return "welcome";
    }

    @GetMapping("/home")
    public String home() { return "home"; }

}
