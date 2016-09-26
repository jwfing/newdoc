# Objective-C SDK 安装指南

## 自动安装
通过 [CocoaPods](http://www.cocoapods.org) 来安装可以最大化地简化安装过程。

首先，在项目根目录下的 Podfile 文件中添加以下 pods：

```ruby
pod 'AVOSCloud'               # 数据存储、短信、云引擎调用等基础服务模块
pod 'AVOSCloudIM'             # 实时通信模块
pod 'AVOSCloudCrashReporting' # 错误报告模块
```

然后在项目根目录执行 `pod install` 命令，就能将 SDK 集成到您的项目中。


## 手动安装

首先，将指定版本的源码 clone 到您的项目根目录：

```sh
git clone --depth 1 https://github.com/leancloud/objc-sdk.git leancloud-objc-sdk
```

如果您希望使用 CrashReporting 模块，还需要进一步 clone submodule：

```sh
cd leancloud-objc-sdk

git submodule update --init
```

将 `AVOS/AVOS.xcodeproj` 项目文件拖进您的项目，作为 subproject。就像下面这样：

![img](images/quick_start/ios/subproject.png)

接着，为您的应用设置 Build Phases。添加 Link Binary With Libraries：

![img](images/quick_start/ios/link-binary.png)

注意，作为示例，上图选择了两个支持 iOS 平台的 frameworks（AVOSCloud 与 AVOSCloudIM 模块）。实际情况下，您应该根据应用支持的平台以及需要用到的服务来进行选择。

然后添加 SDK 依赖的系统 framework 和 library：

  * libz
  * libc++
  * libicucore
  * libsqlite3
  * SystemConfiguration.framework
  * MobileCoreServices.framework
  * CoreTelephony.framework
  * CoreLocation.framework

就像下面这样：

![img](images/quick_start/ios/system-dependency.png)

最后，在 Other Linker Flags 中添加 `-ObjC`。

这样就集成完毕了。


## 初始化 SDK

打开 AppDelegate 文件，添加下列导入语句到头部：

```objc
#import <AVOSCloud/AVOSCloud.h>
```

然后粘贴下列代码到 `application:didFinishLaunchingWithOptions` 函数内：

```objc
// 如果使用美国站点，请加上下面这行代码：
// [AVOSCloud setServiceRegion:AVServiceRegionUS];

[AVOSCloud setApplicationId:@"{{appid}}" clientKey:@"{{appkey}}"];
```

如果想跟踪统计应用的打开情况，后面还可以添加下列代码：

```objc
[AVAnalytics trackAppOpenedWithLaunchOptions:launchOptions];
```


创建应用后，可以在 [控制台 > 应用设置](/app.html?appid={{appid}}#/key) 里面找到应用对应的 id 和 key。


保证在你的源文件里包含了 SDK 库文件：

```objc
#import <AVOSCloud/AVOSCloud.h>
```

将下面的代码拷贝到你的 app 里，比如在 `viewDidLoad` 方法（或者其他在运行 app 时会调用到的方法）：

```
AVObject *testObject = [AVObject objectWithClassName:@"TestObject"];
[testObject setObject:@"bar" forKey:@"foo"];
[testObject save];
```


运行 app，一个类名为 `TestObject` 的新对象会被发送到 LeanCloud 并保存下来。当做完这一切，访问 [控制台 > 数据管理](/data.html?appid={{appid}}#/TestObject) 可以看到上面创建的 TestObject 的相关数据。



## 社交组件

最后，如果希望使用社交组件功能，可以使用我们的开源组件：[leancloud-social-ios](https://github.com/leancloud/leancloud-social-ios)。
