package org.example.week_01;
import java.awt.*;
import javax.swing.*;

/** Khung giao diện chung cho các form mã hóa (Plaintext / Key / Ciphertext / 4 nút). */
public abstract class CipherFrame extends JFrame {

    protected final JTextArea txt_plaintext = new JTextArea(3, 45);
    protected final JTextField txt_key = new JTextField(15);
    protected final JTextArea txt_ciphertext = new JTextArea(3, 45);
    protected final JButton btn_encrypt = new JButton("Encrypt");
    protected final JButton btn_decrypt = new JButton("Decrypt");
    protected final JButton btn_open = new JButton("Open Ciphertext File");
    protected final JButton btn_save = new JButton("Save Ciphertext File");
    protected final JLabel lbl_status = new JLabel("Sẵn sàng.");
    protected final JLabel lbl_hint = new JLabel();
    protected final JPanel extra = new JPanel(new FlowLayout(FlowLayout.LEFT)); // chỗ chèn thêm control
    protected final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));

    protected CipherFrame(String title, String heading, String defaultKey, String hint, String saveName) {
        super(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        txt_key.setText(defaultKey);
        lbl_hint.setText(hint);
        txt_plaintext.setLineWrap(true);
        txt_ciphertext.setLineWrap(true);

        JLabel head = new JLabel(heading);
        head.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel keyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        keyRow.add(txt_key);
        keyRow.add(Box.createHorizontalStrut(10));
        keyRow.add(lbl_hint);

        buttons.add(btn_encrypt);
        buttons.add(btn_decrypt);
        buttons.add(btn_open);
        buttons.add(btn_save);
        extra.setVisible(false);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        p.add(head, c);
        addRow(p, 1, "Plaintext:", new JScrollPane(txt_plaintext), true);
        addRow(p, 2, "Key:", keyRow, false);
        c.gridx = 0; c.gridy = 3; c.gridwidth = 2;
        p.add(extra, c);
        addRow(p, 4, "Ciphertext:", new JScrollPane(txt_ciphertext), true);
        c.gridx = 0; c.gridy = 5; c.gridwidth = 2;
        p.add(buttons, c);
        c.gridy = 6;
        p.add(lbl_status, c);

        setContentPane(p);

        btn_encrypt.addActionListener(e -> {
            String text = txt_plaintext.getText();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hãy nhập Plaintext trước khi mã hóa.");
                return;
            }
            try {
                String result = doEncrypt(text, txt_key.getText().trim());
                txt_ciphertext.setText(result);
                lbl_status.setText(statusAfter(true, text, result));
            } catch (IllegalArgumentException ex) {
                showError(ex);
            }
        });

        btn_decrypt.addActionListener(e -> {
            String text = txt_ciphertext.getText();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Hãy nhập Ciphertext trước khi giải mã.");
                return;
            }
            try {
                String result = doDecrypt(text, txt_key.getText().trim());
                txt_plaintext.setText(result);
                lbl_status.setText(statusAfter(false, text, result));
            } catch (IllegalArgumentException ex) {
                showError(ex);
            }
        });

        btn_open.addActionListener(e -> {
            String path = FileTextIO.openText(this, txt_ciphertext);
            if (path != null) {
                lbl_status.setText("Đã mở tệp: " + path);
            }
        });

        btn_save.addActionListener(e -> {
            String path = FileTextIO.saveText(this, txt_ciphertext, saveName);
            if (path != null) {
                lbl_status.setText("Đã lưu tệp: " + path);
            }
        });

        pack();
        setLocationRelativeTo(null);
    }

    private static void addRow(JPanel p, int row, String label, JComponent comp, boolean fill) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0; c.gridy = row;
        p.add(new JLabel(label), c);
        c.gridx = 1;
        if (fill) {
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
        }
        p.add(comp, c);
    }

    private void showError(IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(),
                "Dữ liệu không hợp lệ", JOptionPane.ERROR_MESSAGE);
    }

    /** Các form con ghi đè: ném IllegalArgumentException nếu khóa/dữ liệu sai. */
    protected abstract String doEncrypt(String plaintext, String key);

    protected abstract String doDecrypt(String ciphertext, String key);

    /** Dòng trạng thái sau khi chạy xong; form tổng hợp ghi đè để thêm thông báo lưu SQL. */
    protected String statusAfter(boolean encrypt, String input, String result) {
        return encrypt ? "Đã mã hóa." : "Đã giải mã.";
    }

    protected static int parseInt(String s, String message) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
    }
}