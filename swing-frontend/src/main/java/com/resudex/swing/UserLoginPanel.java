package com.resudex.swing;

import javax.swing.*;
import java.awt.*;

/**
 * "welcome back" sign-in + "new account" sign-up with glassmorphic cards.
 */
public class UserLoginPanel extends JPanel {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    public UserLoginPanel() {
        setLayout(new BorderLayout());

        cards.setOpaque(false);
        cards.add(createSignInPanel(), "SIGNIN");
        cards.add(createSignUpPanel(), "SIGNUP");

        // Background panel with gradient + subtle grid
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
        main.add(cards);
        add(main, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Sign In panel  ("welcome back")
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createSignInPanel() {

        JTextField  idField   = glassField(320, 45);
        JPasswordField passField = glassPassField(320, 45);
        JLabel msg = msgLabel();

        JButton btn = pillButton("SIGN IN \u2192");
        btn.addActionListener(e -> {
            String u    = idField.getText().trim();
            String pStr = new String(passField.getPassword()).trim();
            if (u.isEmpty() || pStr.isEmpty()) { msg.setText("Fill all fields."); return; }
            btn.setText("AUTHORIZING...");
            new Thread(() -> {
                int uid = ApiClient.login(u, pStr);
                SwingUtilities.invokeLater(() -> {
                    if (uid != -1) {
                        ResudexApp.currentUserId = uid;
                        ResudexApp.showUserDashboard();
                    } else {
                        btn.setText("SIGN IN \u2192");
                        msg.setText("INVALID CREDENTIALS");
                    }
                });
            }).start();
        });

        JButton toSignup = linkButton("NO ACCOUNT?  <font color='#00F0FF'><b>CREATE ACCOUNT</b></font>");
        toSignup.addActionListener(e -> cardLayout.show(cards, "SIGNUP"));

        // Layout
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(brandingLabel());
        p.add(Box.createRigidArea(new Dimension(0, 35)));
        p.add(centeredLabel("welcome back", 42, Font.BOLD, Color.WHITE));
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(centeredLabel("Sign in to your account.", 14, Font.PLAIN, new Color(148, 163, 184)));
        p.add(Box.createRigidArea(new Dimension(0, 35)));
        p.add(labeledRow("USERNAME", idField));
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(labeledRow("PASSWORD", passField));
        p.add(Box.createRigidArea(new Dimension(0, 10)));
        p.add(center(msg));
        p.add(Box.createRigidArea(new Dimension(0, 16)));
        p.add(center(btn));
        p.add(Box.createRigidArea(new Dimension(0, 16)));
        p.add(center(toSignup));

        return glassCard(p);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Sign Up panel  ("new account")
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel createSignUpPanel() {

        JTextField  nameField  = glassField(320, 45);
        JTextField  emailField = glassField(320, 45);
        JTextField  idField    = glassField(320, 45);
        JPasswordField passField  = glassPassField(320, 45);
        JLabel msg = msgLabel();

        JButton btn = pillButton("CREATE ACCOUNT \u2192");
        btn.addActionListener(e -> {
            String f    = nameField.getText().trim();
            String em   = emailField.getText().trim();
            String u    = idField.getText().trim();
            String pStr = new String(passField.getPassword()).trim();
            if (f.isEmpty() || em.isEmpty() || u.isEmpty() || pStr.isEmpty()) {
                msg.setText("All fields required.");
                return;
            }
            btn.setText("Creating...");
            new Thread(() -> {
                String err = ApiClient.register(u, pStr, f, em);
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

        JButton toSignin = linkButton("BACK TO  <font color='#00F0FF'><b>SIGN IN</b></font>");
        toSignin.addActionListener(e -> cardLayout.show(cards, "SIGNIN"));

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(brandingLabel());
        p.add(Box.createRigidArea(new Dimension(0, 28)));
        p.add(centeredLabel("new account", 42, Font.BOLD, Color.WHITE));
        p.add(Box.createRigidArea(new Dimension(0, 6)));
        p.add(centeredLabel("Join RESUDEX.", 14, Font.PLAIN, new Color(148, 163, 184)));
        p.add(Box.createRigidArea(new Dimension(0, 28)));
        p.add(labeledRow("FULL NAME", nameField));
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(labeledRow("EMAIL", emailField));
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(labeledRow("USERNAME", idField));
        p.add(Box.createRigidArea(new Dimension(0, 12)));
        p.add(labeledRow("PASSWORD", passField));
        p.add(Box.createRigidArea(new Dimension(0, 10)));
        p.add(center(msg));
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(center(btn));
        p.add(Box.createRigidArea(new Dimension(0, 14)));
        p.add(center(toSignin));

        return glassCard(p);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Wraps content in translucent glassmorphic card */
    private JPanel glassCard(JPanel content) {
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
        glass.setBorder(BorderFactory.createEmptyBorder(40, 45, 40, 45));
        glass.add(content, BorderLayout.CENTER);
        return glass;
    }

    /** A labeled field row:  CYAN LABEL above the field */
    private JPanel labeledRow(String labelText, JComponent field) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setForeground(new Color(0, 240, 255, 180));
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

    private JPasswordField glassPassField(int w, int h) {
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

    private JButton pillButton(String text) {
        JButton b = new JButton(text) {
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

    private JButton linkButton(String html) {
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

    private JLabel msgLabel() {
        JLabel l = new JLabel(" ", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        l.setForeground(new Color(255, 70, 70));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel centeredLabel(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel brandingLabel() {
        JLabel l = new JLabel("<html><font color='#00F0FF'>&#x26A1;</font> RESUDEX</html>", SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.BOLD, 18));
        l.setForeground(Color.WHITE);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    /** Centers a component in a transparent row */
    private JPanel center(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        p.setOpaque(false);
        p.add(c);
        p.setAlignmentX(Component.CENTER_ALIGNMENT);
        return p;
    }

    }
