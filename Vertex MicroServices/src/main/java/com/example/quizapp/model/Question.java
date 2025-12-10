package com.example.quizapp.model;

public class Question {
    private int id;
    private String text;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int correctIndex;

    public Question() {}
    //creates a question with the text,options and the correct answer index
    public Question(int id, String text, String optionA, String optionB, String optionC, String optionD, int correctIndex) {
        this.id = id;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctIndex = correctIndex;
    }
    //returns the uniqueID of this question
    public int getId() { return id; }
    //returns the text or content of the question
    public String getText() { return text; }
    //returns option-A
    public String getOptionA() { return optionA; }
    //returns option-B
    public String getOptionB() { return optionB; }
    //returns option-C
    public String getOptionC() { return optionC; }
    //returns option-D
    public String getOptionD() { return optionD; }

    //returns the index of the correct answer(0–3)
    public int getCorrectIndex() { return correctIndex; }
    //updates the questionID
    public void setId(int id) { this.id = id; }
    //updates the question text
    public void setText(String text) { this.text = text; }

    //updates option-A
    public void setOptionA(String optionA) { this.optionA = optionA; }
    //updates option-B
    public void setOptionB(String optionB) { this.optionB = optionB; }
    //updates option-C
    public void setOptionC(String optionC) { this.optionC = optionC; }
    //updates option-D
    public void setOptionD(String optionD) { this.optionD = optionD; }


    //updates which option index is the correct one
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }
}