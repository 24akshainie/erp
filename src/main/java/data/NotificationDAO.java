package data;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import domain.Notification;
import data.DBConnection;

public class NotificationDAO {

    // INSERT a new notification
    public static boolean createNotification(Notification n) {
        String sql = "INSERT INTO notifications (admin_id, target_role, target_user_id, message) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, n.getAdminId());
            ps.setString(2, n.getTargetRole());
            
            if (n.getTargetUserId() == null)
                ps.setNull(3, java.sql.Types.INTEGER);
            else
                ps.setInt(3, n.getTargetUserId());

            ps.setString(4, n.getMessage());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // FETCH notifications for any user
    public static List<Notification> getNotifications(int userId, String role) {
        List<Notification> list = new ArrayList<>();

        String sql = "SELECT * FROM notifications "
                   + "WHERE target_role = 'ALL' "
                   + "OR target_role = ? "
                   + "OR target_user_id = ? "
                   + "ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role.toUpperCase());
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Notification n = new Notification();
                n.setId(rs.getInt("id"));
                n.setAdminId(rs.getInt("admin_id"));
                n.setTargetRole(rs.getString("target_role"));
                n.setTargetUserId(rs.getObject("target_user_id") != null ? rs.getInt("target_user_id") : null);
                n.setMessage(rs.getString("message"));
                n.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
