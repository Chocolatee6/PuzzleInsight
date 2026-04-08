import java.awt.*;
import javax.swing.*;

public class PauseOverlay extends JPanel {

    public interface PauseAction {
        void onResume();   // Chơi tiếp (Back)
        void onRestart();  // Chơi lại (Reload)
        void onHome();     // Về màn hình chính (Home)
    }

    private PauseAction actionListener;

    public PauseOverlay() {
        setOpaque(false);
        setLayout(null); // Tự do định vị tọa độ

        // 1. Nút Resume (Chơi tiếp - Dùng ảnh Back)
        JButton btnResume = createImageButton("images/Back (3).png");
        btnResume.setBounds(100, 200, 80, 80); // Căn tọa độ
        btnResume.addActionListener(e -> { if (actionListener != null) actionListener.onResume(); });
        add(btnResume);

        // 2. Nút Reload (Chơi lại màn hiện tại)
        JButton btnReload = createImageButton("images/Reload (3).png");
        btnReload.setBounds(200, 200, 80, 80);
        btnReload.addActionListener(e -> { if (actionListener != null) actionListener.onRestart(); });
        add(btnReload);

        // 3. Nút Home (Về Start Menu)
        JButton btnHome = createImageButton("images/Home (3).png");
        btnHome.setBounds(300, 200, 80, 80);
        btnHome.addActionListener(e -> { if (actionListener != null) actionListener.onHome(); });
        add(btnHome);

        setVisible(false); // Mặc định ẩn đi
    }

    public void setPauseAction(PauseAction action) {
        this.actionListener = action;
    }

    // ── HÀM TẠO NÚT CÓ ÉP SIZE ẢNH ────────────────────────────
    private JButton createImageButton(String imagePath) {
        // Đọc ảnh và thu nhỏ xuống 80x80 để không bị khổng lồ
        ImageIcon icon = new ImageIcon(imagePath);
        Image scaledImg = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        
        JButton btn = new JButton(new ImageIcon(scaledImg));
        btn.setContentAreaFilled(false); // Tắt màu nền vuông
        btn.setBorderPainted(false);     // Tắt viền
        btn.setFocusPainted(false);      // Tắt khung focus khi click
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Phủ một lớp màu đen mờ (opacity 180/255) lên toàn bộ màn hình game
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Vẽ thêm chữ PAUSED cho đẹp
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        g2.setColor(Color.WHITE);
        String text = "PAUSED";
        int textX = (getWidth() - g2.getFontMetrics().stringWidth(text)) / 2;
        g2.drawString(text, textX, 150);
    }
}