package com.example.quizapp.service;

import com.example.quizapp.model.Question;
import com.example.quizapp.storage.FileStorage;

import java.util.*;

public class QuizService {

    private final FileStorage fs = new FileStorage();

    // ---------------- CREATE QUIZ WITH RANDOM 6 DIGIT ID ----------------

    public void createQuizWithId(int quizId, String title, int creatorId) {
        fs.appendQuizMeta(quizId, title, creatorId);
    }

    // ---------------- LIST QUIZ META ----------------

    public List<Map<String, Object>> listQuizMeta() {
        return fs.loadQuizMeta();
    }

    // ---------------- ADD QUESTION ----------------

    public void addQuestion(int quizId, Question q) {
        fs.saveQuestionToQuiz(quizId, q);
    }

    // ---------------- GET QUESTIONS ----------------

    public List<Question> getQuestions(int quizId) {
        return fs.loadQuestionsForQuiz(quizId);
    }

    // ---------------- OVERWRITE QUESTIONS ----------------

    public void overwriteQuestions(int quizId, List<Question> questions) {
        fs.overwriteQuizFile(quizId, questions);
    }

    // ---------------- CHECK CREATOR ----------------

    public boolean isCreator(int quizId, int userId) {
        List<Map<String, Object>> meta = fs.loadQuizMeta();

        for (Map<String, Object> m : meta) {
            int id = (int) m.get("id");
            if (id == quizId) {
                int creator = (int) m.get("creatorId");
                return creator == userId;
            }
        }
        return false;
    }
}
