package ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.mindrot.jbcrypt.BCrypt;

import data.DBConnection;

public class ManageUsersPanel extends JPanel {

    private DefaultTableModel model;
    private JTable table;

    public ManageUsersPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header 
        JLabel header = new JLabel("Manage Users", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(new Color(35, 55, 80));
        header.setBorder(new EmptyBorder(10, 5, 15, 5));
        add(header, BorderLayout.NORTH);

        // Table columns
        String[] cols = { "User ID", "Username", "Role", "Status" };
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        styleTable(table);
        refreshTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        // Buttons Panel
        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton addUserBtn = styledButton("Add User");
        JButton editUserBtn = styledButton("Edit User");
        JButton updateRoleBtn = styledButton("Update Role");
        JButton deactivateBtn = styledButton("Deactivate User");
        JButton activateBtn = styledButton("Activate User");
        JButton refreshBtn = styledButton("Refresh");

        // button actions
        addUserBtn.addActionListener(e -> addUser());
        editUserBtn.addActionListener(e -> editUser());
        updateRoleBtn.addActionListener(e -> updateUserRole());
        deactivateBtn.addActionListener(e -> deactivateUser());
        activateBtn.addActionListener(e -> activateUser());
        refreshBtn.addActionListener(e -> refreshTable());


        btnPanel.add(addUserBtn);
        btnPanel.add(editUserBtn);
        btnPanel.add(updateRoleBtn);
        btnPanel.add(deactivateBtn);
        btnPanel.add(activateBtn);
        btnPanel.add(refreshBtn);


        add(btnPanel, BorderLayout.SOUTH);
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
                new EmptyBorder(8, 18, 8, 18)));

        // hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(225, 233, 245));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.WHITE);
            }
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
        header.setBackground(new Color(55, 70, 115));
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

    // load users from db into table
    private void refreshTable() {
        model.setRowCount(0);
        String sql = "SELECT user_id, username, role, status FROM users_auth ORDER BY user_id";
        try (Connection c = DBConnection.getAuthConnection();
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("status")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users!", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // add user based on role
    private void addUser() {

        // choose role
        String[] roles = { "Student", "Instructor", "Admin" };
        JComboBox<String> roleBox = new JComboBox<>(roles);

        int roleChoice = JOptionPane.showConfirmDialog(
                this, roleBox, "Select Role for New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (roleChoice != JOptionPane.OK_OPTION)
            return;

        String role = (String) roleBox.getSelectedItem();

        // role based form 
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Username:"));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        form.add(passwordField);

        JTextField rollField = null;
        JTextField programField = null;
        JTextField yearField = null;

        JTextField deptField = null;

        if (role.equals("Student")) {
            rollField = new JTextField();
            programField = new JTextField();
            yearField = new JTextField();

            form.add(new JLabel("Roll No:"));
            form.add(rollField);
            form.add(new JLabel("Program:"));
            form.add(programField);
            form.add(new JLabel("Year:"));
            form.add(yearField);
        } else if (role.equals("Instructor")) {
            deptField = new JTextField();
            form.add(new JLabel("Department:"));
            form.add(deptField);
        }

        int formResult = JOptionPane.showConfirmDialog(
                this, form, "Enter Details", JOptionPane.OK_CANCEL_OPTION);

        if (formResult != JOptionPane.OK_OPTION)
            return;

        // validating username and password
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty!");
            return;
        }

        try (Connection authConn = DBConnection.getAuthConnection();
                Connection erpConn = DBConnection.getErpConnection()) {

            var check = authConn.prepareStatement("SELECT username FROM users_auth WHERE username=?");
            check.setString(1, username);
            var rs = check.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already exists!");
                return;
            }

            var psId = authConn.prepareStatement("SELECT COALESCE(MAX(user_id),0) + 1 FROM users_auth");
            rs = psId.executeQuery();
            rs.next();
            int newId = rs.getInt(1);

            // hashing password
            String hash = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(10));

            // inserting data into auth db
            var addAuth = authConn.prepareStatement(
                    "INSERT INTO users_auth (user_id, username, role, password_hash, status, last_login) VALUES (?, ?, ?, ?, 'ACTIVE', NOW())");
            addAuth.setInt(1, newId);
            addAuth.setString(2, username);
            addAuth.setString(3, role);
            addAuth.setString(4, hash);
            addAuth.executeUpdate();

            if (role.equals("Student")) {

                String roll = rollField.getText().trim();
                String program = programField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());

                var addStu = erpConn.prepareStatement(
                        "INSERT INTO students(user_id, roll_no, program, year) VALUES (?, ?, ?, ?)");
                addStu.setInt(1, newId);
                addStu.setString(2, roll);
                addStu.setString(3, program);
                addStu.setInt(4, year);
                addStu.executeUpdate();
            }

            if (role.equals("Instructor")) {

                String dept = deptField.getText().trim();

                var addIns = erpConn.prepareStatement(
                        "INSERT INTO instructors(user_id, department) VALUES (?, ?)");
                addIns.setInt(1, newId);
                addIns.setString(2, dept);
                addIns.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "User created successfully!");

            refreshTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating user!");
        }
    }

    // update user role / password
    private void updateUserRole() {

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user first!");
            return;
        }

        int userId = (int) model.getValueAt(row, 0);
        String currentRole = (String) model.getValueAt(row, 2);

        String[] options = { "Change Role", "Change Password", "Cancel" };
        int choice = JOptionPane.showOptionDialog(
                this,
                "What do you want to update?",
                "Update User",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 2 || choice == -1)
            return;

        // change password
        if (choice == 1) {

            JPasswordField pass1 = new JPasswordField();
            JPasswordField pass2 = new JPasswordField();

            JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
            p.add(new JLabel("New Password:"));
            p.add(pass1);
            p.add(new JLabel("Confirm Password:"));
            p.add(pass2);

            int result = JOptionPane.showConfirmDialog(this, p, "Change Password", JOptionPane.OK_CANCEL_OPTION);
            if (result != JOptionPane.OK_OPTION)
                return;

            String pw1 = new String(pass1.getPassword());
            String pw2 = new String(pass2.getPassword());

            if (!pw1.equals(pw2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!");
                return;
            }

            if (pw1.length() < 3) {
                JOptionPane.showMessageDialog(this, "Password must be at least 3 characters!");
                return;
            }

            String hash = BCrypt.hashpw(pw1, BCrypt.gensalt());

            try (Connection conn = DBConnection.getAuthConnection()) {
                var ps = conn.prepareStatement("UPDATE users_auth SET password_hash=? WHERE user_id=?");
                ps.setString(1, hash);
                ps.setInt(2, userId);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Password updated successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating password!");
            }

            return;
        }

        // change role
        String[] roles = { "Admin", "Instructor", "Student" };
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setSelectedItem(currentRole);

        int result = JOptionPane.showConfirmDialog(
                this, roleBox, "Select New Role", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION)
            return;

        String newRole = (String) roleBox.getSelectedItem();
        if (newRole.equals(currentRole)) {
            JOptionPane.showMessageDialog(this, "Role is already the same!");
            return;
        }

        String roll = null, program = null, dept = null;
        Integer year = null;

        // STUDENT DETAILS
        if (newRole.equals("Student")) {

            JTextField rollField = new JTextField();
            JTextField progField = new JTextField();
            JTextField yearField = new JTextField();

            JPanel stuPanel = new JPanel(new GridLayout(3, 2, 8, 8));
            stuPanel.add(new JLabel("Roll No:"));
            stuPanel.add(rollField);
            stuPanel.add(new JLabel("Program:"));
            stuPanel.add(progField);
            stuPanel.add(new JLabel("Year:"));
            stuPanel.add(yearField);

            int ok = JOptionPane.showConfirmDialog(this, stuPanel, "Enter Student Details",
                    JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) {
                JOptionPane.showMessageDialog(this, "Update cancelled.");
                return;
            }

            try {
                roll = rollField.getText().trim();
                program = progField.getText().trim();
                year = Integer.parseInt(yearField.getText().trim());

                if (roll.isEmpty() || program.isEmpty())
                    throw new Exception();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid student details. Update cancelled.");
                return;
            }
        }

        if (newRole.equals("Instructor")) {

            JTextField deptField = new JTextField();

            int ok = JOptionPane.showConfirmDialog(this, deptField, "Enter Department", JOptionPane.OK_CANCEL_OPTION);
            if (ok != JOptionPane.OK_OPTION) {
                JOptionPane.showMessageDialog(this, "Update cancelled.");
                return;
            }

            dept = deptField.getText().trim();
            if (dept.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Department cannot be empty. Update cancelled.");
                return;
            }
        }

        // update db
        try (
                Connection authConn = DBConnection.getAuthConnection();
                Connection erpConn = DBConnection.getErpConnection()) {
            erpConn.setAutoCommit(false); // TRANSACTION START

            // DELETE OLD PROFILE
            if (currentRole.equals("Student")) {
                var del = erpConn.prepareStatement("DELETE FROM students WHERE user_id=?");
                del.setInt(1, userId);
                del.executeUpdate();
            }
            if (currentRole.equals("Instructor")) {
                var del = erpConn.prepareStatement("DELETE FROM instructors WHERE user_id=?");
                del.setInt(1, userId);
                del.executeUpdate();
            }

            if (newRole.equals("Student")) {
                var addStu = erpConn.prepareStatement(
                        "INSERT INTO students (user_id, roll_no, program, year) VALUES (?, ?, ?, ?)");
                addStu.setInt(1, userId);
                addStu.setString(2, roll);
                addStu.setString(3, program);
                addStu.setInt(4, year);
                addStu.executeUpdate();
            }

            if (newRole.equals("Instructor")) {
                var addIns = erpConn.prepareStatement(
                        "INSERT INTO instructors (user_id, department) VALUES (?, ?)");
                addIns.setInt(1, userId);
                addIns.setString(2, dept);
                addIns.executeUpdate();
            }

            var ps = authConn.prepareStatement("UPDATE users_auth SET role=? WHERE user_id=?");
            ps.setString(1, newRole);
            ps.setInt(2, userId);
            ps.executeUpdate();

            erpConn.commit(); 

            JOptionPane.showMessageDialog(this, "Role updated successfully!");
            refreshTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating role. No changes were made!");
        }
    }

    private void activateUser() {

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first!");
            return;
        }

        int userId = (int) model.getValueAt(row, 0);
        String status = (String) model.getValueAt(row, 3);

        if ("ACTIVE".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "User is already ACTIVE.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Activate this user?",
                "Confirm Activation",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        try (Connection conn = DBConnection.getAuthConnection()) {

            var ps = conn.prepareStatement(
                    "UPDATE users_auth SET status='ACTIVE' WHERE user_id=?");
            ps.setInt(1, userId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User activated successfully!");
            refreshTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error activating user!");
        }
    }

    private void deactivateUser() {

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first!");
            return;
        }

        int userId = (int) model.getValueAt(row, 0);
        String status = (String) model.getValueAt(row, 3);

        if ("INACTIVE".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "User is already INACTIVE.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Deactivate this user?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        try (Connection conn = DBConnection.getAuthConnection()) {

            var ps = conn.prepareStatement(
                    "UPDATE users_auth SET status='INACTIVE' WHERE user_id=?");
            ps.setInt(1, userId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User deactivated successfully!");
            refreshTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deactivating user!");
        }
    }

    private void editUser() {

        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user first!");
            return;
        }

        int userId = (int) model.getValueAt(row, 0);
        String username = (String) model.getValueAt(row, 1);
        String role     = (String) model.getValueAt(row, 2);

        JTextField userField = new JTextField(username);

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Username:"));
        form.add(userField);

        JTextField rollField = null, programField = null, yearField = null;
        JTextField deptField = null;

        try (Connection erpConn = DBConnection.getErpConnection()) {

            if (role.equals("Student")) {

                var ps = erpConn.prepareStatement("SELECT roll_no, program, year FROM students WHERE user_id=?");
                ps.setInt(1, userId);
                var rs = ps.executeQuery();

                if (rs.next()) {
                    rollField = new JTextField(rs.getString("roll_no"));
                    programField = new JTextField(rs.getString("program"));
                    yearField = new JTextField(String.valueOf(rs.getInt("year")));
                }

                form.add(new JLabel("Roll No:"));
                form.add(rollField);
                form.add(new JLabel("Program:"));
                form.add(programField);
                form.add(new JLabel("Year:"));
                form.add(yearField);
            }

            if (role.equals("Instructor")) {

                var ps = erpConn.prepareStatement("SELECT department FROM instructors WHERE user_id=?");
                ps.setInt(1, userId);
                var rs = ps.executeQuery();

                if (rs.next()) {
                    deptField = new JTextField(rs.getString("department"));
                }

                form.add(new JLabel("Department:"));
                form.add(deptField);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        int ok = JOptionPane.showConfirmDialog(this, form, "Edit User Details", JOptionPane.OK_CANCEL_OPTION);

        if (ok != JOptionPane.OK_OPTION) return;

        String newUsername = userField.getText().trim();

        try (
            Connection authConn = DBConnection.getAuthConnection();
            Connection erpConn  = DBConnection.getErpConnection()
        ) {
            erpConn.setAutoCommit(false);

            // update auth db
            var ps1 = authConn.prepareStatement("UPDATE users_auth SET username=? WHERE user_id=?");
            ps1.setString(1, newUsername);
            ps1.setInt(2, userId);
            ps1.executeUpdate();

            // update erp db
            if (role.equals("Student")) {
                var ps2 = erpConn.prepareStatement(
                    "UPDATE students SET roll_no=?, program=?, year=? WHERE user_id=?"
                );
                ps2.setString(1, rollField.getText());
                ps2.setString(2, programField.getText());
                ps2.setInt(3, Integer.parseInt(yearField.getText()));
                ps2.setInt(4, userId);
                ps2.executeUpdate();
            }

            if (role.equals("Instructor")) {
                var ps3 = erpConn.prepareStatement(
                    "UPDATE instructors SET department=? WHERE user_id=?"
                );
                ps3.setString(1, deptField.getText());
                ps3.setInt(2, userId);
                ps3.executeUpdate();
            }

            erpConn.commit();

            JOptionPane.showMessageDialog(this, "User updated successfully!");
            refreshTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating user!");
        }
    }

}
