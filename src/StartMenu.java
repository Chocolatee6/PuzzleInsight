import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class StartMenu extends JPanel {

    private static final Color BG_TOP = new Color(10, 5, 30);
    private static final Color BG_BOT = new Color(25, 10, 60);
    private static final Color ACCENT = new Color(160, 80, 255);
    private static final Color GOLD = new Color(255, 215, 50);

    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 40);
    private static final Font MENU_FONT = new Font("Arial", Font.BOLD, 24);

    private final String[] menuItems = {"PLAY", "OPTIONS", "EXIT"};
    private int selectedIndex = 0;
    
    // Mảng lưu trữ vùng chạm (hitbox) của từng nút
    private Rectangle[] hitboxes = new Rectangle[menuItems.length];

    private Image backgroundImage;

    public interface MenuAction {
        void onPlay();
        void onExit();
    }

    private MenuAction actionListener;

    public StartMenu() {

        try {
            backgroundImage = new ImageIcon("images/background1.png").getImage();
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh nền Menu!");
        }

        setOpaque(true);
        setFocusable(true);

        // 1. Xử lý bàn phím (Giữ nguyên)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    selectedIndex--;
                    if (selectedIndex < 0) selectedIndex = menuItems.length - 1;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    selectedIndex++;
                    if (selectedIndex >= menuItems.length) selectedIndex = 0;
                    repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleSelection();
                }
            }
        });

        // 2. Xử lý chuột (Thêm mới)
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean isHovering = false;
                for (int i = 0; i < hitboxes.length; i++) {
                    // Kiểm tra xem chuột có nằm trong vùng hitbox không
                    if (hitboxes[i] != null && hitboxes[i].contains(e.getPoint())) {
                        if (selectedIndex != i) {
                            selectedIndex = i;
                            repaint(); // Vẽ lại để cập nhật con trỏ mũi tên
                        }
                        isHovering = true;
                        break;
                    }
                }
                // Đổi con trỏ thành hình bàn tay nếu đang lướt qua nút
                setCursor(isHovering ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) 
                                   : Cursor.getDefaultCursor());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // Khi click chuột, kiểm tra xem có đang click trúng hitbox nào không
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

    private void handleSelection() {
        if (actionListener != null) {
            switch (selectedIndex) {
                case 0: // PLAY
                    actionListener.onPlay();
                    break;
                case 1: // OPTIONS
                    System.out.println("Options selected");
                    break;
                case 2: // EXIT
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

        // Background
        if (backgroundImage != null) {
            // Vẽ ảnh full màn hình
            g2.drawImage(backgroundImage, 0, 0, w, h, this);
            
            // Tùy chọn: Phủ thêm một lớp màu đen mờ (opacity) lên trên ảnh
            // để làm nổi bật chữ PUZZLE INSIGHT và các nút Menu
            g2.setColor(new Color(0, 0, 0, 100)); // Số 100 là độ mờ, bạn có thể chỉnh (0-255)
            g2.fillRect(0, 0, w, h);
        } else {
            // Nếu lỗi không load được ảnh thì quay về vẽ Gradient mặc định
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

        // Menu Items
        g2.setFont(MENU_FONT);
        FontMetrics fmMenu = g2.getFontMetrics();
        int menuStartY = h / 2 + 30; 
        int itemSpacing = 50;

        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];
            int itemW = fmMenu.stringWidth(item);
            int itemH = fmMenu.getHeight();
            
            int ix = (w - itemW) / 2;
            int iy = menuStartY + (i * itemSpacing);

            // CẬP NHẬT HITBOX LÚC VẼ:
            // Tính toán tạo khung chữ nhật bao quanh chữ. Cộng trừ thêm vài pixel để dễ bấm hơn.
            hitboxes[i] = new Rectangle(ix - 30, iy - fmMenu.getAscent() - 5, itemW + 60, itemH + 10);

            if (i == selectedIndex) {
                g2.setColor(GOLD);
                g2.drawString("▶", ix - 30, iy); // Con trỏ
            } else {
                g2.setColor(new Color(200, 200, 200)); 
            }
            g2.drawString(item, ix, iy);
            
            // Bật dòng này lên nếu bạn muốn nhìn thấy hitbox tàng hình (dùng để debug)
            // g2.draw(hitboxes[i]); 
        }

        g2.dispose();
    }
}