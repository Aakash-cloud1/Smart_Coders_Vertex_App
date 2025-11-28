package com.example.quizapp.model;

public class Attempt {
    private String username;
    private int quizId;
    private int score;
    private int total;
    private String timestamp;

    public Attempt() {}

    public Attempt(String username, int quizId, int score, int total, String timestamp) {
        this.username = username;
        this.quizId = quizId;
        this.score = score;
        this.total = total;
        this.timestamp = timestamp;
    }

    public String getUsername() { return username; }
    public int getQuizId() { return quizId; }
    public int getScore() { return score; }
    public int getTotal() { return total; }
    public String getTimestamp() { return timestamp; }

    public void setUsername(String username) { this.username = username; }
    public void setQuizId(int quizId) { this.quizId = quizId; }
    public void setScore(int score) { this.score = score; }
    public void setTotal(int total) { this.total = total; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
