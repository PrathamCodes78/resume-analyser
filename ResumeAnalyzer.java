import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

public class ResumeAnalyzer extends JFrame {

    // ─── Colors (Black Theme) ───────────────────────────────
    static final Color BG_DARK      = new Color(0, 0, 0);        // Pure black background
    static final Color BG_CARD      = new Color(15, 15, 15);     // Card background
    static final Color BG_INPUT     = new Color(20, 20, 20);     // Input background
    static final Color ACCENT       = new Color(99, 102, 241);   // Purple accent
    static final Color ACCENT_HOVER = new Color(79, 82, 221);    // Button hover
    static final Color TEXT_WHITE   = new Color(240, 240, 240);  // Main text
    static final Color TEXT_GREY    = new Color(120, 120, 120);  // Subtext
    static final Color GREEN        = new Color(34, 197, 94);    // Excellent
    static final Color BLUE         = new Color(59, 130, 246);   // Good
    static final Color ORANGE       = new Color(245, 158, 11);   // Partial
    static final Color RED          = new Color(239, 68, 68);    // Poor
    static final Color BORDER       = new Color(35, 35, 35);     // Card border

    // ─── Fonts ──────────────────────────────────────────────
    static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FONT_LABEL    = new Font("Segoe UI", Font.BOLD, 12);
    static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FONT_SCORE    = new Font("Segoe UI", Font.BOLD, 48);
    static final Font FONT_VERDICT  = new Font("Segoe UI", Font.BOLD, 18);
    static final Font FONT_BTN      = new Font("Segoe UI", Font.BOLD, 14);

    // ─── API ────────────────────────────────────────────────
    // 🔁 Change this to your Render URL after deployment
    static final String API_URL = "http://localhost:5000/analyze";

    // ─── State ──────────────────────────────────────────────
    File selectedFile = null;

    // ─── UI Components ──────────────────────────────────────
    JLabel fileLabel;
    JTextArea jobDescArea;
    JButton analyzeBtn;

    // Result panels
    JPanel resultPanel;
    JLabel scoreLabel, verdictLabel, messageLabel, emojiLabel;
    JProgressBar progressBar;
    JLabel resumeWordsLabel, jobWordsLabel, matchedCountLabel;
    JPanel matchedPanel, missingPanel;
    JTextArea adviceArea;

    // ────────────────────────────────────────────────────────
    public ResumeAnalyzer() {
        setTitle("AI Resume Analyzer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 900);
        setLocationRelativeTo(null);
        setBackground(BG_DARK);
        setResizable(true);

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setBackground(BG_DARK);
        main.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Header
        main.add(buildHeader());
        main.add(Box.createVerticalStrut(20));

        // Upload Card
        main.add(buildCard("STEP 1 — Upload Your Resume (PDF)", buildUploadPanel()));
        main.add(Box.createVerticalStrut(14));

        // Job Description Card
        main.add(buildCard("STEP 2 — Paste Job Description", buildJobDescPanel()));
        main.add(Box.createVerticalStrut(20));

        // Analyze Button
        analyzeBtn = buildButton("🔍  Analyze My Resume");
        analyzeBtn.addActionListener(e -> runAnalysis());
        main.add(analyzeBtn);
        main.add(Box.createVerticalStrut(24));

        // Result Panel (hidden initially)
        resultPanel = buildResultPanel();
        resultPanel.setVisible(false);
        main.add(resultPanel);

        // Scroll
        JScrollPane scroll = new JScrollPane(main);
        scroll.setBackground(BG_DARK);
        scroll.getViewport().setBackground(BG_DARK);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll);

        setVisible(true);
    }

    // ─── Header ─────────────────────────────────────────────
    JPanel buildHeader() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setAlignmentX(CENTER_ALIGNMENT);

        JLabel icon = new JLabel("📄", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        icon.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("AI Resume Analyzer", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_WHITE);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Compare your resume with any job description", SwingConstants.CENTER);
        sub.setFont(FONT_SUBTITLE);
        sub.setForeground(TEXT_GREY);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        p.add(icon);
        p.add(Box.createVerticalStrut(6));
        p.add(title);
        p.add(Box.createVerticalStrut(4));
        p.add(sub);
        return p;
    }

    // ─── Card Wrapper ────────────────────────────────────────
    JPanel buildCard(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel lbl = new JLabel(title);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_GREY);
        lbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        card.add(lbl, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height + 60));
        return card;
    }

    // ─── Upload Panel ────────────────────────────────────────
    JPanel buildUploadPanel() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setBackground(BG_CARD);

        fileLabel = new JLabel("No file selected");
        fileLabel.setFont(FONT_BODY);
        fileLabel.setForeground(TEXT_GREY);

        JButton browseBtn = new JButton("Choose PDF");
        styleSmallButton(browseBtn);
        browseBtn.addActionListener(e -> chooseFile());

        p.add(fileLabel, BorderLayout.CENTER);
        p.add(browseBtn, BorderLayout.EAST);
        return p;
    }

    // ─── Job Description Panel ───────────────────────────────
    JPanel buildJobDescPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_CARD);

        jobDescArea = new JTextArea(7, 40);
        jobDescArea.setFont(FONT_BODY);
        jobDescArea.setForeground(TEXT_WHITE);
        jobDescArea.setBackground(BG_INPUT);
        jobDescArea.setCaretColor(TEXT_WHITE);
        jobDescArea.setLineWrap(true);
        jobDescArea.setWrapStyleWord(true);
        jobDescArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Placeholder
        jobDescArea.setText("Paste the full job description here...");
        jobDescArea.setForeground(TEXT_GREY);
        jobDescArea.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (jobDescArea.getText().startsWith("Paste the full")) {
                    jobDescArea.setText("");
                    jobDescArea.setForeground(TEXT_WHITE);
                }
            }
            public void focusLost(FocusEvent e) {
                if (jobDescArea.getText().isEmpty()) {
                    jobDescArea.setText("Paste the full job description here...");
                    jobDescArea.setForeground(TEXT_GREY);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(jobDescArea);
        scroll.setBorder(new LineBorder(BORDER, 1));
        scroll.setBackground(BG_INPUT);
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }

    // ─── Main Button ─────────────────────────────────────────
    JButton buildButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(ACCENT); }
        });
        return btn;
    }

    void styleSmallButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 34));
    }

    // ─── Result Panel ────────────────────────────────────────
    JPanel buildResultPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);

        // Score Card
        JPanel scoreCard = new JPanel();
        scoreCard.setLayout(new BoxLayout(scoreCard, BoxLayout.Y_AXIS));
        scoreCard.setBackground(BG_CARD);
        scoreCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(24, 24, 24, 24)
        ));

        emojiLabel   = centeredLabel("", new Font("Segoe UI Emoji", Font.PLAIN, 36), TEXT_WHITE);
        verdictLabel = centeredLabel("", FONT_VERDICT, TEXT_WHITE);
        scoreLabel   = centeredLabel("", FONT_SCORE, ACCENT);
        messageLabel = centeredLabel("", FONT_BODY, TEXT_GREY);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setBackground(new Color(30, 30, 30));
        progressBar.setForeground(ACCENT);
        progressBar.setBorderPainted(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        progressBar.setAlignmentX(CENTER_ALIGNMENT);

        scoreCard.add(emojiLabel);
        scoreCard.add(Box.createVerticalStrut(6));
        scoreCard.add(verdictLabel);
        scoreCard.add(Box.createVerticalStrut(4));
        scoreCard.add(scoreLabel);
        scoreCard.add(Box.createVerticalStrut(10));
        scoreCard.add(progressBar);
        scoreCard.add(Box.createVerticalStrut(10));
        scoreCard.add(messageLabel);

        p.add(scoreCard);
        p.add(Box.createVerticalStrut(14));

        // Stats Row
        JPanel statsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        statsRow.setBackground(BG_DARK);
        resumeWordsLabel  = new JLabel("-", SwingConstants.CENTER);
        jobWordsLabel     = new JLabel("-", SwingConstants.CENTER);
        matchedCountLabel = new JLabel("-", SwingConstants.CENTER);
        statsRow.add(statBox(resumeWordsLabel,  "Resume Words"));
        statsRow.add(statBox(jobWordsLabel,     "Job Desc Words"));
        statsRow.add(statBox(matchedCountLabel, "Keywords Matched"));
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        p.add(statsRow);
        p.add(Box.createVerticalStrut(14));

        // Matched Keywords
        matchedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        matchedPanel.setBackground(BG_CARD);
        p.add(buildCard("✅  MATCHED KEYWORDS", matchedPanel));
        p.add(Box.createVerticalStrut(14));

        // Missing Keywords
        missingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        missingPanel.setBackground(BG_CARD);
        p.add(buildCard("❌  MISSING KEYWORDS  (Add these to your resume)", missingPanel));
        p.add(Box.createVerticalStrut(14));

        // Advice
        adviceArea = new JTextArea(5, 40);
        adviceArea.setFont(FONT_BODY);
        adviceArea.setForeground(TEXT_WHITE);
        adviceArea.setBackground(new Color(20, 20, 40));
        adviceArea.setEditable(false);
        adviceArea.setLineWrap(true);
        adviceArea.setWrapStyleWord(true);
        adviceArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane adviceScroll = new JScrollPane(adviceArea);
        adviceScroll.setBorder(new LineBorder(new Color(60, 60, 100), 1));
        p.add(buildCard("💡  SUGGESTIONS TO IMPROVE YOUR RESUME", adviceScroll));

        return p;
    }

    JPanel statBox(JLabel valLabel, String labelText) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(BG_CARD);
        box.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(12, 10, 12, 10)
        ));
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLabel.setForeground(ACCENT);
        valLabel.setAlignmentX(CENTER_ALIGNMENT);
        JLabel lbl = new JLabel(labelText, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(TEXT_GREY);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        box.add(valLabel);
        box.add(Box.createVerticalStrut(4));
        box.add(lbl);
        return box;
    }

    JLabel centeredLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        return lbl;
    }

    // ─── File Chooser ────────────────────────────────────────
    void chooseFile() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = fc.getSelectedFile();
            fileLabel.setText("📄 " + selectedFile.getName());
            fileLabel.setForeground(TEXT_WHITE);
        }
    }

    // ─── Run Analysis ─────────────────────────────────────────
    void runAnalysis() {
        String jobDesc = jobDescArea.getText().trim();

        if (selectedFile == null) {
            showError("Please choose a PDF resume first.");
            return;
        }
        if (jobDesc.isEmpty() || jobDesc.startsWith("Paste the full")) {
            showError("Please paste a job description.");
            return;
        }
        if (jobDesc.length() < 20) {
            showError("Job description is too short. Please enter more details.");
            return;
        }

        analyzeBtn.setEnabled(false);
        analyzeBtn.setText("⏳  Analyzing...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            protected String doInBackground() throws Exception {
                return sendRequest(selectedFile, jobDesc);
            }
            protected void done() {
                try {
                    String response = get();
                    parseAndShow(response);
                } catch (Exception ex) {
                    showError("Connection failed. Make sure Flask backend is running.\n" + ex.getMessage());
                } finally {
                    analyzeBtn.setEnabled(true);
                    analyzeBtn.setText("🔍  Analyze My Resume");
                }
            }
        };
        worker.execute();
    }

    // ─── HTTP Multipart Request ───────────────────────────────
    String sendRequest(File pdfFile, String jobDesc) throws Exception {
        String boundary = "----JavaBoundary" + System.currentTimeMillis();
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            // PDF file part
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"resume\"; filename=\"" + pdfFile.getName() + "\"\r\n");
            out.writeBytes("Content-Type: application/pdf\r\n\r\n");
            out.write(Files.readAllBytes(pdfFile.toPath()));
            out.writeBytes("\r\n");

            // Job description part
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"job_description\"\r\n\r\n");
            out.writeBytes(jobDesc + "\r\n");
            out.writeBytes("--" + boundary + "--\r\n");
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        return sb.toString();
    }

    // ─── Parse JSON & Display Result ─────────────────────────
    void parseAndShow(String json) {
        // Simple JSON parser (no external library needed)
        double score      = parseDouble(json, "score");
        String verdict    = parseStr(json, "verdict");
        String emoji      = parseStr(json, "emoji");
        String color      = parseStr(json, "color");
        String message    = parseStr(json, "message");
        int resumeWords   = (int) parseDouble(json, "resume_word_count");
        int jobWords      = (int) parseDouble(json, "job_word_count");
        String matchedRaw = parseArray(json, "matched_keywords");
        String missingRaw = parseArray(json, "missing_keywords");

        String[] matched = splitKeywords(matchedRaw);
        String[] missing = splitKeywords(missingRaw);

        // Set score card color
        Color cardColor = switch (color) {
            case "green"  -> GREEN;
            case "blue"   -> BLUE;
            case "orange" -> ORANGE;
            default       -> RED;
        };

        emojiLabel.setText(emoji + " ");
        verdictLabel.setText(verdict);
        verdictLabel.setForeground(cardColor);
        scoreLabel.setText(score + "%");
        scoreLabel.setForeground(cardColor);
        messageLabel.setText("<html><div style='text-align:center;width:500px'>" + message + "</div></html>");
        progressBar.setForeground(cardColor);
        progressBar.setValue((int) score);

        resumeWordsLabel.setText(String.valueOf(resumeWords));
        jobWordsLabel.setText(String.valueOf(jobWords));
        matchedCountLabel.setText(String.valueOf(matched.length));

        // Matched keywords (green tags)
        matchedPanel.removeAll();
        if (matched.length == 0 || matched[0].isEmpty()) {
            matchedPanel.add(grayLabel("No strong keyword matches found"));
        } else {
            for (String kw : matched) matchedPanel.add(keywordTag("✓ " + kw, GREEN, new Color(10, 40, 20)));
        }

        // Missing keywords (red tags)
        missingPanel.removeAll();
        if (missing.length == 0 || missing[0].isEmpty()) {
            missingPanel.add(grayLabel("Great! No critical keywords missing"));
        } else {
            for (String kw : missing) missingPanel.add(keywordTag("✗ " + kw, RED, new Color(40, 10, 10)));
        }

        // Advice
        StringBuilder advice = new StringBuilder();
        if (score < 75) advice.append("• Add more keywords from the job description naturally into your resume.\n");
        if (missing.length > 3) {
            advice.append("• Consider adding these missing skills if you have them: ");
            for (int i = 0; i < Math.min(5, missing.length); i++) advice.append(missing[i]).append(i < 4 ? ", " : ".\n");
            advice.append("\n");
        }
        if (score < 55) advice.append("• Rewrite your resume summary to align with the job role.\n");
        if (score < 35) advice.append("• Tailor this resume specifically for this job posting.\n");
        advice.append("• Quantify your achievements (e.g. 'Improved performance by 30%').\n");
        advice.append("• Make sure your Skills section lists all relevant technologies.\n");
        advice.append("• Use action verbs: developed, built, designed, managed, optimized.\n");
        adviceArea.setText(advice.toString());

        resultPanel.setVisible(true);
        revalidate();
        repaint();
    }

    JLabel keywordTag(String text, Color fg, Color bg) {
        JLabel tag = new JLabel(text);
        tag.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tag.setForeground(fg);
        tag.setBackground(bg);
        tag.setOpaque(true);
        tag.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(fg, 1, true),
            new EmptyBorder(3, 10, 3, 10)
        ));
        return tag;
    }

    JLabel grayLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_GREY);
        return lbl;
    }

    void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ─── Simple JSON Helpers (no external lib) ────────────────
    double parseDouble(String json, String key) {
        try {
            String pattern = "\"" + key + "\":";
            int i = json.indexOf(pattern) + pattern.length();
            int j = json.indexOf(",", i);
            if (j == -1) j = json.indexOf("}", i);
            return Double.parseDouble(json.substring(i, j).trim());
        } catch (Exception e) { return 0; }
    }

    String parseStr(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"";
            int i = json.indexOf(pattern) + pattern.length();
            int j = json.indexOf("\"", i);
            return json.substring(i, j);
        } catch (Exception e) { return ""; }
    }

    String parseArray(String json, String key) {
        try {
            String pattern = "\"" + key + "\":[";
            int i = json.indexOf(pattern) + pattern.length();
            int j = json.indexOf("]", i);
            return json.substring(i, j).replaceAll("\"", "").trim();
        } catch (Exception e) { return ""; }
    }

    String[] splitKeywords(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new String[]{""};
        return raw.split(",");
    }

    // ─── Main ────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}

        SwingUtilities.invokeLater(ResumeAnalyzer::new);
    }
}
