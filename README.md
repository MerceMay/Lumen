<p align="center">
  <img src="logo.svg" width="128" alt="Lumen Logo"/>
</p>

# Lumen

Lumen is a native Android app for testing and selecting better Cloudflare edge IPs.

## Features

- Native Android implementation written in Kotlin
- Cloudflare IP range testing based on CloudflareSpeedTest behavior
- TCP latency testing and optional HTTP request testing
- Configurable test thread count, attempts, port, test URL, latency/loss/speed filters
- Local test history
- `ADD.txt` generation and upload to a compatible Worker admin endpoint
- Local `geoip.dat` support for country-based `ADD.txt` remarks, such as `#US1`, `#HK1`, and `#Unknown1`

## Tech Stack

- Kotlin
- Jetpack Compose Material 3
- Hilt
- Room
- DataStore
- WorkManager
- OkHttp

## Build

```bash
./gradlew testDebugUnitTest assembleDebug
```

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Project URL

```text
https://github.com/MerceMay/Lumen
```

## Credits

- [XIU2/CloudflareSpeedTest](https://github.com/XIU2/CloudflareSpeedTest)
- [cmliu/edgetunnel](https://github.com/cmliu/edgetunnel)
- [v2fly/geoip](https://github.com/v2fly/geoip)
- [Cloudflare](https://www.cloudflare.com)
