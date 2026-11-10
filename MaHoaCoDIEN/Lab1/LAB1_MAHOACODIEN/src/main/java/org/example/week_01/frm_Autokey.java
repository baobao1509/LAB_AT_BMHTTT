package org.example.week_01;

public class frm_Autokey extends CipherFrame {
    public frm_Autokey() {
        super("Autokey Cipher", "MẬT MÃ AUTOKEY", "KEY", "Khóa mồi chỉ gồm chữ A-Z, ví dụ KEY", "autokey_ciphertext.txt");
    }

    @Override protected String doEncrypt(String text, String key) { return AutokeyCipher.encrypt(text, key); }
    @Override protected String doDecrypt(String text, String key) { return AutokeyCipher.decrypt(text, key); }
}
