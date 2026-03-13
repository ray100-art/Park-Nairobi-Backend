CREATE TABLE IF NOT EXISTS bookings (
                                        id                  BIGINT          NOT NULL AUTO_INCREMENT,
                                        user_id             BIGINT          NOT NULL,
                                        slot_id             VARCHAR(50)     NOT NULL,
    parking_area_id     BIGINT,
    status              VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    vehicle_plate       VARCHAR(20)     NOT NULL,
    check_in_time       DATETIME,
    check_out_time      DATETIME,
    reserved_at         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at          DATETIME,
    total_amount        DECIMAL(10,2)   DEFAULT 0.00,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_booking_user
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_slot
    FOREIGN KEY (slot_id) REFERENCES slots (slot_id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_area
    FOREIGN KEY (parking_area_id) REFERENCES parking_areas (id) ON DELETE SET NULL
    );

CREATE INDEX idx_bookings_user      ON bookings (user_id);
CREATE INDEX idx_bookings_slot      ON bookings (slot_id);
CREATE INDEX idx_bookings_status    ON bookings (status);
CREATE INDEX idx_bookings_reserved  ON bookings (reserved_at);
