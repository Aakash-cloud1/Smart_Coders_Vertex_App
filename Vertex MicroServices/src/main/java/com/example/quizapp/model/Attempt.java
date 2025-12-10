package com.example.quizapp.model;

import java.util.Map;

public class Attempt {

    private String username;
    private int quizId;
    private int score;
    private int total;
    private String timestamp;

    //stores each question id mapped to the submitted answer index
    private Map<Integer, Integer> answers;

    public Attempt() {}//creates an empty attempt object


    public Attempt(String username, int quizId, int score, int total, String timestamp) {
        this.username = username;
        this.quizId = quizId;
        this.score = score;
        this.total = total;
        this.timestamp = timestamp;
    }

    // NEW constructor with answer storage
    public Attempt(String username, int quizId, int score, int total,
                   String timestamp, Map<Integer, Integer> answers) {
        this.username = username;
        this.quizId = quizId;
        this.score = score;
        this.total = total;
        this.timestamp = timestamp;
        this.answers = answers;
    }
    //returns the username of the person who took the quiz
    public String getUsername() { return username; }
    //returns the ID of the quiz taken
    public int getQuizId() { return quizId; }
    //returns how many questions the user got correct
    public int getScore() { return score; }
    //returns the total number of questions in the quiz
    public int getTotal() { return total; }
    //returns the timestamp when the quiz was completed
    public String getTimestamp() { return timestamp; }

    //updates the username for this attempt
    public void setUsername(String username) { this.username = username; }
    //updates the quizID for this attempt
    public void setQuizId(int quizId) { this.quizId = quizId; }
    //updates the user's score
    public void setScore(int score) { this.score = score; }
    //updates the total number of questions
    public void setTotal(int total) { this.total = total; }
    //updates the timestamp of when the quiz was taken
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public Map<Integer, Integer> getAnswers() { return answers; }
    // stores or updates the user's submitted answers
    public void setAnswers(Map<Integer, Integer> answers) {
        this.answers = answers;
    }
}
