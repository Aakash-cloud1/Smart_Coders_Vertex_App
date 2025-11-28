package com.example.quizapp.service;

import com.example.quizapp.model.*;
import com.example.quizapp.storage.FileStorage;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final FileStorage fs = new FileStorage();

    // REGISTER WITH ROLE
    public boolean register(String username, String password, String role) {

        // Check duplicate
        for (User u : fs.loadAdmins()) {
            if (u.getUsername().equalsIgnoreCase(username)) return false;
        }
        for (User u : fs.loadStudents()) {
            if (u.getUsername().equalsIgnoreCase(username)) return false;
        }

        int id = fs.nextUserId();
        String hash = FileStorage.sha256(password);

        User newUser;

        if (role.equalsIgnoreCase("ADMIN")) {
            newUser = new Admin(id, username, hash, "ADMIN");
            fs.appendAdmin(newUser);
        } else {
            newUser = new Student(id, username, hash, "STUDENT");
            fs.appendStudent(newUser);
        }

        return true;
    }

    // LOGIN WITH ROLE
    public User authenticate(String username, String password, String role) {

        String hash = FileStorage.sha256(password);

        if (role.equalsIgnoreCase("ADMIN")) {
            for (User u : fs.loadAdmins()) {
                if (u.getUsername().equalsIgnoreCase(username)
                        && u.getPasswordHash().equals(hash)) {
                    return u;
                }
            }
        }

        if (role.equalsIgnoreCase("STUDENT")) {
            for (User u : fs.loadStudents()) {
                if (u.getUsername().equalsIgnoreCase(username)
                        && u.getPasswordHash().equals(hash)) {
                    return u;
                }
            }
        }

        return null;
    }

    public User authenticateWithoutRole(String username, String password) {
        String hash = FileStorage.sha256(password);

        for (User u : fs.loadAdmins()) {
            if (u.getUsername().equalsIgnoreCase(username)
                    && u.getPasswordHash().equals(hash)) {
                return u;
            }
        }
        return null;
    }

}
