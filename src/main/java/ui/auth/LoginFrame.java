package ui.auth;

import auth.session.SessionManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import service.AuthService;
import ui.admin.AdminDashboard;
import ui.instructor.InstructorDashboard;
import ui.student.StudentDashboard;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private final AuthService authService = new AuthService(); // to verify login
    private int failedAttempts = 0; // Counter for failed login attempts
    private long lockEndTime = 0; //timestamp when lock ends

    public LoginFrame() {
        setTitle("University ERP - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 243, 248));// soft gray

        //main panel
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel card = new RoundedPanel(30);
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(430, 470));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(35, 45, 35, 45));

        // header title
        JLabel title = new JLabel("Sign-in to your ERP", SwingConstants.CENTER);//title
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        JLabel subtitle = new JLabel(
                "<html><center>Please login using your assigned credentials.<br>Only specific users can access this portal.</center></html>",
                SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(110, 110, 110));
        subtitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 25, 0));

        // username label and field
        JLabel userLabel = new JLabel("Username");//username
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField = new JTextField();
        styleInput(usernameField);

        // password label and field
        JLabel passLabel = new JLabel("Password");//password
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField = new JPasswordField();
        styleInput(passwordField);

        //login button
        JButton loginBtn = new JButton("Login");//login button
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setPreferredSize(new Dimension(250, 45));
        loginBtn.setMaximumSize(new Dimension(250, 45));
        loginBtn.setBackground(new Color(55, 70, 115));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginBtn.addActionListener(this::handleLogin);


        card.add(title);
        card.add(subtitle);

        card.add(userLabel);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(15));

        card.add(passLabel);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(25));

        card.add(loginBtn);

        wrapper.add(card);
        add(wrapper, BorderLayout.CENTER);
    }

    private void styleInput(JTextField field) {
        field.setPreferredSize(new Dimension(320, 40));
        field.setMaximumSize(new Dimension(320, 40));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    // handels login verification, lock mechanism and error messages
    private void handleLogin(ActionEvent e) {

        long now = System.currentTimeMillis();

        if (now < lockEndTime) {//check if locked
            long secondsLeft = (lockEndTime - now) / 1000;
            JOptionPane.showMessageDialog(
                    this,
                    "Too many failed attempts!\nPlease wait " + secondsLeft + " seconds.",
                    "Locked",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        //fetch input values
        String uname = usernameField.getText().trim();
        String pwd = new String(passwordField.getPassword());

        //input validation
        if (uname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int result = authService.login(uname, pwd);

            switch (result) {
                case 1 -> {
                    failedAttempts = 0; // reset
                    this.dispose();
                    openDashboard(SessionManager.getCurrentRole());
                }
                case 0, -1 -> {
                    failedAttempts++;

                    if (failedAttempts >= 5) {
                        lockEndTime = System.currentTimeMillis() + 30_000; // 30 seconds lock
                        JOptionPane.showMessageDialog(
                                this,
                                "Too many failed attempts!\nLogin locked for 30 seconds.",
                                "Locked",
                                JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "Invalid username or password.\nAttempts left: " + (5 - failedAttempts),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
                default ->
                    JOptionPane.showMessageDialog(this, "Unknown error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // open dashboard based on role
    private void openDashboard(String role) {
        switch (role) {
            case "Admin" -> new AdminDashboard().setVisible(true);
            case "Instructor" -> new InstructorDashboard().setVisible(true);
            case "Student" -> new StudentDashboard().setVisible(true);
            default -> JOptionPane.showMessageDialog(this, "Unknown role!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }
}
