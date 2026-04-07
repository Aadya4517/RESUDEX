package com.resudex.swing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    
    // Screening Section
    private JList<String> jobList;
    private DefaultListModel<String> jobListModel;
    private JsonArray jobsData = new JsonArray();
    private JTable applicantTable;
    private DefaultTableModel applicantModel;
    private JsonArray currentApplicants = new JsonArray();
    private JLabel applJobLabel;

    private JTextField titleField;
    private JTextArea descArea;
    private JLabel formStatus;

    private ScoreChart scoreChart;

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 2, 33));

        add(buildSidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        contentPanel.add(buildScreeningSection(), "SCREEN");
        contentPanel.add(buildPostSection(),      "POST");
        contentPanel.add(buildAnalyticsSection(), "STATS");

        add(contentPanel, BorderLayout.CENTER);

        loadJobs();
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(240, 0));
        side.setBackground(new Color(23, 11, 59));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(48, 31, 95)));

        JPanel brand = new JPanel(new BorderLayout());
        brand.setOpaque(false);
        brand.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
        JLabel logo = new JLabel("RESUDEX ADM");
        logo.setFont(new Font("SansSerif", Font.BOLD, 22));
        logo.setForeground(new Color(0, 240, 255));
        brand.add(logo);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JButton btnScreen = createNavBtn("  Screen Applicants", "🔍");
        JButton btnPost   = createNavBtn("  Post Vacancy", "➕");
        JButton btnStats  = createNavBtn("  Recruitment Insights", "📊");

        btnScreen.addActionListener(e -> cardLayout.show(contentPanel, "SCREEN"));
        btnPost.addActionListener(e -> cardLayout.show(contentPanel, "POST"));
        btnStats.addActionListener(e -> { cardLayout.show(contentPanel, "STATS"); loadAnalytics(); });

        nav.add(btnScreen);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnPost);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnStats);

        JButton logout = new JButton("Exit Portal");
        logout.putClientProperty("JButton.buttonType", "borderless");
        logout.setForeground(new Color(200, 120, 120));
        logout.addActionListener(e -> ResudexApp.showHome());
        
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20));
        bottom.add(logout, BorderLayout.SOUTH);

        side.add(brand, BorderLayout.NORTH);
        side.add(nav,   BorderLayout.CENTER);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private JButton createNavBtn(String text, String icon) {
        JButton b = new JButton(icon + text);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setForeground(new Color(176, 149, 246));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.putClientProperty("JButton.buttonType", "borderless");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ================== SECTION: SCREENING ==================
    private JPanel buildScreeningSection() {
        JPanel p = new JPanel(new BorderLayout(25, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // LEFT: Jobs list in a small card
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(280, 0));
        
        JLabel lj = new JLabel("Open Positions");
        lj.setFont(new Font("SansSerif", Font.BOLD, 16));
        lj.setForeground(new Color(0, 240, 255));
        left.add(lj, BorderLayout.NORTH);

        jobListModel = new DefaultListModel<>();
        jobList = new JList<>(jobListModel);
        jobList.setBackground(new Color(32, 21, 71));
        jobList.setFixedCellHeight(40);
        jobList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && jobList.getSelectedIndex() >= 0) {
                int idx = jobList.getSelectedIndex();
                JsonObject j = jobsData.get(idx).getAsJsonObject();
                loadApplicants(getInt(j, "id", "ID"), getString(j, "title", "TITLE"));
            }
        });

        JScrollPane jsp = new JScrollPane(jobList);
        jsp.setBorder(BorderFactory.createLineBorder(new Color(60, 42, 112)));
        left.add(jsp, BorderLayout.CENTER);

        // Sidebar Job Controls
        JPanel jobCtrls = new JPanel(new GridLayout(1, 2, 8, 0));
        jobCtrls.setOpaque(false);
        JButton btnEdit = new JButton("Edit");
        JButton btnDel = new JButton("Delete");
        btnEdit.putClientProperty("JButton.buttonType", "roundRect");
        btnDel.putClientProperty("JButton.buttonType", "roundRect");
        btnDel.setBackground(new Color(150, 50, 50));
        btnDel.setForeground(Color.WHITE);

        btnEdit.addActionListener(e -> {
            int idx = jobList.getSelectedIndex();
            if (idx < 0) return;
            showEditJobDialog(jobsData.get(idx).getAsJsonObject());
        });

        btnDel.addActionListener(e -> {
            int idx = jobList.getSelectedIndex();
            if (idx < 0) return;
            JsonObject j = jobsData.get(idx).getAsJsonObject();
            int res = JOptionPane.showConfirmDialog(this, "Deleing '" + getString(j, "title", "TITLE") + "' will also remove all its applicants. Proceed?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                new Thread(() -> {
                    String err = ApiClient.deleteJob(getInt(j, "id", "ID"));
                    SwingUtilities.invokeLater(() -> {
                        if (err == null) loadJobs();
                        else ToastNotification.show(this, err, false);
                    });
                }).start();
            }
        });

        jobCtrls.add(btnEdit);
        jobCtrls.add(btnDel);
        left.add(jobCtrls, BorderLayout.SOUTH);

        // RIGHT: Applicant table
        JPanel right = new JPanel(new BorderLayout(0, 20));
        right.setOpaque(false);

        JPanel headerWrap = new JPanel(new BorderLayout(0, 10));
        headerWrap.setOpaque(false);

        applJobLabel = new JLabel("Select a position to screen candidates");
        applJobLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        applJobLabel.setForeground(Color.WHITE);

        scoreChart = new ScoreChart();
        scoreChart.setPreferredSize(new Dimension(0, 80));

        headerWrap.add(applJobLabel, BorderLayout.NORTH);
        headerWrap.add(scoreChart,   BorderLayout.CENTER);

        String[] cols = {"#", "Candidate Name", "Match %", "Quiz Score", "Status", "Top Skills"};
        applicantModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        applicantTable = new JTable(applicantModel);
        applicantTable.setRowHeight(40);
        applicantTable.setShowGrid(false);
        applicantTable.getTableHeader().setReorderingAllowed(false);
        
        applicantTable.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
        applicantTable.getColumnModel().getColumn(3).setCellRenderer(new QuizScoreRenderer());
        applicantTable.getColumnModel().getColumn(0).setMaxWidth(40);
        applicantTable.getColumnModel().getColumn(2).setMaxWidth(80);
        applicantTable.getColumnModel().getColumn(3).setMaxWidth(100);

        // Right Bottom: Action Buttons
        JPanel actionPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        actionPnl.setOpaque(false);

        JButton reviewBtn = new JButton("Review Profile");
        reviewBtn.setBackground(new Color(60, 42, 112));
        reviewBtn.setForeground(Color.WHITE);
        reviewBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        reviewBtn.setPreferredSize(new Dimension(160, 45));
        reviewBtn.putClientProperty("JButton.buttonType", "roundRect");
        reviewBtn.addActionListener(e -> {
            int row = applicantTable.getSelectedRow();
            if (row >= 0) showApplicantReview(currentApplicants.get(row).getAsJsonObject());
        });

        JButton feedbackBtn = new JButton("Send Feedback");
        feedbackBtn.setBackground(new Color(255, 190, 11));
        feedbackBtn.setForeground(Color.BLACK);
        feedbackBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        feedbackBtn.setPreferredSize(new Dimension(160, 45));
        feedbackBtn.putClientProperty("JButton.buttonType", "roundRect");
        feedbackBtn.addActionListener(e -> {
            int row = applicantTable.getSelectedRow();
            if (row < 0) return;
            JsonObject a = currentApplicants.get(row).getAsJsonObject();
            int appId = getInt(a, "app_id", "APP_ID");
            String f = JOptionPane.showInputDialog(this, "Enter feedback for " + getString(a, "username") + ":");
            if (f != null && !f.isBlank()) {
                new Thread(() -> {
                    String err = ApiClient.provideFeedback(appId, f);
                    SwingUtilities.invokeLater(() -> {
                        if (err == null) ToastNotification.show(this, "Feedback sent!", true);
                        else ToastNotification.show(this, err, false);
                    });
                }).start();
            }
        });

        JButton shortlistBtn = new JButton("Shortlist");
        shortlistBtn.setBackground(new Color(247, 37, 133));
        shortlistBtn.setForeground(Color.WHITE);
        shortlistBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        shortlistBtn.setPreferredSize(new Dimension(140, 45));
        shortlistBtn.putClientProperty("JButton.buttonType", "roundRect");
        shortlistBtn.addActionListener(e -> {
            int row = applicantTable.getSelectedRow();
            if (row < 0) return;
            JsonObject a = currentApplicants.get(row).getAsJsonObject();
            int appId = getInt(a, "app_id", "APP_ID");
            new Thread(() -> {
                String error = ApiClient.selectApplicant(appId);
                SwingUtilities.invokeLater(() -> {
                    if (error == null) {
                        ToastNotification.show(this, "Candidate Shortlisted! Notification sent.", true);
                        int selIdx = jobList.getSelectedIndex();
                        if (selIdx >= 0) loadApplicants(getInt(jobsData.get(selIdx).getAsJsonObject(), "id", "ID"), "");
                    } else ToastNotification.show(this, error, false);
                });
            }).start();
        });

        actionPnl.add(reviewBtn);
        actionPnl.add(feedbackBtn);
        actionPnl.add(shortlistBtn);

        JScrollPane asp = new JScrollPane(applicantTable);
        asp.setBorder(BorderFactory.createLineBorder(new Color(60, 42, 112)));

        right.add(applJobLabel, BorderLayout.NORTH);
        right.add(asp, BorderLayout.CENTER);
        right.add(actionPnl, BorderLayout.SOUTH);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.CENTER);
        return p;
    }

    // ================== SECTION: POST VACANCY ==================
    private JPanel buildPostSection() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(32, 21, 71));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 42, 112), 1, true),
            BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        card.setPreferredSize(new Dimension(550, 450));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(8, 0, 8, 0);

        JLabel h = new JLabel("Post New Vacancy", SwingConstants.CENTER);
        h.setFont(new Font("SansSerif", Font.BOLD, 24));
        h.setForeground(new Color(0, 240, 255));

        titleField = new JTextField();
        titleField.putClientProperty("JTextField.placeholderText", "Job Designation (e.g. Senior Java Dev)");
        titleField.setPreferredSize(new Dimension(400, 42));

        descArea = new JTextArea(6, 40);
        descArea.putClientProperty("JTextField.placeholderText", "Describe skills, requirements, and responsibilities...");
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(descArea);
        scrollDesc.setPreferredSize(new Dimension(400, 120));

        JButton postBtn = new JButton("BROADCAST VACANCY");
        postBtn.setBackground(new Color(6, 214, 160));
        postBtn.setForeground(Color.BLACK);
        postBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        postBtn.putClientProperty("JButton.buttonType", "roundRect");
        postBtn.setPreferredSize(new Dimension(400, 50));

        formStatus = new JLabel(" ", SwingConstants.CENTER);
        formStatus.setFont(new Font("SansSerif", Font.ITALIC, 13));

        postBtn.addActionListener(e -> {
            String t = titleField.getText().trim();
            String d = descArea.getText().trim();
            if (t.isEmpty() || d.isEmpty()) return;
            postBtn.setEnabled(false);
            new Thread(() -> {
                String err = ApiClient.createJob(t, d);
                SwingUtilities.invokeLater(() -> {
                    postBtn.setEnabled(true);
                    if (err == null) {
                        formStatus.setText("✔ Vacancy posted successfully!");
                        formStatus.setForeground(new Color(120, 255, 150));
                        titleField.setText(""); descArea.setText("");
                        loadJobs();
                    } else {
                        formStatus.setText("✖ Error: " + err);
                        formStatus.setForeground(new Color(255, 120, 120));
                    }
                });
            }).start();
        });

        c.gridy = 0; card.add(h, c);
        c.gridy = 1; card.add(Box.createVerticalStrut(20), c);
        c.gridy = 2; card.add(new JLabel("Position Title"), c);
        c.gridy = 3; card.add(titleField, c);
        c.gridy = 4; card.add(new JLabel("Detailed Requirements"), c);
        c.gridy = 5; card.add(scrollDesc, c);
        c.gridy = 6; card.add(Box.createVerticalStrut(15), c);
        c.gridy = 7; card.add(postBtn, c);
        c.gridy = 8; card.add(formStatus, c);

        p.add(card);
        return p;
    }

    private void loadJobs() {
        new Thread(() -> {
            JsonArray jobs = ApiClient.getJobs();
            SwingUtilities.invokeLater(() -> {
                jobsData = jobs;
                jobListModel.clear();
                for (int i = 0; i < jobs.size(); i++) {
                    JsonObject jobObj = jobs.get(i).getAsJsonObject();
                    String title = getString(jobObj, "title", "TITLE");
                    jobListModel.addElement("  " + (i + 1) + ".  " + (title != null ? title : "No Title"));
                }
            });
        }).start();
    }

    private void loadApplicants(int jobId, String jobTitle) {
        new Thread(() -> {
            JsonArray applicants = ApiClient.getApplicants(jobId);
            SwingUtilities.invokeLater(() -> {
                currentApplicants = applicants;
                applicantModel.setRowCount(0);
                if (jobTitle != null && !jobTitle.isEmpty()) applJobLabel.setText("Ranking: " + jobTitle);
                
                for (int i = 0; i < applicants.size(); i++) {
                    JsonObject a = applicants.get(i).getAsJsonObject();
                    String username = getString(a, "username", "USERNAME");
                    int    score    = getInt(a, "score", "SCORE");
                    int    techSc   = getInt(a, "techScore", "tech_score", "TECH_SCORE");
                    String status   = getString(a, "status", "STATUS");
                    
                    String matchedLines = getString(a, "matchedSkills", "MATCHEDSKILLS");
                    if (matchedLines == null) matchedLines = "";

                    applicantModel.addRow(new Object[]{
                        i + 1,
                        username,
                        score + "%",
                        (techSc < 0 ? "N/A" : techSc + "/5"),
                        (status != null && status.equalsIgnoreCase("SELECTED")) ? "✔ SELECTED" : "Applied",
                        matchedLines
                    });
                }
                if (scoreChart != null) scoreChart.updateData(applicants);
            });
        }).start();
    }

    // --- Custom Analytics Chart ---
    private static class ScoreChart extends JPanel {
        private JsonArray data = new JsonArray();
        public ScoreChart() { setOpaque(false); }
        public void updateData(JsonArray newData) { this.data = newData; repaint(); }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.size() == 0) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int barWidth = Math.max(12, (w - 40) / Math.max(1, data.size()));
            for (int i = 0; i < data.size() && i < 20; i++) {
                int s = getInt(data.get(i).getAsJsonObject(), "score", "SCORE");
                int bh = (int)( (h-20) * (s/100.0) );
                int x = 10 + i * (barWidth + 5);
                int y = h - bh - 5;
                g2.setPaint(new GradientPaint(x, y, new Color(6, 214, 160), x, y+bh, new Color(3, 107, 80)));
                g2.fillRoundRect(x, y, barWidth, bh, 5, 5);
            }
        }
        private int getInt(JsonObject obj, String... keys) {
            for (String k : keys) if (obj.has(k) && !obj.get(k).isJsonNull()) try { return obj.get(k).getAsInt(); } catch(Exception e){}
            return 0;
        }
    }

    private String getString(JsonObject obj, String... keys) {
        for (String k : keys) {
            if (obj.has(k) && !obj.get(k).isJsonNull()) {
                com.google.gson.JsonElement el = obj.get(k);
                if (el.isJsonArray()) {
                    JsonArray arr = el.getAsJsonArray();
                    StringBuilder sb = new StringBuilder();
                    for (int i=0; i<arr.size(); i++) {
                        sb.append(arr.get(i).getAsString());
                        if (i < arr.size()-1) sb.append(", ");
                    }
                    return sb.toString();
                }
                return el.getAsString();
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

    // --- Custom Renderer for Pills ---
    private static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.BOLD, 11));
            
            String status = (value != null) ? value.toString() : "";
            if (status.contains("SELECTED")) {
                l.setForeground(Color.BLACK);
                l.setBackground(new Color(6, 214, 160));
            } else {
                l.setForeground(Color.WHITE);
                l.setBackground(new Color(60, 42, 112));
            }
            l.setOpaque(true);
            l.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            return l;
        }
    }

    private static class QuizScoreRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSel, boolean hasFocus, int row, int col) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSel, hasFocus, row, col);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setFont(new Font("SansSerif", Font.BOLD, 12));
            
            String val = (value != null) ? value.toString() : "N/A";
            if (val.contains("5/5") || val.contains("4/5")) {
                l.setForeground(new Color(6, 214, 160)); // Green
            } else if (val.contains("3/5") || val.contains("2/5")) {
                l.setForeground(new Color(255, 190, 11)); // Yellow/Gold
            } else if (val.contains("1/5") || val.contains("0/5")) {
                l.setForeground(new Color(255, 70, 70)); // Redish
            } else {
                l.setForeground(Color.WHITE);
            }
            return l;
        }
    }

    private void showEditJobDialog(JsonObject job) {
        int id = getInt(job, "id", "ID");
        String title = getString(job, "title", "TITLE");
        String desc = getString(job, "description", "DESCRIPTION");

        JDialog diag = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Position", true);
        diag.setSize(500, 400);
        diag.setLocationRelativeTo(this);
        diag.getContentPane().setBackground(new Color(23, 11, 59));
        diag.setLayout(new BorderLayout(15, 15));

        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 1.0;

        JTextField tF = new JTextField(title);
        JTextArea dF = new JTextArea(desc, 8, 20);
        dF.setLineWrap(true); dF.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(dF);

        c.gridy = 0; p.add(new JLabel("Job Title"), c);
        c.gridy = 1; p.add(tF, c);
        c.gridy = 2; c.insets = new Insets(10, 0, 0, 0); p.add(new JLabel("Description"), c);
        c.gridy = 3; c.insets = new Insets(2, 0, 0, 0); p.add(sp, c);

        JButton save = new JButton("Save Changes");
        save.setBackground(new Color(0, 240, 255));
        save.setForeground(Color.BLACK);
        save.addActionListener(e -> {
            new Thread(() -> {
                String error = ApiClient.updateJob(id, tF.getText(), dF.getText());
                SwingUtilities.invokeLater(() -> {
                    if (error == null) {
                        diag.dispose();
                        loadJobs();
                    } else ToastNotification.show(this, error, false);
                });
            }).start();
        });

        diag.add(p, BorderLayout.CENTER);
        diag.add(save, BorderLayout.SOUTH);
        diag.setVisible(true);
    }

    private void showApplicantReview(JsonObject app) {
        JDialog diag = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Candidate Deep Review", true);
        diag.setSize(600, 650);
        diag.setLocationRelativeTo(this);
        diag.getContentPane().setBackground(new Color(13, 2, 33));
        diag.setLayout(new BorderLayout());

        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;

        JLabel nameL = new JLabel("PROFILE: " + getString(app, "username", "USERNAME").toUpperCase());
        nameL.setFont(new Font("SansSerif", Font.BOLD, 26));
        nameL.setForeground(new Color(0, 240, 255));
        c.gridy = 0; c.insets = new Insets(0,0,20,0); p.add(nameL, c);

        // RANK SUMMARY
        if (app.has("rankSummary")) {
            JPanel summP = new JPanel();
            summP.setOpaque(false);
            summP.setLayout(new BoxLayout(summP, BoxLayout.Y_AXIS));
            summP.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(60, 42, 112)), "AI Insights", 0, 0, null, Color.GRAY));

            JsonArray summ = app.getAsJsonArray("rankSummary");
            for (int i=0; i<summ.size(); i++) {
                JLabel l = new JLabel("  " + summ.get(i).getAsString());
                l.setForeground(Color.WHITE);
                l.setFont(new Font("SansSerif", Font.PLAIN, 14));
                summP.add(Box.createVerticalStrut(5));
                summP.add(l);
            }
            summP.add(Box.createVerticalStrut(10));
            c.gridy = 1; c.insets = new Insets(0,0,20,0); p.add(summP, c);
        }

        // TECH SCORE BADGE
        int ts = getInt(app, "techScore", "tech_score", "TECH_SCORE");
        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        scoreRow.setOpaque(false);
        JLabel tsH = new JLabel("Assessment Score: ");
        tsH.setForeground(Color.GRAY);
        JLabel tsL = new JLabel(ts < 0 ? "N/A" : ts + " / 5");
        tsL.setForeground(new Color(255, 190, 11));
        tsL.setFont(new Font("SansSerif", Font.BOLD, 18));
        scoreRow.add(tsH); scoreRow.add(tsL);
        c.gridy = 2; p.add(scoreRow, c);

        // SKILLS
        JLabel skillH = new JLabel("Detected Capabilities:");
        skillH.setForeground(new Color(176, 149, 246));
        skillH.setFont(new Font("SansSerif", Font.BOLD, 14));
        c.gridy = 3; c.insets = new Insets(20,0,5,0); p.add(skillH, c);

        String rawSkills = getString(app, "matchedSkills", "MATCHEDSKILLS");
        JTextArea skills = new JTextArea(rawSkills != null ? rawSkills : "None detected");
        skills.setOpaque(false); 
        skills.setEditable(false); 
        skills.setLineWrap(true); 
        skills.setWrapStyleWord(true);
        skills.setForeground(new Color(6, 214, 160));
        skills.setFont(new Font("Monospaced", Font.BOLD, 14));
        c.gridy = 4; c.insets = new Insets(0,0,30,0); p.add(skills, c);

        diag.add(new JScrollPane(p), BorderLayout.CENTER);
        
        JButton close = new JButton("DISMISS");
        close.setBackground(new Color(247, 37, 133));
        close.setForeground(Color.WHITE);
        close.setFont(new Font("SansSerif", Font.BOLD, 14));
        close.setPreferredSize(new Dimension(0, 50));
        close.addActionListener(e -> diag.dispose());
        diag.add(close, BorderLayout.SOUTH);

        diag.setVisible(true);
    }

    // ================== SECTION: ANALYTICS ==================
    private JPanel funnelPanel;
    private JPanel trendsPanel;

    private JPanel buildAnalyticsSection() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel h = new JLabel("Recruitment Analytics Dashboard");
        h.setFont(new Font("SansSerif", Font.BOLD, 28));
        h.setForeground(Color.WHITE);
        p.add(h, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0));
        grid.setOpaque(false);

        funnelPanel = new JPanel(new BorderLayout());
        funnelPanel.setBackground(new Color(23, 11, 59));
        funnelPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(48,31,95)), "Recruitment Funnel", 0, 0, null, new Color(0,240,255)));

        trendsPanel = new JPanel(new BorderLayout());
        trendsPanel.setBackground(new Color(23, 11, 59));
        trendsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(48,31,95)), "Skill Demand Trends", 0, 0, null, new Color(176, 149, 246)));

        grid.add(funnelPanel);
        grid.add(trendsPanel);
        p.add(grid, BorderLayout.CENTER);

        return p;
    }

    private void loadAnalytics() {
        new Thread(() -> {
            JsonObject stats = ApiClient.getAdminAnalytics();
            SwingUtilities.invokeLater(() -> updateAnalyticsUI(stats));
        }).start();
    }

    private void updateAnalyticsUI(JsonObject stats) {
        if (stats == null) return;

        funnelPanel.removeAll();
        trendsPanel.removeAll();

        int totalA = stats.get("totalApps").getAsInt();
        int selC = stats.get("selectedCount").getAsInt();

        // Funnel Visualization (Simplified)
        JPanel funnel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                
                // Applicants Bar
                g2.setColor(new Color(60, 42, 112));
                g2.fillRoundRect(50, 50, w-100, 60, 15, 15);
                g2.setColor(Color.WHITE);
                g2.drawString("Total Applicants: " + totalA, 70, 85);

                // Selected Bar
                int sw = (totalA == 0) ? 0 : (int)((w-100) * (selC / (double)totalA));
                g2.setColor(new Color(6, 214, 160));
                g2.fillRoundRect(50, 130, Math.max(50, sw), 60, 15, 15);
                g2.setColor(Color.BLACK);
                g2.drawString("Selected: " + selC, 70, 165);
            }
        };
        funnel.setOpaque(false);
        funnelPanel.add(funnel);

        // Trends Visualization
        JsonObject trends = stats.getAsJsonObject("skillTrends");
        JPanel trendList = new JPanel();
        trendList.setLayout(new BoxLayout(trendList, BoxLayout.Y_AXIS));
        trendList.setOpaque(false);
        trendList.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        trends.keySet().stream()
            .sorted((a,b) -> trends.get(b).getAsInt() - trends.get(a).getAsInt())
            .limit(10)
            .forEach(skill -> {
                JLabel l = new JLabel(skill.toUpperCase() + ": " + trends.get(skill).getAsInt() + " jobs");
                l.setForeground(Color.LIGHT_GRAY);
                l.setFont(new Font("Monospaced", Font.BOLD, 14));
                trendList.add(l);
                trendList.add(Box.createVerticalStrut(8));
            });

        trendsPanel.add(new JScrollPane(trendList));

        revalidate(); repaint();
    }
}
