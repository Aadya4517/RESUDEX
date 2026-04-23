package com.resudex.swing;

import javax.swing.*;
import java.awt.*;

/**
 * Recruiter login screen.
 * Purple glow buttons and glass finish.
 */
public class AdminLoginPanel extends JPanel {

    public AdminLoginPanel() {
        setLayout(new BorderLayout());

        JPanel main_bg = new JPanel(new GridBagLayout()) {
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
        main_bg.add(make_admin_card());
        add(main_bg, BorderLayout.CENTER);
    }

    private JPanel make_admin_card() {
        JTextField     uid_f = new_text_f(340, 50);
        uid_f.setText("admin");
        JPasswordField pwd_f = new_pass_f(340, 50);
        pwd_f.setText("admin123");
        JLabel         err_lbl = new JLabel(" ", SwingConstants.CENTER);
        err_lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        err_lbl.setForeground(new Color(255, 70, 70));
        err_lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton go_btn = auth_btn("AUTHORIZE  \u2192");
        go_btn.addActionListener(e -> {
            String u = uid_f.getText().trim();
            String p = new String(pwd_f.getPassword()).trim();
            if (u.isEmpty() || p.isEmpty()) { err_lbl.setText("Authorization required."); return; }
            go_btn.setText("VERIFYING...");
            new Thread(() -> {
                boolean ok = ApiClient.admin_auth(u, p);
                SwingUtilities.invokeLater(() -> {
                    if (ok) {
                        ResudexApp.go_admin_pnl();
                    } else {
                        go_btn.setText("AUTHORIZE  \u2192");
                        err_lbl.setText("ACCESS DENIED");
                    }
                });
            }).start();
        });

        JButton ret_btn = new JButton("\u2190 RETURN TO FRONTEND");
        ret_btn.setContentAreaFilled(false);
        ret_btn.setBorderPainted(false);
        ret_btn.setFocusPainted(false);
        ret_btn.setForeground(new Color(148, 163, 184));
        ret_btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ret_btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ret_btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        ret_btn.addActionListener(e -> ResudexApp.go_home());

        JPanel p_lay = new JPanel();
        p_lay.setOpaque(false);
        p_lay.setLayout(new BoxLayout(p_lay, BoxLayout.Y_AXIS));

        JLabel brand = new JLabel("<html>&#x1F6E1; RESUDEX &nbsp;<font color='#94A3B8'>ADMIN</font></html>", SwingConstants.CENTER);
        brand.setFont(new Font("SansSerif", Font.BOLD, 22));
        brand.setForeground(Color.WHITE);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel head = new JLabel("Recruiter Access", SwingConstants.CENTER);
        head.setFont(new Font("SansSerif", Font.BOLD, 38));
        head.setForeground(Color.WHITE);
        head.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub_txt = new JLabel("<html><center>Secure portal for candidate selection and job management.</center></html>", SwingConstants.CENTER);
        sub_txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub_txt.setForeground(new Color(148, 163, 184));
        sub_txt.setAlignmentX(Component.CENTER_ALIGNMENT);

        p_lay.add(brand);
        p_lay.add(Box.createRigidArea(new Dimension(0, 34)));
        p_lay.add(head);
        p_lay.add(Box.createRigidArea(new Dimension(0, 8)));
        p_lay.add(sub_txt);
        p_lay.add(Box.createRigidArea(new Dimension(0, 32)));
        p_lay.add(put_row("USERNAME", uid_f));
        p_lay.add(Box.createRigidArea(new Dimension(0, 14)));
        p_lay.add(put_row("PASSWORD", pwd_f));
        p_lay.add(Box.createRigidArea(new Dimension(0, 10)));
        p_lay.add(wrap_mid(err_lbl));
        p_lay.add(Box.createRigidArea(new Dimension(0, 18)));
        p_lay.add(wrap_mid(go_btn));
        p_lay.add(Box.createRigidArea(new Dimension(0, 18)));
        p_lay.add(wrap_mid(ret_btn));

        JPanel glass_box = new JPanel(new BorderLayout()) {
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
        glass_box.setOpaque(false);
        glass_box.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        glass_box.add(p_lay, BorderLayout.CENTER);
        return glass_box;
    }

    private JPanel put_row(String lbl_txt, JComponent f) {
        JPanel r = new JPanel();
        r.setOpaque(false);
        r.setLayout(new BoxLayout(r, BoxLayout.Y_AXIS));
        r.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l = new JLabel(lbl_txt);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(148, 163, 184, 200));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        f.setAlignmentX(Component.CENTER_ALIGNMENT);
        r.add(l);
        r.add(Box.createRigidArea(new Dimension(0, 5)));
        r.add(f);
        return r;
    }

    private JTextField new_text_f(int w, int h) {
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

    private JPasswordField new_pass_f(int w, int h) {
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

    private JButton auth_btn(String txt) {
        JButton b = new JButton(txt) {
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

    private JPanel wrap_mid(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(c);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        return p;
    }
}
