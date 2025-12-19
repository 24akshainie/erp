package ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import data.DBConnection;

public class ManageSectionsPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;

    public ManageSectionsPanel() {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel header = new JLabel("Manage Sections", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(35, 55, 80));
        header.setBorder(new EmptyBorder(10, 5, 15, 5));
        add(header, BorderLayout.NORTH);

        String[] cols = { "Section ID", "Course Code", "Instructor ID", "Day and Time", "Room", "Capacity", "Semester", "Year" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        refreshTable();

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton addBtn = createStyledButton("Add Section");
        JButton refreshBtn = createStyledButton("Refresh");
        JButton updateBtn = createStyledButton("Update Section");
        updateBtn.addActionListener(e -> updateSection());
        addBtn.addActionListener(e -> addSection());
        refreshBtn.addActionListener(e -> refreshTable());

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(new Color(245, 248, 250));
        btn.setForeground(new Color(50, 50, 50));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190)),
                new EmptyBorder(8, 18, 8, 18)));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(225, 235, 245));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(245, 248, 250));
            }
        });

        return btn;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(28);
        table.setGridColor(new Color(235, 235, 235));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(55, 70, 115));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);

                if (isSelected) {
                    c.setBackground(new Color(200, 220, 240));
                } else if (row % 2 == 0) {
                    c.setBackground(new Color(250, 250, 250));
                } else {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        });
    }

    private void refreshTable() {
        model.setRowCount(0);
        String sql = """
                    SELECT
                        s.id AS section_id,
                        c.code AS course_code,
                        s.instructor_id,
                        s.day_time,
                        s.room,
                        s.capacity,
                        s.semester,
                        s.year
                    FROM sections s
                    JOIN courses c ON s.course_id = c.code
                """;

        try (Connection conn = DBConnection.getErpConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("section_id"),
                        rs.getString("course_code"),
                        (rs.getObject("instructor_id") == null ? "None" : rs.getInt("instructor_id")),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getString("semester"),
                        rs.getInt("year")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading sections!");
        }
    }

    private void addSection() {

        JComboBox<String> courseBox = new JComboBox<>();
        JComboBox<String> instructorBox = new JComboBox<>();

        try (Connection conn = DBConnection.getErpConnection();
                Statement stmt = conn.createStatement()) {

            ResultSet rc = stmt.executeQuery("SELECT code FROM courses ORDER BY code");
            while (rc.next()) courseBox.addItem(rc.getString("code"));
            rc.close();

            instructorBox.addItem("None");

            ResultSet ri = stmt.executeQuery("SELECT i.user_id, u.username FROM instructors i JOIN auth_db.users_auth u ON i.user_id = u.user_id");
            while (ri.next()) instructorBox.addItem(ri.getInt("user_id") + " - " + ri.getString("username"));
            ri.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading dropdown data!");
            return;
        }

        JTextField dayField = new JTextField();
        JTextField timeField = new JTextField();
        JTextField roomField = new JTextField();
        JTextField capacityField = new JTextField();
        JTextField semesterField = new JTextField();
        JTextField yearField = new JTextField();

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("Course:"));
        form.add(courseBox);
        form.add(new JLabel("Instructor:"));
        form.add(instructorBox);
        form.add(new JLabel("Day:"));
        form.add(dayField);
        form.add(new JLabel("Time:"));
        form.add(timeField);
        form.add(new JLabel("Room:"));
        form.add(roomField);
        form.add(new JLabel("Capacity:"));
        form.add(capacityField);
        form.add(new JLabel("Semester:"));
        form.add(semesterField);
        form.add(new JLabel("Year:"));
        form.add(yearField);

        int result = JOptionPane.showConfirmDialog(this, form, "Add New Section", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String courseCode = courseBox.getSelectedItem().toString();
            String instSel = instructorBox.getSelectedItem().toString();
            int year = Integer.parseInt(yearField.getText().trim());
            String day_time = dayField.getText().trim() + " " + timeField.getText().trim();

            String sql = """
                        INSERT INTO sections (course_id, instructor_id, day_time, room, capacity, semester, year)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

            try (Connection conn = DBConnection.getErpConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, courseCode);
                if (instSel.equals("None")) ps.setNull(2, java.sql.Types.INTEGER);
                else ps.setInt(2, Integer.parseInt(instSel.split(" - ")[0]));
                int capacity = Integer.parseInt(capacityField.getText().trim());
                ps.setString(3, day_time);
                ps.setString(4, roomField.getText().trim());
                ps.setInt(5, capacity);
                ps.setString(6, semesterField.getText().trim());
                ps.setInt(7, year);

                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Section added successfully!");
            refreshTable();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Capacity and Year must be numbers!");
        } catch (SQLIntegrityConstraintViolationException e) {
            JOptionPane.showMessageDialog(this, "Duplicate or invalid entry!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding section!");
        }
    }

    private void updateSection() {

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a section first!");
            return;
        }

        int sectionId = (int) model.getValueAt(row, 0);

        JComboBox<String> courseBox = new JComboBox<>();
        JComboBox<String> instructorBox = new JComboBox<>();

        try (Connection conn = DBConnection.getErpConnection();
                Statement stmt = conn.createStatement()) {

            ResultSet rc = stmt.executeQuery("SELECT code FROM courses ORDER BY code");
            while (rc.next()) courseBox.addItem(rc.getString("code"));
            rc.close();

            instructorBox.addItem("None");

            ResultSet ri = stmt.executeQuery("SELECT i.user_id, u.username FROM instructors i JOIN auth_db.users_auth u ON i.user_id = u.user_id");
            while (ri.next()) instructorBox.addItem(ri.getInt("user_id") + " - " + ri.getString("username"));
            ri.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading dropdown data!");
            return;
        }

        String oldCourse = model.getValueAt(row, 1).toString();
        String oldInstructor = model.getValueAt(row, 2).toString();
        String oldDayTime = model.getValueAt(row, 3).toString();
        String oldRoom = model.getValueAt(row, 4).toString();
        String oldCapacity = model.getValueAt(row, 5).toString();
        String oldSemester = model.getValueAt(row, 6).toString();
        String oldYear = model.getValueAt(row, 7).toString();

        String[] dtSplit = oldDayTime.split(" ", 2);
        String oldDay = (dtSplit.length > 0) ? dtSplit[0] : "";
        String oldTime = (dtSplit.length > 1) ? dtSplit[1] : "";

        JTextField dayField = new JTextField(oldDay);
        JTextField timeField = new JTextField(oldTime);
        JTextField roomField = new JTextField(oldRoom);
        JTextField capacityField = new JTextField(oldCapacity);
        JTextField semesterField = new JTextField(oldSemester);
        JTextField yearField = new JTextField(oldYear);

        courseBox.setSelectedItem(oldCourse);

        if (oldInstructor.equals("None")) instructorBox.setSelectedIndex(0);
        else {
            for (int i = 1; i < instructorBox.getItemCount(); i++) {
                if (instructorBox.getItemAt(i).startsWith(oldInstructor + " -")) {
                    instructorBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        JPanel form = new JPanel(new GridLayout(8, 2, 8, 8));
        form.setBorder(new EmptyBorder(10, 10, 10, 10));

        form.add(new JLabel("Course:"));
        form.add(courseBox);
        form.add(new JLabel("Instructor:"));
        form.add(instructorBox);
        form.add(new JLabel("Day:"));
        form.add(dayField);
        form.add(new JLabel("Time:"));
        form.add(timeField);
        form.add(new JLabel("Room:"));
        form.add(roomField);
        form.add(new JLabel("Capacity:"));
        form.add(capacityField);
        form.add(new JLabel("Semester:"));
        form.add(semesterField);
        form.add(new JLabel("Year:"));
        form.add(yearField);

        int result = JOptionPane.showConfirmDialog(this, form, "Update Section", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String newCourseCode = courseBox.getSelectedItem().toString();
            String instSel = instructorBox.getSelectedItem().toString();
            Integer instructorId = null;
            if (!instSel.equals("None")) instructorId = Integer.parseInt(instSel.split(" - ")[0]);
            int capacity = Integer.parseInt(capacityField.getText().trim());
            int year = Integer.parseInt(yearField.getText().trim());
            String newDayTime = dayField.getText().trim() + " " + timeField.getText().trim();

            String sql = """
                        UPDATE sections
                        SET course_id=?, instructor_id=?, day_time=?, room=?, capacity=?, semester=?, year=?
                        WHERE id=?
                    """;

            try (Connection conn = DBConnection.getErpConnection();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, newCourseCode);
                if (instructorId == null) ps.setNull(2, java.sql.Types.INTEGER);
                else ps.setInt(2, instructorId);
                ps.setString(3, newDayTime);
                ps.setString(4, roomField.getText().trim());
                ps.setInt(5, capacity);
                ps.setString(6, semesterField.getText().trim());
                ps.setInt(7, year);
                ps.setInt(8, sectionId);

                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Section updated successfully!");
            refreshTable();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating section!");
        }
    }

}
