



# iOS 实时通信开发指南

## 简介

实时通信服务可以让你一行后端代码都不用写，就能做出一个功能完备的实时聊天应用，或是一个实时对战类的游戏。所有聊天记录都保存在云端，离线消息会通过消息推送来及时送达，推送消息文本可以灵活进行定制。

>在继续阅读本文档之前，请先阅读[《实时通信开发指南》](./realtime_v2.html)，了解一下实时通信的基本概念和模型。



### 文档贡献
我们欢迎和鼓励大家对本文档的不足提出修改建议。请访问我们的 [Github 文档仓库](https://github.com/leancloud/docs) 来提交 Pull Request。

## Demo
相比阅读文档，如果你更喜欢从代码入手了解功能的具体实现，可以下载 Demo 来研究：


* [LeanMessage](https://github.com/leancloud/LeanMessage-Demo)（推荐）
* [LeanChat](https://github.com/leancloud/leanchat-ios)


我们把所有 Demo 项目放在了 [LeanCloud Demos 资源库](https://github.com/leancloud/leancloud-demos) 中，方便大家浏览和参考。

## 安装和初始化

请参考详细的 [iOS / OS X SDK 安装指南](sdk_setup-ios.html)。



### 示例代码约定

在以下示例代码中，若无特殊说明，所有代码均位于下面这个类的实现文件中：

```objc
@interface TomAndJerryEpisode : NSObject

@end

@implementation TomAndJerryEpisode

// 所有示例代码均位于此处

@end
```

对于像 `self.prop` 这样的引用，我们约定 `prop` 属性在 `TomAndJerryEpisode` 类中已经有了正确的实现。例如：

```
self.client = [[AVIMClient alloc] init];
```

若想让它正确执行，需要在当前的 `ViewController.m` 中添加一个 `AVIMClient` 属性：

```
@property (nonatomic, strong) AVIMClient *client;
```

以此类推。

我们也故意省略了错误处理，有时还会省略一些上下文逻辑，目的是让示例代码简明扼要。

示例代码并不是最佳实践，仅为演示 SDK 接口的基础用法。


## 单聊

我们先从最简单的环节入手。此场景类似于微信的私聊、微博的私信和 QQ 单聊。我们创建了一个统一的概念来描述聊天的各种场景：`AVIMConversation`（对话），在[《实时通信开发指南》](./realtime_v2.html) 里也有相关的详细介绍。

### 发送消息

![Tom and Jerry](images/tom-and-jerry-avatar.png)

Tom 想给 Jerry 发一条消息，实现代码如下：


```objc
- (void)tomSendMessageToJerry {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Jerry 的会话
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 发了一条消息给 Jerry
            [conversation sendMessage:[AVIMTextMessage messageWithText:@"耗子，起床！" attributes:nil] callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
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



### 接收消息

要让 Jerry 收到 Tom 的消息，需要这样写：


```objc
- (void)jerryReceiveMessageFromTom {
    // Jerry 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Jerry"];

    // 设置 client 的 delegate，并实现 delegate 方法
    self.client.delegate = self;

    // Jerry 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // ...
    }];
}

#pragma mark - AVIMClientDelegate

// 接收消息的回调函数
- (void)conversation:(AVIMConversation *)conversation didReceiveTypedMessage:(AVIMTypedMessage *)message {
    NSLog(@"%@", message.text); // 耗子，起床！
}
```




## 群聊

对于多人同时参与的固定群组，我们有成员人数限制，最大不能超过 500 人。对于另外一种多人聊天的形式，譬如聊天室，其成员不固定，用户可以随意进入发言的这种「临时性」群组，后面会单独介绍。

### 发送消息

Tom 想建立一个群，把自己好朋友都拉进这个群，然后给他们发消息，他需要做的事情是：

1. 建立一个朋友列表
2. 新建一个对话，把朋友们列为对话的参与人员
3. 发送消息


```objc
- (void)tomCreateConversationWithFriends {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与朋友们的会话
        NSArray *friends = @[@"Jerry", @"Bob", @"Harry", @"William"];
        [self.client createConversationWithName:@"Tom and friends" clientIds:friends callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 发了一条消息给朋友们
            [conversation sendMessage:[AVIMTextMessage messageWithText:@"你们在哪儿？" attributes:nil] callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
}
```

### 接收消息

群聊的接收消息与单聊的接收消息在代码写法上是一致的。


```objc
- (void)bobReceiveMessageFromFriends {
    // Bob 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Bob"];

    // 设置 client 的 delegate，并实现 delegate 方法
    self.client.delegate = self;

    // Bob 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // ...
    }];
}

#pragma mark - AVIMClientDelegate

- (void)conversation:(AVIMConversation *)conversation didReceiveTypedMessage:(AVIMTypedMessage *)message {
    NSLog(@"%@", message.text); // 你们在哪儿？

    AVIMTextMessage *reply = [AVIMTextMessage messageWithText:@"Tom，我在 Jerry 家，你跟 Harry 什么时候过来？还有 William 和你在一起么？" attributes:nil];

    [conversation sendMessage:reply callback:^(BOOL succeeded, NSError *error) {
        if (succeeded) {
            NSLog(@"回复成功！");
        }
    }];
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


```objc
- (void)tomSendLocalImageToJerry {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Jerry 的会话
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 创建了一个图像消息
            NSString *filePath = [self imagePath];
            NSDictionary *attributes = @{ @"location": @"旧金山" };
            AVIMImageMessage *message = [AVIMImageMessage messageWithText:@"发自我的 iPhone" attachedFilePath:filePath attributes:attributes];

            // Tom 将图像消息发给 Jerry
            [conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
}
```


【场景二】从微博上复制的一个图像链接来创建图像消息：


```objc
- (void)tomSendExternalImageToJerry {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Jerry 的会话
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 发了一张图片给 Jerry
            AVFile *file = [AVFile fileWithURL:[self imageURL]];
            AVIMImageMessage *message = [AVIMImageMessage messageWithText:@"萌妹子一枚" file:file attributes:nil];
            [conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
}
```


以上两种场景对于 SDK 的区别为：

* 场景一：SDK 获取了完整的图像数据流，先上传文件到云端，再将文件的元数据以及 URL 等一并包装，发送出去。

* 场景二：SDK 并没有将图像实际上传到云端，而仅仅把 URL 包装在消息体内发送出去，这种情况下接收方是无法从消息体中获取图像的元信息数据，但是接收方可以自行通过客户端技术去分析图片的格式、大小、长宽之类的元数据。

##### 接收图像消息


在接收图像消息这种富媒体消息时，需要使用 `conversation:didReceiveTypedMessage:` 方法。实际上接收所有富媒体消息都是如此，因为它们都是从 `AVIMTypedMessage` 派生出来的。相关内容可以在下面的 [消息类详解](#消息类详解) 中找到。



```objc
- (void)jerryReceiveImageMessageFromTom {
    // Jerry 创建了一个 client，用自己的名字作为 clientId
    self.clientJerry = [[AVIMClient alloc] initWithClientId:@"Jerry"];
    self.clientJerry.delegate = self;

    // Jerry 打开 client
    [self.clientJerry openWithCallback:^(BOOL succeeded, NSError *error) {
        // ...
    }];
}

#pragma mark - AVIMClientDelegate

- (void)conversation:(AVIMConversation *)conversation didReceiveTypedMessage:(AVIMTypedMessage *)message {
    AVIMImageMessage *imageMessage = (AVIMImageMessage *)message;

    // 消息的 id
    NSString *messageId = imageMessage.messageId;
    // 图像文件的 URL
    NSString *imageUrl = imageMessage.file.url;
    // 发该消息的 ClientId
    NSString *fromClientId = message.clientId;
}
```


#### 音频消息

##### 发送音频消息

发送音频消息的基本流程是：读取音频文件（或者录制音频）> 构建音频消息 > 消息发送。


```objc
- (void)tomSendAudioToJerry {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Jerry 的会话
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 发了一首歌曲给 Jerry
            NSString *path = [[NSBundle mainBundle] pathForResource:@"忐忑" ofType:@"mp3"];
            AVFile *file = [AVFile fileWithName:@"忐忑.mp3" contentsAtPath:path];
            AVIMImageMessage *message = [AVIMImageMessage messageWithText:@"听听人类的神曲~" file:file attributes:nil];
            [conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
}
```


与图像消息类似，音频消息也支持从 URL 构建：


```objc
- (void)tomSendExternalAudioToJerry {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Jerry 的会话
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 发了一首歌曲给 Jerry
            AVFile *file = [AVFile fileWithURL:@"http://ac-lhzo7z96.clouddn.com/1427444393952"];
            AVIMAudioMessage *message = [AVIMAudioMessage messageWithText:@"听听人类的神曲~" file:file attributes:nil];
            [conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
}
```


##### 接收音频消息


与接收图像消息类似，需要使用 `conversation:didReceiveTypedMessage:` 方法来响应，实例代码请参照 [图像消息接收](#接收图像消息)。


#### 视频消息

##### 发送视频消息

与发送音频消息的流程类似，视频的来源可以是手机录制，可以是系统中某一个具体的视频文件：


```objc
- (void)tomSendVideoToJerry {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Jerry 的会话
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 发了一个视频给 Jerry
            NSString *path = [[NSBundle mainBundle] pathForResource:@"BBC_奶酪" ofType:@"mp4"];
            AVFile *file = [AVFile fileWithName:@"BBC_奶酪.mp4" contentsAtPath:path];
            AVIMVideoMessage *message = [AVIMVideoMessage messageWithText:nil file:file attributes:nil];
            [conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
}
```


同样我们也支持从一个视频的 URL 创建视频消息，然后发送出去：


```objc
- (void)tomSendExternalVideoToJerry {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Jerry 的会话
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 发了一段视频给 Jerry
            AVFile *file = [AVFile fileWithURL:@"http://ac-lhzo7z96.clouddn.com/1427267336319"];
            AVIMVideoMessage *message = [AVIMVideoMessage messageWithText:nil file:file attributes:nil];
            [conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
}
```


**注：这里说的 URL 指的是视频文件自身的 URL，而不是视频网站上播放页的 URL。**

##### 接收视频消息


与接收图像消息类似，需要使用 `conversation:didReceiveTypedMessage:` 方法来响应，实例代码请参照 [图像消息接收](#接收图像消息)。




#### 地理位置消息

地理位置消息构建方式如下：


```objc
[AVIMLocationMessage messageWithText:nil latitude:45.0 longitude:34.0 attributes:nil];
```


##### 发送地理位置消息


```objc
- (void)tomSendLocationToJerry {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Jerry 的会话
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 发了一个地理位置给 Jerry
            // NOTE: 开发者更可以通过具体的设备的 API 去获取设备的地理位置
            AVIMLocationMessage *message = [AVIMLocationMessage messageWithText:@"新开的蛋糕店！耗子咱们有福了…" latitude:45.0 longitude:34.0 attributes:nil];
            [conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
}
```


##### 接收地理位置消息


与接收图像消息类似，需要使用 `conversation:didReceiveTypedMessage:` 方法来响应，实例代码请参照 [图像消息接收](#接收图像消息)。



```objc
- (void)conversation:(AVIMConversation *)conversation didReceiveTypedMessage:(AVIMTypedMessage *)message;
```


### 暂态消息

暂态消息不会被自动保存（以后在历史消息中无法找到它），也不支持延迟接收，离线用户更不会收到推送通知，所以适合用来做控制协议。譬如聊天过程中「某某正在输入...」这样的状态信息，就适合通过暂态消息来发送；或者当群聊的名称修改以后，也可以用暂态消息来通知该群的成员「群名称被某某修改为...」。


```objc
typedef NS_ENUM(NSInteger, YourCustomMessageType) {
    YourCustomMessageTypeOperation = 1
};

@interface YourOperationMessage : AVIMTextMessage <AVIMTypedMessageSubclassing>

@end

@implementation YourOperationMessage

+ (AVIMMessageMediaType)classMediaType {
    return YourCustomMessageTypeOperation;
}

@end

@implementation ViewController

+ (void)load {
    // 自定义消息需要注册
    [YourOperationMessage registerSubclass];
}

- (void)tomOpenConversation {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.tomClient = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.tomClient openWithCallback:^(BOOL succeeded, NSError *error) {
        AVIMConversationQuery *query = [self.tomClient conversationQuery];
        // Tom 获取 id 为 551260efe4b01608686c3e0f 的会话
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {
            self.tomConversation = conversation;
        }];
    }];
}

- (void)textFieldDidChange:(UITextField *)textField {
    // 发送一条暂态消息给 Jerry，让 Jerry 知道 Tom 正在输入
    YourOperationMessage *message = [YourOperationMessage messageWithText:@"正在输入……" attributes:nil];
    [self.tomConversation sendMessage:message options:AVIMMessageSendOptionTransient callback:nil];
}

@end
```


而对话中的其他成员在聊天界面中需要有以下代码做出响应：


```objc
- (void)jerryOnline {
    // Jerry 创建了一个 client，用自己的名字作为 clientId
    self.jerryClient = [[AVIMClient alloc] initWithClientId:@"Jerry"];

    // Jerry 打开 client
    [self.jerryClient openWithCallback:^(BOOL succeeded, NSError *error) {
        NSLog("Jerry opened client")
    }];
}

- (void)conversation:(AVIMConversation *)conversation didReceiveTypedMessage:(AVIMTypedMessage *)message {
    if (message.mediaType == YourCustomMessageTypeOperation) {
        NSLog(@"正在输入……");
    }
}
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


这样 iOS 平台上的用户就可以收到消息推送了。当然，前提是应用本身申请到了 RemoteNotification 权限，也将正确的推送证书上传到了 LeanCloud 控制台。


#### 消息送达回执

是指消息被对方收到之后，云端会发送一个回执通知给发送方，表明消息已经送达。
需要注意的是：

> 只有在发送时设置了「等待回执」标记，云端才会发送回执，默认不发送回执。该回执并不代表用户已读。


调用 `sendMessage` 方法时，在 options 中传入 `AVIMMessageSendOptionRequestReceipt`：

```objc
[conversation sendMessage:message options:AVIMMessageSendOptionRequestReceipt callback:^(BOOL succeeded, NSError *error) {
  if (succeeded) {
    NSLog(@"发送成功！需要回执");
  }
}];
```

监听消息是否已送达实现 `conversation:messageDelivered` 即可。
```objc
- (void)conversation:(AVIMConversation *)conversation messageDelivered:(AVIMMessage *)message{
    NSLog(@"%@", @"消息已送达。"); // 打印消息
}
```


### 消息的接收




实时通信 SDK 内部封装了对富媒体消息的支持，所有富媒体消息都是从 AVIMTypedMessage 派生出来的。发送的时候可以直接调用 `[AVIMConversation sendMessage:callback:]` 函数。在接收端，我们也在 `AVIMClientDelegate` 中专门增加了一个回调函数：

```
- (void)conversation:(AVIMConversation *)conversation didReceiveTypedMessage:(AVIMTypedMessage *)message;
```
这样，如果发送端发送的是 AVIMMessage 消息，那么接受端就是 `conversation:didReceiveCommonMessage:` 被调用；如果发送的是 AVIMTypedMessage（及其子类）的消息，那么接受端就是 `conversaion:didReceiveTypedMessage` 被调用。



#### 未拉取的离线消息

未拉取的离线消息指的是客户端尚未主动拉取到本地的消息。

SDK 默认的接收机制是：当客户端上线时，离线消息会自动通过长连接发送至客户端。如果开启了「未读消息」，消息接收机制变为：当客户端上线时，会收到其参与过的会话的离线消息数量，云端不再主动将离线消息通知发送至客户端，转而由客户端负责主动拉取。


要开启未读消息，需要在 AVOSCloud 初始化语句后面加上：

```objc
[AVIMClient setUserOptions:@{
    AVIMUserOptionUseUnread: @(YES)
}];
```

然后使用代理方法 `conversation:didReceiveUnread:` 来从服务端取回未读消息：

```objc
- (void)conversation:(AVIMConversation *)conversation didReceiveUnread:(NSInteger)unread {
  // unread 是未读消息数量，conversation 为所属的会话
  // 没有未读消息就跳过
  if (unread <= 0) return;
  
  // 否则从服务端取回未读消息
  [conversation queryMessagesFromServerWithLimit:unread callback:^(NSArray *objects, NSError *error) {
    if (!error && objects.count) {
      // 显示消息或进行其他处理 
    }
  }];
  // 将这些消息标记为已读 
  [conversation markAsReadInBackground];
}
```


注意：客户端<u>在线上时</u>收到的消息默认为**已读**，不存在未读的情况。因此开发者不需要将这样的消息标记为已读。


### 消息类详解

![message type diagram](images/message_type_diagram.png)



所有消息都是 `AVIMMessage` 的实例，每种消息实例都具备如下属性：

属性|类型|描述
---|---|---
content|NSString|消息内容
clientId|NSString|指消息发送者的 clientId
conversationId|NSString|消息所属对话 id
messageId|NSString|消息发送成功之后，由 LeanCloud 云端给每条消息赋予的唯一 id
sendTimestamp|int64_t|消息发送的时间。消息发送成功之后，由 LeanCloud 云端赋予的全局的时间戳。
deliveredTimestamp|int64_t|消息被对方接收到的时间。消息被接收之后，由 LeanCloud 云端赋予的全局的时间戳。
status|AVIMMessageStatus 枚举|消息状态，有五种取值：<br/><br/>`AVIMMessageStatusNone`（未知）<br/>`AVIMMessageStatusSending`（发送中）<br/>`AVIMMessageStatusSent`（发送成功）<br/>`AVIMMessageStatusDelivered`（被接收）<br/>`AVIMMessageStatusFailed`（失败）
ioType|AVIMMessageIOType 枚举|消息传输方向，有两种取值：<br/><br/>`AVIMMessageIOTypeIn`（发给当前用户）<br/>`AVIMMessageIOTypeOut`（由当前用户发出）

我们为每一种富媒体消息定义了一个消息类型，实时通信 SDK 自身使用的类型是负数（如下面列表所示），所有正数留给开发者自定义扩展类型使用，0 作为「没有类型」被保留起来。

消息 | 类型
--- | ---
文本消息|-1
图像消息|-2
音频消息|-3
视频消息|-4
位置消息|-5
文件消息|-6

<!-- >TODO: 举例说明如何使用这样的数字类型 -->


### 自定义消息

在某些场景下，开发者需要在发送消息时附带上自己业务逻辑需求的自定义属性，比如消息发送的设备名称，或是图像消息的拍摄地点、视频消息的来源等等，开发者可以通过 `AVIMTypedMessage.attributes` 实现这一需求。

【场景】发照片给朋友，告诉对方照片的拍摄地点：


```objc
- (void)tomSendLocalImageToJerry {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Jerry 的会话
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 创建了一个图像消息
            NSString *filePath = [self imagePath];
            NSDictionary *attributes = @{ @"location": @"拉萨布达拉宫" };
            AVIMImageMessage *message = [AVIMImageMessage messageWithText:@"这蓝天……我彻底是醉了" attachedFilePath:filePath attributes:attributes];

            // Tom 将图像消息发给 Jerry
            [conversation sendMessage:message callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"发送成功！");
                }
            }];
        }];
    }];
}
```


接收时可以读取这一属性：


```objc
- (void)jerryReceiveMessageFromTom {
    // Jerry 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"friend"];

    // 设置 client 的 delegate，并实现 delegate 方法
    self.client.delegate = self;

    // Jerry 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // ...
    }];
}

#pragma mark - AVIMClientDelegate

- (void)conversation:(AVIMConversation *)conversation didReceiveTypedMessage:(AVIMTypedMessage *)message {
    if ([message isKindOfClass:[AVIMImageMessage class]]) {
        AVIMImageMessage *imageMessage = (AVIMImageMessage *)message;

        // 拉萨布达拉宫
        NSString *location = imageMessage.attributes[@"location"];
    }
}
```


所有的 `AVIMTypedMessage` 消息都支持 `attributes` 这一属性。

#### 创建新的消息类型


继承于 `AVIMTypedMessage`，开发者也可以扩展自己的富媒体消息。其要求和步骤是：

* 实现 `AVIMTypedMessageSubclassing` 协议；
* 子类将自身类型进行注册，一般可在子类的 `+load` 方法或者 UIApplication 的 `-application:didFinishLaunchingWithOptions:` 方法里面调用 `[YourClass registerSubclass]`。


> **什么时候需要自己创建新的消息类型？**
>
>譬如有一条图像消息，除了文本之外，还需要附带地理位置信息，为此开发者需要创建一个新的消息类型吗？从上面的例子可以看出，其实完全没有必要。这种情况只要使用消息类中预留的 `AVIMTypedMessage.attributes` 属性就可以保存额外的地理位置信息了。
>
>只有在我们的消息类型完全无法满足需求的时候，才需要扩展自己的消息类型。譬如「今日头条」里面允许用户发送某条新闻给好友，在展示上需要新闻的标题、摘要、图片等信息（类似于微博中的 linkcard）的话，这时候就可以扩展一个新的 NewsMessage 类。

## 对话

以上章节基本演示了实时通信 SDK 的核心概念「对话」，即 `AVIMConversation`。我们将单聊和群聊（包括聊天室）的消息发送和接收都依托于 `AVIMConversation` 这个统一的概念进行操作，所以开发者需要强化理解的一个概念就是：
>SDK 层面不区分单聊和群聊。



对话的管理包括「成员管理」和「属性管理」两个方面。

在讲解下面的内容之前，我们先来创建一个多人对话。后面的举例都要基于这个对话，所以**这一步是必须的**。请将以下代码复制到 IDE 并且执行。


```objc
- (void)jerryCreateConversationWithFriends {
    // Jerry 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Jerry"];

    // Jerry 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Jerry 建立了与朋友们的会话
        NSArray *friends = @[@"Jerry", @"Bob", @"Harry", @"William"];
        [self.client createConversationWithName:@"Tom and friends" clientIds:friends callback:^(AVIMConversation *conversation, NSError *error) {
            if (!error) {
                NSLog(@"创建成功");
            }
        }];
    }];
}
```



### 创建对话

有两个方法可以创建对话：

```objc
/*!
 创建一个新的用户对话。
 在单聊的场合，传入对方一个 clientId 即可；群聊的时候，支持同时传入多个 clientId 列表
 @param name - 对话名称。
 @param clientIds - 聊天参与者（发起人除外）的 clientId 列表。
 @param callback － 对话建立之后的回调
 @return None.
 */
- (void)createConversationWithName:(NSString *)name
                         clientIds:(NSArray *)clientIds
                          callback:(AVIMConversationResultBlock)callback;

/*!
 创建一个新的用户对话。
 在单聊的场合，传入对方一个 clientId 即可；群聊的时候，支持同时传入多个 clientId 列表
 @param name - 对话名称。
 @param clientIds - 聊天参与者（发起人除外）的 clientId 列表。
 @param attributes - 对话的自定义属性。
 @param options － 可选参数，可以使用或 “|” 操作表示多个选项
 @param callback － 对话建立之后的回调
 @return None.
 */
- (void)createConversationWithName:(NSString *)name
                         clientIds:(NSArray *)clientIds
                        attributes:(NSDictionary *)attributes
                           options:(AVIMConversationOption)options
                          callback:(AVIMConversationResultBlock)callback;
```

各参数含义如下：

* **name** － 表示对话名字，可以指定任意有意义的名字，也可不填。
* **clientIds** － 表示对话初始成员，可不填。如果填写了初始成员，则 LeanCloud 云端会直接给这些成员发出邀请，省掉再专门发一次邀请请求。
* **attributes** － 表示额外属性，Dictionary，支持任意的 key/value，可不填。
* **options** － 对话选项：
    1. `AVIMConversationOptionTransient`：聊天室，具体可以参见[创建聊天室](#创建聊天室)；
    2. `AVIMConversationOptionNone`：普通对话；
    3. `AVIMConversationOptionUnique`：根据成员（clientIds）创建原子对话。如果没有这个选项，服务端会为相同的 clientIds 创建新的对话。clientIds 即 \_Conversation 表的 **m** 字段。
    
  其中，`AVIMConversationOptionNone` 和 `AVIMConversationOptionUnique` 可以使用 `|` 来组合使用，其他选项则不允许。
* **callback** － 结果回调，在操作结束之后调用，通知开发者成功与否。


### 对话的成员管理

成员管理，是在对话中对成员的一个实时生效的操作，一旦操作成功则不可逆。

#### 成员变更接口
成员变更操作接口简介如下表：

操作目的|接口名
----|---
自身主动加入 |  `AVIMConversation.joinWithCallback`
添加其他成员 |  `AVIMConversation.addMembersWithClientIds`
自身主动退出 |  `AVIMConversation.quitWithCallback`
剔除其他成员 |  `AVIMConversation.removeMembersWithClientIds`

成员变动之后，所有对话成员如果在线的话，都会得到相应的通知。


在 iOS 中，开发者需要实现 `AVIMClientDelegate` 代理，并且为 AVIMClient 指定该代理的一个实例。

`AVIMClientDelegate` 关于的成员变更通知的代理解释如下：

```
@protocol AVIMClientDelegate <NSObject>

/*!
 对话中有新成员加入的通知。
 @param conversation － 所属对话
 @param clientIds - 加入的新成员列表
 @param clientId - 邀请者的 id
 @return None.
 */
- (void)conversation:(AVIMConversation *)conversation membersAdded:(NSArray *)clientIds byClientId:(NSString *)clientId;
/*!
 对话中有成员离开的通知。
 @param conversation － 所属对话
 @param clientIds - 离开的成员列表
 @param clientId - 操作者的 id
 @return None.
 */
- (void)conversation:(AVIMConversation *)conversation membersRemoved:(NSArray *)clientIds byClientId:(NSString *)clientId;

/*!
 被邀请加入对话的通知。
 @param conversation － 所属对话
 @param clientId - 邀请者的 id
 @return None.
 */
- (void)conversation:(AVIMConversation *)conversation invitedByClientId:(NSString *)clientId;

/*!
 从对话中被移除的通知。
 @param conversation － 所属对话
 @param clientId - 操作者的 id
 @return None.
 */
- (void)conversation:(AVIMConversation *)conversation kickedByClientId:(NSString *)clientId;
```

接下来，我们将结合代码，针对各种成员变更的操作以及对应的事件回调进行详细讲解。


#### 自身主动加入

Tom 想主动加入 Jerry、Bob、Harry 和 William 的对话，以下代码将帮助他实现这个功能：


```objc
- (void)tomJoinConversation {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {
            [conversation joinWithCallback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"加入成功！");
                }
            }];
        }];
    }];
}
```



该群的其他成员（比如 Bob）如果在线的话，会收到该操作的事件回调：

```objc
- (void)bobNoticedTomDidJoin {
    // Bob 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Bob"];
    self.client.delegate = self;

    // Bob 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // ...
    }];
}

#pragma mark - AVIMClientDelegate

- (void)conversation:(AVIMConversation *)conversation membersAdded:(NSArray *)clientIds byClientId:(NSString *)clientId {
    NSLog(@"%@", [NSString stringWithFormat:@"%@ 加入到对话，操作者为：%@",[clientIds objectAtIndex:0],clientId]);
}

```

Tom 自身主动加入对话之后，相关方收到通知的时序是这样的：

No.|加入者|其他人
---|---|---
1|发出请求 join|  
2||收到 membersAdded 通知




#### 添加其他成员

Jerry 想再把 Mary 加入到对话中，需要如下代码帮助他实现这个功能：


```objc
- (void)jerryInviteMary {
    // Jerry 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Jerry"];

    // Jerry 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {
            // Jerry 邀请 Mary 到会话中
            [conversation addMembersWithClientIds:@[@"Mary"] callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"邀请成功！");
                }
            }];
        }];
    }];
}
```

如果 Mary 在线的话，就会收到 `invitedByClientId` 通知：

```
-(void)maryNoticedWhenJerryInviteMary{
    // Mary 创建一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Mary"];
    self.client.delegate = self;
    
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // 登录成功
    }];
}
#pragma mark - AVIMClientDelegate
// Mary 被邀请进入对话之后，会得到如下回调
-(void)conversation:(AVIMConversation *)conversation invitedByClientId:(NSString *)clientId{
    NSLog(@"%@", [NSString stringWithFormat:@"当前 ClientId(Mary) 被 %@ 邀请，加入了对话",clientId]);
}
```


该对话的其他成员（例如 Harry）也会受到该项操作的影响，收到事件被响应的通知，类似于第一小节 [自身主动加入](#自身主动加入) 中**Tom 加入对话之后，Bob 受到的影响。**


邀请成功以后，相关方收到通知的时序是这样的：

No.|邀请者|被邀请者|其他人
---|---|---|---
1|发出请求 addMembers| | 
2| |收到 invitedByClientId 通知| 
3|收到 membersAdded 通知|收到 membersAdded 通知 | 收到 membersAdded 通知


>注意：如果在进行邀请操作时，被邀请者不在线，那么通知消息并不会被离线缓存，所以等到 Ta 再次上线的时候将不会收到通知。

#### 自身退出对话
这里一定要区分**自身退出对话**的主动性，它与**自身被动被踢出**（下一小节）在逻辑上完全是不一样的。

Tom 主动从对话中退出，他需要如下代码实现需求：


```objc
- (void)tomQuitConversation {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {
            [conversation quitWithCallback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"退出成功！");
                }
            }];
        }];
    }];
}
```

如果 Harry 在线的话，他将收到 `membersRemoved` 通知：

```
-(void)harryNoticedWhenTomQuitConversation{
    // Harry 创建一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Harry"];
    self.client.delegate = self;
    
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // 登录成功
    }];
}

#pragma mark - AVIMClientDelegate
// Harry 登录之后，Tom 退出了对话，在 Harry 所在的客户端就会激发以下回调
-(void)conversation:(AVIMConversation *)conversation membersRemoved:(NSArray *)clientIds byClientId:(NSString *)clientId{
    NSLog(@"%@", [NSString stringWithFormat:@"%@ 离开了对话， 操作者为：%@",[clientIds objectAtIndex:0],clientId]);
}
```

Tom 自身主动退出对话之后，相关方收到通知的时序是这样的：

No.|退出者|其他人
---|---|---
1|发出请求 quit|  
2||收到 membersRemoved 通知



#### 剔除其他成员

Harry 被 William 从对话中删除。实现代码如下（关于 William 如何获得权限在后面的 [签名和安全](#签名和安全) 中会做详细阐述，此处不宜扩大话题范围。）：


```objc
- (void)williamKickHarry {
    // William 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"William"];

    // William 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {
            [conversation removeMembersWithClientIds:@[@"Harry"] callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"踢人成功！");
                }
            }];
        }];
    }];
}
```

如果 Harry 在线的话，会收到 `kickedByClientId` 通知：

```
-(void)harryNoticedWhenKickedByWilliam{
    // Harry 创建一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Harry"];
    self.client.delegate = self;
    
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // 登录成功
    }];
}
#pragma mark - AVIMClientDelegate
// Harry 登录之后，William 把 Harry 从对话中 剔除，在 Harry 所在的客户端就会触发以下回调
-(void)conversation:(AVIMConversation *)conversation kickedByClientId:(NSString *)clientId{
    NSLog(@"%@", [NSString stringWithFormat:@"当前 ClientId(Harry) 被提出对话， 操作者为：%@",clientId]);
}
```



踢人时，相关方收到通知的时序如下：

No.|踢人者|被踢者|其他人
---|---|---|---
1|发出请求 removeMembers| | 
2| |收到 kickedByClientId 通知| 
3|收到 membersRemoved 通知| | 收到 membersRemoved 通知


>注意：如果在进行踢人操作时，被踢者不在线，那么通知消息并不会被离线缓存，所以等到 Ta 再次上线的时候将不会收到通知。

#### 查询成员数量
`conversation:countMembersWithCallback:`这个方法返回的是实时数据：


```objc
- (void)tomCountConversationMembers {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 查看会话中成员的数量
            [conversation countMembersWithCallback:^(NSInteger number, NSError *error) {
            // 打印成员数量
            NSLog(@"%ld", number);
        }];
        }];
    }];
}
```


### 对话的属性管理

对话实例（AVIMConversation）与控制台中 `_Conversation` 表是一一对应的，默认提供的属性的对应关系如下：


AVIMConversation 属性名 | _Conversation 字段|含义
--- | ------------ | -------------
`conversationId`| `objectId` |全局唯一的 Id
`name` |  `name` |成员共享的统一的名字
`members`|`m` |成员列表
`creator` | `c` |对话创建者
`attributes`| `attr`|自定义属性
`transient`|`tr`|是否为聊天室（暂态对话）


#### 名称

这是一个全员共享的属性，它可以在创建时指定，也可以在日后的维护中被修改。

Tom 想建立一个名字叫「喵星人」 对话并且邀请了好友 Black 加入对话：


```objc
- (void)tomCreateNamedConversation {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 建立了与 Black 的会话，会话名称是 "喵星人"
        [self.client createConversationWithName:@"喵星人" clientIds:@[@"Black"] callback:^(AVIMConversation *conversation, NSError *error) {
            if (succeeded) {
                NSLog(@"创建成功！");
            }
        }];
    }];
}
```


Black 发现对话名字不够酷，他想修改成「聪明的喵星人」 ，他需要如下代码：


```objc
- (void)blackChangeConversationName {
    // Black 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Black"];

    // Black 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Black 查询 id 为 551260efe4b01608686c3e0f 的会话
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {
            // Black 修改 conversation 的名称
            AVIMConversationUpdateBuilder *updateBuilder = [conversation newUpdateBuilder];
            updateBuilder.name = @"聪明的喵星人";
            [conversation update:[updateBuilder dictionary] callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"修改成功！");
                }
            }];
        }];
    }];
}
```


####  成员

是当前对话中所有成员的 `clientId`。默认情况下，创建者是在包含在成员列表中的，直到 TA 退出对话。

>**强烈建议开发者切勿在控制台中对其进行修改**。所有关于成员的操作请参照上一章节中的 [对话的成员管理](#对话的成员管理) 来进行。

#### 静音
假如某一用户不想再收到某对话的消息提醒，但又不想直接退出对话，可以使用静音操作，即开启「免打扰模式」。

比如 Tom 工作繁忙，对某个对话设置了静音：


```objc
- (void)tomMuteConversation {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 查询 id 为 551260efe4b01608686c3e0f 的会话
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {
            // Tom 将会话设置为静音
            [conversation muteWithCallback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"修改成功！");
                }
            }];
        }];
    }];
}
```


>设置静音之后，iOS 和 Windows Phone 的用户就不会收到推送消息了。

与之对应的就是取消静音的操作，即取消免打扰模式。此操作会修改云端 `_Conversation` 里面的 `mu` 属性。**强烈建议开发者切勿在控制台中对 `mu` 随意进行修改**。

#### 创建者

即对话的创建者，它的值是对话创建者的 `clientId`。

它等价于 QQ 群中的「群创建者」，但区别于「群管理员」。比如 QQ 群的「创建者」是固定不变的，它的图标颜色与「管理员」的图标颜色都不一样。所以根据对话中成员的 `clientId` 是否与 `AVIMConversation.creator` 一致就可以判断出他是不是群的创建者。

#### 自定义属性

通过该属性，开发者可以随意存储自己的键值对，为对话添加自定义属性，来满足业务逻辑需求。

给某个对话加上两个自定义的属性：type = "private"（类型为私有）、isSticky = true（置顶显示）：


```objc
- (void)tomCreateConversationWithAttributes {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建名称为「猫和老鼠」的会话，并附加会话属性
        NSDictionary *attributes = @{ 
            @"type": @"private",
            @"isSticky": @(YES) 
        };
        [self.client createConversationWithName:@"猫和老鼠" clientIds:@[@"Jerry"] attributes:attributes options:AVIMConversationOptionNone callback:^(AVIMConversation *conversation, NSError *error) {
            if (succeeded) {
                NSLog(@"创建成功！");
            }
        }];
    }];
}
```



接下来，Tom 将 type 修改为 public：

```objc
-(void)tomUpdateConversationAttributes {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 查询 id 为 551260efe4b01608686c3e0f 的对话
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {

            AVIMConversationUpdateBuilder *updateBuilder = [conversation newUpdateBuilder];
            
            // ---------  非常重要！！！--------------
            // 将所有属性转交给 updateBuilder 统一处理。
            // 如果缺失这一步，下面没有改动过的属性，如上例中的 isSticky，
            // 在保存后会被删除。
            // -------------------------------------
            updateBuilder.attributes = conversation.attributes;
            
            // 将 type 值改为 public
            [updateBuilder setObject:@"public" forKey:@"type"];

            // 其他操作方法：删除 type 
            // [updateBuilder removeObjectForKey:@"type"];

            // 将更新后的全部属性写回对话
            [conversation update:[updateBuilder dictionary] callback:^(BOOL succeeded, NSError *error) {
                if (succeeded) {
                    NSLog(@"更新 attr 成功");
                }
            }];
        }
    }];
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


```objc
- (void)tomQueryConversation {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 查询 id 为 551260efe4b01608686c3e0f 的会话
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query getConversationById:@"551260efe4b01608686c3e0f" callback:^(AVIMConversation *conversation, NSError *error) {
            if (succeeded) {
                NSLog(@"查询成功！");
            }
        }];
    }];
}
```


#### 对话列表

用户登录进应用后，获取最近的 10 个对话（包含暂态对话，如聊天室）：


```objc
- (void)tomQueryConversationList {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 构建一个查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        // 执行查询
        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"找到 %ld 个对话！", [objects count]);
        }];
    }];
}
```


对话的查询默认返回 10 个结果，若要更改返回结果数量，请设置 `limit` 值。


```objc
- (void)tomQueryConversationWithLimit {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 构建一个查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        // Tom 设置查询最近 20 个活跃对话
        query.limit = 20;

        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"查询成功！");
        }];
    }];
}
```


#### 条件查询

##### 构建查询

对话的条件查询需要注意的对话属性的存储结构，在对话的属性一章节我们介绍的对话的几个基本属性，这些属性都是 SDK 提供的默认属性，根据默认属性查询的构建如下：


```
// 查询对话名称为「LeanCloud 粉丝群」的对话
[query whereKey:@"name" equalTo:@"LeanCloud 粉丝群"];

// 查询对话名称包含 「LeanCloud」 的对话
[query whereKey:@"name" containsString:@"LeanCloud"];

// 查询过去24小时活跃的对话
NSDate *today = [NSDate date];
NSDate *yesterday = [today dateByAddingTimeInterval: -86400.0];
[query whereKey:@"lm" greaterThan:yesterday];
```


相对于默认属性的查询，开发者自定义属性的查询需要在构建查询的时在关键字（key）前加上一个特殊的前缀：`attr`，不过每个 SDK 都提供相关的快捷方式帮助开发者方便的构建查询：


```
// 查询话题为 DOTA2 对话
[query whereKey:@"attr.topic" equalTo:@"DOTA2"];
// 查询等级大于 5 的对话
[query whereKey:@"attr.level" greaterThan:@(5)];
```

在 iOS SDK 中，针对自定义属性的查询，可以使用预定义的宏 `AVIMAttr` 为自定义属性查询添加 `attr` 前缀：

```
// 查询话题为 DOTA2 对话
[query whereKey:AVIMAttr(@"topic") equalTo:@"DOTA2"];
// 它与下面这行代码是一样的
[query whereKey:@"attr.topic" equalTo:@"DOTA2"];
```


默认属性以及自定义属性的区分便于 SDK 后续的内建属性拓展和维护，自定义属性的开放有利于开发者在可控的范围内进行查询的构建。


条件查询又分为：比较查询、正则匹配查询、包含查询，以下会做分类演示。


#### 比较查询

比较查询在一般的理解上都包含以下几种：


逻辑操作 | AVIMConversationQuery 方法|
---|---
等于 | `equalTo`
不等于 |  `notEqualTo` 
大于 | `greaterThan`
大于等于 | `greaterThanOrEqualTo`
小于 | `lessThanOrEqualTo`
小于等于 | `lessThanOrEqualTo`


比较查询最常用的是等于查询：


```objc
- (void)tomQueryConversationByEqualTo {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建属性中 topic 是 movie 的查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query whereKey:@"attr.topic" equalTo:@"movie"];
        // 执行查询
        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"查询成功！");
        }];
    }];
}
```


目前条件查询只针对 `AVIMConversation` 对象的自定义属性进行操作，也就是针对 `_Conversation` 表中的 `attr` 字段进行 `AVQuery` 查询。



下面检索一下类型不是私有的对话：


```objc
- (void)tomQueryConversationByNotEqualTo {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建 type 不等于 private 的查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query whereKey:AVIMAttr(@"type") notEqualTo:@"private"];
        // 执行查询
        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"找到 %ld 个对话！", [objects count]);
        }];
    }];
}
```


对于可以比较大小的整型、浮点等常用类型，可以参照以下示例代码进行扩展：


```objc
- (void)tomQueryConversationByGreaterThan {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建 attr.age 大于 18 的查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query whereKey:AVIMAttr(@"age") greaterThan:@(18)];
        // 执行查询
        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"找到 %ld 个对话！", [objects count]);
        }];
    }];
}
```


#### 正则匹配查询


匹配查询是指在 `AVIMConversationQuery` 的查询条件中使用正则表达式来匹配数据。


比如要查询所有 language 是中文的对话：


```objc
- (void)tomQueryConversationByRegExp {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建 attr.language 为中文字符的查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query whereKey:AVIMAttr(@"language") matchesRegex:@"[\u4e00-\u9fa5]"];
        // 执行查询
        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"找到 %ld 个对话！", [objects count]);
        }];
    }];
}
```


#### 包含查询

包含查询是指方法名字包含 `Contains` 单词的方法，例如查询关键字包含「教育」的对话：


```objc
- (void)tomQueryConversationByContains {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建 attr.keywords 包含 「教育」的查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query whereKey:AVIMAttr(@"keywords") containsString:@"教育"];
        // 执行查询
        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"找到 %ld 个对话！", [objects count]);
        }];
    }];
}
```


另外，包含查询还能检索与成员相关的对话数据。以下代码将帮助 Tom 查找出 Jerry 以及 Bob 都加入的对话：


```objc
- (void)tomQueryConversationByMembers {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建会话中有 Bob 和 Jerry 的查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query whereKey:@"m" containAllObjectsInArray:@[@"Bob", @"Jerry"]];
        // 执行查询
        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"找到 %ld 个对话！", [objects count]);
        }];
    }];
}
```




#### 组合查询

组合查询的概念就是把诸多查询条件合并成一个查询，再交给 SDK 去云端进行查询。

例如，要查询年龄小于 18 岁，并且关键字包含「教育」的对话：


```objc
- (void)tomQueryConversationByCombination {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建 attr.keywords 包含「教育」、attr.age < 18 的查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query whereKey:AVIMAttr(@"keywords") containsString:@"教育"];
        [query whereKey:AVIMAttr(@"age") greaterThan:@(18)];
        // 执行查询
        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"找到 %ld 个对话！", [objects count]);
        }];
    }];
}
```


只要查询构建得合理，开发者完全不需要担心组合查询的性能。


#### 缓存查询

通常，将查询结果缓存到磁盘上是一种行之有效的方法，这样就算设备离线，应用刚刚打开，网络请求尚未完成时，数据也能显示出来。或者为了节省用户流量，在应用打开的第一次查询走网络，之后的查询可优先走本地缓存。

值得注意的是，默认的策略是先走本地缓存的再走网络的，缓存时间是一小时。AVIMConversationQuery 中有如下方法：



```objc
// 设置缓存策略，默认是 kAVCachePolicyCacheElseNetwork
@property (nonatomic) AVCachePolicy cachePolicy;

// 设置缓存的过期时间，默认是 1 小时（1 * 60 * 60）
@property (nonatomic) NSTimeInterval cacheMaxAge;
```



有时你希望先走网络查询，发生网络错误的时候，再从本地查询，可以这样：

```objc
    AVIMConversationQuery *query = [[AVIMClient defaultClient] conversationQuery];
    query.cachePolicy = kAVCachePolicyNetworkElseCache;
    [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
        
    }];
```


各种查询缓存策略的行为可以参考  [存储指南 &middot; AVQuery 缓存查询](leanstorage_guide-ios.html#缓存查询) 一节。


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


创建一个聊天室跟创建一个普通对话差不多，只是在 `[imClient createConversationWithName:clientIds:attributes:options:callback:]` 中给 `options:` 传入特定的选项值 `AVIMConversationOptionTransient`。


比如喵星球正在直播选美比赛，主持人 Tom 创建了一个临时对话，与喵粉们进行互动：


```objc
- (void)tomCreateTransientConversation {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建名称为 「HelloKitty PK 加菲猫」的会话
        [self.client createConversationWithName:@"HelloKitty PK 加菲猫" clientIds:@[] attributes:nil options:AVIMConversationOptionTransient callback:^(AVIMConversation *conversation, NSError *error) {
            if (!error) {
                NSLog(@"创建成功！");
            }
        }];
    }];
}
```




### 查询在线人数

 `[AVIMConversation countMembersWithCallback:]`  可以用来查询普通对话的成员总数，在聊天室中，它返回的就是实时在线的人数：


```objc
- (void)tomCountsChatroomMembers{
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];
    NSString *conversationId=@"55dd9d7200b0c86eb4fdcbaa";
    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建一个对话的查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        // 根据已知 Id 获取对话实例，当前实例为聊天室。
        [query getConversationById:conversationId callback:^(AVIMConversation *conversation, NSError *error) {
            // 查询在线人数
            [conversation countMembersWithCallback:^(NSInteger number, NSError *error) {
                NSLog(@"%ld",number);
            }];
        }];
    }];
}
```


### 查找聊天室

开发者需要注意的是，通过 `[AVIMClient conversationQuery]` 这样得到的 `AVIMConversationQuery` 实例默认是查询全部对话的，也就是说，如果想查询指定的聊天室，需要额外再调用  `whereKey:`  方法来限定更多的查询条件：

比如查询主题包含「奔跑吧，兄弟」的聊天室：


```objc
- (void)tomQueryConversationByConditions {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建属性中 topic 是 movie 的查询
        AVIMConversationQuery *query = [self.client conversationQuery];
        [query whereKey:AVIMAttr(@"topic") equalTo:@"movie"];
        // 额外调用一次确保查询的是聊天室而不是普通对话
        [query whereKey:@"tr" equalTo:@(YES)];
        [query findConversationsWithCallback:^(NSArray *objects, NSError *error) {
            NSLog(@"查询成功！");
        }];
    }];
}
```




## 聊天记录

聊天记录一直是客户端开发的一个重点，QQ 和 微信的解决方案都是依托客户端做缓存，当收到一条消息时就按照自己的业务逻辑存储在客户端的文件或者是各种客户端数据库中。

我们的 SDK 会将普通的对话消息自动保存在云端，开发者可以通过 AVIMConversation 来获取该对话的所有历史消息。

获取该对话中最近的 N 条（默认 20，最大值 1000）历史消息，通常在第一次进入对话时使用：


```objc
- (void)tomQueryMessagesWithLimit {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建查询会话的 query
        AVIMConversationQuery *query = [self.client conversationQuery];
        // Tom 获取 id 为 2f08e882f2a11ef07902eeb510d4223b 的会话
        [query getConversationById:@"2f08e882f2a11ef07902eeb510d4223b" callback:^(AVIMConversation *conversation, NSError *error) {
            // 查询对话中最后 10 条消息
            [conversation queryMessagesWithLimit:10 callback:^(NSArray *objects, NSError *error) {
                NSLog(@"查询成功！");
            }];
        }];
    }];
}
```


获取某条消息之前的历史消息，通常用在翻页加载更多历史消息的场景中。

```objc
- (void)tomQueryMessagesBeforeMessage {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建查询会话的 query
        AVIMConversationQuery *query = [self.client conversationQuery];
        // Tom 获取 id 为 2f08e882f2a11ef07902eeb510d4223b 的会话
        [query getConversationById:@"2f08e882f2a11ef07902eeb510d4223b" callback:^(AVIMConversation *conversation, NSError *error) {
            // 从指定的某条消息（id 为 grqEG2OqSL+i8FSX9j3l2g，时间戳为 1436137606358）开始查询
            [conversation queryMessagesBeforeId:@"grqEG2OqSL+i8FSX9j3l2g" timestamp:1436137606358 limit:10 callback:^(NSArray *objects, NSError *error) {
                NSLog(@"查询成功！");
            }];
        }];
    }];
}
```


翻页获取历史消息的时候，LeanCloud 云端是从某条消息开始，往前查找所指定的 N 条消息来返回给客户端。为此，获取历史消息需要传入三个参数：

* 起始消息的 messageId
* 起始消息的发送时间戳
* 需要获取的消息条数

假如每一页为 10 条信息，下面的代码将演示如何翻页：


```objc
- (void)tomQueryMessagesWithLimit {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建查询会话的 query
        AVIMConversationQuery *query = [self.client conversationQuery];
        // Tom 获取 id 为 2f08e882f2a11ef07902eeb510d4223b 的会话
        [query getConversationById:@"2f08e882f2a11ef07902eeb510d4223b" callback:^(AVIMConversation *conversation, NSError *error) {
            // 查询对话中最后 10 条消息
            [conversation queryMessagesWithLimit:10 callback:^(NSArray *objects, NSError *error) {
                [self TomLoadMoreMessage:objects forConversation:conversation];
            }];
        }];
    }];
}

- (void)tomLoadMoreMessage:(NSArray *)messages forConversation:(AVIMConversation *)conversation {
    AVIMMessage *oldestMessage = [messages firstObject];
    [conversation queryMessagesBeforeId:oldestMessage.messageId timestamp:oldestMessage.sendTimestamp limit:10 callback:^(NSArray *objects, NSError *error) {
        NSLog(@"查询成功！");
    }];
}
```



### 客户端聊天记录缓存

为了减少客户端的请求数量，以及减少用户的流量，SDK 实现了一套缓存同步策略。用户在调用获取聊天记录的接口时优先从缓存中获取，SDK 是有算法保证本地与云端聊天记录是同步的。

聊天记录的缓存功能默认为**开启**，但如果开发者出于自身业务逻辑需求，不想在客户端使用缓存功能，可以使用如下接口将其关闭：


```objc
- (void)tomQueryMessagesWithLimitAndIgnoreCache {
    // Tom 创建了一个 client，用自己的名字作为 clientId
    self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];

    // Tom 关闭了 SDK 内建的消息缓存功能，忽略本地缓存。
    self.client.messageQueryCacheEnabled = NO;

    // Tom 打开 client
    [self.client openWithCallback:^(BOOL succeeded, NSError *error) {
        // Tom 创建查询会话的 query
        AVIMConversationQuery *query = [self.client conversationQuery];
        // Tom 获取 id 为 2f08e882f2a11ef07902eeb510d4223b 的会话
        [query getConversationById:@"2f08e882f2a11ef07902eeb510d4223b" callback:^(AVIMConversation *conversation, NSError *error) {
            // 查询对话中最后 10 条消息，由于之前关闭了消息缓存功能，查询会走网络请求。
            [conversation queryMessagesWithLimit:10 callback:^(NSArray *objects, NSError *error) {
                NSLog(@"查询成功！");
            }];
        }];
    }];
}
```




## 客户端事件

### 网络状态响应

当网络连接出现中断、恢复等状态变化时，可以通过以下接口来处理响应：


与网络相关的通知（网络断开、恢复等）要采用 `AVIMClientDelegate` 代理方式来实现，主要接口如下：

* `imClientPaused:(AVIMClient *)imClient` 指网络连接断开事件发生，此时聊天服务不可用。
* `imClientResuming:(AVIMClient *)imClient` 指网络断开后开始重连，此时聊天服务依然不可用。
* `imClientResumed:(AVIMClient *)imClient` 指网络连接恢复正常，此时聊天服务变得可用。

在网络中断的情况下，所有的消息收发和对话操作都会出现问题。


>注意：网络状态在短时间内很可能会发生频繁变化，但这并不代表对话的接收与发送一定会受到影响，因此开发者在处理此类事件响应时，比如更新 UI，要适应加入更多的逻辑判断，以免影响用户的使用体验。


### 断线重连
目前 iOS SDK 默认内置了断线重连的功能，从客户端与云端建立连接成功开始，只要没有调用退出登录的接口，SDK 会一直尝试和云端保持长连接，此时 AVIMClient 的状态可以通过 [网络状态响应](#网络状态响应)接口得到。

**注意：用户如果自行实现了重连逻辑可能会报出 1001 错误**。


### 退出登录

要退出当前的登录状态或要切换账户，方法如下：


在 app 退出的时候，或者切换用户的时候，我们需要断开与 LeanCloud 实时通信服务的长连接，这时候需要调用 `[AVIMClient closeWithCallback:]` 函数。一般情况下，这个函数都会关闭连接并立刻返回，这时实时通信服务端就会认为当前用户已经下线。




## 安全与签名

在继续阅读下文之前，请确保你已经对 [实时通信服务开发指南 &middot; 权限和认证](realtime_v2.html#权限和认证) 有了充分的了解。

### 实现签名工厂

为了满足开发者对权限和认证的要求，我们设计了操作签名的机制。签名启用后，所有的用户登录、对话创建/加入、邀请成员、踢出成员等登录都需要验证签名，这样开发者就对消息具有了完全的掌控。


我们强烈推荐启用签名，具体步骤是 [控制台 > 设置 > 应用选项](/app.html?appid={{appid}}#/permission)，勾选 **聊天、推送** 下的 **聊天服务，启用签名认证**。



客户端这边究竟该如何使用呢？我们只需要实现 AVIMSignatureDataSource 协议接口，然后在用户登录之前，把这个接口赋值给 `AVIMClient.signatureDataSource` 即可。示例代码如下：

```objc
// Tom 创建了一个 client，用自己的名字作为 clientId
AVIMClient *imClient = [[AVIMClient alloc] initWithClientId:@"Tom"];
imClient.delegate = self;
imClient.signatureDataSource = signatureDelegate;

// Tom 打开 client
[imClient openWithCallback:^(BOOL succeeded, NSError *error){
    if (error) {
        // 出错了，可能是网络问题无法连接 LeanCloud 云端，请检查网络之后重试。
        // 此时聊天服务不可用。
        UIAlertView *view = [[UIAlertView alloc] initWithTitle:@"聊天不可用！" message:[error description] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [view show];
    } else {
        // 成功登录，可以开始进行聊天了。
    }
}];
```

设定了 signatureDataSource 之后，对于需要鉴权的操作，实时通信 SDK 与服务器端通讯的时候都会带上应用自己生成的 Signature 信息，LeanCloud 云端会使用 app 的 masterKey 来验证信息的有效性，保证聊天渠道的安全。

对于 AVIMSignatureDataSource 接口，我们只需要实现这一个函数即可：

```objc
/*!
 对一个操作进行签名. 注意:本调用会在后台线程被执行
 @param clientId - 操作发起人的 id
 @param conversationId － 操作所属对话的 id
 @param action － 操作的种类，主要有：
                "join": 表示操作发起人要加入对话
                "invite": 表示邀请其他人加入对话
                "kick": 表示从对话中踢出部分人
 @param clientIds － 操作目标的 id 列表
 @return 一个 AVIMSignature 签名对象.
 */
- (AVIMSignature *)signatureWithClientId:(NSString *)clientId
                          conversationId:(NSString *)conversationId
                                  action:(NSString *)action
                       actionOnClientIds:(NSArray *)clientIds;
```

你需要做的就是按照前文所述的签名算法实现签名，其中 `AVIMSignature` 声明如下：

```objc
@interface AVIMSignature : NSObject

@property (nonatomic, strong) NSString *signature;
@property (nonatomic) int64_t timestamp;
@property (nonatomic, strong) NSString *nonce;
@property (nonatomic, strong) NSError *error;

@end
```

其中四个属性分别是：

* signature：签名
* timestamp：时间戳，单位秒
* nonce：随机字符串 nonce
* error：签名错误信息

在启用签名功能的情况下，实时通信 SDK 在进行一些重要操作前，都会首先请求 `AVIMSignatureDataSource` 接口，获取签名信息 `AVIMSignature`，然后把操作信息和第三方签名一起发给 LeanCloud 云端，由云端根据签名的结果来对操作进行处理。 

用户登录是通过调用 `AVIMClient` 对象中以「open」开头的方法来实现的，以下是其中一个方法：

```objc
// 开启某个账户的聊天
- (void)openWithCallback:(AVIMBooleanResultBlock)callback;
```

各参数含义如下：

* clientId：操作发起人的 id，以后使用该账户的所有聊天行为，都由此人发起。
* callback：聊天开启之后的回调，在操作结束之后调用，通知开发者成功与否

我们现在来实际看一下这个过程如何实现。假定聊天发起方名叫 Tom，为直观起见，我们使用用户名来作为 `clientId` 登录聊天系统（LeanCloud 云端只要求 `clientId` 在应用内唯一即可，具体用什么数据由应用层决定）。示例代码如下：

```objc
// Tom 创建了一个 client，用自己的名字作为 clientId
self.client = [[AVIMClient alloc] initWithClientId:@"Tom"];
self.client.delegate = self;

// Tom 打开 client
[self.client openWithCallback:^(BOOL succeeded, NSError *error){
    if (error) {
        // 出错了，可能是网络问题无法连接 LeanCloud 云端，请检查网络之后重试。
        // 此时聊天服务不可用。
        UIAlertView *view = [[UIAlertView alloc] initWithTitle:@"聊天不可用！" message:[error description] delegate:nil cancelButtonTitle:@"OK" otherButtonTitles:nil];
        [view show];
    } else {
        // 成功登录，可以进入聊天主界面了。
        MainViewController *mainView = [[MainViewController alloc] init];
        [self.navigationController pushViewController:mainView animated:YES];
    }
}];
```


> 需要强调的是：开发者切勿在客户端直接使用 MasterKey 进行签名操作，因为 MaterKey 一旦泄露，会造成应用的数据处于高危状态，后果不容小视。因此，强烈建议开发者将签名的具体代码托管在安全性高稳定性好的云端服务器上（例如 LeanCloud 云引擎）。

为了帮助开发者理解云端签名的算法，我们开源了一个用 Node.js + 云引擎实现签名的云端，供开发者学习和使用：[LeanCloud 实时通信云引擎签名 Demo](https://github.com/leancloud/realtime-messaging-signature-cloudcode)。


### 单点登录

一款聊天应用，随着不断的发展，会衍生出多个平台的不同客户端。以 QQ 为例，目前它所提供的客户端如下：

- PC：Windows PC、Mac OS、Linux（已停止更新）
- 移动：Windows Phone、iOS、Android
- [Web QQ](http://noreferer.net/?url=http://w.qq.com)

经过测试，我们发现 QQ 存在以下几种行为：

1. 同一个 QQ 账号不可以同时在 2 个 PC 端登录（例如，在 Mac OS 上登录已经在另外一台 Windows PC 上登录的 QQ，该 QQ 号在 Windows PC 上会被强行下线）。
2. 同一个 QQ 账号不可以同时在 2 个移动端上登录。
3. Web QQ 也不能与 PC 端同时登录
4. 同一个 QQ 只能同时在 1 个移动版本和 1 PC 版本（或者 Web 版本）上登录，并实现一些 PC 与移动端互动的功能，例如互传文件。

通过规律不难发现，QQ 按照自己的需求实现了「单点登录」的功能：同一个平台上只允许一个 QQ 登录一台设备。

下面我们来详细说明：如何使用我们的 SDK 去实现单点登录。

#### 设置登录标记 Tag

假设开发者想实现 QQ 这样的功能，那么需要在登录到云端的时候，也就是打开与云端长连接的时候，标记一下这个链接是从什么类型的客户端登录到云端的：


```objc
AVIMClient *currentClient = [[AVIMClient alloc] initWithClientId:@"Tom" tag:@"Mobile"];
[currentClient openWithCallback:^(BOOL succeeded, NSError *error) {
    if (succeeded) {
        // 与云端建立连接成功
    }
}];
```


上述代码可以理解为 LeanCloud 版 QQ 的登录，而另一个带有同样 Tag 的客户端打开连接，则较早前登录系统的客户端会被强制下线。

#### 处理登录冲突

我们可以看到上述代码中，登录的 Tag 是 `Mobile`。当存在与其相同的 Tag 登录的客户端，较早前登录的设备会被云端强行下线，而且他会收到被云端下线的通知：


```objc
-(void)client:(AVIMClient *)client didOfflineWithError:(NSError *)error{
    if ([error code]  == 4111) {
        //适当的弹出友好提示，告知当前用户的 Client Id 在其他设备上登陆了
    }
};
```

为了更灵活地控制登录过程，我们在登录接口上增加了一个选项，以下是方法签名：

```objc
- (void)openWithOption:(AVIMClientOpenOption *)option callback:(AVIMBooleanResultBlock)callback;
```

登录选项由 `AVIMClientOpenOption` 对象表示，其中的每一个属性表示具体的选项，目前支持以下选项：

```objc
@interface AVIMClientOpenOption : NSObject

@property (nonatomic, assign) BOOL force;

@end
```

`force` 选项设置登录动作的强制性。自然地，登录动作也区分成两种不同的类型，即强制登录和非强制登录。

* 强制登录表示这个动作是强制的，不管当前设备有没有被其他设备踢下线过，都强制性地登录。
* 非强制登录表示这个动作是非强制的，如果当前设备曾被其他设备踢下线过，登录会返回错误。

将 `force` 设置为 `YES` 表示强制登录；设置为 `NO` 表示非强制登录。例如，如果希望实现强制登录，代码可以写成：

```objc
 self.client = [[AVIMClient alloc] initWithClientId:@"Tom" tag:@"Mobile"];

AVIMClientOpenOption *option = [[AVIMClientOpenOption alloc] init];
option.force = YES;

[self.client openWithCallback:^(BOOL succeeded, NSError *error) {
    // Your code
}];
```

如果 `option` 设置为 nil，或者使用 `-[AVIMClient openWithCallback:]` 方法进行登录，默认的登录类型为非强制登录。



如上述代码中，被动下线的时候，云端会告知原因，因此客户端在做展现的时候也可以做出类似于 QQ 一样友好的通知。





## 实时通信云引擎 Hook
一些应用因其特殊的业务逻辑需要在消息发送时或者消息接收时插入一定的逻辑，因此我们也提供了[实时通信云引擎 Hook](realtime_v2.html#云引擎_Hook)。

## 实时通信 REST API
有些应用需要在用户登录之前就提前创建一些对话或者是针对对话进行操作，因此可以通过[实时通信 REST API](realtime_rest_api.html)来实现。

## 常见问题

**我只想实现两个用户的私聊，是不是每次都得重复创建对话？**

不需要重复创建。我们推荐的方式是开发者可以用**自定义属性**来实现对私聊和群聊的标识，并且在进行私聊之前，需要查询当前两个参与对话的 ClientId 是否之前已经存在一个私聊的对话了。另外，SDK 已经提供了创建唯一对话的接口，请查看 [创建对话](#创建对话)。


**某个成员退出对话之后，再加入，在他离开的这段期间内的产生的聊天记录，他还能获取么？**

可以。目前聊天记录从属关系是属于对话的，也就是说，只要对话 Id 不变，不论人员如何变动，只要这个对话产生的聊天记录，当前成员都可以获取。

**我自己没有云端，如何实现签名的功能？**

LeanCloud 云引擎提供了托管 Python 和 Node.js 运行的方式，开发者可以所以用这两种语言按照签名的算法实现签名，完全可以支持开发者的自定义权限控制。

**客户端连接被关闭**

导致这一情况的原因很多，请参考 [云端错误码说明](realtime_v2.html#云端错误码说明)。


<a id="duplicate_message_notification" name="duplicate_message_notification"></a>**为何离线消息重复推送了两次？**

大部分原因是这种情况造成的：成员 A 和成员 B 同在一个对话中。A 调用了 `openWithCallback` 登录实时通信，在没有调用 `closeWithCallback` 退出登录的情况下，B 使用同一个设备也调用了 `openWithCallback` 登录了实时通信。此时应用退出到后台，其他同在这个对话中的成员向这个对话发送了消息，服务器会给不在线的 A 和 B 发送消息推送，这个设备就会收到两条消息推送。解决方案是确保 B 登录时 A 已经调用 `closeWithCallback` 成功地退出了登录。

