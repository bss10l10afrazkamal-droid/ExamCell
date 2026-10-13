// JDBC access for ExamCell. Connection settings can be supplied through
// EXAMCELL_DB_URL, EXAMCELL_DB_USER and EXAMCELL_DB_PASSWORD.
import java.sql.*;
import java.util.*;

public final class DatabaseHelper {
    private static final String URL = setting("EXAMCELL_DB_URL", "jdbc:mysql://localhost:3306/examcell?serverTimezone=UTC");
    private static final String USER = setting("EXAMCELL_DB_USER", "root");
    private static final String PASSWORD = setting("EXAMCELL_DB_PASSWORD", "");

    private DatabaseHelper() { }

    private static String setting(String key, String fallback) {
        String value = System.getenv(key);
        return value == null ? fallback : value;
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static IllegalStateException databaseError(String action, SQLException cause) {
        return new IllegalStateException(action + ". Check that MySQL is running, the examcell schema exists, and EXAMCELL_DB_URL/USER/PASSWORD are correct. Details: " + cause.getMessage(), cause);
    }

    public static void addSubject(Subject s) {
        String sql = "INSERT INTO subjects (name, department, semester, estimated_students) VALUES (?, ?, ?, ?)";
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, s.getName()); p.setString(2, s.getDepartment());
            p.setInt(3, s.getSemester()); p.setInt(4, s.getEstimatedStudents()); p.executeUpdate();
        } catch (SQLException e) { throw databaseError("Could not add subject", e); }
    }

    public static List<Subject> getAllSubjects() {
        List<Subject> rows = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM subjects ORDER BY subject_id")) {
            while (r.next()) rows.add(new Subject(r.getInt("subject_id"), r.getString("name"), r.getString("department"), r.getInt("semester"), r.getInt("estimated_students")));
            return rows;
        } catch (SQLException e) { throw databaseError("Could not load subjects", e); }
    }

    public static void addClassroom(Classroom room) {
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement("INSERT INTO classrooms (room_name, capacity) VALUES (?, ?)")) {
            p.setString(1, room.getRoomName()); p.setInt(2, room.getCapacity()); p.executeUpdate();
        } catch (SQLException e) { throw databaseError("Could not add classroom", e); }
    }

    public static List<Classroom> getAllClassrooms() {
        List<Classroom> rows = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM classrooms ORDER BY room_id")) {
            while (r.next()) rows.add(new Classroom(r.getInt("room_id"), r.getString("room_name"), r.getInt("capacity")));
            return rows;
        } catch (SQLException e) { throw databaseError("Could not load classrooms", e); }
    }

    public static void addFaculty(Faculty faculty) {
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement("INSERT INTO faculty (name, department) VALUES (?, ?)")) {
            p.setString(1, faculty.getName()); p.setString(2, faculty.getDepartment()); p.executeUpdate();
        } catch (SQLException e) { throw databaseError("Could not add faculty", e); }
    }

    public static List<Faculty> getAllFaculty() {
        List<Faculty> rows = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM faculty ORDER BY faculty_id")) {
            while (r.next()) rows.add(new Faculty(r.getInt("faculty_id"), r.getString("name"), r.getString("department")));
            return rows;
        } catch (SQLException e) { throw databaseError("Could not load faculty", e); }
    }

    public static void addTimeSlot(TimeSlot slot) {
        try (Connection c = getConnection(); PreparedStatement p = c.prepareStatement("INSERT INTO time_slots (exam_date, start_time, end_time) VALUES (?, ?, ?)")) {
            p.setString(1, slot.getExamDate()); p.setString(2, slot.getStartTime()); p.setString(3, slot.getEndTime()); p.executeUpdate();
        } catch (SQLException e) { throw databaseError("Could not add time slot", e); }
    }

    public static List<TimeSlot> getAllTimeSlots() {
        List<TimeSlot> rows = new ArrayList<>();
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery("SELECT * FROM time_slots ORDER BY exam_date, start_time")) {
            while (r.next()) rows.add(new TimeSlot(r.getInt("slot_id"), r.getString("exam_date"), r.getString("start_time"), r.getString("end_time")));
            return rows;
        } catch (SQLException e) { throw databaseError("Could not load time slots", e); }
    }

    // Replace a schedule atomically: a failed insert leaves the previous one intact.
    public static void saveAllocations(List<Allocation> allocations) {
        String insert = "INSERT INTO allocations (subject_id, room_id, faculty_id, slot_id) VALUES (?, ?, ?, ?)";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (Statement clear = c.createStatement(); PreparedStatement p = c.prepareStatement(insert)) {
                clear.executeUpdate("DELETE FROM allocations");
                for (Allocation a : allocations) {
                    p.setInt(1, a.getSubject().getSubjectId()); p.setInt(2, a.getClassroom().getRoomId());
                    p.setInt(3, a.getFaculty().getFacultyId()); p.setInt(4, a.getTimeSlot().getSlotId()); p.addBatch();
                }
                p.executeBatch(); c.commit();
            } catch (SQLException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw databaseError("Could not save the timetable", e); }
    }

    public static List<String[]> getTimetable() {
        List<String[]> rows = new ArrayList<>();
        String sql = "SELECT s.name AS subject_name, s.department, s.semester, c.room_name, f.name AS faculty_name, t.exam_date, t.start_time, t.end_time " +
                     "FROM allocations a JOIN subjects s ON a.subject_id=s.subject_id JOIN classrooms c ON a.room_id=c.room_id " +
                     "JOIN faculty f ON a.faculty_id=f.faculty_id JOIN time_slots t ON a.slot_id=t.slot_id ORDER BY t.exam_date,t.start_time,s.department,s.semester";
        try (Connection c = getConnection(); Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            while (r.next()) rows.add(new String[]{r.getString("subject_name"), r.getString("department") + " Sem-" + r.getInt("semester"), r.getString("room_name"), r.getString("faculty_name"), r.getString("exam_date"), r.getString("start_time") + " - " + r.getString("end_time")});
            return rows;
        } catch (SQLException e) { throw databaseError("Could not load the timetable", e); }
    }
}
