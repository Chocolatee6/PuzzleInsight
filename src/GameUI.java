import java.awt.*;
import javax.swing.*;

/**
 * GameUI – Cửa sổ chính, điều phối toàn bộ hệ thống màn chơi.
 *
 * Luồng:
 * loadLevel(i) → GameBoard.initLevel + GameHeader.initLevel + startTimer
 * Score >= target → onLevelWin()
 * Moves exhausted → onLevelLose()
 * Timer expired → onLevelLose()
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

    public GameUI() {
        setTitle("Puzzle Insight");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        ImageIcon[] icons = loadIcons();

        // ── Tạo components ──
        gameBoard = new GameBoard(icons);
        header = new GameHeader();
        overlay = new LevelOverlay();

        // ── Kết nối Score → Header + kiểm tra win ──
        gameBoard.setScoreUpdateListener(newScore -> {
            currentScore = newScore;
            header.setScore(newScore);
            checkWin(newScore);
        });

        // ── Kết nối Moves → Header + kiểm tra lose ──
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

        // ── BoardLayer: board + overlay chồng nhau ──
        JLayeredPane boardLayer = new JLayeredPane();
        boardLayer.setPreferredSize(new Dimension(BOARD_PX, BOARD_PX));
        gameBoard.setBounds(0, 0, BOARD_PX, BOARD_PX);
        overlay.setBounds(0, 0, BOARD_PX, BOARD_PX);
        boardLayer.add(gameBoard, JLayeredPane.DEFAULT_LAYER);
        boardLayer.add(overlay, JLayeredPane.POPUP_LAYER);

        // ── Layout chính ──
        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(boardLayer, BorderLayout.CENTER);

        // ── Load màn đầu ──
        loadLevel(0);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Level Management ─────────────────────────────────────────────────────

    private void loadLevel(int index) {
        currentLevelIndex = index;
        currentScore = 0;
        overlayShowing = false;

        LevelConfig config = LevelConfig.generateLevel(index);
        gameBoard.initLevel(config);
        header.initLevel(config);
        header.startTimer();

        // Hiển thị thông báo nhỏ màn mới
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
        loadLevel(currentLevelIndex); // Thử lại màn hiện tại
        });
    }

    /** Hiện banner "Level X" nhỏ trong 1.2 giây (dùng JLabel tạm) */
    /** Hiện banner "Level X" nhỏ trong 1.5 giây */
    private void showLevelBanner(int lvNum) {
        JLabel banner = new JLabel("LEVEL " + lvNum, SwingConstants.CENTER);
        banner.setFont(new Font("Arial", Font.BOLD, 28));
        banner.setForeground(new Color(255, 230, 100));
        banner.setOpaque(true);
        banner.setBackground(new Color(20, 5, 40, 230)); // Nền tối trong suốt
        banner.setBorder(BorderFactory.createLineBorder(new Color(160, 80, 255), 3));

        // Dùng LayeredPane của JFrame thay vì GlassPane để tránh lỗi đồ họa
        JLayeredPane lp = getLayeredPane();
        
        int bw = 200, bh = 50;
        // Cố định toạ độ dựa trên kích thước bảng (BOARD_PX = 480, Header = 100)
        // Để banner luôn nằm giữa bàn cờ dù cửa sổ chưa pack() xong
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

    // ── Helpers ──────────────────────────────────────────────────────────────

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

    
    

    
    

      
            
            