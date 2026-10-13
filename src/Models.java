// Models.java - All data model classes for ExamCell
// Concepts: Classes, Objects, Constructors, Encapsulation (Exp 2)
//           Method Overloading & Overriding (Exp 4), Interface (Exp 3)

import java.util.*;

// ============ Subject ============
class Subject {
    private int subjectId;
    private String name;
    private String department;
    private int semester;
    private int estimatedStudents;

    // Constructor
    public Subject(int subjectId, String name, String department, int semester, int estimatedStudents) {
        this.subjectId = subjectId;
        this.name = name;
        this.department = department;
        this.semester = semester;
        this.estimatedStudents = estimatedStudents;
    }

    // Overloaded constructor (Exp 4 - Method Overloading)
    public Subject(int subjectId, String name) {
        this.subjectId = subjectId;
        this.name = name;
        this.department = "General";
        this.semester = 1;
        this.estimatedStudents = 30;
    }

    // Getters
    public int getSubjectId() { return subjectId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public int getSemester() { return semester; }
    public int getEstimatedStudents() { return estimatedStudents; }

    // Overriding toString (Exp 4 - Method Overriding)
    @Override
    public String toString() {
        return name + " (" + department + " Sem-" + semester + ")";
    }
}

// ============ Classroom ============
class Classroom {
    private int roomId;
    private String roomName;
    private int capacity;

    public Classroom(int roomId, String roomName, int capacity) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.capacity = capacity;
    }

    public int getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public int getCapacity() { return capacity; }

    @Override
    public String toString() {
        return roomName + " (Cap: " + capacity + ")";
    }
}

// ============ Faculty ============
class Faculty {
    private int facultyId;
    private String name;
    private String department;

    public Faculty(int facultyId, String name, String department) {
        this.facultyId = facultyId;
        this.name = name;
        this.department = department;
    }

    public int getFacultyId() { return facultyId; }
    public String getName() { return name; }
    public String getDepartment() { return department; }

    @Override
    public String toString() {
        return name + " (" + department + ")";
    }
}

// ============ TimeSlot ============
class TimeSlot {
    private int slotId;
    private String examDate;
    private String startTime;
    private String endTime;

    public TimeSlot(int slotId, String examDate, String startTime, String endTime) {
        this.slotId = slotId;
        this.examDate = examDate;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getSlotId() { return slotId; }
    public String getExamDate() { return examDate; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }

    @Override
    public String toString() {
        return examDate + " " + startTime + "-" + endTime;
    }
}

// ============ Allocation (Result of Scheduling) ============
class Allocation {
    private Subject subject;
    private Classroom classroom;
    private Faculty faculty;
    private TimeSlot timeSlot;

    public Allocation(Subject subject, Classroom classroom, Faculty faculty, TimeSlot timeSlot) {
        this.subject = subject;
        this.classroom = classroom;
        this.faculty = faculty;
        this.timeSlot = timeSlot;
    }

    public Subject getSubject() { return subject; }
    public Classroom getClassroom() { return classroom; }
    public Faculty getFaculty() { return faculty; }
    public TimeSlot getTimeSlot() { return timeSlot; }

    @Override
    public String toString() {
        return subject.getName() + " | " + classroom.getRoomName() + " | " +
               faculty.getName() + " | " + timeSlot;
    }
}

// ============ Exportable Interface (Exp 3 - Interface) ============
interface Exportable {
    void exportToFile(String filePath) throws Exception;
}
