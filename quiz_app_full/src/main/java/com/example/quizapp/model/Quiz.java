package com.example.quizapp.model;

import java.util.List;

public class Quiz {
    private int id;
    private String title;
    private int creatorUserId;
    private List<Question> questions;

    public Quiz() {}

    public Quiz(int id, String title, int creatorUserId) {
        this.id = id;
        this.title = title;
        this.creatorUserId = creatorUserId;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getCreatorUserId() { return creatorUserId; }
    public List<Question> getQuestions() { return questions; }

    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCreatorUserId(int creatorUserId) { this.creatorUserId = creatorUserId; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}
