package org.example.week_01;

import java.awt.*;
import javax.swing.*;

public class frm_TanCong extends JFrame {

    private final JTextArea txt_cipher = new JTextArea(2, 40);
    private final JTextField txt_known_plain = new JTextField("HELLO", 10);
    private final JTextField txt_known_cipher = new JTextField("RIJVS", 10);
    private final JTextArea txt_result = new JTextArea(12, 40);
    private final JLabel lbl_status = new JLabel("Sẵn sàng.");
    private final JButton btn_bruteforce = new JButton("Brute Force Caesar");
    private final JButton btn_frequency = new JButton("Frequency Analysis");
    private final JButton btn_known = new JButton("Known Plaintext (Vigenere)");

    public frm_TanCong() {
        super("Phân tích mật mã cổ điển");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel head = new JLabel("PHÂN TÍCH (TẤN CÔNG) MẬT MÃ CỔ ĐIỂN");
        head.setFont(new Font("Segoe UI", Font.BOLD, 18));

        txt_cipher.setLineWrap(true);
        txt_result.setEditable(false);
        txt_result.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row1.add(new JLabel("Ciphertext:"));
        row1.add(new JScrollPane(txt_cipher));

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row2.add(btn_bruteforce);
        row2.add(btn_frequency);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row3.add(new JLabel("Bản rõ đã biết:"));
        row3.add(txt_known_plain);
        row3.add(new JLabel("Bản mã:"));
        row3.add(txt_known_cipher);
        row3.add(btn_known);

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));
        for (JComponent c : new JComponent[] {head, row1, row2, row3}) {
            c.setAlignmentX(Component.LEFT_ALIGNMENT);
            top.add(c);
        }

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(txt_result), BorderLayout.CENTER);
        add(lbl_status, BorderLayout.SOUTH);

        btn_bruteforce.addActionListener(e -> {
            String cipher = txt_cipher.getText().trim();
            if (cipher.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hãy nhập Ciphertext.");
                return;
            }
            txt_result.setText(AttackDemos.caesarBruteForce(cipher));
            txt_result.setCaretPosition(0);
            lbl_status.setText("Đã thử đủ 26 khóa Caesar.");
        });

        btn_frequency.addActionListener(e -> {
            String cipher = txt_cipher.getText().trim();
            if (cipher.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hãy nhập Ciphertext.");
                return;
            }
            txt_result.setText(AttackDemos.frequency(cipher));
            txt_result.setCaretPosition(0);
            lbl_status.setText("Đã thống kê tần suất chữ cái.");
        });

        btn_known.addActionListener(e -> {
            try {
                String key = AttackDemos.knownPlaintext(txt_known_plain.getText(), txt_known_cipher.getText());
                txt_result.setText("Khóa Vigenère suy ra từng vị trí: " + key
                        + "\nNếu khóa ngắn hơn bản rõ, khóa sẽ lặp lại (KEY KEY ...).");
                lbl_status.setText("Khóa suy ra: " + key);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setLocationRelativeTo(null);
    }
}