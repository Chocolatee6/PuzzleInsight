import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.*;

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

    // ── Hệ thống gợi ý AI (Idle Hint) ──
    private Timer idleTimer;
    private Timer hintBlinkTimer;
    private GameLogic.Move hintMove = null;
    private boolean hintVisible = false;
    private static final int IDLE_SECONDS = 5;   // giây chờ trước khi gợi ý
    private static final int BLINK_DELAY  = 400; // ms mỗi lần nhấp nháy
    private boolean isAutoPlaying = false;
    private int currentAlgorithm = 2;
    
    public boolean  isAutoPlaying(){
        return isAutoPlaying;
    }


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
            }else if(isAutoPlaying){
                Timer delayTimer = new Timer(1500,e->makeAIMove());
                delayTimer.setRepeats(false);
                delayTimer.start();
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

        // Khởi động idle timer
        startIdleTimer();
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
        isAutoPlaying = false;

        movesLeft = config.maxMoves;
        logic.resetScore();
        animator.resetCancel();
        clearHint();
        resetBoard();
        restartIdleTimer();
    }

    // ── Idle Hint System ──────────────────────────────────────────────────────

    /** Bắt đầu idle timer lần đầu */
    private void startIdleTimer() {
        idleTimer = new Timer(IDLE_SECONDS * 1000, e -> showHint());
        idleTimer.setRepeats(false);
        idleTimer.start();
    }

    /** Reset idle timer về 0 (gọi mỗi khi người chơi tương tác) */
    private void restartIdleTimer() {
        clearHint();
        if (idleTimer != null) idleTimer.stop();
        idleTimer = new Timer(IDLE_SECONDS * 1000, e -> showHint());
        idleTimer.setRepeats(false);
        idleTimer.start();
    }

    /** Tìm nước đi gợi ý bằng BFS và bật hiệu ứng nhấp nháy */
    private void showHint() {
        if (isAnimating) {
            restartIdleTimer(); // Đang animation thì đợi thêm
            return;
        }
        hintMove = logic.BFS();
        if (hintMove == null) return; // Không có nước nào hợp lệ

        hintVisible = false;
        hintBlinkTimer = new Timer(BLINK_DELAY, e -> {
            hintVisible = !hintVisible;
            applyHintBorder(hintMove, hintVisible);
        });
        hintBlinkTimer.start();
    }

    // BAT AI CUNG THUAT TOAN DUOC CHON
    public void startAutoPlay(int algoType)
    {
        this.currentAlgorithm = algoType;
        this.isAutoPlaying = true;
        System.out.println("Da bat AI");
        if(!isAnimating) makeAIMove();
    }


    /** Áp dụng / xóa viền nhấp nháy cho 2 ô gợi ý */
    private void applyHintBorder(GameLogic.Move m, boolean show) {
    JButton btn1 = board[m.r1][m.c1].getButton();
    JButton btn2 = board[m.r2][m.c2].getButton();

    if (show) {
        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 220, 0), 3),
                BorderFactory.createLineBorder(new Color(255, 255, 180), 1));
        
        btn1.setBorder(border);
        btn2.setBorder(border);
        // PHẢI BẬT LÊN THÌ VIỀN MỚI HIỂN THỊ
        btn1.setBorderPainted(true);
        btn2.setBorderPainted(true); 
    } else {
        btn1.setBorder(null);
        btn2.setBorder(null);
        // TẮT ĐI KHI KHÔNG CẦN GỢI Ý NỮA
        btn1.setBorderPainted(false);
        btn2.setBorderPainted(false);
    }
}

    /** Tắt gợi ý và xóa viền */
    private void clearHint() {
        if (hintBlinkTimer != null) hintBlinkTimer.stop();
        hintBlinkTimer = null;
        if (hintMove != null) {
            applyHintBorder(hintMove, false);
            hintMove = null;
        }
        hintVisible = false;
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

                // Người chơi tương tác → reset idle timer
                restartIdleTimer();

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

    

    

    private void makeAIMove() {
        if (!isAutoPlaying) return; // Nếu đã tắt thì không làm gì cả
        
        GameLogic.Move bestMove = null;
        
        switch (currentAlgorithm) {
            case 0:
                bestMove = logic.BFS();
                break;
            case 1:
                bestMove = logic.DFS();
                break;
            case 2:
                bestMove = logic.BestFirstSearch();
                break;
            case 3:
                bestMove = logic.AStar();
                break;
        }
        if(bestMove!=null){
            isAnimating = true;
            clearHint();
            restartIdleTimer();
            animator.animateSwap(bestMove.r1, bestMove.c1, bestMove.r2, bestMove.c2);
        }else{
            System.out.println("AI: Bàn cờ không còn nước đi! Tắt Auto-play.");
            isAutoPlaying = false;
        }
        
    }

    public void stopAutoPlay(){
        isAutoPlaying = false;
        System.out.println("Đã TẮT AI");
    }
}