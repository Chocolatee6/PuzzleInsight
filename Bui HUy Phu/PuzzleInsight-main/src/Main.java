import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {

    CardLayout cardLayout;
    JPanel mainPanel;
    GameUI game;
    public boolean soundOn = true;
    public int soundVolume = 80;
    public int musicVolume = 80;
    public Main() {
        setTitle("Puzzle Game");
        setSize(500,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // tạo các màn hình
        MenuPanel menu = new MenuPanel(this);
        game = new GameUI(this);
        MapPanel map = new MapPanel(this);
        SettingPanel setting = new SettingPanel(this);
        GuidePanel guide = new GuidePanel(this);

        // add vào hệ thống
        mainPanel.add(menu, "MENU");
        mainPanel.add(game, "GAME");
        mainPanel.add(map, "MAP");
        mainPanel.add(setting, "SETTING");
        mainPanel.add(guide, "GUIDE");

        add(mainPanel);

        cardLayout.show(mainPanel, "MENU"); // mở menu

        setVisible(true);
    }

    // hàm chuyển màn hình
    public void showScreen(String name){ 
        if(name.equals("GAME")){
        game.restartGame();   // 👈 reset mỗi lần vào game
        }
        cardLayout.show(mainPanel, name);
    }

    public static void main(String[] args) {
        new Main();
    }
   
}