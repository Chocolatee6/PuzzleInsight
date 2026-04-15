import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import javax.swing.*;

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

    /** Reset điểm về 0 khi bắt đầu màn mới */
    public void resetScore() {
        score = 0;
        if (scoreListener != null) scoreListener.onScoreChanged(score);
    }

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

        // đếm số lượng ô bị phá hủy 
        int destroyedCnt = 0;
        for(int i=0; i<SIZE;i++){
            for(int j=0; j<SIZE;j++)
            {
                if(mark[i][j])
                {
                    board[i][j].setType(-1);
                    board[i][j].getButton().setIcon(null);
                    destroyedCnt++;
                }
            }
                
        }

        if(destroyedCnt==3)
        {
            score+=30;
        }else if(destroyedCnt==4)
        {
            score+=50;
        }else if(destroyedCnt>=5)
        {
            score+=80;
        }



        if (destroyedCnt > 0 && scoreListener != null) {
            scoreListener.onScoreChanged(score);
        }
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
    

    // 1. Class Move đại diện cho Toán tử (Operator)
public static class Move {
    public int r1, c1, r2, c2, score;
    public Move(int r1, int c1, int r2, int c2, int score) {
        this.r1 = r1; this.c1 = c1; this.r2 = r2; this.c2 = c2; this.score = score;
    }
}

    // 2. Hàm Heuristic h(u): Đánh giá lợi ích của trạng thái [cite: 395]
    public int simulateSwapAndEvaluate(int r1, int c1, int r2, int c2) {
        int t1 = board[r1][c1].getType(), t2 = board[r2][c2].getType();
        board[r1][c1].setType(t2); board[r2][c2].setType(t1); // Tráo tạm

        int h = 0;
            
        if (checkMatch(r1, c1)) {
            h += calculatePotentialScore(r1, c1);
        }
        if (checkMatch(r2, c2)) {
            h += calculatePotentialScore(r2, c2);
        }

        board[r1][c1].setType(t1); board[r2][c2].setType(t2); // Hoàn nguyên [cite: 717]
        return h;
    }


    public Move BFS(){
        Queue<Move> L = new LinkedList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (c < SIZE - 1 && simulateSwapAndEvaluate(r, c, r, c + 1) > 0) {
                    L.add(new Move(r, c, r, c + 1, 0)); // Đưa vào cuối L
                }
                // Kiểm tra tráo đổi dọc
                if (r < SIZE - 1 && simulateSwapAndEvaluate(r, c, r + 1, c) > 0) {
                    L.add(new Move(r, c, r + 1, c, 0)); // Đưa vào cuối L
                }
            }
        }
        
        // Rút trạng thái u ở đầu danh sách L ra để sử dụng
        if (!L.isEmpty()) {
            return L.poll(); 
        }
        return null;
    }

    public Move DFS() {
        Stack<Move> L = new Stack<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (c < SIZE - 1 && simulateSwapAndEvaluate(r, c, r, c + 1) > 0) {
                    L.push(new Move(r, c, r, c + 1, 0)); // Đưa vào đỉnh L
                }
                if (r < SIZE - 1 && simulateSwapAndEvaluate(r, c, r + 1, c) > 0) {
                    L.push(new Move(r, c, r + 1, c, 0)); // Đưa vào đỉnh L
                }
            }
        }
        if (!L.isEmpty()) {
            return L.pop(); 
        }
        return null;
    }

    // 1. TÌM KIẾM TỐT NHẤT ĐẦU TIÊN (Best-First Search)
    // Đặc điểm: Chỉ dùng hàm h(u) - Chỉ quan tâm đến điểm số tức thời.
    public Move BestFirstSearch() {
        List<Move> L = new ArrayList<>();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                // Kiểm tra tráo đổi ngang
                if (c < SIZE - 1) {
                    int h = simulateSwapAndEvaluate(r, c, r, c + 1); // h(u) là số điểm ăn được
                    if (h > 0) {
                        L.add(new Move(r, c, r, c + 1, h)); // Chỉ lưu h(u)
                    }
                }
                // Kiểm tra tráo đổi dọc
                if (r < SIZE - 1) {
                    int h = simulateSwapAndEvaluate(r, c, r + 1, c); // h(u) là số điểm ăn được
                    if (h > 0) {
                        L.add(new Move(r, c, r + 1, c, h)); // Chỉ lưu h(u)
                    }
                }
            }
        }

        // Sắp xếp danh sách L theo điểm số giảm dần (tham lam chọn điểm cao nhất)
        L.sort((m1, m2) -> Integer.compare(m2.score, m1.score));

        // Rút trạng thái tốt nhất ở đầu danh sách
        if (!L.isEmpty()) {
            return L.get(0); 
        }
        return null;
    }



    // 2. TÌM KIẾM TỐI ƯU A* (A-Star Search)
    // Đặc điểm: Dùng hàm f(u) = g(u) + h(u). 
    // Tối ưu hóa việc tạo Combo bằng cách ưu tiên đáy bàn cờ.
    public Move AStar() {
        List<Move> L = new ArrayList<>();
        

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                // Kiểm tra tráo đổi ngang
                if (c < SIZE - 1) {
                    int h = simulateSwapAndEvaluate(r, c, r, c + 1);
                    if (h > 0) {
                        int g = (r + 1) * 2; // g(u): Trọng số độ sâu (Càng gần đáy điểm g càng cao)
                        int f = g + (h*1000);       // CÔNG THỨC A*: f(u) = g(u) + h(u)
                        
                        L.add(new Move(r, c, r, c + 1, f));
                        
                    }
                }
                // Kiểm tra tráo đổi dọc
                if (r < SIZE - 1) {
                    int h = simulateSwapAndEvaluate(r, c, r + 1, c);
                    if (h > 0) {
                        int g = (r + 2) * 2; // g(u): Lấy r+2 vì ô bị tráo nằm ở dưới
                        int f = g + (h*1000);       // CÔNG THỨC A*: f(u) = g(u) + h(u)
                        L.add(new Move(r, c, r + 1, c,f));
                        
                    }
                }
            }
        }

        // Sắp xếp danh sách L theo giá trị f(u) giảm dần
        L.sort((m1, m2) -> Integer.compare(m2.score, m1.score));

        // Rút trạng thái tối ưu nhất ở đầu danh sách
        if (!L.isEmpty()) {
            return L.get(0); 
        }
        return null;
    }




    private int calculatePotentialScore(int r, int c) {
        int type = board[r][c].getType();
        if(type == -1) return  0;
        int horizontal = 1;
        for(int j=c-1;j>=0 && board[r][j].getType()==type;j--) horizontal++;
        for(int j = c+1;j<SIZE && board[r][j].getType() == type;j++) horizontal++;
        int vertical = 1;
        for (int i = r - 1; i >= 0 && board[i][c].getType() == type; i--) vertical++;
        for (int i = r + 1; i < SIZE && board[i][c].getType() == type; i++) vertical++;
        int totalScore = 0;
        if(horizontal == 3) totalScore+=30;
        else if(horizontal == 4) totalScore+=50;
        else if(horizontal>=5) totalScore+=80;

        if (vertical == 3) totalScore += 30;
        else if (vertical == 4) totalScore += 50;
        else if (vertical >= 5) totalScore += 80;

        if (horizontal >= 3 && vertical >= 3) {
            totalScore += 100; 
        }

        return totalScore;
        
    } 




}
