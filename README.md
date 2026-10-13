# ExamCell

ExamCell is a small university examination resource scheduler. The office keeps a list of subjects, rooms, invigilating faculty, and exam time slots. The desktop app assigns one room, one faculty member, and one slot to each subject while checking the project constraints. The web app displays the saved timetable for students and faculty.

This manages block-level scheduling. It does not assign individual seats or student IDs.

## What the project does (Feynman explanation)

Imagine organizing exams as a puzzle. Every subject is a card that needs a date/time, a room, and a supervisor. The rules say:

* Two exams for the same department and semester cannot happen at the same time.
* A room cannot host two exams at the same time, and must fit the subject's estimated class size.
* A faculty member cannot supervise two exams at the same time.

The scheduler picks one subject and tries a combination. If all the rules fit, it keeps that choice and moves to the next subject. If a later subject gets stuck, it erases the last choice and tries another. That erase-and-try-again idea is called **backtracking**. Once every subject has a valid assignment, the app saves the result in MySQL.

The Swing window is the office's control panel. MySQL is the shared notebook. The servlet is a receptionist: when someone visits `/timetable`, it reads the saved rows and hands them to a JSP page that displays the table. The algorithm runs on a background thread so the Swing window remains usable while it searches.

## Requirements

* Windows 10/11 and PowerShell.
* **Installed on this computer:** Eclipse Temurin JDK 17 at `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot`; MySQL Server 8.0 (service `MySQL80`); Apache Tomcat 9.0.123 at `tools\apache-tomcat-9.0.123`.
* Tomcat 10 uses `jakarta.servlet` and is not compatible with this project's `javax.servlet` imports.
* The JDBC driver is included at `lib/mysql-connector-j-9.6.0.jar`.

## Exact setup and run steps (Windows)

1. Open PowerShell and go to the project directory (the directory containing this README):

   ```powershell
   cd 'C:\Users\HP\Desktop\MPJ\MPJ_1\MPJ_1'
   ```

2. Confirm Java tools are available:

   ```powershell
   java -version
   javac -version
   ```

   If `javac` is not recognized in a PowerShell window that was already open during installation, close and reopen PowerShell. The JDK installer added its `bin` directory to the machine `PATH`.

3. Start the **MySQL80** service from Windows Services (`services.msc`) if it is not already running. For this machine, the schema and a least-privilege app account have already been created. Configure that app account in this PowerShell window (use the password supplied when ExamCell was set up; change it if you rotate the account password):

   ```powershell
   $env:EXAMCELL_DB_URL = 'jdbc:mysql://localhost:3306/examcell?serverTimezone=UTC'
   $env:EXAMCELL_DB_USER = 'examcell_app'
   $env:EXAMCELL_DB_PASSWORD = 'YOUR_EXAMCELL_APP_PASSWORD'
   ```

   These are session-only settings and are read by both the desktop app and Tomcat if started from this same terminal. For a safer long-term setup, create a dedicated MySQL user and set that user's credentials here.

4. On a fresh installation only, create the database and sample rows once. Run this from the project directory; it prompts for the MySQL root password and executes the included schema:

   ```powershell
   cmd /c "mysql -u root -p < sql\schema.sql"
   ```

   The script creates the `examcell` database, tables, and demo records. It is intended for a fresh demo database; do not rerun it on an already populated database because its sample `INSERT`s will be repeated. Then create the app account in MySQL (replace the sample password with your own):

   ```sql
   CREATE USER 'examcell_app'@'localhost' IDENTIFIED BY 'choose-a-private-password';
   GRANT SELECT, INSERT, UPDATE, DELETE ON examcell.* TO 'examcell_app'@'localhost';
   ```

5. Run the admin app:

   ```powershell
   powershell -ExecutionPolicy Bypass -File .\scripts\run-admin.ps1
   ```

   In **Subjects**, **Classrooms**, and **Faculty**, add the records you need. In **Time Slots**, add slots using `yyyy-MM-dd` and 24-hour `HH:mm` (example: `2026-11-02`, `09:00`, `12:00`). Then choose **Generate Schedule**. If a valid schedule exists, it is saved in MySQL. **Export to File** writes `timetable.txt` in the project directory.

6. (Optional) Deploy the student/faculty timetable. In the *same PowerShell session* where the three database environment variables above are set, configure the installed Tomcat and deploy:

   ```powershell
   $env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
   $env:CATALINA_HOME = (Resolve-Path '.\tools\apache-tomcat-9.0.123').Path
   powershell -ExecutionPolicy Bypass -File .\scripts\deploy-web.ps1 -TomcatHome $env:CATALINA_HOME
   ```

   Start Tomcat from that same session so it inherits the database variables:

   ```powershell
   & "$env:CATALINA_HOME\bin\startup.bat"
   ```

   Open `http://localhost:8080/examcell/timetable`. The desktop app and the web app use the same MySQL database. To stop Tomcat, run `& "$env:CATALINA_HOME\bin\shutdown.bat"`.

## Constraints and limitations

* Department plus semester is treated as one batch.
* At most one exam can use a room or a faculty member in a time slot.
* A single subject is assigned one classroom; the scheduler does not split a batch across rooms.
* Faculty is considered unavailable for all other exams in the same slot. Per-person availability calendars are not modeled.
* The schedule is replaced as one database transaction after a complete solution is found. If no solution exists, the existing saved timetable is retained.
* This is a classroom miniproject, not a production system: there is no authentication, editing/deleting interface, or individual student seating.

## Where to look in the source

* `src/Models.java`: Subject, Classroom, Faculty, TimeSlot, Allocation.
* `src/Scheduler.java` and `src/BacktrackingScheduler.java`: scheduling abstraction, custom error, search and hard-constraint checks.
* `src/DatabaseHelper.java`: JDBC reads/writes and atomic schedule replacement.
* `src/MainFrame.java`: Swing admin UI, background scheduling, and text export.
* `src/ViewTimetableServlet.java`, `web/WEB-INF/web.xml`, `web/WEB-INF/timetable.jsp`: web endpoint and rendered timetable.
* `sql/schema.sql`: MySQL tables and demo data.
* `scripts/`: repeatable Windows build/run/deploy commands.
