package com.resudex.swing;

import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import javax.swing.*;
import java.awt.*;

/**
 * ResudexApp – Main Swing entry point.
 */
public class ResudexApp extends JFrame {

    public static ResudexApp app;

    // user state
    public static int    uid = -1;
    public static String usr = "";

    private JPanel p_main;

    public ResudexApp() {
        app = this;

        setTitle("RESUDEX - Resume Intelligence Platform");
        setSize(900, 650);
        setMinimumSize(new Dimension(750, 550));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        p_main = new JPanel(new BorderLayout());
        add(p_main);

        // start screen
        go_home();
    }

    /** swap bits */
    public static void pop(JPanel panel) {
        app.p_main.removeAll();
        app.p_main.add(panel, BorderLayout.CENTER);
        app.p_main.revalidate();
        app.p_main.repaint();
    }

    public static void go_home()      { pop(new HomePanel()); }
    public static void go_user_in()   { pop(new UserLoginPanel()); }
    public static void go_user_pnl()  { pop(new UserDashboardPanel()); }
    public static void go_admin_in()  { pop(new AdminLoginPanel()); }
    public static void go_admin_pnl() { pop(new AdminDashboardPanel()); }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatMacDarkLaf());
            
            Color bg = new Color(13, 2, 33); 
            Color cyan = new Color(0, 240, 255);
            Color blue = new Color(37, 99, 235);
            
            UIManager.put("Panel.background", bg);
            UIManager.put("Label.foreground", Color.WHITE);
            UIManager.put("Button.arc", 999); 
            UIManager.put("Component.arc", 12);
            UIManager.put("TextComponent.arc", 12);
            UIManager.put("Table.alternateRowColor", new Color(17, 3, 44));
            
            UIManager.put("Button.background", blue);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Component.focusColor", cyan);
            UIManager.put("TabbedPane.selectedBackground", cyan);
            UIManager.put("ProgressBar.foreground", cyan);
        } catch (Exception e) {
            System.err.println("Theme fail");
        }

        SwingUtilities.invokeLater(() -> {
            ResudexApp main = new ResudexApp();
            main.setVisible(true);
        });
    }
}
