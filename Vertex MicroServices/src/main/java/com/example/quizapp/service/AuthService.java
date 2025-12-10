package com.example.quizapp.service;

import com.example.quizapp.model.*;
import com.example.quizapp.storage.FileStorage;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final FileStorage fs = new FileStorage();

    //handles user registration for both admin and student accounts
    public boolean register(String username, String password, String role) {

        //chech if the username already exists in either user group
        for (User u : fs.loadAdmins()) {
            if (u.getUsername().equalsIgnoreCase(username)) return false;
        }
        for (User u : fs.loadStudents()) {
            if (u.getUsername().equalsIgnoreCase(username)) return false;
        }

        int id = fs.nextUserId();//assigns the next available ID
        String hash = FileStorage.sha256(password);//store password securely

        User newUser;
        //creates an admin or student object based on role
        if (role.equalsIgnoreCase("ADMIN")) {
            newUser = new Admin(id, username, hash, "ADMIN");
            fs.appendAdmin(newUser);
        } else {
            newUser = new Student(id, username, hash, "STUDENT");
            fs.appendStudent(newUser);
        }

        return true;
    }

    //authenticates a user based on username,password and the expected role
    public User authenticate(String username, String password, String role) {

        String hash = FileStorage.sha256(password);//hash provided password
        //try to login as admin
        if (role.equalsIgnoreCase("ADMIN")) {
            for (User u : fs.loadAdmins()) {
                if (u.getUsername().equalsIgnoreCase(username)
                        && u.getPasswordHash().equals(hash)) {
                    return u;//correct login
                }
            }
        }
        //try to login as student
        if (role.equalsIgnoreCase("STUDENT")) {
            for (User u : fs.loadStudents()) {
                if (u.getUsername().equalsIgnoreCase(username)
                        && u.getPasswordHash().equals(hash)) {
                    return u;//correct login
                }
            }
        }

        return null;
    }
    //allows login without specifying role, mostly for admin-only flows
    public User authenticateWithoutRole(String username, String password) {
        String hash = FileStorage.sha256(password);
        //only admins are checked here
        for (User u : fs.loadAdmins()) {
            if (u.getUsername().equalsIgnoreCase(username)
                    && u.getPasswordHash().equals(hash)) {
                return u;//admin authenticated
            }
        }
        return null;//when no matching admin found
    }

}
