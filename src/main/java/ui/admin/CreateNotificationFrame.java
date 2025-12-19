package ui.admin;

import javax.swing.*;
import java.awt.*;

import data.NotificationDAO;
import domain.Notification;
import auth.session.SessionManager;

public class CreateNotificationFrame extends JFrame {

    public CreateNotificationFrame() {
        setTitle("Create Notification");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 1, 10, 10));

        JLabel roleLbl = new JLabel("Select Target Role:");
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"STUDENT", "INSTRUCTOR", "ALL"});

        JLabel messageLbl = new JLabel("Message:");
        JTextArea messageArea = new JTextArea();
        messageArea.setLineWrap(true);

        JButton sendBtn = new JButton("Send Notification");

        sendBtn.addActionListener(e -> {
            String role = roleBox.getSelectedItem().toString();
            String message = messageArea.getText().trim();

            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Message cannot be empty!");
                return;
            }

            Notification n = new Notification();
            n.setAdminId(SessionManager.getCurrentUserId());
            n.setTargetRole(role);
            n.setTargetUserId(null); 
            n.setMessage(message);

            if (NotificationDAO.createNotification(n)) {
                JOptionPane.showMessageDialog(this, "Notification sent successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create notification.");
            }
        });

        add(roleLbl);
        add(roleBox);
        add(messageLbl);
        add(new JScrollPane(messageArea));
        add(sendBtn);
    }
}
