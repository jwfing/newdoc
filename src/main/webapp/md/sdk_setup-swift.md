


# Swift SDK 安装指南

## 获取 SDK

获取 SDK 有多种方式，较为推荐的方式是通过包依赖管理工具下载最新版本。

### 包依赖管理工具安装


[CocoaPods](http://www.cocoapods.org/) 是开发 OS X 和 iOS 应用程序的一个第三方库的依赖管理工具，通过它可以定义自己的依赖关系（称作 pods），并且随着时间的推移，它会让整个开发环境中对第三方库的版本管理变得非常方便。具体可以参考 [CocoaPods 安装和使用教程](http://code4app.com/article/cocoapods-install-usage)。

首先确保开发环境中已经安装了 Ruby（一般安装了 Xcode，Ruby 会被自动安装上），如果没有安装请执行以下命令行：

```sh
$ sudo gem install cocoapods
```

如果遇到网络问题无法从国外主站上直接下载，我们推荐一个国内的镜像：[RubyGems 镜像](http://ruby.taobao.org/)，具体操作步骤如下：

```sh
$ gem sources --remove https://rubygems.org/
$ gem sources -a https://ruby.taobao.org/
# 请确保下列命令的输出只有 ruby.taobao.org
$ gem sources -l
*** CURRENT SOURCES ***
https://ruby.taobao.org
```

然后再安装 CocoaPods：

```sh
$ sudo gem install cocoapods
```

在项目根目录下创建一个名为 `Podfile` 的文件（无扩展名），并添加以下内容：

```ruby
use_frameworks! # LeanCloud Swift SDK can only be integrated as framework.

target '你的项目名称' do
	pod 'LeanCloud'
end
```

执行命令 `pod install --verbose` 安装 SDK。





## 初始化

首先来获取 App ID 以及 App Key。

打开 [控制台 / 设置 / 应用 Key](/app.html?appid={{appid}}#/key)，如下图：


![setting_app_key](images/setting_app_key.png)


打开 `AppDelegate.m` 文件，添加下列导入语句到头部：

```swift
import LeanCloud
```

然后粘贴下列代码到 `application:didFinishLaunchingWithOptions` 函数内：

```swift
// applicationId 即 App Id，applicationKey 是 App Key
LeanCloud.initialize(applicationID: "{{appid}}", applicationKey: "{{appkey}}")
```


### 启用指定节点

SDK 的初始化方法默认使用**中国大陆节点**，如需切换到 [其他可用节点](#全球节点)，请参考如下用法：



```swift
// 如果使用美国站点，请加上这行代码，并且写在初始化前面
LeanCloud.setServiceRegion(.US)

// applicationId 即 App Id，applicationKey 是 App Key
LeanCloud.initialize(applicationID: "{{appid}}", applicationKey: "{{appkey}}")
```


### 全球节点

- 中国大陆节点 **leancloud.cn**（SDK 初始化方法**默认**使用该节点）
- 北美节点 **us.leancloud.cn**（服务北美市场）
- ~~香港节点，服务东南亚市场~~（未上线，准备中）

<div class="callout callout-danger">各个节点彼此独立，开发者账号无法跨节点来创建应用或调用 API。</div>

## 验证

首先，确认本地网络环境是可以访问 LeanCloud 服务器的，可以执行以下命令行：

```shell
ping api.leancloud.cn
```
如果当前网路正常将会得到如下响应：

```shell
PING api.leancloud.cn (120.132.49.239): 56 data bytes
64 bytes from 120.132.49.239: icmp_seq=3 ttl=49 time=65.165 ms
64 bytes from 120.132.49.239: icmp_seq=4 ttl=49 time=53.273 ms
64 bytes from 120.132.49.239: icmp_seq=5 ttl=49 time=51.519 ms
64 bytes from 120.132.49.239: icmp_seq=6 ttl=49 time=68.442 ms
```
然后在项目中编写如下测试代码：



```swift
let post = LCObject(className: "TestObject")

post.set("words", value: "Hello World!")

post.save()
```

然后，点击 `Run` 运行调试，真机和虚拟机均可。



然后打开 [控制台 > 存储 > 数据 > TestObject](/data.html?appid={{appid}}#/TestObject)，如果看到如下内容，说明 SDK 已经正确地执行了上述代码，安装完毕。


![testobject_saved](images/testobject_saved.png)

如果控制台没有发现对应的数据，请参考 [问题排查](#问题排查)。

## 问题排查

### 401 Unauthorized

如果 SDK 抛出 401 异常或者查看本地网络访问日志存在：

```json
{
  "code": 401,
  "error": "Unauthorized."
}
```
则可认定为 App ID 或者 App Key 输入有误，或者是不匹配，很多开发者同时注册了多个应用，导致拷贝粘贴的时候，用 A 应用的 App ID 匹配 B 应用的 App Key，这样就会出现服务端鉴权失败的错误。

### 客户端无法访问网络

客户端尤其是手机端，应用在访问网络的时候需要申请一定的权限。






 
