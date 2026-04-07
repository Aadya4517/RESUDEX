package com.resudex.swing;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * HomePanel - Landing screen with background image and premium buttons.
 */
public class HomePanel extends JPanel {

    private Image bgImage;

    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 2, 33)); // #0D0221

        JPanel mainContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Deep Midnight Gradient Overlay
                GradientPaint gp = new GradientPaint(0, 0, new Color(13, 2, 45), 0, getHeight(), new Color(6, 0, 18));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Subtle Grid Pattern (Simulated)
                g2.setColor(new Color(0, 240, 255, 10)); // Cyan hint
                for (int i = 0; i < getWidth(); i += 40) g2.drawLine(i, 0, i, getHeight());
                for (int j = 0; j < getHeight(); j += 40) g2.drawLine(0, j, getWidth(), j);
                
                g2.dispose();
            }
        };
        mainContent.setLayout(new GridBagLayout());
        
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Branding Header
        JLabel branding = new JLabel("<html><font color='#00F0FF'>&#x26A1;</font> RESUDEX</html>");
        branding.setFont(new Font("SansSerif", Font.BOLD, 18));
        branding.setForeground(Color.WHITE);
        branding.setAlignmentX(Component.CENTER_ALIGNMENT);
        

        JLabel title = new JLabel("<html><center>LAUNCH YOUR<br>EXPERT CAREER</center></html>", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 72));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("<html><center>Precision resume matching — for roles that fit your skills, not just keywords.</center></html>", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        subtitle.setForeground(new Color(148, 163, 184)); // Slate-400
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(branding);
        card.add(Box.createRigidArea(new Dimension(0, 40)));
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 40)));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);

        JButton userBtn = createProPillButton("START YOUR JOURNEY →", true);
        JButton adminBtn = createProPillButton("RECRUITER ACCESS", false);

        userBtn.addActionListener(e -> ResudexApp.showUserLogin());
        adminBtn.addActionListener(e -> ResudexApp.showAdminLogin());

        btnPanel.add(userBtn);
        btnPanel.add(adminBtn);
        card.add(btnPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        mainContent.add(card, gbc);
        add(mainContent, BorderLayout.CENTER);
    }

    private JButton createProPillButton(String text, boolean primary) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (primary) {
                    GradientPaint gp = new GradientPaint(0, 0, new Color(0, 145, 178), getWidth(), 0, new Color(37, 99, 235));
                    g2.setPaint(gp);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                } else {
                    g2.setColor(new Color(13, 2, 33));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, getHeight(), getHeight());
                }
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(240, 50));
        return b;
    }
}
