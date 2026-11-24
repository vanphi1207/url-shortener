package me.ihqqq.url_shortener.util;

public final class Base62 {

    private static final String ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length(); // 62

    private Base62() {
    }

    public static String encode(long n) {
        if (n < 0) {
            throw new IllegalArgumentException("Số cần mã hóa không được âm: " + n);
        }
        if (n == 0) {
            return String.valueOf(ALPHABET.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        while (n > 0) {
            sb.append(ALPHABET.charAt((int) (n % BASE)));
            n /= BASE;
        }
        return sb.reverse().toString();
    }

    public static long decode(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Chuỗi cần giải mã không được rỗng");
        }
        long result = 0;
        for (int i = 0; i < s.length(); i++) {
            int digit = ALPHABET.indexOf(s.charAt(i));
            if (digit < 0) {
                throw new IllegalArgumentException(
                        "Ký tự không hợp lệ '" + s.charAt(i) + "' trong chuỗi: " + s);
            }
            try {
                result = Math.addExact(Math.multiplyExact(result, (long) BASE), digit);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Chuỗi quá dài, vượt giới hạn kiểu long: " + s);
            }
        }
        return result;
    }
}