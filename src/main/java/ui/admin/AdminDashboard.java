package ui.admin;

import java.awt.*;
import javax.swing.*;

import auth.session.SessionManager;
import service.AdminService;
import ui.common.ChangePasswordDialog;
import ui.common.NotificationPanel;
import ui.admin.CreateNotificationFrame;

public class AdminDashboard extends JFrame {

    private JPanel contentPanel;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // HEADER
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(55, 70, 115));

        JLabel header = new JLabel("University ERP - Admin Panel", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        headerPanel.add(header, BorderLayout.CENTER);

        // TOP RIGHT NOTIFICATION BUTTON
        JButton notifBtn = new JButton("🔔");
        notifBtn.setPreferredSize(new Dimension(40, 40));
        notifBtn.setFocusPainted(false);
        notifBtn.setBorderPainted(false);
        notifBtn.setBackground(new Color(255, 255, 255, 0));
        notifBtn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        notifBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        notifBtn.addActionListener(
                e -> new NotificationPanel(
                        SessionManager.getCurrentUserId(),
                        SessionManager.getCurrentRole()
                ).setVisible(true)
        );

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(notifBtn);

        headerPanel.add(rightPanel, BorderLayout.EAST);

        // MAINTENANCE BANNER
        data.SettingsDAO settingsDAO = new data.SettingsDAO();
        boolean maintenance = settingsDAO.isMaintenanceOn();
        if (maintenance) {
            JLabel maintenanceBanner = new JLabel(
                    "Maintenance Mode: Editing disabled",
                    SwingConstants.CENTER
            );
            maintenanceBanner.setOpaque(true);
            maintenanceBanner.setBackground(Color.YELLOW);
            maintenanceBanner.setForeground(Color.BLACK);
            maintenanceBanner.setFont(new Font("Segoe UI", Font.BOLD, 14));
            maintenanceBanner.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            headerPanel.add(maintenanceBanner, BorderLayout.SOUTH);
        }

        add(headerPanel, BorderLayout.NORTH);

        // SIDEBAR
        JPanel navPanel = new JPanel(new GridLayout(9, 1, 0, 10)); // increased from 8 → 9
        navPanel.setPreferredSize(new Dimension(230, 0));
        navPanel.setBackground(new Color(240, 243, 248));
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton manageUsers = new JButton("Manage Users");
        JButton manageCourses = new JButton("Manage Courses");
        JButton manageSections = new JButton("Manage Sections");
        JButton assignInstructor = new JButton("Assign Instructor");
        JButton maintenanceBtn = new JButton("Maintenance Toggle");
        JButton backup = new JButton("Backup Database");
        JButton changePwdBtn = new JButton("Change Password");
        JButton logout = new JButton("Logout");

        // NEW: CREATE NOTIFICATION BUTTON
        JButton createNotifBtn = new JButton("Create Notification");

        JButton[] buttons = {
                manageUsers, manageCourses, manageSections,
                assignInstructor, maintenanceBtn, backup,
                createNotifBtn, changePwdBtn, logout
        };

        // styling
        for (JButton btn : buttons) {
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            btn.setFocusPainted(false);
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(new Color(225, 235, 245));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(Color.WHITE);
                }
            });
        }

        // add to sidebar
        navPanel.add(manageUsers);
        navPanel.add(manageCourses);
        navPanel.add(manageSections);
        navPanel.add(assignInstructor);
        navPanel.add(maintenanceBtn);
        navPanel.add(backup);

        // ADD create notification button
        navPanel.add(createNotifBtn);

        navPanel.add(changePwdBtn);
        navPanel.add(logout);

        add(navPanel, BorderLayout.WEST);

        // MAIN CONTENT
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);

        JLabel welcomeText = new JLabel(
                "Welcome " + SessionManager.getCurrentUsername(),
                SwingConstants.CENTER
        );
        welcomeText.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        welcomeText.setForeground(new Color(60, 60, 60));

        welcomePanel.add(welcomeText, BorderLayout.CENTER);
        contentPanel.add(welcomePanel, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // BUTTON ACTIONS
        manageUsers.addActionListener(e -> setContent(new ManageUsersPanel()));
        manageCourses.addActionListener(e -> setContent(new ManageCoursesPanel()));
        manageSections.addActionListener(e -> setContent(new ManageSectionsPanel()));
        assignInstructor.addActionListener(e -> setContent(new AssignInstructorPanel()));

        maintenanceBtn.addActionListener(e -> {
            AdminService.toggleMaintenance();
            JOptionPane.showMessageDialog(this, "Maintenance mode toggled!");
            this.dispose();
            SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
        });

        // OPEN CREATE NOTIFICATION WINDOW
        createNotifBtn.addActionListener(
                e -> new CreateNotificationFrame().setVisible(true)
        );

        backup.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                AdminService.backupErpDatabase(chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Backup completed successfully!");
            }
        });

        changePwdBtn.addActionListener(e -> {
            new ChangePasswordDialog(SessionManager.getCurrentUserId()).setVisible(true);
        });

        logout.addActionListener(e -> {
            SessionManager.endSession();
            dispose();
            new ui.auth.LoginFrame().setVisible(true);
        });
    }

    // replace content
    private void setContent(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public static void main(String[] args) {
        SessionManager.startSession(1, "admin1", "Admin");
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}
