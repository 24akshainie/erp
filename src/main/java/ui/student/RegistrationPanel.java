package ui.student;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import access.AccessControl;
import auth.session.SessionManager;
import data.DBConnection;
import data.EnrollmentDAO;
import data.SectionDAO;
import domain.Enrollment;
import domain.Section;

public class RegistrationPanel extends JPanel {

    private JTable sectionTable;
    private DefaultTableModel model;

    public RegistrationPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // -Header
        JLabel header = new JLabel("Course Sections Available", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(35, 55, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 5));
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {
                "Section ID", "Course ID", "Instructor ID",
                "Day/Time", "Room", "Enrolled/Capacity",
                "Semester", "Year", "Deadline"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        sectionTable = new JTable(model);
        styleTable(sectionTable);

        JScrollPane scroll = new JScrollPane(sectionTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton registerBtn = styledButton("Register");
        JButton dropBtn = styledButton("Drop Course");
        JButton refreshBtn = styledButton("Refresh");

        registerBtn.addActionListener(e -> {
            try { registerCourse(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        dropBtn.addActionListener(e -> dropCourse());
        refreshBtn.addActionListener(e -> refreshSectionList());

        btnPanel.add(registerBtn);
        btnPanel.add(dropBtn);
        btnPanel.add(refreshBtn);

        add(btnPanel, BorderLayout.SOUTH);

        refreshSectionList();
    }

    // Table styling
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.setShowGrid(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(55,70,115));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {

                Component c = super.getTableCellRendererComponent(
                        t, val, isSelected, hasFocus, row, col
                );

                Color normal1 = new Color(250, 250, 250);
                Color normal2 = Color.WHITE;

                if (isSelected) {
                    c.setBackground(new Color(200, 220, 240));
                } else {
                    // grey out deadlined passed
                    Object deadlineObj = model.getValueAt(row, 8);
                    if (deadlineObj != null) {
                        Date deadline = (Date) deadlineObj;
                        Date today = new Date(System.currentTimeMillis());

                        if (deadline.before(today)) {
                            c.setBackground(new Color(210, 210, 210)); 
                            return c;
                        }
                    }

                    c.setBackground((row % 2 == 0) ? normal1 : normal2);
                }

                return c;
            }
        });
    }

    // button styling
    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(50, 50, 50));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190)),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        return btn;
    }

    private void refreshSectionList() {
        model.setRowCount(0);

        List<Section> list = new SectionDAO().getAllSections();

        if (list == null || list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No sections available!");
            return;
        }

        try (Connection conn = DBConnection.getErpConnection()) {

            for (Section s : list) {

                // Get enrolled count
                int enrolledCount = new SectionDAO().getEnrolledCount(s.getId());

                // Fetch deadline
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT registration_deadline FROM courses WHERE code = ?"
                );
                ps.setString(1, s.getCourseId());
                ResultSet rs = ps.executeQuery();

                Date deadline = null;
                if (rs.next()) {
                    deadline = rs.getDate("registration_deadline");
                }

                model.addRow(new Object[]{
                        s.getId(),
                        s.getCourseId(),
                        s.getInstructorId(),
                        s.getDayTime(),
                        s.getRoom(),
                        enrolledCount + "/" + s.getCapacity(),
                        s.getSemester(),
                        s.getYear(),
                        deadline  
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading deadlines.");
        }
    }

    // Register for a course if deadline and seats left
    private void registerCourse() throws Exception {

        int row = sectionTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a section first.");
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);
        String courseId = (String) model.getValueAt(row, 1);
        int studentId = SessionManager.getCurrentUserId();

        // deadline check
        Date deadline = (Date) model.getValueAt(row, 8);
        Date today = new Date(System.currentTimeMillis());

        if (deadline != null && today.after(deadline)) {
            JOptionPane.showMessageDialog(this,
                    "Registration deadline for this course has passed!\nDeadline: " + deadline);
            return;
        }

        // capacity check
        String enrolledCap = (String) model.getValueAt(row, 5); 
        String[] parts = enrolledCap.split("/");
        int enrolled = Integer.parseInt(parts[0]);
        int capacity = Integer.parseInt(parts[1]);

        if (enrolled >= capacity) {
            JOptionPane.showMessageDialog(this, "Section is full!");
            return;
        }

        Enrollment e = new Enrollment(studentId, sectionId, "ENROLLED");
        boolean ok = new EnrollmentDAO().register(e);

        if (ok) {
            JOptionPane.showMessageDialog(this, "Registered successfully!");
            refreshSectionList();
        } else {
            JOptionPane.showMessageDialog(this, "Already registered or failed.");
        }
    }

    
    private void dropCourse() {
        if (!AccessControl.isAllowed(SessionManager.getCurrentRole(), "drop")) {
            JOptionPane.showMessageDialog(this, "Not allowed or system under maintenance.");
            return;
        }

        String input = JOptionPane.showInputDialog(this, "Enter Section ID to drop:");
        if (input == null || input.isBlank()) return;

        try {
            int sectionId = Integer.parseInt(input.trim());
            int studentId = SessionManager.getCurrentUserId();

            boolean ok = new EnrollmentDAO().drop(studentId, sectionId);
            if (ok)
                JOptionPane.showMessageDialog(this, "Dropped successfully!");
            else
                JOptionPane.showMessageDialog(this, "Drop failed or not enrolled.");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Section ID.");
        }
    }
}
