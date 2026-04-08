import java.awt.*;
import javax.swing.*;

/**
 * Overlay hiển thị kết quả màn: LEVEL CLEAR / OUT OF MOVES / YOU WIN.
 */
public class LevelOverlay extends JPanel {

    public enum Mode { WIN, LOSE, FINAL_WIN }
    public interface OverlayAction { void onAction(); }

    private Mode mode = Mode.WIN;
    private int score, target;
    private OverlayAction action;
    private final JButton actionBtn;
    private final Timer animTimer;
    private float glowPhase = 0f;

    // Confetti particles [x, y, vx, vy] * N
    private static final int NUM_PARTICLES = 36;
    private final float[] px = new float[NUM_PARTICLES];
    private final float[] py = new float[NUM_PARTICLES];
    private final float[] pvx = new float[NUM_PARTICLES];
    private final float[] pvy = new float[NUM_PARTICLES];
    private final Color[] confettiColors = {
        new Color(255,215,50), new Color(100,220,130),
        new Color(150,100,255), new Color(255,100,150),
        new Color(100,200,255), new Color(255,160,50)
    };

    public LevelOverlay() {
        setOpaque(false);
        setLayout(null);

        actionBtn = new JButton();
        actionBtn.setFont(new Font("Arial", Font.BOLD, 16));
        actionBtn.setFocusPainted(false);
        actionBtn.setBorderPainted(false);
        actionBtn.setOpaque(true);
        actionBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        actionBtn.addActionListener(e -> { if (action != null) action.onAction(); });
        add(actionBtn);

        animTimer = new Timer(16, e -> {
            glowPhase += 0.055f;
            updateParticles();
            repaint();
        });

        setVisible(false);
    }

    @Override
    public void doLayout() {
        int w = getWidth(), h = getHeight();
        actionBtn.setBounds((w - 200) / 2, h * 58 / 100, 200, 46);
    }

    // ── Public API ──────────────────────────────────────────────────────────

    public void showWin(int score, int target, OverlayAction action) {
        this.mode = Mode.WIN; this.score = score; this.target = target; this.action = action;
        actionBtn.setText("NEXT LEVEL  ▶");
        actionBtn.setBackground(new Color(60, 190, 100));
        actionBtn.setForeground(Color.WHITE);
        spawnParticles();
        setVisible(true); animTimer.start();
    }

    public void showFinalWin(int score, OverlayAction action) {
        this.mode = Mode.FINAL_WIN; this.score = score; this.action = action;
        actionBtn.setText("PLAY AGAIN  ↺");
        actionBtn.setBackground(new Color(220, 165, 30));
        actionBtn.setForeground(new Color(30, 10, 0));
        spawnParticles();
        setVisible(true); animTimer.start();
    }

    public void showLose(int score, int target, OverlayAction action) {
        this.mode = Mode.LOSE; this.score = score; this.target = target; this.action = action;
        actionBtn.setText("TRY AGAIN  ↺");
        actionBtn.setBackground(new Color(200, 50, 50));
        actionBtn.setForeground(Color.WHITE);
        setVisible(true); animTimer.start();
    }

    public void hideOverlay() { animTimer.stop(); setVisible(false); }

    // ── Particles ────────────────────────────────────────────────────────────

    private void spawnParticles() {
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < NUM_PARTICLES; i++) {
            px[i]  = rng.nextFloat();
            py[i]  = -rng.nextFloat() * 0.3f;
            pvx[i] = (rng.nextFloat() - 0.5f) * 0.004f;
            pvy[i] = rng.nextFloat() * 0.004f + 0.002f;
        }
    }

    private void updateParticles() {
        for (int i = 0; i < NUM_PARTICLES; i++) {
            px[i] += pvx[i]; py[i] += pvy[i];
            if (py[i] > 1.1f) { py[i] = -0.05f; px[i] = (float)Math.random(); }
        }
    }

    // ── Painting ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // Outer dim
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, w, h);

        // Dialog box
        Color boxBg   = (mode == Mode.LOSE) ? new Color(70, 5, 5, 220)   : new Color(10, 5, 40, 220);
        Color boxBord = (mode == Mode.LOSE) ? new Color(255, 80, 80)
                      : (mode == Mode.FINAL_WIN) ? new Color(255, 215, 50)
                      : new Color(80, 210, 120);
        int bx = 30, by = 30, bw = w - 60, bh = h - 60;
        g2.setColor(boxBg);
        g2.fillRoundRect(bx, by, bw, bh, 20, 20);
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(boxBord);
        g2.drawRoundRect(bx, by, bw, bh, 20, 20);

        if (mode != Mode.LOSE) drawConfetti(g2, w, h);
        if (mode == Mode.LOSE) drawLoseContent(g2, w, h);
        else                   drawWinContent(g2, w, h);

        g2.dispose();
    }

    private void drawConfetti(Graphics2D g2, int w, int h) {
        for (int i = 0; i < NUM_PARTICLES; i++) {
            g2.setColor(confettiColors[i % confettiColors.length]);
            g2.fillRect((int)(px[i] * w), (int)(py[i] * h), 7, 7);
        }
    }

    private void drawWinContent(Graphics2D g2, int w, int h) {
        float glow = (float)(0.5 + 0.5 * Math.sin(glowPhase));

        // Stars
        g2.setFont(new Font("Arial", Font.BOLD, 32));
        String stars = "\u2605 \u2605 \u2605";
        FontMetrics fm = g2.getFontMetrics();
        int gold = (int)(180 + 75 * glow);
        g2.setColor(new Color(gold, (int)(160 + 55 * glow), 30));
        g2.drawString(stars, (w - fm.stringWidth(stars)) / 2, h / 2 - 60);

        // Title
        String title = (mode == Mode.FINAL_WIN) ? "YOU WIN!" : "LEVEL CLEAR!";
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2, ty = h / 2 - 15;
        int ga = (int)(80 + 100 * glow);
        g2.setColor(new Color(100, 255, 150, ga));
        for (int r = 4; r >= 1; r--) g2.drawString(title, tx + r, ty + r);
        g2.setColor(new Color(255, 240, 100));
        g2.drawString(title, tx, ty);

        // Score
        g2.setFont(new Font("Arial", Font.BOLD, 17));
        String sc = "SCORE: " + score + " pts";
        fm = g2.getFontMetrics();
        g2.setColor(new Color(190, 210, 255));
        g2.drawString(sc, (w - fm.stringWidth(sc)) / 2, h / 2 + 22);
    }

    private void drawLoseContent(Graphics2D g2, int w, int h) {
        float glow = (float)(0.5 + 0.5 * Math.sin(glowPhase));

        // Title
        String title = "OUT OF MOVES";
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2, ty = h / 2 - 20;
        int ga = (int)(60 + 80 * glow);
        g2.setColor(new Color(255, 80, 80, ga));
        for (int r = 4; r >= 1; r--) g2.drawString(title, tx + r, ty + r);
        g2.setColor(new Color(255, 120, 120));
        g2.drawString(title, tx, ty);

        // Score vs target
        g2.setFont(new Font("Arial", Font.BOLD, 17));
        String sc = score + " / " + target + " pts";
        fm = g2.getFontMetrics();
        g2.setColor(new Color(200, 180, 180));
        g2.drawString(sc, (w - fm.stringWidth(sc)) / 2, h / 2 + 20);

        // Progress bar
        int bw = 200, bh = 7;
        int bx = (w - bw) / 2, by = h / 2 + 32;
        g2.setColor(new Color(255, 255, 255, 25));
        g2.fillRoundRect(bx, by, bw, bh, bh, bh);
        int filled = (int)(bw * Math.min(1.0, (double) score / target));
        g2.setColor(new Color(255, 110, 110));
        g2.fillRoundRect(bx, by, Math.max(0, filled), bh, bh, bh);
    }
}
