package me.ihqqq.url_shortener.repository;

import me.ihqqq.url_shortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {

    // Dùng cho luồng redirect: tra link dài từ mã ngắn
    Optional<Url> findByShortCode(String shortCode);

    // Dùng cho luồng tạo link: kiểm tra link dài đã được rút gọn chưa
    Optional<Url> findByLongUrlHash(String longUrlHash);
}