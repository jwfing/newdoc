# iOS 崩溃报告使用指南

崩溃报告可以自动收集用户端产生的各种异常崩溃信息，包括完整的调用堆栈、设备信息、应用信息等等。如果还没有安装 LeanCloud iOS SDK，请阅读 [快速入门](./start.html) 来获得该 SDK，并在 Xcode 中运行和熟悉示例代码。

## 启用

要启用崩溃报告功能，需要将 AVOSCloudCrashReporting.framework 添加到项目的依赖库列表，然后在 AppDelegate 之中，在 LeanCloud SDK 初始化之前添加如下代码：

```objc
// Enable Crash Reporting
[AVOSCloudCrashReporting enable];

// Setup AVOSCloud
[AVOSCloud setApplicationId:@"{{appid}}" clientKey:@"{{appkey}}"];
```

崩溃报告打开后，应用的崩溃信息会发送到 LeanCloud 云端，具体内容可在 [控制台 / 分析 / 错误分析](/stat.html?appid={{appid}}#/stat/crashreport) 里看到。


## 符号化

将应用每个版本的符号文件上传至 LeanCloud，会有助于 LeanCLoud 后台正确解析崩溃堆栈信息，更准确地定位问题，因此这一步骤非常重要。

上传符号文件有两种方式：每次构建应用时自动上传和手动上传。我们推荐使用自动上传，这样开发者就不需要在每次发布版本时，还要记着去上传符号文件。实现符号文件**自动上传**的步骤如下：

安装客户端工具（如果出现权限错误，可以在命令前面加上 `sudo`）：

```sh
gem update --system
gem install --no-wrappers -n /usr/local/bin leancloud
```

在应用的 Targets 中，选择 **Build Settings** > **Build Options** > **Debug Information Format**，将其设置为 **DWARF with dSYM File**；再打开 **Build Phases** ><span class="text-muted">（点击 + 号）</span>> **New Run Script Phase**，添加一个 **Run Script** 步骤：

```sh
leancloud upload_symbol \
-f "${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}" \
-i "{{appid}}" \
-k "{{appkey}}"
```

如果只想在 Archive 发布时运行这个脚本，可以勾选 **Run script only when installing**，这样可以加快调试时 Build 的速度。

要手动上传符号文件，则使用：

```sh
leancloud upload_symbol \
-f "<dSYM/xcarchive/DWARF 请按下面提示替换本路径>" \
-i "{{appid}}" \
-k "{{appkey}}"
```

符号文件将默认上传到中国节点。如果你的应用使用了中国之外的其他节点，例如美国节点，需要通过 `-r` 或 `--region` 选项来设置：

```sh
leancloud upload_symbol -r us \
-f "<dSYM/xcarchive/DWARF 请按下面提示替换本路径>" \
-i "{{appid}}" \
-k "{{appkey}}"
```

目前支持以下节点：

- **cn**：中国
- **us**：美国

符号文件的存放位置，与你的发布流程有关，具体为：

* **xcarchive 文件**：对应用进行 Archive 后会生成一个 .xcarchive 文件，在 Xcode 中打开 **Window** / **Organizer** / **Archives** 选项卡里可以看到它的位置。

* **dSYM 文件**：一般仅在使用 Release 或 Archiving 构建应用时产生，可以在 /Xcode/DerivedData 文件夹下，与生成的 .app 同级的文件夹里找到。

* **DWARF 文件**：在路径的最底层，它可以通过 strip 应用生成的二进制文件来获得。

请参考以下路径结构，应用文件名为 Demo.app：

```
├── Build
    ├── Intermediates
        ├── ArchiveIntermediates
            └── Demo
                ├── BuildProductsPath
                    └── Release-iphoneos
                        ├── Demo.app -> /Users/admin/Library/Developer/Xcode/DerivedData/Demo-dkvwhbscbgyeoldhsxxzmorfqmyf/Build/Intermediates/ArchiveIntermediates/Demo/InstallationBuildProductsLocation/Applications/Demo.app
                        └── Demo.app.dSYM
                            └── Contents
                                ├── Info.plist
                                └── Resources
                                    └── DWARF
                                        └── Demo
```

如果在发布应用时忘了上传符号文件，可以手动来上传。如果一个崩溃报告没有对应的符号文件，系统会提示要符号化。

## 测试

崩溃报告启用之后，要测试崩溃信息是否能发送到 LeanCloud 云端，可以在 view controller 或 AppDelegate 中添加一个 `crash` 方法：

```objc
- (void)crash {
    [NSException raise:NSGenericException format:@"只是测试，模拟一条崩溃信息。"];
}
```

然后把下面的调用代码插入到主 view controller 的 `viewDidLoad` 方法中，或 AppDelegate 的 `application:didFinishLaunchingWithOptions:` 方法中：

```objc
[self performSelector:@selector(crash) withObject:nil afterDelay:5.0];
```
接下来的步骤：

1. build、run，然后 stop 应用；
2. 在 Home 界面点开应用（不要使用调试器开启应用，因为调试器开启时不会收集崩溃信息）；
3. 等待应用崩溃；
4. 再次运行应用。应用将在启动时自动将崩溃报告发送给 LeanCloud 云端。


这时打开应用控制台的 [错误分析](/stat.html?appid={{appid}}#/stat/crashreport)，就会看到刚才的崩溃信息。


> 注意：崩溃信息可能会延迟几分钟显示。

## 流程

使用崩溃报告可以极大地改进应用质量，提升用户体验。我们推荐使用以下流程来有效地修复应用崩溃问题：

1. 确认要修复的崩溃；<br/>
  后台的错误分析列表，可以显示出崩溃发生的次数。由此，你可以分析出哪项崩溃对应用影响最大，对其进行优先修复。点击每条崩溃记录可以查看到更详细的信息，包括调用堆栈、系统版本、设备类型和受影响的应用版本等等。
2. 修复崩溃；<br/>
  调试代码，修复崩溃问题。
3. 构建新的版本；
4. 标记已修复的崩溃，向 App Store 发布新版本；<br/>
  标记为「已修复」的崩溃不会在默认的列表中出现。如果同样的崩溃在新版本中再次出现，那么这条崩溃记录将自动恢复成「未修复」状态，并且重新出现在默认列表里面。
5. 重复以上步骤，不断改进应用。

## 排错

**为什么编译的时候会报错：Undefined symbols for architecture i386: "std::__1::basic_string<char, std::__1::char_traits<char>...？**

这是在项目依赖里面缺少 libc++.dylib 库。打开项目的 **TARGETS** / **General** / **Linked Frameworks and Libraries**，添加 libc++.dylib 即可。

**为什么我的崩溃没有在后台显示？**

你的应用可能没有向 LeanCloud 云端发送崩溃报告。需要检查：
  
- 确认在 LeanCloud SDK 初始化之前开启了崩溃报告。
- 如果正在测试崩溃报告，要确认应用不是通过调试器运行的；需要执行 build、run、stop 之后，再在 Home 界面点开应用。
- 崩溃信息是在崩溃发生之后、下次应用启动之时发送的，确保应用不会在崩溃信息发送之前再次崩溃。如果是在调试崩溃报告，可以添加一段延时做为保障。
- 确保没有使用其他崩溃报告的库，否则会互相干扰。

**为什么我的崩溃没有符号化？**

你可能没有上传与应用版本对应的符号文件。请检查：
  
- 如果设置了自动上传符号文件，切换到 Xcode 的 Report Navigator（Command + 8），验证一下上传是否正常。
- 确认日志正常，上传过程没有错误。
- 如果为之前没有符号化的崩溃上传了对应的符号文件，当同一崩溃再次发生时它就会被符号化；而之前没有符号化的崩溃信息不会再被符号化了。所以，这时可以把之前的崩溃记录标记为「已解决」并等待其再次发生。
- 如果使用了自定义的动态链接框架（dynamic framework），并且部分堆栈信息没有符号化，请确认所有动态链接框架以及主应用的符号文件都已经上传。

