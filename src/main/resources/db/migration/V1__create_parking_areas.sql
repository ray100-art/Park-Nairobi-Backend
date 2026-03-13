CREATE TABLE IF NOT EXISTS parking_areas (
                                             id            BIGINT          NOT NULL AUTO_INCREMENT,
                                             name          VARCHAR(100)    NOT NULL,
    address       VARCHAR(255)    NOT NULL,
    latitude      DECIMAL(10, 7)  NOT NULL,
    longitude     DECIMAL(10, 7)  NOT NULL,
    total_slots   INT             NOT NULL DEFAULT 0,
    active        BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

CREATE INDEX idx_parking_areas_location ON parking_areas (latitude, longitude);

INSERT INTO parking_areas (name, address, latitude, longitude, total_slots) VALUES
                                                                                ('Nairobi CBD Parking', 'Kimathi Street, Nairobi CBD', -1.2864, 36.8172, 5),
                                                                                ('Westlands Parking',   'Westlands Road, Westlands',   -1.2673, 36.8094, 3),
                                                                                ('Upper Hill Parking',  'Upper Hill Road, Upper Hill', -1.2967, 36.8219, 2);