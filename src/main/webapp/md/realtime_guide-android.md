



# Android 实时通信开发指南

## 简介

实时通信服务可以让你一行后端代码都不用写，就能做出一个功能完备的实时聊天应用，或是一个实时对战类的游戏。所有聊天记录都保存在云端，离线消息会通过消息推送来及时送达，推送消息文本可以灵活进行定制。

>在继续阅读本文档之前，请先阅读[《实时通信开发指南》](./realtime_v2.html)，了解一下实时通信的基本概念和模型。



### 文档贡献
我们欢迎和鼓励大家对本文档的不足提出修改建议。请访问我们的 [Github 文档仓库](https://github.com/leancloud/docs) 来提交 Pull Request。

## Demo
相比阅读文档，如果你更喜欢从代码入手了解功能的具体实现，可以下载 Demo 来研究：


* [LeanMessage](https://github.com/leancloud/LeanMessage-Demo)（推荐）
* [LeanChat](https://github.com/leancloud/leanchat-android)


我们把所有 Demo 项目放在了 [LeanCloud Demos 资源库](https://github.com/leancloud/leancloud-demos) 中，方便大家浏览和参考。

## 安装和初始化

请参考详细的 [Android SDK 安装指南](sdk_setup-android.html)。




## 单聊

我们先从最简单的环节入手。此场景类似于微信的私聊、微博的私信和 QQ 单聊。我们创建了一个统一的概念来描述聊天的各种场景：`AVIMConversation`（对话），在[《实时通信开发指南》](./realtime_v2.html) 里也有相关的详细介绍。

### 发送消息

![Tom and Jerry](images/tom-and-jerry-avatar.png)

Tom 想给 Jerry 发一条消息，实现代码如下：


<div class="callout callout-info">注意：**启用实时通信一定要正确配置** `AndroidManifest.xml`，请仔细阅读 [Android SDK 初始化配置](sdk_setup-android.html#初始化)。</div>

```
  public void sendMessageToJerryFromTom() {
    // Tom 用自己的名字作为clientId，获取AVIMClient对象实例
    AVIMClient tom = AVIMClient.getInstance("Tom");
    // 与服务器连接
    tom.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 创建与Jerry之间的对话
          client.createConversation(Arrays.asList("Jerry"), "Tom & Jerry", null,
              new AVIMConversationCreatedCallback() {

                @Override
                public void done(AVIMConversation conversation, AVIMException e) {
                  if (e == null) {
                    AVIMTextMessage msg = new AVIMTextMessage();
                    msg.setText("耗子，起床！");
                    // 发送消息
                    conversation.sendMessage(msg, new AVIMConversationCallback() {

                      @Override
                      public void done(AVIMException e) {
                        if (e == null) {
                          Log.d("Tom & Jerry", "发送成功！");
                        }
                      }
                    });
                  }
                }
              });
        }
      }
    });
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



```
public class MyApplication extends Application{
 public static class CustomMessageHandler extends AVIMMessageHandler{
   //接收到消息后的处理逻辑 
   @Override
   public void onMessage(AVIMMessage message,AVIMConversation conversation,AVIMClient client){
     if(message instanceof AVIMTextMessage){
       Log.d("Tom & Jerry",((AVIMTextMessage)message).getText());
     }
   }
   
   public void onMessageReceipt(AVIMMessage message,AVIMConversation conversation,AVIMClient client){
   
   }
 }	
 public void onCreate(){
   ...
   AVOSCloud.initialize(this,"{{appid}}","{{appkey}}");   
   //注册默认的消息处理逻辑
   AVIMMessageManager.registerDefaultMessageHandler(new CustomMessageHandler());
   ...
 }
...
public void jerryReceiveMsgFromTom(){
  //Jerry登录
  AVIMClient jerry = AVIMClient.getInstance("Jerry");
  jerry.open(new AVIMClientCallback(){
  
    @Override
    public void done(AVIMClient client,AVIMException e){
    	if(e==null){
    	 ...//登录成功后的逻辑
    	}
    }
  });
}
}
```



#### MessageHandler 的处理逻辑

在 Android SDK 中接收消息的 AVIMMessageHandler 在 AVIMMessageManager 中进行注册时有两个不同的方法： `registerDefaultMessageHandler` 和 `registerMessageHandler`。

当客户端收到一条消息的时候，会优先根据消息类型通知当前所有注册的对应类型的普通的 `messageHandler`,如果发现当前没有任何注册的普通的 `messageHandler`，才会去通知 `defaultMessageHandler`。

在 `AVIMMessageManager` 中多次注册 `defaultMessageHandler` ，只有最后一次调用的才是有效的；而通过 `registerMessageHandler` 注册的 `AVIMMessageHandler`，则是可以同存的。

通过在 UI 组件（比如 Activity）的 `onResume` 方法中间去调用 `registerMessageHandler`,而在 `onPaused` 方法中间调用 `unregisterMessageHandler` 的组合，让对应的 `messageHandler` 处理当前页面的处理逻辑；而当没有页面时，则通过 defaultMessageHandler 去发送 `Notification`。



## 群聊

对于多人同时参与的固定群组，我们有成员人数限制，最大不能超过 500 人。对于另外一种多人聊天的形式，譬如聊天室，其成员不固定，用户可以随意进入发言的这种「临时性」群组，后面会单独介绍。

### 发送消息

Tom 想建立一个群，把自己好朋友都拉进这个群，然后给他们发消息，他需要做的事情是：

1. 建立一个朋友列表
2. 新建一个对话，把朋友们列为对话的参与人员
3. 发送消息


```
  public void sendMessageToJerryFromTom() {
    // Tom 用自己的名字作为clientId，获取AVIMClient对象实例
    AVIMClient tom = AVIMClient.getInstance("Tom");
    // 与服务器连接
    tom.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 创建与 Jerry，Bob,Harry,William 之间的对话
          client.createConversation(Arrays.asList("Jerry","Bob","Harry","William"), "Tom & Jerry & friedns", null,
              new AVIMConversationCreatedCallback() {

                @Override
                public void done(AVIMConversation conversation, AVIMException e) {
                  if (e == null) {
                    AVIMTextMessage msg = new AVIMTextMessage();
                    msg.setText("你们在哪儿？");
                    // 发送消息
                    conversation.sendMessage(msg, new AVIMConversationCallback() {

                      @Override
                      public void done(AVIMException e) {
                        if (e == null) {
                          Log.d("Tom & Jerry", "发送成功！");
                        }
                      }
                    });
                  }
                }
              });
        }
      }
    });
  }
```

### 接收消息

群聊的接收消息与单聊的接收消息在代码写法上是一致的。


```
public class MyApplication extends Application{
  public void onCreate(){
   ...
   AVOSCloud.initialize(this,"{{appid}}","{{appkey}}");
   //这里指定只处理AVIMTextMessage类型的消息
   AVIMMessageManager.registerMessageHandler(AVIMTextMessage.class,new CustomMessageHanlder());
  }
}

- CustomMessageHandler.java
public class CustomMessageHandler<AVIMTextMessage> implements AVIMTypedMessageHandler{
 
  @Override
  public void onMessage(AVIMTextMessage msg,AVIMConversation conv,AVIMClient client){
    Log.d("Tom & Jerry",msg.getText();)//你们在哪儿?
    // 收到消息之后一般的做法是做 UI 展现，示例代码在此处做消息回复，仅为了演示收到消息之后的操作，仅供参考。
    AVIMTextMessage reply = new AVIMTextMessage();
    reply.setText("Tom，我在 Jerry 家，你跟 Harry 什么时候过来？还有 William 和你在一起么？");
    conv.sendMessage(reply,new AVIMConversationCallback(){
  	   public void done(AVIMException e){
  	     if(e==null){
  	     //回复成功!
  	     }
  	   }
  	 });
  }
  
public void onMessageReceipt(AVIMTextMessage msg,AVIMConversation conv,AVIMClient client){
  
}
}


- SomeActivity.java
public void loginAsBob(){
	AVIMClient bob = AVIMClient.getInstance("Bob");
	//Bob登录
	bob.open(new AVIMClientCallback(){
	  public void done(AVIMClient client,AVIMException e){
	  	if(e==null){
	  		//登录成功
	  	}
	  }
	});
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



```
public void sendImage(String filePath){
  AVIMClient tom = AVIMClient.getInstance("Tom");

  tom.open(new AVIMClientCallback(){
  
    @Override
    public void done(AVIMClient client,AVIMException e){
      if(e==null){
      //登录成功
      // 创建对话，默认创建者是在包含在成员列表中的
      client.createConversation(Arrays.asList("Jerry"),new AVIMConversationCreatedCallback(){
      
        @Override
        public void done(AVIMConversation conv,AVIMException e){
          if(e==null){
            AVIMImageMessage picture = new AVIMImageMessage(filePath);
            picture.setText("发自我的小米");
            Map<String,Object> attributes = new HashMap<String,Object>();
            attributes.put("location","旧金山");
            picture.setAttribute(attributes);
            conv.sendMessage(picture,new AVIMConversationCallback(){
              
              @Override
              public void done(AVIMException e){
                if(e==null){
                //发送成功！
                }
              }
            });
          }
        }
      });
      }
    }
  });
}
```


【场景二】从微博上复制的一个图像链接来创建图像消息：


```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 创建名为“猫和老鼠”的对话
          client.createConversation(Arrays.asList("Jerry"), "猫和老鼠", null,
              new AVIMConversationCreatedCallback() {
                @Override
                public void done(AVIMConversation conv, AVIMException e) {
                  if (e == null) {
                    AVFile file =new AVFile("萌妹子","http://pic2.zhimg.com/6c10e6053c739ed0ce676a0aff15cf1c.gif", null);
                    AVIMImageMessage m = new AVIMImageMessage(file);
                    m.setText("萌妹子一枚");
                    // 创建一条图片消息
                    conv.sendMessage(m, new AVIMConversationCallback() {
                      @Override
                      public void done(AVIMException e) {
                        if (e == null) {
                          // 发送成功
                        }
                      }
                    });
                  }
                }
              });
        }
      }
});
```


以上两种场景对于 SDK 的区别为：

* 场景一：SDK 获取了完整的图像数据流，先上传文件到云端，再将文件的元数据以及 URL 等一并包装，发送出去。

* 场景二：SDK 并没有将图像实际上传到云端，而仅仅把 URL 包装在消息体内发送出去，这种情况下接收方是无法从消息体中获取图像的元信息数据，但是接收方可以自行通过客户端技术去分析图片的格式、大小、长宽之类的元数据。

##### 接收图像消息





```
//注册消息处理逻辑
AVIMMessageManager.registerMessageHandler(AVIMImageMessage.class,
        new AVIMTypedMessageHandler<AVIMImageMessage>() {

          @Override
          public void onMessage(AVIMImageMessage msg, AVIMConversation conv, AVIMClient client) {
          	//只处理 Jerry 这个客户端的消息
          	//并且来自 conversationId 为 55117292e4b065f7ee9edd29 的conversation 的消息	
            if ("Jerry".equals(client.getClientId()) && "55117292e4b065f7ee9edd29".equals(conv.getConversationId())) {
                String fromClientId = msg.getFrom();
                String messageId = msg.getMessageId();
                String url = msg.getFileUrl();
                Map<String, Object> metaData = msg.getFileMetaData();
                if (metaData.containsKey("size")) {
                  int size = (Integer) metaData.get("size");
                }
                if (metaData.containsKey("width")) {
                  int width = (Integer) metaData.get("width");
                }
                if (metaData.containsKey("height")) {
                  int height = (Integer) metaData.get("height");
                }
                if (metaData.containsKey("format")) {
                  String format = (String) metaData.get("format");
                }
            }
          }
        });
        
    AVIMClient jerry = AVIMClient.getInstance("Jerry");
    jerry.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {

        }
      }
    });
```


#### 音频消息

##### 发送音频消息

发送音频消息的基本流程是：读取音频文件（或者录制音频）> 构建音频消息 > 消息发送。


```
 AVIMClient tom = AVIMClient.getInstance("Tom");
    tom.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 创建名为“猫和老鼠”的对话
          client.createConversation(Arrays.asList("Jerry"), "猫和老鼠", null,
              new AVIMConversationCreatedCallback() {
                @Override
                public void done(AVIMConversation conv, AVIMException e) {
                  if (e == null) {
                    AVFile file = AVFile.withAbsoluteLocalPath("忐忑.mp3",localFilePath);
                    AVIMAudioMessage m = new AVIMAudioMessage(file);
                    m.setText("听听人类的神曲~");
                    // 创建一条音频消息
                    conv.sendMessage(m, new AVIMConversationCallback() {
                      @Override
                      public void done(AVIMException e) {
                        if (e == null) {
                          // 发送成功
                        }
                      }
                    });
                  }
                }
              });
        }
      }
    });
``` 


与图像消息类似，音频消息也支持从 URL 构建：


```
AVIMClient tom = AVIMClient.getInstance("Tom");
    tom.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 创建名为「猫和老鼠」的对话
          client.createConversation(Arrays.asList("Jerry"), "猫和老鼠", null,
              new AVIMConversationCreatedCallback() {
                @Override
                public void done(AVIMConversation conv, AVIMException e) {
                  if (e == null) {
                    AVFile file = new AVFile("music", "http://ac-lhzo7z96.clouddn.com/1427444393952", null);
                    AVIMAudioMessage m = new AVIMAudioMessage(file);
                    // 创建一条音频消息
                    conv.sendMessage(m, new AVIMConversationCallback() {
                      @Override
                      public void done(AVIMException e) {
                        if (e == null) {
                          // 发送成功
                        }
                      }
                    });
                  }
                }
              });
        }
      }
    });
```


##### 接收音频消息


```
AVIMMessageManager.registerMessageHandler(AVIMAudioMessage.class,
        new AVIMTypedMessageHandler<AVIMAudioMessage>() {

          @Override
          public void onMessage(AVIMAudioMessage msg, AVIMConversation conv, AVIMClient client) {
          	//只处理 Jerry 这个客户端的消息
          	//并且来自 conversationId 为 55117292e4b065f7ee9edd29 的conversation 的消息	
            if ("Jerry".equals(client.getClientId()) && "55117292e4b065f7ee9edd29".equals(conv.getConversationId())) {
                String fromClientId = msg.getFrom();
                String messageId = msg.getMessageId();
                String url = msg.getFileUrl();
                Map<String, Object> metaData = msg.getFileMetaData();
                if (metaData.containsKey("size")) {
                  int size = (Integer) metaData.get("size");
                }
                if (metaData.containsKey("format")) {
                  String format = (String) metaData.get("format");
                }
            }
          }
        });
        
    AVIMClient jerry = AVIMClient.getInstance("Jerry");
    jerry.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {

        }
      }
    });
```


#### 视频消息

##### 发送视频消息

与发送音频消息的流程类似，视频的来源可以是手机录制，可以是系统中某一个具体的视频文件：


```
  AVIMClient tom = AVIMClient.getInstance("Tom");
  tom.open(new AVIMClientCallback() {
    @Override
    public void done(AVIMClient client, AVIMException e) {
      if (e == null) {
        // 创建名为“猫和老鼠”的对话
        client.createConversation(Arrays.asList("Jerry"), "猫和老鼠", null,
                new AVIMConversationCreatedCallback() {
                  @Override
                  public void done(AVIMConversation conv, AVIMException e) {
                    if (e == null) {
                      AVFile file = AVFile.withAbsoluteLocalPath("bbc_奶酪.mp4", localFilePath);
                      AVIMVideoMessage m = new AVIMVideoMessage(file);
                      // 创建一条视频消息
                      conv.sendMessage(m, new AVIMConversationCallback() {
                        @Override
                        public void done(AVIMException e) {
                          if (e == null) {
                            // 发送成功
                          }
                        }
                      });
                    }
                  }
                });
      }
    }
  });
```


同样我们也支持从一个视频的 URL 创建视频消息，然后发送出去：


```
 AVIMClient tom = AVIMClient.getInstance("Tom");
    tom.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 创建名为「猫和老鼠」的对话
          client.createConversation(Arrays.asList("Jerry"), "猫和老鼠", null,
              new AVIMConversationCreatedCallback() {
                @Override
                public void done(AVIMConversation conv, AVIMException e) {
                  if (e == null) {
                    AVFile file =
                        new AVFile("video", "http://ac-lhzo7z96.clouddn.com/1427267336319", null);
                    AVIMVideoMessage m = new AVIMVideoMessage(file);
                    // 创建一条视频消息
                    conv.sendMessage(m, new AVIMConversationCallback() {
                      @Override
                      public void done(AVIMException e) {
                        if (e == null) {
                          // 发送成功
                        }
                      }
                    });
                  }
                }
              });
        }
      }
    });
```


**注：这里说的 URL 指的是视频文件自身的 URL，而不是视频网站上播放页的 URL。**

##### 接收视频消息


视频消息的接收与图像消息一样，它的元数据都可以通过 `getFileMetaData()` 来获取。




#### 地理位置消息

地理位置消息构建方式如下：


```
      AVIMLocationMessage m = new AVIMLocationMessage();
      m.setLocation(new AVGeoPoint(45.0,34.0));
 ```


##### 发送地理位置消息


```
final AVIMLocationMessage locationMessage=new AVIMLocationMessage();
// 开发者更可以通过具体的设备的 API 去获取设备的地理位置，此处仅设置了 2 个经纬度常量仅做演示
locationMessage.setLocation(new AVGeoPoint(138.12454,52.56461));
locationMessage.setText("新开的蛋糕店！耗子咱们有福了…");
conversation.sendMessage(locationMessage, new AVIMConversationCallback() {
    @Override
    public void done(AVIMException e) {
        if (null != e) {
          e.printStackTrace();
        } else {
          // 发送成功
        }
    }
});
```


##### 接收地理位置消息



地址消息的接收与图像消息一样，它的地址信息可以通过 `getLocation` 方法来获取




### 接收富媒体消息

实时通信 SDK 内部封装了对富媒体消息的支持，所有富媒体消息都是从 AVIMTypedMessage 派生出来的。发送的时候可以直接调用 `conversation.sendMessage()` 函数。在接收端，我们也专门增加了一类回调接口 AVIMTypedMessageHandler，其定义为：

```
public class AVIMTypedMessageHandler<T extends AVIMTypedMessage> extends MessageHandler<T> {

  @Override
  public void onMessage(T message, AVIMConversation conversation, AVIMClient client);

  @Override
  public void onMessageReceipt(T message, AVIMConversation conversation, AVIMClient client);
}
```

开发者可以编写自己的消息处理 handler，然后调用 `AVIMMessageManager.registerMessageHandler()` 函数来注册目标 handler。

接收端对于富媒体消息的通知处理的示例代码如下：

```
class MsgHandler extends AVIMTypedMessageHandler<AVIMTypedMessage> {

  @Override
  public void onMessage(AVIMTypedMessage message, AVIMConversation conversation, AVIMClient client) {
    // 请按自己需求改写
    switch(message.getMessageType()) {
    case AVIMReservedMessageType.TextMessageType:
      AVIMTextMessage textMsg = (AVIMTextMessage)message;
      Logger.d("收到文本消息:" + textMsg.getText() + ", msgId:" + textMsg.getMessageId());
      break;

    case AVIMReservedMessageType.FileMessageType:
      AVIMFileMessage fileMsg = (AVIMFileMessage)message;
      Logger.id("收到文件消息。msgId=" + fileMsg.getMessageId() + ", url=" + fileMsg.getFileUrl() + ", size=" + fileMsg.getSize());
      break;

    case AVIMReservedMessageType.ImageMessageType:
      AVIMImageMessage imageMsg = (AVIMImageMessage)message;
      Logger.id("收到图片消息。msgId=" + imageMsg.getMessageId() + ", url=" + imageMsg.getFileUrl() + ", width=" + imageMsg.getWidth() + ", height=" + imageMsg.getHeight());
      break;

    case AVIMReservedMessageType.AudioMessageType:
      AVIMAudioMessage audioMsg = (AVIMAudioMessage)message;
      Logger.id("收到音频消息。msgId=" + audioMsg.getMessageId() + ", url=" + audioMsg.getFileUrl() + ", duration=" + audioMsg.getDuration());
      break;

    case AVIMReservedMessageType.VideoMessageType:
      AVIMVideoMessage videoMsg = (AVIMAudioMessage)message;
      Logger.id("收到视频消息。msgId=" + videoMsg.getMessageId() + ", url=" + videoMsg.getFileUrl() + ", duration=" + videoMsg.getDuration());
      break;

    case AVIMReservedMessageType.LocationMessageType:
      AVIMLocationMessage locMsg = (AVIMLocationMessage)message;
      Logger.id("收到位置消息。msgId=" + locMsg.getMessageId() + ", latitude=" + locMsg.getLocation().getLatitude() + ", longitude=" + locMsg.getLocation().getLongitude());
      break;
    }
  }

  @Override
  public void onMessageReceipt(AVIMTypedMessage message, AVIMConversation conversation, AVIMClient client) {
    // 请加入你自己需要的逻辑...
  }
}

MsgHandler msgHandler = new MsgHandler();
AVIMMessageManager.registerMessageHandler(AVIMTypedMessage.class, msgHandler);
```

SDK 内部在接收消息时的处理逻辑是这样的：

* 当收到新消息时，实时通信 SDK 会先解析消息的类型，然后找到开发者为这一类型所注册的处理响应 handler chain，再逐一调用这些 handler 的 onMessage 函数
* 如果没有找到专门处理这一类型消息的 handler，就会转交给 defaultHandler 处理。

这样一来，在开发者为 `AVIMTypedMessage`（及其子类） 指定了专门的 handler，也指定了全局的 defaultHandler 了的时候，如果发送端发送的是通用的 AVIMMessage 消息，那么接受端就是 `AVIMMessageManager.registerDefaultMessageHandler()` 中指定的 handler 被调用；如果发送的是 AVIMTypedMessage（及其子类）的消息，那么接受端就是 `AVIMMessageManager.registerMessageHandler()` 中指定的 handler 被调用。


### 暂态消息

暂态消息不会被自动保存（以后在历史消息中无法找到它），也不支持延迟接收，离线用户更不会收到推送通知，所以适合用来做控制协议。譬如聊天过程中「某某正在输入...」这样的状态信息，就适合通过暂态消息来发送；或者当群聊的名称修改以后，也可以用暂态消息来通知该群的成员「群名称被某某修改为...」。



```
//自定义的消息类型，用于发送和接收所有的用户操作消息
- AVIMOperationMessage.java
 
//指定type类型，可以根据实际换成其他正整数
@AVIMMessageType(type = 1)
public class AVIMOperationMessage extends AVIMTypedMessage {

  @AVIMMessageField(name = "op")
  String op;

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }
}

- CustomApplication.java
public CustomApplication extends Application {
   ...
   //注册自定义的消息类型
   AVIMMessageManager.registerAVIMMessageType(AVIMOperationMessage.class);
   ...
}

- SomeActivity.java
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 登录成功
          AVIMConversation conv = client.getConversation("551260efe4b01608686c3e0f");
          AVIMOperationMessage msg = new AVIMOperationMessage();
          msg.setOp("keyboard inputing");
          // AVIMConversation.TRANSIENT_MESSAGE_FLAG 表示该条消息为暂态消息
          // 
          conv.sendMessage(msg, AVIMConversation.TRANSIENT_MESSAGE_FLAG,
              new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                  if (e == null) {
                    // 发送成功
                  }
                }
              });
        }
      }
    });
```


而对话中的其他成员在聊天界面中需要有以下代码做出响应：



```
//自定义的消息类型，用于发送和接收所有的用户操作消息
- AVIMOperationMessage.java
 
//指定type类型，可以根据实际换成其他正整数
@AVIMMessageType(type = 1)
public class AVIMOperationMessage extends AVIMTypedMessage {

  @AVIMMessageField(name = "op")
  String op;

  public String getOp() {
    return op;
  }

  public void setOp(String op) {
    this.op = op;
  }
}

- CustomApplication.java
public CustomApplication extends Application {
   ...
   //注册自定义的消息类型
   AVIMMessageManager.registerAVIMMessageType(AVIMOperationMessage.class);
   ...
}

- SomeActivity.java
final String USER_OPERATION = "% is %";
// 设置消息接收的 Handler，接收到消息之后的将执行具体的操作
AVIMMessageManager.registerMessageHandler(AVIMOperationMessage.class,
    new AVIMTypedMessageHandler<AVIMOperationMessage>() {
        @Override
        public void onMessage(AVIMOperationMessage msg, AVIMConversation conv, AVIMClient client) {
            if ("Jerry".equals(client.getClientId())
                && "551260efe4b01608686c3e0f".equals(conv.getConversationId())) {
              String opeartion = String.format(USER_OPERATION, msg.getFrom(), msg.getOp());
              System.out.println(opeartion);
            }
        }
});
        
// 登录操作，建立和服务端的连接，开始接收消息
AVIMClient jerry = AVIMClient.getInstance("Jerry");
jerry.open(new AVIMClientCallback() {
    @Override
    public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          // 登录成功
        }
      }
});
```


### 消息的发送

#### 消息发送接口
在 Android SDK 中，发送消息的方法是：`AVIMConversation.sendMessage`，它最核心的一个重载声明如下：

```
/**
 *
 * @param message 发送的消息实体，可以是任何 AVIMMessage 子类
 * @param messageFlag AVIMConversation.TRANSIENT_MESSAGE_FLAG(0)：暂态消息，只有在消息发送时，对方也是在线的才能收到这条消息；
 *                    AVIMConversation.NONTRANSIENT_MESSAGE_FLAG(1)：非暂态消息，当消息发送时，对方不在线的话，消息会变成离线消息；
 *                    AVIMConversation.RECEIPT_MESSAGE_FLAG(17)：回执消息，当消息送到到对方以后，发送方会收到消息回执说明消息已经成功达到接收方
 * @param callback 消息发送之后的回调，发送异常或者发送成功都可以在回调里进行操作
 */
public void sendMessage(final AVIMMessage message, final int messageFlag, final AVIMConversationCallback callback)
```
为了满足通用需求，SDK 还提供了一个更为常用的重载声明：

```
/**
 *
 * @param message 发送的消息实体，可以是任何 AVIMMessage 子类
 * @param callback 消息发送之后的回调，发送异常或者发送成功都可以在回调里进行操作
 */
public void sendMessage(AVIMMessage message, AVIMConversationCallback callback)
```

其实本质上，调用 `sendMessage(message, callback)` 就等价于调用 `sendMessage(message,1, callback)` ，因为一般情况下消息存在的形式多以**非暂态**消息为主





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

>**Android 聊天服务是和后台的推送服务共享连接的，所以只要有网络就永远在线，不需要专门做推送。**消息达到后，你可以根据用户的设置来判断是否需要弹出通知。网络断开时，我们为每个对话保存 20 条离线消息。

这一功能默认是关闭的，你可以在 LeanCloud 应用控制台中开启它。操作方法请参考 [实时通信概览 &middot; 离线推送通知](realtime_v2.html#离线推送通知)。



#### 消息送达回执

是指消息被对方收到之后，云端会发送一个回执通知给发送方，表明消息已经送达。
需要注意的是：

> 只有在发送时设置了「等待回执」标记，云端才会发送回执，默认不发送回执。该回执并不代表用户已读。



```
AVIMMessageHandler handler = new AVIMMessageHandler(){

    public void onMessageReceipt(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
     //此处就是对方收到消息以后的回调
	  Log.i("Tom & Jerry","msg received");
  }
}

//注册对应的handler
AVIMMessageManager.registerMessageHandler(AVIMMessage.class,handler);

//发送消息

AVIMClient jerry = AVIMClient.getInstance("Jerry");
AVIMConversation conv = jerry.getConversation("551260efe4b01608686c3e0f");
AVIMMessage msg = new AVIMMessage();
msg.setContent("Ping");
conv.sendMessage(msg,AVIMConversation.RECEIPT_MESSAGE_FLAG);

```


### 消息的接收






#### 未拉取的离线消息

未拉取的离线消息指的是客户端尚未主动拉取到本地的消息。

SDK 默认的接收机制是：当客户端上线时，离线消息会自动通过长连接发送至客户端。如果开启了「未读消息」，消息接收机制变为：当客户端上线时，会收到其参与过的会话的离线消息数量，云端不再主动将离线消息通知发送至客户端，转而由客户端负责主动拉取。


要开启未读消息，需要在 AVOSCloud 初始化语句后面加上：

```
AVIMClient.setOfflineMessagePush(true);
```

然后实现 AVIMConversationEventHandler 的代理方法 `onOfflineMessagesUnread` 来从服务端取回未读消息：

```
onOfflineMessagesUnread(AVIMClient client, AVIMConversation conversation, int unreadCount) {
  //如果有多个 conversation 有未读消息，此函数会执行多次
  if (unreadCount > 0) {
    // 可以根据 readCount 更新 UI
    
    // 也可以拉取对应的未读消息
    conversation.queryMessages(unreadCount, new AVIMMessagesQueryCallback() {
      @Override
      public void done(List<AVIMMessage> list, AVIMException e) {
        if (e == null) {
          // 获得对应的未读消息
        }
      }
    });
  }
}
```
`AVIMConversationEventHandler` 的实现和定义在[自身主动加入](#自身主动加入)里面有详细的代码和介绍。


注意：客户端<u>在线上时</u>收到的消息默认为**已读**，不存在未读的情况。因此开发者不需要将这样的消息标记为已读。


### 消息类详解

消息类型之间的关系

![消息的类图](http://ac-lhzo7z96.clouddn.com/1440485935481)




消息类均包含以下公用属性：

属性|描述|类型
---|---|---
content|String|消息内容
clientId|String|指消息发送者的 clientId 
conversationId|String|消息所属对话 id
messageId|String|消息发送成功之后，由 LeanCloud 云端给每条消息赋予的唯一 id 
timestamp|long|消息发送的时间。消息发送成功之后，由 LeanCloud 云端赋予的全局的时间戳。
receiptTimestamp|long| 消息被对方接收到的时间。消息被接收之后，由 LeanCloud 云端赋予的全局的时间戳。
status|AVIMMessageStatus 枚举|消息状态，有五种取值：<br/><br/>`AVIMMessageStatusNone`（未知）<br/>`AVIMMessageStatusSending`（发送中）<br/>`AVIMMessageStatusSent`（发送成功）<br/>`AVIMMessageStatusReceipt`（被接收）<br/>`AVIMMessageStatusFailed`（失败）
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
 


### 自定义消息

在某些场景下，开发者需要在发送消息时附带上自己业务逻辑需求的自定义属性，比如消息发送的设备名称，或是图像消息的拍摄地点、视频消息的来源等等，开发者可以通过 `AVIMTypedMessage.attributes` 实现这一需求。

【场景】发照片给朋友，告诉对方照片的拍摄地点：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback() {
    @Override
    public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
            AVIMImageMessage msg = new AVIMImageMessage(someLocalFile);
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("location", "拉萨布达拉宫");
            attributes.put("Title", "这蓝天……我彻底是醉了");
            msg.setAttrs(attributes);
            client.getConversation("551260efe4b01608686c3e0f").sendMessage(msg,
                new AVIMConversationCallback() {
                    @Override
                    public void done(AVIMException e) {
                      if (e == null) {
                    // 发送成功
                    }
                }
            });
        }
      }
});
```


接收时可以读取这一属性：



```
AVIMMessageManager.registerMessageHandler(AVIMImageMessage.class,
    new AVIMTypedMessageHandler<AVIMImageMessage>() {
        @Override
            public void onMessage(AVIMImageMessage msg, AVIMConversation conv, AVIMClient client) {
                //此处应该是"拉萨布达拉宫"
                System.out.println(msg.getAttrs().get("location"));
            }
    }
);

AVIMClient friend = AVIMClient.getInstance("friend");
friend.open(new AVIMClientCallback() {
    @Override
    public void done(AVIMClient client, AVIMException e) {
        if (e == null) {}
    }
});
```


所有的 `AVIMTypedMessage` 消息都支持 `attributes` 这一属性。

#### 创建新的消息类型


继承于 AVIMTypedMessage，开发者也可以扩展自己的富媒体消息。其要求和步骤是：

* 实现新的消息类型，继承自 AVIMTypedMessage。这里需要注意两点：
  * 在 class 上增加一个 @AVIMMessageType(type=123) 的 Annotation，具体消息类型的值（这里是 `123`）由开发者自己决定（LeanCloud 内建的 [消息类型使用负数](#消息类详解)，所有正数都预留给开发者扩展使用）。
  * 在消息内部属性上要增加 @AVIMMessageField(name="") 的 Annotation，name 为可选字段在声明字段属性，同时自定义的字段要有对应的 getter/setter 方法。
* 调用 `AVIMMessageManager.registerAVIMMessageType()` 函数进行类型注册。
* 调用 `AVIMMessageManager.registerMessageHandler()` 函数进行消息处理 handler 注册。

AVIMTextMessage 的源码如下，可供参考：

```
@AVIMMessageType(type = -1)
public class AVIMTextMessage extends AVIMTypedMessage {

  @AVIMMessageField(name = "_lctext")
  String text;
  @AVIMMessageField(name = "_lcattrs")
  Map<String, Object> attrs;

  public String getText() {
    return this.text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public Map<String, Object> getAttrs() {
    return this.attrs;
  }

  public void setAttrs(Map<String, Object> attr) {
    this.attrs = attr;
  }
}
```


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


```
AVIMClient jerry = AVIMClient.getInstance("Jerry");
jerry.open(new AVIMClientCallback() {
    @Override
    public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
            // 创建名为“猫和老鼠”的对话
            client.createConversation(Arrays.asList("Bob", "Harry", "William"), "猫和老鼠", null,
                new AVIMConversationCreatedCallback() {
                    @Override
                        public void done(AVIMConversation conv, AVIMException e) {
                            if (e == null) {
                                // 创建成功
                            }
                    }
             });
        }
      }
});
```



### 创建对话
创建对话的接口在 `AVIMClient` 中共有 4 个方法重写，下面我们以参数最详尽的这个重写来说明其中每个参数的意义。

```
  /**
   * 创建或查询一个已有 conversation
   *
   * @param members 对话的成员
   * @param name 对话的名字
   * @param attributes 对话的额外属性
   * @param isTransient 是否是暂态对话
   * @param isUnique 如果已经存在符合条件的对话，是否返回已有对话
   *                 为 false 时，则一直为创建新的对话
   *                 为 true 时，则先查询，如果已有符合条件的对话，则返回已有的，否则，创建新的并返回
   *                 为 true 时，仅 members 为有效查询条件
   * @param callback
   */
  public void createConversation(final List<String> members, final String name,
      final Map<String, Object> attributes, final boolean isTransient, final boolean isUnique,
      final AVIMConversationCreatedCallback callback)
```
参数说明：

* members - 对话的初始成员列表。在对话创建成功后，这些成员会收到和邀请加入对话一样的相应通知。
* name - 对话的名字，主要是用于标记对话，让用户更好地识别对话。
* attributes - 额外属性
* isTransient - 是否为 [暂态对话](#聊天室)
* isUnique - 是否创建唯一对话，当 `isUnique` 为 true 时，如果当前已经有**相同成员**的对话存在则返回该对话，否则会创建新的对话。该值默认为 false。

<div class="callout callout-info">由于暂态对话不支持创建唯一对话，所以将 `isTransient` 和 `isUnique` 同时设为 true 时并不会产生预期效果。</div>


### 对话的成员管理

成员管理，是在对话中对成员的一个实时生效的操作，一旦操作成功则不可逆。

#### 成员变更接口
成员变更操作接口简介如下表：

操作目的|接口名
----|---
自身主动加入 |  `AVIMConversation.join`
添加其他成员 |  `AVIMConversation.addMembersWithClientIds`
自身主动退出 |  `AVIMConversation.quitWithCallback`
剔除其他成员 |  `AVIMConversation.removeMembersWithClientIds`

成员变动之后，所有对话成员如果在线的话，都会得到相应的通知。


在 Android 中，开发者需要实现 `AVIMConversationEventHandler` 代理，并且为 `AVIMClient` 指定该代理的一个实例。

`AVIMConversationEventHandler` 的实现和定义在下一节[自身主动加入](#自身主动加入)里面有详细的代码和介绍。



#### 自身主动加入

Tom 想主动加入 Jerry、Bob、Harry 和 William 的对话，以下代码将帮助他实现这个功能：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	    //登录成功
		AVIMConversation conv = client.getConversation("551260efe4b01608686c3e0f");
		conv.join(new AVIMConversationCallback(){
			@Override
			public void done(AVIMException e){
			  if(e==null){
			  //加入成功
			  }
			}
		});
	  }
	}
});

```



该群的其他成员（比如 Bob）登录之后，调用 `AVIMMessageManager.setConversationEventHandler` 设置一下回调的代理，会收到该操作的事件回调：

```
AVIMMessageManager.setConversationEventHandler(new CustomConversationEventHandler());
AVIMClient bob = AVIMClient.getInstance("Bob");
bob.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  }
	}
});

-- CustomConversationEventHandler.java
public class CustomConversationEventHandler extends AVIMConversationEventHandler {

  @Override
  public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members,
      String kickedBy) {
      // 有其他成员离开时，执行此处逻辑
  }

  @Override
  public void onMemberJoined(AVIMClient client, AVIMConversation conversation,
      List<String> members, String invitedBy) {
      // 手机屏幕上会显示一小段文字：Tom 加入到 551260efe4b01608686c3e0f ；操作者为：Tom
      Toast.makeText(AVOSCloud.applicationContext,
        members + "加入到" + conversation.getConversationId() + "；操作者为： "
            + invitedBy, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {
    // 当前 ClientId(Bob) 被踢出对话，执行此处逻辑
  }

  @Override
  public void onInvited(AVIMClient client, AVIMConversation conversation, String invitedBy) {
    // 当前 ClientId(Bob) 被邀请到对话，执行此处逻辑
  }
}

```



#### 添加其他成员

Jerry 想再把 Mary 加入到对话中，需要如下代码帮助他实现这个功能：



```
 AVIMClient jerry = AVIMClient.getInstance("Jerry");
    jerry.open(new AVIMClientCallback() {

      @Override
      public void done(AVIMClient client, AVIMException e) {
        if (e == null) {
          //登录成功
          final AVIMConversation conv = client.getConversation("551260efe4b01608686c3e0f");
          conv.join(new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
              if (e == null) {
                //加入成功
                conv.addMembers(Arrays.asList("Mary"), new AVIMConversationCallback() {
                  @Override
                  public void done(AVIMException e) {
                  }
                });
              }
            }
          });
        }
      }
    });

```


该对话的其他成员（例如 Harry）也会受到该项操作的影响，收到事件被响应的通知，类似于第一小节 [自身主动加入](#自身主动加入) 中**Tom 加入对话之后，Bob 受到的影响。**


邀请成功以后，相关方收到通知的时序是这样的：

No.|邀请者|被邀请者|其他人
---|---|---|---
1|发出请求 addMembers| | 
2| |收到 onInvited 通知| 
3|收到 onMemberJoined 通知| | 收到 onMemberJoined 通知


>注意：如果在进行邀请操作时，被邀请者不在线，那么通知消息并不会被离线缓存，所以等到 Ta 再次上线的时候将不会收到通知。

#### 自身退出对话
这里一定要区分**自身退出对话**的主动性，它与**自身被动被踢出**（下一小节）在逻辑上完全是不一样的。

Tom 主动从对话中退出，他需要如下代码实现需求：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
		final AVIMConversation conv = client.getConversation("551260efe4b01608686c3e0f");
		conv.join(new AVIMConversationCallback(){
			@Override
			public void done(AVIMException e){
			  if(e==null){
			  //加入成功
			  conv.quit(new AVIMConversationCallback(){
			    @Override
			    public void done(AVIMException e){
			      if(e==null){
			      //退出成功
			      }
			    } 
			  });
			  }
			}
		});
	  }
	}
});
``` 


#### 剔除其他成员

Harry 被 William 从对话中删除。实现代码如下（关于 William 如何获得权限在后面的 [签名和安全](#签名和安全) 中会做详细阐述，此处不宜扩大话题范围。）：



```
AVIMClient william = AVIMClient.getInstance("William");
william.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
		final AVIMConversation conv = client.getConversation("551260efe4b01608686c3e0f");
		conv.join(new AVIMConversationCallback(){
			@Override
			public void done(AVIMException e){
			  if(e==null){
			  //加入成功
			  conv.kickMembers(Arrays.asList("Harry"),new AVIMConversationCallback(){
			  
			  	 @Override
			    public void done(AVIMException e){
			    }
			  );
			  }
			}
		});
	  }
	}
});
```



踢人时，相关方收到通知的时序如下：

No.|操作者（管理员）|被踢者|其他人
---|---|---|---
1|发出请求 kickMembers| | 
2| |收到 onKicked 通知| 
3|收到 onMemberLeft 通知| |收到 onMemberLeft 通知


>注意：如果在进行踢人操作时，被踢者不在线，那么通知消息并不会被离线缓存，所以等到 Ta 再次上线的时候将不会收到通知。

#### 查询成员数量
 `AVIMConversation.getMemberCount` 这个方法返回的是实时数据：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  query.setLimit(1);
	  query.findInBackground(new AVIMConversationQueryCallback(){
       @Override
       public void done(List<AVIMConversation> convs,AVIMException e){
         if(e==null){
           if(convs!=null && !convs.isEmpty()){
             AVIMConversation conv = convs.get(0);
             conv.getMemberCount(new AVIMConversationMemberCountCallback(){
               
               @Override
               public void done(Integer count,AVIMException e){			
               if(e==null){						
               Log.d("Tom & Jerry","conversation got "+count+" members");
				}
               }
             });
           }
         }
       }
     });
	  }
	}
});
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
`isTransient`|`tr`|是否为聊天室（暂态对话）
`lastMessageAt`|`lm`|该对话最后一条消息，也可以理解为最后一次活跃时间



#### 名称

这是一个全员共享的属性，它可以在创建时指定，也可以在日后的维护中被修改。

Tom 想建立一个名字叫「喵星人」 对话并且邀请了好友 Black 加入对话：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  client.createConversation(Arrays.asList("Black"),"喵星人",null,
	           new AVIMConversationCreatedCallback(){
	           
	             @Override
	             public void done(AVIMConversation conv,AVIMException e){
	               if(e==null){
	                 //创建成功
	               }
	             }
	           });
	  }
	}
});

```


Black 发现对话名字不够酷，他想修改成「聪明的喵星人」 ，他需要如下代码：



```
AVIMClient black = AVIMClient.getInstance("Black");
black.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversation conv = client.getConversation("55117292e4b065f7ee9edd29");
	  conv.setName("聪明的喵星人");
	  conv.updateInfoInBackground(new AVIMConversationCallback(){
	    
	    @Override
	    public void done(AVIMException e){	    
	      if(e==null){
	      //更新成功
	      }
	    }
	  });
	  }
	}
});
```


####  成员

是当前对话中所有成员的 `clientId`。默认情况下，创建者是在包含在成员列表中的，直到 TA 退出对话。

>**强烈建议开发者切勿在控制台中对其进行修改**。所有关于成员的操作请参照上一章节中的 [对话的成员管理](#对话的成员管理) 来进行。

#### 静音
假如某一用户不想再收到某对话的消息提醒，但又不想直接退出对话，可以使用静音操作，即开启「免打扰模式」。

比如 Tom 工作繁忙，对某个对话设置了静音：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversation conv = client.getConversation("551260efe4b01608686c3e0f");
	  conv.mute(new AVIMConversationCallback(){
	  
	    @Override
	    public void done(AVIMException e){
	      if(e==null){
	      //设置成功
	      }
	    }
	  });
	  }
	}
});

```


>设置静音之后，iOS 和 Windows Phone 的用户就不会收到推送消息了。

与之对应的就是取消静音的操作，即取消免打扰模式。此操作会修改云端 `_Conversation` 里面的 `mu` 属性。**强烈建议开发者切勿在控制台中对 `mu` 随意进行修改**。

#### 创建者

即对话的创建者，它的值是对话创建者的 `clientId`。

它等价于 QQ 群中的「群创建者」，但区别于「群管理员」。比如 QQ 群的「创建者」是固定不变的，它的图标颜色与「管理员」的图标颜色都不一样。所以根据对话中成员的 `clientId` 是否与 `AVIMConversation.creator` 一致就可以判断出他是不是群的创建者。

#### 自定义属性

通过该属性，开发者可以随意存储自己的键值对，为对话添加自定义属性，来满足业务逻辑需求。

给某个对话加上两个自定义的属性：type = "private"（类型为私有）、isSticky = true（置顶显示）：


```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  HashMap<String,Object> attr = new HashMap<String,Object>();
	  attr.put("type","private");
    attr.put("isSticky",true);
	  client.createConversation(Arrays.asList("Jerry"),"猫和老鼠",attr,
	           new AVIMConversationCreatedCallback(){
	             @Override
	             public void done(AVIMConversation conv,AVIMException e){
	               if(e==null){
	                 //创建成功
	               }
	             }
	           });
	  }
	}
});
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



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  query.whereEqualTo("objectId","551260efe4b01608686c3e0f");
	  query.findInBackground(new AVIMConversationQueryCallback(){
	    @Override
	    public void done(List<AVIMConversation> convs,AVIMException e){
	      if(e==null){
			  if(convs!=null && !convs.isEmpty()){
			    //convs.get(0) 就是想要的conversation
			  }
	      }
	    }
	  });
	  }
	}
});
```


#### 对话列表

用户登录进应用后，获取最近的 10 个对话（包含暂态对话，如聊天室）：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  query.findInBackground(new AVIMConversationQueryCallback(){
	    @Override
	    public void done(List<AVIMConversation> convs,AVIMException e){
	      if(e==null){
          //convs就是获取到的conversation列表
          //注意：按每个对话的最后更新日期（收到最后一条消息的时间）倒序排列
	      }
	    }
	  });	  
	  }
	}
});
```


对话的查询默认返回 10 个结果，若要更改返回结果数量，请设置 `limit` 值。



```
AVIMConversationQuery query = client.getQuery();
query.limit(20);
query.findInBackground(new AVIMConversationQueryCallback(){
	@Override
	public void done(List<AVIMConversation> convs,AVIMException e){
	if(e==null){
    //convs就是获取到的conversation列表
    //注意：按每个对话的最后更新日期（收到最后一条消息的时间）倒序排列
	}
	}
});	
```


#### 条件查询

##### 构建查询

对话的条件查询需要注意的对话属性的存储结构，在对话的属性一章节我们介绍的对话的几个基本属性，这些属性都是 SDK 提供的默认属性，根据默认属性查询的构建如下：



```
// 查询对话名称为「LeanCloud 粉丝群」的对话
conversationQuery.whereEqualTo("name", "LeanCloud 粉丝群");

// 查询对话名称包含 「LeanCloud」 的对话
conversationQuery.whereContains("name", "LeanCloud");

// 查询过去24小时活跃的对话
Calendar yesterday= Calendar.getInstance();
yesterday.add(Calendar.DATE, -1);
conversationQuery.whereGreaterThan("lm", yesterday);
```
针对默认属性的查询可以如上进行构建。


相对于默认属性的查询，开发者自定义属性的查询需要在构建查询的时在关键字（key）前加上一个特殊的前缀：`attr`，不过每个 SDK 都提供相关的快捷方式帮助开发者方便的构建查询：



```
// 查询话题为 DOTA2 对话
conversationQuery.whereEqualTo("attr.topic", "DOTA2");
// 查询等级大于 5 的对话
conversationQuery.whereGreaterThan("attr.level", 5);
```

在 Andorid SDK 中，如果在针对自定义查询的时候，不主动加上 `attr` 的前缀，SDK 会自动添加，比如上述的代码中查询话题为 DOTA2 的对话如下书写效果一致：

```
conversationQuery.whereEqualTo("topic", "DOTA2");
```
特别注意：

> 因为 Android 会自动添加 attr 前缀进行查询构建，所以在设置自定义属性的时候，**禁止**使用以下：`name`,`lm`,`c`,`tr`,`m`,`objectId`等已被默认属性占用的 key 值。



默认属性以及自定义属性的区分便于 SDK 后续的内建属性拓展和维护，自定义属性的开放有利于开发者在可控的范围内进行查询的构建。


条件查询又分为：比较查询、正则匹配查询、包含查询、空值查询，以下会做分类演示。


#### 比较查询

比较查询在一般的理解上都包含以下几种：



比较查询最常用的是等于查询：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  query.whereEqualTo("attr.topic","movie");
	  query.findInBackground(new AVIMConversationQueryCallback(){
	    @Override
	    public void done(List<AVIMConversation> convs,AVIMException e){
	      if(e==null){
			  if(convs!=null && !convs.isEmpty()){
			    //获取符合查询条件的Conversation列表
			  }
	      }
	    }
	  });
	  }
	}
});
```


目前条件查询只针对 `AVIMConversation` 对象的自定义属性进行操作，也就是针对 `_Conversation` 表中的 `attr` 字段进行 `AVQuery` 查询。



下面检索一下类型不是私有的对话：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  query.whereNotEqualTo("attr.type","private");
	  query.setLimit(50);//limit 设为 50 ,默认为 10 个
	  
	  query.findInBackground(new AVIMConversationQueryCallback(){
	    @Override
	    public void done(List<AVIMConversation> convs,AVIMException e){
	      if(e==null){
			  if(convs!=null && !convs.isEmpty()){
			    //获取符合查询条件的Conversation列表
			  }
	      }
	    }
	  });
	  }
	}
});
```


对于可以比较大小的整型、浮点等常用类型，可以参照以下示例代码进行扩展：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  query.whereGreaterThan("attr.age",18);
	  
	  query.findInBackground(new AVIMConversationQueryCallback(){
	    @Override
	    public void done(List<AVIMConversation> convs,AVIMException e){
	      if(e==null){
			  if(convs!=null && !convs.isEmpty()){
			    //获取符合查询条件的Conversation列表
			  }
	      }
	    }
	  });
	  }
	}
});
```


#### 正则匹配查询


匹配查询是指在 `AVIMConversationQuery` 的查询条件中使用正则表达式来匹配数据。


比如要查询所有 language 是中文的对话：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  query.whereMatches("attr.language","[\\u4e00-\\u9fa5]"); //attr.language 是中文字符 
	  
	  query.findInBackground(new AVIMConversationQueryCallback(){
	    @Override
	    public void done(List<AVIMConversation> convs,AVIMException e){
	      if(e==null){
			  if(convs!=null && !convs.isEmpty()){
			    //获取符合查询条件的Conversation列表
			  }
	      }
	    }
	  });
	  }
	}
});
```


#### 包含查询

包含查询是指方法名字包含 `Contains` 单词的方法，例如查询关键字包含「教育」的对话：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  
	  //查询attr.keywords 包含 「教育」的Conversation
	  query.whereContains("attr.keywords","教育"); 
	  
	  query.findInBackground(new AVIMConversationQueryCallback(){
	    @Override
	    public void done(List<AVIMConversation> convs,AVIMException e){
	      if(e==null){
			  if(convs!=null && !convs.isEmpty()){
			    //获取符合查询条件的Conversation列表
			  }
	      }
	    }
	  });
	  }
	}
});
```


另外，包含查询还能检索与成员相关的对话数据。以下代码将帮助 Tom 查找出 Jerry 以及 Bob 都加入的对话：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  
	  //查询对话成员有 Bob 和 Jerry的Conversation
	  query.withMembers(Arrays.as("Bob","Jerry"));
	  
	  query.findInBackground(new AVIMConversationQueryCallback(){
	    @Override
	    public void done(List<AVIMConversation> convs,AVIMException e){
	      if(e==null){
			  if(convs!=null && !convs.isEmpty()){
			    //获取符合查询条件的Conversation列表
			  }
	      }
	    }
	  });
	  }
	}
});
```



#### 空值查询

空值查询是指查询相关列是否为空值的方法，例如要查询 lm 列为空值的对话：

```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

  @Override
  public void done(AVIMClient client,AVIMException e){
    if(e==null){
    //登录成功
    AVIMConversationQuery query = client.getQuery();
    
    //查询 lm 列为空的 Conversation 列表
    query.whereDoesNotExist("lm");
    
    query.findInBackground(new AVIMConversationQueryCallback(){
      @Override
      public void done(List<AVIMConversation> convs,AVIMException e){
        if(e==null){
          if(convs!=null && !convs.isEmpty()){
            //获取符合查询条件的Conversation列表
          }
        }
      }
    });
    }
  }
});

```

如果要查询 lm 列不为空的对话，则替换为如下：

```
query.whereExists("lm");

```


#### 组合查询

组合查询的概念就是把诸多查询条件合并成一个查询，再交给 SDK 去云端进行查询。

例如，要查询年龄小于 18 岁，并且关键字包含「教育」的对话：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  AVIMConversationQuery query = client.getQuery();
	  
	  //查询 attr.keywords 包含 「教育」并且 attr.age 小于 18 的对话
	  query.whereContains("attr.keywords", "教育");
	  query.whereLessThan("attr.age", 18);
	  
	  query.findInBackground(new AVIMConversationQueryCallback(){
	    @Override
	    public void done(List<AVIMConversation> convs,AVIMException e){
	      if(e==null){
			  if(convs!=null && !convs.isEmpty()){
			    //获取符合查询条件的Conversation列表
			  }
	      }
	    }
	  });
	  }
	}
});
```


只要查询构建得合理，开发者完全不需要担心组合查询的性能。


#### 缓存查询

通常，将查询结果缓存到磁盘上是一种行之有效的方法，这样就算设备离线，应用刚刚打开，网络请求尚未完成时，数据也能显示出来。或者为了节省用户流量，在应用打开的第一次查询走网络，之后的查询可优先走本地缓存。

值得注意的是，默认的策略是先走本地缓存的再走网络的，缓存时间是一小时。AVIMConversationQuery 中有如下方法：



```java
  // 设置 AVIMConversationQuery的查询策略
  public void setQueryPolicy(AVQuery.CachePolicy policy);
```



有时你希望先走网络查询，发生网络错误的时候，再从本地查询，可以这样：

```java
    AVIMConversationQuery query = client.getQuery();
    query.setQueryPolicy(AVQuery.CachePolicy.NETWORK_ELSE_CACHE);
    query.findInBackground(new AVIMConversationQueryCallback() {
      @Override
      public void done(List<AVIMConversation> conversations, AVIMException e) {
        
      }
    });
```


各种查询缓存策略的行为可以参考 [存储指南 - AVQuery 缓存查询](leanstorage_guide-android.html#缓存查询) 一节。



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


和建立普通对话类似，建立一个聊天室只是在 `AVIMClient.createConversation(conversationMembers, name, attributes, isTransient, callback)` 中传入 `isTransient=true`。


比如喵星球正在直播选美比赛，主持人 Tom 创建了一个临时对话，与喵粉们进行互动：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){

	@Override
	public void done(AVIMClient client,AVIMException e){
	  if(e==null){
	  //登录成功
	  //创建一个 名为 "HelloKitty PK 加菲猫" 的暂态对话
	  client.createConversation(Collections.emptyList(),"HelloKitty PK 加菲猫",null,true,
	    new AVIMConversationCreatedCallback(){
	      @Override
	      public void done(AVIMConversation conv,AVIMException e){
	        
	      }
	    });
	  }
	}
});

```




### 查询在线人数

 `AVIMConversation.getMemberCount()`  可以用来查询普通对话的成员总数，在聊天室中，它返回的就是实时在线的人数：



```
private void TomQueryWithLimit() {
  AVIMClient tom = AVIMClient.getInstance("Tom");
  tom.open(new AVIMClientCallback() {
    
    @Override
    public void done(AVIMClient client, AVIMException e) {
      if (e == null) {
        //登录成功
        AVIMConversationQuery query = tom.getQuery();
        query.setLimit(1);
        //获取第一个对话
        query.findInBackground(new AVIMConversationQueryCallback() {
          @Override
          public void done(List<AVIMConversation> convs, AVIMException e) {
            if (e == null) {
              if (convs != null && !convs.isEmpty()) {
                AVIMConversation conv = convs.get(0);
                //获取第一个对话的
                conv.getMemberCount(new AVIMConversationMemberCountCallback() {
                  
                  @Override
                  public void done(Integer count, AVIMException e) {
                    if (e == null) {
                      Log.d("Tom & Jerry", "conversation got " + count + " members");
                    }
                  }
                });
              }
            }
          }
        });
      }
    }
  });
}
```


### 查找聊天室

开发者需要注意的是，通过 `AVIMClient.getQuery()` 这样得到的 `AVIMConversationQuery` 实例默认是查询全部对话的，也就是说，如果想查询指定的聊天室，需要额外再调用 以 `where` 开头的 方法来限定更多的查询条件：

比如查询主题包含「奔跑吧，兄弟」的聊天室：



```
  AVIMClient tom = AVIMClient.getInstance("Tom");
  tom.open(new AVIMClientCallback() {

    @Override
    public void done(AVIMClient client, AVIMException e) {
      if (e == null) {
        //登录成功
        //查询 attr.topic 为 "奔跑吧，兄弟" 的暂存聊天室
        AVIMConversationQuery query = client.getQuery();
        query.whereEqualTo("attr.topic", "奔跑吧，兄弟");
        query.whereEqualTo("tr", true);
        //获取第一个对话
        query.findInBackground(new AVIMConversationQueryCallback() {
          @Override
          public void done(List<AVIMConversation> convs, AVIMException e) {
            if (e == null) {
              if (convs != null && !convs.isEmpty()) {
                AVIMConversation conv = convs.get(0);
                //获取第一个对话的
                conv.getMemberCount(new AVIMConversationMemberCountCallback() {
                  @Override
                  public void done(Integer count, AVIMException e) {
                    if (e == null) {
                      Log.d("Tom & Jerry", "conversation got " + count + " members");
                    }
                  }
                });
              }
            }
          }
        });
      }
    }
  });
```




## 聊天记录

聊天记录一直是客户端开发的一个重点，QQ 和 微信的解决方案都是依托客户端做缓存，当收到一条消息时就按照自己的业务逻辑存储在客户端的文件或者是各种客户端数据库中。

我们的 SDK 会将普通的对话消息自动保存在云端，开发者可以通过 AVIMConversation 来获取该对话的所有历史消息。

获取该对话中最近的 N 条（默认 20，最大值 1000）历史消息，通常在第一次进入对话时使用：



```
  AVIMClient tom = AVIMClient.getInstance("Tom");
  tom.open(new AVIMClientCallback() {

    @Override
    public void done(AVIMClient client, AVIMException e) {
      if (e == null) {
        //登录成功
        AVIMConversation conv = client.getConversation("551260efe4b01608686c3e0f");
        int limit = 10;// limit 取值范围 1~1000 之内的整数
        // 不使用 limit 默认返回 20 条消息
        conv.queryMessages(limit, new AVIMMessagesQueryCallback() {
          @Override
          public void done(List<AVIMMessage> messages, AVIMException e) {
            if (e == null) {
              //成功获取最新10条消息记录
            }
          }
        });
      }
    }
  });
```


获取某条消息之前的历史消息，通常用在翻页加载更多历史消息的场景中。


```

  AVIMClient tom = AVIMClient.getInstance("Tom");
  tom.open(new AVIMClientCallback(){

    @Override
    public void done(AVIMClient client,AVIMException e){
      if(e==null){
        //登录成功
        final AVIMConversation conv = client.getConversation("551260efe4b01608686c3e0f");
        conv.queryMessages(new AVIMMessagesQueryCallback(){
          @Override
          public void done(List<AVIMMessage> messages,AVIMException e){
            if(e==null){
              if(messages!=null && !messages.isEmpty()){
                Log.d("Tom & Jerry","got "+messages.size()+" messages ");

                //返回的消息一定是时间增序排列，也就是最早的消息一定是第一个
                AVIMMessage oldestMessage = messages.get(0);

                conv.queryMessages(oldestMessage.getMessageId(), oldestMessage.getTimestamp(),20,
                        new AVIMMessageQueryCallback(){
                          @Override
                          public void done(List<AVIMMessage> msgs,AVIMException e){
                            if(e== null){
                              //查询成功返回
                              Log.d("Tom & Jerry","got "+msgs.size()+" messages ");
                            }
                          }
                        });
              }
            }
          }
        });
      }
    }
  });
```


翻页获取历史消息的时候，LeanCloud 云端是从某条消息开始，往前查找所指定的 N 条消息来返回给客户端。为此，获取历史消息需要传入三个参数：

* 起始消息的 messageId
* 起始消息的发送时间戳
* 需要获取的消息条数

假如每一页为 10 条信息，下面的代码将演示如何翻页：


```
  final int pageSize = 10;
  conversation.queryMessages(pageSize, new AVIMMessagesQueryCallback() {
    @Override
    public void done(List<AVIMMessage> firstPage, AVIMException e) {
      if (firstPage != null && !firstPage.isEmpty()) {
        Log.d("Tom & Jerry", "got " + firstPage.size() + " messages ");

        // 获取第一页的消息里面最旧的一条消息
        AVIMMessage pager = firstPage.get(0);
        conversation.queryMessages(pager.getMessageId(), pager.getTimestamp(), pageSize, new AVIMMessagesQueryCallback() {
          @Override
          public void done(List<AVIMMessage> secondPage, AVIMException e) {
            // secondPage 就是第二页的数据
          }
        });
      }
    }
  });
```



### 客户端聊天记录缓存

为了减少客户端的请求数量，以及减少用户的流量，SDK 实现了一套缓存同步策略。用户在调用获取聊天记录的接口时优先从缓存中获取，SDK 是有算法保证本地与云端聊天记录是同步的。

聊天记录的缓存功能默认为**开启**，但如果开发者出于自身业务逻辑需求，不想在客户端使用缓存功能，可以使用如下接口将其关闭：



```java
AVIMClient.setMessageQueryCacheEnable(false);
```




## 客户端事件

### 网络状态响应

当网络连接出现中断、恢复等状态变化时，可以通过以下接口来处理响应：


与网络相关的通知（网络断开、恢复等）会由 `AVIMClientEventHandler` 做出响应，接口函数有：

* `onConnectionPaused()` 指网络连接断开事件发生，此时聊天服务不可用。
* `onConnectionResume()` 指网络连接恢复正常，此时聊天服务变得可用。
* `onClientOffline()` 指[单点登录](#单点登录)被踢下线的事件。

在网络中断的情况下，所有的消息收发和对话操作都会出现问题。

通过 `AVIMClient.setClientEventHandler()` 可以设定全局的客户端事件响应（ClientEventHandler）。


>注意：网络状态在短时间内很可能会发生频繁变化，但这并不代表对话的接收与发送一定会受到影响，因此开发者在处理此类事件响应时，比如更新 UI，要适应加入更多的逻辑判断，以免影响用户的使用体验。


### 断线重连
目前 Android SDK 默认内置了断线重连的功能，从客户端与云端建立连接成功开始，只要没有调用退出登录的接口，SDK 会一直尝试和云端保持长连接，此时 AVIMClient 的状态可以通过 [网络状态响应](#网络状态响应)接口得到。

**注意：用户如果自行实现了重连逻辑可能会报出 1001 错误**。


### 退出登录

要退出当前的登录状态或要切换账户，方法如下：



```
AVIMClient tom = AVIMClient.getInstance("Tom");
tom.open(new AVIMClientCallback(){
  
  @Override
  public void done(AVIMClient client,AVIMException e){
  	if(e==null){
  	  //登录成功
  	  client.close(new AVIMClientCallback(){
  	  	@Override
  	  	public void done(AVIMClient client,AVIMException e){
  	  		if(e==null){
  	  		//登出成功
  	  		}
  	  	}
  	  });
  	}
  }
});
```




## 安全与签名

在继续阅读下文之前，请确保你已经对 [实时通信服务开发指南 &middot; 权限和认证](realtime_v2.html#权限和认证) 有了充分的了解。

### 实现签名工厂

为了满足开发者对权限和认证的要求，我们设计了操作签名的机制。签名启用后，所有的用户登录、对话创建/加入、邀请成员、踢出成员等登录都需要验证签名，这样开发者就对消息具有了完全的掌控。


我们强烈推荐启用签名，具体步骤是 [控制台 > 设置 > 应用选项](/app.html?appid={{appid}}#/permission)，勾选 **聊天、推送** 下的 **聊天服务，启用签名认证**。



客户端这边究竟该如何使用呢？我们只需要实现 SignatureFactory 接口，然后在用户登录之前，把这个接口的实例赋值给 AVIMClient 即可（`AVIMClient.setSignatureFactory(factory)`）。

设定了 signatureFactory 之后，对于需要鉴权的操作，实时通信 SDK 与服务器端通讯的时候都会带上应用自己生成的 Signature 信息，LeanCloud 云端会使用 app 的 masterKey 来验证信息的有效性，保证聊天渠道的安全。

对于 SignatureFactory 接口，我们只需要实现这两个函数即可：

```
  /**
   * 实现一个基础签名方法 其中的签名算法会在SessionManager和AVIMClient(V2)中被使用
   */
  public Signature createSignature(String peerId, List<String> watchIds) throws SignatureException;

  /**
   * 实现AVIMConversation相关的签名计算
   * 
   * @param conversationId
   * @param clientId
   * @param targetIds - 此次操作的member的clientIds
   * @param action - 此次行为的动作，行为分别对应常量 invite（加群和邀请）和 kick（踢出群）
   * @return
   * @throws SignatureException 如果签名计算中间发生任何问题请抛出本异常
   */  /**
   * 实现AVIMConversation相关的签名计算
   * @param action - 此次行为的动作，行为分别对应常量 invite（加群和邀请）和 kick（踢出群）
   */
  public Signature createConversationSignature(String conversationId, String clientId,
      List<String> targetIds, String action) throws SignatureException;
```

`createSignature` 函数会在用户登录、对话创建的时候被调用，`createConversationSignature` 会在对话加入成员、邀请成员、踢出成员等操作时被调用。

你需要做的就是按照前文所述的签名算法实现签名，其中 `Signature` 声明如下：

```
public class Signature {
  public List<String> getSignedPeerIds();
  public void setSignedPeerIds(List<String> signedPeerIds);

  public String getSignature();
  public void setSignature(String signature);

  public long getTimestamp();
  public void setTimestamp(long timestamp);

  public String getNonce();
  public void setNonce(String nonce);
}
```

其中四个属性分别是:

* signature 签名
* timestamp 时间戳，单位秒
* nonce 随机字符串 nonce
* signedPeerIds 放行的 clientId 列表，v2 中已经**废弃不用**

下面的代码展示了基于 LeanCloud 云引擎进行签名时，客户端的实现片段，你可以参考它来完成自己的逻辑实现：

```
public class KeepAliveSignatureFactory implements SignatureFactory {
 @Override
 public Signature createSignature(String peerId, List<String> watchIds) {
   Map<String,Object> params = new HashMap<String,Object>();
   params.put("self_id",peerId);
   params.put("watch_ids",watchIds);

   try{
     Object result =  AVCloud.callFunction("sign",params);
     if(result instanceof Map){
       Map<String,Object> serverSignature = (Map<String,Object>) result;
       Signature signature = new Signature();
       signature.setSignature((String)serverSignature.get("signature"));
       signature.setTimestamp((Long)serverSignature.get("timestamp"));
       signature.setNonce((String)serverSignature.get("nonce"));
       return signature;
     }
   }catch(AVException e){
     throw (SignatureFactory.SignatureException) e;
   }
   return null;
 }

  @Override
  public Signature createConversationSignature(String convId, String peerId, List<String> targetPeerIds,String action){
   Map<String,Object> params = new HashMap<String,Object>();
   params.put("client_id",peerId);
   params.put("conv_id",convId);
   params.put("members",targetPeerIds);
   params.put("action",action);

   try{
     Object result = AVCloud.callFunction("sign2",params);
     if(result instanceof Map){
        Map<String,Object> serverSignature = (Map<String,Object>) result;
        Signature signature = new Signature();
        signature.setSignature((String)serverSignature.get("signature"));
        signature.setTimestamp((Long)serverSignature.get("timestamp"));
        signature.setNonce((String)serverSignature.get("nonce"));
        return signature;
     }
   }catch(AVException e){
     throw (SignatureFactory.SignatureException) e;
   }
   return null;
  }
}
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



```java
    // 第二个参数：登录标记 Tag
    AVIMClient currentClient = AVIMClient.getInstance(clientId,"Mobile");
    currentClient.open(new AVIMClientCallback() {
      @Override
      public void done(AVIMClient avimClient, AVIMException e) {
        if(e == null){
          // 与云端建立连接成功
        }
      }
    });
```



上述代码可以理解为 LeanCloud 版 QQ 的登录，而另一个带有同样 Tag 的客户端打开连接，则较早前登录系统的客户端会被强制下线。

#### 处理登录冲突

我们可以看到上述代码中，登录的 Tag 是 `Mobile`。当存在与其相同的 Tag 登录的客户端，较早前登录的设备会被云端强行下线，而且他会收到被云端下线的通知：



```java
public class MyApplication extends Application{
  public void onCreate(){
   ...
   AVOSCloud.initialize(this,"{{appid}}","{{appkey}}");
   // 自定义实现的 AVIMClientEventHandler 需要注册到 SDK 后，SDK 才会通过回调 onClientOffline 来通知开发者
   AVIMClient.setClientEventHandler(new AVImClientManager());
   ...
  }
}

public class AVImClientManager extends AVIMClientEventHandler {
  ...
  @Override
  public void onClientOffline(AVIMClient avimClient, int i) {
    if(i == 4111){
      // 适当地弹出友好提示，告知当前用户的 Client Id 在其他设备上登陆了
    }
  }
  ...
}
```



如上述代码中，被动下线的时候，云端会告知原因，因此客户端在做展现的时候也可以做出类似于 QQ 一样友好的通知。




### 自动登录

如果开发者希望控制 App 重新启动后是否由 SDK 自动登录实时通讯，这可通过如下接口实现：

```
AVIMClient.setAutoOpen(false);
```

如果为 true，SDK 会在 App 重新启动后进行实时通讯的自动重连，如果为 false，则 App 重新启动后不会做自动重连操作，默认值为 true。

注意：此设置并不影响在 App 生命周期内因网络获取等问题造成的重连。


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


