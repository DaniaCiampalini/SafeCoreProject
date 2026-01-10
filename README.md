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

---

# SafeCore – Vault di Password Sicuro

SafeCore è un'applicazione desktop Java avanzata progettata per la gestione sicura delle credenziali. Il progetto è stato sviluppato seguendo elevati principi di Ingegneria del Software, concentrandosi su un'architettura robusta e testabile, design pattern avanzati e sicurezza dei dati all'avanguardia.

A differenza dei semplici gestori di password, SafeCore integra **Spring Boot** con **JavaFX**, garantendo una gestione pulita della dependency injection, del ciclo di vita dell'applicazione e un'interfaccia utente altamente reattiva.

## Funzionalità Principali

*   **Autenticazione Sicura**: Login e registrazione utente con hashing delle password tramite **BCrypt** e validazione della robustezza in tempo reale.
*   **Vault Crittografato**: Archiviazione delle credenziali (Servizio, Username, Password) cifrate con l'algoritmo **AES-256** utilizzando IV univoci per ogni voce.
*   **Integrazione Spring Boot & JavaFX**: Architettura moderna con gestione dei controller tramite il contesto Spring e caricamento automatizzato dei file FXML.
*   **UI/UX Avanzata**: Interfaccia fluida con transizioni animate (Fade In/Out), effetti hover e un sistema centralizzato di notifiche "Toast".
*   **Generatore di Password**: Strumento integrato per creare password sicure e personalizzabili.
*   **Sistema di Auto-Fill**: Automazione intelligente della digitazione delle credenziali tramite `java.awt.Robot` per velocizzare i processi di login.
*   **Security Audit**: Valutazione in tempo reale della sicurezza del vault, identificando password deboli, riutilizzate o compromesse.
*   **SafeSend**: Condivisione sicura di testo sensibile o password tramite link temporanei crittografati con opzioni di auto-distruzione.
*   **Gestore Alias Email**: Genera e gestisce alias email specifici per servizio per proteggere il tuo indirizzo primario da spam e leak.
*   **Backup ed Esportazione**: Funzionalità per esportare il vault in un formato JSON cifrato (.scb) per una portabilità sicura dei dati.
*   **Ricerca Intelligente**: Filtraggio rapido e in tempo reale delle credenziali salvate all'interno della Dashboard.

## Eccellenza Tecnica e Design Patterns

Il progetto implementa diversi pattern software avanzati per garantire manutenibilità e scalabilità:

*   **Observer Pattern**: Implementato nel `VaultService` per notificare automaticamente la UI quando i dati cambiano, eliminando la logica di refresh manuale.
*   **Factory Pattern**: Utilizzato per le strategie di crittografia, permettendo al sistema di passare tra diversi algoritmi (es. AES, ChaCha20) a runtime.
*   **Strategy Pattern**: Definisce algoritmi di crittografia intercambiabili incapsulati all'interno della factory.
*   **Builder Pattern**: Utilizzato per la creazione di oggetti di dominio complessi come `User` mantenendo l'immutabilità.
*   **Gestione Globale delle Eccezioni**: Un meccanismo centralizzato cattura gli errori a runtime e le eccezioni del database, presentandoli all'utente tramite dialoghi JavaFX chiari invece di semplici stack trace.

## Tecnologie Utilizzate

*   **Core**: Java 17, Spring Boot 3.x
*   **UI**: JavaFX 17 (FXML, CSS)
*   **Persistenza**: Spring Data JPA, Hibernate
*   **Database**: H2 (Embedded per portabilità)
*   **Sicurezza**: BCrypt (Hashing), AES-256 (Crittografia), Jackson (Serializzazione JSON sicura)
*   **Validazione**: JSR 380 (Bean Validation pronto all'uso)
*   **Build Tool**: Maven

## Requisiti di Sistema

*   **Java Development Kit (JDK)**: Versione 17 o superiore.
*   **Maven**: Versione 3.6 o superiore.
*   **OS**: macOS, Windows o Linux.

## Installazione e Configurazione

### 1. Clonare il Repository
```bash
git clone https://github.com/DaniaCiampalini/SafeCoreProject.git
cd SafeCoreProject
```

### 2. Configurazione Dipendenze (Maven)
Assicurati che il tuo ambiente sia configurato per Java 17 e Maven. Installa tutte le dipendenze richieste:
```bash
mvn clean install
```

### 3. Database
L'applicazione è configurata di default per utilizzare **H2** in modalità file. Il file del database (`safecore_db.mv.db`) verrà creato automaticamente nella cartella del progetto alla prima esecuzione.

## Come Avviare l'Applicazione

### Opzione A: Tramite IntelliJ IDEA (Consigliato)
1.  Apri il progetto in IntelliJ.
2.  Attendi il caricamento delle dipendenze Maven.
3.  Trova la classe `com.safecore.SafeCoreApplication`.
4.  Fai clic con il tasto destro sulla classe e seleziona **Run**.

### Opzione B: Tramite Linea di Comando
```bash
mvn spring-boot:run
```

## Architettura del Progetto

SafeCore segue una **Clean Layered Architecture**:

*   **UI Layer** (`com.safecore.ui`): Controller JavaFX, styling CSS e layout FXML.
*   **Business Layer** (`com.safecore.business`): Servizi contenenti la logica core (Vault, Audit, SafeSend, ecc.).
*   **Security Layer** (`com.safecore.security`): Strategie di crittografia, utility di hashing e gestione delle chiavi.
*   **Persistence Layer** (`com.safecore.persistence`): Entità JPA e Repository per l'accesso ai dati.

## Dettagli sulla Sicurezza

*   **Zero Knowledge**: Le password del vault vengono cifrate localmente prima di essere salvate. SafeCore non memorizza mai la tua master password o le credenziali del vault in chiaro.
*   **Integrità del Dominio**: Una validazione robusta garantisce che nessun dato non valido o pericoloso entri nel sistema.
*   **Gestione della Sessione**: Un `SessionContext` centralizzato gestisce in modo sicuro l'identità dell'utente loggato tra le diverse scene della UI.

## Testing

SafeCore mantiene un'elevata qualità del codice attraverso test completi:
*   **Unit Testing**: Test isolati dei servizi utilizzando **Mockito**.
*   **Integration Testing**: Verifica dell'intero flusso (Registrazione -> Login -> Vault) utilizzando `@SpringBootTest`.

Per avviare i test:
```bash
mvn test
```

