package util;

import javax.swing.*;

public class MessageUtil {

    public static void info(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void error(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void warn(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }
}
