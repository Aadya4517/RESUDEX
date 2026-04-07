package com.resudex.swing;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class QuizDialog extends JDialog {
    private int score = 0;
    private boolean completed = false;
    private int currentQuestionIndex = 0;
    private List<Question> selectedQuestions = new ArrayList<>();

    private JLabel questionLabel;
    private JRadioButton[] options = new JRadioButton[4];
    private ButtonGroup group;
    private JButton nextBtn;

    public QuizDialog(Frame owner, String jobTitle, String jobDescription) {
        super(owner, "Technical Assessment: " + jobTitle, true);
        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(23, 11, 59));

        selectQuestions(jobTitle + " " + jobDescription);

        if (selectedQuestions.isEmpty()) {
            completed = true;
            score = 3; // Automatic pass if no questions found
            return;
        }

        // --- Header ---
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setOpaque(false);
        JLabel title = new JLabel("Quick Skill Check");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setForeground(new Color(0, 240, 255));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // --- Center contents ---
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 20, 10, 20);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;

        questionLabel = new JLabel("Question text here?");
        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        questionLabel.setForeground(Color.WHITE);
        c.gridy = 0; center.add(questionLabel, c);

        group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            options[i] = new JRadioButton();
            options[i].setOpaque(false);
            options[i].setForeground(new Color(200, 200, 220));
            group.add(options[i]);
            c.gridy = i + 1; center.add(options[i], c);
        }

        add(center, BorderLayout.CENTER);

        // --- Footer ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        nextBtn = new JButton("Next Question");
        nextBtn.setBackground(new Color(247, 37, 133));
        nextBtn.setForeground(Color.WHITE);
        nextBtn.addActionListener(e -> nextQuestion());
        footer.add(nextBtn);
        add(footer, BorderLayout.SOUTH);

        showQuestion(0);
    }

    private void selectQuestions(String text) {
        text = text.toLowerCase();
        List<Question> pool = new ArrayList<>();

        if (text.contains("java")) {
            pool.add(new Question("What is JVM stands for?", "Java Virtual Machine", List.of("Java Visual Machine", "Joint Virtual Machine", "Java Virtual Manager")));
            pool.add(new Question("Which keyword is used for inheritance?", "extends", List.of("implements", "inherits", "using")));
            pool.add(new Question("Is Java platform-independent?", "Yes", List.of("No", "Only on Windows", "Depends on the IDE")));
            pool.add(new Question("Which collection allows duplicate elements?", "List", List.of("Set", "Map", "Queue")));
            pool.add(new Question("What is the default value of a boolean in Java?", "false", List.of("true", "null", "undefined")));
            pool.add(new Question("Which class is the root of all classes in Java?", "Object", List.of("System", "Runtime", "Class")));
            pool.add(new Question("What does 'final' keyword on a class mean?", "Cannot be inherited", List.of("Cannot be modified", "Static member", "Private member")));
        } else if (text.contains("python")) {
            pool.add(new Question("Which keyword is used to define a function?", "def", List.of("func", "function", "lambda")));
            pool.add(new Question("What is the extension of Python files?", ".py", List.of(".python", ".pt", ".p")));
            pool.add(new Question("Is Python indentation-sensitive?", "Yes", List.of("No", "Optional", "Only for classes")));
            pool.add(new Question("How do you start a comment in Python?", "#", List.of("//", "/*", "--")));
            pool.add(new Question("Which of these is a mutable data type?", "List", List.of("Tuple", "String", "Int")));
            pool.add(new Question("What does 'pip' stand for?", "Pip Installs Packages", List.of("Python Install Point", "Preferred Install Proc", "Point Install Part")));
        } else if (text.contains("react") || text.contains("web")) {
            pool.add(new Question("Which hook is used for side effects?", "useEffect", List.of("useState", "useContext", "useMemo")));
            pool.add(new Question("What does JSX stand for?", "JavaScript XML", List.of("JavaScript Extension", "Java Syntax Extension", "JSON XML")));
            pool.add(new Question("Component names must start with...", "Capital letter", List.of("Lowercase", "$", "_")));
            pool.add(new Question("Virtual DOM helps React to...", "Optimize updates", List.of("Render faster", "Manage State", "Style elements")));
            pool.add(new Question("Which keyword exports a component?", "export", List.of("output", "send", "module.out")));
        } else {
            // General CS
            pool.add(new Question("What is the time complexity of a Binary Search?", "O(log n)", List.of("O(n)", "O(1)", "O(n log n)")));
            pool.add(new Question("Which data structure uses FIFO?", "Queue", List.of("Stack", "Tree", "Graph")));
            pool.add(new Question("What does HTTP stand for?", "HyperText Transfer Protocol", List.of("HyperText Text Protocol", "High Transfer Protocol", "Hyper Transfer Protocol")));
            pool.add(new Question("What is the decimal value of binary 1010?", "10", List.of("8", "12", "15")));
            pool.add(new Question("Which SQL statement is used to extract data?", "SELECT", List.of("GET", "QUERY", "EXTRACT")));
        }

        Collections.shuffle(pool);
        selectedQuestions = pool.subList(0, Math.min(5, pool.size())); // Ask 5 questions
    }

    private void showQuestion(int index) {
        Question q = selectedQuestions.get(index);
        questionLabel.setText("<html><body style='width: 400px;'>" + (index + 1) + ". " + q.text + "</body></html>");
        
        List<String> allOptions = new ArrayList<>(q.distractors);
        allOptions.add(q.correct);
        Collections.shuffle(allOptions);

        for (int i = 0; i < 4; i++) {
            options[i].setText(allOptions.get(i));
            options[i].setSelected(false);
            options[i].setActionCommand(allOptions.get(i));
        }

        if (index == selectedQuestions.size() - 1) {
            nextBtn.setText("Finish Assessment");
        }
    }

    private void nextQuestion() {
        ButtonModel sel = group.getSelection();
        if (sel == null) {
            JOptionPane.showMessageDialog(this, "Please select an answer.");
            return;
        }

        if (sel.getActionCommand().equals(selectedQuestions.get(currentQuestionIndex).correct)) {
            score++;
        }

        currentQuestionIndex++;
        if (currentQuestionIndex < selectedQuestions.size()) {
            showQuestion(currentQuestionIndex);
        } else {
            completed = true;
            dispose();
        }
    }

    public int getScore() { return score; }
    public boolean isCompleted() { return completed; }

    private static class Question {
        String text;
        String correct;
        List<String> distractors;
        Question(String t, String c, List<String> d) { this.text = t; this.correct = c; this.distractors = d; }
    }
}
