package com.example.quizapp.service;

import com.example.quizapp.model.Question;
import com.example.quizapp.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QuizService {

    private final FileStorage fs = new FileStorage();

    public void createQuizWithId(int id,
                                 String title,
                                 int creatorId,
                                 String startDateTime,
                                 String endDateTime,
                                 String instructions) {
        fs.appendQuizMeta(id, title, creatorId, startDateTime, endDateTime, instructions);
    }
    //returns a list of all quiz metadata-basically shows all the quizzes created so far
    public List<Map<String, Object>> listQuizMeta() {
        return fs.loadQuizMeta();
    }
    //adds a single question to the given quiz
    public void addQuestion(int quizId, Question q) {
        fs.saveQuestionToQuiz(quizId, q);
    }
    //fetch all the questions belonging to a particular quiz
    public List<Question> getQuestions(int quizId) {
        return fs.loadQuestionsForQuiz(quizId);
    }
    //completely replace all the questions of a quiz with a new set
    public void overwriteQuestions(int quizId, List<Question> questions) {
        fs.overwriteQuizFile(quizId, questions);
    }
    //checks if the given user is actually the creator of the quiz
    public boolean isCreator(int quizId, int userId) {
        for (Map<String, Object> m : fs.loadQuizMeta()) {
            int id = ((Number) m.get("id")).intValue();
            if (id == quizId) {
                int creator = ((Number) m.get("creatorId")).intValue();
                return creator == userId;
            }
        }
        return false;//when quiz not found or user dosen't match
    }
    //retrieves metadata for a single quiz by its ID
    public Map<String, Object> getQuizMeta(int quizId) {
    for (Map<String, Object> m : fs.loadQuizMeta()) {
        int id = ((Number) m.get("id")).intValue();
        if (id == quizId) {
            return m;//returns the quiz details if ID matches
        }
    }
    return null;//no quiz found wiht this ID
}

}
