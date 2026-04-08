import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * GameHeader – Panel header phong cách arcade/RPG.
 * Bao gồm:
 * - Tiêu đề neon-glow
 * - HUD Score với hiệu ứng pulse khi điểm thay đổi
 * - HUD Moves hiển thị số bước đi còn lại
 * - Countdown timer
 * - Background gradient tối + shimmer animation
 */
public class GameHeader extends JPanel {

    // ── Màu sắc chủ đạo ──
    private static final Color BG_TOP = new Color(10, 5, 30);
    private static final Color BG_BOT = new Color(25, 10, 60);
    private static final Color ACCENT = new Color(160, 80, 255);
    private static final Color ACCENT_GLOW = new Color(200, 120, 255, 80);
    private static final Color GOLD = new Color(255, 215, 50);
    private static final Color GOLD_DIM = new Color(180, 140, 30);
    private static final Color PANEL_BG = new Color(0, 0, 0, 150);
    private static final Color PANEL_BORDER = new Color(180, 80, 255, 180);
    private static final Color TIMER_OK = new Color(80, 220, 120);
    private static final Color TIMER_WARN = new Color(255, 200, 50);
    private static final Color TIMER_CRIT = new Color(255, 60, 60);
    private static final Color MOVES_COLOR = new Color(100, 200, 255);

    // ── Fonts ──
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 22); // Thu nhỏ 1 chút để nhường chỗ cho HUD
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 11);
    private static final Font VALUE_FONT = new Font("Arial", Font.BOLD, 24);

    // ── Dữ liệu HUD ──
    private int score = 0;
    private int highScore = 0;
    private int timeLeft = 120; // giây
    private int maxTime  = 120; // để vẽ progress bar
    private int movesLeft = 0;  // Số bước đi còn lại
    private boolean timerRunning = false;

    private int targetScore = 0;
    private int currentLevel = 1;

    // ── Hiệu ứng ──
    private float shimmerX = 0f;
    private float pulseFactor = 1f; // scale pulse khi score tăng
    private boolean pulsingScore = false;

    // ── Timers ──
    private final Timer animTimer;
    private Timer countdownTimer;

    // ── Listener ──
    public interface TimerExpiredListener {
        void onTimerExpired();
    }

    private TimerExpiredListener timerExpiredListener;

    // ── Particle stars: lưu vị trí ngẫu nhiên tĩnh ──
    private final float[][] stars;

    public GameHeader() {
        setPreferredSize(new Dimension(0, 100));
        setOpaque(false);

        // Tạo 30 ngôi sao ngẫu nhiên (tọa độ tỉ lệ 0-1)
        stars = new float[30][2];
        for (int i = 0; i < 30; i++) {
            stars[i][0] = (float) Math.random();
            stars[i][1] = (float) Math.random();
        }

        // Animation loop ~60fps: shimmer + repaint
        animTimer = new Timer(16, e -> {
            shimmerX += 1.5f;
            if (shimmerX > getWidth() + 200)
                shimmerX = -200;
            if (pulsingScore) {
                pulseFactor -= 0.04f;
                if (pulseFactor <= 1f) {
                    pulseFactor = 1f;
                    pulsingScore = false;
                }
            }
            repaint();
        });
        animTimer.start();
    }

    // ─────────────────────────────────────────────
    // API công khai
    // ─────────────────────────────────────────────
    public void setScore(int newScore) {
        if (newScore > score) {
            pulseFactor = 1.5f;
            pulsingScore = true;
        }
        score = newScore;
        if (score > highScore)
            highScore = score;
        repaint();
    }

    public void startTimer() {
        if (timerRunning)
            return;
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

    public void setTimerExpiredListener(TimerExpiredListener l) {
        this.timerExpiredListener = l;
    }

    /** Reset bảng cho màn mới: reset timer + điểm + moves */
    public void initLevel(LevelConfig config) {
        setScore(0);
        this.targetScore = config.targetScore;
        this.currentLevel = config.levelNumber;
        resetTimer(config.timerSeconds);
        setMovesLeft(config.maxMoves);
    }

    /** Dừng timer (không reset thời gian) */
    public void stopTimer() {
        if (countdownTimer != null) countdownTimer.stop();
        timerRunning = false;
    }

    /** Cập nhật số bước đi còn lại */
    public void setMovesLeft(int moves) {
        this.movesLeft = moves;
        repaint();
    }

    // ─────────────────────────────────────────────
    // Vẽ toàn bộ header
    // ─────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // 1. Background gradient
        drawBackground(g2, w, h);

        // 2. Shimmer sweep
        drawShimmer(g2, w, h);

        // 3. Stars
        drawStars(g2, w, h);

        // 4. Bottom divider glow
        drawDivider(g2, w, h);

        // 5. Tiêu đề game (Đẩy lên cao)
        drawTitle(g2, w, h);

        // 6. HUD Score (Bên trái)
        drawScoreHUD(g2, w, h);

        // 7. HUD Moves (Ở giữa)
        drawMovesHUD(g2, w, h);

        // 8. Timer HUD (Bên phải)
        drawTimerHUD(g2, w, h);

        g2.dispose();
    }

    private void drawBackground(Graphics2D g2, int w, int h) {
        GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOT);
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);
    }

    private void drawShimmer(Graphics2D g2, int w, int h) {
        float sx = shimmerX;
        GradientPaint shimmer = new GradientPaint(
                sx - 100, 0, new Color(255, 255, 255, 0),
                sx, 0, new Color(255, 255, 255, 18),
                true);
        g2.setPaint(shimmer);
        g2.fillRect(0, 0, w, h);
    }

    private void drawStars(Graphics2D g2, int w, int h) {
        long t = System.currentTimeMillis();
        for (float[] star : stars) {
            float x = star[0] * w;
            float y = star[1] * h;
            double phase = (t / 800.0 + star[0] * 10);
            int alpha = (int) (120 + 130 * Math.abs(Math.sin(phase)));
            g2.setColor(new Color(255, 255, 255, alpha));
            float r = 1.2f + (float) (Math.sin(phase) * 0.5);
            g2.fill(new Ellipse2D.Float(x - r, y - r, r * 2, r * 2));
        }
    }

    private void drawDivider(Graphics2D g2, int w, int h) {
        GradientPaint div = new GradientPaint(
                0, h - 1, new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 0),
                w / 2, h - 1, ACCENT,
                true);
        g2.setStroke(new BasicStroke(2f));
        g2.setPaint(div);
        g2.drawLine(0, h - 1, w, h - 1);

        g2.setStroke(new BasicStroke(4f));
        g2.setColor(ACCENT_GLOW);
        g2.drawLine(0, h - 2, w, h - 2);
    }

    private void drawTitle(Graphics2D g2, int w, int h) {
        String title = "PUZZLE INSIGHT";
        String lvText = "  Lv." + currentLevel; // Thêm khoảng trắng để cách tiêu đề ra một chút

        // 1. Tính toán chiều rộng của cả 2 chuỗi để căn giữa toàn bộ
        g2.setFont(TITLE_FONT);
        FontMetrics fm1 = g2.getFontMetrics();
        int w1 = fm1.stringWidth(title);

        g2.setFont(new Font("Arial", Font.BOLD, 20)); // Font cho chữ Level
        FontMetrics fm2 = g2.getFontMetrics();
        int w2 = fm2.stringWidth(lvText);

        int totalW = w1 + w2;
        int tx = (w - totalW) / 2; // Căn giữa cả cụm (Title + Level)
        int ty = 26; // Cố định ở mép trên

        // 2. Vẽ chữ PUZZLE INSIGHT với hiệu ứng bóng
        g2.setFont(TITLE_FONT);
        for (int r = 8; r >= 1; r--) {
            int a = (int) (40.0 * (1.0 - r / 9.0));
            g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), a));
            g2.drawString(title, tx, ty + r);
            g2.drawString(title, tx, ty - r);
            g2.drawString(title, tx + r, ty);
            g2.drawString(title, tx - r, ty);
        }

        GradientPaint textGrad = new GradientPaint(
                tx, ty - fm1.getAscent(), new Color(255, 230, 255),
                tx, ty, ACCENT);
        g2.setPaint(textGrad);
        g2.drawString(title, tx, ty);

        // 3. Vẽ chữ Lv. X bên cạnh (Màu Vàng)
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(GOLD);
        g2.drawString(lvText, tx + w1, ty); // Đẩy tọa độ X sang bên phải chữ Title
    }

    private void drawScoreHUD(Graphics2D g2, int w, int h) {
        int panelW = 125, panelH = 58;
        int px = 10, py = h - panelH - 8;

        drawHUDPanel(g2, px, py, panelW, panelH);

        g2.setFont(LABEL_FONT);
        g2.setColor(ACCENT);
        String lbl = "SCORE";
        FontMetrics lm = g2.getFontMetrics();
        g2.drawString(lbl, px + (panelW - lm.stringWidth(lbl)) / 2, py + 16);

        AffineTransform old = g2.getTransform();
        int cx = px + panelW / 2;
        int cy = py + panelH / 2 + 8;
        g2.translate(cx, cy);
        g2.scale(pulseFactor, pulseFactor);
        g2.translate(-cx, -cy);

        g2.setFont(VALUE_FONT);
        String sv = String.valueOf(score);
        FontMetrics vm = g2.getFontMetrics();
        for (int r = 4; r >= 1; r--) {
            g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 30 * r));
            g2.drawString(sv, cx - vm.stringWidth(sv) / 2 + r, cy + r);
        }
        g2.setColor(GOLD);
        g2.drawString(sv, cx - vm.stringWidth(sv) / 2, cy);
        g2.setTransform(old);

        // ĐỔI TỪ BEST SANG TARGET (Ở cuối hàm drawScoreHUD)
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(150, 255, 150)); // Đổi sang màu xanh nhạt cho dễ nhìn
        String targetStr = "TARGET: " + targetScore;
        FontMetrics bm = g2.getFontMetrics();
        g2.drawString(targetStr, px + (panelW - bm.stringWidth(targetStr)) / 2, py + panelH - 4);
    }

    private void drawMovesHUD(Graphics2D g2, int w, int h) {
        int panelW = 100, panelH = 58;
        int px = (w - panelW) / 2; // Canh giữa
        int py = h - panelH - 8;

        drawHUDPanel(g2, px, py, panelW, panelH);

        g2.setFont(LABEL_FONT);
        g2.setColor(ACCENT);
        String lbl = "MOVES";
        FontMetrics lm = g2.getFontMetrics();
        g2.drawString(lbl, px + (panelW - lm.stringWidth(lbl)) / 2, py + 16);

        int cx = px + panelW / 2;
        int cy = py + panelH / 2 + 12;

        g2.setFont(VALUE_FONT);
        String mv = String.valueOf(movesLeft);
        FontMetrics vm = g2.getFontMetrics();

        // Đổi màu đỏ nếu số bước dưới 5
        Color drawColor = (movesLeft <= 5) ? TIMER_CRIT : MOVES_COLOR;

        for (int r = 4; r >= 1; r--) {
            g2.setColor(new Color(drawColor.getRed(), drawColor.getGreen(), drawColor.getBlue(), 30 * r));
            g2.drawString(mv, cx - vm.stringWidth(mv) / 2 + r, cy + r);
        }
        g2.setColor(drawColor);
        g2.drawString(mv, cx - vm.stringWidth(mv) / 2, cy);
    }

    private void drawTimerHUD(Graphics2D g2, int w, int h) {
        int panelW = 125, panelH = 58;
        int px = w - panelW - 10;
        int py = h - panelH - 8;

        drawHUDPanel(g2, px, py, panelW, panelH);

        g2.setFont(LABEL_FONT);
        g2.setColor(ACCENT);
        String lbl = "TIME";
        FontMetrics lm = g2.getFontMetrics();
        g2.drawString(lbl, px + (panelW - lm.stringWidth(lbl)) / 2, py + 16);

        Color tColor;
        if (timeLeft > 60)
            tColor = TIMER_OK;
        else if (timeLeft > 20)
            tColor = TIMER_WARN;
        else
            tColor = TIMER_CRIT;

        Color tDraw = tColor;
        if (timeLeft <= 10 && timerRunning) {
            long ms = System.currentTimeMillis();
            if ((ms / 300) % 2 == 0)
                tDraw = new Color(tColor.getRed(), tColor.getGreen(), tColor.getBlue(), 80);
        }

        int cx = px + panelW / 2;
        int cy = py + panelH / 2 + 8;

        g2.setFont(VALUE_FONT);
        int mins = timeLeft / 60, secs = timeLeft % 60;
        String tv = String.format("%d:%02d", mins, secs);
        FontMetrics vm = g2.getFontMetrics();

        for (int r = 4; r >= 1; r--) {
            g2.setColor(new Color(tColor.getRed(), tColor.getGreen(), tColor.getBlue(), 25 * r));
            g2.drawString(tv, cx - vm.stringWidth(tv) / 2 + r, cy + r);
        }
        g2.setColor(tDraw);
        g2.drawString(tv, cx - vm.stringWidth(tv) / 2, cy);

        int barX = px + 10, barY = py + panelH - 8;
        int barW = panelW - 20, barH = 4;
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRoundRect(barX, barY, barW, barH, barH, barH);
        int filled = (maxTime > 0) ? (int)(barW * (double)timeLeft / maxTime) : 0;
        GradientPaint barGrad = new GradientPaint(barX, 0, tColor, barX + filled, 0,
                new Color(tColor.getRed(), tColor.getGreen(), tColor.getBlue(), 160));
        g2.setPaint(barGrad);
        g2.fillRoundRect(barX, barY, filled, barH, barH, barH);
    }

    private void drawHUDPanel(Graphics2D g2, int x, int y, int w, int h) {
        int arc = 14;
        g2.setColor(PANEL_BG);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(PANEL_BORDER);
        g2.drawRoundRect(x, y, w, h, arc, arc);
        GradientPaint inner = new GradientPaint(
                x, y, new Color(255, 255, 255, 40),
                x, y + h / 2, new Color(255, 255, 255, 0));
        g2.setPaint(inner);
        g2.fillRoundRect(x + 1, y + 1, w - 2, h / 2, arc, arc);
    }
}