import java.awt.*;
import java.util.Random;
import javax.swing.*;

/**
 * GameBoard = bàn cờ 8x8.
 * Quản lý:
 *  - Khởi tạo các ô (BoardCell)
 *  - Xử lý sự kiện click (handleClick)
 *  - Phối hợp GameLogic và AnimationManager
 */
public class GameBoard extends JPanel {

    private static final int SIZE      = 8;
    private static final int CELL_SIZE = 60;

    // ── Dữ liệu bàn cờ ──
    private final BoardCell[][] board  = new BoardCell[SIZE][SIZE];
    private final ImageIcon[]   icons;
    private final Random        rd     = new Random();

    // ── Trạng thái lựa chọn ──
    private int  selectedRow = -1;
    private int  selectedCol = -1;
    private boolean isAnimating = false;

    // ── Thành phần con ──
    private final JLayeredPane    layeredPane = new JLayeredPane();
    private final GameLogic       logic;
    private final AnimationManager animator;

    // ── Callback cập nhật điểm lên GameUI ──
    public interface ScoreUpdateListener {
        void onScoreChanged(int newScore);
    }

    private ScoreUpdateListener scoreUpdateListener;

    public GameBoard(ImageIcon[] icons) {
        this.icons = icons;

        int boardPx = CELL_SIZE * SIZE;

        // Setup layeredPane
        layeredPane.setLayout(null);
        layeredPane.setPreferredSize(new Dimension(boardPx, boardPx));

        // Panel nền gradient
        JPanel gridPanel = new GradientPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));
        gridPanel.setBounds(0, 0, boardPx, boardPx);

        // Khởi tạo các ô
        initCells(gridPanel);

        layeredPane.add(gridPanel, JLayeredPane.DEFAULT_LAYER);

        // Setup logic & animator
        logic    = new GameLogic(board, icons);
        animator = new AnimationManager(board, icons, layeredPane, logic);

        logic.setScoreListener(newScore -> {
            if (scoreUpdateListener != null)
                scoreUpdateListener.onScoreChanged(newScore);
        });

        animator.setAnimationCallback(() -> isAnimating = false);

        // Bọc layeredPane vào center
        setLayout(new GridBagLayout());
        add(layeredPane);
    }

    public void setScoreUpdateListener(ScoreUpdateListener listener) {
        this.scoreUpdateListener = listener;
    }

    // ─────────────────────────────────────────────
    //  Khởi tạo từng ô (không cho match ngay từ đầu)
    // ─────────────────────────────────────────────
    private void initCells(JPanel gridPanel) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setContentAreaFilled(false);

                int candy;
                do {
                    candy = rd.nextInt(5);
                } while (hasEarlyMatch(i, j, candy));

                board[i][j] = new BoardCell(candy, btn);
                btn.setIcon(icons[candy]);

                final int row = i, col = j;
                btn.addActionListener(e -> handleClick(row, col));

                gridPanel.add(btn);
            }
        }
    }

    /** Kiểm tra nếu đặt candy tại (r,c) có tạo match 3 ngay khi init không */
    private boolean hasEarlyMatch(int r, int c, int candy) {
        if (r >= 2
                && board[r - 1][c] != null && board[r - 1][c].getType() == candy
                && board[r - 2][c] != null && board[r - 2][c].getType() == candy)
            return true;
        if (c >= 2
                && board[r][c - 1] != null && board[r][c - 1].getType() == candy
                && board[r][c - 2] != null && board[r][c - 2].getType() == candy)
            return true;
        return false;
    }

    // ─────────────────────────────────────────────
    //  Xử lý click vào ô (r, c)
    // ─────────────────────────────────────────────
    public void handleClick(int r, int c) {
        if (isAnimating) return;

        if (selectedRow == -1) {
            // Chọn ô đầu tiên
            selectedRow = r;
            selectedCol = c;
            board[r][c].getButton().setBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 3));
        } else {
            // Bỏ highlight ô trước
            board[selectedRow][selectedCol].getButton()
                    .setBorder(UIManager.getBorder("Button.border"));

            if (logic.checkKeNhau(selectedRow, selectedCol, r, c)) {
                isAnimating = true;
                animator.animateSwap(selectedRow, selectedCol, r, c);
            }

            selectedRow = -1;
            selectedCol = -1;
        }
    }
}
