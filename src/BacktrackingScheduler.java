// BacktrackingScheduler.java - Solves exam scheduling using backtracking
// Concepts: Inheritance (Exp 3), Method Overriding (Exp 4)

import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class BacktrackingScheduler extends Scheduler {

    // Constructor - passes data to parent class
    public BacktrackingScheduler(List<Subject> subjects, List<Classroom> rooms,
                                 List<Faculty> faculty, List<TimeSlot> slots) {
        super(subjects, rooms, faculty, slots);
    }

    // Override abstract method from Scheduler (Exp 4 - Method Overriding)
    @Override
    public List<Allocation> schedule() throws SchedulingException {
        List<Allocation> result = new ArrayList<>();

        // Start backtracking from the first subject
        if (!solve(0, result)) {
            throw new SchedulingException("Cannot generate a conflict-free timetable! Try adding more time slots or rooms.");
        }

        return result;
    }

    // ---- Backtracking Algorithm ----
    // Tries to assign a slot, room, and faculty to each subject one by one.
    // If a conflict is found, it backtracks and tries a different combination.
    private boolean solve(int subjectIndex, List<Allocation> current) {

        // Base case: all subjects have been successfully scheduled
        if (subjectIndex >= subjects.size()) {
            return true;
        }

        Subject currentSubject = subjects.get(subjectIndex);

        // Try every combination of time slot, room, and faculty
        for (TimeSlot slot : slots) {
            for (Classroom room : rooms) {
                for (Faculty fac : faculty) {

                    // Check if this combination is valid (no conflicts)
                    if (isValid(currentSubject, slot, room, fac, current)) {

                        // Assign this combination
                        Allocation alloc = new Allocation(currentSubject, room, fac, slot);
                        current.add(alloc);

                        // Recurse: try to schedule the next subject
                        if (solve(subjectIndex + 1, current)) {
                            return true; // solution found!
                        }

                        // Backtrack: remove the assignment and try next combination
                        current.remove(current.size() - 1);
                    }
                }
            }
        }

        // No valid combination found for this subject
        return false;
    }

    // ---- Constraint Checking ----
    // Returns true if the given assignment does NOT cause any conflict
    private boolean isValid(Subject subject, TimeSlot slot, Classroom room,
                            Faculty fac, List<Allocation> current) {

        for (Allocation existing : current) {
            // Compare actual date/time intervals. Separate slot rows may overlap.
            if (overlaps(existing.getTimeSlot(), slot)) {

                // Constraint 1: Room is already booked in this slot
                if (existing.getClassroom().getRoomId() == room.getRoomId()) {
                    return false;
                }

                // Constraint 2: Faculty is already assigned in this slot
                if (existing.getFaculty().getFacultyId() == fac.getFacultyId()) {
                    return false;
                }

                // Constraint 3: Batch conflict - same dept + semester has exam in this slot
                if (existing.getSubject().getDepartment().trim().equalsIgnoreCase(subject.getDepartment().trim()) &&
                    existing.getSubject().getSemester() == subject.getSemester()) {
                    return false;
                }
            }
        }

        // Constraint 4: Room must have enough capacity for the students
        if (room.getCapacity() < subject.getEstimatedStudents()) {
            return false;
        }

        return true; // no conflicts
    }

    private boolean overlaps(TimeSlot first, TimeSlot second) {
        if (!LocalDate.parse(first.getExamDate()).equals(LocalDate.parse(second.getExamDate()))) return false;
        LocalTime firstStart = LocalTime.parse(first.getStartTime());
        LocalTime firstEnd = LocalTime.parse(first.getEndTime());
        LocalTime secondStart = LocalTime.parse(second.getStartTime());
        LocalTime secondEnd = LocalTime.parse(second.getEndTime());
        return firstStart.isBefore(secondEnd) && secondStart.isBefore(firstEnd);
    }
}
