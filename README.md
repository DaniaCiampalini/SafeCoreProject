English | [Italiano](https://github.com/DaniaCiampalini/SafeCoreProject/new/main) 

# SafeCore – Secure Password Vault

SafeCore is an advanced Java desktop application designed for secure credential management. The project was developed following high-level Software Engineering principles, focusing on a robust, testable architecture, advanced design patterns, and the newest data security.

Unlike simple password managers, SafeCore integrates **Spring Boot** with **JavaFX**, ensuring clean dependency injection, professional application lifecycle management, and a highly responsive user interface.

## Key Features

*   **Secure Authentication**: User login and registration with password hashing via **BCrypt** and real-time strength validation.
*   **Encrypted Vault**: Storage of credentials (Service, Username, Password) encrypted using the **AES-256** algorithm with unique IVs for each entry.
*   **Spring Boot & JavaFX Integration**: Modern architecture with controller management through the Spring Context and automated FXML loading.
*   **Advanced UI/UX**: Smooth interface with animated transitions (Fade In/Out), hover effects, and a centralized "Toast" notification system.
*   **Smart Password Generator**: Built-in tool to create strong, customizable passwords.
*   **Auto-Fill System**: Intelligent credential typing automation using `java.awt.Robot` to streamline login processes.
*   **Security Audit**: Real-time health score for your vault, identifying weak, reused, or compromised passwords.
*   **SafeSend**: Securely share sensitive text or passwords via encrypted temporary links with self-destruction options.
*   **Email Alias Manager**: Generate and manage service-specific email aliases to protect your primary address from spam and leaks.
*   **Backup & Export**: Functionality to export the vault into an encrypted JSON format (.scb) for secure data portability.
*   **Intelligent Search**: Quick, real-time filtering of saved credentials within the Dashboard.

## Technical Excellence & Design Patterns

The project implements several advanced software patterns to ensure maintainability and scalability:

*   **Observer Pattern**: Implemented in the `VaultService` to automatically notify the UI when data changes, eliminating manual refresh logic.
*   **Factory Pattern**: Used for encryption strategies, allowing the system to switch between different algorithms (e.g., AES, ChaCha20) at runtime.
*   **Strategy Pattern**: Defines interchangeable encryption algorithms encapsulated within the factory.
*   **Builder Pattern**: Utilized for creating complex domain objects like `User` while maintaining immutability.
*   **Global Exception Handling**: A centralized mechanism captures runtime errors and database exceptions, presenting them to the user via friendly JavaFX dialogs instead of raw stack traces.

## Tech Stack

*   **Core**: Java 17, Spring Boot 3.x
*   **UI**: JavaFX 17 (FXML, CSS)
*   **Persistence**: Spring Data JPA, Hibernate
*   **Database**: H2 (Embedded for portability)
*   **Security**: BCrypt (Hashing), AES-256 (Encryption), Jackson (Secure JSON Serialization)
*   **Validation**: JSR 380 (Bean Validation ready)
*   **Build Tool**: Maven

## System Requirements

*   **Java Development Kit (JDK)**: Version 17 or higher.
*   **Maven**: Version 3.6 or higher.
*   **OS**: macOS, Windows, or Linux.

## Installation and Configuration

### 1. Clone the Repository
```bash
git clone https://github.com/DaniaCiampalini/SafeCoreProject.git
cd SafeCoreProject
```

### 2. Dependency Configuration (Maven)
Ensure your environment is set up for Java 17 and Maven. Install all required dependencies:
```bash
mvn clean install
```

### 3. Database
The application is configured by default to use **H2** in file mode. The database file (`safecore_db.mv.db`) will be created automatically in the project folder upon the first execution.

## How to Run the Application

### Option A: Via IntelliJ IDEA (Recommended)
1.  Open the project in IntelliJ.
2.  Wait for Maven dependencies to load.
3.  Locate the class `com.safecore.SafeCoreApplication`.
4.  Right-click the class and select **Run**.

### Option B: Via Command Line
```bash
mvn spring-boot:run
```

## Project Architecture

SafeCore follows a **Clean Layered Architecture**:

*   **UI Layer** (`com.safecore.ui`): JavaFX Controllers, CSS styling, and FXML layouts.
*   **Business Layer** (`com.safecore.business`): Services containing the core logic (Vault, Audit, SafeSend, etc.).
*   **Security Layer** (`com.safecore.security`): Encryption strategies, Hashing utilities, and Key Management.
*   **Persistence Layer** (`com.safecore.persistence`): JPA Entities and Repositories for data access.

## Security Details

*   **Zero Knowledge**: Vault passwords are encrypted locally before being saved. SafeCore never stores your master password or vault credentials in plain text.
*   **Domain Integrity**: Robust validation ensures that no invalid or dangerous data enters the system.
*   **Session Management**: A centralized `SessionContext` securely manages the identity of the logged-in user across different UI scenes.

## Testing

SafeCore maintains high code quality through comprehensive testing:
*   **Unit Testing**: Isolated testing of services using **Mockito**.
*   **Integration Testing**: Full-flow verification (Registration → Login → Vault) using `@SpringBootTest`.

To run tests:
```bash
mvn test
```

