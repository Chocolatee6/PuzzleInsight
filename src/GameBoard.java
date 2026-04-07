import java.awt.*;
import java.awt.event.*;
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

    // ── Trạng thái drag ──
    private int     dragStartRow = -1;
    private int     dragStartCol = -1;
    private int     dragStartX   = 0;
    private int     dragStartY   = 0;
    private boolean dragFired    = false;   // đã trigger swap trong lần drag này chưa
    private boolean isAnimating  = false;

    /** Ngưỡng pixel tối thiểu để coi là kéo */
    private static final int DRAG_THRESHOLD = 15;

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
                addDragListeners(btn, row, col);

                gridPanel.add(btn);
            }
        }
    }

    // ─────────────────────────────────────────────
    //  Gắn MouseListener + MouseMotionListener (drag)
    // ─────────────────────────────────────────────
    private void addDragListeners(JButton btn, int row, int col) {
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isAnimating) return;
                // Bắt đầu drag tại ô này
                dragStartRow = row;
                dragStartCol = col;
                dragStartX   = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).x;
                dragStartY   = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).y;
                dragFired    = false;
                // Highlight ô đang giữ
                board[row][col].getButton().setBorder(
                        BorderFactory.createLineBorder(Color.WHITE, 3));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Xóa highlight
                if (dragStartRow >= 0 && dragStartCol >= 0) {
                    board[dragStartRow][dragStartCol].getButton()
                            .setBorder(UIManager.getBorder("Button.border"));
                }
                dragStartRow = -1;
                dragStartCol = -1;
                dragFired    = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (isAnimating || dragFired || dragStartRow < 0) return;

                int cx = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).x;
                int cy = SwingUtilities.convertPoint(btn, e.getPoint(), layeredPane).y;
                int dx = cx - dragStartX;
                int dy = cy - dragStartY;

                if (Math.abs(dx) < DRAG_THRESHOLD && Math.abs(dy) < DRAG_THRESHOLD) return;

                // Xác định hướng kéo chính
                int targetRow = dragStartRow;
                int targetCol = dragStartCol;
                if (Math.abs(dx) >= Math.abs(dy)) {
                    targetCol += (dx > 0) ? 1 : -1;   // ngang
                } else {
                    targetRow += (dy > 0) ? 1 : -1;   // dọc
                }

                // Kiểm tra hợp lệ
                if (targetRow < 0 || targetRow >= SIZE || targetCol < 0 || targetCol >= SIZE) return;
                if (!logic.checkKeNhau(dragStartRow, dragStartCol, targetRow, targetCol)) return;

                // Xóa highlight & trigger swap
                board[dragStartRow][dragStartCol].getButton()
                        .setBorder(UIManager.getBorder("Button.border"));
                dragFired   = true;
                isAnimating = true;
                animator.animateSwap(dragStartRow, dragStartCol, targetRow, targetCol);
            }
        };

        btn.addMouseListener(ma);
        btn.addMouseMotionListener(ma);
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

}

