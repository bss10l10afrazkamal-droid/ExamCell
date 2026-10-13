// MainFrame.java - Swing Admin GUI for ExamCell
// Concepts: Swing UI (Exp 6), Multithreading (Exp 5), File I/O (Exp 3), Interface (Exp 3)

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class MainFrame extends JFrame implements Exportable {

    // Table models for each tab
    private DefaultTableModel subjectModel, classroomModel, facultyModel, slotModel, resultModel;
    private JButton generateBtn, exportBtn;
    private List<Allocation> currentSchedule;

    public MainFrame() {
        setTitle("ExamCell - Exam Timetable Scheduler");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // center on screen

        // Create tabbed layout
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Subjects", createSubjectPanel());
        tabs.addTab("Classrooms", createClassroomPanel());
        tabs.addTab("Faculty", createFacultyPanel());
        tabs.addTab("Time Slots", createTimeSlotPanel());
        tabs.addTab("Generate Schedule", createSchedulePanel());

        add(tabs);
    }

    // ==================== SUBJECT TAB ====================
    private JPanel createSubjectPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Table
        subjectModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Department", "Semester", "Students"}, 0);
        JTable table = new JTable(subjectModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Add form at bottom
        JPanel form = new JPanel();
        JTextField nameField = new JTextField(10);
        JTextField deptField = new JTextField(6);
        JTextField semField = new JTextField(3);
        JTextField studField = new JTextField(4);
        JButton addBtn = new JButton("Add");

        form.add(new JLabel("Name:"));      form.add(nameField);
        form.add(new JLabel("Dept:"));       form.add(deptField);
        form.add(new JLabel("Sem:"));        form.add(semField);
        form.add(new JLabel("Students:"));   form.add(studField);
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String dept = deptField.getText().trim();
                int sem = Integer.parseInt(semField.getText().trim());
                int students = Integer.parseInt(studField.getText().trim());

                if (name.isEmpty() || dept.isEmpty() || sem < 1 || students < 1) {
                    JOptionPane.showMessageDialog(this, "Enter a name and department, and use positive semester and student counts.");
                    return;
                }

                DatabaseHelper.addSubject(new Subject(0, name, dept, sem, students));
                refreshSubjects();

                // Clear fields
                nameField.setText("");
                deptField.setText("");
                semField.setText("");
                studField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Semester and Students must be numbers!");
            } catch (RuntimeException ex) {
                showDatabaseError(ex);
            }
        });

        panel.add(form, BorderLayout.SOUTH);
        refreshSubjects();
        return panel;
    }

    private void refreshSubjects() {
        subjectModel.setRowCount(0);
        for (Subject s : DatabaseHelper.getAllSubjects()) {
            subjectModel.addRow(new Object[]{
                s.getSubjectId(), s.getName(), s.getDepartment(),
                s.getSemester(), s.getEstimatedStudents()
            });
        }
    }

    // ==================== CLASSROOM TAB ====================
    private JPanel createClassroomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        classroomModel = new DefaultTableModel(new String[]{"ID", "Room Name", "Capacity"}, 0);
        JTable table = new JTable(classroomModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel();
        JTextField nameField = new JTextField(10);
        JTextField capField = new JTextField(5);
        JButton addBtn = new JButton("Add");

        form.add(new JLabel("Room Name:"));  form.add(nameField);
        form.add(new JLabel("Capacity:"));   form.add(capField);
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                int cap = Integer.parseInt(capField.getText().trim());

                if (name.isEmpty() || cap < 1) {
                    JOptionPane.showMessageDialog(this, "Enter a room name and a positive capacity.");
                    return;
                }

                DatabaseHelper.addClassroom(new Classroom(0, name, cap));
                refreshClassrooms();
                nameField.setText("");
                capField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Capacity must be a number!");
            } catch (RuntimeException ex) {
                showDatabaseError(ex);
            }
        });

        panel.add(form, BorderLayout.SOUTH);
        refreshClassrooms();
        return panel;
    }

    private void refreshClassrooms() {
        classroomModel.setRowCount(0);
        for (Classroom c : DatabaseHelper.getAllClassrooms()) {
            classroomModel.addRow(new Object[]{c.getRoomId(), c.getRoomName(), c.getCapacity()});
        }
    }

    // ==================== FACULTY TAB ====================
    private JPanel createFacultyPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        facultyModel = new DefaultTableModel(new String[]{"ID", "Name", "Department"}, 0);
        JTable table = new JTable(facultyModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel();
        JTextField nameField = new JTextField(10);
        JTextField deptField = new JTextField(8);
        JButton addBtn = new JButton("Add");

        form.add(new JLabel("Name:"));        form.add(nameField);
        form.add(new JLabel("Department:"));  form.add(deptField);
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String dept = deptField.getText().trim();

            if (name.isEmpty() || dept.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }

            try {
                DatabaseHelper.addFaculty(new Faculty(0, name, dept));
                refreshFaculty();
                nameField.setText("");
                deptField.setText("");
            } catch (RuntimeException ex) { showDatabaseError(ex); }
        });

        panel.add(form, BorderLayout.SOUTH);
        refreshFaculty();
        return panel;
    }

    private void refreshFaculty() {
        facultyModel.setRowCount(0);
        for (Faculty f : DatabaseHelper.getAllFaculty()) {
            facultyModel.addRow(new Object[]{f.getFacultyId(), f.getName(), f.getDepartment()});
        }
    }

    // ==================== TIME SLOT TAB ====================
    private JPanel createTimeSlotPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        slotModel = new DefaultTableModel(new String[]{"ID", "Date (yyyy-MM-dd)", "Start (HH:mm)", "End (HH:mm)"}, 0);
        panel.add(new JScrollPane(new JTable(slotModel)), BorderLayout.CENTER);
        JPanel form = new JPanel();
        JTextField date = new JTextField(10), start = new JTextField(5), end = new JTextField(5);
        JButton add = new JButton("Add Time Slot");
        form.add(new JLabel("Date:")); form.add(date); form.add(new JLabel("Start:")); form.add(start);
        form.add(new JLabel("End:")); form.add(end); form.add(add);
        add.addActionListener(e -> {
            try {
                java.time.LocalDate d = java.time.LocalDate.parse(date.getText().trim());
                java.time.LocalTime from = java.time.LocalTime.parse(start.getText().trim());
                java.time.LocalTime to = java.time.LocalTime.parse(end.getText().trim());
                if (!to.isAfter(from)) throw new IllegalArgumentException("End time must be after start time.");
                DatabaseHelper.addTimeSlot(new TimeSlot(0, d.toString(), from.toString(), to.toString()));
                refreshTimeSlots(); date.setText(""); start.setText(""); end.setText("");
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Use date yyyy-MM-dd and times HH:mm (24-hour), e.g. 2026-05-10, 09:00, 12:00.");
            } catch (RuntimeException ex) { showDatabaseError(ex); }
        });
        panel.add(form, BorderLayout.SOUTH);
        refreshTimeSlots();
        return panel;
    }

    private void refreshTimeSlots() {
        slotModel.setRowCount(0);
        for (TimeSlot s : DatabaseHelper.getAllTimeSlots())
            slotModel.addRow(new Object[]{s.getSlotId(), s.getExamDate(), s.getStartTime(), s.getEndTime()});
    }

    private void showDatabaseError(RuntimeException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "ExamCell error", JOptionPane.ERROR_MESSAGE);
    }

    // ==================== SCHEDULE TAB ====================
    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        resultModel = new DefaultTableModel(
            new String[]{"Subject", "Room", "Faculty", "Date", "Time"}, 0);
        JTable table = new JTable(resultModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel();
        generateBtn = new JButton("Generate Schedule");
        exportBtn = new JButton("Export to File");
        exportBtn.setEnabled(false);

        generateBtn.addActionListener(e -> generateSchedule());
        exportBtn.addActionListener(e -> {
            try {
                exportToFile("timetable.txt");
                JOptionPane.showMessageDialog(this, "Timetable exported to timetable.txt!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage());
            }
        });

        btnPanel.add(generateBtn);
        btnPanel.add(exportBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    // ==================== GENERATE SCHEDULE ====================
    // Uses multithreading to run algorithm in background (Exp 5)
    private void generateSchedule() {
        generateBtn.setEnabled(false);
        generateBtn.setText("Generating...");

        // Run scheduler on a background thread so UI doesn't freeze
        new Thread(() -> {
            try {
                // Load data from database
                List<Subject> subjects = DatabaseHelper.getAllSubjects();
                List<Classroom> rooms = DatabaseHelper.getAllClassrooms();
                List<Faculty> faculty = DatabaseHelper.getAllFaculty();
                List<TimeSlot> slots = DatabaseHelper.getAllTimeSlots();

                // Validate that we have enough data
                if (subjects.isEmpty() || rooms.isEmpty() ||
                    faculty.isEmpty() || slots.isEmpty()) {
                    throw new SchedulingException(
                        "Please add subjects, rooms, faculty, and time slots first!");
                }

                // Run backtracking scheduler
                BacktrackingScheduler scheduler =
                    new BacktrackingScheduler(subjects, rooms, faculty, slots);
                currentSchedule = scheduler.schedule();

                // Save to database
                DatabaseHelper.saveAllocations(currentSchedule);

                // Update UI on Swing thread
                SwingUtilities.invokeLater(() -> {
                    resultModel.setRowCount(0);
                    for (Allocation a : currentSchedule) {
                        resultModel.addRow(new Object[]{
                            a.getSubject().getName(),
                            a.getClassroom().getRoomName(),
                            a.getFaculty().getName(),
                            a.getTimeSlot().getExamDate(),
                            a.getTimeSlot().getStartTime() + "-" + a.getTimeSlot().getEndTime()
                        });
                    }
                    generateBtn.setText("Generate Schedule");
                    generateBtn.setEnabled(true);
                    exportBtn.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "Schedule generated successfully!");
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Could not generate schedule", JOptionPane.ERROR_MESSAGE);
                    generateBtn.setText("Generate Schedule");
                    generateBtn.setEnabled(true);
                });
            }
        }).start();
    }

    // ==================== FILE EXPORT (Exp 3 - File I/O) ====================
    // Implements Exportable interface
    @Override
    public void exportToFile(String filePath) throws Exception {
        if (currentSchedule == null || currentSchedule.isEmpty()) {
            throw new Exception("No schedule to export!");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

        writer.write("========================================");
        writer.newLine();
        writer.write("     EXAM TIMETABLE - ExamCell");
        writer.newLine();
        writer.write("========================================");
        writer.newLine();
        writer.newLine();

        // Write header
        writer.write(String.format("%-25s %-12s %-18s %-12s %-15s",
            "Subject", "Room", "Faculty", "Date", "Time"));
        writer.newLine();
        writer.write("--------------------------------------------------------------------------------");
        writer.newLine();

        // Write each allocation
        for (Allocation a : currentSchedule) {
            writer.write(String.format("%-25s %-12s %-18s %-12s %-15s",
                a.getSubject().getName(),
                a.getClassroom().getRoomName(),
                a.getFaculty().getName(),
                a.getTimeSlot().getExamDate(),
                a.getTimeSlot().getStartTime() + "-" + a.getTimeSlot().getEndTime()
            ));
            writer.newLine();
        }

        writer.newLine();
        writer.write("Generated by ExamCell Scheduler");
        }
    }
}
