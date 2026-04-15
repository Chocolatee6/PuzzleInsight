import java.awt.*;
import java.util.prefs.Preferences;
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
    private final AIOverlay aiOverlay;

    // Quản lý lưu game
    private Preferences prefs; 
    private boolean isGameStarted = false; // Phân biệt lúc mới mở app và lúc đang chơi dở

    public GameUI() {
        setTitle("Puzzle Insight");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // ── ĐỌC DỮ LIỆU ĐÃ LƯU TỪ Ổ CỨNG ──
        prefs = Preferences.userNodeForPackage(GameUI.class);
        int savedLevel = prefs.getInt("savedLevel", -1); // -1 nghĩa là chưa từng chơi

        // Nếu có dữ liệu cũ, lập tức đưa level hiện tại về level đã lưu
        if (savedLevel != -1) {
            this.currentLevelIndex = savedLevel;
        }

        ImageIcon[] icons = loadIcons();

        // ── Khởi tạo các components ──
        gameBoard = new GameBoard(icons);
        header = new GameHeader();
        overlay = new LevelOverlay();
        startMenu = new StartMenu();
        if (savedLevel != -1) {
            startMenu.setResumeVisible(true); // Bật nút Resume ngay khi mở app!
        }
        pauseOverlay = new PauseOverlay();

        overlay.setHomeAction(() -> {
            overlay.hideOverlay();
            overlayShowing = false;
            cardLayout.show(mainContainer, "MENU"); // Chuyển về thẻ Menu chính
            startMenu.setResumeVisible(true);
            startMenu.requestFocusInWindow();
        });

        aiOverlay = new AIOverlay();
        aiOverlay.setVisible(false);
        aiOverlay.setListener(new AIOverlay.AISelectListener() {
            @Override
            public void onAlgorithmSelected(int algoIndex) {
                aiOverlay.setVisible(false); // Tắt overlay
                overlayShowing = false;      // Mở khóa UI
                gameBoard.startAutoPlay(algoIndex); // Ra lệnh AI đánh
                header.startTimer();         // Tiếp tục đồng hồ
            }

            @Override
            public void onCancel() {
                aiOverlay.setVisible(false);
                overlayShowing = false;
                header.startTimer();         // Cứ tiếp tục đồng hồ nếu hủy
            }
        });

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
                if (gameBoard.isAutoPlaying()) {
                    gameBoard.stopAutoPlay(); // Đang chơi thì tắt
                } else {
                    // Đang tắt thì bật bảng chọn (Dừng đồng hồ & Khóa UI)
                    header.stopTimer();
                    overlayShowing = true; 
                    aiOverlay.setVisible(true);
                }
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
                // Chuyển màn hình về MENU và bật nút Resume
                cardLayout.show(mainContainer, "MENU"); 
                startMenu.setResumeVisible(true);
                startMenu.requestFocusInWindow();
            }
        });

        // ── ĐÓNG GÓI MÀN HÌNH GAME CHÍNH ──
        JLayeredPane boardLayer = new JLayeredPane();
        boardLayer.setPreferredSize(new Dimension(BOARD_PX, BOARD_PX));
        
        gameBoard.setBounds(0, 0, BOARD_PX, BOARD_PX);
        pauseOverlay.setBounds(0, 0, BOARD_PX, BOARD_PX); 
        overlay.setBounds(0, 0, BOARD_PX, BOARD_PX);
        aiOverlay.setBounds(0,0,BOARD_PX,BOARD_PX);
        
        boardLayer.add(gameBoard, JLayeredPane.DEFAULT_LAYER);
        boardLayer.add(pauseOverlay, JLayeredPane.PALETTE_LAYER); 
        boardLayer.add(overlay, JLayeredPane.POPUP_LAYER);       
        boardLayer.add(aiOverlay,JLayeredPane.DRAG_LAYER); 

        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(new Color(240, 245, 250)); // Tô màu nền cho khoảng trống 2 bên
        
        gamePanel.add(header, BorderLayout.NORTH);
        
        // ── TẠO WRAPPER CĂN GIỮA BÀN CỜ ──
        JPanel centerWrapper = new JPanel(new GridBagLayout()); 
        centerWrapper.setOpaque(false); 
        centerWrapper.add(boardLayer);  // Gói bàn cờ vào giữa wrapper
        
        // ── THÊM WRAPPER VÀO GAME PANEL THAY VÌ THÊM TRỰC TIẾP ──
        gamePanel.add(centerWrapper, BorderLayout.CENTER);

        // ── THIẾT LẬP CARDLAYOUT ──
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        mainContainer.add(startMenu, "MENU");
        mainContainer.add(gamePanel, "GAME");

        // ── THIẾT LẬP CÁC NÚT TRÊN START MENU ──
        startMenu.setMenuAction(new StartMenu.MenuAction() {
            @Override
            public void onNewGame() {
                isGameStarted = true; // Đánh dấu đã vào game
                cardLayout.show(mainContainer, "GAME");
                loadLevel(0); // Load level 0 (Hàm loadLevel sẽ tự động lưu game mới)
                startMenu.setResumeVisible(true);
            }

            @Override
            public void onResume() {
                cardLayout.show(mainContainer, "GAME");
                
                // NẾU VỪA MỞ APP: Game chưa được nạp, ta phải gọi loadLevel
                if (!isGameStarted) {
                    loadLevel(currentLevelIndex);
                    isGameStarted = true;
                }
                
                // Tắt Pause và chạy tiếp thời gian
                if (pauseOverlay.isVisible()) {
                    pauseOverlay.setVisible(false);
                    header.startTimer();
                } else if (!overlayShowing) {
                    header.startTimer();
                }
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
        prefs.putInt("savedLevel", currentLevelIndex);

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
            gameBoard.stopAutoPlay();

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

        gameBoard.stopAutoPlay();

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
        int gx = (getWidth() - bw) / 2;
        int gy = (getHeight() - bh) / 2;
        
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