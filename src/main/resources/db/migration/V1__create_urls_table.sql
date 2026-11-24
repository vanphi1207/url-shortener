CREATE TABLE urls (
                      id            BIGINT       PRIMARY KEY,
                      short_code    VARCHAR(7)   NOT NULL,
                      long_url      TEXT         NOT NULL,
                      long_url_hash VARCHAR(64)  NOT NULL,
                      created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
                      CONSTRAINT uq_urls_short_code    UNIQUE (short_code),
                      CONSTRAINT uq_urls_long_url_hash UNIQUE (long_url_hash)
);