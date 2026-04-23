package com.resudex.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;

/**
 * flashy alerts.
 */
public class ToastNotification extends JDialog {
    private String txt;
    private float al = 0f;
    private final float al_max = 0.9f;
    private Timer t_in;
    private Timer t_out;

    public static void pop(Component p, String txt, boolean ok) {
        new ToastNotification(p, txt, ok);
    }

    private ToastNotification(Component p, String txt, boolean ok) {
        this.txt = txt;
        
        setUndecorated(true);
        setAlwaysOnTop(true);
        setFocusableWindowState(false);
        setBackground(new Color(0, 0, 0, 0));

        JLabel lbl = new JLabel(txt);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(Color.WHITE);
        lbl.setOpaque(false);

        JPanel pan = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (ok) {
                    g2.setColor(new Color(40, 160, 110, (int) (al * 255))); 
                } else {
                    g2.setColor(new Color(220, 60, 60, (int) (al * 255))); 
                }
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 15, 15));
                g2.dispose();
            }
        };
        pan.setOpaque(false);
        pan.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        pan.add(lbl, BorderLayout.CENTER);

        add(pan);
        pack();

        // location
        Window win = SwingUtilities.getWindowAncestor(p);
        if (win != null) {
            int x = win.getX() + (win.getWidth() - getWidth()) / 2;
            int y = win.getY() + win.getHeight() - getHeight() - 50; 
            setLocation(x, y);
        } else {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (d.width - getWidth()) / 2;
            int y = d.height - getHeight() - 100;
            setLocation(x, y);
        }

        setVisible(true);
        run_fx();
    }

    private void run_fx() {
        t_in = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                al += 0.05f;
                if (al >= al_max) {
                    al = al_max;
                    t_in.stop();
                    hold();
                }
                repaint();
            }
        });
        t_in.start();
    }

    private void hold() {
        Timer t_wait = new Timer(2500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run_fade();
            }
        });
        t_wait.setRepeats(false);
        t_wait.start();
    }

    private void run_fade() {
        t_out = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                al -= 0.05f;
                if (al <= 0) {
                    al = 0;
                    t_out.stop();
                    dispose();
                }
                repaint();
            }
        });
        t_out.start();
    }
}
