import java.awt.*;
import javax.swing.*;

/**
 * Overlay hiển thị kết quả màn: LEVEL CLEAR / OUT OF MOVES / YOU WIN.
 * Thiết kế theo phong cách Candy Crush.
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
    private Color btnColor = new Color(60, 190, 100); // Màu mặc định của nút

    // Confetti particles (Hiệu ứng pháo giấy)
    private static final int NUM_PARTICLES = 40;
    private final float[] px = new float[NUM_PARTICLES];
    private final float[] py = new float[NUM_PARTICLES];
    private final float[] pvx = new float[NUM_PARTICLES];
    private final float[] pvy = new float[NUM_PARTICLES];
    private final Color[] confettiColors = {
        new Color(255, 215, 50), new Color(100, 220, 130),
        new Color(150, 100, 255), new Color(255, 100, 150),
        new Color(100, 200, 255), new Color(255, 160, 50)
    };

    public LevelOverlay() {
        setOpaque(false);
        setLayout(null);

        // ── KHỞI TẠO NÚT BẤM KẸO NGỌT ──
        actionBtn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Bóng đổ
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);

                // Màu nền thay đổi theo hiệu ứng hover/click
                Color bgColor = getModel().isPressed() ? btnColor.darker() : btnColor;
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 6, 40, 40);
                
                // Viền sáng bóng bên trong (Glossy effect)
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(4, 4, getWidth() - 12, getHeight() - 14, 30, 30);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        actionBtn.setFont(new Font("Arial", Font.BOLD, 18));
        actionBtn.setForeground(Color.WHITE);
        actionBtn.setContentAreaFilled(false);
        actionBtn.setBorderPainted(false);
        actionBtn.setFocusPainted(false);
        actionBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        actionBtn.addActionListener(e -> { if (action != null) action.onAction(); });
        add(actionBtn);

        animTimer = new Timer(16, e -> {
            glowPhase += 0.08f;
            updateParticles();
            repaint();
        });

        setVisible(false);
    }

    @Override
    public void doLayout() {
        int w = getWidth(), h = getHeight();
        // Căn giữa nút bấm ở nửa dưới của hộp thoại
        actionBtn.setBounds((w - 220) / 2, h / 2 + 50, 220, 50);
    }

    // ── Public API ──────────────────────────────────────────────────────────

    public void showWin(int score, int target, OverlayAction action) {
        this.mode = Mode.WIN; this.score = score; this.target = target; this.action = action;
        actionBtn.setText("NEXT LEVEL");
        btnColor = new Color(50, 205, 50); // Xanh lá Candy
        spawnParticles();
        setVisible(true); animTimer.start();
    }

    public void showFinalWin(int score, OverlayAction action) {
        this.mode = Mode.FINAL_WIN; this.score = score; this.action = action;
        actionBtn.setText("PLAY AGAIN");
        btnColor = new Color(255, 165, 0); // Cam Candy
        spawnParticles();
        setVisible(true); animTimer.start();
    }

    public void showLose(int score, int target, OverlayAction action) {
        this.mode = Mode.LOSE; this.score = score; this.target = target; this.action = action;
        actionBtn.setText("TRY AGAIN");
        btnColor = new Color(255, 20, 147); // Hồng DeepPink Candy
        setVisible(true); animTimer.start();
    }

    public void hideOverlay() { animTimer.stop(); setVisible(false); }

    // ── Hiệu ứng pháo giấy (Particles) ───────────────────────────────────────

    private void spawnParticles() {
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < NUM_PARTICLES; i++) {
            px[i]  = rng.nextFloat();
            py[i]  = -rng.nextFloat() * 0.3f;
            pvx[i] = (rng.nextFloat() - 0.5f) * 0.005f;
            pvy[i] = rng.nextFloat() * 0.005f + 0.003f;
        }
    }

    private void updateParticles() {
        for (int i = 0; i < NUM_PARTICLES; i++) {
            px[i] += pvx[i]; py[i] += pvy[i];
            if (py[i] > 1.1f) { py[i] = -0.05f; px[i] = (float)Math.random(); }
        }
    }

    // ── HÀM TIỆN ÍCH: VẼ CHỮ CÓ VIỀN (OUTLINE TEXT) ─────────────────────────
    private void drawOutlinedText(Graphics2D g2, String text, int x, int y, Color fill, Color outline, int thickness) {
        g2.setColor(outline);
        // Vẽ các bóng xung quanh để tạo viền
        for (int dx = -thickness; dx <= thickness; dx++) {
            for (int dy = -thickness; dy <= thickness; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(text, x + dx, y + dy);
            }
        }
        // Vẽ chữ chính ở giữa
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }

    // ── VẼ GIAO DIỆN CHÍNH ──────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        // Lớp nền đen mờ
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, w, h);

        // Kích thước hộp thoại
        int bw = 340, bh = 240;
        int bx = (w - bw) / 2, by = (h - bh) / 2 - 20;

        // Vẽ hộp thoại kẹo ngọt
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(bx + 8, by + 8, bw, bh, 50, 50); // Bóng đổ

        if (mode == Mode.LOSE) {
            // Hộp thoại Thua (Viền Tím Đậm, Nền Tím Nhạt)
            g2.setColor(new Color(138, 43, 226)); // BlueViolet
            g2.fillRoundRect(bx, by, bw, bh, 45, 45);
            g2.setColor(new Color(230, 230, 250)); // Lavender
            g2.fillRoundRect(bx + 8, by + 8, bw - 16, bh - 16, 35, 35);
        } else {
            // Hộp thoại Thắng (Viền Vàng Nhạt, Nền Xanh Nhạt)
            g2.setColor(new Color(255, 215, 0)); // Gold
            g2.fillRoundRect(bx, by, bw, bh, 45, 45);
            g2.setColor(new Color(224, 255, 255)); // LightCyan
            g2.fillRoundRect(bx + 8, by + 8, bw - 16, bh - 16, 35, 35);
        }

        // Vẽ hiệu ứng pháo giấy nếu thắng
        if (mode != Mode.LOSE) drawConfetti(g2, w, h);

        // Vẽ Nội dung (Chữ, Ngôi sao, Điểm)
        if (mode == Mode.LOSE) drawLoseContent(g2, w, h, bx, by, bw, bh);
        else                   drawWinContent(g2, w, h, bx, by, bw, bh);

        g2.dispose();
    }

    private void drawConfetti(Graphics2D g2, int w, int h) {
        for (int i = 0; i < NUM_PARTICLES; i++) {
            g2.setColor(confettiColors[i % confettiColors.length]);
            // Vẽ pháo giấy hình tròn cho dễ thương
            g2.fillOval((int)(px[i] * w), (int)(py[i] * h), 10, 10);
        }
    }

    private void drawWinContent(Graphics2D g2, int w, int h, int bx, int by, int bw, int bh) {
        float glow = (float)(0.5 + 0.5 * Math.sin(glowPhase));

        // 1. Vẽ 3 ngôi sao (Dùng text có viền)
        g2.setFont(new Font("SansSerif", Font.BOLD, 55));
        String stars = "★★★";
        FontMetrics fm = g2.getFontMetrics();
        int sx = (w - fm.stringWidth(stars)) / 2;
        int sy = by + 50;
        // Chữ vàng, viền cam đậm
        drawOutlinedText(g2, stars, sx, sy, new Color(255, 215, 0), new Color(184, 134, 11), 3);

        // 2. Vẽ Tiêu đề "LEVEL CLEARED" nhấp nháy
        String title = (mode == Mode.FINAL_WIN) ? "YOU WIN!" : "LEVEL CLEARED!";
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2;
        int ty = by + 100;
        // Chữ sáng lên theo glow
        Color titleFill = new Color(255, (int)(200 + 55 * glow), 50); 
        drawOutlinedText(g2, title, tx, ty, titleFill, new Color(139, 69, 19), 3); // Viền nâu

        // 3. Vẽ Điểm số
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        String sc = "SCORE: " + score;
        fm = g2.getFontMetrics();
        drawOutlinedText(g2, sc, (w - fm.stringWidth(sc)) / 2, by + 145, Color.WHITE, new Color(0, 100, 200), 2);
    }

    private void drawLoseContent(Graphics2D g2, int w, int h, int bx, int by, int bw, int bh) {
        // 1. Vẽ 3 ngôi sao Rỗng (Thất bại)
        g2.setFont(new Font("SansSerif", Font.BOLD, 55));
        String stars = "☆☆☆";
        FontMetrics fm = g2.getFontMetrics();
        int sx = (w - fm.stringWidth(stars)) / 2;
        int sy = by + 50;
        drawOutlinedText(g2, stars, sx, sy, new Color(150, 150, 150), new Color(100, 100, 100), 2);

        // 2. Vẽ Tiêu đề "OUT OF MOVES"
        String title = "OUT OF MOVES!";
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2;
        int ty = by + 100;
        drawOutlinedText(g2, title, tx, ty, new Color(255, 100, 100), new Color(100, 0, 0), 3);

        // 3. Điểm số vs Mục tiêu
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        String sc = score + " / " + target;
        fm = g2.getFontMetrics();
        drawOutlinedText(g2, sc, (w - fm.stringWidth(sc)) / 2, by + 135, Color.WHITE, new Color(75, 0, 130), 2);

        // 4. Thanh tiến trình kẹo ngọt (Progress Bar)
        int barW = 240, barH = 14;
        int barX = (w - barW) / 2, barY = by + 150;
        
        // Vỏ thanh tiến trình
        g2.setColor(new Color(150, 100, 200));
        g2.fillRoundRect(barX, barY, barW, barH, barH, barH);
        
        // Lõi thanh tiến trình
        double ratio = Math.min(1.0, (double) score / target);
        int filledW = (int)(barW * ratio);
        if (filledW > 0) {
            g2.setColor(new Color(255, 105, 180)); // HotPink
            g2.fillRoundRect(barX, barY, filledW, barH, barH, barH);
            // Vệt sáng cho giống thạch
            g2.setColor(new Color(255, 255, 255, 150));
            g2.fillRoundRect(barX + 2, barY + 2, filledW - 4, barH / 2 - 2, barH, barH);
        }
    }
}