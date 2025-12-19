package ui.admin;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import data.DBConnection;

public class AssignInstructorPanel extends JPanel {

    private JComboBox<String> courseMenu; // Dropdown for courses without instructors
    private JComboBox<String> instructorMenu; // Dropdown for available instructors

    public AssignInstructorPanel() {

        // Main Layout
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 252)); // same tone as login

        JPanel card = new RoundedPanel(25);
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(450, 350));
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Panel Title
        JLabel title = new JLabel("Assign Instructor to Course", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(45, 60, 90));

        // Dropdowns
        courseMenu = new JComboBox<>();
        instructorMenu = new JComboBox<>();
        styleCombo(courseMenu);
        styleCombo(instructorMenu);

        // Load data from db
        loadUnassignedCourses();
        loadInstructors();

        JButton assignBtn = new JButton("Assign");
        stylePrimaryButton(assignBtn);
        assignBtn.addActionListener(e -> {
            try {
                assignInstructor();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        card.add(new JLabel("Course:"), gbc);

        gbc.gridx = 1;
        card.add(courseMenu, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Instructor:"), gbc);

        gbc.gridx = 1;
        card.add(instructorMenu, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        card.add(assignBtn, gbc);

        add(card);
    }

    private void styleCombo(JComboBox<String> box) {
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                new EmptyBorder(6, 8, 6, 8)));
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(new Color(55, 70, 115));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(72, 92, 140));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(55, 70, 115));
            }
        });
    }

    // Loads courses that currently have no assigned instructor.
    private void loadUnassignedCourses() {
        courseMenu.removeAllItems();

        String sql = """
                SELECT s.id AS section_id, c.code, c.title
                FROM sections s
                JOIN courses c ON c.code = s.course_id
                WHERE s.instructor_id IS NULL;
                """;

        try (Connection conn = DBConnection.getErpConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            // Show “No instructor selected” as the first option
            courseMenu.addItem(null);

            while (rs.next()) {
                String item = rs.getInt("section_id") + " - " +
                        rs.getString("code") + " - " +
                        rs.getString("title");
                courseMenu.addItem(item);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading unassigned sections!");
        }
    }

    // Loads all instructors into dropdown.
    private void loadInstructors() {
        instructorMenu.removeAllItems();
        String sql = "SELECT user_id, department FROM instructors";
        try (Connection conn = DBConnection.getErpConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                // Only show user_id and department (no 'name' column)
                instructorMenu.addItem(rs.getInt("user_id") + " - " + rs.getString("department"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading instructors!");
        }
    }

    // Assigns selected instructor to the selected course.
    private void assignInstructor() {
        String selectedSection = (String) courseMenu.getSelectedItem();
        if (selectedSection == null || selectedSection.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a section!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedInstructor = (String) instructorMenu.getSelectedItem();
        if (selectedInstructor == null || selectedInstructor.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an instructor!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // parse section id (expected "12 - CS101 - Intro to Programming")
        final int sectionId;
        try {
            sectionId = Integer.parseInt(selectedSection.split(" - ")[0].trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid section selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // parse instructor id (expected "2 - Computer Science")
        final int instructorId;
        try {
            instructorId = Integer.parseInt(selectedInstructor.split(" - ")[0].trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid instructor selected.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE sections SET instructor_id = ? WHERE id = ?";

        try (Connection conn = DBConnection.getErpConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, instructorId);
            ps.setInt(2, sectionId);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                JOptionPane.showMessageDialog(this,
                        "✔ Instructor assigned to section " + sectionId + " successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadUnassignedCourses(); // refresh dropdown so this section disappears
            } else {
                JOptionPane.showMessageDialog(this,
                        "No update performed. Section might already have an instructor.",
                        "Warning", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Unexpected error: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Rounded Card Component
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
