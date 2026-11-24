package me.ihqqq.url_shortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashUtilTest {

    @Test
    void sha256Hex_theoVectorChuan() {
        // Vector kiểm thử chuẩn của SHA-256 cho chuỗi "abc"
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                HashUtil.sha256Hex("abc"));
    }

    @Test
    void sha256Hex_luonDai64KyTu() {
        assertEquals(64, HashUtil.sha256Hex("https://www.example.com/a/very/long/path?x=1").length());
    }

    @Test
    void sha256Hex_haiChuoiKhacNhauChoHaiHashKhacNhau() {
        assertNotEquals(HashUtil.sha256Hex("https://a.com"), HashUtil.sha256Hex("https://b.com"));
    }
}