import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * GameBoard – Quản lý bàn cờ 8x8, xử lý kéo thả và phối hợp các thành phần logic.
 */
public class GameBoard extends JPanel {

    private static final int SIZE = 8;
    private static final int CELL_SIZE = 60;

    // ── Dữ liệu bàn cờ ──
    private final BoardCell[][] board = new BoardCell[SIZE][SIZE];
    private final ImageIcon[] icons;
    private final Random rd = new Random();

    // ── Trạng thái kéo thả (Drag) ──
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

    private Timer idleTimer;
    private boolean isAutoPlaying = false;
    private int currentAIMode = 0;
    

    


    // ── Giao diện lắng nghe (Listeners) – Quan trọng để kết nối với GameUI ──
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

        // Cập nhật điểm lên UI khi có thay đổi từ logic
        logic.setScoreListener(newScore -> {
            if (scoreUpdateListener != null)
                scoreUpdateListener.onScoreChanged(newScore);
        });

        // Xử lý sau khi kết thúc hiệu ứng (nổ kẹo, rơi kẹo)
        animator.setAnimationCallback(() -> {
            isAnimating = false;
            if (pendingExhausted) {
                pendingExhausted = false;
                if (moveListener != null)
                    moveListener.onMovesExhausted();
            }
        });

        // Trừ bước đi khi người chơi thực hiện đổi chỗ thành công
        animator.setMovePerformedCallback(() -> {
            movesLeft--;
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

    /** Reset bàn cờ cho màn chơi mới */
    public void initLevel(LevelConfig config) {
        animator.cancel(); 
        isAnimating = false;
        pendingExhausted = false;
        movesLeft = config.maxMoves;
        logic.resetScore();
        animator.resetCancel(); 
        resetBoard();
    }

    // ── Nội bộ (Private) ──────────────────────────────────────────────────────

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

    /** Xáo lại bàn cờ khi bắt đầu màn mới hoặc khi bí nước */
    private void resetBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j].setType(-1);
                board[i][j].getButton().setIcon(null);
            }
        }
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                int candy;
                do {
                    candy = rd.nextInt(5);
                } while (hasEarlyMatch(i, j, candy));
                board[i][j].setType(candy);
                board[i][j].getButton().setIcon(icons[candy]);
            }
        }
    }

    private void addDragListeners(JButton btn, int row, int col) {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isAnimating) return;
                
                dragStartRow = row;
                dragStartCol = col;
                dragStartX = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).x;
                dragStartY = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).y;
                dragFired = false;
                
                // Hiệu ứng viền khi chọn kẹo
                board[row][col].getButton().setBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 3));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragStartRow >= 0 && dragStartCol >= 0)
                    board[dragStartRow][dragStartCol].getButton().setBorder(null);
                dragStartRow = -1;
                dragStartCol = -1;
                dragFired = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isAnimating || dragFired || dragStartRow < 0) return;

                int cx = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).x;
                int cy = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).y;
                int dx = cx - dragStartX;
                int dy = cy - dragStartY;

                if (Math.abs(dx) < DRAG_THRESHOLD && Math.abs(dy) < DRAG_THRESHOLD) return;

                int targetRow = dragStartRow;
                int targetCol = dragStartCol;
                
                if (Math.abs(dx) >= Math.abs(dy))
                    targetCol += (dx > 0) ? 1 : -1;
                else
                    targetRow += (dy > 0) ? 1 : -1;

                if (targetRow < 0 || targetRow >= SIZE || targetCol < 0 || targetCol >= SIZE) return;
                if (!logic.checkKeNhau(dragStartRow, dragStartCol, targetRow, targetCol)) return;

                board[dragStartRow][dragStartCol].getButton().setBorder(null);
                dragFired = true;
                isAnimating = true;

                // Thực hiện đổi chỗ
                animator.animateSwap(dragStartRow, dragStartCol, targetRow, targetCol);
            }
        };
        btn.addMouseListener(ma);
        btn.addMouseMotionListener(ma);
    }

    /** Ngăn chặn việc khởi tạo bàn cờ có sẵn các tổ hợp ăn điểm */
    private boolean hasEarlyMatch(int r, int c, int candy) {
        if (r >= 2 && board[r - 1][c] != null && board[r - 1][c].getType() == candy
                && board[r - 2][c] != null && board[r - 2][c].getType() == candy)
            return true;
        if (c >= 2 && board[r][c - 1] != null && board[r][c - 1].getType() == candy
                && board[r][c - 2] != null && board[r][c - 2].getType() == candy)
            return true;
        return false;
    }
}