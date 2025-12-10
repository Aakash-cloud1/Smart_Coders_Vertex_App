package com.example.quizapp.model;
//represents an admin user, inherits all common user fields and behaviour
public class Admin extends User {
    public Admin() {}
    public Admin(int id, String username, String passwordHash, String role) {
        super(id, username, passwordHash, role);
    }
}
