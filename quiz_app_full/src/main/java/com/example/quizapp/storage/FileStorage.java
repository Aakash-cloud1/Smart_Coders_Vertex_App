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

    public FileStorage() {
        try {
            // Enable polymorphic support for Admin/Student classes
            mapper.activateDefaultTyping(
                    mapper.getPolymorphicTypeValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL);

            Files.createDirectories(dataDir);
            Files.createDirectories(usersDir);
            Files.createDirectories(quizzesDir);

            if (!Files.exists(adminFile)) Files.write(adminFile, "[]".getBytes());
            if (!Files.exists(studentFile)) Files.write(studentFile, "[]".getBytes());
            if (!Files.exists(metaFile)) Files.write(metaFile, "[]".getBytes());
            if (!Files.exists(attemptsFile)) Files.write(attemptsFile, "[]".getBytes());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ======================= USER HANDLING =========================

    public synchronized List<User> loadAdmins() {
        try {
            return mapper.readValue(adminFile.toFile(), new TypeReference<List<User>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized List<User> loadStudents() {
        try {
            return mapper.readValue(studentFile.toFile(), new TypeReference<List<User>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized void appendAdmin(User u) {
        List<User> admins = loadAdmins();
        admins.add(u);
        saveAdmins(admins);
    }

    public synchronized void appendStudent(User u) {
        List<User> students = loadStudents();
        students.add(u);
        saveStudents(students);
    }

    private synchronized void saveAdmins(List<User> admins) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(adminFile.toFile(), admins);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void saveStudents(List<User> students) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(studentFile.toFile(), students);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Generate next user ID (admin + student combined count)
    public synchronized int nextUserId() {
        return loadAdmins().size() + loadStudents().size() + 1;
    }

    // ======================= QUIZ META =========================

    public synchronized List<Map<String, Object>> loadQuizMeta() {
        try {
            return mapper.readValue(metaFile.toFile(),
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized void appendQuizMeta(int id, String title, int creatorId) {
        List<Map<String, Object>> meta = loadQuizMeta();
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", id);
        entry.put("title", title);
        entry.put("creatorId", creatorId);
        meta.add(entry);
        saveQuizMeta(meta);
    }

    private synchronized void saveQuizMeta(List<Map<String, Object>> meta) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(metaFile.toFile(), meta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ======================= QUIZ QUESTIONS =========================

    public synchronized void saveQuestionToQuiz(int quizId, Question q) {
        List<Question> all = loadQuestionsForQuiz(quizId);
        all.add(q);
        saveQuizQuestions(quizId, all);
    }

    public synchronized List<Question> loadQuestionsForQuiz(int quizId) {
        Path quizFile = quizzesDir.resolve("quiz-" + quizId + ".json");
        if (!Files.exists(quizFile)) return new ArrayList<>();
        try {
            return mapper.readValue(quizFile.toFile(), new TypeReference<List<Question>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public synchronized void overwriteQuizFile(int quizId, List<Question> questions) {
        saveQuizQuestions(quizId, questions);
    }

    private void saveQuizQuestions(int quizId, List<Question> list) {
        Path quizFile = quizzesDir.resolve("quiz-" + quizId + ".json");
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(quizFile.toFile(), list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ======================= ATTEMPTS =========================

    public synchronized void appendAttempt(Attempt a) {
        List<Attempt> attempts = loadAllAttempts();
        attempts.add(a);
        saveAttempts(attempts);
    }

    public synchronized List<Attempt> loadAttemptsForQuiz(int quizId) {
        List<Attempt> all = loadAllAttempts();
        List<Attempt> out = new ArrayList<>();
        for (Attempt a : all) {
            if (a.getQuizId() == quizId) out.add(a);
        }
        return out;
    }

    private List<Attempt> loadAllAttempts() {
        try {
            return mapper.readValue(attemptsFile.toFile(), new TypeReference<List<Attempt>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void saveAttempts(List<Attempt> attempts) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(attemptsFile.toFile(), attempts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ======================= HASH =========================

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
}
