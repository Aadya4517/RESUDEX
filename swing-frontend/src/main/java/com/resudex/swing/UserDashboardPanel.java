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

    private CardLayout c_lay;
    private JPanel pan_mid;
    private File f_cv = null;
    private JLabel lbl_drop;
    private JButton b_up;
    private JLabel lbl_cv_msg;

    private JPanel pan_jobs; 
    private JTextField f_search;
    private JsonArray cache_jobs = new JsonArray();

    // sections
    private JPanel pan_apps;
    private JPanel pan_recs;
    
    private JProgressBar bar_line;
    private Timer t_notif;

    public UserDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(13, 2, 33)); // Deep Voltage Violet

        // left side
        add(get_side_pan(), BorderLayout.WEST);

        // mid area
        c_lay = new CardLayout();
        pan_mid = new JPanel(c_lay);
        pan_mid.setOpaque(false);

        pan_mid.add(get_recs_pan(), "RECS");
        pan_mid.add(get_apps_pan(), "APPS");
        pan_mid.add(get_sync_pan(), "SYNC");
        pan_mid.add(get_prof_pan(), "PROF");
        pan_mid.add(get_progress_pan(), "PROGRESS");

        add(pan_mid, BorderLayout.CENTER);

        // default view
        c_lay.show(pan_mid, "RECS"); 

        // loading bits
        fetch_recs();
        fetch_apps();
        poll_notifs();
    }

    private void poll_notifs() {
        t_notif = new Timer(8000, e -> {
            new Thread(() -> {
                JsonArray ns = ApiClient.list_notifs(ResudexApp.uid);
                if (ns.size() > 0) {
                    for (int i=0; i<ns.size(); i++) {
                        JsonObject x = ns.get(i).getAsJsonObject();
                        int nid = getInt(x, "id");
                        String txt = getString(x, "message");
                        
                        SwingUtilities.invokeLater(() -> {
                            ToastNotification.pop(this, txt, true);
                            ApiClient.done_notif(nid);
                            fetch_apps(); // refresh list
                        });
                    }
                }
            }).start();
        });
        t_notif.start();
    }

    private JPanel get_side_pan() {
        JPanel s = new JPanel();
        s.setPreferredSize(new Dimension(240, 0));
        s.setBackground(new Color(23, 11, 59));
        s.setLayout(new BorderLayout());
        s.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(48, 31, 95)));

        // branding
        JPanel box_logo = new JPanel(new BorderLayout());
        box_logo.setOpaque(false);
        box_logo.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));
        JLabel lbl_logo = new JLabel("RESUDEX");
        lbl_logo.setFont(new Font("SansSerif", Font.BOLD, 26));
        lbl_logo.setForeground(new Color(0, 240, 255));
        box_logo.add(lbl_logo, BorderLayout.CENTER);

        // navigation
        JPanel pan_nav = new JPanel();
        pan_nav.setOpaque(false);
        pan_nav.setLayout(new BoxLayout(pan_nav, BoxLayout.Y_AXIS));
        pan_nav.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JButton b_recs = make_nav_b("  Opportunities", "🎯");
        JButton b_apps = make_nav_b("  My Applications", "📋");
        JButton b_sync = make_nav_b("  Sync Resume", "📄");
        JButton b_prof = make_nav_b("  My Profile", "👤");
        JButton b_prog = make_nav_b("  My Progress", "📈");
        
        JButton b_pdf = make_nav_b("  Export ATS PDF", "📄");
        b_pdf.addActionListener(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080/api/resume/get_pdf/" + ResudexApp.uid));
            } catch (Exception ex) {}
        });
        
        b_recs.addActionListener(e -> { c_lay.show(pan_mid, "RECS"); fetch_recs(); });
        b_apps.addActionListener(e -> { c_lay.show(pan_mid, "APPS"); fetch_apps(); });
        b_sync.addActionListener(e -> c_lay.show(pan_mid, "SYNC"));
        b_prof.addActionListener(e -> c_lay.show(pan_mid, "PROF"));
        b_prog.addActionListener(e -> { c_lay.show(pan_mid, "PROGRESS"); fetch_progress(); });

        pan_nav.add(b_recs);
        pan_nav.add(Box.createVerticalStrut(10));
        pan_nav.add(b_apps);
        pan_nav.add(Box.createVerticalStrut(10));
        pan_nav.add(b_sync);
        pan_nav.add(Box.createVerticalStrut(10));
        pan_nav.add(b_prof);
        pan_nav.add(Box.createVerticalStrut(10));
        pan_nav.add(b_prog);
        pan_nav.add(Box.createVerticalStrut(10));
        pan_nav.add(b_pdf);

        // footer
        JPanel box_foot = new JPanel(new BorderLayout());
        box_foot.setOpaque(false);
        box_foot.setBorder(BorderFactory.createEmptyBorder(20, 20, 25, 20));
        
        JLabel lbl_usr = new JLabel("@" + ResudexApp.usr);
        lbl_usr.setForeground(new Color(150, 170, 190));
        lbl_usr.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton b_exit = new JButton("Sign Out");
        b_exit.putClientProperty("JButton.buttonType", "borderless");
        b_exit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b_exit.setForeground(new Color(255, 100, 100));
        b_exit.addActionListener(e -> ResudexApp.go_home());

        box_foot.add(lbl_usr, BorderLayout.CENTER);
        box_foot.add(b_exit, BorderLayout.SOUTH);

        s.add(box_logo, BorderLayout.NORTH);
        s.add(pan_nav,   BorderLayout.CENTER);
        s.add(box_foot, BorderLayout.SOUTH);

        return s;
    }

    private JButton make_nav_b(String txt, String ico) {
        JButton b = new JButton(ico + txt);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        b.setFont(new Font("SansSerif", Font.BOLD, 15));
        b.setForeground(new Color(176, 149, 246)); 
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.putClientProperty("JButton.buttonType", "borderless");
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // Job Explorer methods removed as they were replaced by 'Opportunities' section

    private JPanel make_empty_pan(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(msg, SwingConstants.CENTER);
        l.setFont(new Font("SansSerif", Font.PLAIN, 18));
        l.setForeground(new Color(120, 130, 150));
        p.add(l);
        return p;
    }

    private JPanel make_job_card(JsonObject j) {
        String t = getString(j, "title", "TITLE");
        String d = getString(j, "description", "DESCRIPTION");
        int    sc    = getInt(j, "sc", "SCORE");

        // get skill sets
        String hits = "";
        String miss = "";
        if (j.has("hits") && !j.get("hits").isJsonNull()) {
            JsonArray arr = j.get("hits").getAsJsonArray();
            if (arr.size() > 0) hits = arr.toString().replace("\"", "").replace("[", "").replace("]", "").replace(",", ", ");
        }
        if (j.has("miss") && !j.get("miss").isJsonNull()) {
            JsonArray arr = j.get("miss").getAsJsonArray();
            if (arr.size() > 0) miss = arr.toString().replace("\"", "").replace("[", "").replace("]", "").replace(",", ", ");
        }

        JPanel card = new JPanel(new BorderLayout(20, 10));
        card.setBackground(new Color(32, 21, 71)); 
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 42, 112), 1, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        JPanel L = new JPanel();
        L.setOpaque(false);
        L.setLayout(new BoxLayout(L, BoxLayout.Y_AXIS));

        JPanel row_top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        row_top.setOpaque(false);

        JLabel lbl_t = new JLabel(t);
        lbl_t.setFont(new Font("SansSerif", Font.BOLD, 19));
        lbl_t.setForeground(new Color(0, 240, 255)); 
        
        JLabel bar_fit = new JLabel(sc + "% MATCH");
        bar_fit.setFont(new Font("SansSerif", Font.BOLD, 11));
        bar_fit.setOpaque(true);
        bar_fit.setForeground(Color.WHITE);
        bar_fit.setBackground(sc > 75 ? new Color(6, 214, 160) : (sc > 30 ? new Color(255, 159, 28) : new Color(80, 80, 100)));
        bar_fit.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        row_top.add(lbl_t);
        row_top.add(bar_fit);

        JTextArea area_d = new JTextArea(d);
        area_d.setOpaque(false);
        area_d.setEditable(false);
        area_d.setFont(new Font("SansSerif", Font.PLAIN, 13));
        area_d.setForeground(new Color(220, 230, 245));
        area_d.setWrapStyleWord(true);
        area_d.setLineWrap(true);
        area_d.setRows(2);

        L.add(row_top);
        L.add(Box.createVerticalStrut(8));
        L.add(area_d);

        // insights
        if (!hits.isEmpty() || !miss.isEmpty()) {
            L.add(Box.createVerticalStrut(12));
            JPanel pan_info = new JPanel(new GridLayout(0, 1, 0, 4));
            pan_info.setOpaque(false);
            
            if (!hits.isEmpty()) {
                JLabel l = new JLabel("✔ Matched: " + hits);
                l.setFont(new Font("SansSerif", Font.BOLD, 12));
                l.setForeground(new Color(6, 214, 160));
                pan_info.add(l);
            } else {
                String req = j.has("mandatorySkills") ? j.get("mandatorySkills").getAsJsonArray().toString().replace("[","").replace("]","").replace("\"","") : "General Fit";
                JLabel l = new JLabel("🎯 Goal Skills: " + req);
                l.setFont(new Font("SansSerif", Font.ITALIC, 11));
                l.setForeground(new Color(150, 160, 180));
                pan_info.add(l);
            }
            if (!miss.isEmpty()) {
                JLabel l = new JLabel("✖ Missing: " + miss);
                l.setFont(new Font("SansSerif", Font.BOLD, 12));
                l.setForeground(new Color(247, 37, 133));
                pan_info.add(l);
            }
            L.add(pan_info);
        }

        JPanel R = new JPanel(new BorderLayout());
        R.setOpaque(false);

        JButton b_ok = new JButton("Quick Apply");
        b_ok.setBackground(new Color(247, 37, 133)); 
        b_ok.setForeground(Color.WHITE);
        b_ok.setFont(new Font("SansSerif", Font.BOLD, 13));
        b_ok.putClientProperty("JButton.buttonType", "roundRect");
        b_ok.setPreferredSize(new Dimension(140, 40));

        JButton b_cl = new JButton("INSIGHT ✨");
        b_cl.setBackground(new Color(0, 240, 255));
        b_cl.setForeground(Color.BLACK);
        b_cl.setFont(new Font("SansSerif", Font.BOLD, 12));
        b_cl.putClientProperty("JButton.buttonType", "roundRect");
        b_cl.setPreferredSize(new Dimension(140, 35));

        b_cl.addActionListener(e -> pop_magic(j));

        b_ok.addActionListener(e -> {
            QuizDialog qd = new QuizDialog((Frame)SwingUtilities.getWindowAncestor(this), t, d);
            qd.setVisible(true);
            
            if (!qd.isCompleted()) return;

            int score_raw = qd.getScore();
            int jid = getInt(j, "id");
            b_ok.setEnabled(false);
            b_ok.setText("Applying...");
            new Thread(() -> {
                String err = ApiClient.apply_to_job(ResudexApp.uid, jid, score_raw);
                SwingUtilities.invokeLater(() -> {
                    if (err == null) {
                        b_ok.setText("Applied ✔");
                        ToastNotification.pop(this, "Quiz Score: " + score_raw + "/5 | Applied!", true);
                        fetch_apps();
                    } else {
                        b_ok.setText("Apply");
                        b_ok.setEnabled(true);
                        ToastNotification.pop(this, err, false);
                    }
                });
            }).start();
        });

        JPanel box_btns = new JPanel(new GridLayout(2, 1, 0, 8));
        box_btns.setOpaque(false);
        box_btns.add(b_ok);
        box_btns.add(b_cl);

        R.add(box_btns, BorderLayout.SOUTH);

        card.add(L, BorderLayout.CENTER);
        card.add(R, BorderLayout.EAST);
        return card;
    }

    // ================== SECTION: MY APPLICATIONS ==================
    private JPanel get_apps_pan() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel h = new JLabel("My Applications");
        h.setFont(new Font("SansSerif", Font.BOLD, 28));
        h.setForeground(Color.WHITE);
        
        JPanel box_h = new JPanel(new BorderLayout());
        box_h.setOpaque(false);
        box_h.add(h, BorderLayout.WEST);
        box_h.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        p.add(box_h, BorderLayout.NORTH);

        pan_apps = new JPanel();
        pan_apps.setLayout(new BoxLayout(pan_apps, BoxLayout.Y_AXIS));
        pan_apps.setOpaque(false);

        JScrollPane sp = new JScrollPane(pan_apps);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void sync_apps(JsonArray ns) {
        pan_apps.removeAll();
        if (ns.size() == 0) {
            pan_apps.add(make_empty_pan("You haven't applied to any jobs yet."));
        } else {
            for (int i = 0; i < ns.size(); i++) {
                JsonObject x = ns.get(i).getAsJsonObject();
                pan_apps.add(make_app_card(x));
                pan_apps.add(Box.createVerticalStrut(20));
            }
        }
        pan_apps.revalidate();
        pan_apps.repaint();
    }

    private JPanel make_app_card(JsonObject x) {
        String t = getString(x, "title", "TITLE");
        String s = getString(x, "status", "STATUS", "APPLIED");
        String f = getString(x, "feedback", "FEEDBACK");

        JPanel card = new JPanel(new BorderLayout(20, 10));
        card.setBackground(new Color(32, 38, 48));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 70, 90), 1, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel L = new JPanel();
        L.setOpaque(false);
        L.setLayout(new BoxLayout(L, BoxLayout.Y_AXIS));

        JLabel lbl_t = new JLabel(t);
        lbl_t.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl_t.setForeground(new Color(0, 240, 255));

        L.add(lbl_t);
        L.add(Box.createVerticalStrut(5));
        
        JLabel lbl_id = new JLabel("Application ID: #" + getString(x, "app_id", "APP_ID"));
        lbl_id.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl_id.setForeground(new Color(150, 160, 180));
        L.add(lbl_id);

        if (f != null && !f.isBlank()) {
            L.add(Box.createVerticalStrut(10));
            JLabel lbl_fb = new JLabel("💬 Recruiter Feedback: " + f);
            lbl_fb.setFont(new Font("SansSerif", Font.ITALIC, 12));
            lbl_fb.setForeground(new Color(255, 190, 11)); 
            L.add(lbl_fb);
        }

        JPanel R = new JPanel(new BorderLayout());
        R.setOpaque(false);

        JLabel lbl_st = new JLabel(s.toUpperCase());
        lbl_st.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl_st.setOpaque(true);
        lbl_st.setHorizontalAlignment(SwingConstants.CENTER);
        lbl_st.setPreferredSize(new Dimension(140, 35));
        
        if (s.equalsIgnoreCase("SELECTED")) {
            lbl_st.setBackground(new Color(6, 214, 160));
            lbl_st.setForeground(Color.BLACK);
        } else {
            lbl_st.setBackground(new Color(60, 42, 112));
            lbl_st.setForeground(Color.WHITE);
        }
        
        R.add(lbl_st, BorderLayout.CENTER);

        card.add(L, BorderLayout.CENTER);
        card.add(R, BorderLayout.EAST);
        return card;
    }

    private JPanel get_prof_pan() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(new Color(23, 11, 59));
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(48, 31, 95), 1, true),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        box.setPreferredSize(new Dimension(500, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0; gbc.weightx = 1.0;

        JLabel lbl_h = new JLabel("My Profile Settings");
        lbl_h.setFont(new Font("SansSerif", Font.BOLD, 24));
        lbl_h.setForeground(new Color(0, 240, 255));
        gbc.gridy = 0; box.add(lbl_h, gbc);

        bar_line = new JProgressBar(0, 100);
        bar_line.setStringPainted(true);
        bar_line.setString("Profile Strength: 0%");
        bar_line.setForeground(new Color(6, 214, 160));
        bar_line.setBackground(new Color(48, 31, 95));
        gbc.gridy = 1; box.add(bar_line, gbc);
        gbc.gridy = 2; box.add(Box.createVerticalStrut(10), gbc);

        JTextField f_name = new JTextField();
        f_name.setPreferredSize(new Dimension(300, 40));
        f_name.putClientProperty("JTextField.placeholderText", "Your Full Name");

        JTextField f_mail = new JTextField();
        f_mail.setPreferredSize(new Dimension(300, 40));
        f_mail.putClientProperty("JTextField.placeholderText", "Contact Email");

        JTextArea f_bio = new JTextArea(4, 20);
        f_bio.setLineWrap(true); f_bio.setWrapStyleWord(true);
        f_bio.putClientProperty("JTextField.placeholderText", "Brief professional bio...");
        JScrollPane sp_bio = new JScrollPane(f_bio);

        JButton b_save = new JButton("UPDATE PROFILE");
        b_save.setBackground(new Color(247, 37, 133));
        b_save.setForeground(Color.WHITE);
        b_save.putClientProperty("JButton.buttonType", "roundRect");
        b_save.setPreferredSize(new Dimension(200, 45));

        gbc.gridy = 3; box.add(new JLabel("Full Name"), gbc);
        gbc.gridy = 4; box.add(f_name, gbc);
        gbc.gridy = 5; box.add(new JLabel("Email Address"), gbc);
        gbc.gridy = 6; box.add(f_mail, gbc);
        gbc.gridy = 7; box.add(new JLabel("Professional Bio"), gbc);
        gbc.gridy = 8; box.add(sp_bio, gbc);
        gbc.gridy = 9; box.add(Box.createVerticalStrut(20), gbc);
        gbc.gridy = 10; box.add(b_save, gbc);

        p.add(box);

        // load up
        new Thread(() -> {
            JsonObject x = ApiClient.see_profile(ResudexApp.uid);
            if (x != null && !x.has("error")) {
                SwingUtilities.invokeLater(() -> {
                    f_name.setText(getString(x, "full_name"));
                    f_mail.setText(getString(x, "email"));
                    f_bio.setText(getString(x, "bio"));
                    calc_prof_line(x);
                });
            }
        }).start();

        b_save.addActionListener(e -> {
            b_save.setEnabled(false);
            new Thread(() -> {
                String error = ApiClient.set_profile(ResudexApp.uid, f_name.getText(), f_mail.getText(), f_bio.getText());
                SwingUtilities.invokeLater(() -> {
                    b_save.setEnabled(true);
                    if (error == null) {
                        ToastNotification.pop(this, "Profile Updated ✔", true);
                        new Thread(() -> {
                            JsonObject x2 = ApiClient.see_profile(ResudexApp.uid);
                            SwingUtilities.invokeLater(() -> calc_prof_line(x2));
                        }).start();
                    }
                    else ToastNotification.pop(this, error, false);
                });
            }).start();
        });

        return p;
    }

    private void calc_prof_line(JsonObject x) {
        int val = 0;
        if (getString(x, "full_name") != null && !getString(x, "full_name").isEmpty()) val += 25;
        if (getString(x, "email") != null && !getString(x, "email").isEmpty()) val += 25;
        if (getString(x, "bio") != null && !getString(x, "bio").isEmpty()) val += 25;
        if (getString(x, "resume_text") != null && !getString(x, "resume_text").isEmpty()) val += 25;
        
        bar_line.setValue(val);
        bar_line.setString("Profile Strength: " + val + "%");
    }

    // ================== SECTION: RESUME SYNC ==================
    private JPanel get_sync_pan() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);

        JPanel pan_dz = new JPanel(new GridBagLayout());
        pan_dz.setBackground(new Color(23, 11, 59));
        pan_dz.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(new Color(114, 9, 183), 2, 5, 2, true),
            BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));
        pan_dz.setPreferredSize(new Dimension(500, 350));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.fill = GridBagConstraints.CENTER;

        JLabel lbl_ico = new JLabel("☁️");
        lbl_ico.setFont(new Font("SansSerif", Font.PLAIN, 64));
        lbl_ico.setForeground(new Color(100, 160, 255));

        lbl_drop = new JLabel("Select your Resume (PDF/DOCX)", SwingConstants.CENTER);
        lbl_drop.setFont(new Font("SansSerif", Font.BOLD, 18));
        lbl_drop.setForeground(new Color(200, 210, 230));

        JButton b_pick = new JButton("Browse Files");
        b_pick.putClientProperty("JButton.buttonType", "roundRect");
        b_pick.setPreferredSize(new Dimension(200, 45));

        b_up = new JButton("SYNC PROFILE");
        b_up.setBackground(new Color(0, 120, 215));
        b_up.setForeground(Color.WHITE);
        b_up.putClientProperty("JButton.buttonType", "roundRect");
        b_up.setPreferredSize(new Dimension(200, 45));
        b_up.setEnabled(false);

        lbl_cv_msg = new JLabel(" ");
        lbl_cv_msg.setFont(new Font("SansSerif", Font.ITALIC, 13));

        JProgressBar bar_up = new JProgressBar();
        bar_up.setIndeterminate(true);
        bar_up.setVisible(false);
        bar_up.setForeground(new Color(114, 9, 183));
        bar_up.setPreferredSize(new Dimension(300, 8));

        pan_dz.setTransferHandler(new TransferHandler() {
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
                        f_cv = files.get(0);
                        lbl_drop.setText("Ready: " + f_cv.getName());
                        b_up.setEnabled(true);
                        pan_dz.setBackground(new Color(35, 45, 60));
                        return true;
                    }
                } catch (Exception ex) { ex.printStackTrace(); }
                return false;
            }
        });

        b_pick.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                f_cv = jfc.getSelectedFile();
                lbl_drop.setText("Ready: " + f_cv.getName());
                b_up.setEnabled(true);
                pan_dz.setBackground(new Color(35, 45, 60));
            }
        });

        b_up.addActionListener(e -> {
            b_up.setEnabled(false);
            b_up.setText("Uploading...");
            bar_up.setVisible(true);
            pan_dz.revalidate(); pan_dz.repaint();
            new Thread(() -> {
                String error = ApiClient.push_cv(ResudexApp.uid, f_cv);
                SwingUtilities.invokeLater(() -> {
                    b_up.setEnabled(true);
                    b_up.setText("SYNC PROFILE");
                    bar_up.setVisible(false);
                    if (error == null) {
                        lbl_cv_msg.setText("✔ Resume successfully analyzed and synced.");
                        lbl_cv_msg.setForeground(new Color(120, 255, 150));
                        ToastNotification.pop(this, "Profile Synced & Analyzed!", true);
                        fetch_recs(); // refresh fits
                        
                        // update strength
                        new Thread(() -> {
                            JsonObject x = ApiClient.see_profile(ResudexApp.uid);
                            if (x != null) SwingUtilities.invokeLater(() -> calc_prof_line(x));
                        }).start();
                        
                        // show results
                        new Thread(() -> {
                            JsonObject stats = ApiClient.get_cv_stats(ResudexApp.uid);
                            SwingUtilities.invokeLater(() -> pop_stats(stats));
                        }).start();
                    } else {
                        lbl_cv_msg.setText("✖ Sync error: " + error);
                        lbl_cv_msg.setForeground(new Color(255, 120, 120));
                        ToastNotification.pop(this, "Upload Failed", false);
                    }
                });
            }).start();
        });

        gbc.gridy = 0; pan_dz.add(lbl_ico, gbc);
        gbc.gridy = 1; pan_dz.add(Box.createVerticalStrut(20), gbc);
        gbc.gridy = 2; pan_dz.add(lbl_drop, gbc);
        gbc.gridy = 3; pan_dz.add(Box.createVerticalStrut(20), gbc);
        gbc.gridy = 4; pan_dz.add(bar_up, gbc);
        gbc.gridy = 5; pan_dz.add(Box.createVerticalStrut(20), gbc);
        gbc.gridy = 6; pan_dz.add(b_pick, gbc);
        gbc.gridy = 7; pan_dz.add(Box.createVerticalStrut(15), gbc);
        gbc.gridy = 8; pan_dz.add(b_up, gbc);
        gbc.gridy = 9; pan_dz.add(Box.createVerticalStrut(15), gbc);
        gbc.gridy = 10; pan_dz.add(lbl_cv_msg, gbc);

        p.add(pan_dz);
        return p;
    }

    // loadJobs is deprecated in favor of loadRecommendations
    private void fetch_jobs() {
        fetch_recs();
    }

    // ================== SECTION: MY PROGRESS ==================
    private JPanel pan_progress_content;

    private JPanel get_progress_pan() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel h = new JLabel("My Skill Trajectory 📈");
        h.setFont(new Font("SansSerif", Font.BOLD, 28));
        h.setForeground(Color.WHITE);
        JPanel box_h = new JPanel(new BorderLayout());
        box_h.setOpaque(false);
        box_h.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        box_h.add(h, BorderLayout.WEST);
        p.add(box_h, BorderLayout.NORTH);

        pan_progress_content = new JPanel();
        pan_progress_content.setLayout(new BoxLayout(pan_progress_content, BoxLayout.Y_AXIS));
        pan_progress_content.setOpaque(false);

        JScrollPane sp = new JScrollPane(pan_progress_content);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void fetch_progress() {
        new Thread(() -> {
            JsonArray snaps = ApiClient.get_trajectory(ResudexApp.uid);
            SwingUtilities.invokeLater(() -> render_progress(snaps));
        }).start();
    }

    private void render_progress(JsonArray snaps) {
        pan_progress_content.removeAll();

        if (snaps.size() == 0) {
            pan_progress_content.add(make_empty_pan("No history yet — sync your resume to start tracking!"));
            pan_progress_content.revalidate(); pan_progress_content.repaint();
            return;
        }

        String[] dom_keys  = {"java_sc","web_sc","python_sc","cpp_sc","devops_sc","db_sc"};
        String[] dom_names = {"Java","Web","Python","C/C++","DevOps","Databases"};
        Color[]  colors    = {new Color(58,134,255), new Color(6,214,160), new Color(255,209,102),
                               new Color(239,71,111), new Color(17,138,178), new Color(131,56,236)};

        JsonObject latest = snaps.get(snaps.size()-1).getAsJsonObject();
        JsonObject first  = snaps.get(0).getAsJsonObject();

        // summary card
        int best_dom = 0; String best_name = "General";
        for (int i = 0; i < dom_keys.length; i++) {
            int v = getInt(latest, dom_keys[i]);
            if (v > best_dom) { best_dom = v; best_name = dom_names[i]; }
        }
        JPanel sum = new JPanel(new BorderLayout());
        sum.setBackground(new Color(23, 11, 59));
        sum.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 240, 255), 1, true),
            BorderFactory.createEmptyBorder(18, 25, 18, 25)));
        sum.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        JLabel lbl_sum = new JLabel("<html><b style='color:#00F0FF;font-size:15px'>📊 " +
            snaps.size() + " resume upload" + (snaps.size()>1?"s":"") + " tracked</b><br>" +
            "<span style='color:#B0B8C8'>Strongest domain: <b>" + best_name + " (" + best_dom + "%)</b>" +
            " &nbsp;|&nbsp; Experience: <b>" + getInt(latest, "exp_yrs") + " yrs</b></span></html>");
        sum.add(lbl_sum, BorderLayout.CENTER);
        pan_progress_content.add(sum);
        pan_progress_content.add(Box.createVerticalStrut(18));

        // domain bars with delta
        JPanel bars_pan = new JPanel(new GridLayout(dom_keys.length, 1, 0, 10));
        bars_pan.setOpaque(false);
        for (int i = 0; i < dom_keys.length; i++) {
            int cur   = getInt(latest, dom_keys[i]);
            int old   = snaps.size() > 1 ? getInt(first, dom_keys[i]) : cur;
            int delta = cur - old;

            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);

            JLabel lbl_name = new JLabel(dom_names[i]);
            lbl_name.setForeground(Color.LIGHT_GRAY);
            lbl_name.setFont(new Font("SansSerif", Font.BOLD, 13));
            lbl_name.setPreferredSize(new Dimension(80, 24));

            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(cur);
            bar.setForeground(colors[i]);
            bar.setBackground(new Color(40, 30, 70));

            String ds = delta > 0 ? "▲ +" + delta + "%" : delta < 0 ? "▼ " + delta + "%" : cur + "%";
            Color  dc = delta > 0 ? new Color(6,214,160) : delta < 0 ? new Color(239,71,111) : Color.GRAY;
            JLabel lbl_val = new JLabel(cur + "%  " + ds);
            lbl_val.setForeground(dc);
            lbl_val.setFont(new Font("SansSerif", Font.BOLD, 12));
            lbl_val.setPreferredSize(new Dimension(110, 24));

            row.add(lbl_name, BorderLayout.WEST);
            row.add(bar,      BorderLayout.CENTER);
            row.add(lbl_val,  BorderLayout.EAST);
            bars_pan.add(row);
        }
        JPanel bars_card = new JPanel(new BorderLayout());
        bars_card.setBackground(new Color(23, 11, 59));
        bars_card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 42, 112), 1, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)));
        bars_card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 310));
        JLabel bt = new JLabel("Domain Expertise — Current vs First Upload");
        bt.setFont(new Font("SansSerif", Font.BOLD, 14));
        bt.setForeground(new Color(176, 149, 246));
        bt.setBorder(BorderFactory.createEmptyBorder(0,0,12,0));
        bars_card.add(bt,       BorderLayout.NORTH);
        bars_card.add(bars_pan, BorderLayout.CENTER);
        pan_progress_content.add(bars_card);
        pan_progress_content.add(Box.createVerticalStrut(18));

        // upload history
        JPanel hist = new JPanel();
        hist.setLayout(new BoxLayout(hist, BoxLayout.Y_AXIS));
        hist.setBackground(new Color(23, 11, 59));
        hist.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 42, 112), 1, true),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)));
        hist.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        JLabel ht = new JLabel("Upload History");
        ht.setFont(new Font("SansSerif", Font.BOLD, 14));
        ht.setForeground(new Color(176, 149, 246));
        ht.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        hist.add(ht);

        for (int i = snaps.size()-1; i >= 0; i--) {
            JsonObject s = snaps.get(i).getAsJsonObject();
            String file = getString(s, "filename");
            int exp     = getInt(s, "exp_yrs");
            int top_v = 0; String top_d = "General";
            for (int k = 0; k < dom_keys.length; k++) {
                int v = getInt(s, dom_keys[k]);
                if (v > top_v) { top_v = v; top_d = dom_names[k]; }
            }
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            JLabel lbl = new JLabel("<html><b style='color:#00F0FF'>#" + (i+1) + "</b>  " +
                "<span style='color:#DDE'>" + (file != null ? file : "resume") + "</span>  " +
                "<span style='color:#8A9BB0'>| Top: " + top_d + " " + top_v + "% | Exp: " + exp + " yrs</span></html>");
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
            row.add(lbl, BorderLayout.CENTER);
            hist.add(row);
        }
        pan_progress_content.add(hist);
        pan_progress_content.revalidate();
        pan_progress_content.repaint();
    }
    private void fetch_apps() {
        new Thread(() -> {
            JsonArray ns = ApiClient.usr_history(ResudexApp.uid);
            SwingUtilities.invokeLater(() -> {
                sync_apps(ns);
            });
        }).start();
    }

    private void pop_stats(JsonObject x) {
        if (x == null || x.has("error")) return;

        JDialog diag = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Resume Insights", true);
        diag.setSize(600, 500);
        diag.setLocationRelativeTo(this);
        diag.getContentPane().setBackground(new Color(23, 11, 59));
        diag.setLayout(new BorderLayout(20, 20));

        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        JLabel head = new JLabel("AI Resume Analysis");
        head.setFont(new Font("SansSerif", Font.BOLD, 22));
        head.setForeground(new Color(0, 240, 255));
        gbc.gridy = 0; p.add(head, gbc);

        int exp = x.get("exp_yrs").getAsInt();
        JLabel lbl_exp = new JLabel("Estimated Experience: " + exp + " years");
        lbl_exp.setForeground(Color.WHITE);
        lbl_exp.setFont(new Font("SansSerif", Font.BOLD, 15));
        gbc.gridy = 1; gbc.insets = new Insets(10, 0, 20, 0); p.add(lbl_exp, gbc);

        JLabel lbl_dom = new JLabel("Domain Expertise Fit:");
        lbl_dom.setForeground(new Color(176, 149, 246));
        lbl_dom.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridy = 2; gbc.insets = new Insets(0, 0, 5, 0); p.add(lbl_dom, gbc);

        JsonObject map_fit = x.getAsJsonObject("domain_fit");
        int row = 3;
        for (String dom : map_fit.keySet()) {
            int val = map_fit.get(dom).getAsInt();
            if (val > 0) {
                JPanel box_bar = new JPanel(new BorderLayout(10, 0));
                box_bar.setOpaque(false);
                JLabel lbl_d = new JLabel(dom);
                lbl_d.setForeground(Color.LIGHT_GRAY);
                lbl_d.setPreferredSize(new Dimension(120, 25));
                
                JProgressBar bar = new JProgressBar(0, 100);
                bar.setValue(val);
                bar.setStringPainted(true);
                bar.setForeground(new Color(6, 214, 160));
                bar.setBackground(new Color(48, 31, 95));
                
                box_bar.add(lbl_d, BorderLayout.WEST);
                box_bar.add(bar, BorderLayout.CENTER);
                gbc.gridy = row++; gbc.insets = new Insets(2, 0, 2, 0);
                p.add(box_bar, gbc);
            }
        }

        diag.add(p, BorderLayout.CENTER);
        
        JButton b_exit = new JButton("Awesome!");
        b_exit.setBackground(new Color(247, 37, 133));
        b_exit.setForeground(Color.WHITE);
        b_exit.addActionListener(e -> diag.dispose());
        diag.add(b_exit, BorderLayout.SOUTH);

        diag.setVisible(true);
    }

    // ================== SECTION: RECOMMENDATIONS ==================
    private JPanel get_recs_pan() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel h = new JLabel("Opportunities for You");
        h.setFont(new Font("SansSerif", Font.BOLD, 28));
        h.setForeground(new Color(0, 240, 255));

        JPanel box_h = new JPanel(new BorderLayout());
        box_h.setOpaque(false);
        box_h.add(h, BorderLayout.WEST);
        box_h.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        p.add(box_h, BorderLayout.NORTH);

        pan_recs = new JPanel();
        pan_recs.setLayout(new BoxLayout(pan_recs, BoxLayout.Y_AXIS));
        pan_recs.setOpaque(false);

        JScrollPane sp = new JScrollPane(pan_recs);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setUnitIncrement(16);

        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void fetch_recs() {
        new Thread(() -> {
            JsonArray recs = ApiClient.list_match_jobs(ResudexApp.uid);
            SwingUtilities.invokeLater(() -> {
                JsonArray filtered = new JsonArray();
                for (int i=0; i<recs.size(); i++) {
                    JsonObject j = recs.get(i).getAsJsonObject();
                    if (getInt(j, "sc", "SC") >= 0) filtered.add(j);
                }
                
                java.util.List<JsonObject> items = new java.util.ArrayList<>();
                for (int i=0; i<filtered.size(); i++) items.add(filtered.get(i).getAsJsonObject());
                items.sort((a,b) -> getInt(b, "sc", "SC") - getInt(a, "sc", "SC"));
                
                JsonArray res = new JsonArray();
                for (JsonObject s : items) res.add(s);
                sync_recs(res);
            });
        }).start();
    }

    private void sync_recs(JsonArray ns) {
        pan_recs.removeAll();
        if (ns.size() == 0) {
            pan_recs.add(make_empty_pan("No high-match roles yet! ⚡ Try syncing a detailed resume to unlock personalized 'Best Fits'."));
        } else {
            for (int i = 0; i < ns.size(); i++) {
                pan_recs.add(make_job_card(ns.get(i).getAsJsonObject()));
                pan_recs.add(Box.createVerticalStrut(20));
            }
        }
        pan_recs.revalidate();
        pan_recs.repaint();
    }

    private void pop_letter(String t, String txt) {
        JDialog diag = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "AI Generated Cover Letter", true);
        diag.setSize(600, 650);
        diag.setLocationRelativeTo(this);
        diag.getContentPane().setBackground(new Color(13, 2, 33));
        diag.setLayout(new BorderLayout(15, 15));

        JTextArea area = new JTextArea(txt);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setForeground(Color.WHITE);
        area.setBackground(new Color(23, 11, 59));
        area.setMargin(new Insets(20, 20, 20, 20));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);

        JScrollPane sp = new JScrollPane(area);
        sp.setBorder(BorderFactory.createLineBorder(new Color(48, 31, 95)));
        
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        p.add(new JLabel("<html><b style='color:#00F0FF; font-size:16px;'>Personalized for: " + t + "</b></html>"), BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);

        JButton b_copy = new JButton("Copy to Clipboard");
        b_copy.addActionListener(e -> {
            java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(txt);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            ToastNotification.pop(this, "Copied!", true);
        });

        diag.add(p, BorderLayout.CENTER);
        diag.add(b_copy, BorderLayout.SOUTH);
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

    private void pop_magic(JsonObject j) {
        String t = getString(j, "title");
        JsonArray hits = j.has("hits") ? j.getAsJsonArray("hits") : new JsonArray();
        JsonArray miss = j.has("miss") ? j.getAsJsonArray("miss") : new JsonArray();
        JsonArray road = j.has("roadmap") ? j.getAsJsonArray("roadmap") : new JsonArray();

        JDialog diag = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Magic Match Insight", true);
        diag.setSize(580, 700);
        diag.setLocationRelativeTo(this);
        diag.getContentPane().setBackground(new Color(13, 2, 33));
        diag.setLayout(new BorderLayout());

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel head = new JLabel("LEVEL UP: " + t.toUpperCase());
        head.setFont(new Font("SansSerif", Font.BOLD, 22));
        head.setForeground(new Color(0, 240, 255));
        head.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(head);
        p.add(Box.createVerticalStrut(25));

        // Heatmap
        JPanel heat = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = 35;
                int gap = 8;
                int cols = 6;
                for (int i=0; i<hits.size() + miss.size(); i++) {
                    int r = i / cols;
                    int c = i % cols;
                    int x = c * (size + gap);
                    int y = r * (size + gap);
                    if (i < hits.size()) {
                        g2.setPaint(new GradientPaint(x, y, new Color(6, 214, 160), x+size, y+size, new Color(3, 107, 80)));
                        g2.fillRoundRect(x, y, size, size, 8, 8);
                        g2.setColor(new Color(255, 255, 255, 100));
                        g2.drawString("✔", x + 12, y + 22);
                    } else {
                        g2.setColor(new Color(255, 255, 255, 10));
                        g2.fillRoundRect(x, y, size, size, 8, 8);
                        g2.setColor(new Color(255, 255, 255, 30));
                        g2.drawRoundRect(x, y, size, size, 8, 8);
                    }
                }
                g2.dispose();
            }
        };
        heat.setOpaque(false);
        heat.setPreferredSize(new Dimension(300, 150));
        heat.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel h_lbl = new JLabel("Match Strength Heatmap");
        h_lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        h_lbl.setForeground(Color.GRAY);
        h_lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        p.add(h_lbl);
        p.add(Box.createVerticalStrut(10));
        p.add(heat);
        p.add(Box.createVerticalStrut(30));

        // Roadmap
        JPanel rd = new JPanel();
        rd.setOpaque(false);
        rd.setLayout(new BoxLayout(rd, BoxLayout.Y_AXIS));
        rd.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(247, 37, 133), 1),
            "THE ROADMAP", 0, 0, new Font("SansSerif", Font.BOLD, 13), new Color(247, 37, 133)));

        if (road.size() == 0) {
            JLabel l = new JLabel(" You are a perfect fit! No steps needed.");
            l.setForeground(Color.WHITE);
            rd.add(l);
        } else {
            for (int i = 0; i < road.size(); i++) {
                String step = road.get(i).getAsString();
                Color col;
                if (step.startsWith("🎯") || step.startsWith("🗺️") || step.startsWith("⏱️") || step.startsWith("📌"))
                    col = new Color(0, 240, 255);
                else if (step.startsWith("💡") || step.startsWith("📝"))
                    col = new Color(6, 214, 160);
                else
                    col = new Color(220, 230, 245);

                JLabel l = new JLabel("<html><body style='width:380px; padding:2px 0'>" + step + "</body></html>");
                l.setForeground(col);
                l.setFont(new Font("SansSerif", step.startsWith("Step") ? Font.PLAIN : Font.BOLD, 13));
                l.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                rd.add(l);
                if (i < road.size() - 1)
                    rd.add(Box.createVerticalStrut(2));
            }
        }
        p.add(rd);

        JButton b_close = new JButton("I GOT THIS");
        b_close.addActionListener(e -> diag.dispose());
        
        diag.add(new JScrollPane(p), BorderLayout.CENTER);
        diag.add(b_close, BorderLayout.SOUTH);
        diag.setVisible(true);
    }
}
