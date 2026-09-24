import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern, Interactive Number Guessing Game.
 * Fully compatible with Java 8+ Swing.
 * Preserves 100% of original business logic, rules, and scoring formulas.
 */
public class NumberGame extends JFrame implements ActionListener {

    private final Random random = new Random();

    int secretNumber;
    int attempts;
    final int MAX_ATTEMPTS = 10;

    int totalGames = 0;
    int gamesWon = 0;
    int totalScore = 0;

    // Preserved UI components for full backward compatibility
    JLabel titleLabel;
    JLabel instructionLabel;
    JLabel resultLabel;
    JLabel attemptsLabel;
    JLabel scoreLabel;
    JLabel statsLabel;

    ModernTextField guessField;

    JButton submitButton;
    JButton newGameButton;
    JButton exitButton;

    // Modern Interactive Enhancements
    private AttemptsProgressView attemptsProgress;
    private ToastOverlay toastOverlay;
    private CelebrationOverlay celebrationOverlay;

    // Design System Color Palette (Modern Indigo / Slate Theme)
    private static final Color BG_TOP = new Color(241, 245, 249);       // Slate 100
    private static final Color BG_BOTTOM = new Color(226, 232, 240);    // Slate 200
    private static final Color CARD_BG = new Color(255, 255, 255);
    private static final Color CARD_BORDER = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);     // Slate 900
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);// Slate 500
    private static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400
    private static final Color ACCENT_INDIGO = new Color(79, 70, 229);   // Indigo 600
    private static final Color ACCENT_INDIGO_HOVER = new Color(67, 56, 202);
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129); // Emerald 500
    private static final Color ACCENT_ROSE = new Color(239, 68, 68);     // Rose 500
    private static final Color ACCENT_AMBER = new Color(245, 158, 11);   // Amber 500
    private static final Color ACCENT_SKY = new Color(14, 165, 233);     // Sky 500

    public NumberGame() {
        setTitle("Number Guessing Game");
        setSize(500, 480);
        setMinimumSize(new Dimension(460, 440));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);

        // Native Look & Feel & Anti-aliasing setup
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        setupGlobalRenderingHints();

        // Window Closing listener with confirmation
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });

        // Layered pane setup to support celebratory confetti and toast notifications
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));

        // Background Gradient Panel
        BackgroundPanel mainBackground = new BackgroundPanel();
        mainBackground.setLayout(new BorderLayout(0, 0));

        // Scrollable content wrapper for flawless responsiveness on all screen resolutions
        JPanel contentContainer = new JPanel();
        contentContainer.setOpaque(false);
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setBorder(new EmptyBorder(24, 28, 24, 28));

        // 1. HEADER SECTION
        JPanel headerPanel = buildHeaderSection();
        contentContainer.add(headerPanel);
        contentContainer.add(Box.createVerticalStrut(16));

        // 2. MAIN GAME INTERACTION CARD
        JPanel gameCard = buildGameInteractionCard();
        contentContainer.add(gameCard);
        contentContainer.add(Box.createVerticalStrut(16));

        // 3. FOOTER ACTIONS (New Game, Exit)
        JPanel footerPanel = buildFooterSection();
        contentContainer.add(footerPanel);

        // Put content into scroll pane with invisible/smooth borders
        JScrollPane scrollPane = new JScrollPane(contentContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        mainBackground.add(scrollPane, BorderLayout.CENTER);

        // Overlays
        toastOverlay = new ToastOverlay();
        celebrationOverlay = new CelebrationOverlay();

        layeredPane.add(celebrationOverlay, Integer.valueOf(300));
        layeredPane.add(toastOverlay, Integer.valueOf(200));
        layeredPane.add(mainBackground, Integer.valueOf(100));

        setContentPane(layeredPane);

        // Keyboard navigation and shortcuts
        setupKeyBindings();

        // Initialize statsLabel for backwards compatibility
        statsLabel = new JLabel("<html>Total Games : 0<br>Games Won : 0<br>Total Score : 0</html>");

        startNewGame();
        setVisible(true);
    }

    private void setupGlobalRenderingHints() {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    private void setupKeyBindings() {
        getRootPane().setDefaultButton(submitButton);

        // Escape key triggers exit confirmation
        getRootPane().registerKeyboardAction(
            e -> confirmExit(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Ctrl + N or F2 starts a new game
        getRootPane().registerKeyboardAction(
            e -> startNewGame(),
            KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
            e -> startNewGame(),
            KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JPanel buildHeaderSection() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Main Title Label
        titleLabel = new JLabel("Number Guessing Game");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(4));

        // Instruction Label
        instructionLabel = new JLabel("Guess a number between 1 and 100");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        instructionLabel.setForeground(TEXT_SECONDARY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(instructionLabel);

        return panel;
    }

    private JPanel buildGameInteractionCard() {
        RoundedCard card = new RoundedCard(16, CARD_BG, CARD_BORDER);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Progress section (Attempts Remaining)
        JPanel progressHeader = new JPanel(new BorderLayout());
        progressHeader.setOpaque(false);

        attemptsLabel = new JLabel("Attempts Left : 10");
        attemptsLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        attemptsLabel.setForeground(TEXT_PRIMARY);

        scoreLabel = new JLabel("Score : 0");
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        scoreLabel.setForeground(ACCENT_INDIGO);

        progressHeader.add(attemptsLabel, BorderLayout.WEST);
        progressHeader.add(scoreLabel, BorderLayout.EAST);
        card.add(progressHeader);

        card.add(Box.createVerticalStrut(8));

        // Visual Animated Progress Bar
        attemptsProgress = new AttemptsProgressView(MAX_ATTEMPTS);
        attemptsProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(attemptsProgress);

        card.add(Box.createVerticalStrut(20));

        // Input Box Section
        JLabel prompt = new JLabel("Enter your guess:");
        prompt.setFont(new Font("Segoe UI", Font.BOLD, 12));
        prompt.setForeground(TEXT_SECONDARY);
        prompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(prompt);

        card.add(Box.createVerticalStrut(8));

        guessField = new ModernTextField("Type 1 - 100 and press Enter");
        guessField.setMaximumSize(new Dimension(320, 48));
        guessField.setPreferredSize(new Dimension(280, 48));
        guessField.setHorizontalAlignment(JTextField.CENTER);
        guessField.setFont(new Font("Segoe UI", Font.BOLD, 22));
        guessField.setForeground(TEXT_PRIMARY);
        guessField.addActionListener(e -> checkGuess());
        card.add(guessField);

        card.add(Box.createVerticalStrut(18));

        // Submit Button (Primary Interactive Action)
        submitButton = new ModernButton("Submit Guess", ACCENT_INDIGO, ACCENT_INDIGO_HOVER, Color.WHITE, true);
        submitButton.setMaximumSize(new Dimension(280, 46));
        submitButton.setPreferredSize(new Dimension(240, 46));
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.setToolTipText("Check your guess (Press Enter)");
        submitButton.addActionListener(this);
        card.add(submitButton);

        card.add(Box.createVerticalStrut(14));

        // Result Label
        resultLabel = new JLabel("Start Guessing...");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resultLabel.setForeground(TEXT_PRIMARY);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(resultLabel);

        return card;
    }

    private JPanel buildFooterSection() {
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttonBar.setOpaque(false);

        // New Game Button
        newGameButton = new ModernButton("New Game", new Color(241, 245, 249), new Color(226, 232, 240), TEXT_PRIMARY, false);
        newGameButton.setPreferredSize(new Dimension(140, 40));
        newGameButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        newGameButton.setToolTipText("Start a fresh round (Ctrl+N or F2)");
        newGameButton.addActionListener(this);

        // Exit Button
        exitButton = new ModernButton("Exit Game", new Color(254, 242, 242), new Color(254, 226, 226), ACCENT_ROSE, false);
        exitButton.setPreferredSize(new Dimension(140, 40));
        exitButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        exitButton.setToolTipText("Close application (Esc)");
        exitButton.addActionListener(this);

        buttonBar.add(newGameButton);
        buttonBar.add(exitButton);
        footer.add(buttonBar);

        return footer;
    }

    void startNewGame() {
        secretNumber = random.nextInt(100) + 1;
        attempts = 10;

        guessField.setText("");
        guessField.setEnabled(true);

        resultLabel.setText("Start Guessing...");
        resultLabel.setForeground(TEXT_PRIMARY);

        attemptsLabel.setText("Attempts Left : 10");
        attemptsProgress.updateAttempts(attempts, MAX_ATTEMPTS);

        scoreLabel.setText("Score : 0");
        submitButton.setEnabled(true);

        SwingUtilities.invokeLater(() -> guessField.requestFocusInWindow());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            checkGuess();
        } else if (e.getSource() == newGameButton) {
            startNewGame();
        } else if (e.getSource() == exitButton) {
            confirmExit();
        }
    }

    private void confirmExit() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Exit Number Guessing Game",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    void checkGuess() {
        String input = guessField.getText().trim();

        if (input.isEmpty()) {
            toastOverlay.showToast("Please enter a number.", ToastOverlay.Type.WARNING);
            guessField.triggerShake();
            guessField.requestFocus();
            return;
        }

        int guess;
        try {
            guess = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            toastOverlay.showToast("Invalid number! Enter whole digits only.", ToastOverlay.Type.ERROR);
            guessField.triggerShake();
            guessField.requestFocus();
            return;
        }

        if (guess < 1 || guess > 100) {
            toastOverlay.showToast("Enter a number between 1 and 100.", ToastOverlay.Type.WARNING);
            guessField.triggerShake();
            guessField.requestFocus();
            return;
        }

        attempts--;
        attemptsProgress.updateAttempts(attempts, MAX_ATTEMPTS);
        attemptsLabel.setText("Attempts Left : " + attempts);

        // Win Condition
        if (guess == secretNumber) {
            int score = (attempts + 1) * 10;
            totalGames++;
            gamesWon++;
            totalScore += score;

            resultLabel.setText("Congratulations! Correct Guess");
            resultLabel.setForeground(ACCENT_EMERALD);

            scoreLabel.setText("Score : " + score);
            guessField.setText("");
            guessField.setEnabled(false);
            submitButton.setEnabled(false);

            celebrationOverlay.triggerCelebration();
            updateStats();
            return;
        }

        // Hint Direction
        if (guess < secretNumber) {
            resultLabel.setText("Too Low!");
            resultLabel.setForeground(ACCENT_SKY);
        } else {
            resultLabel.setText("Too High!");
            resultLabel.setForeground(ACCENT_AMBER);
        }

        guessField.setText("");
        guessField.requestFocus();

        // Game Over Condition
        if (attempts == 0) {
            totalGames++;

            resultLabel.setText("Game Over! Number was " + secretNumber);
            resultLabel.setForeground(ACCENT_ROSE);

            guessField.setText("");
            guessField.setEnabled(false);
            submitButton.setEnabled(false);
            updateStats();
        }
    }

    void updateStats() {
        // Preserve original HTML label format for backwards compatibility
        statsLabel.setText("<html>"
            + "Total Games : " + totalGames + "<br>"
            + "Games Won : " + gamesWon + "<br>"
            + "Total Score : " + totalScore
            + "</html>");
    }

    // ==========================================
    // MODERN UI & INTERACTIVE CUSTOM COMPONENTS
    // ==========================================

    /**
     * Smooth Gradient Background
     */
    private static class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOTTOM);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    /**
     * Card Panel with Rounded Corners, Subtle Drop Border, and Anti-Aliasing
     */
    public static class RoundedCard extends JPanel {
        private final int radius;
        private final Color bg;
        private final Color border;

        public RoundedCard(int radius, Color bg, Color border) {
            this.radius = radius;
            this.bg = bg;
            this.border = border;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Subtle drop shadow effect
            g2.setColor(new Color(0, 0, 0, 8));
            g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, radius, radius);

            // Card body
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 2, radius, radius);

            // Border
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 2, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    /**
     * Rounded Stroke Border
     */
    private static class RoundedBorder extends EmptyBorder {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedBorder(Color color, int radius, int thickness) {
            super(thickness, thickness, thickness, thickness);
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }

    /**
     * Modern Animated Button with Hover & Press Feedback
     */
    public static class ModernButton extends JButton {
        private final Color normalBg;
        private final Color hoverBg;
        private final Color fg;
        private final boolean isPrimary;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public ModernButton(String text, Color normalBg, Color hoverBg, Color fg, boolean isPrimary) {
            super(text);
            this.normalBg = normalBg;
            this.hoverBg = hoverBg;
            this.fg = fg;
            this.isPrimary = isPrimary;

            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(fg);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int corner = 14;

            Color currentBg = normalBg;
            if (!isEnabled()) {
                currentBg = new Color(226, 232, 240);
                g2.setColor(new Color(148, 163, 184));
            } else if (isPressed) {
                currentBg = isPrimary ? hoverBg.darker() : normalBg.darker();
            } else if (isHovered) {
                currentBg = hoverBg;
            }

            // Draw button background
            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, width, height, corner, corner);

            // Subtle border for secondary buttons
            if (!isPrimary && isEnabled()) {
                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, width - 1, height - 1, corner, corner);
            }

            // Focus indicator ring if focused
            if (hasFocus() && isEnabled()) {
                g2.setColor(new Color(99, 102, 241, 160));
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawRoundRect(2, 2, width - 5, height - 5, corner - 2, corner - 2);
            }

            // Draw Text
            FontMetrics fm = g2.getFontMetrics(getFont());
            int textX = (width - fm.stringWidth(getText())) / 2;
            int textY = (height - fm.getHeight()) / 2 + fm.getAscent();

            g2.setFont(getFont());
            g2.setColor(isEnabled() ? fg : new Color(148, 163, 184));
            g2.drawString(getText(), textX, isPressed ? textY + 1 : textY);

            g2.dispose();
        }
    }

    /**
     * Modern Animated Text Field with Placeholder, Focus Glow, and Shake Animation
     */
    public static class ModernTextField extends JTextField {
        private final String placeholder;
        private boolean isFocused = false;
        private int shakeOffset = 0;
        private Timer shakeTimer;

        public ModernTextField(String placeholder) {
            this.placeholder = placeholder;
            setOpaque(false);
            setBorder(new EmptyBorder(10, 16, 10, 16));

            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    isFocused = true;
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    isFocused = false;
                    repaint();
                }
            });
        }

        public void triggerShake() {
            if (shakeTimer != null && shakeTimer.isRunning()) {
                shakeTimer.stop();
            }
            final int[] frames = { -8, 8, -6, 6, -4, 4, -2, 2, 0 };
            final int[] step = { 0 };

            shakeTimer = new Timer(25, e -> {
                if (step[0] < frames.length) {
                    shakeOffset = frames[step[0]];
                    step[0]++;
                    repaint();
                } else {
                    shakeOffset = 0;
                    repaint();
                    ((Timer) e.getSource()).stop();
                }
            });
            shakeTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int corner = 12;

            // Background
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(shakeOffset, 0, width - 1, height - 1, corner, corner);

            // Focus glow or normal border
            if (isFocused) {
                g2.setColor(new Color(79, 70, 229, 60));
                g2.setStroke(new BasicStroke(3.5f));
                g2.drawRoundRect(shakeOffset + 1, 1, width - 3, height - 3, corner, corner);

                g2.setColor(ACCENT_INDIGO);
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(shakeOffset + 1, 1, width - 3, height - 3, corner, corner);
            } else {
                g2.setColor(new Color(203, 213, 225));
                g2.setStroke(new BasicStroke(1.4f));
                g2.drawRoundRect(shakeOffset, 0, width - 1, height - 1, corner, corner);
            }

            g2.dispose();

            super.paintComponent(g);

            // Placeholder text when empty and unfocused
            if (getText().isEmpty() && !isFocused) {
                Graphics2D gPlaceholder = (Graphics2D) g.create();
                gPlaceholder.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                gPlaceholder.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                gPlaceholder.setColor(TEXT_MUTED);
                FontMetrics fm = gPlaceholder.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(placeholder)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                gPlaceholder.drawString(placeholder, x, y);
                gPlaceholder.dispose();
            }
        }
    }



    /**
     * Modern Animated Attempt Dots & Progress Bar
     */
    public static class AttemptsProgressView extends JPanel {
        private final int maxAttempts;
        private int currentAttempts;

        public AttemptsProgressView(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            this.currentAttempts = maxAttempts;
            setOpaque(false);
            setPreferredSize(new Dimension(320, 22));
            setMaximumSize(new Dimension(420, 22));
        }

        public void updateAttempts(int attempts, int max) {
            this.currentAttempts = attempts;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Background track
            g2.setColor(new Color(241, 245, 249));
            g2.fillRoundRect(0, 6, width, 10, 10, 10);

            // Dynamic color based on attempts left
            Color barColor;
            if (currentAttempts > 6) {
                barColor = ACCENT_EMERALD;
            } else if (currentAttempts > 3) {
                barColor = ACCENT_AMBER;
            } else {
                barColor = ACCENT_ROSE;
            }

            int filledWidth = (int) Math.round(((double) currentAttempts / maxAttempts) * width);
            if (filledWidth > 0) {
                g2.setColor(barColor);
                g2.fillRoundRect(0, 6, filledWidth, 10, 10, 10);
            }

            g2.dispose();
        }
    }




    /**
     * Floating Animated Toast Notification Overlay
     */
    public static class ToastOverlay extends JPanel {
        public enum Type { INFO, WARNING, ERROR, SUCCESS }

        private String message = "";
        private Type type = Type.INFO;
        private boolean isVisible = false;
        private float alpha = 0.0f;
        private Timer fadeTimer;

        public ToastOverlay() {
            setOpaque(false);
            setLayout(null);
        }

        public void showToast(String msg, Type toastType) {
            this.message = msg;
            this.type = toastType;
            this.isVisible = true;
            this.alpha = 1.0f;
            repaint();

            if (fadeTimer != null && fadeTimer.isRunning()) {
                fadeTimer.stop();
            }

            fadeTimer = new Timer(2200, e -> {
                Timer dismissTimer = new Timer(30, ev -> {
                    alpha -= 0.1f;
                    if (alpha <= 0.0f) {
                        alpha = 0.0f;
                        isVisible = false;
                        ((Timer) ev.getSource()).stop();
                    }
                    repaint();
                });
                dismissTimer.start();
                ((Timer) e.getSource()).stop();
            });
            fadeTimer.setRepeats(false);
            fadeTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!isVisible || alpha <= 0.01f) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

            Font font = new Font("Segoe UI", Font.BOLD, 13);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();

            int textWidth = fm.stringWidth(message);
            int boxWidth = Math.max(260, textWidth + 40);
            int boxHeight = 40;
            int x = (getWidth() - boxWidth) / 2;
            int y = 20;

            Color bg;
            Color fg;
            switch (type) {
                case ERROR:
                    bg = new Color(220, 38, 38);
                    fg = Color.WHITE;
                    break;
                case WARNING:
                    bg = new Color(217, 119, 6);
                    fg = Color.WHITE;
                    break;
                case SUCCESS:
                    bg = new Color(16, 185, 129);
                    fg = Color.WHITE;
                    break;
                default:
                    bg = new Color(30, 41, 59);
                    fg = Color.WHITE;
                    break;
            }

            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(x + 2, y + 3, boxWidth, boxHeight, 14, 14);

            // Toast body
            g2.setColor(bg);
            g2.fillRoundRect(x, y, boxWidth, boxHeight, 14, 14);

            // Message text
            g2.setColor(fg);
            int textX = x + (boxWidth - textWidth) / 2;
            int textY = y + (boxHeight - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(message, textX, textY);

            g2.dispose();
        }
    }

    /**
     * Celebratory Confetti & Particle Overlay on Win
     */
    public static class CelebrationOverlay extends JPanel {
        private static class Particle {
            double x, y;
            double vx, vy;
            Color color;
            int size;
            int rotation;

            Particle(double x, double y, Color color) {
                this.x = x;
                this.y = y;
                this.color = color;
                Random r = new Random();
                this.vx = (r.nextDouble() - 0.5) * 12;
                this.vy = -r.nextDouble() * 12 - 4;
                this.size = r.nextInt(6) + 6;
                this.rotation = r.nextInt(360);
            }

            void update() {
                x += vx;
                y += vy;
                vy += 0.45; // gravity
                rotation += 8;
            }
        }

        private final List<Particle> particles = new ArrayList<>();
        private Timer animationTimer;
        private boolean active = false;

        public CelebrationOverlay() {
            setOpaque(false);
            setLayout(null);
        }

        public void triggerCelebration() {
            particles.clear();
            active = true;

            Color[] colors = {
                new Color(239, 68, 68), new Color(245, 158, 11),
                new Color(16, 185, 129), new Color(14, 165, 233),
                new Color(139, 92, 246), new Color(236, 72, 153)
            };

            int startX = getWidth() > 0 ? getWidth() / 2 : 280;
            int startY = getHeight() > 0 ? getHeight() / 3 : 200;

            Random rnd = new Random();
            for (int i = 0; i < 70; i++) {
                particles.add(new Particle(startX + (rnd.nextInt(60) - 30), startY, colors[rnd.nextInt(colors.length)]));
            }

            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }

            final int[] ticks = { 0 };
            animationTimer = new Timer(25, e -> {
                ticks[0]++;
                for (Particle p : particles) {
                    p.update();
                }
                repaint();

                if (ticks[0] > 70) {
                    active = false;
                    particles.clear();
                    repaint();
                    ((Timer) e.getSource()).stop();
                }
            });
            animationTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!active || particles.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Particle p : particles) {
                g2.setColor(p.color);
                Graphics2D pG = (Graphics2D) g2.create();
                pG.translate(p.x, p.y);
                pG.rotate(Math.toRadians(p.rotation));
                pG.fillRoundRect(-p.size / 2, -p.size / 2, p.size, p.size / 2, 2, 2);
                pG.dispose();
            }

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new NumberGame());
    }
}