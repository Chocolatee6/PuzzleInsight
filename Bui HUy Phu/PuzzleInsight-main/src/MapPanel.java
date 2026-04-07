import javax.swing.*;
import java.awt.*;

public class MapPanel extends JPanel {

    Main main;

    public MapPanel(Main main){
        this.main = main;

        setLayout(new BorderLayout());

        // ===== BACKGROUND PANEL =====
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
        JLabel title = new JLabel("CHỌN MAP", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));

        bg.add(title, BorderLayout.NORTH);

        // ===== MAP LIST =====
        JPanel mapPanel = new JPanel(new GridLayout(2,2,20,20));
        mapPanel.setOpaque(false);
        mapPanel.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));

        // tạo các map
        mapPanel.add(createMapButton("Map 1", true));
        mapPanel.add(createMapButton("Map 2", true));
        mapPanel.add(createMapButton("Map 3", false)); // lock
        mapPanel.add(createMapButton("Map 4", false)); // lock

        bg.add(mapPanel, BorderLayout.CENTER);

        // ===== BACK BUTTON =====
        JButton back = new JButton("← MENU");
        styleButton(back, new Color(255,120,0));

        back.addActionListener(e -> main.showScreen("MENU"));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(back);

        bg.add(bottom, BorderLayout.SOUTH);
    }

    // ===== TẠO NÚT MAP =====
    JButton createMapButton(String text, boolean unlocked){

        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);

        if(unlocked){
            styleButton(btn, new Color(0,180,0));

            btn.addActionListener(e -> {
                main.showScreen("GAME");
            });

        } else {
            styleButton(btn, new Color(100,100,100));
            btn.setText(text + " 🔒");
            btn.setEnabled(false);
        }

        // hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseEntered(java.awt.event.MouseEvent evt){
                if(unlocked) btn.setBackground(btn.getBackground().brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt){
                if(unlocked) btn.setBackground(new Color(0,180,0));
            }
        });

        return btn;
    }

    // ===== STYLE BUTTON =====
    void styleButton(JButton btn, Color color){
        btn.setBackground(color);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(15,10,15,10));
    }
}