ALTER TABLE users ADD COLUMN token_version INT NOT NULL DEFAULT 0;

DELETE FROM users
WHERE email IN ('admin@carparking.com', 'driver@carparking.com')
  AND password_hash LIKE '$2a$10$placeholder%';
