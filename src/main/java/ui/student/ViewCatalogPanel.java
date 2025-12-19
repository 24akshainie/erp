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

import data.DBConnection;

public class ViewCatalogPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public ViewCatalogPanel() {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // header
        JLabel header = new JLabel("Course Catalog (All Sections)", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(35, 55, 80));
        header.setBorder(BorderFactory.createEmptyBorder(10, 5, 15, 5));
        add(header, BorderLayout.NORTH);

        // table
        String[] cols = {
                "Course Code", "Course Title", "Credits",
                "Registration Deadline",
                "Instructor ID", "Day / Time", "Room",
                "Capacity", "Semester", "Year"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = styledButton(" Refresh");
        refreshBtn.addActionListener(e -> loadCatalog());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        btnPanel.add(refreshBtn);

        add(btnPanel, BorderLayout.SOUTH);

        // load initial data
        loadCatalog();
    }

    // table styling
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
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel,
                                                           boolean focus, int row, int col) {

                Component c = super.getTableCellRendererComponent(t, val, sel, focus, row, col);

                if (sel) c.setBackground(new Color(200, 220, 240));
                else if (row % 2 == 0) c.setBackground(new Color(250, 250, 250));
                else c.setBackground(Color.WHITE);

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
        btn.setForeground(Color.BLACK);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        return btn;
    }

    // load all courses from db
    private void loadCatalog() {

        model.setRowCount(0);

        String sql = """
            SELECT 
                c.code, c.title, c.credits, c.registration_deadline,
                s.instructor_id, s.day_time, s.room,
                s.capacity, s.semester, s.year
            FROM courses c
            LEFT JOIN sections s
                ON s.course_id = c.code
            ORDER BY c.code, s.id;
        """;

        try (Connection conn = DBConnection.getErpConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            boolean any = false;

            while (rs.next()) {
                any = true;
                model.addRow(new Object[] {
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits"),
                        rs.getDate("registration_deadline"), // NEW COLUMN
                        rs.getObject("instructor_id"),
                        rs.getString("day_time"),
                        rs.getString("room"),
                        rs.getObject("capacity"),
                        rs.getString("semester"),
                        rs.getObject("year")
                });
            }

            if (!any) {
                JOptionPane.showMessageDialog(this, "No catalog entries found.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading catalog.");
        }
    }
}
