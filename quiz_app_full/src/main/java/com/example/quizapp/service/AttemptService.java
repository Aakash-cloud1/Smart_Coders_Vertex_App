package com.example.quizapp.service;

import com.example.quizapp.model.Attempt;
import com.example.quizapp.storage.FileStorage;
import java.util.List;

public class AttemptService {
    private final FileStorage fs = new FileStorage();

    public void saveAttempt(Attempt a) {
        fs.appendAttempt(a);
    }

    public java.util.List<Attempt> getAttemptsForQuiz(int quizId) {
        return fs.loadAttemptsForQuiz(quizId);
    }
}
