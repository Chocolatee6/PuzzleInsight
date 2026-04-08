import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * GameBoard = bàn cờ 8×8.
 * Quản lý:
 * - Khởi tạo các ô (BoardCell)
 * - Xử lý sự kiện drag
 * - Phối hợp GameLogic và AnimationManager
 * - Đếm số bước di chuyển
 */
public class GameBoard extends JPanel {

    private static final int SIZE = 8;
    private static final int CELL_SIZE = 60;

    // ── Dữ liệu bàn cờ ──
    private final BoardCell[][] board = new BoardCell[SIZE][SIZE];
    private final ImageIcon[] icons;
    private final Random rd = new Random();

    // ── Trạng thái drag ──
    private int dragStartRow = -1;
    private int dragStartCol = -1;
    private int dragStartX = 0;
    private int dragStartY = 0;
    private boolean dragFired = false;
    private boolean isAnimating = false;
    private static final int DRAG_THRESHOLD = 15;

    // ── Level / Moves ──
    private int movesLeft = 25;
    private boolean pendingExhausted = false;

    // ── Thành phần con ──
    private final JLayeredPane layeredPane = new JLayeredPane();
    private final GameLogic logic;
    private final AnimationManager animator;

    // ── Listeners ──
    public interface ScoreUpdateListener {
        void onScoreChanged(int newScore);
    }

    private ScoreUpdateListener scoreUpdateListener;

    public interface MoveListener {
        void onMoveUsed(int movesLeft);

        void onMovesExhausted();
    }

    private MoveListener moveListener;

    public GameBoard(ImageIcon[] icons) {
        this.icons = icons;
        int boardPx = CELL_SIZE * SIZE;

        layeredPane.setLayout(null);
        layeredPane.setPreferredSize(new Dimension(boardPx, boardPx));

        JPanel gridPanel = new GradientPanel();
        gridPanel.setLayout(new GridLayout(SIZE, SIZE));
        gridPanel.setBounds(0, 0, boardPx, boardPx);

        initCells(gridPanel);

        layeredPane.add(gridPanel, JLayeredPane.DEFAULT_LAYER);

        logic = new GameLogic(board, icons);
        animator = new AnimationManager(board, icons, layeredPane, logic);

        logic.setScoreListener(newScore -> {
            if (scoreUpdateListener != null)
                scoreUpdateListener.onScoreChanged(newScore);
        });

        animator.setAnimationCallback(() -> {
            isAnimating = false;
            if (pendingExhausted) {
                pendingExhausted = false;
                if (moveListener != null)
                    moveListener.onMovesExhausted();
            }
        });

        // Chỉ trừ bước khi swap tạo được match (swap-back không tốn bước)
        animator.setMovePerformedCallback(() -> {
            movesLeft--; // Mỗi lần thực hiện kéo/đổi chỗ sẽ trừ 1 bước
            if (moveListener != null)
            moveListener.onMoveUsed(movesLeft);
    
            if (movesLeft <= 0)
            pendingExhausted = true;
        });

        setLayout(new GridBagLayout());
        add(layeredPane);
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public void setScoreUpdateListener(ScoreUpdateListener l) {
        this.scoreUpdateListener = l;
    }

    public void setMoveListener(MoveListener l) {
        this.moveListener = l;
    }

    /**
     * Reset bàn cờ cho màn mới: huỷ animation cũ, reset điểm + bước, lấy bàn mới.
     */
    public void initLevel(LevelConfig config) {
        animator.cancel(); // dừng animation đang chạy
        isAnimating = false;
        pendingExhausted = false;
        movesLeft = config.maxMoves;
        logic.resetScore();
        animator.resetCancel(); // cho phép animation mới
        resetBoard();
    }

    // ── Nội bộ ───────────────────────────────────────────────────────────────

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
                addDragListeners(btn, row, col);
                gridPanel.add(btn);
            }
        }
    }

    /** Xáo lại toàn bàn cờ mà không tạo lại JButton */
    private void resetBoard() {
        // Xoá hết
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                board[i][j].setType(-1);
                board[i][j].getButton().setIcon(null);
            }
        // Xáo ngẫu nhiên, tránh match sẵn
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                int candy;
                do {
                    candy = rd.nextInt(5);
                } while (hasEarlyMatch(i, j, candy));
                board[i][j].setType(candy);
                board[i][j].getButton().setIcon(icons[candy]);
            }
    }

    private void addDragListeners(JButton btn, int row, int col) {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isAnimating)
                    return;
                dragStartRow = row;
                dragStartCol = col;
                dragStartX = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).x;
                dragStartY = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).y;
                dragFired = false;
                board[row][col].getButton().setBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 3));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragStartRow >= 0 && dragStartCol >= 0)
                    board[dragStartRow][dragStartCol].getButton()
                            .setBorder(UIManager.getBorder("Button.border"));
                dragStartRow = -1;
                dragStartCol = -1;
                dragFired = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isAnimating || dragFired || dragStartRow < 0)
                    return;

                int cx = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).x;
                int cy = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).y;
                int dx = cx - dragStartX;
                int dy = cy - dragStartY;

                if (Math.abs(dx) < DRAG_THRESHOLD && Math.abs(dy) < DRAG_THRESHOLD)
                    return;

                int targetRow = dragStartRow;
                int targetCol = dragStartCol;
                if (Math.abs(dx) >= Math.abs(dy))
                    targetCol += (dx > 0) ? 1 : -1;
                else
                    targetRow += (dy > 0) ? 1 : -1;

                if (targetRow < 0 || targetRow >= SIZE || targetCol < 0 || targetCol >= SIZE)
                    return;
                if (!logic.checkKeNhau(dragStartRow, dragStartCol, targetRow, targetCol))
                    return;

                board[dragStartRow][dragStartCol].getButton()
                        .setBorder(UIManager.getBorder("Button.border"));
                dragFired = true;
                isAnimating = true;

                animator.animateSwap(dragStartRow, dragStartCol, targetRow, targetCol);
            }
        };
        btn.addMouseListener(ma);
        btn.addMouseMotionListener(ma);
    }

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

}