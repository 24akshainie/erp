package ui.admin;

import java.awt.BorderLayout;
import java.sql.ResultSet;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.util.List;

import service.NotificationService;

public class NotificationPanel extends JFrame {
    public NotificationPanel(int userId, String role) {
        setTitle("Notifications");
        setSize(500, 500);
        setLayout(new BorderLayout());

        JTextArea area = new JTextArea();
        area.setEditable(false);

        try {
            List<String> messages = NotificationService.getNotificationsForUser(userId, role);

            for (String msg : messages) {
                area.append("• " + msg + "\n");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        add(new JScrollPane(area), BorderLayout.CENTER);
    }
}
