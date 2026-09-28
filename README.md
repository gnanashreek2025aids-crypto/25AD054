# ContractWatch – Contract Renewal Reminder Tracker

ContractWatch is a beginner-friendly Spring Boot project designed for small organizations to store vendor and service contracts, track start and end dates, set notice periods, automatically identify contracts entering their renewal window, renew or terminate contracts, and monitor contracts expiring within the next 30 days.

---

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot (Spring Web, Spring Data JPA, MySQL Connector/J, Bean Validation)
- **Frontend**: Clean HTML5, Vanilla CSS3, Modern JavaScript (`fetch` API) — *No React, Angular, Bootstrap, or external libraries*
- **Database**: MySQL

---

## 📁 Project Structure

```text
CONTRACTWATCH/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── example/
    │   │           └── contractwatch/
    │   │               ├── ContractWatchApplication.java
    │   │               ├── controller/
    │   │               │   └── ContractController.java
    │   │               ├── service/
    │   │               │   └── ContractService.java
    │   │               ├── repository/
    │   │               │   └── ContractRepository.java
    │   │               └── model/
    │   │                   └── Contract.java
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── index.html
    │           ├── style.css
    │           └── script.js
    └── test/
        └── java/
            └── com/
                └── example/
                    └── contractwatch/
                        └── ContractWatchApplicationTests.java
```

---

## 🗄️ Database Setup (MySQL)

### Step 1: Create the Database
Open **MySQL Workbench** or your MySQL command line client and execute:

```sql
CREATE DATABASE contractwatch;
```

> **Note:** You do **not** need to create the table manually! Spring Data JPA with `spring.jpa.hibernate.ddl-auto=update` will automatically create the `contracts` table upon starting the application.

---

## ⚙️ Configuration (`application.properties`)

Open `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/contractwatch
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
server.port=8080
```

### How to change the MySQL Password:
Replace `YOUR_MYSQL_PASSWORD` with your actual local MySQL root password. For example:
```properties
spring.datasource.password=root1234
```

---

## 🚀 How to Run the Spring Boot Application

### Option 1: Using Maven Wrapper (Terminal / Command Prompt / PowerShell)
From the project root directory, run:

```bash
# On Windows PowerShell or Command Prompt:
.\mvnw.cmd spring-boot:run

# Or on Mac/Linux:
./mvnw spring-boot:run
```

### Option 2: Using an IDE (IntelliJ IDEA / Eclipse / VS Code)
1. Open the project folder in your IDE.
2. Locate `src/main/java/com/example/contractwatch/ContractWatchApplication.java`.
3. Right-click and choose **Run 'ContractWatchApplication'**.

---

## 🌐 How to Access the Web Application

Once the Spring Boot application starts up, open your web browser and navigate to:

👉 **[http://localhost:8080](http://localhost:8080)**

The frontend is served directly by the Spring Boot server from `src/main/resources/static/index.html`.

---

## 🔍 How to Verify Data in MySQL Workbench

After adding, editing, renewing, or terminating contracts from the web interface, open MySQL Workbench and run:

```sql
USE contractwatch;
SELECT * FROM contracts;
```

You will see all records saved with their corresponding `id`, `vendor`, `contract_name`, `start_date`, `end_date`, `notice_period`, `status`, and `new_end_date`.

---

## 💡 Business Logic: Renewal Reminder Calculation

The system automates contract tracking through the following business rules:

1. **Initial Status**: New contracts are assigned the status `ACTIVE` by default.
2. **Reminder Date Calculation**:
   $$\text{reminderDate} = \text{endDate} - \text{noticePeriod days}$$
3. **Renewal Due Rule**:
   - If **Today's Date** is on or after $\text{reminderDate}$ and on or before $\text{endDate}$, the contract status automatically changes to `RENEWAL_DUE`.
   - **Important Constraint**: A `TERMINATED` contract is **never** changed to `RENEWAL_DUE`.
4. **Renewal Rule**:
   - When renewing a contract, the user specifies a new end date (`newEndDate`).
   - The system updates `endDate` to `newEndDate`, saves `newEndDate`, and sets the status to `RENEWED`.
5. **Termination Rule**:
   - When a contract is terminated, its status is changed to `TERMINATED`.
6. **30-Day Expiry Window**:
   - The endpoint `GET /api/contracts/expiring` fetches contracts whose `endDate` falls between today and the next 30 days.

---

## 📡 REST API Endpoints & CRUD Operations

All REST endpoints are available under the base path `/api/contracts`:

| HTTP Method | Endpoint URL | Description |
| :--- | :--- | :--- |
| **POST** | `/api/contracts` | **Create**: Adds a new contract with JSON payload |
| **GET** | `/api/contracts` | **Read**: Retrieves all saved contracts |
| **GET** | `/api/contracts/{id}` | **Read**: Retrieves a single contract by ID |
| **GET** | `/api/contracts/expiring` | **Read**: Retrieves contracts expiring within the next 30 days |
| **PUT** | `/api/contracts/{id}` | **Update**: Updates an existing contract's information |
| **PUT** | `/api/contracts/{id}/renew?newEndDate=YYYY-MM-DD` | **Renew**: Renews the contract with a new end date |
| **PUT** | `/api/contracts/{id}/terminate` | **Terminate**: Sets the contract status to `TERMINATED` |
| **DELETE** | `/api/contracts/{id}` | **Delete**: Deletes a contract by ID |

---

## 🧪 Sample JSON Payload for Testing (POST /api/contracts)

```json
{
  "vendor": "Acme Cloud Services",
  "contractName": "Enterprise Cloud Hosting",
  "startDate": "2026-01-01",
  "endDate": "2026-10-15",
  "noticePeriod": 30
}
```
