package ui.common;

import data.SettingsDAO;
import javax.swing.*;
import java.awt.*;

public class MaintenanceBanner {

    public static void attachBanner(JFrame frame) {
        SettingsDAO dao = new SettingsDAO();

        if (!dao.isMaintenanceOn()) return; // Maintenance OFF: no banner

        JPanel banner = new JPanel();
        banner.setBackground(new Color(255, 200, 0)); 

        JLabel message = new JLabel(" SYSTEM IS IN MAINTENANCE MODE — Changes are disabled");
        message.setFont(new Font("Segoe UI", Font.BOLD, 14));
        message.setForeground(Color.BLACK);

        banner.add(message);

        frame.add(banner, BorderLayout.NORTH);
        frame.revalidate();
    }
}
