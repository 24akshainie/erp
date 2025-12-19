package data;

import java.sql.*;
import domain.Settings;

public class SettingsDAO {

    // Explicilty sets maintenance mode ON / OFF
    public boolean toggleMaintenance(boolean on) {
        String sql = "UPDATE settings SET value=? WHERE `key`='maintenance_mode'";
        try (Connection c = DBConnection.getErpConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, on ? "ON" : "OFF");
            ps.executeUpdate();
            return true; // if update was successful
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Dynamically flips current maintenance mode
    public boolean toggleMaintenance() {
        String query = "SELECT value FROM settings WHERE `key`='maintenance_mode'";
        String current = "OFF";
        try (Connection c = DBConnection.getErpConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery(query)) {
            if (rs.next())
                current = rs.getString("value");
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean newState = !"ON".equalsIgnoreCase(current);
        return toggleMaintenance(newState);
    }

    // Check if maintenance mode is currently ON
    // (used for resricting access)
    public boolean isMaintenanceOn() {
        String sql = "SELECT value FROM settings WHERE `key`='maintenance_mode'";
        try (Connection c = DBConnection.getErpConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("value").equalsIgnoreCase("ON");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Update a setting record in db
    public boolean saveOrUpdate(Settings s) {
        String sql = "REPLACE INTO settings(`key`, value) VALUES(?,?)";
        try (Connection c = DBConnection.getErpConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, s.getKey());
            ps.setString(2, s.getValue());
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Fetch setting record from db by its key
    // Fetch setting record from db by its key
    public Settings getSetting(String key) throws Exception {
        String sql = "SELECT `key`, value FROM settings WHERE `key` = ?";

        try (Connection conn = DBConnection.getErpConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Settings(
                            rs.getString("key"),
                            rs.getString("value"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
