



# .NET 实时通信开发指南

## 简介

实时通信服务可以让你一行后端代码都不用写，就能做出一个功能完备的实时聊天应用，或是一个实时对战类的游戏。所有聊天记录都保存在云端，离线消息会通过消息推送来及时送达，推送消息文本可以灵活进行定制。

>在继续阅读本文档之前，请先阅读[《实时通信开发指南》](./realtime_v2.html)，了解一下实时通信的基本概念和模型。


目前我们的 .NET 实时通信支持如下运行时：

* Windows Phone Silverlight （8.0 & 8.1）
* Windows Desktop .NET Framework 4.5+
* Xamarin Form 1.4+
* Xamarin iOS 8+
* Xamarin Android 5+

尚未发布但是已在计划内的如下：

* Windows Runtime （for Windows 10）

文档中涉及的语法以及接口均对所有运行时有效。


### 文档贡献
我们欢迎和鼓励大家对本文档的不足提出修改建议。请访问我们的 [Github 文档仓库](https://github.com/leancloud/docs) 来提交 Pull Request。

## Demo
相比阅读文档，如果你更喜欢从代码入手了解功能的具体实现，可以下载 Demo 来研究：


* [.NET Demo](https://github.com/leancloud/windows-phone-sdk-demos)（推荐）


我们把所有 Demo 项目放在了 [LeanCloud Demos 资源库](https://github.com/leancloud/leancloud-demos) 中，方便大家浏览和参考。

## 安装和初始化

为了支持实时聊天， 实时通信 SDK 依赖于几个开源的 WebSocket 的库，推荐开发者从 [Nuget](https://www.nuget.org/packages/LeanCloud/) 上下载我们的 SDK。

导入 SDK 之后，在应用入口函数中添加如下代码：

```c#
   //generated code by visual studio
   ...
   //"你的 AppId", "你的 AppKey"
   AVClient.Initialize("{{appid}}", "{{appkey}}"); 
   ...

```
例如，在 Windows 控制台的 Main 函数入口可以调用以上代码进行初始化。




## 单聊

我们先从最简单的环节入手。此场景类似于微信的私聊、微博的私信和 QQ 单聊。我们创建了一个统一的概念来描述聊天的各种场景：`AVIMConversation`（对话），在[《实时通信开发指南》](./realtime_v2.html) 里也有相关的详细介绍。

### 发送消息

![Tom and Jerry](images/tom-and-jerry-avatar.png)

Tom 想给 Jerry 发一条消息，实现代码如下：


```c#
public async void TomCreateConversationWithJerry()
{
    //Tom 用自己的名字作为 ClientId 建立了一个 AVIMClient
    AVIMClient client = new AVIMClient("Tom");

    //Tom 登录到系统
    await client.ConnectAsync();

    //Tom 建立了与 Jerry 的对话
    AVIMConversation conversation = await client.CreateConversationAsync("Jerry");

    //Tom 发了一条消息给 Jerry
    await conversation.SendTextMessageAsync("耗子，起床！");
}
```



执行完以上代码，在 LeanCloud 网站的 [控制台 /（选择应用）/ 存储 / 数据 / `_Conversation ` 表](/data.html?appid={{appid}}#/_Conversation) 中多了一行数据，其字段含义如下：


名称|类型|描述
---|---|---
name|String|对话唯一的名字
m|Array|对话中成员的列表
lm|Date|对话中最后一条消息发送的时间
c|String|对话的创建者的 ClientId
mu|Array|对话中设置了静音的成员，仅针对 iOS 以及 Windows Phone 用户有效。
attr|Object|开发者设置的对话的自定义属性

>提示：每次调用 `CreateConversationAsync()` 方法，都会生成一个新的 Conversation 实例，即便使用相同 conversationMembers 和 name 也是如此。因此必要时可以先使用 `AVIMConversationQuery` 进行查询，避免重复创建。

### 接收消息

要让 Jerry 收到 Tom 的消息，需要这样写：


```c#
public async void JerryReceiveMessageFromTom()
{
    //Jerry 用自己的名字作为 ClientId 建立了一个 AVIMClient
    AVIMClient client = new AVIMClient("Jerry");

    //Jerry 登录到系统
    await client.ConnectAsync();

    //Jerry 设置接收消息的方法，一旦有消息收到就会调用这个方法
    client.OnMessageReceieved += (s, e) =>
    {
        if (e.Message is AVIMTextMessage)
        {
            string words = ((AVIMTextMessage)e.Message).TextContent;
            //words 内容即为：耗子，起床！
        }
    };
}
```




## 群聊

对于多人同时参与的固定群组，我们有成员人数限制，最大不能超过 500 人。对于另外一种多人聊天的形式，譬如聊天室，其成员不固定，用户可以随意进入发言的这种「临时性」群组，后面会单独介绍。

### 发送消息

Tom 想建立一个群，把自己好朋友都拉进这个群，然后给他们发消息，他需要做的事情是：

1. 建立一个朋友列表
2. 新建一个对话，把朋友们列为对话的参与人员
3. 发送消息


```c#
public async void TomCreateConversationWithFriends()
{
    //Tom 用自己的名字作为 ClientId 建立了一个 AVIMClient
    AVIMClient client = new AVIMClient("Tom");

    //Tom 登录到系统
    await client.ConnectAsync();

    #region 第一步：建立一个朋友列表
    IList<string> friends = new List<string>();
    friends.Add("Jerry");
    friends.Add("Bob");
    friends.Add("Harry");
    friends.Add("William");
    #endregion

    #region 新建一个对话，把朋友们列为对话的参与人员
    AVIMConversation friendConversation = await client.CreateConversationAsync(friends);
    #endregion

    #region 第三步：发送一条消息
    await friendConversation.SendTextMessageAsync("你们在哪儿？");
    #endregion
}
```

### 接收消息

群聊的接收消息与单聊的接收消息在代码写法上是一致的。


```c#
AVIMConversation NotifiedConversation = null;
public async void BobReceiveMessageFromTom()
{
    //Bob 用自己的名字作为 ClientId 建立了一个 AVIMClient
    AVIMClient client = new AVIMClient("Bob");

    //Bob 登录到系统
    await client.ConnectAsync();

    //Bob 设置接收消息的方法，一旦有消息收到就会调用这个方法
    client.OnMessageReceieved += (s, e) =>
    {
        if (e.Message is AVIMTextMessage)
        {
            //words 的内容就是：你们在哪儿呢？
            string words = ((AVIMTextMessage)e.Message).TextContent;

            //AVIMClient 在接收到消息的时候，会一并提供消息所在的 AVIMConversation
            NotifiedConversation = e.Conversation;

            if (NotifiedConversation != null)
            {
                //Bob 收到消息后又回复了一条消息
                NotifiedConversation.SendTextMessageAsync("@Tom, 我在 Jerry 家，你跟 Harry 什么时候过来？还有 William 和你在一起么？");
            }
        }
    };
}
```


以上由 Tom 和 Bob 发送的消息，William 在上线时都会收到。

由此可以看出，**群聊和单聊本质上都是对话**，只是参与人数不同。单聊是一对一的对话，群聊是多对多的对话。

用户在开始聊天之前，需要先登录 LeanCloud 云端。这个登录并不需要用户名和密码认证，只是与 LeanCloud 云端建立一个长连接，所以只需要传入一个唯一标识作为当前用户的 `clientId` 即可。

为直观起见，我们使用了 Tom、Jerry 等字符串作为 clientId 登录聊天系统。LeanCloud 云端只要求 clientId 在应用内唯一、不超过 64 个字符的字符串即可，具体用什么数据由应用层决定。

实时通信 SDK 在内部会为每一个 clientId 创建唯一的 `AVIMClient` 实例，也就是说多次使用相同的 clientId 创建出来的实例还是同一个。因此，如果要支持同一个客户端内多账号登录，只要使用不同的 clientId 来创建多个实例即可。我们的 SDK 也支持多账户同时登录。

## 消息

消息是一个对话的基本组成部分，我们支持的消息类型有：

- 文本消息：`AVIMTextMessage`
- 图像消息：`AVIMImageMessage`
- 音频消息：`AVIMAudioMessage`
- 视频消息：`AVIMVideoMessage`
- 文件消息：`AVIMFileMessage`
- 位置消息：`AVIMLocationMessage`

### 富媒体消息

#### 图像消息

图像可以从系统提供的拍照 API 或本地媒体库中获取，也可以用有效的图像 URL。先调用 SDK  方法构造出一个 `AVIMImageMessage` 对象，然后把它当做参数交由 `AVIMConversation` 发送出去即可。

##### 发送图像消息

【场景一】用系统自身提供的 API 去获取本地媒体库里的照片的数据流，然后构造出 `AVIMImageMessage` 来发送：


```c#
MediaLibrary library = new MediaLibrary();//系统媒体库
var photo = library.Pictures[0];//获取第一张照片，运行这段代码，确保手机以及虚拟机里面的媒体库至少有一张照片

AVIMImageMessage imgMessage = new AVIMImageMessage(photo.Name, photo.GetImage());//构造 AVIMImageMessage
imgMessage.Attributes = new Dictionary<string, object>() 
{ 
    {"location","旧金山"}
};
imgMessage.Title = "发自我的 WP";
await conversation.SendImageMessageAsync(imgMessage);
```


【场景二】从微博上复制的一个图像链接来创建图像消息：



```c#
public async void SendImageMessageAsync_Test()
{
    AVIMClient client = new AVIMClient("Tom");
    
    await client.ConnectAsync();//Tom 登录

    AVIMConversation conversation = await client.CreateConversationAsync("Jerry", "猫和老鼠");//创建对话

    AVIMImageMessage imgMessage = new AVIMImageMessage("http://pic2.zhimg.com/6c10e6053c739ed0ce676a0aff15cf1c.gif");//从外部链接创建图像消息
    await conversation.SendImageMessageAsync(imgMessage);//发送给 Jerry
}
``` 


以上两种场景对于 SDK 的区别为：

* 场景一：SDK 获取了完整的图像数据流，先上传文件到云端，再将文件的元数据以及 URL 等一并包装，发送出去。

* 场景二：SDK 并没有将图像实际上传到云端，而仅仅把 URL 包装在消息体内发送出去，这种情况下接收方是无法从消息体中获取图像的元信息数据，但是接收方可以自行通过客户端技术去分析图片的格式、大小、长宽之类的元数据。

##### 接收图像消息


类似于第一章节中单聊中的接收消息，客户端登录后可以通过 `AVIMClient` 中的 `OnMessageReceived` 方法来接收图像，如果接收方此时正好加载了这个对话，那么接收方在 `AVIMConversation` 里面也会收到 `OnImageMessageReceived` 的事件响应：



```c#
public async void ReceiveImageMessageAsync_Test()
{
    AVIMClient client = new AVIMClient("Jerry");
    await client.ConnectAsync();
    AVIMConversation conversation = client.GetConversationById("55117292e4b065f7ee9edd29");
    await conversation.FetchAsync();
    conversation.OnImageMessageReceived += (s, e) =>
    {
        //图像的 url
        string url = e.Url;
        //图像的元数据
        IDictionary<string, object> metaData = e.FileMetaData;
        //图像的发送者 ClientId
        string  from= e.FromClientId;
        //图像发送者为图像设定的 Title
        string title = e.Title;

        //一些其他的属性都可以在这里获取
    };
}
```


#### 音频消息

##### 发送音频消息

发送音频消息的基本流程是：读取音频文件（或者录制音频）> 构建音频消息 > 消息发送。


```c#
private async void SendAudioMessageAsync()
{
    StorageFolder local = Windows.Storage.ApplicationData.Current.LocalFolder;
    var AudioFile = await local.OpenStreamForReadAsync(recordAudioFileName);
    AVIMAudioMessage audioMessage = new AVIMAudioMessage(recordAudioFileName, AudioFile);//创建音频消息

    await conversation.SendAudioMessageAsync(audioMessage);
    //这段代码运行之前，请确保 `conversation` 已经实例化
}
``` 


与图像消息类似，音频消息也支持从 URL 构建：


```c#
public async void SendAudioMessageAsync()
{
    AVIMClient client = new AVIMClient("Tom");
    //Tom 登录
    await client.ConnectAsync();
    var conversation = await client.CreateConversationAsync("Jerry", "猫和老鼠");//创建对话

    AVIMAudioMessage audioMessage = new AVIMAudioMessage("http://ac-lhzo7z96.clouddn.com/1427444393952");//从外部链接创建音频消息
    await conversation.SendAudioMessageAsync(audioMessage);//发送给 Jerry
}
```


##### 接收音频消息


与接收图像消息类似，由 `AVIMConversation` 的 `OnAudioMessageReceived` 方法来响应，实例代码请参照 [图像消息接收](#接收图像消息)。


#### 视频消息

##### 发送视频消息

与发送音频消息的流程类似，视频的来源可以是手机录制，可以是系统中某一个具体的视频文件：


```c#
private async void SendVideoMessageAsync()
{
    StorageFolder local = Windows.Storage.ApplicationData.Current.LocalFolder;

    var VideoFile = await local.OpenStreamForReadAsync(recordVideoFileName);

    AVIMVideoMessage videoMessage = new AVIMVideoMessage(recordVideoFileName, VideoFile);

    await conversation.SendVideoMessageAsync(videoMessage);
}
```


同样我们也支持从一个视频的 URL 创建视频消息，然后发送出去：



```c#
public async void SendVideoMessageAsync()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录

    var conversation = await client.CreateConversationAsync("Jerry", "猫和老鼠");//创建对话

    AVIMVideoMessage videoMessage = new AVIMVideoMessage("http://ac-lhzo7z96.clouddn.com/1427267336319");//从外部链接创建视频消息
    await conversation.SendVideoMessageAsync(videoMessage);//发送给 Jerry
}
```


**注：这里说的 URL 指的是视频文件自身的 URL，而不是视频网站上播放页的 URL。**

##### 接收视频消息


与接收图像消息类似，由 `AVIMConversation` 的 `OnVideoMessageReceived` 方法来响应，实例代码请参照 [图像消息接收](#接收图像消息)。




#### 通用文件消息

开发者可以用它来发送带附件的消息或离线文件。对于此类消息，实时通信 SDK 内部会先把文件上传到 LeanCloud 文件存储服务器（自带 CDN 功能），然后把文件元数据（url、文件大小等）放在消息包内发送到实时通信云端。

Tom 要发送一份 .doc 文件给 Jerry，可以用下面这种方法：

##### 发送通用文件消息

```c#
public async void SendDocAsync()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录

    var conversation = await client.CreateConversationAsync("Jerry", "猫和老鼠");//创建对话
    StorageFolder local = Windows.Storage.ApplicationData.Current.LocalFolder;
    var docFile = await local.OpenStreamForReadAsync("leancloud.doc");//读取本地文件
    var avfile = new AVFile("leancloud.doc", docFile);//构造 AVFile
    AVIMFileMessage fileMessage = new AVIMFileMessage(avfile);//构造文件消息
    await conversation.SendFileMessageAsync(fileMessage);//发送
}
```

##### 接收通用文件消息

与接收图像消息类似，由 `AVIMConversation` 的 `OnFileMessageReceived` 方法来响应，实例代码请参照 [图像消息接收](#接收图像消息)。



#### 地理位置消息

地理位置消息构建方式如下：


```c#
//1.根据纬度和经度构建
 AVIMLocationMessage locationMessage = new AVIMLocationMessage(Latitude, Longitude);
//2.根据 AVGeoPoint 构建
AVGeoPoint avGeoPoint = new AVGeoPoint(31.3853142377, 121.0553079844);
AVIMLocationMessage locationMessage = new AVIMLocationMessage(avGeoPoint);
```


##### 发送地理位置消息


```c#
public async void SendLocationAsync()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录
    var conversation = await client.CreateConversationAsync("Jerry", "猫和老鼠");//创建对话
    
    AVIMLocationMessage locationMessage = new AVIMLocationMessage(138.12454, 52.56461);//以经度和纬度为参数构建一个地理位置消息，当然开发者更可以通过具体的设备的 API 去获取设备的地理位置，详细的需要查询具体的设备的 API
    await conversation.SendLocationMessageAsync(locationMessage);
}
```


##### 接收地理位置消息


与接收图像消息类似， 由 `AVIMConversation` 的 `OnLocationMessageReceived` 方法来响应，实例代码请参照 [图像消息接收](#接收图像消息)。



此处各个 SDK 平台需要详细介绍一下如何接收 TypedMessage 接收，包含文字和代码。描述风格以及代码示例请参照 iOS 版本。


### 暂态消息

暂态消息不会被自动保存（以后在历史消息中无法找到它），也不支持延迟接收，离线用户更不会收到推送通知，所以适合用来做控制协议。譬如聊天过程中「某某正在输入...」这样的状态信息，就适合通过暂态消息来发送；或者当群聊的名称修改以后，也可以用暂态消息来通知该群的成员「群名称被某某修改为...」。


```
private void txbMessage_TextChanged(object sender, TextChangedEventArgs e)//在消息输入的文本框 TextChanged 事件中
{
  //以下代码需要在整个窗体包含一个 AVIMClient 和 一个 AVIMConversation 实例，并且确保已经被初始化

  //以文本消息的方式发送暂态消息，其他成员在接受到此类消息时需要做特殊处理
  await conversaion.SendTextMessageAsync("Inputting", true, false);
  // 第一个参数 "Inputting" 表示自定义的一个字符串命令，此处开发者可以自行设置
  // 第二个参数 true 表示该条消息为暂态消息
  // 第三个参数 false 表示不要回执
}
```


而对话中的其他成员在聊天界面中需要有以下代码做出响应：


```
client.OnMessageReceieved += (s, e) => 
{
  if (e.Message is AVIMTextMessage)
  {
    //command 的内容就是：Inputting
    string command = ((AVIMTextMessage)e.Message).TextContent;

    // code 
    // 刷新 UI 控件，显示对方正在输入……
    // code
  }
};
```


### 消息的发送




#### 多媒体消息发送

目前 SDK 内置的多媒体消息类如下：

* 图像 `AVIMImageMessage`
* 音频 `AVIMAudioMessage`
* 视频 `AVIMVideoMessage`
* 文件 `AVIMFileMessage`

所有多媒体消息类型的发送流程如下：

如果文件是从**客户端 API 读取的数据流 (Stream)**，步骤为：

1. 从本地构造 AVFile
1. 调用 AVFile 的上传方法将文件上传到云端，并获取文件元信息（MetaData）
1. 把 AVFile 的 objectId、URL ，以及文件的元信息封装在消息体内
1. 调用接口发送消息

如果文件是**外部链接的 URL**，则：

1. 直接将 URL 封装在消息体内，不获取元信息，不包含 objectId
1. 调用接口发送消息

#### 启用离线消息通知

不管是单聊还是群聊，当用户 A 发出消息后，如果目标对话的部分用户当前不在线，LeanCloud 云端可以提供离线推送的方式来提醒用户。



这一功能默认是关闭的，你可以在 LeanCloud 应用控制台中开启它。操作方法请参考 [实时通信概览 &middot; 离线推送通知](realtime_v2.html#离线推送通知)。



#### 消息送达回执

是指消息被对方收到之后，云端会发送一个回执通知给发送方，表明消息已经送达。
需要注意的是：

> 只有在发送时设置了「等待回执」标记，云端才会发送回执，默认不发送回执。该回执并不代表用户已读。


```
//Tom 用自己的名字作为 ClientId 建立了一个 AVIMClient
AVIMClient client = new AVIMClient("Tom");

//Tom 登录到系统
await client.ConnectAsync();

//打开已存在的对话
AVIMConversation conversaion = client.GetConversationById("551260efe4b01608686c3e0f");
//设置送达回执
conversaion.OnMessageDeliverd += (s, e) =>
{
//在这里可以书写消息送达之后的业务逻辑代码
};
//发送消息
await conversaion.SendTextMessageAsync("夜访蛋糕店，约吗？");
```


### 消息的接收


消息接收分为**两个层级**：

* 第一层在 `AVIMClient` 上，它是为了帮助开发者实现被动接收消息，尤其是在本地并没有加载任何对话的时候，类似于刚登录，本地并没有任何 `AVIMConversation` 的时候，如果某个对话产生新的消息，当前负责接收这类消息，但是它并没有针对消息的类型做区分。

* 第二层在 `AVIMConversation` 上，负责接收对话的全部信息，并且针对不同的消息类型有不同的事件类型做响应。

以上两个层级的消息接收策略可以用下表进行描述，假如正在接收的是 `AVIMTextMessage`：

AVIMClient 接收端 | 条件① |条件② |条件③ | 条件④ |条件⑤ 
:---|:---|:---|:---|:---|:---
`AVIMClient.OnMessageReceived` | × | √ | √ | √ | √
`AVIMConversation.OnMessageReceived` | × | × | √ | × | × 
`AVIMConversation.OnTypedMessageReceived`| × | × | × | √ | × 
`AVIMConversation.OnTextMessageReceived` | × | × | × | × | √ 
对应条件如下：

条件①：
```c#
AVIMClient.Status != Online
``` 
条件②：
```c#
   AVIMClient.Status == Online 
&& AVIMClient.OnMessageReceived != null
```
条件③：
```c#
   AVIMClient.Status == Online 
&& AVIMClient.OnMessageReceived != null 
&& AVIMConversation.OnMessageReceived != null
```
条件④：
```c#
   AVIMClient.Status == Online 
&& AVIMClient.OnMessageReceived != null 
&& AVIMConversation.OnMessageReceived != null
&& AVIMConversation.OnTypedMessageReceived != null
&& AVIMConversation.OnTextMessageReceived == null
```

条件⑤：
```c#
   AVIMClient.Status == Online 
&& AVIMClient.OnMessageReceived != null 
&& AVIMConversation.OnMessageReceived != null
&& AVIMConversation.OnTypedMessageReceived != null
&& AVIMConversation.OnTextMessageReceived != null
```

在 `AVIMConversation` 内，接收消息的顺序为： 

`OnTextMessageReceived` > `OnTypedMessageReceived` > `OnMessageReceived`

这是为了方便开发者在接收消息的时候有一个分层操作的空间，这一特性也适用于其他富媒体消息。







### 消息类详解

我们所支持的文本、图像、音频、视频、文件、地理位置等富媒体消息类型都有一个共同的基类：`AVIMTypedMessage`，它们之间的关系如下图所示：

![消息的类图](http://ac-lhzo7z96.clouddn.com/1427252943504)

层级|类名|说明|类型
:---:|---|---|---
一|`AVIMMessage`|所有消息的基类|抽象类
二|`AVIMTypedMessage`| 富媒体消息的基类|抽象类
三|`AVIMTextMessage`|文本消息|实例类
 |`AVIMLocationMessage`|地理位置消息|实例类
 |`AVIMFileMessageBase`| 所有包含了文件内容的消息的基类|抽象类
四|`AVIMImageMessage`|图像消息|实例类
 |`AVIMAudioMessage`|音频消息|实例类
 |`AVIMVideoMessage`|视频消息|实例类
 |`AVIMFileMessage`|通用文件消息类|实例类

实时通信 SDK 在封装时对消息做了明确的分层，开发者需要根据自己的需求去使用。



消息类均包含以下公用属性：

属性|类型|描述
---|---|---
MessageBody|String|消息内容
FromClientId|String|指消息发送者的 clientId
ConversationId|String|消息所属对话 id
Id|String|消息发送成功之后，由 LeanCloud 云端给每条消息赋予的唯一 id
ServerTimestamp|long|消息发送的时间。消息发送成功之后，由 LeanCloud 云端赋予的全局的时间戳。
MessageStatus|AVIMMessageStatus 枚举|消息状态，有五种取值：<br/><br/>`AVIMMessageStatusNone`（未知）<br/>`AVIMMessageStatusSending`（发送中）<br/>`AVIMMessageStatusSent`（发送成功）<br/>`AVIMMessageStatusDelivered`（被接收）<br/>`AVIMMessageStatusFailed`（失败）
MessageIOType|AVIMMessageIOType 枚举|消息传输方向，有两种取值：<br/><br/>`AVIMMessageIOTypeIn`（发给当前用户）<br/>`AVIMMessageIOTypeOut`（由当前用户发出）

我们为每一种富媒体消息定义了一个消息类型，实时通信 SDK 自身使用的类型是负数（如下面列表所示），所有正数留给开发者自定义扩展类型使用，0 作为「没有类型」被保留起来。

消息 | 类型
--- | ---
文本消息|-1
图像消息|-2
音频消息|-3
视频消息|-4
位置消息|-5
文件消息|-6



### 自定义消息

在某些场景下，开发者需要在发送消息时附带上自己业务逻辑需求的自定义属性，比如消息发送的设备名称，或是图像消息的拍摄地点、视频消息的来源等等，开发者可以通过  实现这一需求。

【场景】发照片给朋友，告诉对方照片的拍摄地点：


```c#
AVIMImageMessage imgMessage = new AVIMImageMessage(photo.Name, photo.GetImage());//构造 AVIMImageMessage
imgMessage.Attributes = new Dictionary<string, object>() 
{ 
    {"location","拉萨布达拉宫"}
};
imgMessage.Title = "这蓝天……我彻底是醉了";
await conversation.SendImageMessageAsync(imgMessage);// 假设 conversationId= conversation 并且已经在之前被实例化
```


接收时可以读取这一属性：


```c#
AVIMClient client = new AVIMClient("friend");
await client.ConnectAsync();
AVIMConversation conversaion = client.GetConversationById("55117292e4b065f7ee9edd29");
await conversaion.FetchAsync();
conversaion.OnImageMessageReceived += (s, e) =>
{
//图像的 url
string url = e.Url;
//图像的元数据
IDictionary<string, object> metaData = e.FileMetaData;
//图像的发送者 ClientId
string from = e.FromClientId;
//图像发送者为图像设定的 Title
string title = e.Title;

//一些其他的属性都可以在这里获取
string location = e.Attributes["location"].ToString();// 读取的结果就是拉萨布达拉宫

};
```


所有的 `AVIMTypedMessage` 消息都支持 `Attributes` 这一属性。

#### 创建新的消息类型


.NET 在当前版本尚不支持自定义消息子类，正在研发中。


> **什么时候需要自己创建新的消息类型？**
>
>譬如有一条图像消息，除了文本之外，还需要附带地理位置信息，为此开发者需要创建一个新的消息类型吗？从上面的例子可以看出，其实完全没有必要。这种情况只要使用消息类中预留的  属性就可以保存额外的地理位置信息了。
>
>只有在我们的消息类型完全无法满足需求的时候，才需要扩展自己的消息类型。譬如「今日头条」里面允许用户发送某条新闻给好友，在展示上需要新闻的标题、摘要、图片等信息（类似于微博中的 linkcard）的话，这时候就可以扩展一个新的 NewsMessage 类。

## 对话

以上章节基本演示了实时通信 SDK 的核心概念「对话」，即 `AVIMConversation`。我们将单聊和群聊（包括聊天室）的消息发送和接收都依托于 `AVIMConversation` 这个统一的概念进行操作，所以开发者需要强化理解的一个概念就是：
>SDK 层面不区分单聊和群聊。



对话的管理包括「成员管理」和「属性管理」两个方面。

在讲解下面的内容之前，我们先来创建一个多人对话。后面的举例都要基于这个对话，所以**这一步是必须的**。请将以下代码复制到 IDE 并且执行。



```c#
/// <summary>
/// 这段代码实现的功能就是 Jerry 创建了一个包含 Bob、Harry、William 的对话。
/// </summary>
/// <returns></returns>
public async void JerryCreateConversation()
{
    AVIMClient client = new AVIMClient("Jerry");
    await client.ConnectAsync();//Jerry 登录

    IList<string> friends = new List<string>();
    friends.Add("Bob");
    friends.Add("Harry");
    friends.Add("William");
    //添加好朋友

    await client.CreateConversationAsync(friends);//返回 ConversationId
}
```




### 对话的成员管理

成员管理，是在对话中对成员的一个实时生效的操作，一旦操作成功则不可逆。

#### 成员变更接口
成员变更操作接口简介如下表：

操作目的|接口名
----|---
自身主动加入 |  `AVIMConversation.JoinAsync`
添加其他成员 |  `AVIMConversation.AddMembersAsync`
自身主动退出 |  `AVIMConversation.LeftAsync`
剔除其他成员 |  `AVIMConversation.RemoveMembersAsync`

成员变动之后，所有对话成员如果在线的话，都会得到相应的通知。


在 Dotnet 中，在 AVIMConversaion 定义了如下的事件，

```
//当前 Client 被其他成员邀请加入到当前对话激发的事件。
Public event OnInvited	

//当前 Client 加入到当前对话中激发的事件，区别于 OnMembersLeft，此事件有且仅在当前 Client 加入到当前对话时才响应。
Public event OnJoined	

//当前 Client 被其他成员从当前对话中剔除时激发的事件。
Public event OnKicked	

//当前 Client 离开当前对话中激发的事件，区别于 OnMembersLeft，此事件有且仅在当前 Client 离开当前对话才响应。 
Public event OnLeft	

//有其他的 Members 加入到当前对话时激发的事件。
Public event	OnMembersJoined	

//有其他的 Members 离开到当前对话时激发的事件。 
Public event	OnMembersLeft	
```

接下来，我们将结合代码，针对各种成员变更的操作以及对应的事件响应进行详细讲解。



#### 自身主动加入

Tom 想主动加入 Jerry、Bob、Harry 和 William 的对话，以下代码将帮助他实现这个功能：


```c#
public async void InitiativeJoinAsync()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();

    string conversationId = "551260efe4b01608686c3e0f";//获取 Jerry 创建的对话的 Id，这里是直接从控制台复制了上一节准备工作中 JerryCreateConversation 成功之后的 objectId
    AVIMConversation conversation = client.GetConversationById(conversationId);//Tom 获取到这个对话对象
    await conversation.JoinAsync();//Tom 主动加入到对话中
}
```




该群的其他成员（比如 Bob）会收到该操作的事件回调:

* 如果 Bob 仅仅是登录了应用，并没有加载具体的对话到本地，他只会激发 `AVIMClient` 层级上的回调，代码如下:

```c#
public async void BobOnTomJoined_S1()
{
    AVIMClient client = new AVIMClient("Bob");
    await client.ConnectAsync();

    client.OnConversationMembersChanged += (s, e) =>
    {
        switch (e.AffectedType)
        {
            case AVIMConversationEventType.MembersJoined:
                {
                    IList<string> joinedMemberClientIds = e.AffectedMembers;//这里就是本次加入的 ClientIds
                    string clientId = joinedMemberClientIds.FirstOrDefault();//因为我们已知本次操作只有 Tom 一个人加入了对话，所以这样就可以直接读取到 Tom 的 clientId
                    //开发者可以在这里添加自己的业务逻辑
                }
                break;
        }
    };
}
```

* 如果 Bob 不但登录了，还在客户端加载了当前这个对话，那么他不但会激发 `AVIMClient` 层级上的回调，也会激发 `AVIMConversation` 层级上相关回调，代码如下：

```c#
public async void BobOnTomJoined_S2()
{
    AVIMClient client = new AVIMClient("Bob");
    await client.ConnectAsync();

    client.OnConversationMembersChanged += (s, e) =>
    {
        switch (e.AffectedType)
        {
            case AVIMConversationEventType.MembersJoined:
                {
                    IList<string> joinedMemberClientIds = e.AffectedMembers;//这里就是本次加入的 ClientIds
                    string clientId = joinedMemberClientIds.FirstOrDefault();//因为我们已知本次操作只有 Tom 一个人加入了对话，所以这样就可以直接读取到 Tom 的 clientId
                    //开发者可以在这里添加自己的业务逻辑
                }
                break;
        }
    };

    string conversationId = "551260efe4b01608686c3e0f";

    AVIMConversation conversation = client.GetConversationById(conversationId);//Bob 获取到这个对话的对象

    conversation.OnMembersJoined += (s, e) =>
    {
        IList<string> joinedMemberClientIds = e.AffectedMembers;//这里就是本次加入的 ClientIds
        string clientId = joinedMemberClientIds.FirstOrDefault();//因为我们已知本次操作只有 Tom 一个人加入了对话，所以这样就可以直接读取到 Tom 的 clientId
    };
}
```



#### 添加其他成员

Jerry 想再把 Mary 加入到对话中，需要如下代码帮助他实现这个功能：


```c#
public async void InviteMaryAsync()
{
    AVIMClient client = new AVIMClient("Jerry");
    await client.ConnectAsync();

    string conversationId = "551260efe4b01608686c3e0f";//对话的 Id
    AVIMConversation conversation = client.GetConversationById(conversationId);//Jerry 获取到这个对话的对象
    await conversation.AddMembersAsync("Mary");//Jerry 把 Mary 加入到对话
}
```


该对话的其他成员（例如 Harry）也会受到该项操作的影响，收到事件被响应的通知，类似于第一小节 [自身主动加入](#自身主动加入) 中**Tom 加入对话之后，Bob 受到的影响。**


邀请成功以后，相关方收到通知的时序是这样的：

No.|操作者（管理员）|被邀请者|其他人
---|---|---|---
1|发出请求 addMembers| | 
2| |收到 onInvited 通知| 
3|收到 onMemberJoined 通知| | 收到 onMemberJoined 通知


>注意：如果在进行邀请操作时，被邀请者不在线，那么通知消息并不会被离线缓存，所以等到 Ta 再次上线的时候将不会收到通知。

#### 自身退出对话
这里一定要区分**自身退出对话**的主动性，它与**自身被动被踢出**（下一小节）在逻辑上完全是不一样的。

Tom 主动从对话中退出，他需要如下代码实现需求：


```c#
public async void InitiativeLeftAsync()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();

    string conversationId = "551260efe4b01608686c3e0f";//获取 Jerry 创建的对话的 Id
    AVIMConversation conversation = client.GetConversationById(conversationId);//Tom 获取到这个对话的对象
    await conversation.LeftAsync();//Tom 主动从对话中退出
}
``` 


#### 剔除其他成员

Harry 被 William 从对话中删除。实现代码如下（关于 William 如何获得权限在后面的 [签名和安全](#签名和安全) 中会做详细阐述，此处不宜扩大话题范围。）：


```c#
public async void WilliamKickHarryOutAsync()
{
    AVIMClient client = new AVIMClient("William");
    await client.ConnectAsync();

    string conversationId = "551260efe4b01608686c3e0f";//对话的 Id
    AVIMConversation conversation = client.GetConversationById(conversationId);//William 获取到这个对话的对象
    await conversation.RemoveMembersAsync("Harry");//William 把 Harry 从对话中剔除
}
```



以上的操作可归纳为：

1. 假如对话中已经有了 A 和 C

B 的操作|对 B 的影响|对 A、C 的影响
---|---|---
B 加入| `OnConversationMembersChanged && OnJoined`|`OnConversationMembersChanged && OnMembersJoined`
B 再离开|`OnConversationMembersChanged && OnLeft`|`OnConversationMembersChanged && OnMembersLeft`

2. 假如对话中已经有了 A 和 C

A 对 B 的操作 | 对 B 的影响|对 C 的影响
--- | ------------ | -------------|
A 添加 B | `OnConversationMembersChanged && OnInvited`|`OnConversationMembersChanged && OnMembersJoined`
A 再踢出 B|`OnConversationMembersChanged && OnKicked`|`OnConversationMembersChanged && OnMembersLeft`


>注意：如果在进行踢人操作时，被踢者不在线，那么通知消息并不会被离线缓存，所以等到 Ta 再次上线的时候将不会收到通知。

#### 查询成员数量
 `AVIMConversation.CountMembersAsync` 这个方法返回的是实时数据：


```c#
public async void CountMembers_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端

    AVIMConversation conversation = (await client.GetQuery().FindAsync()).FirstOrDefault();//获取对话列表，找到第一个对话
    int membersCount = await conversation.CountMembersAsync();
}
```


### 对话的属性管理

对话实例（AVIMConversation）与控制台中 `_Conversation` 表是一一对应的，默认提供的属性的对应关系如下：


AVIMConversation 属性名 | _Conversation 字段|含义
--- | ------------ | -------------
`ConversationId`| `objectId` |全局唯一的 Id
`Name` |  `name` |成员共享的统一的名字
`MemberIds`|`m` |成员列表
`Creator` | `c` |对话创建者
`LastMessageAt` | `lm` |对话最后一条消息发送的时间
`Attributes`| `attr`|自定义属性
`IsTransient`|`tr`|是否为聊天室（暂态对话）


#### 名称

这是一个全员共享的属性，它可以在创建时指定，也可以在日后的维护中被修改。

Tom 想建立一个名字叫「喵星人」 对话并且邀请了好友 Black 加入对话：


```c#
public async void CreateConversationAsync()
{
    AVIMClient client = new AVIMClient("Tom");
    string anotherCat = "Black";
    await client.ConnectAsync();

    AVIMConversation conversation = await client.CreateConversationAsync(anotherCat, "喵星人");
}
```


Black 发现对话名字不够酷，他想修改成「聪明的喵星人」 ，他需要如下代码：


```c#
public async void UpdateConversationAsync()
{
    AVIMClient client = new AVIMClient("Black");
    await client.ConnectAsync();//Balck 登录

    AVIMConversation conversation = client.GetConversationById("55117292e4b065f7ee9edd29");//获取 Tom 创建的对话

    conversation.Name = "聪明的喵星人";//修改名称

    await conversation.SaveAsync();//保存到云端
}
```


####  成员

是当前对话中所有成员的 `clientId`。默认情况下，创建者是在包含在成员列表中的，直到 TA 退出对话。

>**强烈建议开发者切勿在控制台中对其进行修改**。所有关于成员的操作请参照上一章节中的 [对话的成员管理](#对话的成员管理) 来进行。

#### 静音
假如某一用户不想再收到某对话的消息提醒，但又不想直接退出对话，可以使用静音操作，即开启「免打扰模式」。

比如 Tom 工作繁忙，对某个对话设置了静音：


```c#
public async void MuteConversationAsync()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录

    string conversationId = "551260efe4b01608686c3e0f";//对话的 Id
    AVIMConversation conversation = client.GetConversationById(conversationId);//Tom 获取到这个对话的对象
    await conversation.MuteAsync();//Tom 设置静音
}
```


>设置静音之后，iOS 和 Windows Phone 的用户就不会收到推送消息了。

与之对应的就是取消静音的操作，即取消免打扰模式可使用 `UnmuteAsync()` 方法。此操作会修改云端 `_Conversation` 里面的 `mu` 属性。**强烈建议开发者切勿在控制台中对 `mu` 随意进行修改**。

#### 创建者

即对话的创建者，它的值是对话创建者的 `clientId`。

它等价于 QQ 群中的「群创建者」，但区别于「群管理员」。比如 QQ 群的「创建者」是固定不变的，它的图标颜色与「管理员」的图标颜色都不一样。所以根据对话中成员的 `clientId` 是否与 `AVIMConversation.Creator` 一致就可以判断出他是不是群的创建者。

#### 自定义属性

通过该属性，开发者可以随意存储自己的键值对，为对话添加自定义属性，来满足业务逻辑需求。

给某个对话加上两个自定义的属性：type = "private"（类型为私有）、isSticky = true（置顶显示）：


```c#
public async void CreateConversationWithCustomAttributesAsync()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();
    IDictionary<string, object> attr = new Dictionary<string, object>();
    attr.Add("type", "private");
    attr.Add("isSticky", true);
    AVIMConversation conversation = await client.CreateConversationAsync("Jerry", "猫和老鼠", attr);//创建对话的时候传入即可。
}
```




**自定义属性在 SDK 级别是对所有成员可见的**。如果要控制所谓的可见性，开发者需要自己维护这一属性的读取权限。要对自定义属性进行查询，请参见 [对话的查询](#对话的查询)。

### 对话的查询

#### 对话的有效期

一个对话（包括普通、暂态、系统对话）如果一年内没有通过 SDK 或者 REST API 发送过新的消息，或者它在 `_Conversation` 表中的任意字段没有被更新过，即被视为**不活跃对话**，云端会自动将其删除。（查询对话的消息记录并不会更新 `_Conversation` 表，所以只查询不发送消息的对话仍会被视为不活跃对话。）

不活跃的对话被删除后，当客户端再次通过 SDK 或 REST API 对其进行查询或发送消息时，会遇到 
`4401 INVALID_MESSAGING_TARGET` 错误，表示该对话已经不存在了。同时，与该对话相关的消息历史也无法获取。

反之，活跃的对话会一直保存在云端。

<!-- #### 基础查询 -->

#### 根据 id 查询

假如已知某一对话的 Id，可以使用它来查询该对话的详细信息：


```c#
 public async void QueryByIdAsync()
 {
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端
    AVIMConversation conversation = await client.GetQuery().GetAsync("551260efe4b01608686c3e0f");
 }
```


#### 对话列表

用户登录进应用后，获取最近的 10 个对话（包含暂态对话，如聊天室）：


```c#
public async void CountMembers_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端

    AVIMConversation conversation = (await client.GetQuery().FindAsync()).FirstOrDefault();//获取对话列表，找到第一个对话
    int membersCount = await conversation.CountMembersAsync();
}
```


对话的查询默认返回 10 个结果，若要更改返回结果数量，请设置 `limit` 值。


```c#
// 构建查询
AVIMConversationQuery conversationQuery = client.GetQuery().Limit(20);
// 查询 Tom 所在的最近 20 个活跃的对话
var conversationList = await conversationQuery.FindAsync();
```


#### 条件查询

##### 构建查询

对话的条件查询需要注意的对话属性的存储结构，在对话的属性一章节我们介绍的对话的几个基本属性，这些属性都是 SDK 提供的默认属性，根据默认属性查询的构建如下：


```
AVIMConversationQuery query = client.GetQuery();

// 查询对话名称为「LeanCloud 粉丝群」的对话
query.WhereEqualTo("attr.topic", "LeanCloud 粉丝群");

// 查询对话名称包含 「LeanCloud」 的对话
query.WhereContains("attr.topic", "LeanCloud");

// 查询过去24小时活跃的对话
query.WhereGreaterThan("lm", DateTime.Now.AddDays(-1));
```


相对于默认属性的查询，开发者自定义属性的查询需要在构建查询的时在关键字（key）前加上一个特殊的前缀：`attr`，不过每个 SDK 都提供相关的快捷方式帮助开发者方便的构建查询：


```
// 查询话题为 DOTA2 对话
query.WhereEqualTo("attr.topic", "DOTA2");

// 查询等级大于 5 的对话
query.WhereGreaterThan("level".InsertAttrPrefix(), 5);
```
在 Dotnet SDK 中提供了 `InsertAttrPrefix` 的拓展方法，为自定义属性查询添加 `attr` 前缀：

```
// 查询话题为 DOTA2 对话
query.WhereEqualTo("topic".InsertAttrPrefix(), "DOTA2");
// 它与下面这行代码是一样的
query.WhereEqualTo("attr.topic", "DOTA2");
```


默认属性以及自定义属性的区分便于 SDK 后续的内建属性拓展和维护，自定义属性的开放有利于开发者在可控的范围内进行查询的构建。


条件查询又分为：比较查询、正则匹配查询、包含查询，以下会做分类演示。


#### 比较查询

比较查询在一般的理解上都包含以下几种：


逻辑操作 | `AVIMConversationQuery` 对应的方法|
---|---
等于 | `WhereEqualTo`
不等于 |  `WhereNotEqualTo` 
大于 | `WhereGreaterThan`
大于等于 | `WhereGreaterThanOrEqualTo` 
小于 | `WhereLessThan`
小于等于 | `WhereLessThanOrEqualTo`


比较查询最常用的是等于查询：


```c#
public async void WhereEqualTo_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端
    AVIMConversationQuery query = client.GetQuery().WhereEqualTo("attr.topic", "movie");//构建 topic 是 movie 的查询
    var result = await query.FindAsync();//执行查询
}
```


目前条件查询只针对 `AVIMConversation` 对象的自定义属性进行操作，也就是针对 `_Conversation` 表中的 `attr` 字段进行 `AVQuery` 查询。


实际上为了方便开发者自动为了自定义属性的 key 值增加 `attr.` 的前缀，SDK 特地添加了一个针对 `string` 类型的[拓展方法](https://msdn.microsoft.com/zh-cn/library/bb383977.aspx)

```c#
/// <summary>
/// 为聊天的自定义属性查询自动添加 "attr." 的前缀
/// </summary>
/// <param name="key">属性 key 值，例如 type </param>
/// <returns>添加前缀的值，例如，attr.type </returns>
public static string InsertAttrPrefix(this string key)
{
    return key.Insert(0, "attr.");
}
```

导入 SDK 之后在 Visual Studio 里面使用 `string` 类型的时候可以智能感应提示该方法。

```c#
AVIMConversationQuery query = client.GetQuery().WhereEqualTo("topic".InsertAttrPrefix(), "movie");//这样就可以实现自动为 `topic` 添加 `attr.` 前缀的效果的效果。
```


下面检索一下类型不是私有的对话：


```c#
public async void WhereNotEqualTo_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端
    AVIMConversationQuery query = client.GetQuery().WhereNotEqualTo("attr.type", "private");//构建 type 不等于 movie 的查询
    var result = await query.FindAsync();//执行查询
}
```


对于可以比较大小的整型、浮点等常用类型，可以参照以下示例代码进行扩展：


```c#
public async void WhereGreaterThan_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端
    AVIMConversationQuery query = client.GetQuery().WhereGreaterThan("attr.age", 18);//构建 年龄大于 18 的查询
    var result = await query.FindAsync();//执行查询
}
```


#### 正则匹配查询


匹配查询指的是在 `AVIMConversationQuery` 中以 `WhereMatches` 为前缀的方法。

Match 类方法的最大便捷之处在于可以使用正则表达式来匹配数据，这样使得客户端在构建基于正则表达式的查询时可以利用 .NET 里面诸多已经熟悉了的概念和接口。


比如要查询所有 language 是中文的对话：


```c#
public async void WhereMatchs_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端
    AVIMConversationQuery query = client.GetQuery().WhereMatches("attr.language", "[\u4e00-\u9fa5]");//查询 language 是中文字符的对话
    var result = await query.FindAsync();//执行查询
}
```


#### 包含查询

包含查询是指方法名字包含 `Contains` 单词的方法，例如查询关键字包含「教育」的对话：


```c#
public async void WhereContains_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端
    AVIMConversationQuery query = client.GetQuery().WhereContains("attr.keywords", "教育");//查询 keywords 包含教育
    var result = await query.FindAsync();//执行查询
}
```


另外，包含查询还能检索与成员相关的对话数据。以下代码将帮助 Tom 查找出 Jerry 以及 Bob 都加入的对话：


```c#
public async void QueryMembers_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");

    IList<string> clientIds = new List<string>();
    clientIds.Add("Bob");
    clientIds.Add("Jerry");

    AVIMConversationQuery query = client.GetQuery().WhereContainedIn<string>("m", clientIds);//查询对话成员 Bob 以及 Jerry 的对话
    var result = await query.FindAsync();//执行查询
}
```




#### 组合查询

组合查询的概念就是把诸多查询条件合并成一个查询，再交给 SDK 去云端进行查询。


我们的 SDK 在查询风格上一直保持以链式方式来创建符合自己业务逻辑的组合条件。
例如，要查询年龄小于 18 岁，并且关键字包含「教育」的对话：


```c#
public async void CombinationQuery_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端
    AVIMConversationQuery query = client.GetQuery().WhereContains("attr.keywords", "教育").WhereLessThan("attr.age", 18);//查询 keywords 包含教并且年龄小于18的对话
    var result = await query.FindAsync();//执行查询
}
```


只要查询构建得合理，开发者完全不需要担心组合查询的性能。


## 聊天室

聊天室本质上就是一个对话，所以上面章节提到的**所有属性、方法、操作以及管理都适用于聊天室**。它仅仅在逻辑上是一种暂态、临时的对话，应用场景有弹幕、直播等等。

聊天室与普通对话或群聊不一样的地方具体体现为：

* **无人数限制**（而普通对话最多允许 500 人加入）<br/><span class="text-muted">从实际经验来看，为避免过量消息刷屏而影响用户体验，我们建议每个聊天室的<u>上限人数控制在 **5000 人**左右</u>。开发者可以考虑从应用层面将大聊天室拆分成多个较小的聊天室。</span>
* 不支持查询成员列表，但可以通过相关 API 查询在线人数。
* 不支持离线消息、离线推送通知、消息回执等功能。
* 没有成员加入、成员离开的通知。
* 一个用户一次登录只能加入一个聊天室，加入新的聊天室后会自动离开原来的聊天室。
* 加入后半小时内断网重连会自动加入原聊天室，超过这个时间则需要重新加入。

### 创建聊天室



比如喵星球正在直播选美比赛，主持人 Tom 创建了一个临时对话，与喵粉们进行互动：


```c#
public async void ChatRoom_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端
    var chatroom = client.CreateConversationAsync(null, "HelloKitty PK 加菲猫", null, true);
    //最后一个参数，transient 如果为 true 就说明是聊天室，逻辑上就是暂态对话
}
```



另外，为了方便开发者快速创建聊天室，SDK 提供了一个快捷方法创建聊天室：

```c#
var chatroom = client.CreateChatRoomAsync("HelloKitty PK 加菲猫");//可以理解为一个语法糖，与调用 `CreateConversationAsync` 没有本质的区别。
```


### 查询在线人数

 `AVIMConversation.CountMembersAsync`  可以用来查询普通对话的成员总数，在聊天室中，它返回的就是实时在线的人数：


```c#
public async void CountMembers_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端

    AVIMConversation conversation = (await client.GetQuery().FindAsync()).FirstOrDefault();
    int membersCount = await conversation.CountMembersAsync();
}
```


### 查找聊天室

开发者需要注意的是，通过 `AVIMClient.GetQuery()` 这样得到的 `AVIMConversationQuery` 实例默认是查询全部对话的，也就是说，如果想查询指定的聊天室，需要额外再调用 以 `Where` 开头的 方法来限定更多的查询条件：

比如查询主题包含「奔跑吧，兄弟」的聊天室：


```c#
public async void QueryChatRoom_SampleCode()
{
    AVIMClient client = new AVIMClient("Tom");
    await client.ConnectAsync();//Tom 登录客户端

    AVIMConversationQuery query = client.GetQuery().WhereContains("topic".InsertAttrPrefix(), "奔跑吧，兄弟").WhereEqualTo("tr", true);
    //比如我想查询主题包含《奔跑吧，兄弟》的聊天室
    var result = await query.FindAsync();//执行查询
}
```


从代码上可以看出，仅仅是多了一个额外的 `WhereEqualTo("tr", true)` 的链式查询即可。

## 聊天记录

聊天记录一直是客户端开发的一个重点，QQ 和 微信的解决方案都是依托客户端做缓存，当收到一条消息时就按照自己的业务逻辑存储在客户端的文件或者是各种客户端数据库中。

我们的 SDK 会将普通的对话消息自动保存在云端，开发者可以通过 AVIMConversation 来获取该对话的所有历史消息。

获取该对话中最近的 N 条（默认 20，最大值 1000）历史消息，通常在第一次进入对话时使用：


```
//Tom 用自己的名字作为 ClientId 建立了一个 AVIMClient
AVIMClient client = new AVIMClient("Tom");

//Tom 登录到系统
await client.ConnectAsync();

//打开已存在的对话
AVIMConversation conversaion = client.GetConversationById("551260efe4b01608686c3e0f");

//查询最新的 10 条消息
await conversaion.QueryHistoryAsync(10);
```


获取某条消息之前的历史消息，通常用在翻页加载更多历史消息的场景中。

```
// 获取早于 messageId = grqEG2OqSL+i8FSX9j3l2g 而且时间戳早于 1436137606358 的 10 条消息
con.QueryHistoryAsync("grqEG2OqSL+i8FSX9j3l2g", 1436137606358, 10);
```


翻页获取历史消息的时候，LeanCloud 云端是从某条消息开始，往前查找所指定的 N 条消息来返回给客户端。为此，获取历史消息需要传入三个参数：

* 起始消息的 messageId
* 起始消息的发送时间戳
* 需要获取的消息条数

假如每一页为 10 条信息，下面的代码将演示如何翻页：


```
// 获取最新的 10 条消息
IEnumerable<AVIMMessage> pageContent = await con.QueryHistoryAsync(10);
// 以第 10 条为分界点
AVIMMessage pager = pageContent.Last();
// 查询第 10 条之前的 10条消息
IEnumerable<AVIMMessage> pageContent2 = await con.QueryHistoryAsync(pager.Id, pager.ServerTimestamp, 10);
```




## 客户端事件

### 网络状态响应

当网络连接出现中断、恢复等状态变化时，可以通过以下接口来处理响应：

 
在 `AVIMClient` 中有的 `Satus` 判断当前连接状态，如下表：

枚举名称 |整型值 | 解释
---|---|---
ClientStatus.None  |  0 |  未知，初始值
ClientStatus.Online | 1 |  在线
ClientStatus.Offline | 2 | 离线


>注意：网络状态在短时间内很可能会发生频繁变化，但这并不代表对话的接收与发送一定会受到影响，因此开发者在处理此类事件响应时，比如更新 UI，要适应加入更多的逻辑判断，以免影响用户的使用体验。


### 断线重连
目前 .NET SDK 默认内置了断线重连的功能，从客户端与云端建立连接成功开始，只要没有调用退出登录的接口，SDK 会一直尝试和云端保持长连接，此时 AVIMClient 的状态可以通过 [网络状态响应](#网络状态响应)接口得到。

**注意：用户如果自行实现了重连逻辑可能会报出 1001 错误**。


### 退出登录

要退出当前的登录状态或要切换账户，方法如下：


```
client.DisconnectAsync();
```




## 安全与签名

在继续阅读下文之前，请确保你已经对 [实时通信服务开发指南 &middot; 权限和认证](realtime_v2.html#权限和认证) 有了充分的了解。

### 实现签名工厂

为了满足开发者对权限和认证的要求，我们设计了操作签名的机制。签名启用后，所有的用户登录、对话创建/加入、邀请成员、踢出成员等登录都需要验证签名，这样开发者就对消息具有了完全的掌控。


我们强烈推荐启用签名，具体步骤是 [控制台 > 设置 > 应用选项](/app.html?appid={{appid}}#/permission)，勾选 **聊天、推送** 下的 **聊天服务，启用签名认证**。


`AVIMClient` 有一个属性：

```c#
/// <summary>
/// 获取签名的接口
/// </summary>
public ISignatureFactoryV2 SignatureFactory { get; set; }
```
是预留给开发者实现签名需求的接口，开发者只需要在登录之前实现这个接口即可。

###  签名的云引擎实例
为了方便开发者理解签名，我们特地开源了签名的[云引擎实例](https://github.com/leancloud/realtime-messaging-signature-cloudcode)，只要按照要求正确配置，就可以在客户端通过调用云引擎的具体的函数实现签名。

演示实例的步骤：

* 首先您需要下载最新版本的[云引擎实例](https://github.com/leancloud/realtime-messaging-signature-cloudcode)到本地，然后部署到您的应用中，详细请参考[云引擎命令行工具使用详解](leanengine_cli.html#)

* 其次，在 Visual Studio 中，新建一个类叫做 `SampleSignatureFactory` ，把下面这段代码拷贝到其中：

```c#
/// <summary>
/// 签名示例类，推荐开发者用这段代码理解签名的整体概念，正式生产环境，请慎用
/// </summary>
public class SampleSignatureFactory : ISignatureFactoryV2
{
    /// <summary>
    /// 为更新对话成员的操作进行签名
    /// </summary>
    /// <param name="conversationId">对话的Id</param>
    /// <param name="clientId">当前的 clientId</param>
    /// <param name="targetIds">被操作所影响到的 clientIds</param>
    /// <param name="action">执行的操作，目前只有 add，remove</param>
    /// <returns></returns>
    public Task<AVIMSignatureV2> CreateConversationSignature(string conversationId, string clientId, IList<string> targetIds, string action)
    {
        var data = new Dictionary<string, object>();

        data.Add("client_id", clientId);//表示当前是谁在操作。
        data.Add("member_ids", targetIds);//memberIds不要包含当前的ClientId。
        data.Add("conversation_id", conversationId);//conversationId是签名必须的参数。
           
        data.Add("action", action);//conversationId是签名必须的参数。
            
            
        //调用云引擎进行签名。
        return AVCloud.CallFunctionAsync<IDictionary<string, object>>("actionOnCoversation", data).ContinueWith<AVIMSignatureV2>(t =>
        {
            return MakeSignature(t.Result); ;//拼装成一个 Signature 对象
        });
        //以上这段代码，开发者无需手动调用，只要开发者对一个 AVIMClient 设置了 SignatureFactory，SDK 会在执行对应的操作时主动调用这个方法进行签名。
    }
    /// <summary>
    /// 登录签名
    /// </summary>
    /// <param name="clientId">当前的 clientId</param>
    /// <returns></returns>
    public Task<AVIMSignatureV2> CreateConnectSignature(string clientId)
    {
        var data = new Dictionary<string, object>();

        data.Add("client_id", clientId);//表示当前是谁要求连接服务器。 

        //调用云引擎进行签名。
        return AVCloud.CallFunctionAsync<IDictionary<string, object>>("connect", data).ContinueWith<AVIMSignatureV2>(t =>
        {
            return MakeSignature(t.Result); ;//拼装成一个 Signature 对象
        });
    }

    /// <summary>
    /// 为创建对话签名
    /// </summary>
    /// <param name="clientId">当前的 clientId </param>
    /// <param name="targetIds">被影响的 clientIds </param>
    /// <returns></returns>
    public Task<AVIMSignatureV2> CreateStartConversationSignature(string clientId, IList<string> targetIds)
    {
        var data = new Dictionary<string, object>();

        data.Add("client_id", clientId);//表示当前是谁在操作。
        data.Add("member_ids", targetIds);//memberIds不要包含当前的ClientId。

        //调用云引擎进行签名。
        return AVCloud.CallFunctionAsync<IDictionary<string, object>>("startConversation", data).ContinueWith<AVIMSignatureV2>(t =>
        {
            return MakeSignature(t.Result); ;//拼装成一个 Signature 对象
        });
    }

    /// <summary>
    /// 获取签名信息并且把它返回给 SDK 去进行下一步的操作
    /// </summary>
    /// <param name="dataFromCloudcode"></param>
    /// <returns></returns>
    protected AVIMSignatureV2 MakeSignature(IDictionary<string, object> dataFromCloudcode)
    {
        AVIMSignatureV2 signature = new AVIMSignatureV2();
        signature.Nonce = dataFromCloudcode["nonce"].ToString();
        signature.SignatureContent = dataFromCloudcode["signature"].ToString();
        signature.Timestamp = (long)dataFromCloudcode["timestamp"];
        return signature;//拼装成一个 Signature 对象
    }

    /// <summary>
    /// 为获取聊天记录的操作签名
    /// </summary>
    /// <param name="clientId">当前的 clientId </param>
    /// <param name="conversationId">对话 Id</param>
    /// <returns></returns>
    public Task<AVIMSignatureV2> CreateQueryHistorySignature(string clientId, string conversationId)
    {
        var data = new Dictionary<string, object>();

        data.Add("client_id", clientId);//表示当前是谁在操作。
        data.Add("convid", conversationId);//memberIds不要包含当前的ClientId。

        //调用云引擎进行签名。
        return AVCloud.CallFunctionAsync<IDictionary<string, object>>("queryHistory", data).ContinueWith<AVIMSignatureV2>(t =>
        {
            return MakeSignature(t.Result); ;//拼装成一个 Signature 对象
        });
    }
}
```

*  然后在调用如下代码进行测试（确保您已经在控制台开启了聊天签名的服务，否则签名操作无效）：

```c#
AVIMClient client = new AVIMClient("Tom");
client.SignatureFactory = new SampleSignatureFactory();//这里是一个开发者自己实现的接口的具体的类
await client.ConnectAsync();//Tom 登录客户端
```


> 需要强调的是：开发者切勿在客户端直接使用 MasterKey 进行签名操作，因为 MaterKey 一旦泄露，会造成应用的数据处于高危状态，后果不容小视。因此，强烈建议开发者将签名的具体代码托管在安全性高稳定性好的云端服务器上（例如 LeanCloud 云引擎）。

为了帮助开发者理解云端签名的算法，我们开源了一个用 Node.js + 云引擎实现签名的云端，供开发者学习和使用：[LeanCloud 实时通信云引擎签名 Demo](https://github.com/leancloud/realtime-messaging-signature-cloudcode)。





## 实时通信云引擎 Hook
一些应用因其特殊的业务逻辑需要在消息发送时或者消息接收时插入一定的逻辑，因此我们也提供了[实时通信云引擎 Hook](realtime_v2.html#云引擎_Hook)。

## 实时通信 REST API
有些应用需要在用户登录之前就提前创建一些对话或者是针对对话进行操作，因此可以通过[实时通信 REST API](realtime_rest_api.html)来实现。

## 常见问题

**我只想实现两个用户的私聊，是不是每次都得重复创建对话？**

不需要重复创建。我们推荐的方式是开发者可以用**自定义属性**来实现对私聊和群聊的标识，并且在进行私聊之前，需要查询当前两个参与对话的 ClientId 是否之前已经存在一个私聊的对话了。


**某个成员退出对话之后，再加入，在他离开的这段期间内的产生的聊天记录，他还能获取么？**

可以。目前聊天记录从属关系是属于对话的，也就是说，只要对话 Id 不变，不论人员如何变动，只要这个对话产生的聊天记录，当前成员都可以获取。

**我自己没有云端，如何实现签名的功能？**

LeanCloud 云引擎提供了托管 Python 和 Node.js 运行的方式，开发者可以所以用这两种语言按照签名的算法实现签名，完全可以支持开发者的自定义权限控制。

**客户端连接被关闭**

导致这一情况的原因很多，请参考 [云端错误码说明](realtime_v2.html#云端错误码说明)。


