CREATE TABLE click_events (
                              id         BIGINT        GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              short_code VARCHAR(7)    NOT NULL,
                              clicked_at TIMESTAMPTZ   NOT NULL DEFAULT now(),
                              ip_address VARCHAR(45),
                              user_agent VARCHAR(512),
                              referrer   VARCHAR(2048)
);

CREATE INDEX idx_click_events_short_code_clicked_at
    ON click_events (short_code, clicked_at);