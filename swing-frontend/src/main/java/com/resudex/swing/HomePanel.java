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
        try {
            // Updated to use the new premium background
            bgImage = ImageIO.read(new File("C:/Users/aadya/.gemini/antigravity/brain/01fc3abf-5bbf-4201-9d27-6f8aa89cfb34/premium_home_bg_1774783473988.png"));
        } catch (Exception e) {
            System.err.println("Could not load premium_home_bg: " + e.getMessage());
        }

        JPanel mainContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                    // Premium dark overlay for text contrast
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(new Color(15, 20, 30, 160));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                } else {
                    super.paintComponent(g);
                    setBackground(new Color(18, 20, 24));
                }
            }
        };
        mainContent.setLayout(new GridBagLayout());
        
        JPanel card = new JPanel();
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("RESUDEX", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 80));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Professional Resume Intelligence & Recruitment", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 20));
        subtitle.setForeground(new Color(180, 200, 220));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 50)));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        btnPanel.setOpaque(false);

        JButton userBtn = createPremiumButton("APPLICANT LOGIN", "Job Seeker Access", new Color(0, 120, 215));
        JButton adminBtn = createPremiumButton("RECRUITER ACCESS", "Admin Dashboard", new Color(40, 160, 100));

        userBtn.addActionListener(e -> ResudexApp.showUserLogin());
        adminBtn.addActionListener(e -> ResudexApp.showAdminLogin());

        btnPanel.add(userBtn);
        btnPanel.add(adminBtn);
        card.add(btnPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        mainContent.add(card, gbc);

        add(mainContent, BorderLayout.CENTER);
    }

    private JButton createPremiumButton(String main, String sub, Color color) {
        JButton b = new JButton("<html><center><b>" + main + "</b><br><font size='3' color='#cccccc'>" + sub + "</font></center></html>");
        b.setPreferredSize(new Dimension(280, 110));
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }
}
