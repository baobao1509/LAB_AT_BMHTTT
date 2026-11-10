package org.example.week_01;

public class frm_Transposition extends CipherFrame {
    public frm_Transposition() {
        super("Transposition Cipher", "MẬT MÃ HOÁN VỊ CỘT (TRANSPOSITION)", "2,3,1",
                "Thứ tự đọc cột, cách nhau dấu phẩy, ví dụ 2,3,1", "transposition_ciphertext.txt");
    }

    @Override protected String doEncrypt(String text, String key) {
        return TranspositionCipher.encrypt(text, TranspositionCipher.parseKey(key));
    }

    @Override protected String doDecrypt(String text, String key) {
        return TranspositionCipher.decrypt(text, TranspositionCipher.parseKey(key));
    }
}