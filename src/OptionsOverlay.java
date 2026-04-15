import java.awt.*;
import javax.swing.*;

public class OptionsOverlay extends JPanel {

    public interface OptionsListener {
        void onClose();
    }
    private OptionsListener listener;

    public OptionsOverlay() {
        setOpaque(false);
        setLayout(new GridBagLayout()); // Tự động căn giữa bảng

        // Tạo khung hộp thoại bo góc màu hồng nhạt
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 245, 250)); // Nền trắng hồng
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(new Color(255, 100, 160)); // Viền hồng đậm
                g2.setStroke(new BasicStroke(6));
                g2.drawRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 36, 36);
            }
        };
        panel.setPreferredSize(new Dimension(400, 300));
        panel.setOpaque(false);
        panel.setLayout(null);

        // ── TIÊU ĐỀ ──
        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(new Color(255, 100, 160));
        title.setBounds(0, 20, 400, 40);
        panel.add(title);

        // ── THANH TRƯỢT ÂM LƯỢNG (SLIDER) ──
        JLabel volLabel = new JLabel("Volume:");
        volLabel.setFont(new Font("Arial", Font.BOLD, 18));
        volLabel.setForeground(new Color(100, 80, 90));
        volLabel.setBounds(40, 90, 80, 30);
        panel.add(volLabel);

        // Tạo thanh trượt từ 0 -> 100, mặc định là 80
        JSlider volSlider = new JSlider(0, 100, 80); 
        volSlider.setBounds(120, 90, 230, 30);
        volSlider.setOpaque(false);
        // Bắt sự kiện mỗi khi kéo thanh trượt
        volSlider.addChangeListener(e -> {
            SoundManager.setVolume(volSlider.getValue());
        });
        panel.add(volSlider);

        // ── NÚT BẬT/TẮT NHẠC NỀN ──
        JButton btnMusic = new JButton("Music: " + (SoundManager.isMusicOn() ? "ON" : "OFF"));
        btnMusic.setFont(new Font("Arial", Font.BOLD, 14));
        btnMusic.setBounds(70, 150, 120, 40);
        btnMusic.addActionListener(e -> {
            boolean isOn = !SoundManager.isMusicOn();
            SoundManager.setMusic(isOn);
            btnMusic.setText("Music: " + (isOn ? "ON" : "OFF"));
            SoundManager.playSound("sounds/click.wav");
        });
        panel.add(btnMusic);

        // ── NÚT BẬT/TẮT TIẾNG ĐỘNG (SFX) ──
        JButton btnSound = new JButton("SFX: " + (SoundManager.isSoundOn() ? "ON" : "OFF"));
        btnSound.setFont(new Font("Arial", Font.BOLD, 14));
        btnSound.setBounds(210, 150, 120, 40);
        btnSound.addActionListener(e -> {
            boolean isOn = !SoundManager.isSoundOn();
            SoundManager.setSound(isOn);
            btnSound.setText("SFX: " + (isOn ? "ON" : "OFF"));
            SoundManager.playSound("sounds/click.wav");
        });
        panel.add(btnSound);

        // ── NÚT ĐÓNG ──
        JButton btnClose = new JButton("CLOSE");
        btnClose.setFont(new Font("Arial", Font.BOLD, 16));
        btnClose.setBounds(140, 220, 120, 45);
        btnClose.addActionListener(e -> {
            SoundManager.playSound("sounds/click.wav");
            if (listener != null) listener.onClose();
        });
        panel.add(btnClose);

        add(panel);
    }

    public void setListener(OptionsListener l) {
        this.listener = l;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Vẽ lớp sương mù đen phía sau bảng setting
        g.setColor(new Color(0, 0, 0, 150)); 
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}