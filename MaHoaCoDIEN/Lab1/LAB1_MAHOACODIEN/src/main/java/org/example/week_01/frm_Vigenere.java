package org.example.week_01;

public class frm_Vigenere extends CipherFrame {
    public frm_Vigenere() {
        super("Vigenere Cipher", "MẬT MÃ VIGENÈRE", "KEY", "Chỉ gồm chữ A-Z, ví dụ KEY", "vigenere_ciphertext.txt");
    }

    @Override protected String doEncrypt(String text, String key) { return VigenereCipher.encrypt(text, key); }
    @Override protected String doDecrypt(String text, String key) { return VigenereCipher.decrypt(text, key); }
}