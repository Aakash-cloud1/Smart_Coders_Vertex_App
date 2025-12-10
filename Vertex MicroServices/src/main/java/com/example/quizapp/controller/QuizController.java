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

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Controller
public class QuizController {

    private final QuizService quizService = new QuizService();
    private final AttemptService attemptService = new AttemptService();
    private final AuthService auth = new AuthService();
    //all the session keys used across the controller
    private static final String S_ATTEMPT_QUIZID = "attemptQuizId";
    private static final String S_ATTEMPT_INDEX = "attemptIndex";
    private static final String S_ATTEMPT_SCORE = "attemptScore";
    private static final String S_VERIFIED_USER = "verifiedUser";

    //maps question id-> submitted answer index for the current attempt
    private static final String S_ATTEMPT_ANSWERS = "attemptAnswers";

    //small data transfer object to send question, submitted answer and correctness to the view
    public static class ReviewItem {
        public int index;
        public Question question;
        public Integer submittedAnswer;
        public boolean isCorrect;

        public ReviewItem(int index, Question question, Integer submittedAnswer, boolean isCorrect) {
            this.index = index;
            this.question = question;
            this.submittedAnswer = submittedAnswer;
            this.isCorrect = isCorrect;
        }

        public int getIndex() {
            return index;
        }

        public Question getQuestion() {
            return question;
        }

        public Integer getSubmittedAnswer() {
            return submittedAnswer;
        }

        public boolean isCorrect() {
            return isCorrect;
        }
    }

    //DASHBOARD
    //admin landing page, this is only accessible to logged-in admins

    @GetMapping("/admin-dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        if (session.getAttribute("role") == null)
            return "redirect:/login";
        if (!"ADMIN".equals(session.getAttribute("role")))
            return "redirect:/login";

        model.addAttribute("username", session.getAttribute("username"));
        return "admin-dashboard";
    }

    @GetMapping("/student-dashboard")
    public String studentDashboard(HttpSession session, Model model) {
        if (session.getAttribute("role") == null)
            return "redirect:/login";
        if (!"STUDENT".equals(session.getAttribute("role")))
            return "redirect:/login";

        model.addAttribute("username", session.getAttribute("username"));
        return "student-dashboard";
    }

    //admin quiz management
    //shows admin's own quizzes, checks role and filters by creator id

    @GetMapping("/admin-check-my-quizzes")
    public String adminCheckMyQuizzes(HttpSession session, Model model) {

        if (session.getAttribute("role") == null)
            return "redirect:/login";
        if (!"ADMIN".equals(session.getAttribute("role")))
            return "redirect:/login";

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
    // this is a page to create a quiz, but must be logged in to do so
    @GetMapping("/create-quiz")
    public String createQuizPage(HttpSession session) {
        if (session.getAttribute("userId") == null)
            return "redirect:/login";
        return "create-quiz";
    }
    //handles quiz creation form submit, validates and stores quiz metadata
    @PostMapping("/create-quiz")
    public String createQuiz(@RequestParam String title,
            @RequestParam int totalQuestions,
            @RequestParam String startDate,
            @RequestParam String startTime,
            @RequestParam String endDate,
            @RequestParam String endTime,
            @RequestParam(required = false, defaultValue = "No instructions provided") String instructions,
            HttpSession session,
            Model model) {

        Integer uid = (Integer) session.getAttribute("userId");
        if (uid == null)
            return "redirect:/login";

        // Checks for unique title, this is case-insensitive
        List<Map<String, Object>> allQuizzes = quizService.listQuizMeta();
        for (Map<String, Object> m : allQuizzes) {
            String existingTitle = (String) m.get("title");
            if (existingTitle != null && existingTitle.trim().equalsIgnoreCase(title.trim())) {
                model.addAttribute("error", "Quiz title '" + title + "' is already taken. Please choose another.");
                model.addAttribute("title", title);
                model.addAttribute("totalQuestions", totalQuestions);
                model.addAttribute("instructions", instructions);
                model.addAttribute("startDate", startDate);
                model.addAttribute("startTime", startTime);
                model.addAttribute("endDate", endDate);
                model.addAttribute("endTime", endTime);
                return "create-quiz";
            }
        }
        //creates data-time strings in a std format so that they can be saved easily
        String startDateTime = startDate + "T" + startTime;
        String endDateTime = endDate + "T" + endTime;

        LocalDateTime startDT = LocalDateTime.parse(startDateTime);
        LocalDateTime endDT = LocalDateTime.parse(endDateTime);

        if (!endDT.isAfter(startDT)) {
            model.addAttribute("error", "End time must be greater than start time");
            // Preserve inputs
            model.addAttribute("title", title);
            model.addAttribute("totalQuestions", totalQuestions);
            model.addAttribute("instructions", instructions);
            model.addAttribute("startDate", startDate);
            model.addAttribute("startTime", startTime);
            model.addAttribute("endDate", endDate);
            model.addAttribute("endTime", endTime);
            return "create-quiz";
        }
        //generates quick random id for the quiz, collision is not that likely for such small app
        Random r = new Random();
        int quizId = 100000 + r.nextInt(900000);

        //persisting metadata including instructions
        quizService.createQuizWithId(quizId, title, uid, startDateTime, endDateTime, instructions);

        session.setAttribute("tempQuizId", quizId);
        session.setAttribute("totalQuestions", totalQuestions);
        session.setAttribute("currentQuestion", 1);

        return "redirect:/add-question";
    }
    //show add-question page when creating a new quiz
    @GetMapping("/add-question")
    public String addQuestionPage(HttpSession session, Model model) {
        Integer quizId = (Integer) session.getAttribute("tempQuizId");
        Integer current = (Integer) session.getAttribute("currentQuestion");
        Integer total = (Integer) session.getAttribute("totalQuestions");

        if (quizId == null || current == null || total == null)
            return redirectDashboard(session);
        //finishes the flow if all the questions are added
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
    //this process adding a single question while creating a quiz
    @PostMapping("/add-question")
    public String addQuestionSubmit(@RequestParam String text,
            @RequestParam String optionA,
            @RequestParam String optionB,
            @RequestParam String optionC,
            @RequestParam String optionD,
            @RequestParam int correctIndex,
            HttpSession session,
            Model model) {

        Integer quizId = (Integer) session.getAttribute("tempQuizId");
        Integer current = (Integer) session.getAttribute("currentQuestion");

        if (quizId == null || current == null) {
            return "redirect:/create-quiz";
        }

        // (1) to validate unique options i.e avoids duplicate choices
        if (optionA.equals(optionB) || optionA.equals(optionC) || optionA.equals(optionD)
                || optionB.equals(optionC) || optionB.equals(optionD)
                || optionC.equals(optionD)) {

            model.addAttribute("error", "Options must be unique. Two or more options are identical.");
            model.addAttribute("quizId", quizId);
            model.addAttribute("current", current);
            model.addAttribute("total", session.getAttribute("totalQuestions"));
            model.addAttribute("text", text);
            model.addAttribute("optionA", optionA);
            model.addAttribute("optionB", optionB);
            model.addAttribute("optionC", optionC);
            model.addAttribute("optionD", optionD);
            model.addAttribute("correctIndex", correctIndex);

            return "add-question";
        }

        // (2) to validate unique question text inside one quiz
        List<Question> existing = quizService.getQuestions(quizId);
        String newTextNorm = text.trim().toLowerCase();
        for (Question qExisting : existing) {
            if (qExisting.getText() != null &&
                    qExisting.getText().trim().toLowerCase().equals(newTextNorm)) {

                model.addAttribute("error",
                        "This question already exists in this quiz. Please enter a different question.");
                model.addAttribute("quizId", quizId);
                model.addAttribute("current", current);
                model.addAttribute("total", session.getAttribute("totalQuestions"));
                model.addAttribute("text", text);
                model.addAttribute("optionA", optionA);
                model.addAttribute("optionB", optionB);
                model.addAttribute("optionC", optionC);
                model.addAttribute("optionD", optionD);
                model.addAttribute("correctIndex", correctIndex);

                return "add-question";
            }
        }

        // OK: clear old errors and save new question
        session.removeAttribute("error");
        Question q = new Question(current, text, optionA, optionB, optionC, optionD, correctIndex);
        quizService.addQuestion(quizId, q);

        session.setAttribute("currentQuestion", current + 1);

        return "redirect:/add-question";
    }

    //quiz attempt(student)
    //lists the quizzes and mark expired ones for display, makes the view simpler

    @GetMapping("/attempt-quiz")
    public String attemptSelect(Model model) {
        List<Map<String, Object>> quizzes = quizService.listQuizMeta();
        LocalDateTime now = LocalDateTime.now();

        for (Map<String, Object> m : quizzes) {
            String startStr = (String) m.get("start"); // "yyyy-MM-ddTHH:mm"
            String endStr = (String) m.get("end");

            try {
                LocalDateTime start = LocalDateTime.parse(startStr);
                LocalDateTime end = LocalDateTime.parse(endStr);

                boolean isExpired = now.isAfter(end);
                m.put("isExpired", isExpired);

                // Format for display (replace T with space)
                m.put("displayStart", startStr.replace("T", " "));
                m.put("displayEnd", endStr.replace("T", " "));

            } catch (Exception e) {
                //in case if parsing fails then just show raw strings and assume not expired
                m.put("isExpired", false);
                m.put("displayStart", startStr);
                m.put("displayEnd", endStr);
            }
        }

        model.addAttribute("quizzes", quizzes);
        return "attempt-select";
    }
    //show instructions for a quiz before student starts attempting the quiz
    @GetMapping("/attempt-quiz/{quizId}")
    public String showInstructions(@PathVariable int quizId, HttpSession session, Model model) {

        String username = (String) session.getAttribute("username");
        if (username == null)
            return "redirect:/login";

        Map<String, Object> meta = quizService.getQuizMeta(quizId);
        if (meta == null) {
            model.addAttribute("message", "Quiz not found.");
            return "review-not-allowed"; // or "quiz-locked"
        }

        String startStr = (String) meta.get("start");
        String endStr = (String) meta.get("end");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = LocalDateTime.parse(startStr);
        LocalDateTime endTime = LocalDateTime.parse(endStr);

        //invalid time check
        if (endTime.isBefore(startTime)) {
            model.addAttribute("message", "Admin entered invalid time: End time must be after Start time.");
            return "review-not-allowed";
        }


        if (now.isBefore(startTime)) {
            model.addAttribute("title", "Quiz has not started");   // ADD THIS
            model.addAttribute("message", "Quiz starts at: " + startStr);
            return "review-not-allowed";
        }


        if (now.isAfter(endTime)) {
            model.addAttribute("title", "Quiz has expired");   // ← ADD THIS
            model.addAttribute("message", "Quiz has expired. (Ended at: " + endStr + ")");
            return "review-not-allowed";
        }

        // show immediate results if attempted already
        List<Attempt> attempts = attemptService.getAttemptsForQuiz(quizId);
        for (Attempt a : attempts) {
            if (a.getUsername().equals(username)) {
                model.addAttribute("score", a.getScore());
                model.addAttribute("total", a.getTotal());
                model.addAttribute("quizId", quizId);
                model.addAttribute("already", true);
                int attemptId = username.hashCode() + quizId;
                model.addAttribute("attemptId", attemptId);
                return "result";
            }
        }

        //not attempted yet, so shows the instructions screen only
        String title = (String) meta.getOrDefault("title", "Quiz");
        String instructions = (String) meta.getOrDefault("instructions", "No instructions provided.");

        model.addAttribute("quizId", quizId);
        model.addAttribute("title", title);
        model.addAttribute("instructions", instructions);

        return "quiz-instructions"; //page with Start button
    }
    //initialize session state and begin a quiz attempt
    @GetMapping("/start-quiz/{quizId}")
    public String startQuiz(@PathVariable int quizId, HttpSession session, Model model) {

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        if (username == null || !"STUDENT".equals(role)) {
            return "redirect:/login";
        }

        //if already attempted, directly goes to the result
        List<Attempt> attempts = attemptService.getAttemptsForQuiz(quizId);
        for (Attempt a : attempts) {
            if (a.getUsername().equals(username)) {
                model.addAttribute("score", a.getScore());
                model.addAttribute("total", a.getTotal());
                model.addAttribute("quizId", quizId);
                model.addAttribute("already", true);
                int attemptId = username.hashCode() + quizId;
                model.addAttribute("attemptId", attemptId);
                return "result";
            }
        }

        //now actually starts the quiz and reset tracking in the session
        session.setAttribute(S_ATTEMPT_QUIZID, quizId);
        session.setAttribute(S_ATTEMPT_INDEX, 0);
        session.setAttribute(S_ATTEMPT_SCORE, 0);
        session.setAttribute(S_ATTEMPT_ANSWERS, new HashMap<Integer, Integer>());

        return "redirect:/attempt-question";
    }
    //shows the current question based on the sesssion index
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
    //handles the answer submission for a question, stores answer and updates the score
    @PostMapping("/attempt-submit")
    public String submitAnswer(@RequestParam(required = false) Integer selected,
            HttpSession session) {

        Integer quizId = (Integer) session.getAttribute(S_ATTEMPT_QUIZID);
        Integer idx = (Integer) session.getAttribute(S_ATTEMPT_INDEX);
        Integer score = (Integer) session.getAttribute(S_ATTEMPT_SCORE);

        List<Question> qs = quizService.getQuestions(quizId);
        Question q = qs.get(idx);

        //stores answer into the session map, use -1 if it is unanswered
        Map<Integer, Integer> answers = (Map<Integer, Integer>) session.getAttribute(S_ATTEMPT_ANSWERS);
        Integer submittedIndex = (selected != null) ? selected : -1;
        answers.put(q.getId(), submittedIndex);
        session.setAttribute(S_ATTEMPT_ANSWERS, answers);

        //grades this question,+1 per correct answer
        if (selected != null && selected == q.getCorrectIndex()) {
            score++;
            session.setAttribute(S_ATTEMPT_SCORE, score);
        }
        //advances the index and decide next page
        session.setAttribute(S_ATTEMPT_INDEX, idx + 1);

        if (idx + 1 >= qs.size())
            return "redirect:/attempt-finish";

        return "redirect:/attempt-question";
    }
    //finish attempt, persist the attempt with answers and clear session state
    @GetMapping("/attempt-finish")
    public String finishAttempt(HttpSession session, Model model) {

        Integer quizId = (Integer) session.getAttribute(S_ATTEMPT_QUIZID);
        Integer score = (Integer) session.getAttribute(S_ATTEMPT_SCORE);
        Map<Integer, Integer> answers = (Map<Integer, Integer>) session.getAttribute(S_ATTEMPT_ANSWERS);

        List<Question> qs = quizService.getQuestions(quizId);
        int total = qs.size();

        String username = (String) session.getAttribute("username");
        if (username == null)
            username = "anonymous";

        String ts = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());

        //save Attempt with answers into the attemptService
        Attempt a = new Attempt(username, quizId, score, total, ts, answers);
        attemptService.saveAttempt(a);

        int attemptId = username.hashCode() + quizId;

        //clear session state so same attempt can't be resumed
        session.removeAttribute(S_ATTEMPT_QUIZID);
        session.removeAttribute(S_ATTEMPT_INDEX);
        session.removeAttribute(S_ATTEMPT_SCORE);
        session.removeAttribute(S_ATTEMPT_ANSWERS);

        model.addAttribute("score", score);
        model.addAttribute("total", total);
        model.addAttribute("quizId", quizId);
        model.addAttribute("attemptId", attemptId);

        return "result";
    }

    //shows simple login form to verify identity before allowing management
    @GetMapping("/manage-my-quizzes-login")
    public String manageMyQuizzesLoginPage() {
        return "manage-my-quizzes-login";
    }
    //verifies password for the currently logged-in user before management actions
    @PostMapping("/manage-my-quizzes-login")
    public String manageMyQuizzesLoginPost(@RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        String sessionUser = (String) session.getAttribute("username");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        //prevents switching accounts during the management flow
        if (!sessionUser.trim().equalsIgnoreCase(username.trim())) {
            model.addAttribute("error",
                    "Warning: You are entering a different username. Please enter your correct username (" + sessionUser
                            + ").");
            return "manage-my-quizzes-login";
        }

        User u = auth.authenticateWithoutRole(username, password);
        if (u == null) {
            model.addAttribute("error", "Invalid credentials");
            return "manage-my-quizzes-login";
        }
        //store verified user id for subsequent management operations
        session.setAttribute(S_VERIFIED_USER, u.getId());
        return "redirect:/manage-my-quizzes";
    }
    //lists the quizzes created by the verified user(creator)
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
    //manage one quiz: show questions and lock status based on the start time
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

        Map<String, Object> meta = quizService.getQuizMeta(quizId);
        String start = (String) meta.get("start");
        String end = (String) meta.get("end");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        //just a small buffer, management becomes locked in 5 min before start
        LocalDateTime bufferTime = startTime.minusMinutes(5);

        boolean locked = now.isAfter(bufferTime);

        model.addAttribute("locked", locked);
        model.addAttribute("questions", quizService.getQuestions(quizId));
        model.addAttribute("quizId", quizId);

        return "manage-one-quiz";
    }
    //shows the form to add a question to an existing quiz, checks creator and locked state
    @GetMapping("/add-question-existing")
    public String addQuestionExistingForm(@RequestParam int quizId,
            HttpSession session,
            Model model) {

        Integer vid = (Integer) session.getAttribute(S_VERIFIED_USER);
        if (vid == null)
            return "redirect:/manage-my-quizzes-login";

        if (!quizService.isCreator(quizId, vid))
            return "redirect:/manage-my-quizzes";

        if (isManagementLocked(quizId))
            return "redirect:/manage-my-quizzes";

        model.addAttribute("quizId", quizId);
        return "add-question-existing";
    }
    //process adding a question to an existing quiz,same validations as the create flow
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

        if (isManagementLocked(quizId))
            return "redirect:/manage-my-quizzes";

        List<Question> qs = quizService.getQuestions(quizId);
        int nextId = qs.size() + 1;

        // 1) validate unique options
        if (optionA.equals(optionB) || optionA.equals(optionC) || optionA.equals(optionD)
                || optionB.equals(optionC) || optionB.equals(optionD)
                || optionC.equals(optionD)) {

            session.setAttribute("error", "Options must be unique. Two or more options are identical.");
            return "redirect:/add-question-existing?quizId=" + quizId;
        }

        // 2) validate unique question text inside this quiz
        String newTextNorm = text.trim().toLowerCase();
        for (Question qExisting : qs) {
            if (qExisting.getText() != null &&
                    qExisting.getText().trim().toLowerCase().equals(newTextNorm)) {

                session.setAttribute("error",
                        "This question already exists in this quiz. Please enter a different question.");
                return "redirect:/add-question-existing?quizId=" + quizId;
            }
        }

        // OK: clear error and save new question into the quiz
        session.removeAttribute("error");
        Question q = new Question(nextId, text, optionA, optionB, optionC, optionD, correctIndex);
        quizService.addQuestion(quizId, q);

        return "redirect:/manage-one-quiz?quizId=" + quizId;
    }
    //delete question, reindexes remaining questions so ids reamain continuous
    @GetMapping("/delete-question")
    public String deleteQuestion(@RequestParam int quizId,
            @RequestParam int qid,
            HttpSession session) {

        Integer vid = (Integer) session.getAttribute(S_VERIFIED_USER);
        if (vid == null)
            return "redirect:/manage-my-quizzes-login";

        if (!quizService.isCreator(quizId, vid))
            return "redirect:/manage-my-quizzes";

        if (isManagementLocked(quizId))
            return "redirect:/manage-my-quizzes";

        List<Question> qs = quizService.getQuestions(quizId);

        qs.removeIf(q -> q.getId() == qid);
        //rebuild ids starting from 1 to keep ordering simple
        List<Question> updated = new ArrayList<>();
        int id = 1;
        for (Question q : qs) {
            updated.add(
                    new Question(id, q.getText(), q.getOptionA(), q.getOptionB(), q.getOptionC(), q.getOptionD(),
                            q.getCorrectIndex()));
            id++;
        }

        quizService.overwriteQuestions(quizId, updated);

        return "redirect:/manage-one-quiz?quizId=" + quizId;
    }
    //shows edit-question form to ensure creator and also unlocked
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

        if (isManagementLocked(quizId))
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
    //process edit submission, validating uniqueness and overwriting the list
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

        if (isManagementLocked(quizId))
            return "redirect:/manage-my-quizzes";

        List<Question> qs = quizService.getQuestions(quizId);
        List<Question> updated = new ArrayList<>();

        // 1) validate unique options
        if (optionA.equals(optionB) || optionA.equals(optionC) || optionA.equals(optionD)
                || optionB.equals(optionC) || optionB.equals(optionD)
                || optionC.equals(optionD)) {

            session.setAttribute("error", "Options must be unique. Two or more options are identical.");
            return "redirect:/edit-question?quizId=" + quizId + "&qid=" + id;
        }

        // 2) validate unique question text inside this quiz (ignore the question being edited)
        String newTextNorm = text.trim().toLowerCase();
        for (Question qExisting : qs) {
            if (qExisting.getId() != id &&
                    qExisting.getText() != null &&
                    qExisting.getText().trim().toLowerCase().equals(newTextNorm)) {

                session.setAttribute("error", "Another question in this quiz already has the same text.");
                return "redirect:/edit-question?quizId=" + quizId + "&qid=" + id;
            }
        }

        // 3) build updated list and replace the question in-memory
        for (Question q : qs) {
            if (q.getId() == id) {
                updated.add(new Question(id, text, optionA, optionB, optionC, optionD, correctIndex));
            } else {
                updated.add(q);
            }
        }

        session.removeAttribute("error");
        quizService.overwriteQuestions(quizId, updated);

        return "redirect:/manage-one-quiz?quizId=" + quizId;
    }

    //student view

    //shows available quizzes to students, ther is no role check here, view-level can hide start
    @GetMapping("/available-quizzes")
    public String availableQuizzes(Model model) {
        model.addAttribute("quizzes", quizService.listQuizMeta());
        return "available-quizzes";
    }
    //leaderboard for a quiz, returns sorted attempts the highest first
    @GetMapping("/leaderboard")
    public String leaderboard(@RequestParam int quizId, Model model, HttpSession session) {

        List<Attempt> attempts = attemptService.getAttemptsForQuiz(quizId);
        attempts.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));

        model.addAttribute("quizId", quizId);
        model.addAttribute("attempts", attempts);
        model.addAttribute("role", session.getAttribute("role"));

        return "leaderboard";
    }

    // review a specific attempt after the quiz end-builds ReviewItem list for view
    @GetMapping("/review-quiz/{attemptId}")
    public String reviewQuiz(@PathVariable int attemptId, HttpSession session, Model model) {

        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        if (username == null || !"STUDENT".equals(role)) {
            return "redirect:/login";
        }

        // checks if the student attempts this quiz
        Attempt attempt = attemptService.findAttemptByMockId(attemptId, username);

        if (attempt == null) {
            // Student has not attempted this quiz (or bad/messed URL)
            model.addAttribute("message", "You did not attempt this quiz, so review is not available.");
            return "review-not-allowed"; // reuse your existing page that shows a message
        }

        // checks if quiz has ended and review is allowed only after the end time
        Map<String, Object> meta = quizService.getQuizMeta(attempt.getQuizId());
        if (meta == null || meta.get("end") == null) {
            model.addAttribute("message", "Review not available because quiz details are missing.");
            return "review-not-allowed";
        }

        String endStr = (String) meta.get("end");
        LocalDateTime endTime = LocalDateTime.parse(endStr);
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(endTime)) {
            // if quiz has not ended yet, block the review and show end time
            model.addAttribute("message",
                    "Review will be available after the quiz ends at: " + endStr);
            return "review-not-allowed";
        }

        // quiz is over and attempt exists then build review data
        List<Question> questions = quizService.getQuestions(attempt.getQuizId());
        Map<Integer, Integer> userAnswers = attempt.getAnswers();

        List<ReviewItem> reviewData = new ArrayList<>();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            Integer submitted = userAnswers.getOrDefault(q.getId(), -1);
            boolean isCorrect = submitted != -1 && submitted.equals(q.getCorrectIndex());

            reviewData.add(new ReviewItem(i + 1, q, submitted, isCorrect));
        }

        model.addAttribute("quizId", attempt.getQuizId());
        model.addAttribute("reviewData", reviewData);
        model.addAttribute("attempt", attempt);

        return "review-quiz";
    }


    //a helper data transfer object inside the QuizController
    public static class AttemptView {
        private int quizId;
        private String quizTitle;
        private int score;
        private int total;
        private String timestamp;
        private int attemptId;

        public int getQuizId() {
            return quizId;
        }

        public String getQuizTitle() {
            return quizTitle;
        }

        public int getScore() {
            return score;
        }

        public int getTotal() {
            return total;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public int getAttemptId() {
            return attemptId;
        }

        public AttemptView(int quizId, String quizTitle, int score, int total, String timestamp, int attemptId) {
            this.quizId = quizId;
            this.quizTitle = quizTitle;
            this.score = score;
            this.total = total;
            this.timestamp = timestamp;
            this.attemptId = attemptId;
        }
    }
    //shows list of quizzes user has attempted with compact AttemptVew data transfer objects(DTOs)
    @GetMapping("/my-attempted-quizzes")
    public String myAttemptedQuizzes(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");

        if (username == null || !"STUDENT".equals(role)) {
            return "redirect:/login";
        }

        List<Attempt> userAttempts = attemptService.getAttemptsForUser(username);
        List<Map<String, Object>> allQuizzes = quizService.listQuizMeta();

        // build quizId -> title map for quick lookup
        Map<Integer, String> quizTitles = new HashMap<>();
        for (Map<String, Object> m : allQuizzes) {
            Integer id = ((Number) m.get("id")).intValue();
            String title = (String) m.get("title");
            quizTitles.put(id, title);
        }

        // Build flat list of AttemptView, attemptId uses username hash + quizId
        List<AttemptView> views = new ArrayList<>();
        int usernameHash = username.hashCode();

        for (Attempt a : userAttempts) {
            int qid = a.getQuizId();
            String title = quizTitles.getOrDefault(qid, "Quiz " + qid);
            int attemptId = usernameHash + qid;

            views.add(new AttemptView(
                    qid,
                    title,
                    a.getScore(),
                    a.getTotal(),
                    a.getTimestamp(),
                    attemptId));
        }

        model.addAttribute("attemptViews", views);
        return "my-attempted-quizzes";
    }

    //redirects user to the appropriate dashboard based on role
    private String redirectDashboard(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if ("ADMIN".equals(role))
            return "redirect:/admin-dashboard";
        else
            return "redirect:/student-dashboard";
    }
    //checks whether quiz management is locked(locked when quizze's start time passed)
    private boolean isManagementLocked(int quizId) {
        Map<String, Object> meta = quizService.getQuizMeta(quizId);
        if (meta == null)
            return false;
        Object startObj = meta.get("start");
        if (startObj == null)
            return false;

        try {
            LocalDateTime start = LocalDateTime.parse(startObj.toString());
            return LocalDateTime.now().isAfter(start);
        } catch (DateTimeParseException e) {
            //in case if parsing fails, then do not lock(fail open)
            return false;
        }
    }
}
