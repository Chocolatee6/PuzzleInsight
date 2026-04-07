import javax.swing.*;
import java.awt.*;

public class GuidePanel extends JPanel {

    public GuidePanel(Main main){

        setLayout(new BorderLayout());

        // ===== BACKGROUND =====
        JPanel bg = new JPanel(){
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;

                GradientPaint gp = new GradientPaint(
                        0,0,new Color(20,20,60),
                        getWidth(),getHeight(),new Color(100,0,140)
                );

                g2.setPaint(gp);
                g2.fillRect(0,0,getWidth(),getHeight());
            }
        };

        bg.setLayout(new BorderLayout());
        add(bg);

        // ===== TITLE =====
        JLabel title = new JLabel("HƯỚNG DẪN CHƠI", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(20,0,20,0));

        bg.add(title, BorderLayout.NORTH);

        // ===== CONTENT =====
        JTextArea txt = new JTextArea();
        txt.setEditable(false);
        txt.setOpaque(false);
        txt.setForeground(Color.WHITE);
        txt.setFont(new Font("Arial", Font.PLAIN, 16));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);

        txt.setText(
            "🎮 CÁCH CHƠI:\n\n" +
            "👉 Đổi chỗ 2 ô kề nhau để tạo hàng 3 giống nhau.\n" +
            "👉 Khi match ≥ 3 → sẽ bị phá và bạn được điểm.\n\n" +

            "💣 COMBO & KỸ NĂNG:\n" +
            "🔥 Match 4 → tạo BOM (nổ hàng + cột).\n" +
            "⚡ Match 5 → tạo LIGHTNING (xóa cả hàng).\n" +
            "💥 Combo liên tiếp → nhân điểm cực mạnh.\n\n" +

            "🎯 MỤC TIÊU:\n" +
            "✔ Đạt đủ điểm để lên level.\n" +
            "✔ Số lượt (Moves) có giới hạn → hãy tính toán!\n\n" +

            "💡 MẸO CHƠI:\n" +
            "• Ưu tiên tạo combo lớn.\n" +
            "• Giữ bomb để dùng lúc cần.\n" +
            "• Quan sát toàn board trước khi swap.\n"
        );

        JScrollPane scroll = new JScrollPane(txt);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        bg.add(scroll, BorderLayout.CENTER);

        // ===== BACK BUTTON =====
        JButton back = new JButton("← MENU");
        back.setBackground(new Color(255,120,0));
        back.setForeground(Color.WHITE);
        back.setFont(new Font("Arial", Font.BOLD, 16));
        back.setFocusPainted(false);

        back.addActionListener(e -> main.showScreen("MENU"));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(back);

        bg.add(bottom, BorderLayout.SOUTH);
    }
}