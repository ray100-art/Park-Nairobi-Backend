CREATE TABLE IF NOT EXISTS users (
                                     id                BIGINT          NOT NULL AUTO_INCREMENT,
                                     full_name         VARCHAR(100)    NOT NULL,
    email             VARCHAR(150)    NOT NULL UNIQUE,
    phone             VARCHAR(20)     NOT NULL UNIQUE,
    password_hash     VARCHAR(255)    NOT NULL,
    role              VARCHAR(20)     NOT NULL DEFAULT 'DRIVER',
    active            BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
    );

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_phone ON users (phone);
CREATE INDEX idx_users_role  ON users (role);

INSERT INTO users (full_name, email, phone, password_hash, role) VALUES
                                                                     ('Admin User',   'admin@carparking.com',  '+254700000001', '$2a$10$placeholder_admin_hash',  'ADMIN'),
                                                                     ('Test Driver',  'driver@carparking.com', '+254700000002', '$2a$10$placeholder_driver_hash', 'DRIVER');
