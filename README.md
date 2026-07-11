# 慢慢来

慢慢来是一款给拖延症患者做近期计划总结的 Android 应用。它把待办事项做成扑克牌形状的任务卡，用户可以像翻看手牌一样拖动、翻面、完成和收藏任务，让计划管理更轻一点，也更有仪式感。

## 功能亮点

- 扑克牌比例任务卡，首页以牌堆作为第一入口。
- 当前任务卡支持上下左右拖动，越过阈值后回到牌堆末尾。
- 点击卡片可 3D 翻面，正面显示任务细节，背面显示创建时间、完成时间、延期次数等摘要信息。
- 完成任务后生成完成卡并进入卡包，卡包内可查看详情，也可返回牌堆。
- 完成卡会保留完成当时的牌面、描述、标签、优先级、截止时间和延期记录。
- 删除任务进入回收站，可恢复或彻底删除。
- 支持任务编辑、优先级、标签、截止时间、分钟级周期提醒。
- 点击通知会自动定位对应任务；“稍后”会将任务移到牌堆末尾并重新安排提醒。
- 内置 8 套主题背景和 4 套牌面样式，主题和牌面可收纳切换。
- 本地 Room 数据库保存任务、卡包和成就，DataStore 保存偏好设置。
- Room v3 使用无损迁移和事务，避免升级清库、重复完成卡或跨表写入一半。
- WorkManager + 系统通知提供温和提醒，完成或删除后的卡片不会继续提醒。

## 技术栈

- Kotlin
- Jetpack Compose
- Material 3
- Room
- DataStore Preferences
- WorkManager
- JUnit

## 本地构建

先确保已经安装 Android Studio、Android SDK 和 JDK 17。Windows 下可直接使用仓库内的 Gradle Wrapper：

```powershell
.\gradlew.bat assembleDebug
```

调试包输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

仓库内的 `dist` 目录保留历史测试包；开发中的最新安装包以本地构建输出为准：

```text
app/build/outputs/apk/debug/app-debug.apk
```

这个 APK 仅用于真机测试，不是正式签名发布版。

正式签名并经过 R8/资源压缩的安装包输出到：

```text
dist/manmanlai-v1.1.0-release.apk
```

release 签名读取项目根目录的 `keystore.properties` 和 `manmanlai-release.jks`。这两个文件已被 Git 忽略，必须在本机另行备份；后续版本需要继续使用同一密钥，否则无法覆盖安装更新。

如果需要指定 Android Studio 自带的 JDK：

```powershell
$env:JAVA_HOME='D:\android-studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assembleDebug
```

## 安装到手机

连接手机并开启 USB 调试后：

```powershell
adb devices
adb install -r -g app/build/outputs/apk/debug/app-debug.apk
```

## 测试

```powershell
.\gradlew.bat test
```

当前包含牌堆逻辑单元测试，以及完成去重、完成卡快照、卡包回流、回收站和延期提醒的 Room 真机测试。

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

如果 Windows 下项目路径包含中文，Gradle 8.4 的本地 JVM 测试可能出现 classpath 编码问题；可从纯英文路径副本执行测试。真机测试也可以直接安装 `app-debug-androidTest.apk` 后通过 `am instrument` 运行。

## 项目结构

```text
app/src/main/java/com/slowly/manmanlai/
├── data/        # Room、数据模型、仓库和牌堆逻辑
├── ui/          # Compose 页面、主题、ViewModel
├── worker/      # 提醒通知 WorkManager
├── MainActivity.kt
└── ManManLaiApp.kt
```

## 隐私

第一版不包含登录、云同步、广告 SDK 或第三方统计。任务、卡包、主题偏好都保存在本机。详见 [PRIVACY.md](PRIVACY.md)。

## 开源协议

本项目使用 [MIT License](LICENSE)。
