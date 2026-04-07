package com.resudex.swing;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import javax.swing.*;
import java.awt.*;

/**
 * ResudexApp – Main Swing entry point.
 * Uses a simple "swap panel" approach: each screen is a JPanel swapped in/out.
 */
public class ResudexApp extends JFrame {

    // Navigation - accessed by all panels via ResudexApp.navigate(...)
    public static ResudexApp instance;

    // Currently logged-in user (set on login)
    public static int    currentUserId   = -1;
    public static String currentUsername = "";

    private JPanel container;

    public ResudexApp() {
        instance = this;

        setTitle("RESUDEX - Resume Intelligence Platform");
        setSize(900, 650);
        setMinimumSize(new Dimension(750, 550));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        container = new JPanel(new BorderLayout());
        add(container);

        // Start at home screen
        showHome();
    }

    /** Replace the entire content with a new panel */
    public static void swap(JPanel panel) {
        instance.container.removeAll();
        instance.container.add(panel, BorderLayout.CENTER);
        instance.container.revalidate();
        instance.container.repaint();
    }

    public static void showHome()           { swap(new HomePanel()); }
    public static void showUserLogin()      { swap(new UserLoginPanel()); }
    public static void showUserDashboard()  { swap(new UserDashboardPanel()); }
    public static void showAdminLogin()     { swap(new AdminLoginPanel()); }
    public static void showAdminDashboard() { swap(new AdminDashboardPanel()); }

    public static void main(String[] args) {
        // Use FlatLaf for a modern "innovative" look
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            
            // Absolute Parity Design Tokens
            Color bgMidnight = new Color(13, 2, 33); // #0D0221
            Color proCyan    = new Color(0, 240, 255); // #00F0FF
            Color proBlue    = new Color(37, 99, 235); // #2563EB
            
            UIManager.put("Panel.background", bgMidnight);
            UIManager.put("Label.foreground", Color.WHITE);
            UIManager.put("Button.arc", 999); // Full Pill Shape
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Table.alternateRowColor", new Color(17, 3, 44));
            
            UIManager.put("Button.background", proBlue);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Component.focusColor", proCyan);
            UIManager.put("TabbedPane.selectedBackground", proCyan);
            UIManager.put("ProgressBar.foreground", proCyan);
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            ResudexApp app = new ResudexApp();
            app.setVisible(true);
        });
    }
}
