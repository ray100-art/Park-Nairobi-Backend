# ParkNairobi Backend

REST API behind **ParkNairobi**, a smart-parking platform for Kenyan towns: real-time bay
availability, GPS-based parking search, bookings, M-Pesa payments and IoT sensor events.
The web client lives in [Park-Nairobi-Frontend](https://github.com/ray100-art/Park-Nairobi-Frontend)
([live demo](https://ray100-art.github.io/Park-Nairobi-Frontend/)).

## Highlights

- **Stateless JWT auth with role-based access.** `DRIVER` and `ADMIN` roles are enforced both in
  the security filter chain and with `@PreAuthorize` on admin operations.
- **Payments that survive real networks.** M-Pesa STK Push, with a callback that is
  authenticated (shared secret, or Safaricom IP ranges) and **idempotent**: a payment settles
  only from `PENDING`, so repeated callbacks cannot double-apply. The paid amount is checked
  against the booking's expected price.
- **Live updates.** Slot changes are broadcast over STOMP WebSockets, with JWT checks on the
  WebSocket channel as well.
- **Versioned schema.** Flyway migrations (`V1`–`V8`) own the database structure.
- **Hardening.** Request rate limiting, CORS configuration, and no credentials in code.
- **Deployable.** Dockerfile and `docker-compose.yml`; see [DEPLOY.md](DEPLOY.md).

## Tech Stack

- **Java 21** · Spring Boot 3.3
- **MySQL 8** · Spring Data JPA · Flyway migrations
- **Spring Security** · JWT authentication
- **WebSocket** · STOMP real-time updates
- **M-Pesa Daraja API** · STK Push payments

## Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8.0 running locally
- Safaricom Daraja API credentials (sandbox or production)

## Setup

**1. Create the database**
```sql
CREATE DATABASE car_parking;
```

**2. Set environment variables**

Copy `.env.example` to `.env` and fill in your values:
```bash
DB_USERNAME=root
DB_PASSWORD=your_db_password
JWT_SECRET=your_jwt_secret_min_32_chars
MPESA_CONSUMER_KEY=your_safaricom_consumer_key
MPESA_CONSUMER_SECRET=your_safaricom_consumer_secret
MPESA_CALLBACK_URL=https://your-domain.com/api/mpesa/callback
```

**3. Run the app**
```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`. Flyway runs migrations automatically on first start.

## API Reference

### Auth — `/api/auth`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/health` | No | Service health check |
| POST | `/register` | No | Register new user |
| POST | `/login` | No | Login, returns JWT token |

**Register request body:**
```json
{
  "fullName": "Jane Doe",
  "email": "jane@example.com",
  "phone": "0712345678",
  "password": "Secret123"
}
```

**Login response:**
```json
{
  "token": "eyJhbGci...",
  "user": { "id": 1, "email": "jane@example.com", "role": "DRIVER" }
}
```

---

### Parking Slots — `/api/slots`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | No | All slots |
| GET | `/nearby?lat=&lon=&radius=` | No | Slots within radius (km) |
| GET | `/summary` | No | Count by status (FREE / OCCUPIED / RESERVED) |
| PUT | `/{slotId}/free` | Admin | Manually free a slot |

---

### Bookings — `/api/bookings`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | Yes | Create a booking (reserves slot for 15 min) |
| GET | `/my` | Yes | Current user's bookings |
| DELETE | `/{id}` | Yes | Cancel a booking |
| GET | `/all` | Admin | All bookings |

**Create booking request body:**
```json
{
  "slotId": "LOT_A-01",
  "vehiclePlate": "KCA 123A"
}
```

---

### M-Pesa Payments — `/api/mpesa`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/pay` | Yes | Initiate STK Push to customer phone |
| POST | `/callback` | Callback secret or Safaricom IP | Safaricom payment callback (webhook) |
| GET | `/status/{checkoutId}` | Yes | Poll payment status |

**Pay request body:**
```json
{
  "bookingId": 1,
  "phone": "0712345678",
  "amount": 150
}
```

---

### Location — `/api/location`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/nearest-areas?lat=&lon=&radius=&max=` | No | Nearest parking areas |
| GET | `/nearest-slots?lat=&lon=&radius=&max=` | No | Nearest free slots |
| GET | `/distance?lat1=&lon1=&lat2=&lon2=` | No | Distance between two GPS points (km) |

---

### Sensors — `/api/sensor`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/entry` | Admin | Car entered bay → marks slot OCCUPIED |
| POST | `/exit` | Admin | Car exited bay → marks slot FREE |
| GET | `/status/{slotId}` | Admin | Current slot status |

---

### Profile — `/api/profile`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/` | Yes | Get current user profile |
| PUT | `/` | Yes | Update name and phone |
| PUT | `/password` | Yes | Change password |

---

### Admin — `/api/admin`

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/users` | Admin | List all users |

---

## Authentication

Include the JWT token in every authenticated request:
```
Authorization: Bearer <token>
```

Tokens expire after 1 hour by default (`JWT_EXPIRATION`, in milliseconds).

## Real-Time Updates (WebSocket)

Connect to `ws://localhost:8080/ws` using STOMP over SockJS, then subscribe to `/topic/slots` to receive live slot status updates whenever a slot is reserved, occupied, or freed.

```javascript
const client = new StompJs.Client({ brokerURL: 'ws://localhost:8080/ws' });
client.onConnect = () => client.subscribe('/topic/slots', msg => console.log(msg.body));
client.activate();
```

## Database Schema

| Table | Description |
|-------|-------------|
| `parking_areas` | Parking lot definitions with GPS coordinates |
| `slots` | Individual bays linked to a parking area |
| `users` | User accounts (DRIVER / ADMIN roles) |
| `bookings` | Reservation records |
| `payments` | M-Pesa transaction records |

Migrations are managed by Flyway (`src/main/resources/db/migration/`).

## Project Structure

```
src/main/java/com/carparking/
├── algorithm/        # Haversine distance, slot sorting
├── config/           # Security, JWT filter, WebSocket, rate limiting
├── model/            # JPA entities
└── service/          # Controllers, services, repositories
```

## Running Tests

```bash
mvn test
```

Unit tests cover the Haversine distance calculations and the parking-slot service.

## License

[MIT](LICENSE)
