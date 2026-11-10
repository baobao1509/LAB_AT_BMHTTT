package org.example.week_01;

public class frm_RailFence extends CipherFrame {
    public frm_RailFence() {
        super("Rail Fence Cipher", "MẬT MÃ RAIL FENCE", "3", "Số hàng (rails) >= 2, ví dụ 3", "railfence_ciphertext.txt");
    }

    private int rails(String k) {
        return parseInt(k, "Số hàng phải là số nguyên, ví dụ 3.");
    }

    @Override protected String doEncrypt(String text, String key) { return RailFenceCipher.encrypt(text, rails(key)); }
    @Override protected String doDecrypt(String text, String key) { return RailFenceCipher.decrypt(text, rails(key)); }
}