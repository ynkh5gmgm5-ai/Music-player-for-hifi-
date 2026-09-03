# 原道音乐 Yuandao Music

原道音乐是一款专门为 HiFi 玩家打造的 Android 本地音乐播放器。

它专注于把本地高品质音乐播放这件事做得简单、稳定、易上手：扫描本机音乐，建立曲库，搜索歌曲，加入播放队列，然后随时开始播放。界面保持简洁，不让复杂的设置和功能干扰听歌本身。

项目的长期目标是支持大部分常见的无损音频格式，并逐步完善本地曲库、播放质量展示和高级输出能力。当前版本优先验证 WAV/FLAC 播放闭环，其他格式的实际支持取决于 Android 与 Media3 的解码能力；DSD、APE、CUE 和 USB DAC 独占输出会在后续阶段单独处理。

## 当前功能

- MediaStore 和 SAF 文件夹扫描。
- 本地歌曲、专辑、艺术家和音频格式信息入库。
- 按歌曲名、艺术家或专辑名搜索本地曲库。
- WAV/FLAC 播放及基础播放控制。
- 播放队列、上一首、下一首、进度拖动、循环和随机播放。
- 队列持久化和播放状态恢复。
- 后台播放、锁屏控制、通知栏控制和耳机拔出暂停。
- 最近播放记录和播放质量信息展示。
- 封面缓存、同名 `.lrc` 歌词解析和当前歌词显示。
- 深色玻璃质感界面、播放详情页和队列抽屉。

## 项目状态

当前处于本地播放 MVP 阶段：播放、队列、扫描、基础曲库搜索和自动化验证已经完成。由于当前开发环境无法进行真机验证，WAV/FLAC 的设备播放、后台稳定性、通知栏和锁屏控制仍需要在 Android 模拟器或真机上复验。

已通过：

```text
testDebugUnitTest
assembleDebug
lintDebug
```

## 如何下载安装包

### 普通用户：从 GitHub Releases 下载

正式发布安装包后，进入仓库的 [Releases](https://github.com/ynkh5gmgm5-ai/Music-player-for-hifi-/releases) 页面，在最新版本的 **Assets** 中下载 `.apk` 文件，然后在 Android 手机上安装。

当前仓库刚刚建立，还没有正式 Release 安装包，因此暂时不会在 Releases 页面看到 APK。后续建议发布签名的 `app-release.apk`，不要把仅用于开发测试的 Debug APK 当作正式版本分发。

### 开发者：从源码构建

```powershell
git clone https://github.com/ynkh5gmgm5-ai/Music-player-for-hifi-.git
cd Music-player-for-hifi-
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug
```

构建成功后，Debug APK 位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

也可以使用 Android Studio 打开项目，等待 Gradle 同步完成后运行或构建 APK。

## 技术栈

- Kotlin
- Jetpack Compose
- Media3 ExoPlayer
- Room + KSP
- MediaStore + Storage Access Framework（SAF）

## 文档

- [项目状态](PROJECT.md)
- [架构说明](docs/ARCHITECTURE.md)
- [开发路线图](docs/ROADMAP.md)
- [Android Studio 运行手册](docs/ANDROID_STUDIO_RUNBOOK.md)
- [WAV/FLAC 验证清单](docs/manual-verification/wav-flac-local-playback.md)
- [运行时问题模板](docs/manual-verification/runtime-issue-template.md)

Auxio 仅作为架构参考，项目没有复制 GPL 代码。

## 开源协议

本项目采用 [Apache License 2.0](LICENSE) 开源。

## 构建要求

- JDK 17
- Android SDK 35
- 首次同步需要网络访问 Gradle 依赖
