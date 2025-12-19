package ui.student;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.FlowLayout;
import java.awt.Cursor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import auth.session.SessionManager;

public class StudentDashboard extends JFrame {

    private JPanel contentPanel; // Right dynamic panel

    public StudentDashboard() {
        setTitle("Student Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(55,70,115));

        JLabel header = new JLabel("University ERP - Student Portal", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setOpaque(false);
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        headerPanel.add(header, BorderLayout.CENTER);

        // ------------ TOP RIGHT NOTIFICATION BUTTON (Added) -------------
        JButton notifBtn = new JButton("🔔");
        notifBtn.setPreferredSize(new Dimension(40, 40));
        notifBtn.setFocusPainted(false);
        notifBtn.setBorderPainted(false);
        notifBtn.setBackground(new Color(255,255,255,0)); 
        notifBtn.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        notifBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        notifBtn.addActionListener(e ->
            new ui.common.NotificationPanel(
                SessionManager.getCurrentUserId(),
                SessionManager.getCurrentRole()
            ).setVisible(true)
        );

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightPanel.setOpaque(false);
        rightPanel.add(notifBtn);

        headerPanel.add(rightPanel, BorderLayout.EAST);
        // -----------------------------------------------------------------

        // Maintenance banner
        data.SettingsDAO settingsDAO = new data.SettingsDAO();
        boolean maintenance = settingsDAO.isMaintenanceOn();
        if (maintenance) {
            JLabel maintenanceBanner = new JLabel("Maintenance Mode: Editing disabled", SwingConstants.CENTER);
            maintenanceBanner.setOpaque(true);
            maintenanceBanner.setBackground(Color.YELLOW);
            maintenanceBanner.setForeground(Color.BLACK);
            maintenanceBanner.setFont(new Font("Segoe UI", Font.BOLD, 14));
            maintenanceBanner.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            headerPanel.add(maintenanceBanner, BorderLayout.SOUTH);
        }

        add(headerPanel, BorderLayout.NORTH);

        // left sidebar menu
        JPanel sideMenu = new JPanel();
        sideMenu.setLayout(new GridLayout(9, 1, 0, 10));
        sideMenu.setPreferredSize(new Dimension(230, 0));
        sideMenu.setBackground(new Color(245, 248, 250));
        sideMenu.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        JButton viewCatalog = styledButton("View Course Catalog");
        JButton myRegistrations = styledButton("My Registrations");
        JButton viewTimetable = styledButton("View Timetable");
        JButton viewGrades = styledButton("View Grades");
        JButton transcript = styledButton("Download Transcript");
        JButton changePassBtn = styledButton("Change Password");
        JButton logout = styledButton("Logout");

        if (maintenance) {
            myRegistrations.setEnabled(false);
        }

        JButton[] buttons = {
            viewCatalog, myRegistrations, viewTimetable,
            viewGrades, transcript, changePassBtn, logout
        };

        for (JButton btn : buttons) sideMenu.add(btn);
        add(sideMenu, BorderLayout.WEST);

        // right content panel
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);

        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        JLabel welcomeText = new JLabel("Welcome " + SessionManager.getCurrentUsername(), SwingConstants.CENTER);
        welcomeText.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        welcomeText.setForeground(new Color(60, 60, 60));
        welcomePanel.add(welcomeText, BorderLayout.CENTER);

        contentPanel.add(welcomePanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        // Button actions
        viewCatalog.addActionListener(e -> setContent(new ViewCatalogPanel()));

        myRegistrations.addActionListener(e -> {
            if (maintenance) {
                JOptionPane.showMessageDialog(this,
                        "Cannot register/drop courses during maintenance mode!");
                return;
            }
            setContent(new RegistrationPanel());
        });

        viewTimetable.addActionListener(e -> setContent(new TimeTablePanel()));
        viewGrades.addActionListener(e -> setContent(new ViewGradesPanel()));
        transcript.addActionListener(e -> new TranscriptFrame().setVisible(true));

        changePassBtn.addActionListener(e -> {
            new ui.common.ChangePasswordDialog(SessionManager.getCurrentUserId()).setVisible(true);
        });

        logout.addActionListener(e -> {
            SessionManager.endSession();
            dispose();
            new ui.auth.LoginFrame().setVisible(true);
        });
    }

    // change right panel
    private void setContent(JPanel panel) {
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // button styling
    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(50, 50, 50));
        btn.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(225, 235, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    public static void main(String[] args) {
        SessionManager.startSession(3, "student1", "Student");
        SwingUtilities.invokeLater(() -> new StudentDashboard().setVisible(true));
    }
}
