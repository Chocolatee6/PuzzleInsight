import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class StartMenu extends JPanel {

    private static final Color BG_TOP = new Color(10, 5, 30);
    private static final Color BG_BOT = new Color(25, 10, 60);
    private static final Color ACCENT = new Color(160, 80, 255);
    private static final Color GOLD = new Color(255, 215, 50);

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 40);
    private static final Font MENU_FONT = new Font("Arial", Font.BOLD, 24);

    // Thay mảng cố định bằng biến cờ (flag) để quản lý hiển thị Resume
    private boolean canResume = false; 
    private int selectedIndex = 0;
    
    private Rectangle[] hitboxes;
    private Image backgroundImage;

    // Cập nhật lại Interface cho rõ ràng
    public interface MenuAction {
        void onResume();
        void onNewGame();
        void onOptions();
        void onExit();
    }

    private MenuAction actionListener;

    public StartMenu() {
        try {
            backgroundImage = new ImageIcon("images/background2.png").getImage();
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh nền Menu!");
        }

        setOpaque(true);
        setFocusable(true);

        // 1. Xử lý bàn phím
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                List<String> currentMenu = getMenuItems();
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    selectedIndex--;
                    if (selectedIndex < 0) selectedIndex = currentMenu.size() - 1;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    selectedIndex++;
                    if (selectedIndex >= currentMenu.size()) selectedIndex = 0;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSelection();
                }
            }
        });

        // 2. Xử lý chuột
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean isHovering = false;
                if (hitboxes == null) return;
                
                for (int i = 0; i < hitboxes.length; i++) {
                    if (hitboxes[i] != null && hitboxes[i].contains(e.getPoint())) {
                        if (selectedIndex != i) {
                            selectedIndex = i;
                            repaint(); 
                        }
                        isHovering = true;
                        break;
                    }
                }
                setCursor(isHovering ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) 
                                   : Cursor.getDefaultCursor());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (hitboxes == null) return;
                for (int i = 0; i < hitboxes.length; i++) {
                    if (hitboxes[i] != null && hitboxes[i].contains(e.getPoint())) {
                        selectedIndex = i;
                        handleSelection();
                        return;
                    }
                }
            }
        };
        addMouseMotionListener(mouseAdapter);
        addMouseListener(mouseAdapter);
    }

    public void setMenuAction(MenuAction action) {
        this.actionListener = action;
    }

    // ── HÀM MỚI: Bật/Tắt nút Resume từ bên ngoài ──
    public void setResumeVisible(boolean visible) {
        this.canResume = visible;
        this.selectedIndex = 0; // Đưa con trỏ về vị trí đầu tiên
        repaint();
    }

    // ── HÀM MỚI: Lấy danh sách menu động ──
    private List<String> getMenuItems() {
        List<String> items = new ArrayList<>();
        if (canResume) {
            items.add("RESUME");
        }
        items.add("NEW GAME");
        items.add("OPTIONS");
        items.add("EXIT");
        return items;
    }

    private void handleSelection() {
        if (actionListener != null) {
            String selectedItem = getMenuItems().get(selectedIndex);
            switch (selectedItem) {
                case "RESUME":
                    actionListener.onResume();
                    break;
                case "NEW GAME":
                    actionListener.onNewGame();
                    break;
                case "OPTIONS":
                    actionListener.onOptions();
                    break;
                case "EXIT":
                    actionListener.onExit();
                    break;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, w, h, this);
            g2.setColor(new Color(0, 0, 0, 100)); 
            g2.fillRect(0, 0, w, h);
        } else {
            GradientPaint gp = new GradientPaint(0, 0, BG_TOP, 0, h, BG_BOT);
            g2.setPaint(gp);
            g2.fillRect(0, 0, w, h);
        }

        // Title
        String title = "PUZZLE INSIGHT";
        g2.setFont(TITLE_FONT);
        FontMetrics fmTitle = g2.getFontMetrics();
        int tx = (w - fmTitle.stringWidth(title)) / 2;
        int ty = h / 3; 

        // Title Glow
        for (int r = 8; r >= 1; r--) {
            int a = (int) (40.0 * (1.0 - r / 9.0));
            g2.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), a));
            g2.drawString(title, tx, ty + r);
            g2.drawString(title, tx, ty - r);
            g2.drawString(title, tx + r, ty);
            g2.drawString(title, tx - r, ty);
        }
        g2.setColor(Color.WHITE);
        g2.drawString(title, tx, ty);

        // Lấy danh sách menu hiện tại
        List<String> currentMenu = getMenuItems();
        hitboxes = new Rectangle[currentMenu.size()];

        g2.setFont(MENU_FONT);
        FontMetrics fmMenu = g2.getFontMetrics();
        int menuStartY = h / 2 + 30; 
        int itemSpacing = 50;

        for (int i = 0; i < currentMenu.size(); i++) {
            String item = currentMenu.get(i);
            int itemW = fmMenu.stringWidth(item);
            int itemH = fmMenu.getHeight();
            
            int ix = (w - itemW) / 2;
            int iy = menuStartY + (i * itemSpacing);

            // Vẽ Hitbox bao quanh
            hitboxes[i] = new Rectangle(ix - 30, iy - fmMenu.getAscent() - 5, itemW + 60, itemH + 10);

            if (i == selectedIndex) {
                g2.setColor(Color.YELLOW);
                int[] xPoints = {ix - 35, ix - 20, ix - 35}; 
                int[] yPoints = {iy - 20, iy - 10, iy};      
                g2.fillPolygon(xPoints, yPoints, 3);
            } else {
                g2.setColor(new Color(200, 200, 200)); 
            }
            
            // Đổi màu đặc biệt cho chữ RESUME để nổi bật
            if (item.equals("RESUME") && i != selectedIndex) {
                g2.setColor(new Color(100, 255, 150)); 
            }
            
            g2.drawString(item, ix, iy);
        }

        g2.dispose();
    }
}