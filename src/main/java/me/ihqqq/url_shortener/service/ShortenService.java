package me.ihqqq.url_shortener.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import me.ihqqq.url_shortener.dto.response.ShortenResponse;
import me.ihqqq.url_shortener.entity.Url;
import me.ihqqq.url_shortener.repository.UrlRepository;
import me.ihqqq.url_shortener.util.Base62;
import me.ihqqq.url_shortener.util.HashUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShortenService {

    UrlRepository urlRepository;
    IdGenerator idGenerator;

    @NonFinal
    @Value("${app.base-url}")
    String baseUrl;

    public ShortenResponse shorten(String rawLongUrl) {
        String longUrl = rawLongUrl.trim();
        String hash = HashUtil.sha256Hex(longUrl);

        Url url = urlRepository.findByLongUrlHash(hash)
                .orElseGet(() -> create(longUrl, hash));

        return toResponse(url);
    }

    private Url create(String longUrl, String hash) {
        long id = idGenerator.nextId();
        Url url = Url.builder()
                .id(id)
                .shortCode(Base62.encode(id))
                .longUrl(longUrl)
                .longUrlHash(hash)
                .build();
        try {
            return urlRepository.saveAndFlush(url);
        } catch (DataIntegrityViolationException e) {
            return urlRepository.findByLongUrlHash(hash).orElseThrow(() -> e);
        }
    }

    private ShortenResponse toResponse(Url url) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return new ShortenResponse(url.getShortCode(), base + "/" + url.getShortCode());
    }
}