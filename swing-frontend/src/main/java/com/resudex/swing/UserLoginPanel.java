package com.resudex.swing;

import javax.swing.*;
import java.awt.*;

/**
 * Handles sign in and sign up with glass look.
 */
public class UserLoginPanel extends JPanel {

    private final CardLayout card_lay = new CardLayout();
    private final JPanel cards = new JPanel(card_lay);

    public UserLoginPanel() {
        setLayout(new BorderLayout());

        cards.setOpaque(false);
        cards.add(get_in_panel(), "SIGNIN");
        cards.add(join_up_panel(), "SIGNUP");

        JPanel bg_box = new JPanel(new GridBagLayout()) {
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
        bg_box.add(cards);
        add(bg_box, BorderLayout.CENTER);
    }

    // sign in view
    private JPanel get_in_panel() {

        JTextField  uid_f   = new_box(320, 45);
        JPasswordField pwd_f = new_pass_box(320, 45);
        JLabel msg = get_msg_lbl();

        JButton btn = go_btn("SIGN IN \u2192");
        btn.addActionListener(e -> {
            String u = uid_f.getText().trim();
            String p = new String(pwd_f.getPassword()).trim();
            if (u.isEmpty() || p.isEmpty()) { msg.setText("Fill all fields."); return; }
            btn.setText("AUTHORIZING...");
            new Thread(() -> {
                int uid = ApiClient.do_login(u, p);
                SwingUtilities.invokeLater(() -> {
                    if (uid != -1) {
                        ResudexApp.uid = uid;
                        ResudexApp.go_user_pnl();
                    } else {
                        btn.setText("SIGN IN \u2192");
                        msg.setText("INVALID CREDENTIALS");
                    }
                });
            }).start();
        });

        JButton to_up = link_btn("NO ACCOUNT?  <font color='#00F0FF'><b>CREATE ACCOUNT</b></font>");
        to_up.addActionListener(e -> card_lay.show(cards, "SIGNUP"));

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(see_brand());
        p.add(Box.createRigidArea(new Dimension(0, 35)));
        p.add(make_lbl("welcome back", 42, Font.BOLD, Color.WHITE));
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(make_lbl("Sign in to your account.", 14, Font.PLAIN, new Color(148, 163, 184)));
        p.add(Box.createRigidArea(new Dimension(0, 35)));
        p.add(put_lbl_row("USERNAME", uid_f));
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(put_lbl_row("PASSWORD", pwd_f));
        p.add(Box.createRigidArea(new Dimension(0, 10)));
        p.add(mid_box(msg));
        p.add(Box.createRigidArea(new Dimension(0, 16)));
        p.add(mid_box(btn));
        p.add(Box.createRigidArea(new Dimension(0, 16)));
        p.add(mid_box(to_up));

        return wrap_glass(p);
    }

    // sign up view
    private JPanel join_up_panel() {

        JTextField  name_f  = new_box(320, 45);
        JTextField  mail_f = new_box(320, 45);
        JTextField  uid_f    = new_box(320, 45);
        JPasswordField pwd_f  = new_pass_box(320, 45);
        JLabel msg = get_msg_lbl();

        JButton btn = go_btn("CREATE ACCOUNT \u2192");
        btn.addActionListener(e -> {
            String f = name_f.getText().trim();
            String em = mail_f.getText().trim();
            String u = uid_f.getText().trim();
            String p = new String(pwd_f.getPassword()).trim();
            if (f.isEmpty() || em.isEmpty() || u.isEmpty() || p.isEmpty()) {
                msg.setText("All fields required.");
                return;
            }
            btn.setText("Creating...");
            new Thread(() -> {
                String err = ApiClient.add_usr(u, p, f, em);
                SwingUtilities.invokeLater(() -> {
                    btn.setText("CREATE ACCOUNT \u2192");
                    if (err == null) {
                        msg.setForeground(new Color(0, 240, 255));
                        msg.setText("Account created. Please sign in.");
                    } else {
                        msg.setForeground(new Color(255, 70, 70));
                        msg.setText(err);
                    }
                });
            }).start();
        });

        JButton to_in = link_btn("BACK TO  <font color='#00F0FF'><b>SIGN IN</b></font>");
        to_in.addActionListener(e -> card_lay.show(cards, "SIGNIN"));

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(see_brand());
        p.add(Box.createRigidArea(new Dimension(0, 28)));
        p.add(make_lbl("new account", 42, Font.BOLD, Color.WHITE));
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(make_lbl("Join RESUDEX.", 14, Font.PLAIN, new Color(148, 163, 184)));
        p.add(Box.createRigidArea(new Dimension(0, 28)));
        p.add(put_lbl_row("FULL NAME", name_f));
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(put_lbl_row("EMAIL", mail_f));
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(put_lbl_row("USERNAME", uid_f));
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(put_lbl_row("PASSWORD", pwd_f));
        p.add(Box.createRigidArea(new Dimension(0, 10)));
        p.add(mid_box(msg));
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(mid_box(btn));
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(mid_box(to_in));

        return wrap_glass(p);
    }

    private JPanel wrap_glass(JPanel content) {
        JPanel g_box = new JPanel(new BorderLayout()) {
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
        g_box.setOpaque(false);
        g_box.setBorder(BorderFactory.createEmptyBorder(40, 45, 40, 45));
        g_box.add(content, BorderLayout.CENTER);
        return g_box;
    }

    private JPanel put_lbl_row(String txt, JComponent field) {
        JPanel r = new JPanel();
        r.setOpaque(false);
        r.setLayout(new BoxLayout(r, BoxLayout.Y_AXIS));
        r.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(new Color(0, 240, 255, 180));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);

        field.setAlignmentX(Component.CENTER_ALIGNMENT);

        r.add(l);
        r.add(Box.createRigidArea(new Dimension(0, 5)));
        r.add(field);
        return r;
    }

    private JTextField new_box(int w, int h) {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(w, h));
        f.setPreferredSize(new Dimension(w, h));
        f.setBackground(new Color(255, 255, 255, 10));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 22), 1, true),
            BorderFactory.createEmptyBorder(0, 14, 0, 14)
        ));
        return f;
    }

    private JPasswordField new_pass_box(int w, int h) {
        JPasswordField f = new JPasswordField();
        f.setMaximumSize(new Dimension(w, h));
        f.setPreferredSize(new Dimension(w, h));
        f.setBackground(new Color(255, 255, 255, 10));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 22), 1, true),
            BorderFactory.createEmptyBorder(0, 14, 0, 14)
        ));
        return f;
    }

    private JButton go_btn(String txt) {
        JButton b = new JButton(txt) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(8, 145, 178), getWidth(), 0, new Color(37, 99, 235));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
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
        b.setPreferredSize(new Dimension(320, 50));
        b.setMaximumSize(new Dimension(320, 50));
        return b;
    }

    private JButton link_btn(String html) {
        JButton b = new JButton("<html>" + html + "</html>");
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(new Color(148, 163, 184));
        b.setFont(new Font("SansSerif", Font.PLAIN, 11));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    private JLabel get_msg_lbl() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(255, 70, 70));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel make_lbl(String txt, int size, int style, Color c) {
        JLabel l = new JLabel(txt, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(c);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel see_brand() {
        JLabel l = new JLabel("<html><font color='#00F0FF'>&#x26A1;</font> RESUDEX</html>", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(Color.WHITE);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JPanel mid_box(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(c);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        return p;
    }
}
