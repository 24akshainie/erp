package auth.store;

import java.sql.*;

import auth.hash.PasswordUtil;
import data.DBConnection;

public class AuthDAO {

    // Validates login credentials.
    // Returns user_id if successful, 0 if user not found/inactive, -1 if password is incorrect.
    public int validateLogin(String username, String plainPassword) throws Exception {
        String sql = "SELECT user_id, password_hash FROM users_auth WHERE username=? AND status='ACTIVE'";

        try (Connection conn = DBConnection.getAuthConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                return 0; // user not found/inactive
            }

            //Get stored user id and hashed password
            int userId = rs.getInt("user_id");
            String hash = rs.getString("password_hash");

            // Verify entered password with hashed password
            if (PasswordUtil.verify(plainPassword, hash)) {
                return userId; // successgul login
            } else {
                return -1; // incorrect password
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception("Database error during login: " + e.getMessage());
        }
    }

    // Get role of user from db, using their userId
    public String getRoleByUserId(int userId) throws Exception {
        String sql = "SELECT role FROM users_auth WHERE user_id=?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Create a new user in the users_auth table (used by Admin)
    public boolean createUser(int userId, String username, String role, String passwordHash) throws Exception {
        String insertUserSql =
                "INSERT INTO users_auth(user_id, username, role, password_hash, status, last_login) " +
                "VALUES (?, ?, ?, ?, 'ACTIVE', NOW())";

        try (Connection conn = DBConnection.getAuthConnection()) {

            // Insert new user record
            try (PreparedStatement ps = conn.prepareStatement(insertUserSql)) {
                ps.setInt(1, userId);
                ps.setString(2, username);
                ps.setString(3, role);
                ps.setString(4, passwordHash);
                ps.executeUpdate();
            }
            return true;  //user created successfully

        } catch (SQLException e) {
            e.printStackTrace();
            return false;  
        }
    }

}
