package org.example.week_01;


public class frm_Caesar extends CipherFrame {
    public frm_Caesar() {
        super("Caesar Cipher", "MẬT MÃ CAESAR", "3", "Số nguyên, ví dụ 3", "caesar_ciphertext.txt");
    }

    private int key(String k) {
        return parseInt(k, "Khóa Caesar phải là số nguyên, ví dụ 3.");
    }

    @Override protected String doEncrypt(String text, String key) { return CaesarCipher.encrypt(text, key(key)); }
    @Override protected String doDecrypt(String text, String key) { return CaesarCipher.decrypt(text, key(key)); }
}