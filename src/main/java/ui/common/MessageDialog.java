package ui.common;

import javax.swing.*;

public class MessageDialog {

    // Show an information popup 
    public static void info(String msg) {
        JOptionPane.showMessageDialog(
                null,
                msg,
                "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Show a success popup
    public static void success(String msg) {
        JOptionPane.showMessageDialog(
                null,
                msg,
                "Success",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // Show an error popup 
    public static void error(String msg) {
        JOptionPane.showMessageDialog(
                null,
                msg,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // Show a warning popup 
    public static void warn(String msg) {
        JOptionPane.showMessageDialog(
                null,
                msg,
                "Warning",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // Show YES / NO confirmation dialog: returns true if YES
    public static boolean confirm(String msg) {
        int response = JOptionPane.showConfirmDialog(
                null,
                msg,
                "Confirm Action",
                JOptionPane.YES_NO_OPTION
        );
        return response == JOptionPane.YES_OPTION;
    }
}
