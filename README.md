# 原道音乐 Yuandao Music

原道音乐是一款专门为 HiFi 玩家打造的 Android 本地音乐播放器。

它专注于把本地高品质音乐播放做得简单、稳定、易上手：扫描本机音乐，建立曲库，搜索歌曲，加入播放队列，然后随时开始播放。界面保持简洁，不让复杂的设置和功能干扰听歌本身。

项目长期目标是支持大部分常见的无损音频格式，并逐步完善本地曲库、播放质量展示和高级输出能力。当前版本优先验证 WAV/FLAC 播放闭环，DSD、APE、CUE 和 USB DAC 独占输出会在后续阶段单独处理。

## 当前功能

- MediaStore 和 SAF 文件夹扫描。
- 本地歌曲、专辑、艺术家和音频格式信息入库。
- 按歌曲名、艺术家或专辑名搜索本地曲库。
- WAV/FLAC 播放、播放队列、循环、随机和进度控制。
- 后台播放、锁屏控制、通知栏控制和耳机拔出暂停。
- 最近播放记录、封面缓存和同名 `.lrc` 歌词显示。
- 简洁的深色界面、播放详情页和队列抽屉。

## 下载与安装

正式发布安装包后，请进入 [GitHub Releases](https://github.com/ynkh5gmgm5-ai/Music-player-for-hifi-/releases)，在最新版本的 **Assets** 中下载 `.apk` 文件，再安装到 Android 手机上。

当前仓库暂时还没有正式 Release 安装包。开发者可以从源码构建：

```powershell
git clone https://github.com/ynkh5gmgm5-ai/Music-player-for-hifi-.git
cd Music-player-for-hifi-\YuandaoMusic
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

构建出的 Debug APK 位于：

```text
YuandaoMusic/app/build/outputs/apk/debug/app-debug.apk
```

## 项目状态

当前处于本地播放 MVP 阶段：播放、队列、扫描、基础曲库搜索和自动化验证已经完成。由于开发环境暂时无法进行真机验证，WAV/FLAC 的设备播放、后台稳定性、通知栏和锁屏控制仍需要在 Android 模拟器或真机上复验。

## 项目目录

应用源码和完整开发文档位于 [`YuandaoMusic/`](YuandaoMusic/) 目录：

- [中文项目说明](YuandaoMusic/README.md)
- [项目状态](YuandaoMusic/PROJECT.md)
- [架构说明](YuandaoMusic/docs/ARCHITECTURE.md)
- [开发路线图](YuandaoMusic/docs/ROADMAP.md)
- [Android Studio 运行手册](YuandaoMusic/docs/ANDROID_STUDIO_RUNBOOK.md)

## 开源协议

本项目采用 [Apache License 2.0](YuandaoMusic/LICENSE) 开源。

Auxio 仅作为架构参考，项目没有复制 GPL 代码。
