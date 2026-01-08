# SafeCore – Secure Password Vault

SafeCore is an advanced Java desktop application designed for secure credential management. The project was developed following Software Engineering principles, focusing on a robust, testable architecture and data security.

Unlike simple password managers, SafeCore integrates Spring Boot with JavaFX, ensuring clean dependency injection and professional application lifecycle management.

## Key Features

* Secure Authentication: User login and registration with password hashing via BCrypt.
* Encrypted Vault: Storage of credentials (Service, Username, Password) encrypted using the AES-256 algorithm.
* Spring Boot & JavaFX Integration: Modern architecture with controller management through the Spring Context.
* Smart Password Hints: Real-time feedback on password strength during registration.
* Backup Export: Functionality to export the vault into an encrypted JSON format for data portability.
* Intelligent Search: Quick filtering of saved credentials within the Dashboard.
* Modern UI: Smooth interface with animated transitions (Fade In/Out) and visual feedback.

## Tech Stack

* Core: Java 17, Spring Boot 3.x
* UI: JavaFX 17 (FXML, CSS)
* Persistence: Spring Data JPA, Hibernate
* Database: H2 (Embedded for portability) / PostgreSQL support
* Security: BCrypt (Hashing), AES (Encryption), Jackson (Secure JSON Serialization)
* Build Tool: Maven

## System Requirements

* Java Development Kit (JDK): Version 17 or higher.
* Maven: Version 3.6 or higher.
* OS: macOS (optimized), Windows, or Linux.

## Installation and Configuration

### 1. Clone the Repository
```bash
git clone [https://github.com/DaniaCiampalini/SafeCoreProject.git](https://github.com/DaniaCiampalini/SafeCoreProject.git)
cd SafeCoreProject
```

### 2. Dependency Configuration (Maven)

Ensure your pom.xml includes the necessary modules for Jackson and JavaFX. Refresh Maven dependencies:
``` bash
mvn clean install
```

### 3. Database

The application is configured by default to use H2 in file mode. The database will be created automatically in the project folder (safecore_db.mv.db) upon the first execution.

How to Run the Application
Option A: Via IntelliJ IDEA (Recommended)

Open the project in IntelliJ.

Wait for Maven dependencies to load.

Locate the class com.safecore.SafeCoreApplication (annotated with @SpringBootApplication).

Right-click the class or the main method and select Run.

Option B: Via Command Line

From the project root directory:
``` bash
mvn spring-boot:run
```
## Project Architecture
The project follows a Clean Layered Architecture:

* UI Layer (com.safecore.ui): JavaFX Controllers, navigation logic (SceneNavigator), and FXML files.

* Business Layer (com.safecore.business): Service classes (UserService, VaultService) containing business logic.

* Security Layer (com.safecore.security): Encryption strategies and hashing utilities.

* Persistence Layer (com.safecore.persistence): JPA Entities and Repository interfaces for data access.

## Security Details
Zero Knowledge: Vault passwords are encrypted locally before being saved. SafeCore never stores your passwords in plain text.

Backup Protection: Exported backup files (.scb) are Base64 encoded encrypted JSON packages, making them unreadable without the correct key.

Session Management: A centralized SessionContext securely manages the identity of the logged-in user across different UI scenes.

## Testing
To run unit and integration tests (JUnit 5):
```bash
mvn test
```

