package me.ihqqq.url_shortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShortenRequest(
        @NotBlank(message = "URL_BLANK")
        @Size(max = 2048, message = "URL_TOO_LONG")
        // Cho phép chuỗi rỗng ở đây, vì trường hợp rỗng đã có @NotBlank lo
        @Pattern(regexp = "^\\s*(https?://\\S+)?\\s*$", message = "URL_INVALID")
        String longUrl
) {
}