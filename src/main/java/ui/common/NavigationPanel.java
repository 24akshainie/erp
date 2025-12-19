package ui.common;

import auth.session.SessionManager;

import javax.swing.*;
import java.awt.*;

public class NavigationPanel extends JPanel {

    private JLabel lblUser;
    private JPanel buttonContainer;

    public NavigationPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(200, 600));
        setBackground(new Color(240, 240, 240));

        // Top User info panel
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new GridLayout(2, 1));
        userPanel.setBackground(new Color(220, 220, 220));
        userPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String username = SessionManager.getCurrentUsername();
        String role = SessionManager.getCurrentRole();

        lblUser = new JLabel("User: " + username);
        JLabel lblRole = new JLabel("Role: " + role);

        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        lblRole.setFont(new Font("Arial", Font.PLAIN, 13));

        userPanel.add(lblUser);
        userPanel.add(lblRole);

        add(userPanel, BorderLayout.NORTH);

        // Center buttons container
        buttonContainer = new JPanel();
        buttonContainer.setLayout(new GridLayout(20, 1, 0, 8));
        buttonContainer.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        buttonContainer.setBackground(new Color(240, 240, 240));

        add(buttonContainer, BorderLayout.CENTER);
    }

    // Adds a navigation button with a label & callback action.
    public void addNavButton(String label, Runnable action) {
        JButton btn = new JButton(label);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));

        btn.addActionListener(e -> action.run());
        buttonContainer.add(btn);
    }

    // Clears all navigation buttons (if needed when switching pages).
    public void clearButtons() {
        buttonContainer.removeAll();
        buttonContainer.revalidate();
        buttonContainer.repaint();
    }
}
