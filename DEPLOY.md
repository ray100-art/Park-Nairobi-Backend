# Deploying ParkNairobi

ParkNairobi is a full-stack app:

| Part | Tech | Repo |
|------|------|------|
| Frontend | Static HTML/JS + nginx | `car-parking-ui` |
| Backend | Spring Boot 3 + MySQL | `car-parking` |

---

## Option 1 — Docker on a VPS (recommended)

Deploy everything on one server (DigitalOcean, AWS EC2, Hetzner, etc.) with Docker.

### Prerequisites

- A Linux VPS (Ubuntu 22.04+)
- Docker & Docker Compose installed
- Both repos cloned as siblings:

```
MyProjects/
├── car-parking/        ← backend (this repo)
└── car-parking-ui/     ← frontend
```

### Steps

**1. Clone both repos on the server**

```bash
git clone https://github.com/ray100-art/Park-Nairobi-Backened.git car-parking
git clone https://github.com/ray100-art/Park-Nairobi-Fronted.git car-parking-ui
```

**2. Configure environment**

```bash
cd car-parking
cp .env.docker.example .env
nano .env   # set DB_PASSWORD, JWT_SECRET, SITE_URL, M-Pesa keys
```

Update `CORS_ALLOWED_ORIGINS` and `MPESA_CALLBACK_URL` to your real domain once you have one.

**3. Build and start**

```bash
docker compose up -d --build
```

**4. Open the site**

Visit `http://YOUR_SERVER_IP` — the frontend proxies `/api` and `/ws` to the backend automatically.

**5. Add HTTPS (production)**

Point your domain's A record to the server IP, then install Caddy or Certbot:

```bash
# Example with Caddy (simplest)
sudo apt install caddy
# Edit /etc/caddy/Caddyfile:
# yourdomain.com {
#     reverse_proxy localhost:80
# }
sudo systemctl reload caddy
```

Then update `.env`:
```
SITE_URL=https://yourdomain.com
CORS_ALLOWED_ORIGINS=https://yourdomain.com
MPESA_CALLBACK_URL=https://yourdomain.com/api/mpesa/callback
```

Restart: `docker compose up -d`

### Useful commands

```bash
docker compose logs -f api      # backend logs
docker compose logs -f frontend # nginx logs
docker compose down             # stop all services
docker compose up -d --build    # rebuild after code changes
```

---

## Option 2 — Split hosting (Netlify + Railway)

Use this if you don't want to manage a VPS.

### Frontend → Netlify (free)

1. Push `car-parking-ui` to GitHub
2. Go to [netlify.com](https://netlify.com) → **Add new site** → Import from Git
3. Build settings: leave blank (static site, no build step)
4. Publish directory: `/` (root)
5. Add a redirect/proxy is NOT needed — set the API URL instead

Before each HTML page loads `api.js`, inject your backend URL. Add this to every HTML file's `<head>`:

```html
<script>window.__API_BASE__ = 'https://YOUR-BACKEND-URL';</script>
<script src="js/config.js"></script>
```

Or edit `js/config.js` directly with your production API URL.

### Backend → Railway (or Render)

1. Push `car-parking` to GitHub
2. Go to [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Add a **MySQL** plugin and note the connection URL
4. Set environment variables:

| Variable | Value |
|----------|-------|
| `DB_URL` | Railway MySQL JDBC URL |
| `DB_USERNAME` | from Railway |
| `DB_PASSWORD` | from Railway |
| `JWT_SECRET` | random 32+ char string |
| `CORS_ALLOWED_ORIGINS` | `https://your-netlify-site.netlify.app` |
| `MPESA_CALLBACK_URL` | `https://your-railway-app.up.railway.app/api/mpesa/callback` |
| `SPRING_PROFILES_ACTIVE` | `prod` |

5. Railway auto-detects the Dockerfile and deploys on port 8080

> **Note:** WebSockets work on Railway/Render. Make sure the frontend uses `wss://` (automatic when `__API_BASE__` is `https://...`).

---

## Environment variables reference

| Variable | Required | Description |
|----------|----------|-------------|
| `DB_URL` | Yes | MySQL JDBC connection string |
| `DB_USERNAME` | Yes | Database user |
| `DB_PASSWORD` | Yes | Database password |
| `JWT_SECRET` | Yes | Min 32 characters |
| `CORS_ALLOWED_ORIGINS` | Yes (prod) | Comma-separated frontend URLs |
| `MPESA_CONSUMER_KEY` | For payments | Safaricom Daraja key |
| `MPESA_CONSUMER_SECRET` | For payments | Safaricom Daraja secret |
| `MPESA_PASSKEY` | For payments | Lipa na M-Pesa passkey |
| `MPESA_CALLBACK_URL` | For payments | Public HTTPS callback URL |
| `PARKING_FEE_AMOUNT` | No | Default KES 50 |

---

## Verify deployment

1. Open the site → register a new account
2. Log in → dashboard loads nearby slots
3. Check API health: `GET https://your-domain/api/auth/health`
4. Admin panel → WebSocket dot turns green (live updates)

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| CORS errors in browser | Set `CORS_ALLOWED_ORIGINS` to exact frontend URL (no trailing slash) |
| WebSocket won't connect | Ensure `/ws` is proxied with Upgrade headers (nginx config handles this) |
| M-Pesa callback fails | `MPESA_CALLBACK_URL` must be public HTTPS |
| DB connection refused | Wait for MySQL healthcheck; check `DB_URL` host is `db` in Docker |
| 502 on API | Check `docker compose logs api` for startup errors |
