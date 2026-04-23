package com.resudex.swing;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Role-specific technical quiz.
 */
public class QuizDialog extends JDialog {
    private int total = 0;
    private boolean done = false;
    private int idx = 0;
    private List<Q> list = new ArrayList<>();

    private JLabel lbl_q;
    private JRadioButton[] rd_opts = new JRadioButton[4];
    private ButtonGroup grp;
    private JButton b_next;
    private JLabel lbl_prog;

    public QuizDialog(Frame p, String t, String d) {
        super(p, "Technical Assessment: " + t, true);
        setSize(560, 440);
        setLocationRelativeTo(p);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(23, 11, 59));

        pick_qs(t + " " + d);

        if (list.isEmpty()) {
            done = true;
            total = 3;
            return;
        }

        // header
        JPanel pan_h = new JPanel(new BorderLayout());
        pan_h.setOpaque(false);
        pan_h.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));
        JLabel lbl_h = new JLabel("Quick Skill Check — " + t);
        lbl_h.setFont(new Font("SansSerif", Font.BOLD, 17));
        lbl_h.setForeground(new Color(0, 240, 255));
        lbl_prog = new JLabel("1 / " + list.size());
        lbl_prog.setForeground(new Color(150, 160, 180));
        lbl_prog.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pan_h.add(lbl_h, BorderLayout.WEST);
        pan_h.add(lbl_prog, BorderLayout.EAST);
        add(pan_h, BorderLayout.NORTH);

        // mid
        JPanel pan_mid = new JPanel(new GridBagLayout());
        pan_mid.setOpaque(false);
        pan_mid.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1.0;

        lbl_q = new JLabel();
        lbl_q.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl_q.setForeground(Color.WHITE);
        gbc.gridy = 0; pan_mid.add(lbl_q, gbc);

        grp = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            rd_opts[i] = new JRadioButton();
            rd_opts[i].setOpaque(false);
            rd_opts[i].setForeground(new Color(200, 210, 230));
            rd_opts[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            grp.add(rd_opts[i]);
            gbc.gridy = i + 1;
            pan_mid.add(rd_opts[i], gbc);
        }
        add(pan_mid, BorderLayout.CENTER);

        // footer
        JPanel pan_f = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pan_f.setOpaque(false);
        pan_f.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 20));
        b_next = new JButton("Next →");
        b_next.setBackground(new Color(247, 37, 133));
        b_next.setForeground(Color.WHITE);
        b_next.setFont(new Font("SansSerif", Font.BOLD, 14));
        b_next.putClientProperty("JButton.buttonType", "roundRect");
        b_next.setPreferredSize(new Dimension(160, 40));
        b_next.addActionListener(e -> go_next());
        pan_f.add(b_next);
        add(pan_f, BorderLayout.SOUTH);

        pop_q(0);
    }

    private void pick_qs(String ctx) {
        String c = ctx.toLowerCase();
        List<Q> pool = new ArrayList<>();

        if (c.contains("c++") || c.contains("cpp") || c.contains("stl") || c.contains("embedded")) {
            pool.add(new Q("What does STL stand for in C++?",
                "Standard Template Library",
                List.of("Standard Type Library", "Static Template Layer", "System Template Library")));
            pool.add(new Q("Which operator is used to access a member via a pointer?",
                "->",
                List.of(".", "::", "=>")));
            pool.add(new Q("What is a dangling pointer?",
                "A pointer pointing to freed memory",
                List.of("A null pointer", "An uninitialized pointer", "A pointer to a function")));
            pool.add(new Q("Which of these is NOT a C++ smart pointer?",
                "auto_ptr (deprecated)",
                List.of("unique_ptr", "shared_ptr", "weak_ptr")));
            pool.add(new Q("What is the output of: int x=5; cout << x++;",
                "5",
                List.of("6", "4", "Compile error")));
            pool.add(new Q("Which keyword prevents a class from being inherited in C++?",
                "final",
                List.of("sealed", "static", "const")));
            pool.add(new Q("What is the size of a pointer on a 64-bit system?",
                "8 bytes",
                List.of("4 bytes", "2 bytes", "Depends on type")));
        } else if (c.contains("java") && (c.contains("spring") || c.contains("backend") || c.contains("microservice"))) {
            pool.add(new Q("What annotation marks a Spring REST controller?",
                "@RestController",
                List.of("@Controller", "@Service", "@Component")));
            pool.add(new Q("Which HTTP method is idempotent and used to update a resource?",
                "PUT",
                List.of("POST", "PATCH", "DELETE")));
            pool.add(new Q("What does JPA stand for?",
                "Java Persistence API",
                List.of("Java Process API", "Java Platform API", "Java Package API")));
            pool.add(new Q("Which Spring annotation injects a dependency automatically?",
                "@Autowired",
                List.of("@Inject", "@Resource", "@Bean")));
            pool.add(new Q("What is the default scope of a Spring bean?",
                "Singleton",
                List.of("Prototype", "Request", "Session")));
            pool.add(new Q("Which status code means 'resource created successfully'?",
                "201",
                List.of("200", "204", "202")));
            pool.add(new Q("What does @Transactional do in Spring?",
                "Wraps method in a DB transaction",
                List.of("Marks a REST endpoint", "Enables caching", "Validates input")));
        } else if (c.contains("java")) {
            pool.add(new Q("What is JVM?", "Java Virtual Machine", List.of("Java Visual Machine", "Joint Virtual Machine", "Java Virtual Manager")));
            pool.add(new Q("Which keyword is used for inheritance?", "extends", List.of("implements", "inherits", "using")));
            pool.add(new Q("Which collection does NOT allow duplicates?", "Set", List.of("List", "ArrayList", "LinkedList")));
            pool.add(new Q("What is the default value of int in Java?", "0", List.of("null", "-1", "undefined")));
            pool.add(new Q("What does 'final' on a class mean?", "Cannot be inherited", List.of("Cannot be modified", "Is static", "Is private")));
            pool.add(new Q("Which interface must be implemented for sorting?", "Comparable", List.of("Serializable", "Cloneable", "Runnable")));
        } else if (c.contains("python") && (c.contains("data") || c.contains("pandas") || c.contains("ml") || c.contains("machine learning"))) {
            pool.add(new Q("Which library is used for data manipulation in Python?",
                "pandas",
                List.of("numpy", "scipy", "matplotlib")));
            pool.add(new Q("What does df.dropna() do in pandas?",
                "Removes rows with null values",
                List.of("Drops all columns", "Fills nulls with 0", "Renames columns")));
            pool.add(new Q("Which function splits data into train/test sets in sklearn?",
                "train_test_split",
                List.of("split_data", "data_split", "test_train_split")));
            pool.add(new Q("What is a DataFrame in pandas?",
                "A 2D labeled data structure",
                List.of("A 1D array", "A database table", "A Python dictionary")));
            pool.add(new Q("Which numpy function creates an array of zeros?",
                "np.zeros()",
                List.of("np.empty()", "np.null()", "np.blank()")));
            pool.add(new Q("What does 'fit' do in a sklearn model?",
                "Trains the model on data",
                List.of("Tests the model", "Scales the data", "Splits the data")));
        } else if (c.contains("python")) {
            pool.add(new Q("Which keyword defines a function?", "def", List.of("func", "function", "lambda")));
            pool.add(new Q("How do you start a comment in Python?", "#", List.of("//", "/*", "--")));
            pool.add(new Q("Which is a mutable data type?", "List", List.of("Tuple", "String", "Frozenset")));
            pool.add(new Q("What does 'pip' do?", "Installs Python packages", List.of("Runs Python scripts", "Compiles Python", "Debugs Python")));
            pool.add(new Q("What is a lambda in Python?", "An anonymous function", List.of("A loop", "A class", "A module")));
            pool.add(new Q("Which method removes the last item from a list?", "pop()", List.of("remove()", "delete()", "discard()")));
        } else if (c.contains("react") || c.contains("frontend") || c.contains("full stack")) {
            pool.add(new Q("Which hook manages component state?", "useState", List.of("useEffect", "useContext", "useRef")));
            pool.add(new Q("What does JSX stand for?", "JavaScript XML", List.of("JavaScript Extension", "Java Syntax Extension", "JSON XML")));
            pool.add(new Q("What is the Virtual DOM?", "An in-memory representation of the real DOM", List.of("A browser API", "A CSS framework", "A database")));
            pool.add(new Q("Which hook runs after every render?", "useEffect", List.of("useState", "useMemo", "useCallback")));
            pool.add(new Q("How do you pass data to a child component?", "Props", List.of("State", "Context", "Refs")));
            pool.add(new Q("What does 'key' prop help React with?", "Identifying list items uniquely", List.of("Styling elements", "Event handling", "API calls")));
            pool.add(new Q("Which method updates component state?", "setState / useState setter", List.of("updateState()", "changeState()", "modifyState()")));
        } else if (c.contains("docker") || c.contains("kubernetes") || c.contains("devops") || c.contains("aws") || c.contains("ci/cd")) {
            pool.add(new Q("What is a Docker container?",
                "A lightweight isolated runtime environment",
                List.of("A virtual machine", "A cloud server", "A build tool")));
            pool.add(new Q("Which command builds a Docker image?",
                "docker build",
                List.of("docker run", "docker create", "docker start")));
            pool.add(new Q("What does Kubernetes do?",
                "Orchestrates containerized applications",
                List.of("Builds Docker images", "Monitors servers", "Manages databases")));
            pool.add(new Q("What is a Kubernetes Pod?",
                "The smallest deployable unit in K8s",
                List.of("A Docker image", "A cluster node", "A load balancer")));
            pool.add(new Q("What does CI/CD stand for?",
                "Continuous Integration / Continuous Delivery",
                List.of("Code Integration / Code Delivery", "Continuous Inspection / Continuous Deploy", "Central Integration / Central Delivery")));
            pool.add(new Q("Which AWS service runs serverless functions?",
                "AWS Lambda",
                List.of("AWS EC2", "AWS S3", "AWS RDS")));
            pool.add(new Q("What is a Dockerfile?",
                "A script to build a Docker image",
                List.of("A container log file", "A Kubernetes config", "A CI pipeline file")));
        } else if (c.contains("sql") || c.contains("database") || c.contains("data engineer")) {
            pool.add(new Q("Which SQL clause filters grouped results?", "HAVING", List.of("WHERE", "GROUP BY", "ORDER BY")));
            pool.add(new Q("What does INNER JOIN return?", "Matching rows from both tables", List.of("All rows from left table", "All rows from right table", "All rows from both tables")));
            pool.add(new Q("Which normal form eliminates transitive dependencies?", "3NF", List.of("1NF", "2NF", "BCNF")));
            pool.add(new Q("What does ACID stand for in databases?", "Atomicity, Consistency, Isolation, Durability", List.of("Access, Control, Index, Data", "Atomic, Concurrent, Isolated, Durable", "Availability, Consistency, Integrity, Durability")));
            pool.add(new Q("Which index type is best for range queries?", "B-Tree index", List.of("Hash index", "Bitmap index", "Full-text index")));
            pool.add(new Q("What is a foreign key?", "A column referencing a primary key in another table", List.of("A unique identifier", "An indexed column", "A computed column")));
        } else {
            // General CS fallback
            pool.add(new Q("What is the time complexity of Binary Search?", "O(log n)", List.of("O(n)", "O(1)", "O(n²)")));
            pool.add(new Q("Which data structure uses LIFO?", "Stack", List.of("Queue", "Tree", "Graph")));
            pool.add(new Q("What does REST stand for?", "Representational State Transfer", List.of("Remote State Transfer", "Reliable State Transfer", "Resource State Transfer")));
            pool.add(new Q("What is a deadlock?", "Two processes waiting on each other indefinitely", List.of("A memory leak", "An infinite loop", "A null pointer error")));
            pool.add(new Q("Which HTTP status means 'Not Found'?", "404", List.of("500", "403", "301")));
            pool.add(new Q("What is Big O notation used for?", "Describing algorithm time/space complexity", List.of("Measuring memory usage", "Counting lines of code", "Describing network speed")));
        }

        Collections.shuffle(pool);
        list = new ArrayList<>(pool.subList(0, Math.min(5, pool.size())));
    }

    private void pop_q(int i) {
        Q q = list.get(i);
        lbl_q.setText("<html><body style='width:420px'>" + (i + 1) + ". " + q.txt + "</body></html>");
        lbl_prog.setText((i + 1) + " / " + list.size());

        List<String> items = new ArrayList<>(q.wrongs);
        items.add(q.ok);
        Collections.shuffle(items);

        for (int x = 0; x < 4; x++) {
            rd_opts[x].setText(items.get(x));
            rd_opts[x].setSelected(false);
            rd_opts[x].setActionCommand(items.get(x));
        }

        b_next.setText(i == list.size() - 1 ? "Finish ✔" : "Next →");
    }

    private void go_next() {
        ButtonModel sel = grp.getSelection();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Please select an answer first.");
            return;
        }
        if (sel.getActionCommand().equals(list.get(idx).ok)) total++;
        idx++;
        if (idx < list.size()) pop_q(idx);
        else { done = true; dispose(); }
    }

    public int getScore()      { return total; }
    public boolean isCompleted() { return done; }

    private static class Q {
        String txt, ok;
        List<String> wrongs;
        Q(String t, String c, List<String> d) { txt = t; ok = c; wrongs = d; }
    }
}
