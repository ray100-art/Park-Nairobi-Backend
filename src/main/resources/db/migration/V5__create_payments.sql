CREATE TABLE IF NOT EXISTS payments (
    id                    BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id            BIGINT          NOT NULL,
    checkout_request_id   VARCHAR(100)    UNIQUE,
    mpesa_receipt_number  VARCHAR(50),
    phone                 VARCHAR(20),
    amount                DECIMAL(10,2),
    status                VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    failure_reason        VARCHAR(255),
    created_at            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at          DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE
);

CREATE INDEX idx_payments_booking        ON payments (booking_id);
CREATE INDEX idx_payments_status         ON payments (status);
CREATE INDEX idx_payments_checkout       ON payments (checkout_request_id);
