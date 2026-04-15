import java.awt.*;
import java.util.Random;
import javax.swing.*;

/**
 * LevelOverlay chịu trách nhiệm hiển thị kết quả màn: LEVEL CLEAR / OUT OF MOVES / YOU WIN.
 * Thiết kế theo phong cách kẹo ngọt Candy Crush.
 */
public class LevelOverlay extends JPanel {

    public enum Mode { WIN, LOSE, FINAL_WIN }
    public interface OverlayAction { void onAction(); }

    private Mode mode = Mode.WIN;
    private int score, target;
    
    private OverlayAction action;
    private Runnable homeAction; // Hành động khi bấm nút Menu
    
    private final CandyButton actionBtn;
    private final CandyButton homeBtn; // Nút Menu mới
    
    private final Timer animTimer;
    private float glowPhase = 0f;

    // Cấu hình confetti particles (pháo giấy)
    private static final int NUM_PARTICLES = 50;
    private final float[] px = new float[NUM_PARTICLES];
    private final float[] py = new float[NUM_PARTICLES];
    private final float[] pvx = new float[NUM_PARTICLES];
    private final float[] pvy = new float[NUM_PARTICLES];
    private final Color[] confettiColors = {
        new Color(255, 105, 180), new Color(100, 220, 130),
        new Color(255, 215, 50),  new Color(150, 100, 255),
        new Color(100, 200, 255), new Color(255, 160, 50)
    };

    // ── CLASS CON: NÚT BẤM KẸO NGỌT ──
    private class CandyButton extends JButton {
        private Color btnColor;

        public CandyButton(String text, Color color) {
            super(text);
            this.btnColor = color;
            setFont(new Font("Arial", Font.BOLD, 16));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void setColor(Color c) {
            this.btnColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // Bóng đổ
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillRoundRect(2, 4, w - 4, h - 4, 40, 40);

            // Màu nền thay đổi khi click
            Color bgColor = getModel().isPressed() ? btnColor.darker() : btnColor;
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, w - 4, h - 6, 40, 40);
            
            // Viền sáng bóng (Glossy)
            g2.setColor(new Color(255, 255, 255, 100));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(4, 4, w - 12, h - 14, 30, 30);
            
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public LevelOverlay() {
        setOpaque(false);
        setLayout(null); // Để tự do setBounds

        // 1. Khởi tạo nút Action (Try Again / Next Level)
        actionBtn = new CandyButton("", Color.WHITE);
        actionBtn.addActionListener(e -> { if (action != null) action.onAction(); });
        add(actionBtn);

        // 2. Khởi tạo nút Home (Menu)
        homeBtn = new CandyButton("MENU", new Color(30, 144, 255)); // Màu xanh dương nhạt (DodgerBlue)
        homeBtn.addActionListener(e -> { if (homeAction != null) homeAction.run(); });
        add(homeBtn);

        animTimer = new Timer(16, e -> {
            glowPhase += 0.08f;
            updateParticles();
            repaint();
        });

        setVisible(false);
    }
    
    // Đón nhận hành động chuyển về Menu từ GameUI
    public void setHomeAction(Runnable homeAction) {
        this.homeAction = homeAction;
    }

    @Override
    public void doLayout() {
        super.doLayout();
        int w = getWidth(), h = getHeight();
        int sy = h / 2 + 40; // Tọa độ Y chung cho các nút

        if (mode == Mode.LOSE) {
            // Khi thua: Hiện 2 nút cạnh nhau
            homeBtn.setVisible(true);
            int btnW = 135; // Chiều rộng mỗi nút
            int gap = 15;   // Khoảng cách giữa 2 nút
            int totalW = btnW * 2 + gap;
            int sx = (w - totalW) / 2; // Căn giữa toàn bộ cụm 2 nút
            
            homeBtn.setBounds(sx, sy, btnW, 50);
            actionBtn.setBounds(sx + btnW + gap, sy, btnW, 50);
        } else {
            // Khi thắng: Ẩn nút Menu, hiện 1 nút to ở giữa
            homeBtn.setVisible(false);
            actionBtn.setBounds((w - 220) / 2, sy, 220, 50);
        }
    }

    // ── Public API ──────────────────────────────────────────────────────────

    public void showWin(int score, int target, OverlayAction action) {
        this.mode = Mode.WIN; this.score = score; this.target = target; this.action = action;
        actionBtn.setText("NEXT LEVEL");
        actionBtn.setColor(new Color(50, 205, 50)); // Xanh lá
        spawnParticles();
        setVisible(true); animTimer.start();
    }

    public void showFinalWin(int score, OverlayAction action) {
        this.mode = Mode.FINAL_WIN; this.score = score; this.action = action;
        actionBtn.setText("PLAY AGAIN");
        actionBtn.setColor(new Color(255, 165, 0)); // Cam
        spawnParticles();
        setVisible(true); animTimer.start();
    }

    public void showLose(int score, int target, OverlayAction action) {
        this.mode = Mode.LOSE; this.score = score; this.target = target; this.action = action;
        actionBtn.setText("RETRY"); // Sửa lại chữ cho ngắn bớt để vừa nút nhỏ
        actionBtn.setColor(new Color(255, 20, 147)); // Hồng đậm
        setVisible(true); animTimer.start();
    }

    public void hideOverlay() { animTimer.stop(); setVisible(false); }

    // ── Hiệu ứng pháo giấy & Vẽ giao diện (Giữ nguyên) ───────────────────────

    private void spawnParticles() {
        Random rng = new Random();
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

    private void drawCandyText(Graphics2D g2, String text, int x, int y, Color fill, Color outline, int thickness) {
        g2.setColor(outline);
        for (int dx = -thickness; dx <= thickness; dx++) {
            for (int dy = -thickness; dy <= thickness; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(text, x + dx, y + dy);
            }
        }
        g2.setColor(fill);
        g2.drawString(text, x, y);
    }

    private void drawWrappedCandy(Graphics2D g2, int x, int y, Color color) {
        g2.setColor(color);
        int w = 26, h = 26;
        g2.fillOval(x, y, w, h);
        
        int endW = 12, endH = 22;
        int[] exl = {x, x - endW, x - endW, x};
        int[] eyl = {y + (h - endH) / 2, y, y + endH, y + (h + endH) / 2};
        g2.fillPolygon(exl, eyl, 4);
        
        int[] exr = {x + w, x + w + endW, x + w + endW, x + w};
        int[] eyr = {y + (h - endH) / 2, y, y + endH, y + (h + endH) / 2};
        g2.fillPolygon(exr, eyr, 4);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRect(0, 0, w, h);

        int bw = 340, bh = 240;
        int bx = (w - bw) / 2, by = (h - bh) / 2 - 20;

        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillRoundRect(bx + 8, by + 8, bw, bh, 50, 50); 
        
        g2.setColor(Color.WHITE); 
        g2.fillRoundRect(bx, by, bw, bh, 45, 45);
        
        g2.setColor(new Color(255, 105, 180)); 
        g2.setStroke(new BasicStroke(8f)); 
        g2.drawRoundRect(bx + 6, by + 6, bw - 16, bh - 16, 40, 40);

        if (mode != Mode.LOSE) drawConfetti(g2, w, h);

        if (mode == Mode.LOSE) drawLoseContent(g2, bx, by, bw, bh);
        else                   drawWinContent(g2, bx, by, bw, bh);

        g2.dispose();
    }

    private void drawConfetti(Graphics2D g2, int w, int h) {
        for (int i = 0; i < NUM_PARTICLES; i++) {
            g2.setColor(confettiColors[i % confettiColors.length]);
            g2.fillOval((int)(px[i] * w), (int)(py[i] * h), 10, 10);
        }
    }

    private void drawWinContent(Graphics2D g2, int bx, int by, int bw, int bh) {
        float glow = (float)(0.5 + 0.5 * Math.sin(glowPhase));

        String title = (mode == Mode.FINAL_WIN) ? "YOU WIN!" : "LEVEL CLEARED!";
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        int tx = bx + (bw - fm.stringWidth(title)) / 2;
        int ty = by + 90;
        
        drawCandyText(g2, title, tx, ty, Color.WHITE, new Color(139, 69, 19), 3); 

        drawWrappedCandy(g2, tx - 50, ty - 22, new Color(255, 165, 0)); 
        drawWrappedCandy(g2, tx + fm.stringWidth(title) + 15, ty - 22, new Color(100, 220, 130)); 

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        String sc = "SCORE: " + score;
        fm = g2.getFontMetrics();
        drawCandyText(g2, sc, bx + (bw - fm.stringWidth(sc)) / 2, by + 140, Color.WHITE, new Color(0, 100, 200), 2);
    }

    
    private void drawLoseContent(Graphics2D g2, int bx, int by, int bw, int bh) {
        String title = "OUT OF MOVES!";
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        int tx = bx + (bw - fm.stringWidth(title)) / 2;
        int ty = by + 90;
        
        drawCandyText(g2, title, tx, ty, Color.WHITE, new Color(255, 20, 147), 3); 

        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String sc = score + " / " + target;
        fm = g2.getFontMetrics();
        drawCandyText(g2, sc, bx + (bw - fm.stringWidth(sc)) / 2, by + 135, Color.WHITE, new Color(75, 0, 130), 2);
    }
}