/**
 * Cấu hình từng màn chơi: điểm mục tiêu, số bước, thời gian.
 */
public class LevelConfig {

    public final int levelNumber;
    public final int targetScore;
    public final int maxMoves;
    public final int timerSeconds;

    public LevelConfig(int levelNumber, int targetScore, int maxMoves, int timerSeconds) {
        this.levelNumber  = levelNumber;
        this.targetScore  = targetScore;
        this.maxMoves     = maxMoves;
        this.timerSeconds = timerSeconds;
    }

    /** 5 màn mặc định */
    public static LevelConfig generateLevel(int levelIndex) {
        int n = levelIndex + 1; // Bắt đầu từ level 1
        
        // 1. Điểm số tăng theo hàm bậc 2 (càng lên cao càng cần nhiều điểm)
        int score = 100 * (n * n + n); 
        
        // 2. Số bước giảm dần, nhưng không bao giờ dưới 10 bước
        int moves = Math.max(10, 30 - (n * 2)); 
        
        // 3. Thời gian giảm dần, nhưng tối thiểu là 30 giây
        int time = Math.max(30, 130 - (n * 10)); 
        
        return new LevelConfig(n, score, moves, time);
    }
}
