<p align="center">
  <img src="logo.svg" width="128" alt="Lumen Logo"/>
</p>

# Lumen

[中文文档](README_CN.md)

Lumen is a native Android app for Cloudflare edge IP optimization. It finds the fastest Cloudflare IPs from your network and pushes them to your [edgetunnel](https://github.com/cmliu/edgetunnel) Worker as `ADD.txt`.

## How It Works

1. **Latency Test** — Tests thousands of Cloudflare IPs concurrently via TCPing
2. **Download Speed Test** — Measures actual bandwidth on the best candidates
3. **Push to Worker** — Uploads results to your edgetunnel admin panel

Results are grouped by country code using local GeoIP data.

## Requirements

- Android 8.0+ (API 26+)
- An [edgetunnel](https://github.com/cmliu/edgetunnel) Worker deployed on Cloudflare

## Setup

1. Install the APK from [Releases](https://github.com/MerceMay/Lumen/releases)
2. Go to **Settings → Push**
3. Enter your edgetunnel domain (e.g. `your-worker.example.com`)
4. Enter your admin password
5. Run a speed test, then tap **Upload Results**

## Configuration

Default settings match [CloudflareSpeedTest](https://github.com/XIU2/CloudflareSpeedTest):

| Parameter | Default | Description |
|-----------|---------|-------------|
| Test threads | 200 | Concurrent TCPing connections |
| Test attempts | 4 | Pings per IP |
| Port | 443 | Target port |
| Result count | 50 | Number of IPs to keep |
| Strategy | TCPing | TCPing or HTTPing |

All parameters are configurable in **Settings → Advanced**.

## Build

```bash
./gradlew assembleDebug
```

## Credits

- [XIU2/CloudflareSpeedTest](https://github.com/XIU2/CloudflareSpeedTest) — Speed test algorithm reference
- [cmliu/edgetunnel](https://github.com/cmliu/edgetunnel) — Worker push interface
- [v2fly/geoip](https://github.com/v2fly/geoip) — Local GeoIP data
- [Cloudflare](https://www.cloudflare.com) — Network infrastructure

## License

MIT
