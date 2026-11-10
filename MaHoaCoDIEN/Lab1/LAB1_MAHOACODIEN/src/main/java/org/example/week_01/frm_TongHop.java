package org.example.week_01;

import javax.swing.*;

public class frm_TongHop extends CipherFrame {

    private final JComboBox<String> cbo_algorithm = new JComboBox<>(CipherTongHop.ALGORITHMS);
    private final JButton btn_sql = new JButton("Kết nối SQL Server");
    private final JButton btn_history = new JButton("Xem lịch sử");

    public frm_TongHop() {
        super("Chương trình tổng hợp - 6 thuật toán", "CHƯƠNG TRÌNH TỔNG HỢP 6 THUẬT TOÁN CỔ ĐIỂN",
                "3", "Khóa mẫu: 3", "ciphertext.txt");

        extra.add(new JLabel("Thuật toán:"));
        extra.add(cbo_algorithm);
        extra.setVisible(true);
        buttons.add(btn_sql);
        buttons.add(btn_history);

        cbo_algorithm.addActionListener(e -> {
            String algorithm = (String) cbo_algorithm.getSelectedItem();
            txt_key.setText(CipherTongHop.sampleKey(algorithm));
            lbl_hint.setText("Khóa mẫu: " + CipherTongHop.sampleKey(algorithm));
        });
        btn_sql.addActionListener(e -> new frm_KetNoiSQL().setVisible(true));
        btn_history.addActionListener(e -> new frm_LichSu().setVisible(true));

        pack();
        setLocationRelativeTo(null);
    }

    private String algorithm() {
        return (String) cbo_algorithm.getSelectedItem();
    }

    @Override protected String doEncrypt(String text, String key) {
        return CipherTongHop.encrypt(algorithm(), text, key);
    }

    @Override protected String doDecrypt(String text, String key) {
        return CipherTongHop.decrypt(algorithm(), text, key);
    }

    @Override protected String statusAfter(boolean encrypt, String input, String result) {
        String saved = HistoryDao.tryInsert(algorithm(), encrypt ? "ENCRYPT" : "DECRYPT", input, result);
        return (encrypt ? "Đã mã hóa " : "Đã giải mã ") + algorithm() + ". " + saved;
    }
}