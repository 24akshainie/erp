package ui.common;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import data.DBConnection;   // your connection class
import org.mindrot.jbcrypt.BCrypt; // bcrypt

public class ChangePasswordDialog extends JDialog {

    private int userId;

    public ChangePasswordDialog(int userId) {
        this.userId = userId;

        setTitle("Change Password");
        setSize(350, 250);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new GridLayout(4, 2, 10, 10));

        JLabel oldPassLabel = new JLabel("Old Password:");
        JLabel newPassLabel = new JLabel("New Password:");
        JLabel confirmPassLabel = new JLabel("Confirm Password:");

        // input fields
        JPasswordField oldPassField = new JPasswordField();
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();

        //buttons
        JButton changeBtn = new JButton("Change");
        JButton cancelBtn = new JButton("Cancel");

        add(oldPassLabel); add(oldPassField);
        add(newPassLabel); add(newPassField);
        add(confirmPassLabel); add(confirmPassField);
        add(changeBtn); add(cancelBtn);

        // handle change button logic
        changeBtn.addActionListener(e -> {
            String oldPass = new String(oldPassField.getPassword());
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());

            // check for empty fields
            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields required!");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match!");
                return;
            }

            if (!verifyOldPassword(oldPass)) {
                JOptionPane.showMessageDialog(this, "Old password_hash is incorrect!");
                return;
            }

            // update password in db 
            if (updatePassword(newPass)) {
                JOptionPane.showMessageDialog(this, "Password updated successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error updating password_hash!");
            }
        });

        cancelBtn.addActionListener(e -> dispose());
    }

    // Verify the user's current password against the stored hash
    private boolean verifyOldPassword(String oldPass) {
        String sql = "SELECT password_hash FROM users_auth WHERE user_id = ?";

        try (Connection c = DBConnection.getAuthConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");

                // BCrypt verify
                return BCrypt.checkpw(oldPass, storedHash);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // hash new password and update in db
    private boolean updatePassword(String newPass) {
        String hashed = BCrypt.hashpw(newPass, BCrypt.gensalt());
        String sql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";

        try (Connection c = DBConnection.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, hashed);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}
