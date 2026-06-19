<p align="center">
  <img src="logo.svg" width="128" alt="Lumen Logo"/>
</p>

<p align="center">
  <a href="README.md">English</a> | <b>简体中文</b>
</p>

<p align="center">
  <a href="https://github.com/MerceMay/Lumen/releases/latest"><img src="https://img.shields.io/github/v/release/MerceMay/Lumen?style=flat-square" alt="Release"/></a>
  <a href="https://github.com/MerceMay/Lumen/blob/main/LICENSE"><img src="https://img.shields.io/github/license/MerceMay/Lumen?style=flat-square" alt="License"/></a>
  <img src="https://img.shields.io/badge/Android-8.0%2B-brightgreen?style=flat-square&logo=android" alt="Android 8.0+"/>
  <img src="https://img.shields.io/badge/Kotlin-2.1-blue?style=flat-square&logo=kotlin" alt="Kotlin"/>
</p>

# Lumen

Lumen 是一个 Android 原生 Cloudflare 优选测速工具，配合 [edgetunnel](https://github.com/cmliu/edgetunnel) 使用。它从你的网络环境中找到延迟最低、速度最快的 Cloudflare 边缘节点 IP，并自动推送到你的 edgetunnel Worker。

## 工作流程

1. **延迟测速** — 并发 TCPing 数千个 Cloudflare IP，筛选延迟最低的节点
2. **下载测速** — 对延迟最优的候选 IP 进行实际带宽测试
3. **自动推送** — 测速完成后自动上传结果到你的 edgetunnel Worker 作为 `ADD.txt`

测速结果使用本地 GeoIP 数据按国家分组编号（如 `#CA1`、`#HK1`、`#Unknown1`）。

## 使用条件

- Android 8.0+（API 26+）
- 一个已部署在 Cloudflare 上的 [edgetunnel](https://github.com/cmliu/edgetunnel) Worker

## 使用方法

1. 从 [Releases](https://github.com/MerceMay/Lumen/releases) 下载安装 APK
2. 进入 **设置 → 推送**
3. 填入你的 edgetunnel 域名（如 `your-worker.example.com`）
4. 填入管理员密码
5. 返回首页运行测速 — 测速完成后会自动上传到 Worker

也可以在推送页手动上传，或在 **设置 → 高级设置** 中关闭自动上传。

## 配置说明

默认参数与 [CloudflareSpeedTest](https://github.com/XIU2/CloudflareSpeedTest) 对齐：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| 延迟测速线程 | 200 | 并发 TCPing 连接数 |
| 延迟测速次数 | 4 | 每个 IP 测试次数 |
| 测速端口 | 443 | 目标端口 |
| 优选数量 | 50 | 保留的结果数量 |
| 测试策略 | TCPing | TCPing 或 HTTPing |
| 自动上传 | 开启 | 测速完成后自动推送到 Worker |

所有参数均可在 **设置 → 高级设置** 中调整。

## 致谢

- [XIU2/CloudflareSpeedTest](https://github.com/XIU2/CloudflareSpeedTest) — 测速算法参考
- [cmliu/edgetunnel](https://github.com/cmliu/edgetunnel) — Worker 推送接口
- [v2fly/geoip](https://github.com/v2fly/geoip) — 本地 GeoIP 数据
- [Cloudflare](https://www.cloudflare.com) — 网络基础设施

## 许可证

MIT
