

# SafeCore

SafeCore is a Java desktop application developed as a **Software Engineering project** with a strong focus on **security, clean architecture, and maintainability**.
The goal of the project is to demonstrate how a real-world authentication system can be designed by applying solid engineering principles rather than building a simple CRUD application.


## Features

* User **registration and login** workflow
* Secure **password hashing with bcrypt**
* **Smart Password Hints** providing real-time, non-blocking feedback on password strength
* JavaFX-based **desktop UI** following the MVC pattern
* Clean **layered architecture** (UI, Service, Persistence, Security)
* JPA/Hibernate persistence with **PostgreSQL**
* Designed for **testability and extensibility**



## Architecture Overview

The application follows a **multi-layered architecture** to ensure clear separation of responsibilities:

* **UI Layer (JavaFX)**
  Handles user interaction and presentation logic only.

* **Service Layer**
  Contains business logic (authentication, validation, security rules).

* **Persistence Layer (DAO + JPA)**
  Manages database access using Hibernate and the DAO pattern.

* **Security Layer**
  Responsible for password hashing and security-related utilities.

This structure improves maintainability, reduces coupling, and allows each layer to evolve independently.



## Smart Password Hints

SafeCore introduces *Smart Password Hints* to improve user awareness of password security.
Instead of blocking registration, the system analyzes the password using modular rules (Strategy pattern) and provides **context-aware hints** with different severity levels.

This approach simulates a real-world security assistant while respecting user autonomy and privacy.



## Tech Stack

* **Java 17**
* **JavaFX**
* **Maven**
* **JPA / Hibernate**
* **PostgreSQL**
* **bcrypt**
* **JUnit 5**
