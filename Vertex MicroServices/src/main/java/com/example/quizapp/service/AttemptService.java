package com.example.quizapp.service;

import com.example.quizapp.model.Attempt;
import com.example.quizapp.storage.FileStorage;

import java.util.List;

public class AttemptService {
    //handles reading or writing attempts to storage files
    private final FileStorage fs = new FileStorage();
    //saves a new attempt to the storage system
    public void saveAttempt(Attempt a) {
        fs.appendAttempt(a);
    }
    //loads all attempts that belong to one specific wuiz
    public List<Attempt> getAttemptsForQuiz(int quizId) {
        return fs.loadAttemptsForQuiz(quizId);
    }
    //returns all attempts made by a particular user
    public List<Attempt> getAttemptsForUser(String username) {
        List<Attempt> all = fs.loadAllAttempts();
        List<Attempt> userAttempts = new java.util.ArrayList<>();

        for (Attempt a : all) {
            if (a.getUsername().equalsIgnoreCase(username)) {
                userAttempts.add(a);
            }
        }
        return userAttempts;
    }

    //finds a spe attempt using a "mockID" created from username+quizid
    public Attempt findAttemptByMockId(int mockAttemptId, String username) {

        List<Attempt> allAttempts = fs.loadAllAttempts();
        int usernameHash = username.hashCode();//used to gen unique mock IDs
        //checks each attempt to find the one that matches the requested mock ID
        for (Attempt attempt : allAttempts) {
            int calcMockId = usernameHash + attempt.getQuizId();

            if (attempt.getUsername().equals(username) &&
                calcMockId == mockAttemptId) {
                return attempt;//found the matching attempt
            }
        }

        return null;//when no matching attempt is found
    }
}
