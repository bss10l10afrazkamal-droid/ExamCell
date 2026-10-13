-- ============================================
-- ExamCell Database Schema
-- Run this file in MySQL before using the app
-- ============================================

CREATE DATABASE IF NOT EXISTS examcell;
USE examcell;

-- Subjects table
CREATE TABLE IF NOT EXISTS subjects (
    subject_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    semester INT NOT NULL,
    estimated_students INT NOT NULL
);

-- Classrooms table
CREATE TABLE IF NOT EXISTS classrooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_name VARCHAR(20) NOT NULL,
    capacity INT NOT NULL
);

-- Faculty table
CREATE TABLE IF NOT EXISTS faculty (
    faculty_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL
);

-- Time slots table
CREATE TABLE IF NOT EXISTS time_slots (
    slot_id INT PRIMARY KEY AUTO_INCREMENT,
    exam_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT chk_slot_time CHECK (end_time > start_time)
);

-- Allocations table (stores the generated schedule)
CREATE TABLE IF NOT EXISTS allocations (
    allocation_id INT PRIMARY KEY AUTO_INCREMENT,
    subject_id INT NOT NULL,
    room_id INT NOT NULL,
    faculty_id INT NOT NULL,
    slot_id INT NOT NULL,
    CONSTRAINT uq_allocation_subject UNIQUE (subject_id),
    CONSTRAINT uq_allocation_room_slot UNIQUE (room_id, slot_id),
    CONSTRAINT uq_allocation_faculty_slot UNIQUE (faculty_id, slot_id),
    FOREIGN KEY (subject_id) REFERENCES subjects(subject_id),
    FOREIGN KEY (room_id) REFERENCES classrooms(room_id),
    FOREIGN KEY (faculty_id) REFERENCES faculty(faculty_id),
    FOREIGN KEY (slot_id) REFERENCES time_slots(slot_id)
);


-- ============================================
-- DEMONSTRATION DATA (run this schema once on a fresh database)
-- ============================================

-- Subjects (mix of CS and IT departments)
INSERT INTO subjects (name, department, semester, estimated_students) VALUES
('Data Structures', 'CS', 6, 55),
('Operating Systems', 'CS', 6, 60),
('Database Systems', 'CS', 6, 50),
('Web Technology', 'IT', 6, 45),
('Digital Electronics', 'IT', 6, 50),
('Computer Networks', 'CS', 4, 55);

-- Classrooms
INSERT INTO classrooms (room_name, capacity) VALUES
('Room A101', 70),
('Room B202', 65),
('Room C303', 80);

-- Faculty
INSERT INTO faculty (name, department) VALUES
('Dr. Sharma', 'CS'),
('Prof. Patil', 'CS'),
('Dr. Jadhav', 'IT'),
('Prof. Gutte', 'CS');

-- Time Slots (4 slots across 2 days)
INSERT INTO time_slots (exam_date, start_time, end_time) VALUES
('2026-11-10', '09:00', '12:00'),
('2026-11-10', '14:00', '17:00'),
('2026-11-12', '09:00', '12:00'),
('2026-11-12', '14:00', '17:00');
