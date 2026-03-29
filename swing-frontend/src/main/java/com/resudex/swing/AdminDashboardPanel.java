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

        btnScreen.addActionListener(e -> cardLayout.show(contentPanel, "SCREEN"));
        btnPost.addActionListener(e -> cardLayout.show(contentPanel, "POST"));

        nav.add(btnScreen);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btnPost);

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

        String[] cols = {"#", "Candidate Name", "Match %", "Status", "Top Skills"};
        applicantModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        applicantTable = new JTable(applicantModel);
        applicantTable.setRowHeight(40);
        applicantTable.setShowGrid(false);
        applicantTable.getTableHeader().setReorderingAllowed(false);
        
        applicantTable.getColumnModel().getColumn(3).setCellRenderer(new StatusBadgeRenderer());
        applicantTable.getColumnModel().getColumn(0).setMaxWidth(40);
        applicantTable.getColumnModel().getColumn(2).setMaxWidth(80);

        // Custom selection action
        JButton shortlistBtn = new JButton("Shortlist Selected Candidate");
        shortlistBtn.setBackground(new Color(247, 37, 133));
        shortlistBtn.setForeground(Color.WHITE);
        shortlistBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        shortlistBtn.setPreferredSize(new Dimension(250, 45));
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
                        loadApplicants(getInt(jobsData.get(jobList.getSelectedIndex()).getAsJsonObject(), "id", "ID"), "");
                    } else {
                        JOptionPane.showMessageDialog(this, error);
                    }
                });
            }).start();
        });

        JScrollPane asp = new JScrollPane(applicantTable);
        asp.setBorder(BorderFactory.createLineBorder(new Color(60, 42, 112)));

        right.add(applJobLabel, BorderLayout.NORTH);
        right.add(asp, BorderLayout.CENTER);
        right.add(shortlistBtn, BorderLayout.SOUTH);

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
                    String status   = getString(a, "status", "STATUS");
                    
                    // mathed skills logic simplified for display
                    String matchedLines = "";
                    if (a.has("matchedSkills") && a.get("matchedSkills").isJsonArray()) {
                        matchedLines = a.get("matchedSkills").getAsJsonArray().toString().replace("[","").replace("]","").replace("\"","");
                    } else if (a.has("MATCHEDSKILLS") && a.get("MATCHEDSKILLS").isJsonArray()) {
                        matchedLines = a.get("MATCHEDSKILLS").getAsJsonArray().toString().replace("[","").replace("]","").replace("\"","");
                    }

                    applicantModel.addRow(new Object[]{
                        i + 1,
                        username,
                        score + "%",
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
            if (obj.has(k) && !obj.get(k).isJsonNull()) return obj.get(k).getAsString();
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
}
