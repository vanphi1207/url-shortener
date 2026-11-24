package me.ihqqq.url_shortener.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    URL_BLANK(1002, "longUrl không được để trống", HttpStatus.BAD_REQUEST),
    URL_TOO_LONG(1003, "longUrl không được dài quá {max} ký tự", HttpStatus.BAD_REQUEST),
    URL_INVALID(1004, "longUrl phải bắt đầu bằng http:// hoặc https://", HttpStatus.BAD_REQUEST),
    URL_NOT_FOUND(1005, "Không tìm thấy liên kết", HttpStatus.NOT_FOUND),
    RESOURCE_NOT_FOUND(1006, "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
    INVALID_REQUEST_BODY(1007, "Nội dung request không đúng định dạng JSON", HttpStatus.BAD_REQUEST),
    ;

    int code;
    String message;
    HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}