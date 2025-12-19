package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import access.AccessControl;
import auth.hash.PasswordUtil;
import auth.store.AuthDAO;
import data.*;
import domain.*;
import util.DatabaseBackupUtil;

public class AdminService {

    // Add new user
    public static void addUser(int userId, String username, String role, String password) throws Exception {
        if (!AccessControl.isAllowed("Admin", "add_user"))
            return;
        String hash = PasswordUtil.hashPassword(password);
        new AuthDAO().createUser(userId, username, role, hash);
    }

    // Add new course
    public static void addCourse(String code, String title, int credits) throws Exception {
        if (!AccessControl.isAllowed("Admin", "add_course"))
            return;
        new CourseDAO().addCourse(new Course(code, title, credits));
    }

    // Add new section
    public static void addSection(String courseId, int instructorId, String dayTime,
            String room, int capacity, String semester, int year) {
        if (!AccessControl.isAllowed("Admin", "add_section"))
            return;
        new SectionDAO().addSection(courseId, instructorId, dayTime, room, capacity, semester, year);
    }

    // Assign instructor to section
    public static void assignInstructor(int sectionId, int instructorId) {
        if (!AccessControl.isAllowed("Admin", "assign_instructor"))
            return;
        new SectionDAO().assignInstructor(sectionId, instructorId);
    }

    // Toggle maintenance mode (flip ON/OFF)
    public static void toggleMaintenance() {
        if (!AccessControl.isAllowed("Admin", "toggle_maintenance"))
            return;
        new SettingsDAO().toggleMaintenance(); // flips ON ↔ OFF
    }

    // Explicit toggle (force ON or OFF)
    public static void toggleMaintenance(boolean on) {
        if (!AccessControl.isAllowed("Admin", "toggle_maintenance"))
            return;
        new SettingsDAO().toggleMaintenance(on);
    }

    // Backup ERP database
    public static void backupErpDatabase(String saveDir) {
        if (!AccessControl.isAllowed("Admin", "backup_db")) {
            System.out.println("Not authorized to back up database!");
            return;
        }

        long start = System.currentTimeMillis();
        System.out.println("Backup starting...");

        boolean success = DatabaseBackupUtil.backupErpDatabase("root", "", "erp_db", saveDir);

        long end = System.currentTimeMillis();
        System.out.println("Backup time: " + (end - start) + " ms");

        System.out.println(success ? "ERP Database backup successful!" : "ERP Database backup failed!");
    }

    public static boolean createNotification(Notification notification) throws Exception {
        String sql = "INSERT INTO notifications (admin_id, target_role, target_user_id, message) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getErpConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, notification.getAdminId()); // sender
            ps.setString(2, notification.getTargetRole()); // 'INSTRUCTOR', 'STUDENT', or 'ALL'
            if (notification.getTargetUserId() != null) {
                ps.setInt(3, notification.getTargetUserId()); // specific user
            } else {
                ps.setNull(3, java.sql.Types.INTEGER); // null if targeting all
            }
            ps.setString(4, notification.getMessage()); // message content

            int rows = ps.executeUpdate();
            return rows > 0; // true if insertion successful

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
