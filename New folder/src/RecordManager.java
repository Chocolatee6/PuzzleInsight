import java.util.*;
import java.util.prefs.Preferences;

public class RecordManager {

    private static final int MAX = 5;
    private Preferences prefs;

    public RecordManager() {
        prefs = Preferences.userNodeForPackage(RecordManager.class);
    }

    public List<GameRecord> getRecords() {
        List<GameRecord> list = new ArrayList<>();

        for (int i = 0; i < MAX; i++) {
            int score = prefs.getInt("score" + i, -1);
            int level = prefs.getInt("level" + i, -1);

            if (score != -1) {
                list.add(new GameRecord(score, level));
            }
        }

        return list;
    }

    public void addRecord(int score, int level) {
        List<GameRecord> list = getRecords();

        list.add(new GameRecord(score, level));

        // sort giảm dần
        list.sort((a, b) -> Integer.compare(b.score, a.score));

        // giữ top 5
        if (list.size() > MAX) {
            list = list.subList(0, MAX);
        }

        // lưu lại
        for (int i = 0; i < list.size(); i++) {
            prefs.putInt("score" + i, list.get(i).score);
            prefs.putInt("level" + i, list.get(i).level);
        }
    }
}