package com.example.quizapp.controller;

import com.example.quizapp.model.User;
import com.example.quizapp.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService auth;
    //injects the AuthService used for login and registration
    public AuthController(AuthService auth) {
        this.auth = auth;
    }
    //show the login page
    @GetMapping("/login")
    public String loginPage() { return "login"; }
    //handles login form submission
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String role,
                        HttpSession session,
                        Model model) {
        //tries to authenticate user with given credentials
        User u = auth.authenticate(username, password, role);
        if (u == null) {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }
        //store basic user info in the session for later use
        session.setAttribute("userId", u.getId());
        session.setAttribute("username", u.getUsername());
        session.setAttribute("role", role);
        //redirect user to the appropriate dashboard based on role
        if (role.equals("ADMIN"))
            return "redirect:/admin-dashboard";
        else
            return "redirect:/student-dashboard";
    }
    //show the registration page
    @GetMapping("/register")
    public String registerPage() { return "register"; }
    //handles registration form submission
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String role,
                           Model model) {
        //Attempt to register a new account
        boolean ok = auth.register(username, password, role);
        //if username is already taken, then show error on the same page
        if (!ok) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }
        //successful registration, goes to "login" page
        return "redirect:/login";
    }
}