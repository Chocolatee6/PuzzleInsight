import java.awt.*;
import javax.swing.*;

/**
 * GameUI = cửa sổ chính (JFrame).
 * Chỉ chịu trách nhiệm:
 *  - Tạo layout (header + board)
 *  - Tải icon
 *  - Nhận callback điểm từ GameBoard và cập nhật JLabel
 */
public class GameUI extends JFrame {

    private static final int CELL_SIZE = 60;
    private static final int NUM_ICONS = 5;

    private final JLabel scoreLabel = new JLabel("Score: 0", SwingConstants.CENTER);

    public GameUI() {
        setTitle("Puzzle Insight");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Tải icon nhân vật
        ImageIcon[] icons = loadIcons();

        // Tạo bàn cờ
        GameBoard gameBoard = new GameBoard(icons);

        // Đăng ký callback cập nhật điểm
        gameBoard.setScoreUpdateListener(
                newScore -> scoreLabel.setText("Score: " + newScore));

        // ── Header ──
        JPanel headerPanel = buildHeader();

        // ── Layout frame ──
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(gameBoard,   BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ─────────────────────────────────────────────
    //  Tải và scale icon nhân vật
    // ─────────────────────────────────────────────
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

    // ─────────────────────────────────────────────
    //  Panel header: tiêu đề + điểm số
    // ─────────────────────────────────────────────
    private JPanel buildHeader() {
        JLabel titleLabel = new JLabel("PUZZLE INSIGHT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(150, 50, 200));

        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(scoreLabel, BorderLayout.SOUTH);
        return panel;
    }
}
