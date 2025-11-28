package com.example.quizapp.controller;

import com.example.quizapp.model.Attempt;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.User;
import com.example.quizapp.service.AttemptService;
import com.example.quizapp.service.AuthService;
import com.example.quizapp.service.QuizService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class QuizController {

    private final QuizService quizService = new QuizService();
    private final AttemptService attemptService = new AttemptService();
    private final AuthService auth = new AuthService();

    private static final String S_ATTEMPT_QUIZID = "attemptQuizId";
    private static final String S_ATTEMPT_INDEX = "attemptIndex";
    private static final String S_ATTEMPT_SCORE = "attemptScore";
    private static final String S_VERIFIED_USER = "verifiedUser";

    @GetMapping("/admin-dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("role") == null) return "redirect:/login";
        if (!session.getAttribute("role").equals("ADMIN")) return "redirect:/login";

        model.addAttribute("username", session.getAttribute("username"));
        return "admin-dashboard";
    }

    @GetMapping("/student-dashboard")
    public String studentDashboard(HttpSession session, Model model) {
        if (session.getAttribute("role") == null) return "redirect:/login";
        if (!session.getAttribute("role").equals("STUDENT")) return "redirect:/login";

        model.addAttribute("username", session.getAttribute("username"));
        return "student-dashboard";
    }

    @GetMapping("/admin-check-my-quizzes")
    public String adminCheckMyQuizzes(HttpSession session, Model model) {

        if (session.getAttribute("role") == null) return "redirect:/login";
        if (!session.getAttribute("role").equals("ADMIN")) return "redirect:/login";

        int uid = (int) session.getAttribute("userId");

        List<Map<String, Object>> all = quizService.listQuizMeta();
        List<Map<String, Object>> mine = new ArrayList<>();

        for (Map<String, Object> m : all) {
            int creator = ((Number) m.get("creatorId")).intValue();
            if (creator == uid)
                mine.add(m);
        }

        model.addAttribute("myQuizzes", mine);
        return "admin-check-my-quizzes";
    }

    @GetMapping("/create-quiz")
    public String createQuizPage(HttpSession session) {
        if (session.getAttribute("userId") == null)
            return "redirect:/login";
        return "create-quiz";
    }

    @PostMapping("/create-quiz")
    public String createQuiz(@RequestParam String title,
                             @RequestParam int totalQuestions,
                             HttpSession session) {

        Integer uid = (Integer) session.getAttribute("userId");
        if (uid == null) return "redirect:/login";

        Random r = new Random();
        int quizId = 100000 + r.nextInt(900000);

        quizService.createQuizWithId(quizId, title, uid);

        session.setAttribute("tempQuizId", quizId);
        session.setAttribute("totalQuestions", totalQuestions);
        session.setAttribute("currentQuestion", 1);

        return "redirect:/add-question";
    }

    @GetMapping("/add-question")
    public String addQuestionPage(HttpSession session, Model model) {
        Integer quizId = (Integer) session.getAttribute("tempQuizId");
        Integer current = (Integer) session.getAttribute("currentQuestion");
        Integer total = (Integer) session.getAttribute("totalQuestions");

        if (quizId == null || current == null || total == null)
            return redirectDashboard(session);

        if (current > total) {
            session.removeAttribute("tempQuizId");
            session.removeAttribute("totalQuestions");
            session.removeAttribute("currentQuestion");
            model.addAttribute("message", "Quiz Created Successfully!");
            return "quiz-created";
        }

        model.addAttribute("quizId", quizId);
        model.addAttribute("current", current);
        model.addAttribute("total", total);

        return "add-question";
    }

    @PostMapping("/add-question")
    public String addQuestionSubmit(@RequestParam String text,
                                    @RequestParam String optionA,
                                    @RequestParam String optionB,
                                    @RequestParam String optionC,
                                    @RequestParam String optionD,
                                    @RequestParam int correctIndex,
                                    HttpSession session) {

        Integer quizId = (Integer) session.getAttribute("tempQuizId");
        Integer current = (Integer) session.getAttribute("currentQuestion");

        Question q = new Question(current, text, optionA, optionB, optionC, optionD, correctIndex);
        quizService.addQuestion(quizId, q);

        session.setAttribute("currentQuestion", current + 1);

        return "redirect:/add-question";
    }

    @GetMapping("/attempt-quiz")
    public String attemptSelect(Model model) {
        model.addAttribute("quizzes", quizService.listQuizMeta());
        return "attempt-select";
    }

    @GetMapping("/attempt-quiz/{quizId}")
    public String startAttempt(@PathVariable int quizId, HttpSession session, Model model) {

        String username = (String) session.getAttribute("username");
        if (username == null)
            return "redirect:/login";

        List<Attempt> attempts = attemptService.getAttemptsForQuiz(quizId);

        for (Attempt a : attempts) {
            if (a.getUsername().equals(username)) {
                model.addAttribute("score", a.getScore());
                model.addAttribute("total", a.getTotal());
                model.addAttribute("quizId", quizId);
                model.addAttribute("already", true);
                return "result";
            }
        }

        session.setAttribute(S_ATTEMPT_QUIZID, quizId);
        session.setAttribute(S_ATTEMPT_INDEX, 0);
        session.setAttribute(S_ATTEMPT_SCORE, 0);

        return "redirect:/attempt-question";
    }

    @GetMapping("/attempt-question")
    public String showAttemptQuestion(HttpSession session, Model model) {

        Integer quizId = (Integer) session.getAttribute(S_ATTEMPT_QUIZID);
        Integer idx = (Integer) session.getAttribute(S_ATTEMPT_INDEX);

        if (quizId == null || idx == null)
            return "redirect:/attempt-quiz";

        List<Question> qs = quizService.getQuestions(quizId);

        if (idx >= qs.size())
            return "redirect:/attempt-finish";

        Question q = qs.get(idx);

        model.addAttribute("quizId", quizId);
        model.addAttribute("question", q);
        model.addAttribute("index", idx + 1);
        model.addAttribute("total", qs.size());

        return "attempt-question";
    }

    @PostMapping("/attempt-submit")
    public String submitAnswer(@RequestParam(required = false) Integer selected,
                               HttpSession session) {

        Integer quizId = (Integer) session.getAttribute(S_ATTEMPT_QUIZID);
        Integer idx = (Integer) session.getAttribute(S_ATTEMPT_INDEX);
        Integer score = (Integer) session.getAttribute(S_ATTEMPT_SCORE);

        List<Question> qs = quizService.getQuestions(quizId);
        Question q = qs.get(idx);

        if (selected != null && selected == q.getCorrectIndex()) {
            score++;
            session.setAttribute(S_ATTEMPT_SCORE, score);
        }

        session.setAttribute(S_ATTEMPT_INDEX, idx + 1);

        if (idx + 1 >= qs.size())
            return "redirect:/attempt-finish";

        return "redirect:/attempt-question";
    }

    @GetMapping("/attempt-finish")
    public String finishAttempt(HttpSession session, Model model) {

        Integer quizId = (Integer) session.getAttribute(S_ATTEMPT_QUIZID);
        Integer score = (Integer) session.getAttribute(S_ATTEMPT_SCORE);

        List<Question> qs = quizService.getQuestions(quizId);
        int total = qs.size();

        String username = (String) session.getAttribute("username");
        if (username == null) username = "anonymous";

        String ts = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());

        Attempt a = new Attempt(username, quizId, score, total, ts);
        attemptService.saveAttempt(a);

        session.removeAttribute(S_ATTEMPT_QUIZID);
        session.removeAttribute(S_ATTEMPT_INDEX);
        session.removeAttribute(S_ATTEMPT_SCORE);

        model.addAttribute("score", score);
        model.addAttribute("total", total);
        model.addAttribute("quizId", quizId);

        return "result";
    }

    @GetMapping("/manage-my-quizzes-login")
    public String manageMyQuizzesLoginPage() {
        return "manage-my-quizzes-login";
    }

    @PostMapping("/manage-my-quizzes-login")
    public String manageMyQuizzesLoginPost(@RequestParam String username,
                                           @RequestParam String password,
                                           HttpSession session,
                                           Model model) {

        User u = auth.authenticateWithoutRole(username, password);
        if (u == null) {
            model.addAttribute("error", "Invalid credentials");
            return "manage-my-quizzes-login";
        }

        session.setAttribute(S_VERIFIED_USER, u.getId());
        return "redirect:/manage-my-quizzes";
    }

    @GetMapping("/manage-my-quizzes")
    public String manageMyQuizzes(HttpSession session, Model model) {

        Integer vid = (Integer) session.getAttribute(S_VERIFIED_USER);
        if (vid == null)
            return "redirect:/manage-my-quizzes-login";

        List<Map<String, Object>> all = quizService.listQuizMeta();
        List<Map<String, Object>> mine = new ArrayList<>();

        for (Map<String, Object> m : all) {
            int creator = ((Number) m.get("creatorId")).intValue();
            if (creator == vid)
                mine.add(m);
        }

        model.addAttribute("myQuizzes", mine);
        return "manage-my-quizzes";
    }

    @GetMapping("/manage-one-quiz")
    public String manageOneQuiz(@RequestParam int quizId,
                                HttpSession session,
                                Model model) {

        Integer vid = (Integer) session.getAttribute(S_VERIFIED_USER);
        if (vid == null)
            return "redirect:/manage-my-quizzes-login";

        if (!quizService.isCreator(quizId, vid)) {
            model.addAttribute("error", "Not allowed");
            return "manage-my-quizzes";
        }

        model.addAttribute("quizId", quizId);
        model.addAttribute("questions", quizService.getQuestions(quizId));

        return "manage-one-quiz";
    }

    @GetMapping("/add-question-existing")
    public String addQuestionExistingForm(@RequestParam int quizId,
                                          HttpSession session,
                                          Model model) {

        Integer vid = (Integer) session.getAttribute(S_VERIFIED_USER);
        if (vid == null)
            return "redirect:/manage-my-quizzes-login";

        if (!quizService.isCreator(quizId, vid))
            return "redirect:/manage-my-quizzes";

        model.addAttribute("quizId", quizId);
        return "add-question-existing";
    }

    @PostMapping("/add-question-existing")
    public String addQuestionExistingSubmit(@RequestParam int quizId,
                                            @RequestParam String text,
                                            @RequestParam String optionA,
                                            @RequestParam String optionB,
                                            @RequestParam String optionC,
                                            @RequestParam String optionD,
                                            @RequestParam int correctIndex,
                                            HttpSession session) {

        Integer vid = (Integer) session.getAttribute(S_VERIFIED_USER);
        if (vid == null)
            return "redirect:/manage-my-quizzes-login";

        if (!quizService.isCreator(quizId, vid))
            return "redirect:/manage-my-quizzes";

        List<Question> qs = quizService.getQuestions(quizId);
        int nextId = qs.size() + 1;

        Question q = new Question(nextId, text, optionA, optionB, optionC, optionD, correctIndex);
        quizService.addQuestion(quizId, q);

        return "redirect:/manage-one-quiz?quizId=" + quizId;
    }

    @GetMapping("/delete-question")
    public String deleteQuestion(@RequestParam int quizId,
                                 @RequestParam int qid,
                                 HttpSession session) {

        Integer vid = (Integer) session.getAttribute(S_VERIFIED_USER);
        if (vid == null)
            return "redirect:/manage-my-quizzes-login";

        if (!quizService.isCreator(quizId, vid))
            return "redirect:/manage-my-quizzes";

        List<Question> qs = quizService.getQuestions(quizId);

        qs.removeIf(q -> q.getId() == qid);

        List<Question> updated = new ArrayList<>();
        int id = 1;
        for (Question q : qs) {
            updated.add(
                new Question(id, q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(), q.getCorrectIndex())
            );
            id++;
        }

        quizService.overwriteQuestions(quizId, updated);

        return "redirect:/manage-one-quiz?quizId=" + quizId;
    }

    @GetMapping("/edit-question")
    public String editQuestionForm(@RequestParam int quizId,
                                   @RequestParam int qid,
                                   HttpSession session,
                                   Model model) {

        Integer vid = (Integer) session.getAttribute(S_VERIFIED_USER);
        if (vid == null)
            return "redirect:/manage-my-quizzes-login";

        if (!quizService.isCreator(quizId, vid))
            return "redirect:/manage-my-quizzes";

        List<Question> qs = quizService.getQuestions(quizId);

        for (Question q : qs) {
            if (q.getId() == qid) {
                model.addAttribute("question", q);
                model.addAttribute("quizId", quizId);
                return "edit-question";
            }
        }

        return "redirect:/manage-one-quiz?quizId=" + quizId;
    }

    @PostMapping("/edit-question")
    public String editQuestionSubmit(@RequestParam int quizId,
                                     @RequestParam int id,
                                     @RequestParam String text,
                                     @RequestParam String optionA,
                                     @RequestParam String optionB,
                                     @RequestParam String optionC,
                                     @RequestParam String optionD,
                                     @RequestParam int correctIndex,
                                     HttpSession session) {

        Integer vid = (Integer) session.getAttribute(S_VERIFIED_USER);
        if (vid == null)
            return "redirect:/manage-my-quizzes-login";

        if (!quizService.isCreator(quizId, vid))
            return "redirect:/manage-my-quizzes";

        List<Question> qs = quizService.getQuestions(quizId);
        List<Question> updated = new ArrayList<>();

        for (Question q : qs) {
            if (q.getId() == id) {
                updated.add(new Question(id, text, optionA, optionB, optionC, optionD, correctIndex));
            } else {
                updated.add(q);
            }
        }

        quizService.overwriteQuestions(quizId, updated);

        return "redirect:/manage-one-quiz?quizId=" + quizId;
    }

    @GetMapping("/available-quizzes")
    public String availableQuizzes(Model model) {
        model.addAttribute("quizzes", quizService.listQuizMeta());
        return "available-quizzes";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(@RequestParam int quizId, Model model, HttpSession session) {

        List<Attempt> attempts = attemptService.getAttemptsForQuiz(quizId);
        attempts.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        model.addAttribute("quizId", quizId);
        model.addAttribute("attempts", attempts);

        model.addAttribute("role", session.getAttribute("role"));

        return "leaderboard";
    }

    private String redirectDashboard(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("ADMIN".equals(role)) return "redirect:/admin-dashboard";
        else return "redirect:/student-dashboard";
    }
}
