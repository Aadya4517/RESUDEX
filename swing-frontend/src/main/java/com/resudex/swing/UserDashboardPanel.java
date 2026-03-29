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
    private JsonArray appsData = new JsonArray();

    public UserDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 2, 33)); // Deep Voltage Violet

        // -------- Sidebar --------
        add(buildSidebar(), BorderLayout.WEST);

        // -------- Content Area (CardLayout) --------
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(buildJobExplorer(), "JOBS");
        contentPanel.add(buildMyApplications(), "APPS");
        contentPanel.add(buildResumeSync(),   "SYNC");

        add(contentPanel, BorderLayout.CENTER);

        // Load initial data
        loadJobs();
        loadMyApplications();
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

        JButton btnJobs = createNavMsg("  Explore Jobs", "💼");
        JButton btnApps = createNavMsg("  My Applications", "📋");
        JButton btnSync = createNavMsg("  Sync Resume", "📄");
        
        btnJobs.addActionListener(e -> { cardLayout.show(contentPanel, "JOBS"); loadJobs(); });
        btnApps.addActionListener(e -> { cardLayout.show(contentPanel, "APPS"); loadMyApplications(); });
        btnSync.addActionListener(e -> cardLayout.show(contentPanel, "SYNC"));

        nav.add(btnJobs);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnApps);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnSync);

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

    // ================== SECTION: JOB EXPLORER ==================
    private JPanel buildJobExplorer() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel h = new JLabel("Available Opportunities");
        h.setFont(new Font("SansSerif", Font.BOLD, 28));
        h.setForeground(Color.WHITE);
        
        // Search bar
        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(300, 40));
        searchField.putClientProperty("JTextField.placeholderText", "Search by title or keyword...");
        searchField.addActionListener(e -> filterJobs());
        // Simple real-time filtering
        searchField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                filterJobs();
            }
        });

        JPanel topContent = new JPanel(new BorderLayout());
        topContent.setOpaque(false);
        topContent.add(h, BorderLayout.WEST);
        topContent.add(searchField, BorderLayout.EAST);
        topContent.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        p.add(topContent, BorderLayout.NORTH);

        jobListPanel = new JPanel();
        jobListPanel.setLayout(new BoxLayout(jobListPanel, BoxLayout.Y_AXIS));
        jobListPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(jobListPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    private void filterJobs() {
        String filter = searchField.getText().toLowerCase();
        JsonArray filtered = new JsonArray();
        for (int i = 0; i < jobsData.size(); i++) {
            JsonObject j = jobsData.get(i).getAsJsonObject();
            String title = getString(j, "title", "TITLE");
            String desc  = getString(j, "description", "DESCRIPTION");
            if (title == null) title = "";
            if (desc == null) desc = "";
            
            if (title.toLowerCase().contains(filter) || desc.toLowerCase().contains(filter)) {
                filtered.add(j);
            }
        }
        updateJobList(filtered);
    }

    private void updateJobList(JsonArray jobs) {
        jobListPanel.removeAll();
        if (jobs.size() == 0) {
            jobListPanel.add(createEmptyState("No opportunities found matching this search."));
        } else {
            for (int i = 0; i < jobs.size(); i++) {
                JsonObject j = jobs.get(i).getAsJsonObject();
                jobListPanel.add(createJobCard(j));
                jobListPanel.add(Box.createVerticalStrut(20));
            }
        }
        jobListPanel.revalidate();
        jobListPanel.repaint();
    }

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
        int    score = getInt(j, "score", "SCORE", "-1"); // -1 if not found
        if (score == 0 && !j.has("score") && !j.has("SCORE")) score = -1;

        // Deep Analysis extractions
        String matchedStr = "";
        String missingStr = "";
        if (j.has("matchedSkills")) {
            matchedStr = j.get("matchedSkills").getAsJsonArray().toString().replace("\"", "").replace("[", "").replace("]", "").replace(",", ", ");
        }
        if (j.has("missingSkills") && !j.get("missingSkills").isJsonNull()) {
            missingStr = j.get("missingSkills").getAsJsonArray().toString().replace("\"", "").replace("[", "").replace("]", "").replace(",", ", ");
        }

        JPanel card = new JPanel(new BorderLayout(20, 10));
        card.setBackground(new Color(32, 21, 71)); // Dark violet card
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 42, 112), 1, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 19));
        t.setForeground(new Color(0, 240, 255)); // Neon Cyan

        JTextArea d = new JTextArea(desc);
        d.setOpaque(false);
        d.setEditable(false);
        d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        d.setForeground(new Color(220, 230, 245));
        d.setWrapStyleWord(true);
        d.setLineWrap(true);
        d.setRows(2);

        left.add(t);
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
            }
            if (!missingStr.isEmpty()) {
                JLabel missL = new JLabel("✖ Missing: " + missingStr);
                missL.setFont(new Font("SansSerif", Font.BOLD, 12));
                missL.setForeground(new Color(247, 37, 133));
                analysisPnl.add(missL);
            }
            left.add(analysisPnl);
        }

        // Smart Match Score Badge
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);

        if (score >= 0) {
            JLabel scoreLbl = new JLabel(score + "% MATCH");
            scoreLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            scoreLbl.setOpaque(true);
            scoreLbl.setHorizontalAlignment(SwingConstants.CENTER);
            scoreLbl.setPreferredSize(new Dimension(100, 25));
            
            if (score > 80) {
                scoreLbl.setBackground(new Color(6, 214, 160)); // Neon Teal
                scoreLbl.setForeground(Color.BLACK);
            } else if (score > 50) {
                scoreLbl.setBackground(new Color(255, 190, 11)); // Neon Yellow
                scoreLbl.setForeground(Color.BLACK);
            } else {
                scoreLbl.setBackground(new Color(60, 42, 112)); // Dim Violet
                scoreLbl.setForeground(new Color(200, 200, 200));
            }
            right.add(scoreLbl, BorderLayout.NORTH);
        }

        JButton apply = new JButton("Quick Apply");
        apply.setBackground(new Color(247, 37, 133)); // Neon Pink
        apply.setForeground(Color.WHITE);
        apply.setFont(new Font("SansSerif", Font.BOLD, 13));
        apply.putClientProperty("JButton.buttonType", "roundRect");
        apply.setPreferredSize(new Dimension(140, 40));

        apply.addActionListener(e -> {
            apply.setEnabled(false);
            apply.setText("Applying...");
            new Thread(() -> {
                String err = ApiClient.applyForJob(ResudexApp.currentUserId, id);
                SwingUtilities.invokeLater(() -> {
                    if (err == null) {
                        apply.setText("Applied ✔");
                        ToastNotification.show(this, "Application Submitted Successfully!", true);
                        loadJobs(); // Refresh statuses
                    } else {
                        apply.setText("Applied ✔");
                        ToastNotification.show(this, "You have already applied for this job.", false);
                    }
                });
            }).start();
        });

        right.add(apply, BorderLayout.SOUTH);

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
        String desc  = getString(j, "description", "DESCRIPTION");
        String status = getString(j, "status", "STATUS", "APPLIED");

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
        t.setForeground(new Color(150, 200, 255));

        left.add(t);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);

        JLabel stLbl = new JLabel(status.toUpperCase());
        stLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        stLbl.setOpaque(true);
        stLbl.setHorizontalAlignment(SwingConstants.CENTER);
        stLbl.setPreferredSize(new Dimension(140, 35));
        
        if (status.equalsIgnoreCase("SELECTED")) {
            stLbl.setBackground(new Color(40, 160, 110));
            stLbl.setForeground(Color.WHITE);
        } else {
            stLbl.setBackground(new Color(70, 90, 150));
            stLbl.setForeground(Color.WHITE);
        }
        
        right.add(stLbl, BorderLayout.CENTER);

        card.add(left, BorderLayout.CENTER);
        card.add(right, BorderLayout.EAST);
        return card;
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
            new Thread(() -> {
                String err = ApiClient.uploadResume(ResudexApp.currentUserId, selectedFile);
                SwingUtilities.invokeLater(() -> {
                    uploadBtn.setEnabled(true);
                    uploadBtn.setText("SYNC PROFILE");
                    if (err == null) {
                        resumeStatus.setText("✔ Resume successfully analyzed and synced.");
                        resumeStatus.setForeground(new Color(120, 255, 150));
                        ToastNotification.show(this, "Profile Synced & Analyzed!", true);
                        loadJobs(); // Refresh matched scores
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
        c.gridy = 3; dz.add(Box.createVerticalStrut(30), c);
        c.gridy = 4; dz.add(browse, c);
        c.gridy = 5; dz.add(Box.createVerticalStrut(10), c);
        c.gridy = 6; dz.add(uploadBtn, c);
        c.gridy = 7; dz.add(resumeStatus, c);

        p.add(dz);
        return p;
    }

    private void loadJobs() {
        new Thread(() -> {
            // Using the smarter "matched" API to get personalized scores
            JsonArray jobs = ApiClient.getMatchedJobs(ResudexApp.currentUserId);
            SwingUtilities.invokeLater(() -> {
                jobsData = jobs;
                updateJobList(jobs);
            });
        }).start();
    }

    private void loadMyApplications() {
        new Thread(() -> {
            JsonArray apps = ApiClient.getUserApplications(ResudexApp.currentUserId);
            SwingUtilities.invokeLater(() -> {
                appsData = apps;
                updateAppsList(apps);
            });
        }).start();
    }

    private String getString(JsonObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) {
                return obj.get(k).getAsString();
            }
        }
        return null;
    }

    private int getInt(JsonObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) {
                try { return obj.get(k).getAsInt(); } catch (Exception ignored) {}
            }
        }
        return 0;
    }
}
