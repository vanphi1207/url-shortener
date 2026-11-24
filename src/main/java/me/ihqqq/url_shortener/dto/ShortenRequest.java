package me.ihqqq.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShortenRequest(
        @NotBlank(message = "longUrl không được để trống")
        @Size(max = 2048, message = "longUrl không được dài quá 2048 ký tự")
        // Chỉ chấp nhận http/https để tránh các scheme nguy hiểm như javascript: hay data:
        @Pattern(regexp = "^\\s*https?://\\S+\\s*$", message = "longUrl phải bắt đầu bằng http:// hoặc https://")
        String longUrl
) {
}