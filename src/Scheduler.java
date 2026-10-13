// Scheduler.java - Abstract base class for scheduling algorithms
// Concepts: Abstract Class (Exp 3), Custom Exception (Exp 5)

import java.util.*;

// Abstract class - defines what any scheduler must do
public abstract class Scheduler {
    protected List<Subject> subjects;
    protected List<Classroom> rooms;
    protected List<Faculty> faculty;
    protected List<TimeSlot> slots;

    public Scheduler(List<Subject> subjects, List<Classroom> rooms,
                     List<Faculty> faculty, List<TimeSlot> slots) {
        this.subjects = subjects;
        this.rooms = rooms;
        this.faculty = faculty;
        this.slots = slots;
    }

    // Abstract method - subclass must implement this
    public abstract List<Allocation> schedule() throws SchedulingException;
}

// Custom exception for scheduling errors (Exp 5 - Exception Handling)
class SchedulingException extends Exception {
    public SchedulingException(String message) {
        super(message);
    }
}
