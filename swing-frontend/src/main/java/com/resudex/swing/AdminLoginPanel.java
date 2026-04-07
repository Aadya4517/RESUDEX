package com.resudex.swing;

import javax.swing.*;
import java.awt.*;

/**
 * AdminLoginPanel - RESUDEX "Recruiter Access" portal parity.
 * Purple/blue gradient authorize button, glassmorphic card, slate labels.
 */
public class AdminLoginPanel extends JPanel {

    public AdminLoginPanel() {
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(13, 2, 45), 0, getHeight(), new Color(6, 0, 18));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 240, 255, 8));
                for (int i = 0; i < getWidth(); i += 80) g2.drawLine(i, 0, i, getHeight());
                for (int j = 0; j < getHeight(); j += 80) g2.drawLine(0, j, getWidth(), j);
                g2.dispose();
            }
        };
        main.add(createAdminCard());
        add(main, BorderLayout.CENTER);
    }

    private JPanel createAdminCard() {
        JTextField     idField   = glassField(340, 50);
        JPasswordField passField = glassPassField(340, 50);
        JLabel         msg       = new JLabel(" ", SwingConstants.CENTER);
        msg.setFont(new Font("SansSerif", Font.PLAIN, 12));
        msg.setForeground(new Color(255, 70, 70));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btn = authorizeButton("AUTHORIZE  \u2192");
        btn.addActionListener(e -> {
            String u    = idField.getText().trim();
            String pStr = new String(passField.getPassword()).trim();
            if (u.isEmpty() || pStr.isEmpty()) { msg.setText("Authorization required."); return; }
            btn.setText("VERIFYING...");
            new Thread(() -> {
                boolean ok = ApiClient.adminLogin(u, pStr);
                SwingUtilities.invokeLater(() -> {
                    if (ok) {
                        ResudexApp.showAdminDashboard();
                    } else {
                        btn.setText("AUTHORIZE  \u2192");
                        msg.setText("ACCESS DENIED");
                    }
                });
            }).start();
        });

        JButton back = new JButton("\u2190 RETURN TO FRONTEND");
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setForeground(new Color(148, 163, 184));
        back.setFont(new Font("SansSerif", Font.PLAIN, 11));
        back.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> ResudexApp.showHome());

        // Card content with BoxLayout
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        // Header branding  ⬡ RESUDEX ADMIN
        JLabel branding = new JLabel("<html>&#x1F6E1; RESUDEX &nbsp;<font color='#94A3B8'>ADMIN</font></html>", SwingConstants.CENTER);
        branding.setFont(new Font("SansSerif", Font.BOLD, 22));
        branding.setForeground(Color.WHITE);
        branding.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heading = new JLabel("Recruiter Access", SwingConstants.CENTER);
        heading.setFont(new Font("SansSerif", Font.BOLD, 38));
        heading.setForeground(Color.WHITE);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("<html><center>Secure portal for candidate selection and job management.</center></html>", SwingConstants.CENTER);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(new Color(148, 163, 184));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel restricted = new JLabel("RESTRICTED SYSTEM ACCESS", SwingConstants.CENTER);
        restricted.setFont(new Font("SansSerif", Font.BOLD, 10));
        restricted.setForeground(new Color(71, 85, 105));
        restricted.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(branding);
        p.add(Box.createRigidArea(new Dimension(0, 34)));
        p.add(heading);
        p.add(Box.createRigidArea(new Dimension(0, 8)));
        p.add(sub);
        p.add(Box.createRigidArea(new Dimension(0, 32)));
        p.add(fieldRow("USERNAME", idField));
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(fieldRow("PASSWORD", passField));
        p.add(Box.createRigidArea(new Dimension(0, 10)));
        p.add(centerWrap(msg));
        p.add(Box.createRigidArea(new Dimension(0, 18)));
        p.add(centerWrap(btn));
        p.add(Box.createRigidArea(new Dimension(0, 18)));
        p.add(centerWrap(back));

        // Glass card wrapper
        JPanel glass = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 6));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                g2.setColor(new Color(255, 255, 255, 18));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 28, 28);
                g2.dispose();
            }
        };
        glass.setOpaque(false);
        glass.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        glass.add(p, BorderLayout.CENTER);
        return glass;
    }

    private JPanel fieldRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(new Color(148, 163, 184, 200));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.add(lbl);
        row.add(Box.createRigidArea(new Dimension(0, 5)));
        row.add(field);
        return row;
    }

    private JTextField glassField(int w, int h) {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(w, h));
        f.setPreferredSize(new Dimension(w, h));
        f.setBackground(new Color(30, 30, 50));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 22), 1, true),
            BorderFactory.createEmptyBorder(0, 14, 0, 14)
        ));
        return f;
    }

    private JPasswordField glassPassField(int w, int h) {
        JPasswordField f = new JPasswordField();
        f.setMaximumSize(new Dimension(w, h));
        f.setPreferredSize(new Dimension(w, h));
        f.setBackground(new Color(30, 30, 50));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 22), 1, true),
            BorderFactory.createEmptyBorder(0, 14, 0, 14)
        ));
        return f;
    }

    /** Purple→Indigo gradient button matching screenshot 1 */
    private JButton authorizeButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(79, 70, 229), getWidth(), 0, new Color(147, 51, 234));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(340, 55));
        b.setMaximumSize(new Dimension(340, 55));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    private JPanel centerWrap(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(c);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        return p;
    }
}
