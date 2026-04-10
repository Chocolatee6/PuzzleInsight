import java.awt.*;
import javax.swing.*;

public class PauseOverlay extends JPanel {

    public interface PauseAction {
        void onResume();   
        void onRestart();  
        void onHome();     
    }

    private PauseAction actionListener;

    public PauseOverlay() {
        setOpaque(false);
        setLayout(new GridBagLayout()); 

        // ── TẠO BẢNG POPUP (HỘP THOẠI KẸO NGỌT) ──
        JPanel popupBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int w = getWidth();
                int h = getHeight();

                // Bóng đổ
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(6, 6, w - 8, h - 8, 45, 45);
                
                // Nền trắng
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(2, 2, w - 8, h - 8, 45, 45);
                
                // Viền hồng dày
                g2.setColor(new Color(255, 105, 180)); 
                g2.setStroke(new BasicStroke(8f)); 
                g2.drawRoundRect(6, 6, w - 16, h - 16, 40, 40);
                
                // Viền nhạt bên trong
                g2.setColor(new Color(255, 192, 203)); 
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(14, 14, w - 32, h - 32, 25, 25);
                
                g2.dispose();
            }
        };
        
        // Tăng chiều cao lên 200 để chứa thoải mái 2 hàng nút
        popupBox.setPreferredSize(new Dimension(340, 200)); 
        popupBox.setOpaque(false);
        popupBox.setLayout(new GridBagLayout()); 
        GridBagConstraints gbc = new GridBagConstraints();

        // ── HÀNG 1: 2 NÚT TRÒN (RESUME & RELOAD) ──
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        topRow.setOpaque(false); 

        JButton btnResume = createImageButton("images/Back (3).png");
        btnResume.addActionListener(e -> { if (actionListener != null) actionListener.onResume(); });
        
        JButton btnReload = createImageButton("images/Reload (3).png");
        btnReload.addActionListener(e -> { if (actionListener != null) actionListener.onRestart(); });

        topRow.add(btnResume);
        topRow.add(btnReload);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0); // Cách hàng dưới 20px
        popupBox.add(topRow, gbc);

        // ── HÀNG 2: NÚT HOME (Làm theo phong cách nút "End Game" màu hồng) ──
        JButton btnHome = createPillButton("VỀ MENU CHÍNH", "images/Home (3).png");
        btnHome.addActionListener(e -> { if (actionListener != null) actionListener.onHome(); });

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0); // Không cách dưới
        popupBox.add(btnHome, gbc);

        add(popupBox);
        setVisible(false); 
    }

    public void setPauseAction(PauseAction action) {
        this.actionListener = action;
    }

    // ── HÀM TẠO NÚT TRÒN BẰNG ẢNH CÓ SẴN ──
    private JButton createImageButton(String imagePath) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image scaledImg = icon.getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
        
        JButton btn = new JButton(new ImageIcon(scaledImg));
        btn.setContentAreaFilled(false); 
        btn.setBorderPainted(false);     
        btn.setFocusPainted(false);      
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Hiệu ứng click: Nút nhỏ lại một chút
        btn.setPressedIcon(new ImageIcon(icon.getImage().getScaledInstance(65, 65, Image.SCALE_SMOOTH)));
        
        return btn;
    }

    // ── HÀM TẠO NÚT DÀI MÀU HỒNG (GIỐNG NÚT END GAME) ──
    private JButton createPillButton(String text, String imagePath) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Bóng đổ
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(2, 4, getWidth() - 4, getHeight() - 4, 40, 40);

                // Màu nền (đổi sang hồng đậm hơn khi click)
                if (getModel().isPressed()) {
                    g2.setColor(new Color(230, 40, 130)); 
                } else {
                    g2.setColor(new Color(255, 20, 147)); // Màu DeepPink
                }
                g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 6, 40, 40);
                
                // Viền sáng bóng bên trong
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(4, 4, getWidth() - 12, getHeight() - 14, 30, 30);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); 
        btn.setBorderPainted(false);     
        btn.setFocusPainted(false);      
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(240, 45)); // Nút dài ra
        
        // Chèn icon Home vào bên cạnh chữ cho đẹp
        try {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaledImg = icon.getImage().getScaledInstance(26, 26, Image.SCALE_SMOOTH);
            btn.setIcon(new ImageIcon(scaledImg));
            btn.setIconTextGap(10); // Khoảng cách giữa icon và chữ
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh Home!");
        }
        
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Phủ lớp nền đen mờ phía sau
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}