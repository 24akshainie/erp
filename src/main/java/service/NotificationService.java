package service;

import data.DBConnection;
import java.sql.*;
import java.util.*;

public class NotificationService {

    // Admin: inserts a broadcast notification that is visible to all users
    public static void sendBroadcast(int senderId, String message) {
        String sql = "INSERT INTO notifications (sender_id, message) VALUES (?, ?)";
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setString(2, message);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Instructor : sends notification to all students of a specific section
    public static void sendToSection(int senderId, int sectionId, String message) {
        String sql = "INSERT INTO notifications (sender_id, section_id, message) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getErpConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, sectionId);
            ps.setString(3, message);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Returns notifications based on user role:
    // Admin → all, Instructor → broadcasts + direct, Student → broadcasts + section
    public static List<String> getNotificationsForUser(int userId, String role) {
    List<String> list = new ArrayList<>();

    String sql = "";

    switch (role) {

        case "Admin" -> {
            sql = """
                SELECT message 
                FROM notifications
                ORDER BY created_at DESC
            """;

            try (Connection c = DBConnection.getErpConnection();
                 PreparedStatement ps = c.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(rs.getString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        case "Instructor" -> {
            sql = """
                SELECT message
                FROM notifications
                WHERE 
                    (target_user_id IS NULL AND section_id IS NULL)
                    OR target_user_id = ?
                ORDER BY created_at DESC
            """;

            try (Connection c = DBConnection.getErpConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    list.add(rs.getString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }

        case "Student" -> {
            sql = """
                SELECT DISTINCT n.message
                FROM notifications n
                LEFT JOIN enrollments e 
                    ON n.section_id = e.section_id
                WHERE 
                    (n.target_user_id IS NULL AND n.section_id IS NULL)
                    OR e.student_id = ?
                ORDER BY n.created_at DESC
            """;

            try (Connection c = DBConnection.getErpConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    list.add(rs.getString("message"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return list;
        }
    }

    return list; // role didn't match
}

}
