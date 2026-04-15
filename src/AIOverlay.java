import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AIOverlay extends JPanel {

    // ── INTERFACE GIAO TIẾP VỚI GAMEUI ──
    public interface AISelectListener {
        void onAlgorithmSelected(int algoIndex);

        void onCancel();
    }

    private AISelectListener listener;

    public AIOverlay() {
        // Căn giữa hộp thoại và làm nền trong suốt để vẽ lớp phủ mờ
        setOpaque(false);
        setLayout(new GridBagLayout());

        // ── KHUNG HỘP THOẠI CHÍNH ──
        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.setBackground(new Color(30, 30, 45, 240)); // Nền xanh đen tối
        dialogPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 200, 255), 2), // Viền xanh sáng
                BorderFactory.createEmptyBorder(25, 35, 25, 35) // Đệm lề trong
        ));

        // ── TIÊU ĐỀ ──
        JLabel title = new JLabel("CHỌN THUẬT TOÁN AI");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Chọn bộ não để máy tự chơi");
        subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitle.setForeground(new Color(180, 180, 200));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── KHAI BÁO 4 NÚT THUẬT TOÁN & NÚT HỦY ──
        JButton btnBFS = createModernButton("1. BFS (Tìm kiếm mù)", new Color(255, 120, 120));
        JButton btnDFS = createModernButton("2. DFS (Tìm kiếm độ sâu)", new Color(255, 180, 120));
        JButton btnBestFirst = createModernButton("3. Best-First (Tham lam)", new Color(120, 255, 120));
        JButton btnAStar = createModernButton("4. A* (Tối ưu Combo)", new Color(120, 200, 255));
        JButton btnCancel = createModernButton("Hủy bỏ", new Color(200, 200, 200));

        // ── GẮN SỰ KIỆN CHO CÁC NÚT ──
        btnBFS.addActionListener(e -> {
            if (listener != null)
                listener.onAlgorithmSelected(0);
        });
        btnDFS.addActionListener(e -> {
            if (listener != null)
                listener.onAlgorithmSelected(1);
        });
        btnBestFirst.addActionListener(e -> {
            if (listener != null)
                listener.onAlgorithmSelected(2);
        });
        btnAStar.addActionListener(e -> {
            if (listener != null)
                listener.onAlgorithmSelected(3);
        });
        btnCancel.addActionListener(e -> {
            if (listener != null)
                listener.onCancel();
        });

        // ── LẮP RÁP VÀO KHUNG THEO CHIỀU DỌC ──
        dialogPanel.add(title);
        dialogPanel.add(Box.createVerticalStrut(5));
        dialogPanel.add(subtitle);
        dialogPanel.add(Box.createVerticalStrut(25));

        dialogPanel.add(btnBFS);
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(btnDFS);
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(btnBestFirst);
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(btnAStar);

        dialogPanel.add(Box.createVerticalStrut(20));
        dialogPanel.add(btnCancel);

        add(dialogPanel); // Thêm khung hộp thoại vào giữa Overlay
    }

    public void setListener(AISelectListener listener) {
        this.listener = listener;
    }

    // ── VẼ LỚP PHỦ SƯƠNG MÙ ĐEN PHÍA SAU ──
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(0, 0, 0, 160)); // Màu đen, độ trong suốt 60%
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    // ── HÀM TẠO NÚT BẤM HIỆN ĐẠI (FLAT DESIGN & HOVER) ──
    private JButton createModernButton(String text, Color hoverColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(50, 50, 70));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 100), 1),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 45));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
                btn.setForeground(Color.BLACK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(50, 50, 70));
                btn.setForeground(Color.WHITE);
            }
        });
        return btn;
    }
}