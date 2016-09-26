# Android 消息推送开发指南

请先阅读 [消息推送概览](push_guide.html) 了解相关概念。

Android 推送功能除了需要必须的 `avoscloud.jar` 以外，还需要额外的 `avospush.jar`。

Android 消息推送有专门的 Demo，请见 [Android-Push-Demo](https://github.com/leancloud/android-push-demo) 项目。

## Installation

当应用在用户设备上安装好以后，如果要使用消息推送功能，LeanCloud SDK 会自动生成一个 Installation 对象。该对象本质上是应用在设备上生成的安装信息，也包含了推送所需要的所有数据，因此要使用它来进行消息推送。

### 保存 Installation

你可以通过以下代码保存你的 Installation id。如果你的系统之前还没有 Installation id，系统会为你自动生成一个。当你的应用卸载后，Installation id 也将会被删除。


```java
AVInstallation.getCurrentInstallation().saveInBackground();
```

**这段代码应该在应用启动的时候调用一次**，保证设备注册到 LeanCloud 云端。你可以监听调用回调，获取 installationId 做数据关联。

```java
AVInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
    public void done(AVException e) {
        if (e == null) {
            // 保存成功
            String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
            // 关联  installationId 到用户表等操作……
        } else {
            // 保存失败，输出错误信息
        }
    }
});
```

## 启动推送服务

通过调用以下代码启动推送服务，同时设置默认打开的 Activity。

```java
// 设置默认打开的 Activity
PushService.setDefaultPushCallback(this, PushDemo.class);
```

## 订阅频道

你的应用可以订阅某个频道（channel）的消息，只要在保存 Installation 之前调用 `PushService.subscribe` 方法：

```java
// 订阅频道，当该频道消息到来的时候，打开对应的 Activity
// 参数依次为：当前的 context、频道名称、回调对象的类
PushService.subscribe(this, "public", PushDemo.class);
PushService.subscribe(this, "private", Callback1.class);
PushService.subscribe(this, "protected", Callback2.class);
```

注意：

- 频道名称：**每个 channel 名称只能包含 26 个英文字母和数字。**
- 回调对象：指用户点击通知栏的通知进入的 Activity 页面。

退订频道也很简单：

```java
PushService.unsubscribe(context, "protected");
//退订之后需要重新保存 Installation
AVInstallation.getCurrentInstallation().saveInBackground();
```


## 推送消息

### 配置

请确保你的 `AndroidManifest.xml` 的 `<application>` 中包含如下内容：

```xml
<service android:name="com.avos.avoscloud.PushService" />
```

同时设置了必要的权限：

```xml
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

为了让应用能在关闭的情况下也可以收到推送，你需要在 `<application>` 中加入：

```xml
<receiver android:name="com.avos.avoscloud.AVBroadcastReceiver">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.USER_PRESENT" />
        <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
    </intent-filter>
</receiver>
```

### 推送给所有的设备

```java
AVPush push = new AVPush();
JSONObject object = new JSONObject();
object.put("alert", "push message to android device directly");
push.setPushToAndroid(true);
push.setData(object);
push.sendInBackground(new SendCallback() {
    @Override
    public void done(AVException e) {
        if (e == null) {
            // push successfully.
        } else {
            // something wrong.
        }
    });
```

### 发送给特定的用户

发送给「public」频道的用户：

```java
AVQuery pushQuery = AVInstallation.getQuery();
pushQuery.whereEqualTo("channels", "public");
AVPush push = new AVPush();
push.setQuery(pushQuery);
push.setMessage("Push to channel.");
push.setPushToAndroid(true);
push.sendInBackground(new SendCallback() {
    @Override
    public void done(AVException e) {
        if (e == null) {

        }   else {

        }
    }
});
```

发送给某个 Installation id 的用户，通常来说，你会将 AVInstallation 关联到设备的登录用户 AVUser 上作为一个属性，然后就可以通过下列代码查询 InstallationId 的方式来发送消息给特定用户，实现类似私信的功能：

```java
AVQuery pushQuery = AVInstallation.getQuery();
// 假设 THE_INSTALLATION_ID 是保存在用户表里的 installationId，
// 可以在应用启动的时候获取并保存到用户表
pushQuery.whereEqualTo("installationId", THE_INSTALLATION_ID);
AVPush.sendMessageInBackground("message to installation",  pushQuery, new SendCallback() {
    @Override
    public void done(AVException e) {

    }
});
```

在 2.6.7 以后，我们加入了通过 CQL 来筛选推送目标的功能，主要代码如下：

```java
    AVPush push = new AVPush();
    JSONObject data =
        new JSONObject(
            "{\"action\": \"com.avos.UPDATE_STATUS\", \"name\": \"Vaughn\", \"newsItem\": \"Man bites dog\"  }");
    push.setData(data);
    String installationId = AVInstallation.getCurrentInstallation().getInstallationId();
    push.setCloudQuery("select * from _Installation where installationId ='" + installationId
        + "'");
    push.sendInBackground(new SendCallback() {

      @Override
      public void done(AVException e) {

      }
    });
```

<div class="callout callout-info">CQL 与 AVQuery 同时只能设置一个，并且 `setPushTarget` 类函数（`setPushToAndroid` / `setPushToIOS` / `setPushToWindowsPhone`）只能与 AVQuery 一起使用。在设置 CQL 时，只能在 CQL 语句中设定目标机器的类型。</div>

### 自定义 Receiver

如果你想推送消息，但不显示在 Android 系统的通知栏中，而是执行应用程序预定义的逻辑，你需要在你的 Android 项目中的 `AndroidManifest.xml` 中声明你的 Receiver：

```xml
<receiver android:name="com.avos.avoscloud.PushDemo.MyCustomReceiver" android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.USER_PRESENT" />
        <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
        <action android:name="com.avos.UPDATE_STATUS" />
    </intent-filter>
</receiver>
```

其中 `com.avos.avoscloud.PushDemo.MyCustomReceiver` 是你的 Android 的 Receiver 类。而 `<action android:name="com.avos.UPDATE_STATUS" />` 需要与 push 的 data 中指定的 action 相对应。

你的 Receiver 可以按照如下方式实现：

```java
public class MyCustomReceiver extends BroadcastReceiver {
    private static final String TAG = "MyCustomReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.log.d(TAG, "Get Broadcat");
        try {
            String action = intent.getAction();
            String channel = intent.getExtras().getString("com.avos.avoscloud.Channel");
            //获取消息内容
            JSONObject json = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));

            Log.d(TAG, "got action " + action + " on channel " + channel + " with:");
            Iterator itr = json.keys();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                Log.d(TAG, "..." + key + " => " + json.getString(key));
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.getMessage());
        }
    }
}
```

同时，要求发送推送的请求也做相应更改，例如：

```sh
curl -X POST \
  -H "X-LC-Id: {{appid}}"          \
  -H "X-LC-Key: {{appkey}}"        \
  -H "Content-Type: application/json" \
  -d '{
        "channels":[ "public"],
        "data": {
          "action": "com.avos.UPDATE_STATUS",
          "name": "LeanCloud."
        }
      }' \
  https://leancloud.cn/1.1/push
```

<div class="callout callout-info">如果你使用自定义的 Receiver，发送的消息必须带 action，并且其值在自定义的 Receiver 配置的 `<intent-filter>` 列表里存在，比如这里的 `'com.avos.UPDATE_STATUS'`，请使用自己的 action，尽量不要跟其他应用混淆，推荐采用域名来定义。</div>

### 跟踪 Android 推送和应用的打开情况

你可以在订阅频道对应的 Activity 中添加跟踪应用打开情况的统计代码，你的 Activity 可以按照如下方式实现 `onStart` 方法：

```java
public class MyActivity extends Activity {
	@Override
	protected void onStart() {
		super.onStart();

		Intent intent = getIntent();
		AVAnalytics.trackAppOpened(intent);
	}
}
```

如果要区分应用是由「推送」打开的这种情况，则需要为从推送跳转到 MyActivity 时要使用的 **intent** 增加一个 `PUSH_INTENT_KEY` 参数：

```java
intent.putExtra(AVConstants.PUSH_INTENT_KEY, 1);
```


然后当 MyActiviy 里 `getIntent()` 拿到这个 **intent** 后，执行 `AVAnalytics.trackAppOpened(intent);` 时发现 `PUSH_INTENT_KEY` 存在且为 1，则认定其来自推送。该统计可以通过 [请求分析 > Push 打开](/apistat.html?appid={{appid}}#/_appOpenWithPush) 查看。



## 混合推送

### 小米推送

#### 环境配置

1. **注册小米账号**：在 [小米开放平台][xiaomi] 上注册小米开发者账号并完成实名认证（[详细流程](http://dev.xiaomi.com/doc/?p=90)）。
2. **创建小米推送服务应用**（[详细流程](http://dev.xiaomi.com/doc/?p=1621)）。
3. **设置小米的 AppId 及 AppSecret**：在 [小米开放平台][xiaomi] > **管理控制台** > **消息推送** > **相关应用** 可以查到具体的小米推送服务应用的 AppId 及 AppSecret。将此 AppId 及 AppSecret 通过 [LeanCloud 控制台][leancloud-console] > **消息** > **推送** > **设置** > **混合推送**，与 LeanCloud 应用关联。

#### 接入 SDK

首先导入 `avoscloud-mixpush` 包。修改 `build.gradle` 文件，在 **dependencies** 中添加依赖：

```
dependencies {
    compile ('cn.leancloud.android:avoscloud-mixpush:v3.+@aar')
}
```

注：如果是通过 jar 包导入，则需要手动下载 jar 包 [小米 Push SDK](http://dev.xiaomi.com/mipush/downpage/)。

然后配置相关 AndroidManifest。添加 Permission：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.GET_TASKS" />
<uses-permission android:name="android.permission.VIBRATE"/>
<permission android:name="<包名>.permission.MIPUSH_RECEIVE" android:protectionLevel="signature" />
<uses-permission android:name="<包名>.permission.MIPUSH_RECEIVE" />
```

添加 service 与 receiver。开发者要将其中的 `<包名>` 替换为自己的应用对应的 package：

```xml
<service
  android:name="com.xiaomi.push.service.XMPushService"
  android:enabled="true"
  android:process=":pushservice"/>

<service
  android:name="com.xiaomi.push.service.XMJobService"
  android:enabled="true"
  android:exported="false"
  android:permission="android.permission.BIND_JOB_SERVICE"
  android:process=":pushservice" />

<service
  android:name="com.xiaomi.mipush.sdk.PushMessageHandler"
  android:enabled="true"
  android:exported="true"/>
  
<service
  android:name="com.xiaomi.mipush.sdk.MessageHandleService"
  android:enabled="true"/>
  
<receiver
  android:name="com.xiaomi.push.service.receivers.NetworkStatusReceiver"
  android:exported="true">
  <intent-filter>
      <action android:name="android.net.conn.CONNECTIVITY_CHANGE"/>
      <category android:name="android.intent.category.DEFAULT"/>
  </intent-filter>
</receiver>

<receiver
  android:name="com.xiaomi.push.service.receivers.PingReceiver"
  android:exported="false"
  android:process=":pushservice">
  <intent-filter>
      <action android:name="com.xiaomi.push.PING_TIMER"/>
  </intent-filter>
</receiver>

<receiver
  android:name="com.avos.avoscloud.AVMiPushMessageReceiver"
  android:exported="true">
  <intent-filter>
      <action android:name="com.xiaomi.mipush.RECEIVE_MESSAGE"/>
  </intent-filter>
  <intent-filter>
      <action android:name="com.xiaomi.mipush.MESSAGE_ARRIVED"/>
  </intent-filter>
  <intent-filter>
      <action android:name="com.xiaomi.mipush.ERROR"/>
  </intent-filter>
</receiver>
```

#### 具体使用

在 `AVOSCloud.initialize` 时调用 `AVMixpushManager.registerXiaomiPush(context, miAppId, miAppKey, profile)` 即可。参数 `profile` 的用法可以参考 [Android 混合推送多配置区分](push_guide.html#Android_混合推送多配置区分)。

注意，LeanCloud 云端只有在以下三个条件都满足的情况下，才会使用小米推送。

- MIUI 系统
- manifest 正确填写
- appId、appKey 有效

如果以上条件不符合，SDK 会有日志输出，开发者可以根据日志判断是什么原因导致注册失败。开发者可以通过查看控制台 `_Installation` 表的相关记录的 **vendor** 字段来判断是否注册成功。

#### 小米推送通知栏消息的点击事件

当小米通知栏消息被点击后，如果已经设置了 [自定义 Receiver](#自定义_Receiver)，则 SDK 会发送一个 action 为 `com.avos.avoscloud.mi_notification_action` 的 broadcast。如有需要，开发者可以通过订阅此消息获取点击事件，否则 SDK 会默认打开 [启动推送服务](#启动推送服务) 对应设置的 Activity。


### 华为推送

#### 环境配置

1. **注册华为账号**：在 [华为开发者联盟](http://developer.huawei.com/cn/consumer/)注册华为开发者账号（[详细流程](http://developer.huawei.com/cn/consumer/wiki/index.php?title=%E6%B3%A8%E5%86%8C%E7%99%BB%E5%BD%95)）。
2. **创建华为应用**：实名认证通过后，需要创建华为移动应用并配置 Push 权益（[详细流程](http://developer.huawei.com/cn/consumer/wiki/index.php?title=%E6%8E%A5%E5%85%A5%E8%AF%B4%E6%98%8E#2.1_.E6.B3.A8.E5.86.8C)）。
3. **设置华为的 AppId 及 AppKey**：在 [华为开发者联盟控制中心](http://developer.huawei.com/cn/consumer/devunion/openPlatform/html/memberCenter.html#appManage#) > **应用管理** > **移动应用详情**  可以查到具体的华为推送服务应用的 AppId 及  AppSecret，将此 AppId 及 AppSecret 通过 [LeanCloud 控制台][leancloud-console] > **消息** > **推送** > **设置** > **混合推送** 与 LeanCloud 应用关联。

#### 接入 SDK

首先导入 `avoscloud-mixpush` 包，修改 `build.gradle` 文件，在 `dependencies` 中添加依赖：

```
dependencies {
    compile ('cn.leancloud.android:avoscloud-mixpush:v3.+@aar')
}
```

注：如果是通过 jar 包导入，则需要手动下载 jar 包：[华为 Push SDK](http://developer.huawei.com/cn/consumer/wiki/index.php?title=PushSDK%E4%B8%8B%E8%BD%BD)。

然后配置相关 AndroidManifest，添加 Permission：

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

再添加 service 与 receiver。开发者要将其中的 `<包名>` 替换为自己的应用的 package：

```xml
<!-- 必须，用于华为 Android 6.0 系统的动态权限页面-->
<activity android:name="com.huawei.android.pushselfshow.permission.RequestPermissionsActivity"/>

<receiver android:name="com.avos.avoscloud.AVHwPushMessageReceiver" >
  <intent-filter>
      <!-- 必须，用于接收 token -->
      <action android:name="com.huawei.android.push.intent.REGISTRATION" />
      <!-- 必须，用于接收消息 -->
      <action android:name="com.huawei.android.push.intent.RECEIVE" />
      <!-- 可选，用于点击通知栏或通知栏上的按钮后触发 onEvent 回调 -->
      <action android:name="com.huawei.android.push.intent.CLICK" />
      <!-- 可选，查看 push 通道是否连接，不查看则不需要 -->
      <action android:name="com.huawei.intent.action.PUSH_STATE" />
  </intent-filter>
</receiver>

<receiver
  android:name="com.huawei.android.pushagent.PushEventReceiver"
  android:process=":pushservice" >
  <intent-filter>
      <action android:name="com.huawei.android.push.intent.REFRESH_PUSH_CHANNEL" />
      <action android:name="com.huawei.intent.action.PUSH" />
      <action android:name="com.huawei.intent.action.PUSH_ON" />
      <action android:name="com.huawei.android.push.PLUGIN" />
  </intent-filter>
  <intent-filter>
      <action android:name="android.intent.action.PACKAGE_ADDED" />
      <action android:name="android.intent.action.PACKAGE_REMOVED" />
      <data android:scheme="<包名>" />
  </intent-filter>
</receiver>
<receiver
  android:name="com.huawei.android.pushagent.PushBootReceiver"
  android:process=":pushservice" >
  <intent-filter>
      <action android:name="com.huawei.android.push.intent.REGISTER" />
      <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
  </intent-filter>
  <meta-data
      android:name="CS_cloud_version"
      android:value="\u0032\u0037\u0030\u0035" />
</receiver>

<service
  android:name="com.huawei.android.pushagent.PushService"
  android:process=":pushservice" >
</service>
```

#### 具体使用

在 `AVOSCloud.initialize` 时调用 `registerHuaweiPush(context, profile)` 即可。参数 `profile` 的用法可以参考 [Android 混合推送多配置区分](push_guide.html#Android_混合推送多配置区分)。

注意，LeanCloud 云端只有在以下两个条件都满足的情况下，才会使用华为推送。

- EMUI 系统
- manifest 正确填写

如果以上条件不符合，SDK 会有日志输出，开发者可以根据日志判断是什么原因导致注册失败。开发者可以通过查看控制台 `_Installation` 表的相关记录的 **vendor** 字段来判断是否注册成功。

### GCM 推送

GCM（Google Cloud Messaging）是 Google 提供的一项将推送通知消息发送到手机的服务。接入时后台不需要任何设置，GCM 相关的 token 由 LeanCloud SDK 来申请。

#### 环境要求

GCM 需要系统为 Android 2.2 及以上并且安装有 Google Play 商店的设备，或者使用了 GppgleAPIs 且系统为 Android 2.2 及以上的模拟器。具体要求详见 [Google Developers &middot; Set up a GCM Client App on Android](https://developers.google.com/cloud-messaging/android/client)。

#### 接入 SDK

首先补充 `AndroidManifest`，添加 Permission，开发者要将其中的 `<包名>` 替换为自己的应用对应的 package：

```xml
<permission android:name="<包名>.permission.C2D_MESSAGE"
                    android:protectionLevel="signature" />
<uses-permission android:name="<包名>.permission.C2D_MESSAGE" />
```

然后添加 service 与 receiver：

```xml
<receiver android:name="com.avos.avoscloud.AVBroadcastReceiver">
  <intent-filter>
      <action android:name="android.intent.action.BOOT_COMPLETED"/>
      <action android:name="android.intent.action.USER_PRESENT"/>
      <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
  </intent-filter>
</receiver>
<service android:name="com.avos.avoscloud.PushService" />
<service android:name="com.avos.avoscloud.AVGCMService">
  <intent-filter>
      <action android:name="com.google.android.c2dm.intent.RECEIVE" />
  </intent-filter>
</service>
<receiver
  android:name="com.google.android.gms.gcm.GcmReceiver"
  android:exported="true"
  android:permission="com.google.android.c2dm.permission.SEND" >
  <intent-filter>
      <action android:name="com.google.android.c2dm.intent.RECEIVE" />
      <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
      <category android:name="<包名>" />
  </intent-filter>
</receiver>
```

接下来设置 GCM 开关。在 `AVOSCloud.initialize` 初始化时设置开关 `AVOSCloud.setGcmOpen(true)`。

注意，LeanCloud 云端只有在以下三个条件都满足的情况下，才会默认走 GCM 通道。

- LeanCloud 美国节点
- 调用 `AVOSCloud.setGcmOpen(true)`
- manifest 正确填写

开发者可以通过查看控制台 `_Installation` 表的相关记录的 **vendor** 字段来判断是否注册成功。

[xiaomi]: http://dev.xiaomi.com/index
[leancloud-console]: https://leancloud.cn/apps.html
