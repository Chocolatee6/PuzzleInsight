import java.awt.*;
import javax.swing.*;

/**
 * AnimationManager chịu trách nhiệm toàn bộ hiệu ứng trượt:
 *  - animateSwap     : trượt hai ô về phía nhau
 *  - animateSwapBack : trượt ngược lại nếu không match
 *  - animateDestroy  : chờ rồi gọi gravity
 *  - animateGravity  : kéo ô xuống từng bước
 */
public class AnimationManager {

    private static final int SWAP_DELAY   = 16;   // ms mỗi frame swap
    private static final int SWAP_STEPS   = 12;   // số frame swap
    private static final int DESTROY_WAIT = 400;  // ms trước khi gravity
    private static final int GRAVITY_DELAY = 80;  // ms mỗi frame rơi

    private final BoardCell[][]  board;
    private final ImageIcon[]    icons;
    private final JLayeredPane   layeredPane;
    private final GameLogic      logic;

    /** Callback báo animation kết thúc (khóa/mở click) */
    public interface AnimationCallback {
        void onAnimationDone();
    }

    private AnimationCallback doneCallback;
    private volatile boolean cancelled = false;

    /** Callback báo GameBoard rằng swap tạo match → mới trừ bước */
    public interface MovePerformedCallback { void onMovePerformed(); }
    private MovePerformedCallback movePerformedCallback;

    public void setMovePerformedCallback(MovePerformedCallback cb) { 
    this.movePerformedCallback = cb; 
}
    public void cancel()      { cancelled = true;  }
    public void resetCancel() { cancelled = false; }

    public AnimationManager(BoardCell[][] board,
                            ImageIcon[]   icons,
                            JLayeredPane  layeredPane,
                            GameLogic     logic) {
        this.board       = board;
        this.icons       = icons;
        this.layeredPane = layeredPane;
        this.logic       = logic;
    }

    public void setAnimationCallback(AnimationCallback cb) {
        this.doneCallback = cb;
    }

    // ─────────────────────────────────────────────
    //  Trượt hai ô về phía nhau
    // ─────────────────────────────────────────────
    public void animateSwap(int r1, int c1, int r2, int c2) {
        JButton b1 = board[r1][c1].getButton();
        JButton b2 = board[r2][c2].getButton();

        int type1 = board[r1][c1].getType();
        int type2 = board[r2][c2].getType();

        Point p1 = SwingUtilities.convertPoint(b1.getParent(), b1.getLocation(), layeredPane);
        Point p2 = SwingUtilities.convertPoint(b2.getParent(), b2.getLocation(), layeredPane);

        JLabel l1 = makeLabel(icons[type1], p1, b1.getWidth(), b1.getHeight());
        JLabel l2 = makeLabel(icons[type2], p2, b2.getWidth(), b2.getHeight());

        layeredPane.add(l1, JLayeredPane.DRAG_LAYER);
        layeredPane.add(l2, JLayeredPane.DRAG_LAYER);

        b1.setIcon(null);
        b2.setIcon(null);

        float dx1 = (float)(p2.x - p1.x) / SWAP_STEPS;
        float dy1 = (float)(p2.y - p1.y) / SWAP_STEPS;
        float dx2 = (float)(p1.x - p2.x) / SWAP_STEPS;
        float dy2 = (float)(p1.y - p2.y) / SWAP_STEPS;

        final float[] x1 = {p1.x}, y1 = {p1.y};
        final float[] x2 = {p2.x}, y2 = {p2.y};
        final int[]   count = {0};

        Timer timer = new Timer(SWAP_DELAY, null);
        timer.addActionListener(e -> {
            if (cancelled) { timer.stop(); removeLabels(l1, l2); b1.setIcon(icons[type1]); b2.setIcon(icons[type2]); return; }
            count[0]++;

            if (count[0] >= SWAP_STEPS) {
                l1.setLocation(p2.x, p2.y);
                l2.setLocation(p1.x, p1.y);
            } else {
                x1[0] += dx1; y1[0] += dy1;
                x2[0] += dx2; y2[0] += dy2;
                l1.setLocation(Math.round(x1[0]), Math.round(y1[0]));
                l2.setLocation(Math.round(x2[0]), Math.round(y2[0]));
            }

            if (count[0] >= SWAP_STEPS) {
                timer.stop();
                removeLabels(l1, l2);

                // Cập nhật dữ liệu
                board[r1][c1].setType(type2);
                board[r2][c2].setType(type1);
                b1.setIcon(icons[type2]);
                b2.setIcon(icons[type1]);

                // Kiểm tra match
                if (movePerformedCallback != null) movePerformedCallback.onMovePerformed();

                if (logic.checkMatch(r1, c1) || logic.checkMatch(r2, c2)) {
                    animateDestroy();
                } else {
                 animateSwapBack(r1, c1, r2, c2, type1, type2);
                }
            }
        });
        timer.start();
    }

    // ─────────────────────────────────────────────
    //  Trượt ngược lại nếu không có match
    // ─────────────────────────────────────────────
    public void animateSwapBack(int r1, int c1, int r2, int c2, int type1, int type2) {
        JButton b1 = board[r1][c1].getButton();
        JButton b2 = board[r2][c2].getButton();

        Point p1 = SwingUtilities.convertPoint(b1.getParent(), b1.getLocation(), layeredPane);
        Point p2 = SwingUtilities.convertPoint(b2.getParent(), b2.getLocation(), layeredPane);

        // Hiển thị trạng thái sau khi swap (type2 ở p1, type1 ở p2)
        JLabel l1 = makeLabel(icons[type2], p1, b1.getWidth(), b1.getHeight());
        JLabel l2 = makeLabel(icons[type1], p2, b2.getWidth(), b2.getHeight());

        layeredPane.add(l1, JLayeredPane.DRAG_LAYER);
        layeredPane.add(l2, JLayeredPane.DRAG_LAYER);

        b1.setIcon(null);
        b2.setIcon(null);

        // Trượt ngược: l1 từ p1→p2, l2 từ p2→p1  (cùng hướng với swap thuận)
        float dx1 = (float)(p2.x - p1.x) / SWAP_STEPS;
        float dy1 = (float)(p2.y - p1.y) / SWAP_STEPS;
        float dx2 = (float)(p1.x - p2.x) / SWAP_STEPS;
        float dy2 = (float)(p1.y - p2.y) / SWAP_STEPS;

        final float[] x1 = {p1.x}, y1 = {p1.y};
        final float[] x2 = {p2.x}, y2 = {p2.y};
        final int[]   count = {0};

        Timer timer = new Timer(SWAP_DELAY, null);
        timer.addActionListener(e -> {
            if (cancelled) { timer.stop(); removeLabels(l1, l2); b1.setIcon(icons[type1]); b2.setIcon(icons[type2]); return; }
            count[0]++;

            if (count[0] >= SWAP_STEPS) {
                l1.setLocation(p2.x, p2.y);
                l2.setLocation(p1.x, p1.y);
            } else {
                x1[0] += dx1; y1[0] += dy1;
                x2[0] += dx2; y2[0] += dy2;
                l1.setLocation(Math.round(x1[0]), Math.round(y1[0]));
                l2.setLocation(Math.round(x2[0]), Math.round(y2[0]));
            }

            if (count[0] >= SWAP_STEPS) {
                timer.stop();
                removeLabels(l1, l2);

                // Khôi phục về type gốc
                board[r1][c1].setType(type1);
                board[r2][c2].setType(type2);
                b1.setIcon(icons[type1]);
                b2.setIcon(icons[type2]);

                if (doneCallback != null) doneCallback.onAnimationDone();
            }
        });
        timer.start();
    }

    // ─────────────────────────────────────────────
    //  Xóa match rồi chờ, sau đó chạy gravity
    // ─────────────────────────────────────────────
    public void animateDestroy() {
        logic.destroyMatch();

        Timer timer = new Timer(DESTROY_WAIT, null);
        timer.setRepeats(false);
        timer.addActionListener(e -> animateGravity());
        timer.start();
    }

    // ─────────────────────────────────────────────
    //  Kéo ô xuống theo từng frame
    // ─────────────────────────────────────────────
    public void animateGravity() {
        Timer timer = new Timer(GRAVITY_DELAY, null);
        timer.addActionListener(e -> {
            if (cancelled) { timer.stop(); return; }
            boolean moved = logic.fallStep();
            if (!moved) {
                timer.stop();
                logic.spawnFromTop();

                // Combo chain: nếu có match mới thì destroy tiếp
                if (logic.hasAnyMatch()) {
                    animateDestroy();
                } else {
                    if (doneCallback != null) doneCallback.onAnimationDone();
                }
            }
        });
        timer.start();
    }

    // ─────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────
    private JLabel makeLabel(ImageIcon icon, Point pos, int w, int h) {
        JLabel lbl = new JLabel(icon);
        lbl.setBounds(pos.x, pos.y, w, h);
        return lbl;
    }

    private void removeLabels(JLabel... labels) {
        for (JLabel l : labels) layeredPane.remove(l);
        layeredPane.repaint();
    }
}
