# 📚 Quiz Application – Java Spring Boot Project

A Java – Spring Boot – Maven based Quiz Application supporting quiz creation, quiz attempts, QR code generation, JSON-based storage, authentication, and a modular MVC architecture.

This project is designed for academic & educational demonstration and showcases a full end-to-end quiz management workflow.

## Team Contributions
### BT2024208 – Attuluri Aakash
- Implemented major components of Authentication (AuthenticationService.java, AuthenticationController.java, User.java, Student.java, Admin.java)
- Coordinated team work distribution, debugging, and cross-file integration.

### BT2024211 – Rudraraju Dhanush Varma
- Implemented all Java files inside the qrcode module.
- Worked on qr-style.css.
- Contributed to authentication logic (Controller + Service).

### BT2024166 – Abhiram PBS
- Implemented all HTML template files (UI pages).
- Contributed to qr-style.css.
- Developed FileStorage.java functionality:
  - Loading Student/Admin details from JSON
  - Adding & saving new users
  - Generating next available user IDs

### BT2024165 – Kenche Siddarth
- Developed Java files for quiz implementation (QuizController.java, QuizService.java, Question.java, Quiz.java)
- Handled JSON-based storage for quizzes: loading, saving, updating structures
- Contributed to FileStorage.java.

### BT2024139 – Mannam Geethika
- Implemented major portions of Quiz Logic (QuizController.java, QuizService.java, Question.java, Quiz.java)
- Contributed to LogoutController.java and PageController.java.

### BT2024215 – Akshaya Akula
- Developed components related to Quiz Attempts (Attempt.java, AttemptService.java)
- Implemented Review My Quiz feature.
- Updated FileStorage.java to support attempts.json operations.

## Project Structure
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

## Key Features
### Secure Authentication
- Login/signup for Admins and Students

### Quiz Management
- Create, modify, and update quizzes
- JSON-based persistent quiz storage

### Quiz Attempt & Review
- Attempt quizzes
- Automatic evaluation
- Review My Quiz feature

### JSON Storage System
- Users, Quizzes, Attempts stored in JSON

### QR Code Generation
- Generate QR codes for quiz results, verification, admin tools

### UI Templates
- Built using Thymeleaf

### Clean MVC Architecture
- Controllers → Services → Models → Storage

## Requirements
- Java 8+
- Maven 3.6+
- Spring Boot dependencies (pom.xml)

## How to Run
```
cd quiz_app_full
mvn clean install
mvn spring-boot:run
```

## Module Explanations
### controller/
Routing & HTTP request handling.

### model/
Data models for Users, Quizzes, Attempts.

### service/
Business logic for authentication, quiz operations, scoring, attempts.

### storage/
Handles JSON-based persistence.

### qrcode/
QR code generator utilities.

### templates/
Thymeleaf UI pages.

## Testing the Application
- Authentication
- Quiz creation/editing
- Quiz attempts
- Review answers
- JSON persistence
- QR code generation

## License
This project is for academic and educational use only.
