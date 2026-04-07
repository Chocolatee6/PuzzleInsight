import javax.swing.*;
import java.awt.*;

public class MenuPanel extends JPanel {

    public MenuPanel(Main main){

        setLayout(new BorderLayout());

        // 🎨 nền gradient
        JPanel background = new JPanel(){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                GradientPaint gp = new GradientPaint(
                        0,0,new Color(80,0,120),
                        getWidth(),getHeight(),new Color(0,150,200)
                );

                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };

        background.setLayout(new GridBagLayout());
        add(background);

        // 📦 box chứa menu
        JPanel box = new JPanel(new GridLayout(6,1,15,15));
        box.setOpaque(false);

        // 🎮 TITLE
        JLabel title = new JLabel("PUZZLE GAME");
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // 🎮 BUTTONS
        JButton btnPlay = createGameButton("▶ PLAY");
        JButton btnMap = createGameButton("🗺 MAPS");
        JButton btnSetting = createGameButton("⚙ SETTINGS");
        JButton btnGuide = createGameButton("❓ GUIDE");
        JButton btnExit = createGameButton("✖ EXIT");

        // 👉 sự kiện
        btnPlay.addActionListener(e -> main.showScreen("GAME"));
        btnMap.addActionListener(e -> main.showScreen("MAP"));
        btnSetting.addActionListener(e -> main.showScreen("SETTING"));
        btnGuide.addActionListener(e -> main.showScreen("GUIDE"));
        btnExit.addActionListener(e -> System.exit(0));

        // add vào box
        box.add(title);
        box.add(btnPlay);
        box.add(btnMap);
        box.add(btnSetting);
        box.add(btnGuide);
        box.add(btnExit);

        background.add(box);
    }

    // 🎮 BUTTON STYLE
    JButton createGameButton(String text){
        JButton btn = new JButton(text);

        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(255, 120, 0));
        btn.setBorder(BorderFactory.createEmptyBorder(12,25,12,25));

        // ✨ hover + click effect
        btn.addMouseListener(new java.awt.event.MouseAdapter(){

            public void mouseEntered(java.awt.event.MouseEvent e){
                btn.setBackground(new Color(255,160,50));
            }

            public void mouseExited(java.awt.event.MouseEvent e){
                btn.setBackground(new Color(255,120,0));
            }

            public void mousePressed(java.awt.event.MouseEvent e){
                btn.setLocation(btn.getX(), btn.getY()+2); // nhún xuống
            }

            public void mouseReleased(java.awt.event.MouseEvent e){
                btn.setLocation(btn.getX(), btn.getY()-2); // bật lên
            }
        });

        return btn;
    }
}