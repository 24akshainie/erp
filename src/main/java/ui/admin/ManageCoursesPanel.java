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
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;


import data.DBConnection;

public class ManageCoursesPanel extends JPanel {

    private DefaultTableModel model; // Table data model
    private JTable table; // course display table

    public ManageCoursesPanel() {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // header
        JLabel header = new JLabel("Manage Courses", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(35, 55, 80));
        header.setBorder(new EmptyBorder(10, 5, 15, 5));
        add(header, BorderLayout.NORTH);

        // table setup
        String[] cols = {"Course Code", "Title", "Credits", "Instructor ID", "Deadline"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        styleTable(table);
        refreshTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // button panel
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton addBtn = createStyledButton("Add Course");
        JButton refreshBtn = createStyledButton("Refresh");

        addBtn.addActionListener(e -> addCourse());
        refreshBtn.addActionListener(e -> refreshTable());

        btnPanel.add(addBtn);
        btnPanel.add(refreshBtn);

        add(btnPanel, BorderLayout.SOUTH);
    }

    // button stylings 
    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(140, 35));
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(45, 45, 45));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                new EmptyBorder(8, 18, 8, 18)
        ));

        // hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(new Color(225, 235, 245)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(Color.WHITE); }
        });

        return btn;
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
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSelected,
                                                            boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSelected, hasFocus, row, col);

                if (isSelected) {
                    c.setBackground(new Color(200, 220, 240));
                } else if (row % 2 == 0) {
                    c.setBackground(new Color(248, 248, 248));
                } else {
                    c.setBackground(Color.WHITE);
                }
                setBorder(noFocusBorder);
                return c;
            }
        });
    }

    // load courses from db into table
    private void refreshTable() {
        model.setRowCount(0);
        String sql = "SELECT code, title, credits, instructor_id, registration_deadline FROM courses";

        try (Connection c = DBConnection.getErpConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
    rs.getString("code"),
    rs.getString("title"),
    rs.getInt("credits"),
    rs.getInt("instructor_id"),
    rs.getString("registration_deadline")
});

            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses!");
        }
    }

// open dialogue box to add new course
private void addCourse() {

    // load instructors from db
    java.util.List<Integer> instructorIds = new java.util.ArrayList<>();
    java.util.List<String> instructorNames = new java.util.ArrayList<>();

    try (Connection c = DBConnection.getErpConnection();
         PreparedStatement ps = c.prepareStatement(
                 "SELECT i.user_id, u.username FROM erp_db.instructors i " +
                 "JOIN auth_db.users_auth u ON i.user_id = u.user_id ORDER BY u.username"
         );
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            instructorIds.add(rs.getInt("user_id"));
            instructorNames.add(rs.getString("username"));
        }

    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Error loading instructors!");
        return;
    }

    if (instructorIds.isEmpty()) {
        JOptionPane.showMessageDialog(this, "No instructors available! Please add an instructor first.");
        return;
    }

    // input fields
    JComboBox<String> instructorBox = new JComboBox<>(instructorNames.toArray(new String[0]));

    JTextField codeField = new JTextField();
    JTextField titleField = new JTextField();
    JTextField creditsField = new JTextField();
    JTextField deadlineField = new JTextField();  // NEW FIELD

    // dialog box layout
    Dimension fieldDim = new Dimension(220, 30);
    codeField.setPreferredSize(fieldDim);
    titleField.setPreferredSize(fieldDim);
    creditsField.setPreferredSize(fieldDim);
    instructorBox.setPreferredSize(fieldDim);
    deadlineField.setPreferredSize(fieldDim);

    Color borderColor = new Color(180, 180, 180);
    JTextField[] fields = {codeField, titleField, creditsField, deadlineField};
    for (JTextField f : fields) {
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                new EmptyBorder(4, 8, 4, 8)
        ));
    }

    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(Color.WHITE);
    form.setBorder(new EmptyBorder(15, 15, 15, 15));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 10, 10, 10);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
    JLabel codeLabel = new JLabel("Course Code:");
    JLabel titleLabel = new JLabel("Title:");
    JLabel creditsLabel = new JLabel("Credits:");
    JLabel instructorLabel = new JLabel("Instructor:");
    JLabel deadlineLabel = new JLabel("Deadline (YYYY-MM-DD):"); // NEW LABEL

    codeLabel.setFont(labelFont);
    titleLabel.setFont(labelFont);
    creditsLabel.setFont(labelFont);
    instructorLabel.setFont(labelFont);
    deadlineLabel.setFont(labelFont);

    gbc.gridx = 0; gbc.gridy = 0; form.add(codeLabel, gbc);
    gbc.gridx = 1; form.add(codeField, gbc);

    gbc.gridx = 0; gbc.gridy = 1; form.add(titleLabel, gbc);
    gbc.gridx = 1; form.add(titleField, gbc);

    gbc.gridx = 0; gbc.gridy = 2; form.add(creditsLabel, gbc);
    gbc.gridx = 1; form.add(creditsField, gbc);

    gbc.gridx = 0; gbc.gridy = 3; form.add(instructorLabel, gbc);
    gbc.gridx = 1; form.add(instructorBox, gbc);

    gbc.gridx = 0; gbc.gridy = 4; form.add(deadlineLabel, gbc);
    gbc.gridx = 1; form.add(deadlineField, gbc);

    JButton okBtn = new JButton("Add");
    JButton cancelBtn = new JButton("Cancel");

    JButton[] buttons = {okBtn, cancelBtn};
    for (JButton btn : buttons) {
        btn.setPreferredSize(new Dimension(100, 32));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(45, 45, 45));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor),
                new EmptyBorder(4, 12, 4, 12)
        ));
    }

    JPanel btnPanel = new JPanel();
    btnPanel.setBackground(Color.WHITE);
    btnPanel.add(okBtn);
    btnPanel.add(cancelBtn);

    gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
    form.add(btnPanel, gbc);

    
    JDialog dialog = new JDialog();
    dialog.setTitle("Add Course");
    dialog.setModal(true);
    dialog.getContentPane().add(form);
    dialog.pack();
    dialog.setLocationRelativeTo(this);

    
    okBtn.addActionListener(e -> {
        try {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            int credits = Integer.parseInt(creditsField.getText().trim());
            String registration_deadline = deadlineField.getText().trim();

            int selectedIndex = instructorBox.getSelectedIndex();
            int instructorId = instructorIds.get(selectedIndex);

            String sql = "INSERT INTO courses (code, title, credits, instructor_id, registration_deadline) VALUES (?, ?, ?, ?, ?)";

            try (Connection c = DBConnection.getErpConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setString(1, code);
                ps.setString(2, title);
                ps.setInt(3, credits);
                ps.setInt(4, instructorId);
                ps.setString(5, registration_deadline);

                ps.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Course added!");
                dialog.dispose();
                refreshTable();

            } catch (SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(dialog, "Course code already exists!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "SQL Error: " + ex.getMessage());
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Credits must be a number!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Database error!");
        }
    });

    cancelBtn.addActionListener(e -> dialog.dispose());

    dialog.setVisible(true);
}



}
