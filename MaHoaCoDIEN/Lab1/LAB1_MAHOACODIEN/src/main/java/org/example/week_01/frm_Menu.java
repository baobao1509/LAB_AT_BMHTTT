package org.example.week_01;

import java.awt.*;
import java.util.function.Supplier;
import javax.swing.*;

public class frm_Menu extends JFrame {

    public frm_Menu() {
        super("LAB 1 - Mã hóa đối xứng cổ điển");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel head = new JLabel("LAB 1 - MÃ HÓA ĐỐI XỨNG CỔ ĐIỂN");
        head.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel sub = new JLabel("Chọn một bài để mở form tương ứng:");
        JLabel foot = new JLabel("Mỗi form mở ra là một cửa sổ riêng; đóng form con không làm tắt chương trình.");

        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 10));
        addBtn(grid, "1.1 Caesar", frm_Caesar::new);
        addBtn(grid, "1.2 Vigenère", frm_Vigenere::new);
        addBtn(grid, "1.3 Rail Fence", frm_RailFence::new);
        addBtn(grid, "1.4 Playfair", frm_Playfair::new);
        addBtn(grid, "1.5 Transposition", frm_Transposition::new);
        addBtn(grid, "1.6 Autokey", frm_Autokey::new);
        addBtn(grid, "Nâng cao 01 - Tổng hợp", frm_TongHop::new);
        addBtn(grid, "Nâng cao 02 - Tấn công", frm_TanCong::new);
        addBtn(grid, "Kết nối SQL Server", frm_KetNoiSQL::new);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        for (JComponent c : new JComponent[] {head, sub, grid, foot}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            root.add(c);
            root.add(Box.createVerticalStrut(10));
        }
        setContentPane(root);

        pack();
        setLocationRelativeTo(null);
    }

    private static void addBtn(JPanel panel, String text, Supplier<JFrame> factory) {
        JButton b = new JButton(text);
        b.addActionListener(e -> factory.get().setVisible(true));
        panel.add(b);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new frm_Menu().setVisible(true));
    }
}