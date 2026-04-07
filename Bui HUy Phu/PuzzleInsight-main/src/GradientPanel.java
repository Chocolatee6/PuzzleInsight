import java.awt.*;
import javax.swing.*;

public class GradientPanel extends JPanel {

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Ép kiểu sang Graphics2D để dùng gradient
        Graphics2D g2d = (Graphics2D) g;

        // Màu bắt đầu và kết thúc
        Color colorStart = new Color(255, 150, 200);
        Color colorEnd = new Color(150, 200, 255);

        // Tạo hiệu ứng gradient
        GradientPaint gradient = new GradientPaint(
                0, 0, colorStart,
                getWidth(), getHeight(), colorEnd
        );

        // Áp dụng gradient
        g2d.setPaint(gradient);

        // Vẽ nền full panel
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}