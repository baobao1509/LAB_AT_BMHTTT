package org.example.week_01;


import java.awt.Font;
import javax.swing.*;

public class frm_Playfair extends CipherFrame {

    private final JTextArea txt_matrix = new JTextArea(5, 9);
    private final JButton btn_matrix = new JButton("Generate Key Matrix");

    public frm_Playfair() {
        super("Playfair Cipher", "MẬT MÃ PLAYFAIR", "KEYWORD", "Chỉ gồm chữ A-Z, ví dụ KEYWORD", "playfair_ciphertext.txt");

        txt_matrix.setEditable(false);
        txt_matrix.setLineWrap(false);
        txt_matrix.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        extra.add(new JLabel("Key Matrix:"));
        extra.add(new JScrollPane(txt_matrix));
        extra.add(btn_matrix);
        extra.setVisible(true);

        btn_matrix.addActionListener(e -> {
            try {
                txt_matrix.setText(PlayfairCipher.getKeyMatrix(txt_key.getText()));
                txt_matrix.setCaretPosition(0);
                lbl_status.setText("Đã tạo ma trận khóa 5x5 (I và J dùng chung ô).");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Khóa không hợp lệ", JOptionPane.ERROR_MESSAGE);
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    @Override protected String doEncrypt(String text, String key) { return PlayfairCipher.encrypt(text, key); }
    @Override protected String doDecrypt(String text, String key) { return PlayfairCipher.decrypt(text, key); }
}