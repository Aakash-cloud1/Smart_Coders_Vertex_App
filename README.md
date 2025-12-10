
# 📚 Quiz Application – Java Spring Boot Project  
A **Java – Spring Boot – Maven** based Quiz Application supporting quiz creation, quiz attempts, QR code generation, JSON-based storage, authentication, and a modular MVC architecture.  

This project is designed for academic & educational demonstration and showcases a full end-to-end quiz management workflow.

## 👥 Team Contributions

### **BT2024208 – Attuluri Aakash**
- Implemented major components of **Authentication**  
  (`AuthenticationService.java`, `AuthenticationController.java`, `User.java`, `Student.java`, `Admin.java`)  
- Coordinated team work distribution, debugging, and cross-file integration.

### **BT2024211 – Rudraraju Dhanush Varma**
- Implemented all Java files inside the **qrcode** module.  
- Worked on **qr-style.css**.  
- Contributed to authentication logic (Controller + Service).

### **BT2024166 – Abhiram PBS**
- Implemented **all HTML template files** (UI pages).  
- Contributed to `qr-style.css`.  
- Developed `FileStorage.java` functionality:
  - Loading Student/Admin details from JSON  
  - Adding & saving new users  
  - Generating next available user IDs  

### **BT2024165 – Kenche Siddarth**
- Developed Java files for **quiz implementation**  
  (`QuizController.java`, `QuizService.java`, `Question.java`, `Quiz.java`)  
- Handled **full JSON-based storage** for quizzes:
  - Loading quizzes  
  - Adding new quiz metadata to `quiz-meta.json`  
  - Saving & updating quiz structures  
- Contributed to `FileStorage.java`.

### **BT2024139 – Mannam Geethika**
- Implemented major portions of **Quiz Logic**  
  (`QuizController.java`, `QuizService.java`, `Question.java`, `Quiz.java`)  
- Contributed to `LogoutController.java` and `PageController.java`.

### **BT2024215 – Akshaya Akula**
- Developed components related to **Quiz Attempts**  
  (`Attempt.java`, `AttemptService.java`)  
- Implemented **Review My Quiz** feature.  
- Updated `FileStorage.java` to support:
  - Appending attempts to `attempts.json`  
  - Fetching attempts for a specific quiz  
  - Loading & saving attempt lists  

## 📁 Project Structure
```
quiz_app_full/
│── pom.xml
│── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── service/
│   │   │   ├── qrcode/
│   │   │   └── storage/
│   │   └── resources/
│   │       ├── templates/
│   │       └── static/
│   └── test/
│── target/
```

## ⭐ Key Features
- 🔐 Secure Authentication  
- 📝 Quiz Management  
- 🎯 Quiz Attempt & Review  
- 🗄 JSON Storage System  
- 🔳 QR Code Generation  
- 🌐 UI Templates  
- 🧱 Clean MVC Architecture  

## 🔧 Requirements
- Java 8+  
- Maven 3.6+  
- Spring Boot dependencies

## 🚀 How to Run
```
cd quiz_app_full
mvn clean install
mvn spring-boot:run
```

## 🧩 Module Overview
Details on controllers, services, models, storage system, QR code utilities, and UI templates.

## 📜 License
Academic and educational use only.

