package ui.instructor;

import auth.session.SessionManager;
import data.DBConnection;

import java.awt.*;
import java.sql.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class MySectionsPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public MySectionsPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // header
        JLabel header = new JLabel("My Sections", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(35, 55, 80));
        header.setBorder(new EmptyBorder(10, 5, 15, 5));
        add(header, BorderLayout.NORTH);

        // table
        String[] cols = {
                "Section ID", "Course Code",
                "Deadline",         
                "Day/Time", "Room", "Capacity", "Semester", "Year"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // buttons
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton refreshBtn = styledButton("Refresh");
        refreshBtn.addActionListener(e -> loadSections());

        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load initial data
        loadSections();
    }

    private JButton styledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(50, 50, 50));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190)),
                new EmptyBorder(8, 18, 8, 18)
        ));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(225, 233, 245)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(Color.WHITE); }
        });
        return btn;
    }

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

                if (isSelected) c.setBackground(new Color(200, 220, 240));
                else if (row % 2 == 0) c.setBackground(new Color(250, 250, 250));
                else c.setBackground(Color.WHITE);

                setBorder(noFocusBorder);
                return c;
            }
        });
    }

    // load sections of instructor from db
    private void loadSections() {
        model.setRowCount(0);
        int instructorId = SessionManager.getCurrentUserId();

        String sql = """
            SELECT 
                s.id, s.course_id,
                c.registration_deadline,       -- NEW
                s.day_time, s.room, 
                s.capacity, s.semester, s.year
            FROM sections s
            LEFT JOIN courses c
                ON s.course_id = c.code
            WHERE s.instructor_id = ?
            ORDER BY s.year DESC, s.semester DESC;
        """;

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, instructorId);
            ResultSet rs = ps.executeQuery();

            boolean found = false;

            while (rs.next()) {
                found = true;

                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("course_id"),
                        rs.getDate("registration_deadline"),   // NEW
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getInt("capacity"),
                        rs.getString("semester"),
                        rs.getInt("year")
                });
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "You have no assigned sections.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data!");
        }
    }
}
