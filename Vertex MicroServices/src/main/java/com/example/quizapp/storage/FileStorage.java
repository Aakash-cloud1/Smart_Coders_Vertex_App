package com.example.quizapp.storage;

import com.example.quizapp.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.security.MessageDigest;

public class FileStorage {

    private final Path dataDir = Paths.get("data");
    private final Path usersDir = dataDir.resolve("users");
    private final Path adminFile = usersDir.resolve("admin.json");
    private final Path studentFile = usersDir.resolve("student.json");

    private final Path metaFile = dataDir.resolve("quiz-meta.json");
    private final Path attemptsFile = dataDir.resolve("attempts.json");
    private final Path quizzesDir = dataDir.resolve("quizzes");

    private final ObjectMapper mapper = new ObjectMapper();
    //creates all necessary folders and empty JSON files if missing
    public FileStorage() {
        try {
            mapper.activateDefaultTyping(
                    mapper.getPolymorphicTypeValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL);

            Files.createDirectories(dataDir);
            Files.createDirectories(quizzesDir);
            Files.createDirectories(usersDir);
            //just to make sure the file exist so we never read null
            if (!Files.exists(adminFile)) Files.write(adminFile, "[]".getBytes());
            if (!Files.exists(studentFile)) Files.write(studentFile, "[]".getBytes());
            if (!Files.exists(metaFile)) Files.write(metaFile, "[]".getBytes());
            if (!Files.exists(attemptsFile)) Files.write(attemptsFile, "[]".getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //loads all admin users from admin.json

    public synchronized List<User> loadAdmins() {
        try {
            return mapper.readValue(adminFile.toFile(), new TypeReference<List<User>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    //adds a new admin to the admin list
    public synchronized void appendAdmin(User u) {
        List<User> admins = loadAdmins();
        admins.add(u);
        saveAdmins(admins);
    }
    //saves the full admin list back to file
    private synchronized void saveAdmins(List<User> admins) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(adminFile.toFile(), admins);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //loads all students from student.json
    public synchronized List<User> loadStudents() {
        try {
            return mapper.readValue(studentFile.toFile(), new TypeReference<List<User>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    //adds a new student to the students list
    public synchronized void appendStudent(User u) {
        List<User> students = loadStudents();
        students.add(u);
        saveStudents(students);
    }
    //saves the full student list back to file
    private synchronized void saveStudents(List<User> students) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(studentFile.toFile(), students);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //loads all mthds for all the quizzes like title,start or end,creator etc
    public synchronized List<Map<String, Object>> loadQuizMeta() {
        try {
            return mapper.readValue(metaFile.toFile(),
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    //adds a new quiz entry to quiz-meta.json
    public synchronized void appendQuizMeta(int id,
                                            String title,
                                            int creatorId,
                                            String startDateTime,
                                            String endDateTime,
                                            String instructions) {
        List<Map<String, Object>> meta = loadQuizMeta();

        Map<String, Object> entry = new HashMap<>();
        entry.put("id", id);
        entry.put("title", title);
        entry.put("creatorId", creatorId);
        entry.put("start", startDateTime);
        entry.put("end", endDateTime);
        entry.put("instructions", instructions);

        meta.add(entry);
        saveQuizMeta(meta);
    }
    //saves full quiz metadata list
    private void saveQuizMeta(List<Map<String, Object>> meta) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(metaFile.toFile(), meta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //adds one question to the quiz file

    public synchronized void saveQuestionToQuiz(int quizId, Question q) {
        List<Question> all = loadQuestionsForQuiz(quizId);
        all.add(q);
        saveQuizQuestions(quizId, all);
    }
    //loads all questions of a specific quiz
    public synchronized List<Question> loadQuestionsForQuiz(int quizId) {
        Path quizFile = quizzesDir.resolve("quiz-" + quizId + ".json");
        if (!Files.exists(quizFile)) return new ArrayList<>();

        try {
            return mapper.readValue(quizFile.toFile(), new TypeReference<List<Question>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    //overwrites the entire question file for a quiz
    public synchronized void overwriteQuizFile(int quizId, List<Question> questions) {
        saveQuizQuestions(quizId, questions);
    }
    //saves the updated set of questions into the quiz's JSON file
    private void saveQuizQuestions(int quizId, List<Question> list) {
        Path quizFile = quizzesDir.resolve("quiz-" + quizId + ".json");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(quizFile.toFile(), list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //appends one attempt record to attempts.json
    public synchronized void appendAttempt(Attempt a) {
        List<Attempt> attempts = loadAllAttempts();
        attempts.add(a);
        saveAttempts(attempts);
    }
    //returns only the attempts made for a spe quiz
    public synchronized List<Attempt> loadAttemptsForQuiz(int quizId) {
        List<Attempt> all = loadAllAttempts();
        List<Attempt> out = new ArrayList<>();

        for (Attempt a : all) {
            if (a.getQuizId() == quizId) out.add(a);
        }
        return out;
    }

    //loads every attempt ever recorded
    public List<Attempt> loadAllAttempts() {
        try {
            return mapper.readValue(attemptsFile.toFile(), new TypeReference<List<Attempt>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    //saves the complete list of attempts
    public void saveAttempts(List<Attempt> attempts) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(attemptsFile.toFile(), attempts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //hasehs a password using SHA-256,used for storing secure passwods
    public static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return s;
        }
    }
    //generates the next available use ID by scanning admins and students
    public synchronized int nextUserId() {
        int max = 0;

        for (User u : loadAdmins()) {
            if (u.getId() > max) max = u.getId();
        }
        for (User u : loadStudents()) {
            if (u.getId() > max) max = u.getId();
        }

        return max + 1;//the next ID in sequence
    }
}
