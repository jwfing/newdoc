# Windows Phone 消息推送开发指南

> 开始之前
> 在看下面的内容之前，我们假设你已经看过我们的[消息推送开发总览](./push_guide.html)，了解了基本的概念和模型。

Windows Phone 8 的推送较为特殊，因为微软在设计的时候把推送消息定义为一个包含跳转页面信息的载体，比如微信推送：你单击微信发送的 Windows Phone 的 Toast 推送消息，单击进去之后，它不是打开微信的默认首页（假如叫做 Main.xaml），而是进入某一个聊天的具体的页面（假如叫做 chat.xaml）。这种场景微软是通过在推送消息里面包含了代码逻辑来实现的，比如要实现刚才这一套流程，微信服务端必须向微软的 MPNS 发送一个如下类似的消息（Http 或者 Https Post 请求）：

```xml
<?xml version="1.0" encoding="utf-8"?>
    <wp:Notification xmlns:wp="WPNotification">
        <wp:Toast>
            <wp:Text1>微信</wp:Text1>
            <wp:Text2>你有一条聊天消息</wp:Text2>
            <wp:Param>/chat.xaml?NavigatedFrom=Toast Notification</wp:Param>
        </wp:Toast>
    </wp:Notification>
```
所以在使用 LeanCloud 推送服务向 Windows Phone 8 平台推送的时候一定要对微软官方的推送有所了解，如果想深入了解，可以点击详细查看微软官方关于 [Windows Phone 8 推送的官方教程](http://msdn.microsoft.com/en-us/library/windows/apps/hh202967.aspx)。

针对 Windows Phone 8 的特殊性，LeanCloud 采用了统一接口去处理，如下 C# 代码可以实现以上所说的功能：
在 LeanCloud 所有 .NET 语言 SDK 均可如下进行操作。（注：Unity 暂时不支持.Wait（）方法 和 await 关键字，所以它需要使用任务的链式表达，详情请查看 Unity 的文档。）

```javascript
AVPush avPush = new AVPush();
avPush.Data = new Dictionary<string, object>();
avPush.Data.Add("title", "微信");
avPush.Data.Add("alert", "你有一条聊天消息");
avPush.Data.Add("wp-param", "/chat.xaml?NavigatedFrom=Toast Notification");
await avPush.SendAsync()；
```
## 推送给所有的设备

```javascript
AVPush push = new AVPush();
push.Alert = "message to all devices.";
var task = push.SendAsync();
await task;
```
以上这段代码就可以实现向所有安装了当前应用的设备推送消息。

## 发送给特定的用户
发送给 public 频道的用户：

```javascript
AVPush push = new AVPush();
push.Alert = "message to public channel.";
push.Query = new AVQuery<AVInstallation>().WhereEqualTo("channels", "public");
var task = push.SendAsync();
await task;
```

## 文档贡献

如果觉得这个文档写的不够好，也可以帮助我们来不断完善。

Github 仓库地址：[https://github.com/leancloud/docs](https://github.com/leancloud/docs)

