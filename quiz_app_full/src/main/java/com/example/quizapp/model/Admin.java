package com.example.quizapp.model;

public class Admin extends User {
    public Admin() {}
    public Admin(int id, String username, String passwordHash, String role) {
        super(id, username, passwordHash, role);
    }
}
