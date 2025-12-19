package ui.instructor;

import auth.session.SessionManager;
import data.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.sql.*;

public class CsvImportExportFrame extends JFrame {

    private JComboBox<String> sectionSelector;
    private JTable table;
    private DefaultTableModel model;

    public CsvImportExportFrame() {
        setTitle("Import / Export Grades (CSV)");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Import / Export Gradebook CSV", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(header, BorderLayout.NORTH);

        sectionSelector = new JComboBox<>();
        loadInstructorSections();
        sectionSelector.addActionListener(e -> loadGrades());

        JPanel top = new JPanel();
        top.add(new JLabel("Select Section:"));
        top.add(sectionSelector);
        add(top, BorderLayout.BEFORE_FIRST_LINE);

        String[] cols = {
                "Enrollment ID", "User ID", "Roll No", "Program", "Year",
                "Quiz", "Midterm", "Final", "Final Grade"
        };
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton importBtn = new JButton("Import CSV");
        JButton exportBtn = new JButton("Export CSV");
        JButton refreshBtn = new JButton("Refresh");

        importBtn.addActionListener(e -> importCSV());
        exportBtn.addActionListener(e -> exportCSV());
        refreshBtn.addActionListener(e -> loadGrades());

        JPanel btnPanel = new JPanel();
        btnPanel.add(importBtn);
        btnPanel.add(exportBtn);
        btnPanel.add(refreshBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    // load sections from db
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

    // load grades from db
    private void loadGrades() {
        model.setRowCount(0);
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

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("enrollment_id"),
                        rs.getInt("user_id"),
                        rs.getString("roll_no"),
                        rs.getString("program"),
                        rs.getInt("year"),
                        rs.getObject("quiz"),
                        rs.getObject("midterm"),
                        rs.getObject("final"),
                        rs.getString("final_grade")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportCSV() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.");
            return;
        }

        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Gradebook_Export.csv"));

            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                FileWriter writer = new FileWriter(chooser.getSelectedFile());

                // Write header
                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.write(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) writer.write(",");
                }
                writer.write("\n");

                // Write rows
                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        writer.write(String.valueOf(model.getValueAt(i, j)));
                        if (j < model.getColumnCount() - 1) writer.write(",");
                    }
                    writer.write("\n");
                }

                writer.close();
                JOptionPane.showMessageDialog(this, "CSV exported successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Export failed!");
        }
    }

    private void importCSV() {
        if (sectionSelector.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Select a section first!");
            return;
        }

        int sectionId = Integer.parseInt(sectionSelector.getSelectedItem().toString().split(" - ")[0]);

        try {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

            BufferedReader br = new BufferedReader(new FileReader(chooser.getSelectedFile()));
            br.readLine(); // Skip header

            int imported = 0;

            String sql = """
                INSERT INTO grades(enrollment_id, component, score)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE score=VALUES(score)
            """;

            Connection conn = DBConnection.getErpConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            String line;
            while ((line = br.readLine()) != null) {

                String[] p = line.split(",");
                if (p.length < 9) continue;

                int enrollmentId = Integer.parseInt(p[0].trim());
                Double quiz = safeNum(p[5]);
                Double mid = safeNum(p[6]);
                Double fin = safeNum(p[7]);

                if (quiz != null) {
                    ps.setInt(1, enrollmentId);
                    ps.setString(2, "Quiz");
                    ps.setDouble(3, quiz);
                    ps.addBatch();
                }
                if (mid != null) {
                    ps.setInt(1, enrollmentId);
                    ps.setString(2, "Midterm");
                    ps.setDouble(3, mid);
                    ps.addBatch();
                }
                if (fin != null) {
                    ps.setInt(1, enrollmentId);
                    ps.setString(2, "Final");
                    ps.setDouble(3, fin);
                    ps.addBatch();
                }

                imported++;
            }

            ps.executeBatch();
            br.close();

            JOptionPane.showMessageDialog(this, "Imported " + imported + " rows!");
            loadGrades();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Import failed!");
        }
    }

    private Double safeNum(String s) {
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { return null; }
    }
}
