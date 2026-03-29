package com.resudex.swing;

import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;

/**
 * AdminLoginPanel - Premium login screen for the admin with background image.
 * Credentials: admin / admin123
 */
public class AdminLoginPanel extends JPanel {

    private Image bgImage;

    public AdminLoginPanel() {
        setLayout(new BorderLayout());
        try {
            bgImage = ImageIO.read(getClass().getResource("/images/login_bg.png"));
        } catch (Exception e) {
            System.err.println("Could not load login_bg.png");
        }

        // -------- Top bar --------
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JButton backBtn = new JButton("← Back");
        backBtn.putClientProperty("JButton.buttonType", "borderless");
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setForeground(new Color(200, 220, 210));
        backBtn.addActionListener(e -> ResudexApp.showHome());

        JLabel topTitle = new JLabel("RESUDEX — RECRUITER ACCESS");
        topTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        topTitle.setForeground(new Color(150, 220, 180));

        topBar.add(backBtn, BorderLayout.WEST);
        topBar.add(topTitle, BorderLayout.CENTER);

        // -------- Form --------
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(20, 25, 22, 230)); // Darker, translucent
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 85, 70), 1, true),
            BorderFactory.createEmptyBorder(35, 45, 35, 45)
        ));
        card.setPreferredSize(new Dimension(420, 380));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 0, 8, 0);

        JLabel heading = new JLabel("Security Check", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 26));
        heading.setForeground(new Color(220, 240, 230));

        JLabel hint = new JLabel("Authorized Personnel Only", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(new Color(120, 140, 130));

        JTextField userField = new JTextField("admin");
        userField.putClientProperty("JTextField.placeholderText", "Username");
        userField.setPreferredSize(new Dimension(300, 42));

        JPasswordField passField = new JPasswordField("admin123");
        passField.putClientProperty("JTextField.placeholderText", "Password");
        passField.setPreferredSize(new Dimension(300, 42));

        JLabel msgLabel = new JLabel(" ", SwingConstants.CENTER);
        msgLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));

        JButton loginBtn = new JButton("ACCESS DASHBOARD");
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginBtn.setBackground(new Color(40, 160, 100));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.putClientProperty("JButton.buttonType", "roundRect");
        loginBtn.setPreferredSize(new Dimension(300, 50));

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            loginBtn.setEnabled(false);
            loginBtn.setText("Verifying Credentials...");
            new Thread(() -> {
                boolean ok = ApiClient.adminLogin(username, password);
                SwingUtilities.invokeLater(() -> {
                    loginBtn.setEnabled(true);
                    loginBtn.setText("ACCESS DASHBOARD");
                    if (ok) {
                        ResudexApp.showAdminDashboard();
                    } else {
                        msgLabel.setText("Access Denied. Try: admin / admin123");
                        msgLabel.setForeground(new Color(255, 120, 120));
                    }
                });
            }).start();
        });

        c.gridy = 0; card.add(heading, c);
        c.gridy = 1; card.add(hint, c);
        c.gridy = 2; card.add(Box.createVerticalStrut(15), c);
        c.gridy = 3; card.add(label("Admin ID"), c);
        c.gridy = 4; card.add(userField, c);
        c.gridy = 5; card.add(label("Passkey"), c);
        c.gridy = 6; card.add(passField, c);
        c.gridy = 7; card.add(msgLabel, c);
        c.gridy = 8; card.add(loginBtn, c);

        center.add(card);
        add(topBar, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (bgImage != null) {
            g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(15, 25, 20, 200));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        } else {
            super.paintComponent(g);
            setBackground(new Color(18, 20, 24));
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(new Color(140, 170, 150));
        return l;
    }
}
