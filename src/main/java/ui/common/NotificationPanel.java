package ui.common;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import data.NotificationDAO;
import domain.Notification;

public class NotificationPanel extends JFrame {

    public NotificationPanel(int userId, String role) {
        setTitle("Notifications");
        setSize(450, 500);
        setLocationRelativeTo(null);

        List<Notification> list = NotificationDAO.getNotifications(userId, role);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (Notification n : list) {
            JPanel card = new JPanel(new BorderLayout());
            card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            card.setBackground(Color.WHITE);

            JLabel msg = new JLabel("<html><p style='width:350px'>" + n.getMessage() + "</p></html>");
            JLabel date = new JLabel(n.getCreatedAt().toString());
            date.setForeground(Color.DARK_GRAY);
            date.setFont(new Font("Segoe UI", Font.PLAIN, 10));

            card.add(msg, BorderLayout.CENTER);
            card.add(date, BorderLayout.SOUTH);

            panel.add(card);
            panel.add(Box.createVerticalStrut(10));
        }

        add(new JScrollPane(panel));
    }
}
