package com.resudex.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.imageio.ImageIO;
import java.io.File;

public class UserLoginPanel extends JPanel {
    
    // Core animation variables
    private boolean isLoginMode = true; 
    private Timer slideTimer;
    private int overlayX = 400; // Starting position (Right side)
    private final int MAX_X = 400;
    private final int MIN_X = 0;
    
    // UI Elements
    private JLayeredPane layeredPane;
    private JPanel signInForm;
    private JPanel signUpForm;
    private OverlayPanel overlayPanel;
    
    public UserLoginPanel() {
        setLayout(new GridBagLayout()); // For perfect centering
        setBackground(new Color(13, 2, 33)); // Match global theme background
        
        // --- Navigation Top Bar (Absolute positioning overlaid on background) ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JButton backBtn = new JButton("← Go Back");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        backBtn.setForeground(new Color(176, 149, 246));
        backBtn.putClientProperty("JButton.buttonType", "borderless");
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> ResudexApp.showHome());
        topBar.add(backBtn, BorderLayout.WEST);
        
        // --- Center Container Card ---
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 500));
        layeredPane.setMinimumSize(new Dimension(800, 500));
        layeredPane.setMaximumSize(new Dimension(800, 500));
        
        // 1. Sign In Form (Static on Left)
        signInForm = buildSignInForm();
        signInForm.setBounds(0, 0, 400, 500);
        
        // 2. Sign Up Form (Static on Right)
        signUpForm = buildSignUpForm();
        signUpForm.setBounds(400, 0, 400, 500);
        
        // 3. Sliding Overlay (Starts on Right covering Sign Up)
        overlayPanel = new OverlayPanel();
        overlayPanel.setBounds(overlayX, 0, 400, 500);
        
        // Add to layered pane with z-indices
        layeredPane.add(signInForm, Integer.valueOf(0));
        layeredPane.add(signUpForm, Integer.valueOf(0));
        layeredPane.add(overlayPanel, Integer.valueOf(1)); // On top
        
        // --- GridBag Assembly ---
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.NORTHWEST; c.weightx = 1.0; c.weighty = 0.0;
        c.insets = new Insets(20, 20, 0, 0);
        add(topBar, c);
        
        c.gridy = 1; c.anchor = GridBagConstraints.CENTER; c.weighty = 1.0; c.insets = new Insets(0, 0, 0, 0);
        add(layeredPane, c);
    }
    
    // Custom background painting
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, new Color(13, 2, 33), getWidth(), getHeight(), new Color(23, 11, 59));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
    }

    // ==========================================================
    // Sign In Form Factory
    // ==========================================================
    private JPanel buildSignInForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 0, 8, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        JLabel title = new JLabel("Sign In", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.BLACK);
        
        JTextField userField = createLightField("Email / Username");
        JPasswordField passField = createLightPassField("Password");
        
        JLabel msgLabel = new JLabel(" ", SwingConstants.CENTER);
        msgLabel.setForeground(Color.RED);
        msgLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        
        JButton btn = createOrangeBtn("SIGN IN");
        btn.addActionListener(e -> {
            String u = userField.getText().trim();
            String pwd = new String(passField.getPassword()).trim();
            if (u.isEmpty() || pwd.isEmpty()) {
                msgLabel.setText("Credentials required.");
                return;
            }
            btn.setText("VERIFYING...");
            btn.setEnabled(false);
            new Thread(() -> {
                int userId = ApiClient.login(u, pwd);
                SwingUtilities.invokeLater(() -> {
                    btn.setText("SIGN IN");
                    btn.setEnabled(true);
                    if (userId > 0) {
                        ResudexApp.currentUserId = userId;
                        ResudexApp.currentUsername = u;
                        ResudexApp.showUserDashboard();
                    } else {
                        msgLabel.setText("Invalid User or Password");
                    }
                });
            }).start();
        });
        
        c.gridy = 0; p.add(title, c);
        c.gridy = 1; c.insets = new Insets(30, 0, 8, 0); p.add(userField, c);
        c.gridy = 2; c.insets = new Insets(8, 0, 8, 0); p.add(passField, c);
        c.gridy = 3; p.add(msgLabel, c);
        c.gridy = 4; c.insets = new Insets(15, 0, 0, 0); p.add(btn, c);
        
        return p;
    }

    // ==========================================================
    // Sign Up Form Factory
    // ==========================================================
    private JPanel buildSignUpForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 0, 6, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        
        JLabel title = new JLabel("Create Account", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(Color.BLACK);
        
        JTextField nameField = createLightField("Username");
        JTextField emailField = createLightField("Email Address");
        JPasswordField passField = createLightPassField("Password");
        
        JLabel msgLabel = new JLabel(" ", SwingConstants.CENTER);
        msgLabel.setForeground(Color.RED);
        msgLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        
        JButton btn = createOrangeBtn("SIGN UP");
        btn.addActionListener(e -> {
            String u = nameField.getText().trim();
            String pwd = new String(passField.getPassword()).trim();
            if (u.isEmpty() || pwd.isEmpty()) {
                msgLabel.setText("Please fill out all fields.");
                return;
            }
            btn.setText("CREATING...");
            btn.setEnabled(false);
            new Thread(() -> {
                String error = ApiClient.register(u, pwd); // Mocking email param via global user handling
                SwingUtilities.invokeLater(() -> {
                    btn.setText("SIGN UP");
                    btn.setEnabled(true);
                    if (error == null) {
                        msgLabel.setForeground(new Color(40, 160, 100)); // Success green
                        msgLabel.setText("Account Created! Please Sign In.");
                        nameField.setText("");
                        passField.setText("");
                        // Optionally trigger slide animation to login
                    } else {
                        msgLabel.setForeground(Color.RED);
                        msgLabel.setText(error);
                    }
                });
            }).start();
        });
        
        c.gridy = 0; p.add(title, c);
        c.gridy = 1; c.insets = new Insets(20, 0, 6, 0); p.add(nameField, c);
        c.gridy = 2; c.insets = new Insets(6, 0, 6, 0); p.add(emailField, c);
        c.gridy = 3; p.add(passField, c);
        c.gridy = 4; p.add(msgLabel, c);
        c.gridy = 5; c.insets = new Insets(10, 0, 0, 0); p.add(btn, c);
        
        return p;
    }

    // ==========================================================
    // UI Utility Helpers (Light Theme for forms)
    // ==========================================================
    private JTextField createLightField(String placeholder) {
        JTextField f = new JTextField();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setPreferredSize(new Dimension(280, 42));
        f.putClientProperty("JComponent.roundRect", true);
        f.putClientProperty("JTextField.padding", new Insets(5, 10, 5, 10));
        return f;
    }
    
    private JPasswordField createLightPassField(String placeholder) {
        JPasswordField f = new JPasswordField();
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setPreferredSize(new Dimension(280, 42));
        f.putClientProperty("JComponent.roundRect", true);
        f.putClientProperty("JTextField.padding", new Insets(5, 10, 5, 10));
        return f;
    }

    private JButton createOrangeBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(new Color(255, 160, 0)); // The golden orange from image
        b.setForeground(Color.WHITE);
        b.putClientProperty("JButton.buttonType", "roundRect");
        b.setPreferredSize(new Dimension(180, 40));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPanel wrapperCenter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapperCenter.setOpaque(false);
        wrapperCenter.add(b);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }
    
    // ==========================================================
    // Overlay Panel (Smooth Sliding Gradient)
    // ==========================================================
    class OverlayPanel extends JPanel {
        
        private JLabel welcomeLbl;
        private JLabel descLbl;
        private JButton toggleBtn;
        
        public OverlayPanel() {
            setLayout(new GridBagLayout());
            setOpaque(false); // We paint gradient manually
            
            GridBagConstraints c = new GridBagConstraints();
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.fill = GridBagConstraints.NONE;
            c.insets = new Insets(10, 0, 10, 0);
            
            welcomeLbl = new JLabel("Welcome, Friend!", SwingConstants.CENTER);
            welcomeLbl.setFont(new Font("SansSerif", Font.BOLD, 26));
            welcomeLbl.setForeground(Color.WHITE);
            
            descLbl = new JLabel("<html><div style='text-align: center;'>Enter your personal details to use<br>all of site features</div></html>", SwingConstants.CENTER);
            descLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
            descLbl.setForeground(new Color(230, 230, 240));
            
            toggleBtn = new JButton("SIGN UP");
            toggleBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
            toggleBtn.setForeground(Color.WHITE);
            // Outline style button
            toggleBtn.setBackground(new Color(255, 255, 255, 50));
            toggleBtn.putClientProperty("JButton.buttonType", "roundRect");
            toggleBtn.setPreferredSize(new Dimension(160, 40));
            toggleBtn.setContentAreaFilled(false);
            toggleBtn.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true));
            toggleBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            
            toggleBtn.addActionListener(e -> toggleAnimation());
            
            add(welcomeLbl, c);
            add(descLbl, c);
            c.insets = new Insets(25, 0, 0, 0);
            add(toggleBtn, c);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Advanced purple gradient from the UI mockup
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(247, 37, 133), // Primary Pink
                getWidth(), getHeight(), new Color(60, 42, 112) // Deep Purple
            );
            g2.setPaint(gp);
            // Draw rounded shape (since card has rounded corners)
            // If the card is fully rounded, we must adapt. We will do 20px radius.
            // Based on slide position, we might only round specific corners but let's keep it simple.
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
        }
        
        public void updateContent(boolean isLogin) {
            if (isLogin) { // Meaning we are showing Sign In, overlay is on RIGHT
                welcomeLbl.setText("Welcome, Friend!");
                descLbl.setText("<html><div style='text-align: center;'>Enter your personal details to use<br>all of site features</div></html>");
                toggleBtn.setText("SIGN UP");
            } else { // Overlay is on LEFT
                welcomeLbl.setText("Welcome Back!");
                descLbl.setText("<html><div style='text-align: center;'>Enter your personal details to use<br>all of site features</div></html>");
                toggleBtn.setText("SIGN IN");
            }
        }
    }
    
    // ==========================================================
    // Core Animation Engine
    // ==========================================================
    private void toggleAnimation() {
        if (slideTimer != null && slideTimer.isRunning()) return; // block spam clicks
        
        final int targetX = isLoginMode ? MIN_X : MAX_X; // if on right (400), move to 0.
        // We update text mid-flight or at start for illusion
        overlayPanel.updateContent(!isLoginMode);

        int jumpDelta = 20; // Animation speed/steps
        slideTimer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isLoginMode) {
                    overlayX -= jumpDelta; // Slide Left
                    if (overlayX <= targetX) {
                        overlayX = targetX;
                        isLoginMode = false;
                        slideTimer.stop();
                    }
                } else {
                    overlayX += jumpDelta; // Slide Right
                    if (overlayX >= targetX) {
                        overlayX = targetX;
                        isLoginMode = true;
                        slideTimer.stop();
                    }
                }
                overlayPanel.setBounds(overlayX, 0, 400, 500);
            }
        });
        slideTimer.start();
    }
}

