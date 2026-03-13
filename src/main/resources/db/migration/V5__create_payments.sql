CREATE TABLE IF NOT EXISTS payments (
                                        id                  BIGINT          NOT NULL AUTO_INCREMENT,
                                        booking_id          BIGINT          NOT NULL,
                                        user_id             BIGINT          NOT NULL,
                                        amount              DECIMAL(10,2)   NOT NULL,
    currency            VARCHAR(10)     NOT NULL DEFAULT 'KES',
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    payment_method      VARCHAR(30)     NOT NULL DEFAULT 'MPESA',
    mpesa_code          VARCHAR(50),
    mpesa_phone         VARCHAR(20),
    transaction_id      VARCHAR(100)    UNIQUE,
    paid_at             DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_booking
    FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
    );

CREATE INDEX idx_payments_booking     ON payments (booking_id);
CREATE INDEX idx_payments_user        ON payments (user_id);
CREATE INDEX idx_payments_status      ON payments (status);
CREATE INDEX idx_payments_mpesa_code  ON payments (mpesa_code);