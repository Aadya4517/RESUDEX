package com.resudex.swing;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminDashboardPanel extends JPanel {
    private CardLayout card_lay;
    private JPanel pan_content;
    
    // Screening Section
    private JList<String> list_jobs;
    private DefaultListModel<String> mod_jobs;
    private JsonArray cache_jobs = new JsonArray();
    private JLabel lbl_job;
    
    private boolean pipe_on = false;
    private JPanel box_mid;
    private JPanel box_pipe;
    private String[] STAGES = {"APPLIED", "IN REVIEW", "SHORTLISTED", "INTERVIEW", "HIRED", "REJECTED"};

    private JTextField f_title;
    private JTextArea f_desc;
    private JComboBox<String> f_status;
    private JLabel lbl_msg;
    private final String[] VIBES = {"Wizard", "Grit", "Cultural Fit", "Fast Learner", "Problem Solver"};

    private ScoreChart chart_viz;
    private JTable tbl_apps;
    private DefaultTableModel mod_apps;
    private JsonArray cache_apps = new JsonArray();

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 2, 33));

        add(get_sidebar(), BorderLayout.WEST);

        card_lay = new CardLayout();
        pan_content = new JPanel(card_lay);
        pan_content.setOpaque(false);

        pan_content.add(get_screen_pnl(), "SCREEN");
        pan_content.add(get_post_pnl(),   "POST");
        pan_content.add(get_stats_pnl(),  "STATS");
        pan_content.add(get_battle_pnl(), "BATTLE");

        add(pan_content, BorderLayout.CENTER);

        fetch_jobs();
    }

    private JPanel get_sidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(240, 0));
        side.setBackground(new Color(23, 11, 59));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(48, 31, 95)));

        JPanel branding = new JPanel(new BorderLayout());
        branding.setOpaque(false);
        branding.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
        JLabel l = new JLabel("RESUDEX ADM");
        l.setFont(new Font("SansSerif", Font.BOLD, 22));
        l.setForeground(new Color(0, 240, 255));
        branding.add(l);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JButton btn_screen = make_nav_btn("  Screen Applicants", "🔍");
        JButton btn_post   = make_nav_btn("  Post Vacancy", "➕");
        JButton btn_stats  = make_nav_btn("  Recruitment Insights", "📊");
        JButton btn_battle = make_nav_btn("  Resume Battle ⚔️", "🥊");

        btn_screen.addActionListener(e -> card_lay.show(pan_content, "SCREEN"));
        btn_post.addActionListener(e -> card_lay.show(pan_content, "POST"));
        btn_stats.addActionListener(e -> { card_lay.show(pan_content, "STATS"); fetch_stats(); });
        btn_battle.addActionListener(e -> { card_lay.show(pan_content, "BATTLE"); load_battle_users(); });

        nav.add(btn_screen);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btn_post);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btn_stats);
        nav.add(Box.createVerticalStrut(10));
        nav.add(btn_battle);

        JButton exit = new JButton("Exit Portal");
        exit.putClientProperty("JButton.buttonType", "borderless");
        exit.setForeground(new Color(200, 120, 120));
        exit.addActionListener(e -> ResudexApp.go_home());
        
        JPanel bot = new JPanel(new BorderLayout());
        bot.setOpaque(false);
        bot.setBorder(BorderFactory.createEmptyBorder(20, 20, 30, 20));
        bot.add(exit, BorderLayout.SOUTH);

        side.add(branding, BorderLayout.NORTH);
        side.add(nav,      BorderLayout.CENTER);
        side.add(bot,      BorderLayout.SOUTH);
        return side;
    }

    private JButton make_nav_btn(String txt, String icon) {
        JButton b = new JButton(icon + txt);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setForeground(new Color(176, 149, 246));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.putClientProperty("JButton.buttonType", "borderless");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ================== SECTION: SCREENING ==================
    // screening view
    private JPanel get_screen_pnl() {
        JPanel p = new JPanel(new BorderLayout(25, 0));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // LEFT: list of jobs
        JPanel L = new JPanel(new BorderLayout(0, 10));
        L.setOpaque(false);
        L.setPreferredSize(new Dimension(280, 0));
        
        JLabel head = new JLabel("Open Positions");
        head.setFont(new Font("SansSerif", Font.BOLD, 16));
        head.setForeground(new Color(0, 240, 255));
        L.add(head, BorderLayout.NORTH);

        mod_jobs = new DefaultListModel<>();
        list_jobs = new JList<>(mod_jobs);
        list_jobs.setBackground(new Color(32, 21, 71));
        list_jobs.setFixedCellHeight(40);
        list_jobs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_jobs.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && list_jobs.getSelectedIndex() >= 0) {
                int i = list_jobs.getSelectedIndex();
                JsonObject j = cache_jobs.get(i).getAsJsonObject();
                fetch_apps(getInt(j, "id", "ID"), getString(j, "title", "TITLE"));
            }
        });

        JScrollPane sp = new JScrollPane(list_jobs);
        sp.setBorder(BorderFactory.createLineBorder(new Color(60, 42, 112)));
        L.add(sp, BorderLayout.CENTER);

        // job actions
        JPanel acts = new JPanel(new GridLayout(1, 2, 8, 0));
        acts.setOpaque(false);
        JButton b_edit = new JButton("Edit");
        JButton b_del = new JButton("Delete");
        b_edit.putClientProperty("JButton.buttonType", "roundRect");
        b_del.putClientProperty("JButton.buttonType", "roundRect");
        b_del.setBackground(new Color(150, 50, 50));
        b_del.setForeground(Color.WHITE);

        b_edit.addActionListener(e -> {
            int idx = list_jobs.getSelectedIndex();
            if (idx < 0) return;
            pop_edit(cache_jobs.get(idx).getAsJsonObject());
        });

        b_del.addActionListener(e -> {
            int idx = list_jobs.getSelectedIndex();
            if (idx < 0) return;
            JsonObject j = cache_jobs.get(idx).getAsJsonObject();
            int res = JOptionPane.showConfirmDialog(this, "Deleing '" + getString(j, "title", "TITLE") + "' will also remove all its applicants. Proceed?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                new Thread(() -> {
                    String err = ApiClient.drop_job(getInt(j, "id", "ID"));
                    SwingUtilities.invokeLater(() -> {
                        if (err == null) fetch_jobs();
                        else ToastNotification.pop(this, err, false);
                    });
                }).start();
            }
        });

        acts.add(b_edit);
        acts.add(b_del);
        L.add(acts, BorderLayout.SOUTH);

        // RIGHT: applicant box
        JPanel side_r = new JPanel(new BorderLayout(0, 20));
        side_r.setOpaque(false);

        JPanel hdr = new JPanel(new BorderLayout(0, 10));
        hdr.setOpaque(false);

        lbl_job = new JLabel("Select a position to screen candidates");
        lbl_job.setFont(new Font("SansSerif", Font.BOLD, 22));
        lbl_job.setForeground(Color.WHITE);

        chart_viz = new ScoreChart();
        chart_viz.setPreferredSize(new Dimension(0, 80));

        hdr.add(lbl_job, BorderLayout.NORTH);
        
        JPanel side_acts = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        side_acts.setOpaque(false);
        JButton b_sw = new JButton("Switch View (Table / Pipeline)");
        b_sw.addActionListener(e -> {
            pipe_on = !pipe_on;
            fetch_apps(getInt(cache_jobs.get(list_jobs.getSelectedIndex()).getAsJsonObject(), "id", "ID"), null);
        });
        side_acts.add(b_sw);
        hdr.add(side_acts, BorderLayout.EAST);
        hdr.add(chart_viz,   BorderLayout.CENTER);

        String[] cols = {"#", "Candidate Name", "Match %", "Quiz Score", "Status", "Top Skills"};
        mod_apps = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tbl_apps = new JTable(mod_apps);
        tbl_apps.setRowHeight(40);
        tbl_apps.setShowGrid(false);
        tbl_apps.getTableHeader().setReorderingAllowed(false);
        
        tbl_apps.getColumnModel().getColumn(4).setCellRenderer(new StatusBadgeRenderer());
        tbl_apps.getColumnModel().getColumn(3).setCellRenderer(new QuizScoreRenderer());
        tbl_apps.getColumnModel().getColumn(0).setMaxWidth(40);
        tbl_apps.getColumnModel().getColumn(2).setMaxWidth(80);
        tbl_apps.getColumnModel().getColumn(3).setMaxWidth(100);
 
        box_pipe = new JPanel(new GridLayout(1, STAGES.length, 10, 0));
        box_pipe.setOpaque(false);
 
        box_mid = new JPanel(new CardLayout());
        box_mid.setOpaque(false);
        box_mid.add(new JScrollPane(tbl_apps), "TABLE");
        box_mid.add(new JScrollPane(box_pipe), "PIPELINE");
 
        // bottom buttons
        JPanel acts_pan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        acts_pan.setOpaque(false);

        JButton b_view = new JButton("Review Profile");
        b_view.setBackground(new Color(60, 42, 112));
        b_view.setForeground(Color.WHITE);
        b_view.setFont(new Font("SansSerif", Font.BOLD, 14));
        b_view.setPreferredSize(new Dimension(160, 45));
        b_view.putClientProperty("JButton.buttonType", "roundRect");
        b_view.addActionListener(e -> {
            int row = tbl_apps.getSelectedRow();
            if (row >= 0) pop_review(cache_apps.get(row).getAsJsonObject());
        });

        JButton b_fb = new JButton("Send Feedback");
        b_fb.setBackground(new Color(255, 190, 11));
        b_fb.setForeground(Color.BLACK);
        b_fb.setFont(new Font("SansSerif", Font.BOLD, 14));
        b_fb.setPreferredSize(new Dimension(160, 45));
        b_fb.putClientProperty("JButton.buttonType", "roundRect");
        b_fb.addActionListener(e -> {
            int row = tbl_apps.getSelectedRow();
            if (row < 0) return;
            JsonObject a = cache_apps.get(row).getAsJsonObject();
            int aid = getInt(a, "app_id", "APP_ID");
            String f = JOptionPane.showInputDialog(this, "Enter feedback for " + getString(a, "username") + ":");
            if (f != null && !f.isBlank()) {
                new Thread(() -> {
                    String err = ApiClient.add_comment(aid, f);
                    SwingUtilities.invokeLater(() -> {
                        if (err == null) ToastNotification.pop(this, "Feedback sent!", true);
                        else ToastNotification.pop(this, err, false);
                    });
                }).start();
            }
        });

        JButton b_pick = new JButton("Shortlist");
        b_pick.setBackground(new Color(247, 37, 133));
        b_pick.setForeground(Color.WHITE);
        b_pick.setFont(new Font("SansSerif", Font.BOLD, 14));
        b_pick.setPreferredSize(new Dimension(140, 45));
        b_pick.putClientProperty("JButton.buttonType", "roundRect");
        b_pick.addActionListener(e -> {
            int row = tbl_apps.getSelectedRow();
            if (row < 0) return;
            JsonObject a = cache_apps.get(row).getAsJsonObject();
            int aid = getInt(a, "app_id", "APP_ID");
            new Thread(() -> {
                String error = ApiClient.pick_app(aid);
                SwingUtilities.invokeLater(() -> {
                    if (error == null) {
                        ToastNotification.pop(this, "Candidate Shortlisted! Notification sent.", true);
                        int selIdx = list_jobs.getSelectedIndex();
                        if (selIdx >= 0) fetch_apps(getInt(cache_jobs.get(selIdx).getAsJsonObject(), "id", "ID"), "");
                    } else ToastNotification.pop(this, error, false);
                });
            }).start();
        });

        acts_pan.add(b_view);
        acts_pan.add(b_fb);
        acts_pan.add(b_pick);

        JScrollPane asp = new JScrollPane(box_mid);
        asp.setBorder(BorderFactory.createLineBorder(new Color(60, 42, 112)));
 
        side_r.add(hdr, BorderLayout.NORTH);
        side_r.add(box_mid, BorderLayout.CENTER);
        side_r.add(acts_pan, BorderLayout.SOUTH);

        p.add(L, BorderLayout.WEST);
        p.add(side_r, BorderLayout.CENTER);
        return p;
    }

    // ================== SECTION: POST VACANCY ==================
    private JPanel get_post_pnl() {
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

        f_title = new JTextField();
        f_title.putClientProperty("JTextField.placeholderText", "Job Designation (e.g. Senior Java Dev)");
        f_title.setPreferredSize(new Dimension(400, 42));

        f_desc = new JTextArea(6, 40);
        f_desc.putClientProperty("JTextField.placeholderText", "Describe skills, requirements, and responsibilities...");
        f_desc.setLineWrap(true);
        f_desc.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(f_desc);
        sp.setPreferredSize(new Dimension(400, 120));

        JButton b_go = new JButton("BROADCAST VACANCY");
        b_go.setBackground(new Color(6, 214, 160));
        b_go.setForeground(Color.BLACK);
        b_go.setFont(new Font("SansSerif", Font.BOLD, 14));
        b_go.putClientProperty("JButton.buttonType", "roundRect");
        b_go.setPreferredSize(new Dimension(400, 50));

        lbl_msg = new JLabel(" ", SwingConstants.CENTER);
        lbl_msg.setFont(new Font("SansSerif", Font.ITALIC, 13));

        b_go.addActionListener(e -> {
            String t = f_title.getText().trim();
            String d = f_desc.getText().trim();
            String s = (String) f_status.getSelectedItem();
            if (t.isEmpty() || d.isEmpty()) {
                lbl_msg.setText("Missing fields.");
                return;
            }
            new Thread(() -> {
                String err = ApiClient.save_new_job(t, d, s);
                SwingUtilities.invokeLater(() -> {
                    if (err == null) {
                        ToastNotification.pop(this, "Job Posted!", true);
                        f_title.setText("");
                        f_desc.setText("");
                        fetch_jobs();
                    } else {
                        lbl_msg.setText(err);
                    }
                });
            }).start();
        });

        c.gridy = 0; card.add(h, c);
        c.gridy = 1; card.add(Box.createVerticalStrut(20), c);
        c.gridy = 2; card.add(new JLabel("Position Title"), c);
        c.gridy = 3; card.add(f_title, c);
        c.gridy = 4; card.add(new JLabel("Detailed Requirements"), c);
        c.gridy = 5; card.add(sp, c);
 
        c.gridy = 6; card.add(new JLabel("Initial Status"), c);
        f_status = new JComboBox<>(new String[]{"OPEN", "DRAFT", "ON_HOLD", "CLOSED"});
        c.gridy = 7; card.add(f_status, c);
 
        c.gridy = 8; card.add(Box.createVerticalStrut(15), c);
        c.gridy = 9; card.add(b_go, c);
        c.gridy = 10; card.add(lbl_msg, c);

        p.add(card);
        return p;
    }

    private void fetch_jobs() {
        new Thread(() -> {
            JsonArray jobs = ApiClient.list_all_jobs(true);
            SwingUtilities.invokeLater(() -> {
                cache_jobs = jobs;
                mod_jobs.clear();
                for (int i = 0; i < jobs.size(); i++) {
                    JsonObject j = jobs.get(i).getAsJsonObject();
                    String t = getString(j, "title", "TITLE");
                    String s = getString(j, "status", "STATUS");
                    String dot = (s != null && s.equalsIgnoreCase("OPEN")) ? "🟢 " : "🔴 ";
                    mod_jobs.addElement("  " + dot + (i + 1) + ".  " + (t != null ? t : "No Title"));
                }
            });
        }).start();
    }

    private void fetch_apps(int jid, String j_title) {
        new Thread(() -> {
            JsonArray apps = ApiClient.list_apps(jid);
            SwingUtilities.invokeLater(() -> {
                cache_apps = apps;
                mod_apps.setRowCount(0);
                if (j_title != null && !j_title.isEmpty()) lbl_job.setText("Ranking: " + j_title);
                
                for (int i = 0; i < apps.size(); i++) {
                    JsonObject a = apps.get(i).getAsJsonObject();
                    String u = getString(a, "username", "USERNAME");
                    int sc = getInt(a, "sc");
                    int tsc = getInt(a, "tech_sc");
                    String s = getString(a, "status", "STATUS");
                    String v = getString(a, "vibes", "VIBES");
                    
                    String s_list = getString(a, "hits");
                    if (s_list == null) s_list = "";
 
                    mod_apps.addRow(new Object[]{
                        i + 1,
                        u,
                        sc + "%",
                        (tsc < 0 ? "N/A" : tsc + "/5"),
                        s,
                        s_list
                    });
                }
                if (chart_viz != null) chart_viz.updateData(apps);

                if (pipe_on) {
                    ((CardLayout) box_mid.getLayout()).show(box_mid, "PIPELINE");
                    sync_pipeline(apps);
                } else {
                    ((CardLayout) box_mid.getLayout()).show(box_mid, "TABLE");
                }
            });
        }).start();
    }

    private void sync_pipeline(JsonArray apps) {
        box_pipe.removeAll();
        for (String stage : STAGES) {
            JPanel col = new JPanel(new BorderLayout());
            col.setOpaque(false);
            col.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(60, 42, 112)), stage, 0, 0, null, Color.GRAY));
            
            JPanel items = new JPanel();
            items.setOpaque(false);
            items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
 
            for (int i=0; i<apps.size(); i++) {
                JsonObject a = apps.get(i).getAsJsonObject();
                String s = getString(a, "status", "STATUS");
                if ( (s != null && s.equalsIgnoreCase(stage)) || (stage.equals("APPLIED") && (s == null || s.equalsIgnoreCase("Applied"))) ) {
                    items.add(make_card(a));
                    items.add(Box.createVerticalStrut(8));
                }
            }
            col.add(new JScrollPane(items), BorderLayout.CENTER);
            box_pipe.add(col);
        }
        box_pipe.revalidate();
        box_pipe.repaint();
    }
    private JPanel make_card(JsonObject a) {
        JPanel c = new JPanel(new BorderLayout());
        c.setBackground(new Color(45, 30, 85));
        c.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel name = new JLabel(getString(a, "username"));
        name.setForeground(Color.WHITE);
        name.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel score = new JLabel(getInt(a, "sc") + "% Match");
        score.setForeground(new Color(0, 240, 255));

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bar.setOpaque(false);
        JButton next_b = new JButton("Advance →");
        next_b.setFont(new Font("SansSerif", Font.PLAIN, 10));
        next_b.addActionListener(e -> {
            String curr = getString(a, "status", "APPLIED");
            String next = get_next_step(curr);
            if (next != null) {
                new Thread(() -> {
                   ApiClient.set_app_status(getInt(a, "app_id"), next);
                   int si = list_jobs.getSelectedIndex();
                   if (si >= 0) fetch_apps(getInt(cache_jobs.get(si).getAsJsonObject(), "id", "ID"), null);
                }).start();
            }
        });
        bar.add(next_b);

        c.add(name, BorderLayout.NORTH);
        c.add(score, BorderLayout.CENTER);
        c.add(bar, BorderLayout.SOUTH);
        return c;
    }

    private String get_next_step(String s) {
        for (int i=0; i<STAGES.length-1; i++) {
            if (STAGES[i].equalsIgnoreCase(s) || (i==0 && s.equalsIgnoreCase("Applied"))) return STAGES[i+1];
        }
        return null;
    }

    // ================== SECTION: RESUME BATTLE ==================
    private JComboBox<String> cb_p1, cb_p2, cb_job;
    private java.util.List<JsonObject> battle_users_cache = new java.util.ArrayList<>();
    private java.util.List<JsonObject> battle_jobs_cache  = new java.util.ArrayList<>();
    private JPanel pan_battle_result;

    private JPanel get_battle_pnl() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // top section: title + controls stacked
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);

        JLabel h = new JLabel("Resume Battle ⚔️");
        h.setFont(new Font("SansSerif", Font.BOLD, 28));
        h.setForeground(new Color(247, 37, 133));
        h.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
        top.add(h);

        JPanel ctrl = new JPanel(new GridLayout(1, 4, 15, 0));
        ctrl.setOpaque(false);
        ctrl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        cb_p1  = new JComboBox<>(); cb_p1.setBackground(new Color(32,21,71));  cb_p1.setForeground(Color.WHITE);
        cb_p2  = new JComboBox<>(); cb_p2.setBackground(new Color(32,21,71));  cb_p2.setForeground(Color.WHITE);
        cb_job = new JComboBox<>(); cb_job.setBackground(new Color(32,21,71)); cb_job.setForeground(Color.WHITE);

        JButton b_go = new JButton("⚔️  BATTLE!");
        b_go.setBackground(new Color(247, 37, 133));
        b_go.setForeground(Color.WHITE);
        b_go.setFont(new Font("SansSerif", Font.BOLD, 14));
        b_go.putClientProperty("JButton.buttonType", "roundRect");
        b_go.addActionListener(e -> run_battle());

        ctrl.add(cb_p1); ctrl.add(cb_p2); ctrl.add(cb_job); ctrl.add(b_go);
        top.add(ctrl);
        top.add(Box.createVerticalStrut(20));
        p.add(top, BorderLayout.NORTH);

        pan_battle_result = new JPanel();
        pan_battle_result.setLayout(new BoxLayout(pan_battle_result, BoxLayout.Y_AXIS));
        pan_battle_result.setOpaque(false);

        JScrollPane sp = new JScrollPane(pan_battle_result);
        sp.setOpaque(false); sp.getViewport().setOpaque(false); sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void load_battle_users() {
        new Thread(() -> {
            JsonArray users = ApiClient.get_battle_users();
            JsonArray jobs  = ApiClient.list_all_jobs(true);
            SwingUtilities.invokeLater(() -> {
                battle_users_cache.clear(); battle_jobs_cache.clear();
                cb_p1.removeAllItems(); cb_p2.removeAllItems(); cb_job.removeAllItems();
                for (int i = 0; i < users.size(); i++) {
                    JsonObject u = users.get(i).getAsJsonObject();
                    battle_users_cache.add(u);
                    String name = u.has("full_name") && !u.get("full_name").isJsonNull()
                        ? u.get("full_name").getAsString() : u.get("username").getAsString();
                    cb_p1.addItem(name); cb_p2.addItem(name);
                }
                for (int i = 0; i < jobs.size(); i++) {
                    JsonObject j = jobs.get(i).getAsJsonObject();
                    battle_jobs_cache.add(j);
                    cb_job.addItem(getString(j, "title", "TITLE"));
                }
            });
        }).start();
    }

    private void run_battle() {
        int i1 = cb_p1.getSelectedIndex();
        int i2 = cb_p2.getSelectedIndex();
        int ij = cb_job.getSelectedIndex();
        if (i1 < 0 || i2 < 0 || ij < 0 || i1 == i2) {
            JOptionPane.showMessageDialog(this, "Pick two different candidates and a job.");
            return;
        }
        int uid1 = getInt(battle_users_cache.get(i1), "id", "ID");
        int uid2 = getInt(battle_users_cache.get(i2), "id", "ID");
        int jid  = getInt(battle_jobs_cache.get(ij),  "id", "ID");

        pan_battle_result.removeAll();
        JLabel loading = new JLabel("⚔️  Analysing...", SwingConstants.CENTER);
        loading.setForeground(Color.GRAY); loading.setFont(new Font("SansSerif", Font.BOLD, 16));
        pan_battle_result.add(loading);
        pan_battle_result.revalidate(); pan_battle_result.repaint();

        new Thread(() -> {
            JsonObject res = ApiClient.run_battle(uid1, uid2, jid);
            SwingUtilities.invokeLater(() -> render_battle(res));
        }).start();
    }

    private void render_battle(JsonObject res) {
        pan_battle_result.removeAll();
        if (res == null || res.has("error")) {
            pan_battle_result.add(new JLabel("Battle failed. Check backend."));
            pan_battle_result.revalidate(); return;
        }

        String job    = res.has("job")    ? res.get("job").getAsString()    : "?";
        String winner = res.has("winner") ? res.get("winner").getAsString() : "?";
        JsonObject p1 = res.getAsJsonObject("p1");
        JsonObject p2 = res.getAsJsonObject("p2");

        // winner banner
        JPanel banner = new JPanel(new GridBagLayout());
        banner.setBackground(winner.equals("TIE") ? new Color(60,60,80) : new Color(6,60,40));
        banner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(6,214,160), 2, true),
            BorderFactory.createEmptyBorder(15,25,15,25)));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        JLabel lbl_w = new JLabel(winner.equals("TIE") ? "🤝 IT'S A TIE!" : "🏆 WINNER: " + winner.toUpperCase());
        lbl_w.setFont(new Font("SansSerif", Font.BOLD, 22));
        lbl_w.setForeground(winner.equals("TIE") ? Color.YELLOW : new Color(6,214,160));
        JLabel lbl_j = new JLabel("  for: " + job);
        lbl_j.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl_j.setForeground(Color.LIGHT_GRAY);
        banner.add(lbl_w); banner.add(lbl_j);
        pan_battle_result.add(banner);
        pan_battle_result.add(Box.createVerticalStrut(20));

        // side-by-side cards
        JPanel cards = new JPanel(new GridLayout(1, 2, 20, 0));
        cards.setOpaque(false);
        cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));
        cards.add(make_battle_card(p1, winner));
        cards.add(make_battle_card(p2, winner));
        pan_battle_result.add(cards);
        pan_battle_result.revalidate(); pan_battle_result.repaint();
    }

    private JPanel make_battle_card(JsonObject p, String winner) {
        String name = p.has("name") ? p.get("name").getAsString() : "?";
        int sc  = p.has("sc")  ? p.get("sc").getAsInt()  : 0;
        int exp = p.has("exp") ? p.get("exp").getAsInt() : 0;

        boolean is_winner = name.equals(winner);
        Color border_col = is_winner ? new Color(6,214,160) : new Color(60,42,112);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(23, 11, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border_col, is_winner ? 2 : 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        JLabel lbl_name = new JLabel((is_winner ? "🏆 " : "") + name);
        lbl_name.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl_name.setForeground(is_winner ? new Color(6,214,160) : new Color(0,240,255));
        lbl_name.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl_name);
        card.add(Box.createVerticalStrut(10));

        // score bar
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(sc);
        bar.setStringPainted(true);
        bar.setString(sc + "% Match");
        bar.setForeground(is_winner ? new Color(6,214,160) : new Color(247,37,133));
        bar.setBackground(new Color(40,30,70));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(bar);
        card.add(Box.createVerticalStrut(8));

        JLabel lbl_exp = new JLabel("Experience: " + exp + " years");
        lbl_exp.setForeground(Color.LIGHT_GRAY);
        lbl_exp.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lbl_exp.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl_exp);
        card.add(Box.createVerticalStrut(12));

        // matched skills
        if (p.has("hits") && !p.get("hits").isJsonNull()) {
            JsonArray hits = p.getAsJsonArray("hits");
            if (hits.size() > 0) {
                JLabel lbl_h = new JLabel("✔ Matched Skills:");
                lbl_h.setForeground(new Color(6,214,160));
                lbl_h.setFont(new Font("SansSerif", Font.BOLD, 12));
                lbl_h.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(lbl_h);
                StringBuilder sb = new StringBuilder("<html><body style='width:200px;color:#AAB'>");
                for (int i = 0; i < hits.size(); i++) sb.append(hits.get(i).getAsString()).append(i<hits.size()-1?", ":"");
                sb.append("</body></html>");
                JLabel lbl_hv = new JLabel(sb.toString());
                lbl_hv.setFont(new Font("SansSerif", Font.PLAIN, 12));
                lbl_hv.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(lbl_hv);
                card.add(Box.createVerticalStrut(8));
            }
        }

        // missing skills
        if (p.has("miss") && !p.get("miss").isJsonNull()) {
            JsonArray miss = p.getAsJsonArray("miss");
            if (miss.size() > 0) {
                JLabel lbl_m = new JLabel("✖ Missing:");
                lbl_m.setForeground(new Color(247,37,133));
                lbl_m.setFont(new Font("SansSerif", Font.BOLD, 12));
                lbl_m.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(lbl_m);
                StringBuilder sb = new StringBuilder("<html><body style='width:200px;color:#AAB'>");
                for (int i = 0; i < miss.size(); i++) sb.append(miss.get(i).getAsString()).append(i<miss.size()-1?", ":"");
                sb.append("</body></html>");
                JLabel lbl_mv = new JLabel(sb.toString());
                lbl_mv.setFont(new Font("SansSerif", Font.PLAIN, 12));
                lbl_mv.setAlignmentX(Component.LEFT_ALIGNMENT);
                card.add(lbl_mv);
            }
        }
        return card;
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
                int s = getInt(data.get(i).getAsJsonObject(), "sc");
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

    private void pop_edit(JsonObject job) {
        int id = getInt(job, "id", "ID");
        String t = getString(job, "title", "TITLE");
        String d = getString(job, "description", "DESCRIPTION");
        String s = getString(job, "status", "STATUS");

        JDialog diag = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Position", true);
        diag.setSize(500, 450);
        diag.setLocationRelativeTo(this);
        diag.getContentPane().setBackground(new Color(23, 11, 59));
        diag.setLayout(new BorderLayout(15, 15));

        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.weightx = 1.0;

        JTextField f_t = new JTextField(t);
        JTextArea f_d = new JTextArea(d, 8, 20);
        f_d.setLineWrap(true); f_d.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(f_d);
        JComboBox<String> f_s = new JComboBox<>(new String[]{"OPEN", "DRAFT", "ON_HOLD", "CLOSED"});
        f_s.setSelectedItem(s);

        c.gridy = 0; p.add(new JLabel("Job Title"), c);
        c.gridy = 1; p.add(f_t, c);
        c.gridy = 2; c.insets = new Insets(10, 0, 0, 0); p.add(new JLabel("Description"), c);
        c.gridy = 3; c.insets = new Insets(2, 0, 0, 0); p.add(sp, c);
        c.gridy = 4; p.add(new JLabel("Status"), c);
        c.gridy = 5; p.add(f_s, c);

        JButton b_ok = new JButton("SAVE CHANGES");
        b_ok.setBackground(new Color(6, 214, 160));
        b_ok.addActionListener(e -> {
            new Thread(() -> {
                String err = ApiClient.edit_job(id, f_t.getText().trim(), f_d.getText().trim(), (String) f_s.getSelectedItem());
                SwingUtilities.invokeLater(() -> {
                    if (err == null) {
                        diag.dispose();
                        ToastNotification.pop(this, "Job Updated ✔", true);
                        fetch_jobs();
                    }
                });
            }).start();
        });

        diag.add(p, BorderLayout.CENTER);
        diag.add(b_ok, BorderLayout.SOUTH);
        diag.setVisible(true);
    }

    private void pop_review(JsonObject app) {
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

        JLabel head = new JLabel("PROFILE: " + getString(app, "username", "USERNAME").toUpperCase());
        head.setFont(new Font("SansSerif", Font.BOLD, 26));
        head.setForeground(new Color(0, 240, 255));
        c.gridy = 0; c.insets = new Insets(0,0,20,0); p.add(head, c);

        // insights
        if (app.has("recs")) {
            JPanel box = new JPanel();
            box.setOpaque(false);
            box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
            box.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(60, 42, 112)), "AI Insights", 0, 0, null, Color.GRAY));

            JsonArray items = app.getAsJsonArray("recs");
            for (int i=0; i<items.size(); i++) {
                JLabel l = new JLabel("  " + items.get(i).getAsString());
                l.setForeground(Color.WHITE);
                l.setFont(new Font("SansSerif", Font.PLAIN, 14));
                box.add(Box.createVerticalStrut(5));
                box.add(l);
            }
            box.add(Box.createVerticalStrut(10));
            c.gridy = 1; c.insets = new Insets(0,0,20,0); p.add(box, c);
        }

        // Assessment Summary
        int ts = getInt(app, "tech_sc");
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setOpaque(false);
        JLabel lbl = new JLabel("Assessment Score: ");
        lbl.setForeground(Color.GRAY);
        JLabel val = new JLabel(ts < 0 ? "N/A" : ts + " / 5");
        val.setForeground(new Color(255, 190, 11));
        val.setFont(new Font("SansSerif", Font.BOLD, 18));
        row.add(lbl); row.add(val);
        c.gridy = 2; p.add(row, c);

        // SKILLS
        JLabel skl_h = new JLabel("Detected Capabilities:");
        skl_h.setForeground(new Color(176, 149, 246));
        skl_h.setFont(new Font("SansSerif", Font.BOLD, 14));
        c.gridy = 3; c.insets = new Insets(20,0,5,0); p.add(skl_h, c);
        String s_txt = getString(app, "hits");
        JTextArea area = new JTextArea(s_txt != null ? s_txt : "None detected");
        area.setOpaque(false); 
        area.setEditable(false); 
        area.setLineWrap(true); 
        area.setWrapStyleWord(true);
        area.setForeground(new Color(6, 214, 160));
        area.setFont(new Font("Monospaced", Font.BOLD, 14));
        c.gridy = 4; c.insets = new Insets(0,0,15,0); p.add(area, c);

        // vibe picker
        JPanel v_box = new JPanel(new FlowLayout(FlowLayout.LEFT));
        v_box.setOpaque(false);
        String cur_v = getString(app, "vibes", "");
        for (String v : VIBES) {
            JCheckBox cb = new JCheckBox(v);
            cb.setOpaque(false);
            cb.setForeground(Color.GRAY);
            if (cur_v != null && cur_v.contains(v)) {
                cb.setSelected(true);
                cb.setForeground(new Color(0, 240, 255));
            }
            cb.addActionListener(e -> {
                new Thread(() -> {
                    String update = "";
                    for (Component comp : v_box.getComponents()) {
                        if (comp instanceof JCheckBox && ((JCheckBox)comp).isSelected()) {
                            update += ((JCheckBox)comp).getText() + ",";
                        }
                    }
                    ApiClient.set_app_vibes(getInt(app, "app_id"), update);
                    SwingUtilities.invokeLater(() -> {
                       cb.setForeground(cb.isSelected() ? new Color(0, 240, 255) : Color.GRAY);
                    });
                }).start();
            });
            v_box.add(cb);
        }
        c.gridy = 5; c.insets = new Insets(0,0,20,0); p.add(v_box, c);

        // notes area
        int uid = getInt(app, "user_id", "USER_ID");
        JPanel pan_n = new JPanel(new BorderLayout(0, 5));
        pan_n.setOpaque(false);
        pan_n.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(60, 42, 112)), "Recruiter Private Notes", 0,0,null,Color.GRAY));
        
        DefaultListModel<String> mod_n = new DefaultListModel<>();
        JList<String> list_n = new JList<>(mod_n);
        list_n.setBackground(new Color(23, 11, 59));
        list_n.setForeground(Color.LIGHT_GRAY);
        JScrollPane sp_n = new JScrollPane(list_n);
        sp_n.setPreferredSize(new Dimension(0, 100));
        
        new Thread(() -> {
            JsonArray n = ApiClient.list_notes(uid);
            SwingUtilities.invokeLater(() -> {
                for (int i=0; i<n.size(); i++) mod_n.addElement(n.get(i).getAsJsonObject().get("note").getAsString());
            });
        }).start();

        JTextField f_note = new JTextField();
        f_note.addActionListener(e -> {
            String txt = f_note.getText().trim();
            if (txt.isEmpty()) return;
            new Thread(() -> {
               ApiClient.put_note(uid, txt);
               SwingUtilities.invokeLater(() -> { mod_n.add(0, txt); f_note.setText(""); });
            }).start();
        });

        pan_n.add(sp_n, BorderLayout.CENTER);
        pan_n.add(f_note, BorderLayout.SOUTH);
        c.gridy = 6; c.insets = new Insets(0,0,20,0); p.add(pan_n, c);

        diag.add(new JScrollPane(p), BorderLayout.CENTER);
        
        JPanel foot = new JPanel(new GridLayout(1, 2));
        JButton b_pdf = new JButton("EXTRACT ATS PDF");
        b_pdf.setBackground(new Color(6, 214, 160));
        b_pdf.setForeground(Color.BLACK);
        b_pdf.addActionListener(e -> {
            int pdf_uid = getInt(app, "user_id");
            new Thread(() -> {
               byte[] raw = ApiClient.get_pdf(pdf_uid);
               if (raw != null) {
                   try (FileOutputStream fos = new FileOutputStream("Candidate_Report_" + getString(app, "username") + ".pdf")) {
                       fos.write(raw);
                       SwingUtilities.invokeLater(() -> ToastNotification.pop(this, "PDF Exported to Root Folder!", true));
                   } catch (Exception ignored) {}
               }
            }).start();
        });

        JButton b_close = new JButton("DISMISS");
        b_close.setBackground(new Color(247, 37, 133));
        b_close.setForeground(Color.WHITE);
        b_close.addActionListener(e -> diag.dispose());

        foot.add(b_pdf);
        foot.add(b_close);
        foot.setPreferredSize(new Dimension(0, 50));
        diag.add(foot, BorderLayout.SOUTH);

        diag.setVisible(true);
    }

    // ================== SECTION: ANALYTICS ==================
    private JPanel pan_funnel;
    private JPanel pan_trends;

    private JPanel get_stats_pnl() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel h = new JLabel("Recruitment Analytics Dashboard");
        h.setFont(new Font("SansSerif", Font.BOLD, 28));
        h.setForeground(Color.WHITE);
        p.add(h, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(1, 2, 30, 0));
        grid.setOpaque(false);

        pan_funnel = new JPanel(new BorderLayout());
        pan_funnel.setBackground(new Color(23, 11, 59));
        pan_funnel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(48,31,95)), "Recruitment Funnel", 0, 0, null, new Color(0,240,255)));

        pan_trends = new JPanel(new BorderLayout());
        pan_trends.setBackground(new Color(23, 11, 59));
        pan_trends.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(48,31,95)), "Skill Demand Trends", 0, 0, null, new Color(176, 149, 246)));

        grid.add(pan_funnel);
        grid.add(pan_trends);
        p.add(grid, BorderLayout.CENTER);

        return p;
    }

    private void fetch_stats() {
        new Thread(() -> {
            JsonObject stats = ApiClient.get_adm_stats();
            SwingUtilities.invokeLater(() -> sync_stats_ui(stats));
        }).start();
    }

    private void sync_stats_ui(JsonObject data) {
        if (data == null) return;

        pan_funnel.removeAll();
        pan_trends.removeAll();

        int n_apps = data.get("totalApps").getAsInt();
        int n_sel = data.get("selectedCount").getAsInt();

        // funnel
        JPanel viz = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                
                // apps
                g2.setColor(new Color(60, 42, 112));
                g2.fillRoundRect(50, 50, w-100, 60, 15, 15);
                g2.setColor(Color.WHITE);
                g2.drawString("Total Applicants: " + n_apps, 70, 85);

                // selected
                int sw = (n_apps == 0) ? 0 : (int)((w-100) * (n_sel / (double)n_apps));
                g2.setColor(new Color(6, 214, 160));
                g2.fillRoundRect(50, 130, Math.max(50, sw), 60, 15, 15);
                g2.setColor(Color.BLACK);
                g2.drawString("Selected: " + n_sel, 70, 165);
            }
        };
        viz.setOpaque(false);
        pan_funnel.add(viz);

        // trends
        JsonObject trends = data.getAsJsonObject("skillTrends");
        JPanel t_list = new JPanel();
        t_list.setLayout(new BoxLayout(t_list, BoxLayout.Y_AXIS));
        t_list.setOpaque(false);
        t_list.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        trends.keySet().stream()
            .sorted((a,b) -> trends.get(b).getAsInt() - trends.get(a).getAsInt())
            .limit(10)
            .forEach(s -> {
                JLabel l = new JLabel(s.toUpperCase() + ": " + trends.get(s).getAsInt() + " jobs");
                l.setForeground(Color.LIGHT_GRAY);
                l.setFont(new Font("Monospaced", Font.BOLD, 14));
                t_list.add(l);
                t_list.add(Box.createVerticalStrut(8));
            });

        pan_trends.add(new JScrollPane(t_list));

        revalidate(); repaint();
    }
}
