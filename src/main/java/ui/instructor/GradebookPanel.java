package ui.instructor;

import auth.session.SessionManager;
import data.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class GradebookPanel extends JPanel {

    private JComboBox<String> sectionSelector;
    private JTable gradeTable;
    private DefaultTableModel tableModel;

    public GradebookPanel() {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // header
        JLabel header = new JLabel("Gradebook – Enter Student Marks", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(35, 55, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 5));
        add(header, BorderLayout.NORTH);

        // filters panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel lbl = new JLabel("Select Section:");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        sectionSelector = new JComboBox<>();
        sectionSelector.setPreferredSize(new Dimension(220, 32));
        sectionSelector.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadInstructorSections();

        JButton loadBtn = styledButton("Load Students");
        loadBtn.addActionListener(e -> loadStudents());

        topPanel.add(lbl);
        topPanel.add(sectionSelector);
        topPanel.add(loadBtn);
        add(topPanel, BorderLayout.BEFORE_FIRST_LINE);

        // table
        String[] cols = {
                "Enrollment ID", "User ID", "Roll No", "Program", "Year",
                "Quiz", "Midterm", "Final", "Final Grade"
        };

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return col >= 5 && col <= 7;
            }
        };

        gradeTable = new JTable(tableModel);
        styleTable(gradeTable);

        JScrollPane scroll = new JScrollPane(gradeTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // buttons 
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JButton saveBtn = styledButton("Save Marks");
        JButton computeBtn = styledButton("Compute Final Grades");

        saveBtn.addActionListener(e -> saveMarks());
        computeBtn.addActionListener(e -> computeFinalGrades());

        bottomPanel.add(saveBtn);
        bottomPanel.add(computeBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    //table styling
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

                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);

                if (isSelected) {
                    c.setBackground(new Color(200, 220, 240));
                } else if (row % 2 == 0) {
                    c.setBackground(new Color(250, 250, 250));
                } else {
                    c.setBackground(Color.WHITE);
                }

                setBorder(noFocusBorder);
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

        // hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(225, 233, 245));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
        });

        return btn;
    }

    // Load sections of a specific instructor from db 
    private void loadInstructorSections() {
        sectionSelector.removeAllItems();
        int instructorId = SessionManager.getCurrentUserId();

        String sql = "SELECT id, course_id FROM sections WHERE instructor_id=?";

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                sectionSelector.addItem(rs.getInt("id") + " - " + rs.getString("course_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // load students of a section from db
 private void loadStudents() {

    tableModel.setRowCount(0);
    if (sectionSelector.getSelectedItem() == null) return;

    int sectionId = Integer.parseInt(sectionSelector.getSelectedItem().toString().split(" - ")[0]);

  String sql = """
    SELECT 
        e.id AS enrollment_id, 
        s.user_id, s.roll_no, s.program, s.year,
        MAX(CASE WHEN g.component='Quiz' THEN g.score END) AS quiz,
        MAX(CASE WHEN g.component='Midterm' THEN g.score END) AS midterm,
        MAX(CASE WHEN g.component='Final' THEN g.score END) AS final,
        MAX(g.final_grade) AS final_grade
    FROM enrollments e
    JOIN students s ON e.student_id = s.user_id
    LEFT JOIN grades g ON e.id = g.enrollment_id
    WHERE e.section_id = ?
    GROUP BY e.id, s.user_id, s.roll_no, s.program, s.year
""";


    try (Connection conn = DBConnection.getErpConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, sectionId);   // ✔ FIX ADDED

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            tableModel.addRow(new Object[]{
                    rs.getInt("enrollment_id"),
                    rs.getInt("user_id"),
                    rs.getString("roll_no"),
                    rs.getString("program"),
                    rs.getInt("year"),
                    rs.getObject("Quiz"),
                    rs.getObject("Midterm"),
                    rs.getObject("Final"),
                    rs.getString("final_grade")
            });
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}



    private void saveMarks() {

        if (sectionSelector.getSelectedItem() == null) return;

        try (Connection conn = DBConnection.getErpConnection()) {

            String sql = """
                INSERT INTO grades(enrollment_id, component, score)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE score=VALUES(score)
            """;

            PreparedStatement ps = conn.prepareStatement(sql);

            String[] components = {"Quiz", "Midterm", "Final"};

            for (int i = 0; i < tableModel.getRowCount(); i++) {

                int enrollmentId = (int) tableModel.getValueAt(i, 0);

                for (int j = 0; j < 3; j++) {
                    Object val = tableModel.getValueAt(i, 5 + j);

                    if (val != null) {
                        ps.setInt(1, enrollmentId);
                        ps.setString(2, components[j]);
                        ps.setDouble(3, Double.parseDouble(val.toString()));
                        ps.addBatch();
                    }
                }
            }

            ps.executeBatch();
            JOptionPane.showMessageDialog(this, "Marks saved successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void computeFinalGrades() {

        if (sectionSelector.getSelectedItem() == null) return;

        try (Connection conn = DBConnection.getErpConnection()) {

            String updateSql = "UPDATE grades SET final_grade=? WHERE enrollment_id=?";
            PreparedStatement ps = conn.prepareStatement(updateSql);

            for (int i = 0; i < tableModel.getRowCount(); i++) {

                int enrollmentId = (int) tableModel.getValueAt(i, 0);

                Double quiz = toDouble(tableModel.getValueAt(i, 5));
                Double mid = toDouble(tableModel.getValueAt(i, 6));
                Double fin = toDouble(tableModel.getValueAt(i, 7));

                if (quiz != null && mid != null && fin != null) {

                    double finalScore =
                            quiz * 0.2 +
                            mid * 0.3 +
                            fin * 0.5;

                    String grade;
                    if (finalScore >= 90) grade = "A";
                    else if (finalScore >= 80) grade = "B";
                    else if (finalScore >= 70) grade = "C";
                    else if (finalScore >= 60) grade = "D";
                    else grade = "F";

                    ps.setString(1, grade);
                    ps.setInt(2, enrollmentId);
                    ps.addBatch();

                    tableModel.setValueAt(grade, i, 8);
                }
            }

            ps.executeBatch();
            JOptionPane.showMessageDialog(this, "Final grades computed!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Double toDouble(Object val) {
        if (val == null) return null;
        try { return Double.parseDouble(val.toString()); }
        catch (Exception e) { return null; }
    }
}