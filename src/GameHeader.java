import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * GameHeader – Chuyển đổi sang phong cách Cute/Pastel/Candy.
 * Phù hợp với giao diện tươi sáng của bàn cờ.
 */
public class GameHeader extends JPanel {

    // ── Màu sắc chủ đạo (Phong cách kẹo ngọt) ──
    private static final Color BG_TOP = new Color(255, 235, 245);
    private static final Color BG_BOT = new Color(230, 240, 255);
    private static final Color PANEL_BG = new Color(255, 255, 255, 220);
    private static final Color TEXT_DARK = new Color(100, 80, 90);
    private static final Color TITLE_COLOR = new Color(255, 100, 160);
    private static final Color SCORE_COLOR = new Color(255, 150, 50);
    private static final Color MOVES_COLOR = new Color(50, 180, 255);
    private static final Color TIME_COLOR  = new Color(80, 210, 100);
    private static final Color TIME_CRIT   = new Color(255, 80, 80);

    // ── Fonts ──
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 13);
    private static final Font VALUE_FONT = new Font("Arial", Font.BOLD, 26);

    // ── Dữ liệu HUD ──
    private int score = 0;
    private int targetScore = 0;
    private int currentLevel = 1;
    private int timeLeft = 120;
    private int maxTime  = 120;
    private int movesLeft = 0;
    private boolean timerRunning = false;

    // ── Hiệu ứng ──
    private float pulseFactor = 1f;
    private boolean pulsingScore = false;
    private float floatAnimY = 0f;
    private final Timer animTimer;
    private Timer countdownTimer;

    // ── Listeners ──
    public interface TimerExpiredListener { void onTimerExpired(); }
    private TimerExpiredListener timerExpiredListener;

    public interface PauseListener { void onPauseClicked(); }
    private PauseListener pauseListener;

    public interface HelpListener { void onHelpClicked(); }
    private HelpListener helpListener;

    public void setTimerExpiredListener(TimerExpiredListener l) { this.timerExpiredListener = l; }
    public void setPauseListener(PauseListener l) { this.pauseListener = l; }
    public void setHelpListener(HelpListener l) { this.helpListener = l; }

    public GameHeader() {
        setPreferredSize(new Dimension(0, 130)); 
        setOpaque(false);
        setLayout(null);
        
        // ── NÚT HELP (Cạnh nút Pause) ──
        ImageIcon iconHelp = new ImageIcon("images/Help (3).png");
        Image scaledHelp = iconHelp.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
        JButton btnHelp = new JButton(new ImageIcon(scaledHelp));
        btnHelp.setContentAreaFilled(false);
        btnHelp.setBorderPainted(false);
        btnHelp.setFocusPainted(false);
        btnHelp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        
        btnHelp.addActionListener(e -> {
            if (helpListener != null) helpListener.onHelpClicked();
        });
        add(btnHelp);

        // ── NÚT PAUSE (Góc phải) ──
        ImageIcon iconPause = new ImageIcon("images/Pause (3).png");
        Image scaledPause = iconPause.getImage().getScaledInstance(45, 45, Image.SCALE_SMOOTH);
        JButton btnPause = new JButton(new ImageIcon(scaledPause));
        btnPause.setContentAreaFilled(false);
        btnPause.setBorderPainted(false);
        btnPause.setFocusPainted(false);
        btnPause.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        
        btnPause.addActionListener(e -> {
            if (pauseListener != null) pauseListener.onPauseClicked();
        });
        add(btnPause);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int w = getWidth(); // Lấy chiều rộng màn hình hiện tại
                btnPause.setBounds(w - 70, 15, 45, 45); // Neo nút Pause cách lề phải 70px
                btnHelp.setBounds(w - 125, 15, 45, 45); // Neo nút Help cạnh nút Pause
            }
        });

        // Animation nhấp nhô và nhịp tim
        animTimer = new Timer(16, e -> {
            long t = System.currentTimeMillis();
            floatAnimY = (float) Math.sin(t / 300.0) * 3f;

            if (pulsingScore) {
                pulseFactor -= 0.05f;
                if (pulseFactor <= 1f) {
                    pulseFactor = 1f;
                    pulsingScore = false;
                }
            }
            repaint();
        });
        animTimer.start();
    }

    public void setScore(int newScore) {
        if (newScore > score) {
            pulseFactor = 1.4f;
            pulsingScore = true;
        }
        score = newScore;
        repaint();
    }

    public void startTimer() {
        if (timerRunning) return;
        timerRunning = true;
        countdownTimer = new Timer(1000, e -> {
            timeLeft--;
            repaint();
            if (timeLeft <= 0) {
                timeLeft = 0;
                ((Timer) e.getSource()).stop();
                timerRunning = false;
                if (timerExpiredListener != null)
                    timerExpiredListener.onTimerExpired();
            }
        });
        countdownTimer.start();
    }

    public void resetTimer(int seconds) {
        if (countdownTimer != null) countdownTimer.stop();
        timerRunning = false;
        maxTime  = seconds;
        timeLeft = seconds;
        repaint();
    }

    public void initLevel(LevelConfig config) {
        setScore(0);
        this.targetScore = config.targetScore;
        this.currentLevel = config.levelNumber;
        resetTimer(config.timerSeconds);
        setMovesLeft(config.maxMoves);
    }

    public void stopTimer() {
        if (countdownTimer != null) countdownTimer.stop();
        timerRunning = false;
    }

    public void setMovesLeft(int moves) {
        this.movesLeft = moves;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOT);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        drawClouds(g2, w, h);
        drawTitle(g2, w, h);

        int panelW = 125, panelH = 55;
        int gap = (w - (panelW * 3)) / 4; 
        int py = 65; 

        drawScoreHUD(g2, gap, py, panelW, panelH);
        drawMovesHUD(g2, gap * 2 + panelW, py, panelW, panelH);
        drawTimerHUD(g2, gap * 3 + panelW * 2, py, panelW, panelH);

        g2.dispose();
    }

    private void drawClouds(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillOval(-20, -10, 80, 50);
        g2.fillOval(40, -20, 100, 60);
        g2.fillOval(w - 90, -15, 80, 50);
        g2.fillOval(w - 30, 10, 60, 40);
    }

    private void drawTitle(Graphics2D g2, int w, int h) {
        String lvText = "Level " + currentLevel; 

        g2.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        
        int tx = (w - fm.stringWidth(lvText)) / 2;
        int ty = 35 + (int) floatAnimY; 

        drawStickerText(g2, lvText, tx, ty, TITLE_COLOR); 
    }

    private void drawScoreHUD(Graphics2D g2, int x, int y, int w, int h) {
        drawCutePanel(g2, x, y, w, h);

        g2.setFont(LABEL_FONT);
        g2.setColor(TEXT_DARK);
        String lbl = "SCORE";
        int lx = x + (w - g2.getFontMetrics().stringWidth(lbl)) / 2;
        g2.drawString(lbl, lx, y + 16);

        AffineTransform old = g2.getTransform();
        int cx = x + w / 2;
        int cy = y + h / 2 + 5;
        g2.translate(cx, cy);
        g2.scale(pulseFactor, pulseFactor);
        g2.translate(-cx, -cy);

        g2.setFont(VALUE_FONT);
        drawStickerText(g2, String.valueOf(score), cx - g2.getFontMetrics().stringWidth(String.valueOf(score)) / 2, cy + 8, SCORE_COLOR);
        g2.setTransform(old);

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(TEXT_DARK);
        String targetStr = "TARGET: " + targetScore;
        g2.drawString(targetStr, x + (w - g2.getFontMetrics().stringWidth(targetStr)) / 2, y + h - 4);
    }

    private void drawMovesHUD(Graphics2D g2, int x, int y, int w, int h) {
        drawCutePanel(g2, x, y, w, h);

        g2.setFont(LABEL_FONT);
        g2.setColor(TEXT_DARK);
        String lbl = "MOVES";
        g2.drawString(lbl, x + (w - g2.getFontMetrics().stringWidth(lbl)) / 2, y + 16);

        g2.setFont(VALUE_FONT);
        String mv = String.valueOf(movesLeft);
        Color drawColor = (movesLeft <= 5) ? TIME_CRIT : MOVES_COLOR;
        drawStickerText(g2, mv, x + (w - g2.getFontMetrics().stringWidth(mv)) / 2, y + h / 2 + 13, drawColor);
    }

    private void drawTimerHUD(Graphics2D g2, int x, int y, int w, int h) {
        drawCutePanel(g2, x, y, w, h);

        g2.setFont(LABEL_FONT);
        g2.setColor(TEXT_DARK);
        String lbl = "TIME";
        g2.drawString(lbl, x + (w - g2.getFontMetrics().stringWidth(lbl)) / 2, y + 16);

        Color tColor = (timeLeft > 20) ? TIME_COLOR : TIME_CRIT;
        
        AffineTransform old = g2.getTransform();
        int cx = x + w / 2;
        int cy = y + h / 2 + 5;
        if (timeLeft <= 10 && timerRunning) {
            float scale = 1f + (float) Math.abs(Math.sin(System.currentTimeMillis() / 150.0)) * 0.15f;
            g2.translate(cx, cy);
            g2.scale(scale, scale);
            g2.translate(-cx, -cy);
        }

        g2.setFont(VALUE_FONT);
        int mins = timeLeft / 60, secs = timeLeft % 60;
        String tv = String.format("%d:%02d", mins, secs);
        drawStickerText(g2, tv, cx - g2.getFontMetrics().stringWidth(tv) / 2, cy + 8, tColor);
        g2.setTransform(old);

        int barX = x + 12, barY = y + h - 10;
        int barW = w - 24, barH = 6;
        g2.setColor(new Color(200, 210, 220)); 
        g2.fillRoundRect(barX, barY, barW, barH, barH, barH);
        int filled = (maxTime > 0) ? (int)(barW * (double)timeLeft / maxTime) : 0;
        g2.setColor(tColor);
        g2.fillRoundRect(barX, barY, filled, barH, barH, barH);
    }

    private void drawCutePanel(Graphics2D g2, int x, int y, int w, int h) {
        int arc = 25;
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(x + 2, y + 3, w, h, arc, arc);
        
        g2.setColor(PANEL_BG);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        
        g2.setStroke(new BasicStroke(3.5f));
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, w, h, arc, arc);
    }

    private void drawStickerText(Graphics2D g2, String text, int x, int y, Color mainColor) {
    // Vẽ bóng đổ 1 lần duy nhất
    g2.setColor(new Color(0, 0, 0, 40));
    g2.drawString(text, x + 2, y + 2);
    
    // Vẽ chữ chính
    g2.setColor(mainColor);
    g2.drawString(text, x, y);
}
}