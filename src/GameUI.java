import java.awt.*;
import javax.swing.*;

/**
 * GameUI – Cửa sổ chính, điều phối toàn bộ hệ thống màn chơi.
 */
public class GameUI extends JFrame {

    private static final int CELL_SIZE = 60;
    private static final int NUM_ICONS = 5;
    private static final int BOARD_PX = CELL_SIZE * 8; // 480

    // ── Level ──
    private int currentLevelIndex = 0;
    private int currentScore = 0;
    private boolean overlayShowing = false;

    // ── UI Components ──
    private final GameBoard gameBoard;
    private final GameHeader header;
    private final LevelOverlay overlay;

    // ── Quản lý chuyển màn hình ──
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private StartMenu startMenu;
    private final PauseOverlay pauseOverlay; 

    public GameUI() {
        setTitle("Puzzle Insight");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        ImageIcon[] icons = loadIcons();

        // ── Khởi tạo các components ──
        gameBoard = new GameBoard(icons);
        header = new GameHeader();
        overlay = new LevelOverlay();
        startMenu = new StartMenu();
        pauseOverlay = new PauseOverlay();

        // ── Kết nối Score ──
        gameBoard.setScoreUpdateListener(newScore -> {
            currentScore = newScore;
            header.setScore(newScore);
            checkWin(newScore);
        });

        // ── Kết nối Moves ──
        gameBoard.setMoveListener(new GameBoard.MoveListener() {
            @Override
            public void onMoveUsed(int movesLeft) {
                header.setMovesLeft(movesLeft);
            }

            @Override
            public void onMovesExhausted() {
                onLevelLose();
            }
        });

        // ── Timer hết → thua ──
        header.setTimerExpiredListener(this::onLevelLose);

        // ── XỬ LÝ SỰ KIỆN NÚT PAUSE ──
        header.setPauseListener(() -> {
            if (!overlayShowing) {
                header.stopTimer();
                pauseOverlay.setVisible(true);
            }
        });

        // ── XỬ LÝ SỰ KIỆN NÚT HELP ──
        header.setHelpListener(() -> {
            if (!overlayShowing) {
                // Tạm dừng timer khi đang xem hướng dẫn
                header.stopTimer(); 
                
                String helpMessage = "HƯỚNG DẪN CHƠI:\n\n"
                                   + "🍎 Kéo để đổi vị trí 2 ô kề nhau.\n"
                                   + "🍏 Tạo thành hàng ngang/dọc >= 3 ô để ghi điểm.\n"
                                   + "🍊 Nối 4 ô được 50 điểm, 5 ô được 80 điểm!\n\n"
                                   + "Chúc bạn chơi vui vẻ!";
                                   
                JOptionPane.showMessageDialog(this, helpMessage, "Trợ giúp", JOptionPane.INFORMATION_MESSAGE);
                
                // Xem xong thì chạy tiếp thời gian
                header.startTimer(); 
            }
        });

        // ── XỬ LÝ NÚT TRONG MÀN HÌNH PAUSE ──
        pauseOverlay.setPauseAction(new PauseOverlay.PauseAction() {
            @Override
            public void onResume() {
                pauseOverlay.setVisible(false); 
                header.startTimer();            
            }

            @Override
            public void onRestart() {
                pauseOverlay.setVisible(false);
                loadLevel(currentLevelIndex);   
            }

            @Override
            public void onHome() {
                pauseOverlay.setVisible(false);
                cardLayout.show(mainContainer, "MENU"); 
                startMenu.requestFocusInWindow();
            }
        });

        // ── ĐÓNG GÓI MÀN HÌNH GAME CHÍNH ──
        JLayeredPane boardLayer = new JLayeredPane();
        boardLayer.setPreferredSize(new Dimension(BOARD_PX, BOARD_PX));
        
        gameBoard.setBounds(0, 0, BOARD_PX, BOARD_PX);
        pauseOverlay.setBounds(0, 0, BOARD_PX, BOARD_PX); 
        overlay.setBounds(0, 0, BOARD_PX, BOARD_PX);
        
        boardLayer.add(gameBoard, JLayeredPane.DEFAULT_LAYER);
        boardLayer.add(pauseOverlay, JLayeredPane.PALETTE_LAYER); 
        boardLayer.add(overlay, JLayeredPane.POPUP_LAYER);        

        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.add(header, BorderLayout.NORTH);
        gamePanel.add(boardLayer, BorderLayout.CENTER);

        // ── THIẾT LẬP CARDLAYOUT ──
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        mainContainer.add(startMenu, "MENU");
        mainContainer.add(gamePanel, "GAME");

        startMenu.setMenuAction(new StartMenu.MenuAction() {
            @Override
            public void onPlay() {
                cardLayout.show(mainContainer, "GAME");
                loadLevel(0); 
            }

            @Override
            public void onExit() {
                System.exit(0); 
            }
        });

        setLayout(new BorderLayout());
        add(mainContainer, BorderLayout.CENTER);
        cardLayout.show(mainContainer, "MENU");

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        startMenu.requestFocusInWindow();
    }

    private void loadLevel(int index) {
        currentLevelIndex = index;
        currentScore = 0;
        overlayShowing = false;

        LevelConfig config = LevelConfig.generateLevel(index);
        gameBoard.initLevel(config);
        header.initLevel(config);
        header.startTimer();

        showLevelBanner(config.levelNumber);
    }

    private void checkWin(int newScore) {
        if (overlayShowing)
            return;
        LevelConfig config = LevelConfig.generateLevel(currentLevelIndex);
        if (newScore >= config.targetScore) {
            overlayShowing = true;
            header.stopTimer();

            overlay.showWin(newScore, config.targetScore, () -> {
                overlay.hideOverlay();
                loadLevel(currentLevelIndex + 1);
            });
        }
    }

    private void onLevelLose() {
        if (overlayShowing)
            return;
        overlayShowing = true;
        header.stopTimer();
        LevelConfig config = LevelConfig.generateLevel(currentLevelIndex);
    
        overlay.showLose(currentScore, config.targetScore, () -> {
            overlay.hideOverlay();
            loadLevel(currentLevelIndex); 
        });
    }

    private void showLevelBanner(int lvNum) {
        JLabel banner = new JLabel("LEVEL " + lvNum, SwingConstants.CENTER);
        banner.setFont(new Font("Arial", Font.BOLD, 28));
        banner.setForeground(new Color(255, 230, 100));
        banner.setOpaque(true);
        banner.setBackground(new Color(20, 5, 40, 230)); 
        banner.setBorder(BorderFactory.createLineBorder(new Color(160, 80, 255), 3));

        JLayeredPane lp = getLayeredPane();
        int bw = 200, bh = 50;
        int gx = (480 - bw) / 2;
        int gy = 100 + (480 - bh) / 2; 
        
        banner.setBounds(gx, gy, bw, bh);
        lp.add(banner, JLayeredPane.POPUP_LAYER);
        lp.repaint();

        Timer t = new Timer(1500, e -> {
            lp.remove(banner);
            lp.repaint();
        });
        t.setRepeats(false);
        t.start();
    }

    private ImageIcon[] loadIcons() {
        ImageIcon[] icons = new ImageIcon[NUM_ICONS];
        for (int i = 0; i < NUM_ICONS; i++) {
            String path = "images/characters_000" + (i + 1) + ".png";
            Image img = new ImageIcon(path)
                    .getImage()
                    .getScaledInstance(CELL_SIZE, CELL_SIZE, Image.SCALE_SMOOTH);
            icons[i] = new ImageIcon(img);
        }
        return icons;
    }
}