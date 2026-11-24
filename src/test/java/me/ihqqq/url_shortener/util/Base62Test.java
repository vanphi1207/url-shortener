package me.ihqqq.url_shortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Base62Test {

    @Test
    void encode_theoViDuTrongSach() {
        assertEquals("2TX", Base62.encode(11157L));
    }

    @Test
    void decode_theoViDuTrongSach() {
        assertEquals(11157L, Base62.decode("2TX"));
    }

    @Test
    void encode_so0() {
        assertEquals("0", Base62.encode(0L));
    }

    @Test
    void encodeRoiDecode_ravePhaiBangSoBanDau() {
        long[] values = {1L, 61L, 62L, 63L, 3843L, 3844L, 987_654_321L,
                56_800_235_584L, 3_521_614_606_207L, Long.MAX_VALUE};
        for (long v : values) {
            assertEquals(v, Base62.decode(Base62.encode(v)), "Sai với giá trị " + v);
        }
    }

    @Test
    void encode_soAm_nemLoi() {
        assertThrows(IllegalArgumentException.class, () -> Base62.encode(-1L));
    }

    @Test
    void decode_kyTuKhongHopLe_nemLoi() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode("ab$"));
    }

    @Test
    void decode_chuoiRongHoacNull_nemLoi() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode(""));
        assertThrows(IllegalArgumentException.class, () -> Base62.decode(null));
    }

    @Test
    void decode_chuoiQuaDai_nemLoi() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode("ZZZZZZZZZZZZZZZZ"));
    }

    @Test
    void maDauTien_coDo_dai_7() {
        // 62^6 là ID nhỏ nhất cho ra mã 7 ký tự
        assertEquals("1000000", Base62.encode(56_800_235_584L));
        assertEquals(7, Base62.encode(56_800_235_584L).length());
    }

    @Test
    void maLonNhat7KyTu_vaSoKeTiepRa8KyTu() {
        assertEquals("ZZZZZZZ", Base62.encode(3_521_614_606_207L));
        assertEquals(8, Base62.encode(3_521_614_606_208L).length());
    }
}