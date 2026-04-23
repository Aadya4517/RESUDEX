package com.resudex.swing;

import javax.swing.*;
import java.awt.*;

/**
 * Landing screen. big text, big energy.
 */
public class HomePanel extends JPanel {

    public HomePanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 2, 33)); // voltage violet

        JPanel box_mid = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // midnight vibes
                GradientPaint gp = new GradientPaint(0, 0, new Color(13, 2, 45), 0, getHeight(), new Color(6, 0, 18));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // grid pattern
                g2.setColor(new Color(0, 240, 255, 10)); 
                for (int i = 0; i < getWidth(); i += 40) g2.drawLine(i, 0, i, getHeight());
                for (int j = 0; j < getHeight(); j += 40) g2.drawLine(0, j, getWidth(), j);
                
                g2.dispose();
            }
        };
        box_mid.setLayout(new GridBagLayout());
        
        JPanel hero = new JPanel();
        hero.setOpaque(false);
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));

        // brand
        JLabel lbl_logo = new JLabel("<html><font color='#00F0FF'>&#x26A1;</font> RESUDEX</html>");
        lbl_logo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl_logo.setForeground(Color.WHITE);
        lbl_logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl_h1 = new JLabel("<html><center>LAUNCH YOUR<br>EXPERT CAREER</center></html>", SwingConstants.CENTER);
        lbl_h1.setFont(new Font("SansSerif", Font.BOLD, 72));
        lbl_h1.setForeground(Color.WHITE);
        lbl_h1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl_sub = new JLabel("<html><center>Precision resume matching — for roles that fit your skills, not just keywords.</center></html>", SwingConstants.CENTER);
        lbl_sub.setFont(new Font("SansSerif", Font.PLAIN, 18));
        lbl_sub.setForeground(new Color(148, 163, 184));
        lbl_sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        hero.add(lbl_logo);
        hero.add(Box.createRigidArea(new Dimension(0, 40)));
        hero.add(lbl_h1);
        hero.add(Box.createRigidArea(new Dimension(0, 20)));
        hero.add(lbl_sub);
        hero.add(Box.createRigidArea(new Dimension(0, 40)));

        JPanel box_btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        box_btns.setOpaque(false);

        JButton b_go = make_pill("START YOUR JOURNEY →", true);
        JButton b_pro = make_pill("RECRUITER ACCESS", false);

        b_go.addActionListener(e -> ResudexApp.go_user_in());
        b_pro.addActionListener(e -> ResudexApp.go_admin_in());

        box_btns.add(b_go);
        box_btns.add(b_pro);
        hero.add(box_btns);

        GridBagConstraints gbc = new GridBagConstraints();
        box_mid.add(hero, gbc);
        add(box_mid, BorderLayout.CENTER);
    }

    private JButton make_pill(String txt, boolean top) {
        JButton b = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (top) {
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
