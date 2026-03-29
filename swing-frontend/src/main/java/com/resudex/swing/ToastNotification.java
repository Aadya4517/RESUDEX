package com.resudex.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

public class ToastNotification extends JDialog {
    private String message;
    private float opacity = 0f;
    private final float maxOpacity = 0.9f;
    private Timer fadeInTimer;
    private Timer fadeOutTimer;

    public static void show(Component parent, String message, boolean isSuccess) {
        new ToastNotification(parent, message, isSuccess);
    }

    private ToastNotification(Component parent, String message, boolean isSuccess) {
        this.message = message;
        
        setUndecorated(true);
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setBackground(new Color(0, 0, 0, 0));

        JLabel label = new JLabel(message);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setOpaque(false);

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSuccess) {
                    g2.setColor(new Color(40, 160, 110, (int) (opacity * 255))); // Green
                } else {
                    g2.setColor(new Color(220, 60, 60, (int) (opacity * 255))); // Red
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        panel.add(label, BorderLayout.CENTER);

        add(panel);
        pack();

        // Compute location
        Window window = SwingUtilities.getWindowAncestor(parent);
        if (window != null) {
            int x = window.getX() + (window.getWidth() - getWidth()) / 2;
            int y = window.getY() + window.getHeight() - getHeight() - 50; // Near bottom
            setLocation(x, y);
        } else {
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (dim.width - getWidth()) / 2;
            int y = dim.height - getHeight() - 100;
            setLocation(x, y);
        }

        setVisible(true);
        startAnimations();
    }

    private void startAnimations() {
        fadeInTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity += 0.05f;
                if (opacity >= maxOpacity) {
                    opacity = maxOpacity;
                    fadeInTimer.stop();
                    startWait();
                }
                repaint();
            }
        });
        fadeInTimer.start();
    }

    private void startWait() {
        Timer waitTimer = new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startFadeOut();
            }
        });
        waitTimer.setRepeats(false);
        waitTimer.start();
    }

    private void startFadeOut() {
        fadeOutTimer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.05f;
                if (opacity <= 0) {
                    opacity = 0;
                    fadeOutTimer.stop();
                    dispose();
                }
                repaint();
            }
        });
        fadeOutTimer.start();
    }
}
