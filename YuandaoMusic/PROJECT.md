# 原道音乐播放器 - 项目总览

最后更新：2026-07-03
替换文件：`progress.md`、`docs/PROJECT_STATUS.md`（本文件整合后两者可归档）

---

## 1. 项目定位与当前阶段

**原道音乐** 是一个 Android 本地 HiFi 音乐播放器。当前处于 **第一阶段：本地 WAV/FLAC 播放闭环验证**。

核心目标：本地扫描 → 入库 → 播放 → 队列 → 后台播放 → UI 联动，这一整条链路稳定可验证。
云端、流媒体、Linux、USB DAC 独占输出、DSD/APE/CUE 均已明确后置。

视觉方向以用户参考图为最终目标，但当前 UI 是功能型第一版，不是 1:1 复刻。

---

## 2. 技术栈与许可证

| 项 | 选择 |
| --- | --- |
| 语言 | Kotlin |
| UI | Jetpack Compose |
| 播放引擎 | Media3 ExoPlayer |
| 数据库 | Room + KSP |
| 扫描 | MediaStore + SAF |
| 许可证 | Apache-2.0 |

**GPL 边界**：Auxio 仅作为架构参考，GPL 代码未复制进本工程。

---

## 3. 关键决策

1. **先稳定再扩展**：本地 WAV/FLAC 播放体验不扎实之前，不进入新格式、新平台、新输出模式。
2. **多数据源预留**：数据模型已有 `LOCAL` / `CLOUD` / `STREAMING`，但当前只激活 `LOCAL`。
3. **视觉后置**：UI 复刻在播放体验闭环之后做，避免返工。
4. **DSD/APE/CUE 后置**：作为独立模块处理，避免早期复杂度失控。
5. **来电暂停不做**：已确认决策。

---

## 4. 功能完成度矩阵

状态标记：✅ 完成  🔶 部分完成  ⬜ 待验证  ⏸ 后置

| 领域 | 功能 | 状态 | 备注 |
| --- | --- | --- | --- |
| **工程** | Android 项目脚手架 | ✅ | Gradle + Compose + Media3 + Room + KSP |
| | 许可证 | ✅ | Apache-2.0 |
| **扫描** | MediaStore 扫描 | ✅ | 元数据入库 |
| | SAF 目录扫描 | ✅ | 基础能力已实现 |
| | 曲目/专辑/歌手数据库 | ✅ | Room 聚合视图 |
| | 封面提取与缓存 | ✅ | 更多格式需要实机样本测试 |
| **播放** | WAV/FLAC 播放 | ✅ | API 35 模拟器实播通过 |
| | MP3/AAC/ALAC/OGG/OPUS | 🔶 | Media3 可处理，非当前重点 |
| | DSD/APE/CUE | ⏸ | 后置 |
| | 播放/暂停/上首/下首 | ✅ | |
| | Seek 进度控制 | ✅ | |
| | 循环模式 | ✅ | |
| | 随机播放 | ✅ | 洗牌队列 + 保留当前曲目 |
| | 队列持久化与断点恢复 | ✅ | |
| | 最近播放/播放历史 | ✅ | |
| **后台** | MediaSessionService | ✅ | |
| | 前台通知 | ✅ | Android 15 启动策略已修复 |
| | 音频焦点 | ✅ | 长失焦暂停/短失焦暂停或duck/恢复 |
| | 耳机拔出暂停 | ✅ | |
| **UI** | 首页基础 UI | ✅ | 曲库入口/继续播放/最近添加/歌手/格式统计/mini player |
| | 播放详情页 | 🔶 | 第一版可用，需继续打磨（大封面/信息/进度/歌词/模式切换） |
| | 队列抽屉/队列页 | 🔶 | 第一版可用，拖拽排序和删除后置 |
| | Mini player | 🔶 | 可能遮挡列表底部，需补安全 padding |
| | 深色主题 | ✅ | 文字对比度问题已修复 |
| **歌词** | 同名 .lrc 解析 | ✅ | 当前行显示可用 |
| | 内嵌歌词 | ⏸ | 后置 |
| **测试** | 单元测试 | ✅ | 模型/队列/随机/歌词/状态/数据库/主题/启动策略 |
| | 完整构建验证 | ⬜ | 前台服务修复后未重新跑完整构建 |
| | 模拟器实播验证 | ⬜ | 前台服务修复后未重新实播 |

---

## 5. 已知风险

- 模拟器与真机在外部文件夹访问上的行为差异（Android 版本、USB 媒体路径）。
- Media3 在高采样率和不常见 WAV 变体上的解码行为可能因设备而异。
- Android 解码链路对位深的暴露不一定准确。
- 后台通知和锁屏行为必须在实机运行时验证。
- 当前工作区 `YuandaoMusic/` 从父目录角度看是 untracked，长期版本控制需要整理。

---

## 6. 当前待办事项

### 6.1 最高优先级：前台服务修复回归验证

修复后只跑过单测（`PlaybackServiceStartupPolicyTest`，已通过），还需完成：

- [ ] 完整构建：`.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`
- [ ] 安装到 API 35 模拟器
- [ ] 重新推入 WAV/FLAC 测试文件，验证扫描和实播
- [ ] 验证后台播放不再触发 `ForegroundServiceDidNotStartInTimeException`
- [ ] 验证按 Home、锁屏、切后台后播放服务稳定
- [ ] 验证通知栏：上一首/下一首/播放暂停/关闭服务

### 6.2 播放页与队列页完善

- [ ] 播放详情页打磨：大封面、曲目信息、采样率/位深/格式、进度、歌词区域、播放模式切换
- [ ] 队列页验证：当前播放列表、下一首列表、随机队列顺序
- [ ] Mini player 底部安全 padding（可能遮挡列表底部内容）

### 6.3 通知栏与媒体会话

- [ ] 通知样式在 Android 13/14/15 上一致性确认
- [ ] 系统媒体控件中标题/歌手/封面/播放状态正确显示

### 6.4 音频焦点与外设行为手测

- [ ] 其他 App 抢占音频焦点 → 暂停/恢复
- [ ] 短暂失焦 → duck
- [ ] 耳机拔出 → 暂停

### 6.5 数据与格式增强

- [ ] 内嵌歌词读取
- [ ] 位深识别增强
- [ ] 用 U 盘 `F:\` 中的真实音乐文件做格式和元数据压力测试

### 6.6 UI 与视觉（播放闭环稳定后再启动）

- [ ] 字体层级、间距、圆角、深色玻璃质感、卡片组件、图标体系、播放状态颜色
- [ ] 中文文案编码与排版统一

---

## 7. 下一窗口建议执行顺序

1. 读取本文件，了解当前状态
2. 跑完整构建：`.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace`
3. 构建通过后安装到 API 35 模拟器
4. 重新推入 WAV/FLAC 测试文件，验证扫描和实播
5. 重点观察前台服务修复是否彻底解决后台播放崩溃
6. 手测通知栏动作和系统媒体会话
7. 修复 mini player 遮挡底部内容的问题
8. 继续完善播放详情页和队列页
9. 每完成一个阶段，更新本文件对应 checklist 的状态

---

## 8. 构建与验证命令

从 `YuandaoMusic/` 目录执行：

```powershell
# 完整构建验证
.\gradlew.bat testDebugUnitTest assembleDebug lintDebug --stacktrace

# 针对性单测
.\gradlew.bat testDebugUnitTest --tests <TestClassName> --stacktrace
```

上次已知通过结果：`BUILD SUCCESSFUL`（前台服务修复前的完整构建）

---

## 9. 用户可协助事项

用户无需编写代码，最有价值的帮助是：

- 在 Android Studio 中运行应用到模拟器或真机
- 提供运行时截图或录屏
- 提供 Logcat 输出（崩溃/播放失败/扫描失败/权限问题）
- 提供用于复现问题的 WAV/FLAC 样本文件
- `F:\` U 盘可作为真实音乐样本库
- 后续进入高保真 UI 阶段时提供 Figma 或设计稿
- 当两个技术上可行的行为冲突时，确认产品选择

---

## 10. 重要文件索引

| 文件 | 用途 |
| --- | --- |
| `PROJECT.md`（本文件） | 项目总览、状态、待办、下一窗口执行顺序 |
| `docs/ROADMAP.md` | 阶段路线图（Phase 0-8） |
| `docs/ARCHITECTURE.md` | 技术架构说明 |
| `docs/ANDROID_STUDIO_RUNBOOK.md` | Android Studio 运行指南 |
| `docs/manual-verification/wav-flac-local-playback.md` | WAV/FLAC 手测流程 |
| `docs/manual-verification/runtime-issue-template.md` | 运行时问题报告模板 |
| `参考图.png` | 视觉参考图（最终目标） |

---

## 11. 更新记录

| 日期 | 变更 |
| --- | --- |
| 2026-07-03 | 整合 `progress.md` 和 `docs/PROJECT_STATUS.md` 为本文件，新增可维护的 checklist 结构 |
