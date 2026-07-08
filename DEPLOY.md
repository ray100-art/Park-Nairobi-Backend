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

## Option 2 — Free path: Netlify (frontend) + Railway (backend)

Best free-ish split for this project (no VPS).

| Part | Host | Cost |
|------|------|------|
| Frontend | [Netlify](https://www.netlify.com) | Free plan |
| Backend + MySQL | [Railway](https://railway.app) | Free trial (~$5 credit), then limited free / Hobby |

> Railway is not forever-free for always-on apps. Use the trial to demo; expect ~$5/month Hobby later if you keep it online.

### A. Deploy backend first (Railway)

1. Sign up at [railway.app](https://railway.app) with GitHub.
2. **New Project** → **Deploy from GitHub repo** → select `Park-Nairobi-Backened`.
3. Railway should detect the `Dockerfile` and build.
4. In the same project: **+ New** → **Database** → **MySQL**.
5. Open the **API service** → **Variables** → add:

| Variable | Value |
|----------|-------|
| `DB_URL` | `jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | `${{MySQL.MYSQLUSER}}` |
| `DB_PASSWORD` | `${{MySQL.MYSQLPASSWORD}}` |
| `JWT_SECRET` | random string, 32+ characters |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `MPESA_CONSUMER_KEY` | from Daraja |
| `MPESA_CONSUMER_SECRET` | from Daraja |
| `MPESA_PASSKEY` | sandbox passkey |
| `CORS_ALLOWED_ORIGINS` | *(set after Netlify — see step C)* |
| `MPESA_CALLBACK_URL` | *(set after public domain — see step C)* |

> Variable names for MySQL references may differ slightly in Railway’s UI — use the **Variable Reference** picker from the MySQL service if the names above don’t autocomplete.

6. **Settings** → **Networking** → **Generate Domain** to get something like `https://xxx.up.railway.app`.
7. Wait until deploy succeeds. Test: `https://xxx.up.railway.app/api/auth/health`

### B. Deploy frontend (Netlify)

1. Sign up at [netlify.com](https://www.netlify.com) with GitHub.
2. **Add new site** → **Import an existing project** → `Park-Nairobi-Fronted`.
3. Build settings (also in `netlify.toml`):
   - **Build command:** `node build-config.js`
   - **Publish directory:** `.` (repo root)
4. Before first deploy, add environment variable:
   - **Key:** `API_BASE`
   - **Value:** your Railway URL, e.g. `https://xxx.up.railway.app` *(no trailing slash)*
5. Deploy. You get a URL like `https://something.netlify.app`.

### C. Connect them (required)

On **Railway** API variables, set:

```text
CORS_ALLOWED_ORIGINS=https://something.netlify.app
MPESA_CALLBACK_URL=https://xxx.up.railway.app/api/mpesa/callback
```

Redeploy the API (or wait for auto-redeploy). Then open the Netlify site and register/login.

> WebSockets: frontend talks to `API_BASE + '/ws'` over HTTPS → SockJS uses secure connections automatically.

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
