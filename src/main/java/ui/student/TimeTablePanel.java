package ui.student;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

import auth.session.SessionManager;
import data.DBConnection;

public class TimeTablePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public TimeTablePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // header
        JLabel header = new JLabel("My Timetable", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(35, 55, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 5));
        add(header, BorderLayout.NORTH);

        // table
        String[] cols = {"Course Code", "Course Title", "Instructor ID", "Day/Time", "Room", "Semester", "Year"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // button panel
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton refreshBtn = styledButton("Refresh");
        refreshBtn.addActionListener(e -> loadTimetable());
        btnPanel.add(refreshBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // Load initial timetable
        loadTimetable();
    }

    // -table styling
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

    // -button styling
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
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(225, 233, 245)); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(Color.WHITE); }
        });

        return btn;
    }

    // load timetable from db based on registered courses
   private void loadTimetable() {
    model.setRowCount(0);
    int studentId = SessionManager.getCurrentUserId();

    String sql = """
        SELECT c.code, c.title, s.instructor_id, s.day_time, s.room, s.semester, s.year
        FROM enrollments e
        JOIN sections s ON e.section_id = s.id
        JOIN courses c ON s.course_id = c.code
        WHERE e.student_id = ?
    """;

    try (Connection conn = DBConnection.getErpConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, studentId);
        ResultSet rs = ps.executeQuery();

        boolean hasData = false;

        while (rs.next()) {
            hasData = true;
            model.addRow(new Object[]{
                    rs.getString("code"),
                    rs.getString("title"),
                    rs.getInt("instructor_id"),
                    rs.getString("day_time"),
                    rs.getString("room"),
                    rs.getString("semester"),
                    rs.getInt("year")
            });
        }

        if (!hasData)
            JOptionPane.showMessageDialog(this, "You are not enrolled in any courses.");

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error loading timetable!");
    }
}

}
