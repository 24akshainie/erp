package ui.common;

import ui.admin.AdminDashboard;
import ui.student.StudentDashboard;
import ui.instructor.InstructorDashboard;
import auth.session.SessionManager;

import javax.swing.*;

public class MainDashboard extends JFrame {

    public MainDashboard() {
        setTitle("ERP System");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        loadDashboard();
    }

    // load dashboard according to the role
    private void loadDashboard() {
        String role = SessionManager.getCurrentRole();

        if (role == null) {
            JOptionPane.showMessageDialog(this, 
                "Error: No user logged in.");
            dispose();
            return;
        }

        switch (role.toLowerCase()) {

            case "admin":
                new AdminDashboard().setVisible(true);
                break;

            case "student":
                new StudentDashboard().setVisible(true);
                break;

            case "instructor":
                new InstructorDashboard().setVisible(true);
                break;

            default:
                JOptionPane.showMessageDialog(this, 
                    "Unknown role: " + role);
                break;
        }

        dispose(); // close the main dashboard after loading role-specific screen
    }
}
