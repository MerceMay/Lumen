<p align="center">
  <img src="logo.svg" width="128" alt="Lumen Logo"/>
</p>

# Lumen

Lumen 是一个 Android 原生 Cloudflare 优选测速工具，配合 [edgetunnel](https://github.com/cmliu/edgetunnel) 使用。它从你的网络环境中找到延迟最低、速度最快的 Cloudflare 边缘节点 IP，并将结果推送到你的 edgetunnel Worker 作为 `ADD.txt`。

## 工作流程

1. **延迟测速** — 并发 TCPing 约 6000 个 Cloudflare IP，筛选延迟最低的节点
2. **下载测速** — 对延迟最优的候选 IP 进行实际带宽测试
3. **推送到 Worker** — 将优选结果上传到你的 edgetunnel 管理面板

测速结果使用本地 GeoIP 数据按国家分组编号。

## 使用条件

- Android 8.0+（API 26+）
- 一个已部署在 Cloudflare 上的 [edgetunnel](https://github.com/cmliu/edgetunnel) Worker

## 使用方法

1. 从 [Releases](https://github.com/MerceMay/Lumen/releases) 下载安装 APK
2. 进入 **设置 → 推送**
3. 填入你的 edgetunnel 域名（如 `your-worker.example.com`）
4. 填入管理员密码
5. 返回首页运行测速，完成后进入推送页点击 **上传测速结果**

## 配置说明

默认参数与 [CloudflareSpeedTest](https://github.com/XIU2/CloudflareSpeedTest) 对齐：

| 参数 | 默认值 | 说明 |
|------|--------|------|
| 延迟测速线程 | 200 | 并发 TCPing 连接数 |
| 延迟测速次数 | 4 | 每个 IP 测试次数 |
| 测速端口 | 443 | 目标端口 |
| 优选数量 | 50 | 保留的结果数量 |
| 测试策略 | TCPing | TCPing 或 HTTPing |

所有参数均可在 **设置 → 高级设置** 中调整。

## 致谢

- [XIU2/CloudflareSpeedTest](https://github.com/XIU2/CloudflareSpeedTest) — 测速算法参考
- [cmliu/edgetunnel](https://github.com/cmliu/edgetunnel) — Worker 推送接口
- [v2fly/geoip](https://github.com/v2fly/geoip) — 本地 GeoIP 数据
- [Cloudflare](https://www.cloudflare.com) — 网络基础设施

## 许可证

MIT
