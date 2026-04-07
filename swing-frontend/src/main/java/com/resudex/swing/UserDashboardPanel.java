package com.resudex.swing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.awt.datatransfer.DataFlavor;

/**
 * UserDashboardPanel – Premium sidebar layout with card-based job discovery.
 */
public class UserDashboardPanel extends JPanel {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private File selectedFile = null;
    private JLabel dropZoneLabel;
    private JButton uploadBtn;
    private JLabel resumeStatus;

    private JPanel jobListPanel; 
    private JTextField searchField;
    private JsonArray jobsData = new JsonArray();

    // Applications Section
    private JPanel appsListPanel;

    // Recommendations Section
    private JPanel recsListPanel;
    
    private JProgressBar profileStrength;
    private Timer notificationTimer;

    public UserDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 2, 33)); // Deep Voltage Violet

        // -------- Sidebar --------
        add(buildSidebar(), BorderLayout.WEST);

        // -------- Content Area (CardLayout) --------
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(buildJobRecommendations(), "RECS");
        contentPanel.add(buildMyApplications(), "APPS");
        contentPanel.add(buildResumeSync(),   "SYNC");
        contentPanel.add(buildProfileSection(), "PROF");

        add(contentPanel, BorderLayout.CENTER);

        // Default to Opportunities
        cardLayout.show(contentPanel, "RECS"); 

        // Load initial data
        loadRecommendations();
        loadMyApplications();
        startNotificationPolling();
    }

    private void startNotificationPolling() {
        notificationTimer = new Timer(8000, e -> {
            new Thread(() -> {
                JsonArray notes = ApiClient.getNotifications(ResudexApp.currentUserId);
                if (notes.size() > 0) {
                    for (int i=0; i<notes.size(); i++) {
                        JsonObject n = notes.get(i).getAsJsonObject();
                        int id = getInt(n, "id");
                        String msg = getString(n, "message");
                        
                        SwingUtilities.invokeLater(() -> {
                            ToastNotification.show(this, msg, true);
                            ApiClient.markNotificationRead(id);
                            loadMyApplications(); // Refresh to see status/feedback
                        });
                    }
                }
            }).start();
        });
        notificationTimer.start();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(240, 0));
        side.setBackground(new Color(23, 11, 59)); // Deep Indigo Sidebar
        side.setLayout(new BorderLayout());
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(48, 31, 95)));

        // Logo/Brand area
        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
        JLabel logoText = new JLabel("RESUDEX");
        logoText.setFont(new Font("SansSerif", Font.BOLD, 26));
        logoText.setForeground(new Color(0, 240, 255)); // Neon Cyan
        brand.add(logoText, BorderLayout.CENTER);

        // Nav buttons
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JButton btnRecs = createNavMsg("  Opportunities", "🎯");
        JButton btnApps = createNavMsg("  My Applications", "📋");
        JButton btnSync = createNavMsg("  Sync Resume", "📄");
        JButton btnProf = createNavMsg("  My Profile", "👤");
        
        JButton btnPdf = createNavMsg("  Export ATS PDF", "📄");
        btnPdf.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080/api/resume/export/" + ResudexApp.currentUserId));
            } catch (Exception ex) {}
        });
        
        btnRecs.addActionListener(e -> { cardLayout.show(contentPanel, "RECS"); loadRecommendations(); });
        btnApps.addActionListener(e -> { cardLayout.show(contentPanel, "APPS"); loadMyApplications(); });
        btnSync.addActionListener(e -> cardLayout.show(contentPanel, "SYNC"));
        btnProf.addActionListener(e -> cardLayout.show(contentPanel, "PROF"));

        nav.add(btnRecs);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnApps);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnSync);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnProf);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnPdf);

        // Bottom area (User + Logout)
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 20, 25, 20));
        
        JLabel userLbl = new JLabel("@" + ResudexApp.currentUsername);
        userLbl.setForeground(new Color(150, 170, 190));
        userLbl.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton logoutBtn = new JButton("Sign Out");
        logoutBtn.putClientProperty("JButton.buttonType", "borderless");
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.setForeground(new Color(255, 100, 100));
        logoutBtn.addActionListener(e -> ResudexApp.showHome());

        bottom.add(userLbl, BorderLayout.CENTER);
        bottom.add(logoutBtn, BorderLayout.SOUTH);

        side.add(brand, BorderLayout.NORTH);
        side.add(nav,   BorderLayout.CENTER);
        side.add(bottom, BorderLayout.SOUTH);

        return side;
    }

    private JButton createNavMsg(String text, String icon) {
        JButton b = new JButton(icon + text);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setForeground(new Color(176, 149, 246)); // Lavender
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.putClientProperty("JButton.buttonType", "borderless");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Job Explorer methods removed as they were replaced by 'Opportunities' section

    private JPanel createEmptyState(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(msg, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 18));
        l.setForeground(new Color(120, 130, 150));
        p.add(l);
        return p;
    }

    private JPanel createJobCard(JsonObject j) {
        String title = getString(j, "title", "TITLE");
        String desc  = getString(j, "description", "DESCRIPTION");
        int    id    = getInt(j, "id", "ID");
        int    score = getInt(j, "score", "SCORE");

        // Deep Analysis extractions
        String matchedStr = "";
        String missingStr = "";
        if (j.has("matchedSkills") && !j.get("matchedSkills").isJsonNull()) {
            JsonArray maArr = j.get("matchedSkills").getAsJsonArray();
            if (maArr.size() > 0) {
                matchedStr = maArr.toString().replace("\"", "").replace("[", "").replace("]", "").replace(",", ", ");
            }
        }
        if (j.has("missingSkills") && !j.get("missingSkills").isJsonNull()) {
            JsonArray miArr = j.get("missingSkills").getAsJsonArray();
            if (miArr.size() > 0) {
                missingStr = miArr.toString().replace("\"", "").replace("[", "").replace("]", "").replace(",", ", ");
            }
        }

        JPanel card = new JPanel(new BorderLayout(20, 10));
        card.setBackground(new Color(32, 21, 71)); // Dark violet card
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 42, 112), 1, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        // Title and Score Row
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 19));
        t.setForeground(new Color(0, 240, 255)); // Neon Cyan
        
        JLabel scoreBadge = new JLabel(score + "% MATCH");
        scoreBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        scoreBadge.setOpaque(true);
        scoreBadge.setForeground(Color.WHITE);
        scoreBadge.setBackground(score > 75 ? new Color(6, 214, 160) : (score > 30 ? new Color(255, 159, 28) : new Color(80, 80, 100)));
        scoreBadge.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        titleRow.add(t);
        titleRow.add(scoreBadge);

        JTextArea d = new JTextArea(desc);
        d.setOpaque(false);
        d.setEditable(false);
        d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        d.setForeground(new Color(220, 230, 245));
        d.setWrapStyleWord(true);
        d.setLineWrap(true);
        d.setRows(2);

        left.add(titleRow);
        left.add(Box.createVerticalStrut(8));
        left.add(d);

        // Analysis panel addition
        if (!matchedStr.isEmpty() || !missingStr.isEmpty()) {
            left.add(Box.createVerticalStrut(12));
            JPanel analysisPnl = new JPanel(new GridLayout(0, 1, 0, 4));
            analysisPnl.setOpaque(false);
            
            if (!matchedStr.isEmpty()) {
                JLabel matchL = new JLabel("✔ Matched: " + matchedStr);
                matchL.setFont(new Font("SansSerif", Font.BOLD, 12));
                matchL.setForeground(new Color(6, 214, 160));
                analysisPnl.add(matchL);
            } else {
                String mandatoryStr = j.has("mandatorySkills") ? j.get("mandatorySkills").getAsJsonArray().toString().replace("[","").replace("]","").replace("\"","") : "General Fit";
                JLabel goalL = new JLabel("🎯 Goal Skills: " + mandatoryStr);
                goalL.setFont(new Font("SansSerif", Font.ITALIC, 11));
                goalL.setForeground(new Color(150, 160, 180));
                analysisPnl.add(goalL);
            }
            if (!missingStr.isEmpty()) {
                JLabel missL = new JLabel("✖ Missing: " + missingStr);
                missL.setFont(new Font("SansSerif", Font.BOLD, 12));
                missL.setForeground(new Color(247, 37, 133));
                analysisPnl.add(missL);
            }
            left.add(analysisPnl);
        }

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);

        JButton apply = new JButton("Quick Apply");
        apply.setBackground(new Color(247, 37, 133)); // Neon Pink
        apply.setForeground(Color.WHITE);
        apply.setFont(new Font("SansSerif", Font.BOLD, 13));
        apply.putClientProperty("JButton.buttonType", "roundRect");
        apply.setPreferredSize(new Dimension(140, 40));

        JButton coverLetter = new JButton("✨ Cover Letter");
        coverLetter.setBackground(new Color(48, 31, 95));
        coverLetter.setForeground(new Color(176, 149, 246));
        coverLetter.setFont(new Font("SansSerif", Font.BOLD, 12));
        coverLetter.putClientProperty("JButton.buttonType", "roundRect");
        coverLetter.setPreferredSize(new Dimension(140, 35));

        coverLetter.addActionListener(e -> {
            new Thread(() -> {
                String letter = ApiClient.generateCoverLetter(ResudexApp.currentUserId, id);
                SwingUtilities.invokeLater(() -> showCoverLetterDialog(title, letter));
            }).start();
        });

        apply.addActionListener(e -> {
            QuizDialog qd = new QuizDialog((Frame)SwingUtilities.getWindowAncestor(this), title, desc);
            qd.setVisible(true);
            
            if (!qd.isCompleted()) return;

            int techScore = qd.getScore();
            apply.setEnabled(false);
            apply.setText("Applying...");
            new Thread(() -> {
                String err = ApiClient.applyForJob(ResudexApp.currentUserId, id, techScore);
                SwingUtilities.invokeLater(() -> {
                    if (err == null) {
                        apply.setText("Applied ✔");
                        ToastNotification.show(this, "Quiz Score: " + techScore + "/5 | Applied!", true);
                        loadMyApplications();
                    } else {
                        apply.setText("Apply");
                        apply.setEnabled(true);
                        ToastNotification.show(this, err, false);
                    }
                });
            }).start();
        });

        JPanel btnFlow = new JPanel(new GridLayout(2, 1, 0, 8));
        btnFlow.setOpaque(false);
        btnFlow.add(apply);
        btnFlow.add(coverLetter);

        right.add(btnFlow, BorderLayout.SOUTH);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    // ================== SECTION: MY APPLICATIONS ==================
    private JPanel buildMyApplications() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel h = new JLabel("My Applications");
        h.setFont(new Font("SansSerif", Font.BOLD, 28));
        h.setForeground(Color.WHITE);
        
        JPanel topContent = new JPanel(new BorderLayout());
        topContent.setOpaque(false);
        topContent.add(h, BorderLayout.WEST);
        topContent.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        p.add(topContent, BorderLayout.NORTH);

        appsListPanel = new JPanel();
        appsListPanel.setLayout(new BoxLayout(appsListPanel, BoxLayout.Y_AXIS));
        appsListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(appsListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void updateAppsList(JsonArray apps) {
        appsListPanel.removeAll();
        if (apps.size() == 0) {
            appsListPanel.add(createEmptyState("You haven't applied to any jobs yet."));
        } else {
            for (int i = 0; i < apps.size(); i++) {
                JsonObject j = apps.get(i).getAsJsonObject();
                appsListPanel.add(createAppCard(j));
                appsListPanel.add(Box.createVerticalStrut(20));
            }
        }
        appsListPanel.revalidate();
        appsListPanel.repaint();
    }

    private JPanel createAppCard(JsonObject j) {
        String title = getString(j, "title", "TITLE");
        String status = getString(j, "status", "STATUS", "APPLIED");
        String feedback = getString(j, "feedback", "FEEDBACK");

        JPanel card = new JPanel(new BorderLayout(20, 10));
        card.setBackground(new Color(32, 38, 48));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 90), 1, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 18));
        t.setForeground(new Color(0, 240, 255));

        left.add(t);
        left.add(Box.createVerticalStrut(5));
        
        JLabel dL = new JLabel("Application ID: #" + getString(j, "app_id", "APP_ID"));
        dL.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dL.setForeground(new Color(150, 160, 180));
        left.add(dL);

        if (feedback != null && !feedback.isBlank()) {
            left.add(Box.createVerticalStrut(10));
            JLabel fL = new JLabel("💬 Recruiter Feedback: " + feedback);
            fL.setFont(new Font("SansSerif", Font.ITALIC, 12));
            fL.setForeground(new Color(255, 190, 11)); // Neon Yellow
            left.add(fL);
        }

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);

        JLabel stLbl = new JLabel(status.toUpperCase());
        stLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        stLbl.setOpaque(true);
        stLbl.setHorizontalAlignment(SwingConstants.CENTER);
        stLbl.setPreferredSize(new Dimension(140, 35));
        
        if (status.equalsIgnoreCase("SELECTED")) {
            stLbl.setBackground(new Color(6, 214, 160));
            stLbl.setForeground(Color.BLACK);
        } else {
            stLbl.setBackground(new Color(60, 42, 112));
            stLbl.setForeground(Color.WHITE);
        }
        
        right.add(stLbl, BorderLayout.CENTER);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
    }

    private JPanel buildProfileSection() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(23, 11, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(48, 31, 95), 1, true),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(500, 500));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 0, 10, 0);
        c.gridx = 0; c.weightx = 1.0;

        JLabel head = new JLabel("My Profile Settings");
        head.setFont(new Font("SansSerif", Font.BOLD, 24));
        head.setForeground(new Color(0, 240, 255));
        c.gridy = 0; card.add(head, c);

        profileStrength = new JProgressBar(0, 100);
        profileStrength.setStringPainted(true);
        profileStrength.setString("Profile Strength: 0%");
        profileStrength.setForeground(new Color(6, 214, 160));
        profileStrength.setBackground(new Color(48, 31, 95));
        c.gridy = 1; card.add(profileStrength, c);
        c.gridy = 2; card.add(Box.createVerticalStrut(10), c);

        JTextField nameF = new JTextField();
        nameF.setPreferredSize(new Dimension(300, 40));
        nameF.putClientProperty("JTextField.placeholderText", "Your Full Name");

        JTextField emailF = new JTextField();
        emailF.setPreferredSize(new Dimension(300, 40));
        emailF.putClientProperty("JTextField.placeholderText", "Contact Email");

        JTextArea bioF = new JTextArea(4, 20);
        bioF.setLineWrap(true); bioF.setWrapStyleWord(true);
        bioF.putClientProperty("JTextField.placeholderText", "Brief professional bio...");
        JScrollPane scrollBio = new JScrollPane(bioF);

        JButton save = new JButton("UPDATE PROFILE");
        save.setBackground(new Color(247, 37, 133));
        save.setForeground(Color.WHITE);
        save.putClientProperty("JButton.buttonType", "roundRect");
        save.setPreferredSize(new Dimension(200, 45));

        c.gridy = 3; card.add(new JLabel("Full Name"), c);
        c.gridy = 4; card.add(nameF, c);
        c.gridy = 5; card.add(new JLabel("Email Address"), c);
        c.gridy = 6; card.add(emailF, c);
        c.gridy = 7; card.add(new JLabel("Professional Bio"), c);
        c.gridy = 8; card.add(scrollBio, c);
        c.gridy = 9; card.add(Box.createVerticalStrut(20), c);
        c.gridy = 10; card.add(save, c);

        p.add(card);

        // Load existing profile data
        new Thread(() -> {
            JsonObject profile = ApiClient.getUserProfile(ResudexApp.currentUserId);
            if (profile != null && !profile.has("error")) {
                SwingUtilities.invokeLater(() -> {
                    nameF.setText(getString(profile, "full_name"));
                    emailF.setText(getString(profile, "email"));
                    bioF.setText(getString(profile, "bio"));
                    updateProfileStrength(profile);
                });
            }
        }).start();

        save.addActionListener(e -> {
            save.setEnabled(false);
            new Thread(() -> {
                String err = ApiClient.updateUserProfile(ResudexApp.currentUserId, nameF.getText(), emailF.getText(), bioF.getText());
                SwingUtilities.invokeLater(() -> {
                    save.setEnabled(true);
                    if (err == null) {
                        ToastNotification.show(this, "Profile Updated ✔", true);
                        new Thread(() -> {
                            JsonObject p2 = ApiClient.getUserProfile(ResudexApp.currentUserId);
                            SwingUtilities.invokeLater(() -> updateProfileStrength(p2));
                        }).start();
                    }
                    else ToastNotification.show(this, err, false);
                });
            }).start();
        });

        return p;
    }

    private void updateProfileStrength(JsonObject p) {
        int strength = 0;
        if (getString(p, "full_name") != null && !getString(p, "full_name").isEmpty()) strength += 25;
        if (getString(p, "email") != null && !getString(p, "email").isEmpty()) strength += 25;
        if (getString(p, "bio") != null && !getString(p, "bio").isEmpty()) strength += 25;
        if (getString(p, "resume_text") != null && !getString(p, "resume_text").isEmpty()) strength += 25;
        
        profileStrength.setValue(strength);
        profileStrength.setString("Profile Strength: " + strength + "%");
    }

    // ================== SECTION: RESUME SYNC ==================
    private JPanel buildResumeSync() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JPanel dz = new JPanel(new GridBagLayout());
        dz.setBackground(new Color(23, 11, 59));
        dz.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(new Color(114, 9, 183), 2, 5, 2, true),
            BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));
        dz.setPreferredSize(new Dimension(500, 350));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.fill = GridBagConstraints.CENTER;

        JLabel icon = new JLabel("☁️");
        icon.setFont(new Font("SansSerif", Font.PLAIN, 64));
        icon.setForeground(new Color(100, 160, 255));

        dropZoneLabel = new JLabel("Select your Resume (PDF/DOCX)", SwingConstants.CENTER);
        dropZoneLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        dropZoneLabel.setForeground(new Color(200, 210, 230));

        JButton browse = new JButton("Browse Files");
        browse.putClientProperty("JButton.buttonType", "roundRect");
        browse.setPreferredSize(new Dimension(200, 45));

        uploadBtn = new JButton("SYNC PROFILE");
        uploadBtn.setBackground(new Color(0, 120, 215));
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.putClientProperty("JButton.buttonType", "roundRect");
        uploadBtn.setPreferredSize(new Dimension(200, 45));
        uploadBtn.setEnabled(false);

        resumeStatus = new JLabel(" ");
        resumeStatus.setFont(new Font("SansSerif", Font.ITALIC, 13));

        JProgressBar uploadProgress = new JProgressBar();
        uploadProgress.setIndeterminate(true);
        uploadProgress.setVisible(false);
        uploadProgress.setForeground(new Color(114, 9, 183));
        uploadProgress.setPreferredSize(new Dimension(300, 8));

        dz.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (!files.isEmpty()) {
                        selectedFile = files.get(0);
                        dropZoneLabel.setText("Ready: " + selectedFile.getName());
                        uploadBtn.setEnabled(true);
                        dz.setBackground(new Color(35, 45, 60));
                        return true;
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
                return false;
            }
        });

        browse.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedFile = jfc.getSelectedFile();
                dropZoneLabel.setText("Ready: " + selectedFile.getName());
                uploadBtn.setEnabled(true);
                dz.setBackground(new Color(35, 45, 60));
            }
        });

        uploadBtn.addActionListener(e -> {
            uploadBtn.setEnabled(false);
            uploadBtn.setText("Uploading...");
            uploadProgress.setVisible(true);
            dz.revalidate(); dz.repaint();
            new Thread(() -> {
                String err = ApiClient.uploadResume(ResudexApp.currentUserId, selectedFile);
                SwingUtilities.invokeLater(() -> {
                    uploadBtn.setEnabled(true);
                    uploadBtn.setText("SYNC PROFILE");
                    uploadProgress.setVisible(false);
                    if (err == null) {
                        resumeStatus.setText("✔ Resume successfully analyzed and synced.");
                        resumeStatus.setForeground(new Color(120, 255, 150));
                        ToastNotification.show(this, "Profile Synced & Analyzed!", true);
                        loadRecommendations(); // Refresh matched scores
                        
                        // Update Profile Strength immediately
                        new Thread(() -> {
                            JsonObject prof = ApiClient.getUserProfile(ResudexApp.currentUserId);
                            if (prof != null) SwingUtilities.invokeLater(() -> updateProfileStrength(prof));
                        }).start();
                        
                        // Idea 2: Load and show analytics
                        new Thread(() -> {
                            JsonObject analytics = ApiClient.getResumeAnalytics(ResudexApp.currentUserId);
                            SwingUtilities.invokeLater(() -> showAnalyticsDashboard(analytics));
                        }).start();
                    } else {
                        resumeStatus.setText("✖ Sync error: " + err);
                        resumeStatus.setForeground(new Color(255, 120, 120));
                        ToastNotification.show(this, "Upload Failed", false);
                    }
                });
            }).start();
        });

        c.gridy = 0; dz.add(icon, c);
        c.gridy = 1; dz.add(Box.createVerticalStrut(20), c);
        c.gridy = 2; dz.add(dropZoneLabel, c);
        c.gridy = 3; dz.add(Box.createVerticalStrut(20), c);
        c.gridy = 4; dz.add(uploadProgress, c);
        c.gridy = 5; dz.add(Box.createVerticalStrut(20), c);
        c.gridy = 6; dz.add(browse, c);
        c.gridy = 7; dz.add(Box.createVerticalStrut(15), c);
        c.gridy = 8; dz.add(uploadBtn, c);
        c.gridy = 9; dz.add(Box.createVerticalStrut(15), c);
        c.gridy = 10; dz.add(resumeStatus, c);

        p.add(dz);
        return p;
    }

    // loadJobs is deprecated in favor of loadRecommendations
    private void loadJobs() {
        loadRecommendations();
    }

    private void loadMyApplications() {
        new Thread(() -> {
            JsonArray apps = ApiClient.getUserApplications(ResudexApp.currentUserId);
            SwingUtilities.invokeLater(() -> {
                updateAppsList(apps);
            });
        }).start();
    }

    private void showAnalyticsDashboard(JsonObject an) {
        if (an == null || an.has("error")) return;

        JDialog diag = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Resume Insights", true);
        diag.setSize(600, 500);
        diag.setLocationRelativeTo(this);
        diag.getContentPane().setBackground(new Color(23, 11, 59));
        diag.setLayout(new BorderLayout(20, 20));

        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;

        JLabel title = new JLabel("AI Resume Analysis");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(new Color(0, 240, 255));
        c.gridy = 0; content.add(title, c);

        int exp = an.get("yearsOfExperience").getAsInt();
        JLabel expL = new JLabel("Estimated Experience: " + exp + " years");
        expL.setForeground(Color.WHITE);
        expL.setFont(new Font("SansSerif", Font.BOLD, 15));
        c.gridy = 1; c.insets = new Insets(10, 0, 20, 0); content.add(expL, c);

        JLabel domT = new JLabel("Domain Expertise Fit:");
        domT.setForeground(new Color(176, 149, 246));
        domT.setFont(new Font("SansSerif", Font.BOLD, 14));
        c.gridy = 2; c.insets = new Insets(0, 0, 5, 0); content.add(domT, c);

        JsonObject domainMap = an.getAsJsonObject("domainFit");
        int row = 3;
        for (String domain : domainMap.keySet()) {
            int val = domainMap.get(domain).getAsInt();
            if (val > 0) {
                JPanel barPnl = new JPanel(new BorderLayout(10, 0));
                barPnl.setOpaque(false);
                JLabel dL = new JLabel(domain);
                dL.setForeground(Color.LIGHT_GRAY);
                dL.setPreferredSize(new Dimension(120, 25));
                
                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue(val);
                bar.setStringPainted(true);
                bar.setForeground(new Color(6, 214, 160));
                bar.setBackground(new Color(48, 31, 95));
                
                barPnl.add(dL, BorderLayout.WEST);
                barPnl.add(bar, BorderLayout.CENTER);
                c.gridy = row++; c.insets = new Insets(2, 0, 2, 0);
                content.add(barPnl, c);
            }
        }

        diag.add(content, BorderLayout.CENTER);
        
        JButton close = new JButton("Awesome!");
        close.setBackground(new Color(247, 37, 133));
        close.setForeground(Color.WHITE);
        close.addActionListener(e -> diag.dispose());
        diag.add(close, BorderLayout.SOUTH);

        diag.setVisible(true);
    }

    // ================== SECTION: RECOMMENDATIONS ==================
    private JPanel buildJobRecommendations() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel h = new JLabel("Opportunities for You");
        h.setFont(new Font("SansSerif", Font.BOLD, 28));
        h.setForeground(new Color(0, 240, 255));

        JPanel topContent = new JPanel(new BorderLayout());
        topContent.setOpaque(false);
        topContent.add(h, BorderLayout.WEST);
        topContent.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        p.add(topContent, BorderLayout.NORTH);

        recsListPanel = new JPanel();
        recsListPanel.setLayout(new BoxLayout(recsListPanel, BoxLayout.Y_AXIS));
        recsListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(recsListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void loadRecommendations() {
        new Thread(() -> {
            JsonArray recs = ApiClient.getMatchedJobs(ResudexApp.currentUserId);
            SwingUtilities.invokeLater(() -> {
                JsonArray filtered = new JsonArray();
                for (int i=0; i<recs.size(); i++) {
                    JsonObject j = recs.get(i).getAsJsonObject();
                    // Display all jobs (>= 0) for verification
                    if (getInt(j, "score", "SCORE") >= 0) filtered.add(j);
                }
                
                // Sort by score descending (highest match first)
                java.util.List<JsonObject> list = new java.util.ArrayList<>();
                for (int i=0; i<filtered.size(); i++) list.add(filtered.get(i).getAsJsonObject());
                list.sort((a,b) -> getInt(b, "score", "SCORE") - getInt(a, "score", "SCORE"));
                
                JsonArray sorted = new JsonArray();
                for (JsonObject s : list) sorted.add(s);
                
                updateRecommendationList(sorted);
            });
        }).start();
    }

    private void updateRecommendationList(JsonArray jobs) {
        recsListPanel.removeAll();
        if (jobs.size() == 0) {
            recsListPanel.add(createEmptyState("No high-match roles yet! ⚡ Try syncing a detailed resume to unlock personalized 'Best Fits'."));
        } else {
            for (int i = 0; i < jobs.size(); i++) {
                recsListPanel.add(createJobCard(jobs.get(i).getAsJsonObject()));
                recsListPanel.add(Box.createVerticalStrut(20));
            }
        }
        recsListPanel.revalidate();
        recsListPanel.repaint();
    }

    private void showCoverLetterDialog(String jobTitle, String letter) {
        JDialog diag = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "AI Generated Cover Letter", true);
        diag.setSize(600, 650);
        diag.setLocationRelativeTo(this);
        diag.getContentPane().setBackground(new Color(13, 2, 33));
        diag.setLayout(new BorderLayout(15, 15));

        JTextArea text = new JTextArea(letter);
        text.setFont(new Font("Monospaced", Font.PLAIN, 13));
        text.setForeground(Color.WHITE);
        text.setBackground(new Color(23, 11, 59));
        text.setMargin(new Insets(20, 20, 20, 20));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);

        JScrollPane sp = new JScrollPane(text);
        sp.setBorder(BorderFactory.createLineBorder(new Color(48, 31, 95)));
        
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        p.add(new JLabel("<html><b style='color:#00F0FF; font-size:16px;'>Personalized for: " + jobTitle + "</b></html>"), BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);

        JButton copy = new JButton("Copy to Clipboard");
        copy.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(letter);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            ToastNotification.show(this, "Copied!", true);
        });

        diag.add(p, BorderLayout.CENTER);
        diag.add(copy, BorderLayout.SOUTH);
        diag.setVisible(true);
    }

    private String getString(JsonObject obj, String... keys) {
        if (obj == null) return null;
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) return obj.get(k).getAsString();
        }
        for (String actualKey : obj.keySet()) {
            for (String target : keys) {
                if (actualKey.equalsIgnoreCase(target) && !obj.get(actualKey).isJsonNull()) {
                    return obj.get(actualKey).getAsString();
                }
            }
        }
        return null;
    }

    private int getInt(JsonObject obj, String... keys) {
        if (obj == null) return 0;
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) {
                try { return obj.get(k).getAsInt(); } catch (Exception ignored) {}
            }
        }
        for (String actualKey : obj.keySet()) {
            for (String target : keys) {
                if (actualKey.equalsIgnoreCase(target) && !obj.get(actualKey).isJsonNull()) {
                    try { return obj.get(actualKey).getAsInt(); } catch (Exception ignored) {}
                }
            }
        }
        return 0;
    }
}
