import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

public class RecordScreen extends JPanel {

    private static final Color BG_TOP = new Color(10, 5, 30);
    private static final Color BG_BOT = new Color(25, 10, 60);
    private static final Color GOLD = new Color(255, 215, 50);

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 40);
    private static final Font ITEM_FONT = new Font("Arial", Font.BOLD, 24);

    private RecordManager manager;
    private Runnable onBack;

    private Rectangle backHitbox;
    private Image backgroundImage;

    public RecordScreen() {

        manager = new RecordManager();

        try {
            backgroundImage = new ImageIcon("images/background1.png").getImage();
        } catch (Exception e) {}

        setFocusable(true);

        // ===== KEY =====
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        || e.getKeyCode() == KeyEvent.VK_ESCAPE) {

                    if (onBack != null) onBack.run();
                }
            }
        });

        // ===== MOUSE =====
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (backHitbox != null && backHitbox.contains(e.getPoint())) {
                    if (onBack != null) onBack.run();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (backHitbox != null && backHitbox.contains(e.getPoint())) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    public void setOnBack(Runnable r) {
        this.onBack = r;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        int w = getWidth();
        int h = getHeight();

        // ===== BACKGROUND =====
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, w, h, this);
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRect(0, 0, w, h);
        } else {
            GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOT);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }

        // ===== TITLE =====
        g2.setFont(TITLE_FONT);
        g2.setColor(Color.WHITE);

        String title = "RECORDS";
        int titleW = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (w - titleW) / 2, h / 4);

        // ===== RECORD LIST =====
        List<GameRecord> list = manager.getRecords();

        g2.setFont(ITEM_FONT);

        int startY = h / 3;
        int spacing = 50;

        for (int i = 0; i < list.size(); i++) {
            GameRecord r = list.get(i);

            String text = (i + 1) + ". Score: " + r.score + "   Level: " + r.level;

            int textW = g2.getFontMetrics().stringWidth(text);
            int x = (w - textW) / 2;
            int y = startY + i * spacing;

            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString(text, x, y);
        }

        // ===== BACK BUTTON =====
        String back = "BACK";
        g2.setFont(new Font("Arial", Font.BOLD, 28));

        int backW = g2.getFontMetrics().stringWidth(back);
        int bx = (w - backW) / 2;
        int by = h - 120;

        backHitbox = new Rectangle(bx - 40, by - 30, backW + 80, 50);

        // hover effect
        Point mouse = getMousePosition();
        boolean hover = mouse != null && backHitbox.contains(mouse);

        g2.setColor(hover ? GOLD : Color.WHITE);
        g2.drawString(back, bx, by);

        if (hover) {
            g2.drawString("▶", bx - 40, by);
        }
    }
}