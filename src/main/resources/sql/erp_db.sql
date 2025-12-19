CREATE DATABASE IF NOT EXISTS erpdb;
USE erpdb;

CREATE TABLE courses (
  code varchar(10) NOT NULL,
  title varchar(100) NOT NULL,
  credits int NOT NULL,
  instructor_id int DEFAULT NULL,
  registration_deadline date DEFAULT NULL,
  PRIMARY KEY (code),
  KEY instructor_id (instructor_id),
  CONSTRAINT courses_ibfk_1 FOREIGN KEY (instructor_id) REFERENCES instructors (user_id)
);

CREATE TABLE enrollments (
  id int NOT NULL AUTO_INCREMENT,
  student_id int NOT NULL,
  section_id int NOT NULL,
  status varchar(20) DEFAULT 'ENROLLED',
  enrolled_on datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY student_id (student_id,section_id),
  KEY section_id (section_id),
  CONSTRAINT enrollments_ibfk_1 FOREIGN KEY (student_id) REFERENCES students (user_id),
  CONSTRAINT enrollments_ibfk_2 FOREIGN KEY (section_id) REFERENCES sections (id)
);

CREATE TABLE grades (
  grade_id int NOT NULL AUTO_INCREMENT,
  enrollment_id int NOT NULL,
  component varchar(20) DEFAULT NULL,
  score decimal(5,2) DEFAULT NULL,
  final_grade varchar(2) DEFAULT NULL,
  PRIMARY KEY (grade_id),
  KEY enrollment_id (enrollment_id),
  CONSTRAINT grades_ibfk_1 FOREIGN KEY (enrollment_id) REFERENCES enrollments (id)
);

CREATE TABLE instructors (
  user_id int NOT NULL,
  department varchar(50) NOT NULL,
  PRIMARY KEY (user_id)
);

CREATE TABLE notifications (
  id int NOT NULL AUTO_INCREMENT,
  admin_id int NOT NULL,
  target_role enum('INSTRUCTOR','STUDENT','ALL') NOT NULL,
  target_user_id int DEFAULT NULL,
  message text NOT NULL,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE registrations (
  id int NOT NULL AUTO_INCREMENT,
  student_id int NOT NULL,
  section_id int NOT NULL,
  registered_on datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY section_id (section_id),
  CONSTRAINT registrations_ibfk_1 FOREIGN KEY (section_id) REFERENCES sections (id)
);

CREATE TABLE sections (
  id int NOT NULL AUTO_INCREMENT,
  course_id varchar(10) NOT NULL,
  instructor_id int DEFAULT NULL,
  day_time varchar(50) DEFAULT NULL,
  room varchar(20) DEFAULT NULL,
  capacity int DEFAULT NULL,
  semester varchar(20) DEFAULT NULL,
  year int DEFAULT NULL,
  current_enrolled int DEFAULT '0',
  PRIMARY KEY (id),
  KEY course_id (course_id),
  KEY instructor_id (instructor_id),
  CONSTRAINT sections_ibfk_1 FOREIGN KEY (course_id) REFERENCES courses (code),
  CONSTRAINT sections_ibfk_2 FOREIGN KEY (instructor_id) REFERENCES instructors (user_id)
);

CREATE TABLE settings (
  key varchar(50) NOT NULL,
  value varchar(100) DEFAULT NULL,
  PRIMARY KEY (key)
);

CREATE TABLE students (
  user_id int NOT NULL,
  roll_no varchar(20) DEFAULT NULL,
  program varchar(50) DEFAULT NULL,
  year int DEFAULT NULL,
  PRIMARY KEY (user_id),
  UNIQUE KEY roll_no (roll_no)
);

INSERT INTO students (user_id, roll_no, program, year) VALUES
(8, '2024376', 'CSAM', 2),
(12, '2024054', 'CSAI', 2);

INSERT INTO instructors (user_id, department) VALUES
(9, 'Computer Science');

INSERT INTO courses (code, title, credits, instructor_id, registration_deadline) VALUES
('CSE201', 'AP', 4, 9, '2025-12-13'),
('MTH100', 'M1', 4, 9, '2025-11-21');
