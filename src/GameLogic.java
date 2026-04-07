import javax.swing.*;
import java.util.Random;

/**
 * GameLogic chứa toàn bộ logic trò chơi thuần túy:
 *  - checkKeNhau     : kiểm tra hai ô kề nhau
 *  - checkMatch      : kiểm tra match >= 3
 *  - hasAnyMatch     : kiểm tra toàn bàn có match không
 *  - destroyMatch    : xóa các ô match, cộng điểm
 *  - fallStep        : kéo các ô xuống 1 bước
 *  - spawnFromTop    : lấp đầy ô trống bằng nhân vật mới
 */
public class GameLogic {

    private static final int SIZE = 8;

    private final BoardCell[][] board;
    private final ImageIcon[]  icons;
    private final Random       rd = new Random();

    private int score = 0;
    private ScoreListener scoreListener;

    /** Callback để thông báo điểm số thay đổi */
    public interface ScoreListener {
        void onScoreChanged(int newScore);
    }

    public GameLogic(BoardCell[][] board, ImageIcon[] icons) {
        this.board = board;
        this.icons  = icons;
    }

    public void setScoreListener(ScoreListener listener) {
        this.scoreListener = listener;
    }

    public int getScore() { return score; }

    // ─────────────────────────────────────────────
    //  Kiểm tra kề nhau (Manhattan distance == 1)
    // ─────────────────────────────────────────────
    public boolean checkKeNhau(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2) == 1;
    }

    // ─────────────────────────────────────────────
    //  Kiểm tra 1 ô có tạo match >= 3 không
    // ─────────────────────────────────────────────
    public boolean checkMatch(int r, int c) {
        int type = board[r][c].getType();
        if (type == -1) return false;

        // Kiểm tra ngang
        int cnt = 1;
        for (int j = c - 1; j >= 0 && board[r][j].getType() == type; j--) cnt++;
        for (int j = c + 1; j < SIZE && board[r][j].getType() == type; j++) cnt++;
        if (cnt >= 3) return true;

        // Kiểm tra dọc
        cnt = 1;
        for (int i = r - 1; i >= 0 && board[i][c].getType() == type; i--) cnt++;
        for (int i = r + 1; i < SIZE && board[i][c].getType() == type; i++) cnt++;
        return cnt >= 3;
    }

    // ─────────────────────────────────────────────
    //  Kiểm tra toàn bàn có ô nào match không
    // ─────────────────────────────────────────────
    public boolean hasAnyMatch() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (checkMatch(i, j)) return true;
        return false;
    }

    // ─────────────────────────────────────────────
    //  Xóa tất cả ô match, cộng điểm
    // ─────────────────────────────────────────────
    public void destroyMatch() {
        boolean[][] mark = new boolean[SIZE][SIZE];

        // Check ngang (>=3 liên tiếp)
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j <= SIZE - 3; j++) {
                int type = board[i][j].getType();
                if (type != -1
                        && type == board[i][j + 1].getType()
                        && type == board[i][j + 2].getType()) {
                    mark[i][j] = mark[i][j + 1] = mark[i][j + 2] = true;
                }
            }
        }

        // Check dọc (>=3 liên tiếp)
        for (int i = 0; i <= SIZE - 3; i++) {
            for (int j = 0; j < SIZE; j++) {
                int type = board[i][j].getType();
                if (type != -1
                        && type == board[i + 1][j].getType()
                        && type == board[i + 2][j].getType()) {
                    mark[i][j] = mark[i + 1][j] = mark[i + 2][j] = true;
                }
            }
        }

        // Phá các ô được đánh dấu
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (mark[i][j]) {
                    board[i][j].setType(-1);
                    board[i][j].getButton().setIcon(null);
                    score += 10;
                }
            }
        }

        if (scoreListener != null) scoreListener.onScoreChanged(score);
    }

    // ─────────────────────────────────────────────
    //  Kéo các ô xuống 1 bước (gravity)
    // ─────────────────────────────────────────────
    public boolean fallStep() {
        boolean moved = false;
        for (int j = 0; j < SIZE; j++) {
            for (int i = SIZE - 1; i > 0; i--) {
                int cur   = board[i][j].getType();
                int above = board[i - 1][j].getType();
                if (cur == -1 && above != -1) {
                    board[i][j].setType(above);
                    board[i][j].getButton().setIcon(icons[above]);

                    board[i - 1][j].setType(-1);
                    board[i - 1][j].getButton().setIcon(null);

                    moved = true;
                }
            }
        }
        return moved;
    }

    // ─────────────────────────────────────────────
    //  Lấp đầy các ô trống bằng nhân vật ngẫu nhiên
    // ─────────────────────────────────────────────
    public void spawnFromTop() {
        for (int j = 0; j < SIZE; j++) {
            for (int i = 0; i < SIZE; i++) {
                if (board[i][j].getType() == -1) {
                    int newType = rd.nextInt(5);
                    board[i][j].setType(newType);
                    board[i][j].getButton().setIcon(icons[newType]);
                }
            }
        }
    }
}
