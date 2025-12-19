CREATE DATABASE IF NOT EXISTS auth_db;

USE auth_db;

CREATE TABLE password_history (
  id int NOT NULL AUTO_INCREMENT,
  user_id int NOT NULL,
  old_hash varchar(255) NOT NULL,
  changed_on datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY user_id (user_id),
  CONSTRAINT password_history_ibfk_1 FOREIGN KEY (user_id) REFERENCES users_auth (user_id)
);

CREATE TABLE users_auth (
  user_id int NOT NULL,
  username varchar(50) NOT NULL,
  role enum('Admin','Instructor','Student') NOT NULL,
  password_hash varchar(255) NOT NULL,
  status varchar(20) DEFAULT NULL,
  last_login datetime DEFAULT NULL,
  PRIMARY KEY (user_id),
  UNIQUE KEY username (username)
);


--entering users in the table

--1	admin1	Admin	$2a$10$k6HlsIVRINPpFm1XihuG.efMX/Z5XS.pu6m3g5lVesIUkUmjoKdQm	ACTIVE	2025-11-18 14:27:00
--8	stu1	Student	$2a$10$0CyqRrsg4sIDgUQqNLAtke8bD21EW3OwVA6M/mMyMFcdo8rtGQiT6	ACTIVE	2025-11-20 23:34:02
--9	inst1	Instructor	$2a$10$jvr7mRi6KIqX12426mVfoeBvLdH5yMQDsDDzm0ZnFj6D3uSWphbum	ACTIVE	2025-11-21 00:33:50
--12	stu2	Student	$2a$10$6mPJ/i2JzyQx2ETMgM.LF.7Hesk9kJOjHnuzIFhAIIPvKNbQxh3gG	ACTIVE	2025-11-21 01:01:56
USE auth_db;

INSERT INTO users_auth (user_id, username, role, password_hash, status, last_login) VALUES
(1, 'admin1', 'Admin',
 '$2a$10$k6HlsIVRINPpFm1XihuG.efMX/Z5XS.pu6m3g5lVesIUkUmjoKdQm',
 'ACTIVE', '2025-11-18 14:27:00'),

(8, 'stu1', 'Student',
 '$2a$10$0CyqRrsg4sIDgUQqNLAtke8bD21EW3OwVA6M/mMyMFcdo8rtGQiT6',
 'ACTIVE', '2025-11-20 23:34:02'),

(9, 'inst1', 'Instructor',
 '$2a$10$jvr7mRi6KIqX12426mVfoeBvLdH5yMQDsDDzm0ZnFj6D3uSWphbum',
 'ACTIVE', '2025-11-21 00:33:50'),

(12, 'stu2', 'Student',
 '$2a$10$6mPJ/i2JzyQx2ETMgM.LF.7Hesk9kJOjHnuzIFhAIIPvKNbQxh3gG',
 'ACTIVE', '2025-11-21 01:01:56');
