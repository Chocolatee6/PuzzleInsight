import javax.swing.*;
import java.awt.*;

public class SettingPanel extends JPanel {

    Main main;

    public SettingPanel(Main main){
        this.main = main;

        setLayout(new BorderLayout());

        // ===== BACKGROUND =====
        JPanel bg = new JPanel(){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                GradientPaint gp = new GradientPaint(
                        0,0,new Color(20,20,60),
                        getWidth(),getHeight(),new Color(120,0,150)
                );

                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };

        bg.setLayout(new BorderLayout());
        add(bg);

        // ===== TITLE =====
        JLabel title = new JLabel("CÀI ĐẶT", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));

        bg.add(title, BorderLayout.NORTH);

        // ===== CENTER PANEL =====
        JPanel center = new JPanel(new GridLayout(4,1,20,20));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(40,80,40,80));

        // 🎧 SOUND TOGGLE
        JToggleButton soundBtn = createToggle("🔊 Sound", main.soundOn);
        soundBtn.addActionListener(e -> {
            main.soundOn = soundBtn.isSelected();
            updateToggleText(soundBtn, "Sound");
        });

        // 🎵 MUSIC TOGGLE
        JToggleButton musicBtn = createToggle("🎵 Music", true);
        musicBtn.addActionListener(e -> {
            updateToggleText(musicBtn, "Music");
        });

        // ===== SLIDER SOUND =====
        JLabel lblSound = createLabel("Âm lượng Sound");

        JSlider soundSlider = new JSlider(0,100, main.soundVolume);
        soundSlider.setOpaque(false);
        soundSlider.addChangeListener(e -> {
            main.soundVolume = soundSlider.getValue();
        });

        // ===== SLIDER MUSIC =====
        JLabel lblMusic = createLabel("Âm lượng Music");

        JSlider musicSlider = new JSlider(0,100, main.musicVolume);
        musicSlider.setOpaque(false);
        musicSlider.addChangeListener(e -> {
            main.musicVolume = musicSlider.getValue();
        });

        center.add(soundBtn);
        center.add(lblSound);
        center.add(soundSlider);

        center.add(musicBtn);
        center.add(lblMusic);
        center.add(musicSlider);

        bg.add(center, BorderLayout.CENTER);

        // ===== BACK BUTTON =====
        JButton back = new JButton("← MENU");
        styleButton(back, new Color(255,120,0));

        back.addActionListener(e -> main.showScreen("MENU"));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(back);

        bg.add(bottom, BorderLayout.SOUTH);
    }

    // ===== LABEL =====
    JLabel createLabel(String text){
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("Arial", Font.BOLD, 16));
        return lbl;
    }

    // ===== TOGGLE BUTTON =====
    JToggleButton createToggle(String text, boolean on){
        JToggleButton btn = new JToggleButton();

        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(on ? new Color(0,180,0) : new Color(120,120,120));
        btn.setOpaque(true);

        btn.setSelected(on);
        updateToggleText(btn, text);

        btn.addChangeListener(e -> {
            if(btn.isSelected()){
                btn.setBackground(new Color(0,180,0));
            } else {
                btn.setBackground(new Color(120,120,120));
            }
        });

        return btn;
    }

    void updateToggleText(JToggleButton btn, String name){
        if(btn.isSelected()){
            btn.setText(name + " : ON");
        } else {
            btn.setText(name + " : OFF");
        }
    }

    // ===== STYLE BUTTON =====
    void styleButton(JButton btn, Color color){
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12,20,12,20));
    }
}