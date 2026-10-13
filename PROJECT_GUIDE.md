# ExamCell Project Guide

## 1. What ExamCell is

ExamCell is an examination timetable builder for a college. An administrator maintains lists of subjects, rooms, faculty, and exam time slots in a desktop application. The scheduler tries to assign every subject a room, an invigilating faculty member, and a time. If it finds a valid timetable, it saves the assignments in MySQL. An optional web page reads and displays that saved timetable.

The project schedules whole subject batches. It does not create seat plans or assign individual students.

## 2. Feynman explanation

Imagine each subject is a card that must be placed into a calendar. Each card needs three things: a time, a room, and a supervisor. The calendar has rules:

1. The room must fit all the students for that subject.
2. A room cannot host two exams whose times overlap.
3. A faculty member cannot supervise two exams whose times overlap.
4. Two subjects for the same department and semester cannot have overlapping exams.

The program puts down one card at a time. When a choice breaks a rule, it removes that choice and tries a different one. This is called **backtracking**: try a choice, follow its consequences, and undo it if it leads to a dead end. If every subject gets a valid place, the program saves the completed calendar. If it cannot find a combination, it reports that more resources or time slots may be needed.

In this analogy, MySQL is the shared notebook, the desktop app is the administrator's control panel, and the web page is a read-only timetable board.

## 3. How the parts fit together

```text
Administrator
    |
    v
Java Swing desktop app (Main / MainFrame)
    |  JDBC: read resources, add resources, save schedule
    v
MySQL database (examcell)
    ^
    |  JDBC: read saved schedule
Java Servlet on Apache Tomcat 9 ---> JSP timetable page in a browser
```

The desktop and web components are separate ways to use the same database. The web part is optional; the desktop scheduler can be used without Tomcat.

### Desktop flow

1. `Main` loads the MySQL JDBC driver and opens the Swing window.
2. `MainFrame` loads database records into the Subjects, Classrooms, Faculty, and Time Slots tabs.
3. The administrator can add records. The forms validate basic values and store each new record in MySQL.
4. **Generate Schedule** loads the four lists and runs the scheduler on a background thread so the UI can still respond.
5. A complete result replaces the prior saved schedule in one database transaction. If saving fails, the transaction rolls back.
6. **Export to File** writes the schedule currently shown by the app to `timetable.txt` in the working directory.

### Web flow

1. A browser requests `/examcell/timetable` from Tomcat.
2. `ViewTimetableServlet` asks `DatabaseHelper` for the saved assignments.
3. The servlet forwards the rows to `timetable.jsp`, which displays them as an HTML table. Displayed values are escaped before being inserted into HTML.
4. If there is no saved schedule, the page explains that one must be generated in the desktop app first.

## 4. Scheduling algorithm and rules

`BacktrackingScheduler` extends the abstract `Scheduler` class. It walks through subjects in list order. For each subject it tries every available time slot, room, and faculty member. A candidate is accepted only if it is valid against the assignments already made. It then recursively attempts the next subject. When a later subject cannot be placed, it removes the last assignment and continues trying alternatives.

Time conflicts are based on actual dates and intervals, not just matching time-slot IDs. For example, 09:00–12:00 conflicts with 11:00–13:00 on the same date. Intervals that only touch at an endpoint do not overlap (09:00–10:00 and 10:00–11:00 are allowed to be adjacent).

The search can become slow as the number of subjects, rooms, faculty, and slots grows, because it explores combinations. This is a small academic scheduler rather than a large-scale optimization service.

## 5. Data stored in MySQL

The schema is in `sql/schema.sql` and creates the `examcell` database:

| Table | Stores |
|---|---|
| `subjects` | Subject name, department, semester, estimated student count |
| `classrooms` | Room name and capacity |
| `faculty` | Faculty name and department |
| `time_slots` | Exam date, start time, and end time |
| `allocations` | One subject-to-room/faculty/time assignment per row |

`allocations` uses foreign keys back to the four resource tables. It also prevents a subject from appearing twice and prevents the same room or faculty member from being allocated to the same slot ID twice. The Java scheduler adds the stronger check for overlapping time intervals, including overlaps between distinct slot rows.

The schema inserts demonstration data as well as creating tables. Run it only once on a fresh database. Running it again will insert the demo records again.

## 6. Source code map

| File | Responsibility |
|---|---|
| `src/Main.java` | Loads the JDBC driver and starts Swing on the event-dispatch thread |
| `src/MainFrame.java` | Desktop tabs, input validation, scheduling action, result display, text export |
| `src/Models.java` | `Subject`, `Classroom`, `Faculty`, `TimeSlot`, `Allocation`, and `Exportable` |
| `src/Scheduler.java` | Abstract scheduling contract and `SchedulingException` |
| `src/BacktrackingScheduler.java` | Backtracking search and scheduling constraint checks |
| `src/DatabaseHelper.java` | JDBC connection, resource queries/inserts, schedule replacement, timetable query |
| `src/ViewTimetableServlet.java` | HTTP endpoint that obtains timetable rows and forwards to JSP |
| `web/WEB-INF/web.xml` | Servlet mapping and web application configuration |
| `web/WEB-INF/timetable.jsp` | HTML timetable rendering |
| `web/index.jsp` | Redirects the web app's root to `/timetable` |
| `sql/schema.sql` | Database tables, constraints, and demo rows |
| `scripts/` | PowerShell build, desktop run, and web deployment scripts |

## 7. Requirements on this Windows computer

The project was set up with:

- Windows 10/11 and PowerShell.
- Eclipse Temurin JDK 17 at `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`.
- MySQL Server 8.0, Windows service `MySQL80`.
- MySQL Connector/J at `lib\mysql-connector-j-9.6.0.jar`.
- For the optional website: Apache Tomcat 9.0.123 in `tools\apache-tomcat-9.0.123`.

Use Tomcat 9 because this project imports `javax.servlet`. Tomcat 10 uses `jakarta.servlet` and requires code changes.

## 8. Run the desktop app

Open PowerShell and run these commands from the folder containing this guide (`C:\Users\HP\Desktop\MPJ\MPJ_1\MPJ_1`). Replace the password placeholder with the current password for the `examcell_app` MySQL account. The environment variables apply only to this PowerShell window and programs started from it.

```powershell
cd 'C:\Users\HP\Desktop\MPJ\MPJ_1\MPJ_1'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:EXAMCELL_DB_URL = 'jdbc:mysql://localhost:3306/examcell?serverTimezone=UTC'
$env:EXAMCELL_DB_USER = 'examcell_app'
$env:EXAMCELL_DB_PASSWORD = 'YOUR_EXAMCELL_APP_PASSWORD'
Get-Service MySQL80
powershell -ExecutionPolicy Bypass -File .\scripts\run-admin.ps1
```

If `Get-Service` reports that MySQL is stopped, start it from an Administrator PowerShell window with `Start-Service MySQL80`, then rerun the application command. The run script compiles the Java code and starts the Swing window. `ExecutionPolicy Bypass` applies to that PowerShell invocation; it does not change the machine-wide execution policy.

### Use the desktop app

1. Confirm the four resource tabs contain the data you intend to schedule. The starter database has demonstration rows.
2. Add subjects with name, department, semester, and estimated students; classrooms with name and capacity; faculty with name and department; and time slots.
3. Enter dates as `yyyy-MM-dd` and times as 24-hour `HH:mm`, for example `2026-11-10`, `09:00`, `12:00`.
4. Open **Generate Schedule** and select **Generate Schedule**.
5. If a solution exists, review the table. Use **Export to File** to create `timetable.txt` in the project folder.

The application currently supports adding these records and generating schedules. It does not have GUI controls to edit or delete existing records.

## 9. Set up the database on a fresh computer

1. Install and start MySQL Server 8.0. Confirm the MySQL command-line client is installed, or run the SQL in MySQL Workbench.
2. From the project folder, execute `sql/schema.sql` once using an administrator database account. In MySQL Workbench, open the file and click the lightning-bolt execute button. The script creates the schema and sample data.
3. In a privileged MySQL session, create a dedicated app user and grant access to this schema. Choose a private password of your own:

   ```sql
   CREATE USER 'examcell_app'@'localhost' IDENTIFIED BY 'choose-a-private-password';
   GRANT SELECT, INSERT, UPDATE, DELETE ON examcell.* TO 'examcell_app'@'localhost';
   ```

4. Use that username and password in the environment variables from section 8. Avoid using the MySQL root account for the application.

Do not rerun `schema.sql` on an existing populated database: its sample inserts are not guarded against duplicates.

## 10. Optional: deploy the web timetable

First set the Java and database environment variables as described in section 8 in the same PowerShell window. Then run:

```powershell
$env:CATALINA_HOME = (Resolve-Path '.\tools\apache-tomcat-9.0.123').Path
powershell -ExecutionPolicy Bypass -File .\scripts\deploy-web.ps1 -TomcatHome $env:CATALINA_HOME
& "$env:CATALINA_HOME\bin\startup.bat"
```

Once Tomcat has started, visit `http://localhost:8080/examcell/timetable`. Generate a schedule in the desktop app first so there are rows to display. To stop Tomcat:

```powershell
& "$env:CATALINA_HOME\bin\shutdown.bat"
```

The deployment script compiles the servlet and packages the web application into `build\examcell.war`, then copies it into Tomcat's `webapps` folder. Start Tomcat from the same PowerShell window that has the database variables so the server process receives them.

## 11. Current limitations

- A subject is assigned to one room; a large batch is not split across rooms.
- A department and semester identify a batch for conflict checking.
- Faculty availability is modeled only as one person cannot supervise overlapping exams. There are no personal availability calendars.
- There are no student accounts, authentication, authorization, or individual seat assignments.
- The web page displays the saved schedule; it does not provide editing or scheduling controls.
- The search prioritizes finding a valid schedule and does not optimize for preferences such as minimizing exam days or balancing faculty workload.

