import javax.swing.*;

/**
 * Đại diện một ô trong bàn cờ 8x8.
 * Lưu trữ type (loại nhân vật) và JButton hiển thị.
 */
public class BoardCell {

    private int type;       // -1 = trống, 0-4 = loại nhân vật
    private JButton button;
    private boolean isBomb = false;

    public void setBomb(boolean b) {
        isBomb = b;
    }

    public boolean isBomb() {
        return isBomb;
    }
    public BoardCell(int type, JButton button) {
        this.type   = type;
        this.button = button;
    }

    public int getType()           { return type; }
    public void setType(int type)  { this.type = type; }
    public JButton getButton()     { return button; }
}
