CREATE TABLE IF NOT EXISTS slots (
                                     slot_id                  VARCHAR(50)     NOT NULL,
    label                    VARCHAR(100)    NOT NULL,
    parking_area_name        VARCHAR(100)    NOT NULL,
    parking_area_id          BIGINT,
    status                   VARCHAR(20)     NOT NULL DEFAULT 'FREE',
    latitude                 DECIMAL(10, 7)  NOT NULL,
    longitude                DECIMAL(10, 7)  NOT NULL,
    floor                    INT             NOT NULL DEFAULT 0,
    reservation_expires_at   DATETIME,
    reserved_by_driver_id    VARCHAR(100),
    created_at               DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (slot_id),
    CONSTRAINT fk_slot_area FOREIGN KEY (parking_area_id) REFERENCES parking_areas (id) ON DELETE SET NULL
    );

CREATE INDEX idx_slots_status   ON slots (status);
CREATE INDEX idx_slots_location ON slots (latitude, longitude);
CREATE INDEX idx_slots_area     ON slots (parking_area_id);