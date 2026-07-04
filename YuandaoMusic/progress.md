# 原道音乐播放器项目进度交接

最后更新：2026-07-02  
项目目录：`D:\我的文档\Documents\YUANDAO\YuandaoMusic`

## 1. 当前阶段结论

当前项目处在 **Android 本地 HiFi 播放器第一阶段**：先把本地 WAV / FLAC 扫描、入库、播放、队列、后台播放、播放页信息联动做扎实。  

Linux 端、USB DAC 独占输出、云端音乐库、流媒体服务、DSD / APE / CUE 等高级或商业依赖较强的功能，已明确后置。现阶段不追求一次做满，而是先保证 Android 本地播放体验稳定、结构可扩展、后续可以继续接云和多端。

视觉方向仍以用户提供的参考图为最终目标，但当前 UI 只是功能型第一版，不是 1:1 复刻版本。Figma 暂时不是必须，后续进入高保真 UI 复刻阶段再使用会更合适。

## 2. 已完成内容

### 2.1 工程与技术路线

- 已新建干净 Android 工程：`YuandaoMusic`。
- 技术栈确定为：Kotlin + Jetpack Compose + Media3 + Room + MediaStore / SAF。
- 许可证选择：Apache-2.0。
- Auxio 只作为架构和本地播放能力参考，没有把 GPL 代码复制进当前工程，避免 GPL 传染风险。
- 已预留多数据源模型，当前主做 `LOCAL`，后续可扩展 `CLOUD`、`STREAMING`。

### 2.2 本地音乐扫描与数据库

- 已实现 MediaStore 扫描。
- 已实现 SAF 目录扫描基础能力。
- 已建立 Room 数据层，包含歌曲、专辑、歌手、库目录、播放状态、队列、播放历史等结构。
- 已能读取本地音乐的基础元数据：标题、歌手、专辑、时长、路径、文件格式等。
- 已实现封面读取与缓存基础能力。
- 当前优先兼容 WAV / FLAC。
- MP3 / AAC / ALAC / OGG / OPUS 具备第一版识别或播放路径，但不是当前重点。
- DSD / APE / CUE 已明确后置。

### 2.3 播放核心

- 已接入 Media3 ExoPlayer。
- 已实现基础播放队列。
- 已实现播放 / 暂停 / 上一首 / 下一首。
- 已实现 seek 进度控制。
- 已实现循环模式。
- 已实现随机播放，并采用重新洗牌后的随机队列，同时保留当前播放项。
- 已实现队列持久化与断点恢复基础能力。
- 已实现最近播放 / 播放历史的数据结构与基础逻辑。

### 2.4 后台播放与系统行为

- 已实现 `MediaSessionService` 方向的后台播放服务。
- 已实现播放通知所需的服务基础。
- 已处理 Android 15 前台服务启动约束问题：
  - 问题表现：`ForegroundServiceDidNotStartInTimeException`
  - 原因：调用 `startForegroundService()` 后服务没有及时进入前台
  - 已加入启动策略与 bootstrap notification
- 已实现音频焦点策略：
  - 长时间失焦暂停
  - 短暂失焦暂停或 duck
  - 恢复焦点后按策略恢复
- 已实现耳机 / 输出设备断开时暂停的基础处理。

### 2.5 播放状态与 UI 联动

- 已建立播放器状态到 Compose UI 的联动。
- 已实现首页基础 UI：
  - 本地曲库入口
  - 继续播放卡片
  - 最近添加
  - 常听歌手
  - Hi-Res / 无损格式统计
  - 资料库概览
  - 底部 mini player
- 已实现播放详情页 / now-playing modal sheet 第一版。
- 已实现队列抽屉 / 队列页面的数据结构和第一版展示。
- 播放时可展示当前音频信息：
  - 格式
  - 采样率
  - 位深，取决于系统 / Media3 能否提供
  - decoder 名称，调试路径中可见

### 2.6 歌词

- 已实现同名 `.lrc` 歌词解析基础能力。
- 已能根据播放进度显示当前歌词行。
- 内嵌歌词读取仍未完成，属于后续增强项。

### 2.7 测试与验证

- 已添加多组单元测试，覆盖模型、队列、随机策略、歌词解析、播放状态、数据库迁移、主题策略、播放服务启动策略等。
- 曾完成一次完整 Gradle 验证：
  - `testDebugUnitTest`
  - `assembleDebug`
  - `lintDebug`
  - 结果：`BUILD SUCCESSFUL`
- 在 API 35 模拟器上做过实播验证：
  - 模拟器：`emulator-5554`
  - 包名：`com.yuandao.music`
  - 推入测试文件：
    - `F:\李荣浩-慢冷(Live).flac`
    - `F:\07.漂洋过海来看你—李宗盛.wav`
  - MediaStore 能识别 FLAC 和 WAV。
  - App 扫描后显示 2 首歌曲。
  - WAV 可播放。
  - FLAC 可播放。
  - `dumpsys media_session` 显示播放状态为 `PLAYING`。
  - FLAC 播放时 logcat 可见 `c2.android.flac.decoder`，采样率 44100，双声道，输出 `audio/raw`。
  - 截图中已能看到 FLAC、16-bit / 44.1kHz 等信息。

### 2.8 已修复的重要问题

- 修复深色主题文字对比度问题：
  - 原因：`MaterialTheme` 未设置全局 `LocalContentColor`，导致深色背景上出现黑字。
  - 修复位置：`app/src/main/java/com/yuandao/music/ui/theme/Theme.kt`
  - 已添加 `YuandaoThemePolicyTest`。
- 修复 Android 15 前台服务延迟崩溃问题：
  - 新增 `PlaybackServiceStartupPolicy`
  - 服务 `onCreate` 中创建通知渠道并进入前台
  - 新增 `PlaybackServiceStartupPolicyTest`
  - 针对该测试已通过。

## 3. 关键决策

- 当前目标不是“马上做商业闭源产品”，而是先做一个可验证、可扩展的开源 Android 本地 HiFi 播放器原型。
- 项目许可证选择 Apache-2.0，保持后续商业路线和开源协作空间。
- Auxio 只参考思路，不复制 GPL 代码。
- 第一阶段优先级：
  1. 本地 WAV / FLAC 扫描与播放稳定
  2. 播放队列、随机、循环、断点续播稳定
  3. 播放状态与 UI 联动稳定
  4. 后台播放、通知、音频焦点稳定
  5. 播放详情页信息足够丰富
- 云端和流媒体先做数据模型预留，不进入真实接入。
- Linux 端后置。
- USB DAC 独占输出后置，并作为独立重点模块处理。
- DSD / APE / CUE 后置，避免早期复杂度失控。
- 视觉最终要向用户参考图靠拢，但在播放体验稳定前不进入 1:1 UI 复刻。

## 4. 当前未完成事项

### 4.1 最高优先级：复测前台服务修复

前台服务崩溃修复后，只跑过 targeted 单测：

`.\gradlew.bat testDebugUnitTest --tests com.yuandao.music.playback.PlaybackServiceStartupPolicyTest --stacktrace`

结果：`BUILD SUCCESSFUL`

但还没有完成以下验证：

- 重新跑完整构建：
  - `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`
- 重新安装到模拟器。
- 重新做 WAV / FLAC 实播。
- 验证后台播放不会再触发 `ForegroundServiceDidNotStartInTimeException`。
- 验证按 Home、锁屏、切后台后播放服务稳定。
- 验证通知栏上一首、下一首、播放 / 暂停、关闭服务行为。

这是下个窗口最应该先做的事。

### 4.2 播放页与队列页需要继续完善

- 播放详情页还需要继续打磨：
  - 大封面
  - 当前曲目信息
  - 采样率 / 位深 / 格式展示
  - 播放进度
  - 歌词区域
  - 播放模式切换
- 队列页 / 队列抽屉需要继续验证：
  - 当前播放列表
  - 下一首列表
  - 随机队列顺序
  - 拖拽排序可后置
  - 删除队列项可后置
- 底部 mini player 目前可能遮挡列表底部内容，需要补底部安全 padding。

### 4.3 通知栏与媒体会话

- 通知栏动作需要完整手测：
  - 上一首
  - 下一首
  - 播放 / 暂停
  - 关闭服务
- 需要确认通知样式在 Android 13 / 14 / 15 上表现一致。
- 需要确认媒体会话在系统媒体控件中能正确显示标题、歌手、封面、播放状态。

### 4.4 音频焦点与外设行为

- 需要手测：
  - 其他 App 抢占音频焦点
  - 短暂失焦
  - duck
  - 恢复焦点
  - 耳机拔出暂停
- 来电暂停暂时不做，这是已确认决策。

### 4.5 数据与格式增强

- 内嵌歌词读取未完成。
- 更完整的位深识别仍需增强，部分信息依赖 Android 解码链路能否提供。
- ALAC / APE / DSD / CUE 暂不作为近期目标。
- 需要继续用 `F:\` U 盘中的真实音乐文件做格式和元数据压力测试。

### 4.6 UI 与视觉

- 当前 UI 是功能型第一版，不是参考图 1:1。
- 后续需要统一：
  - 字体层级
  - 间距
  - 圆角
  - 深色玻璃质感
  - 卡片组件
  - 图标体系
  - 播放状态颜色
  - 中文文案编码与排版
- 进入视觉阶段前，建议先完成播放体验闭环，否则 UI 复刻后返工成本会很高。

## 5. 下一个窗口建议执行顺序

1. 读取本文件和 `docs/PROJECT_STATUS.md`。
2. 先跑完整构建：
   - `.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`
3. 如果构建通过，安装到 API 35 模拟器。
4. 重新推入 WAV / FLAC 测试文件，验证扫描和实播。
5. 重点观察前台服务修复是否彻底解决后台播放崩溃。
6. 手测通知栏动作和系统媒体会话。
7. 修复 mini player 遮挡底部内容的问题。
8. 继续完善播放详情页和队列页。
9. 每完成一个阶段，都同步更新本文件。

## 6. 用户可协助的地方

- 提供真实 Android 设备时，可做真机播放和耳机 / DAC 测试。
- `F:\` U 盘可继续作为真实音乐样本库。
- 如果需要更精确复刻视觉，后续可提供 Figma 文件或更细的设计稿。
- 如果遇到模拟器无法覆盖的问题，再请用户配合真机截图、录屏或导出 logcat。

## 7. 重要文件索引

- 项目状态：`YuandaoMusic/docs/PROJECT_STATUS.md`
- 阶段路线：`YuandaoMusic/docs/ROADMAP.md`
- Android Studio 运行说明：`YuandaoMusic/docs/ANDROID_STUDIO_RUNBOOK.md`
- WAV / FLAC 手测流程：`YuandaoMusic/docs/manual-verification/wav-flac-local-playback.md`
- 架构说明：`YuandaoMusic/docs/ARCHITECTURE.md`
- 参考图：`YuandaoMusic/参考图.png`

