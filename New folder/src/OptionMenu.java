import javax.swing.*;
import java.awt.*;

public class OptionMenu extends JPanel {

    private JCheckBox soundBox;
    private JCheckBox musicBox;
    private JSlider volumeSlider;
    private JButton backBtn;

    public interface OptionAction {
        void onBack();
    }

    private OptionAction listener;

    public OptionMenu() {

        setLayout(new GridBagLayout());
        setBackground(new Color(40, 0, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // ===== TITLE =====
        JLabel title = new JLabel("OPTIONS");
        title.setFont(new Font("Arial", Font.BOLD, 42));
        title.setForeground(Color.WHITE);
        add(title, gbc);

        // ===== SOUND =====
        gbc.gridy++;
        JPanel soundPanel = createRow("SOUND");
        soundBox = new JCheckBox();
        styleCheckBox(soundBox);
        soundBox.setSelected(SoundManager.isSoundOn());

        soundBox.addActionListener(e -> {
            SoundManager.setSound(soundBox.isSelected());
        });

        soundPanel.add(soundBox, BorderLayout.EAST);
        add(soundPanel, gbc);

        // ===== MUSIC =====
        gbc.gridy++;
        JPanel musicPanel = createRow("MUSIC");
        musicBox = new JCheckBox();
        styleCheckBox(musicBox);
        musicBox.setSelected(SoundManager.isMusicOn());

        musicBox.addActionListener(e -> {
            boolean m = musicBox.isSelected();
            SoundManager.setMusic(m);
            if (m) {
                SoundManager.playMusicLoop("sounds/loop.wav");
            }
        });

        musicPanel.add(musicBox, BorderLayout.EAST);
        add(musicPanel, gbc);

        // ===== VOLUME =====
        gbc.gridy++;
        JPanel volPanel = createRow("VOLUME");

        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setPreferredSize(new Dimension(200, 30));
        volumeSlider.addChangeListener(e ->
                SoundManager.setVolume(volumeSlider.getValue())
        );
        volumeSlider.setOpaque(false);
        volumeSlider.setBackground(new Color(0,0,0,0));
        volumeSlider.setForeground(Color.YELLOW); // màu thanh

        // ẩn nền trắng mặc định
        volumeSlider.setUI(new javax.swing.plaf.basic.BasicSliderUI(volumeSlider) {
            @Override
            public void paintTrack(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.GRAY);
                g2.fillRoundRect(trackRect.x, trackRect.y + trackRect.height / 2 - 2,
                        trackRect.width, 4, 5, 5);
            }

            @Override
            public void paintThumb(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.YELLOW);
                g2.fillOval(thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height);
            }
        });
        volPanel.add(volumeSlider, BorderLayout.EAST);
        add(volPanel, gbc);

        // ===== BACK BUTTON =====
        gbc.gridy++;
        backBtn = new JButton("BACK");
        styleButton(backBtn);

        backBtn.addActionListener(e -> {
            if (listener != null) listener.onBack();
        });

        add(backBtn, gbc);

        // focus mặc định vào BACK để Enter ăn luôn
        SwingUtilities.invokeLater(() -> backBtn.requestFocusInWindow());
    }

    // ===== TẠO ROW =====
    private JPanel createRow(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(400, 50));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.LIGHT_GRAY);

        panel.add(label, BorderLayout.WEST);

        return panel;
    }

    // ===== STYLE CHECKBOX =====
    private void styleCheckBox(JCheckBox box) {
        box.setOpaque(false);
        box.setPreferredSize(new Dimension(30, 30));
    }

    // ===== STYLE BUTTON =====
    private void styleButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 24));
        btn.setForeground(Color.BLACK);
        btn.setBackground(new Color(255, 215, 50));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 50));
    }

    public void setOptionAction(OptionAction l) {
        this.listener = l;
    }
}