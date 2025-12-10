package com.example.quizapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    //shows the landing page when the user first opens the app
    @GetMapping("/")
    public String landing() {
        return "landing";
    }
    //shows a simple welcome page after login or navigation
    @GetMapping("/welcome")
    public String welcome() {
        return "welcome";
    }
    //displays the main home page of the quiz application
    @GetMapping("/home")
    public String home() {
        return "home";
    }

}
