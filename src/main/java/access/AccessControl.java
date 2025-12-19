package access;

import data.DBConnection;
import java.sql.*;

public class AccessControl {

    // Check if maintenance mode is ON (from settings table)
    public static boolean isMaintenanceOn() {
        String sql = "SELECT value FROM settings WHERE `key`='maintenance_mode'";
        try (Connection c = DBConnection.getErpConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                return "ON".equalsIgnoreCase(rs.getString("value"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    // Main permission checker to decide if a role is allowed to perform an action
    public static boolean isAllowed(String role, String action) {

        // If system is under maintenance and user is not Admin, deny access
        if (isMaintenanceOn() && !"Admin".equals(role)) {
            return false;
        }

        switch (role) {
            // permissions for Student
            case "Student":
                return switch (action) {
                    case "view_catalog", "register", "drop", "view_timetable", "view_grades", "csv_transcript", "view_registrations"-> true;
                    default -> false;
                };

            // permissions for Instructor
            case "Instructor":
                return switch (action) {
                    case "enter_scores", "view_gradebook", "compute_final" -> true;
                    default -> false;
                };

            // permissions for Admin
            case "Admin":
                return switch (action) {
                    case "add_user", "add_course", "add_section", "assign_instructor", "toggle_maintenance", "backup_db" -> true;
                    default -> false;
                };

            default:
                return false;
        }
    }
}
