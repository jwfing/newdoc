# Android 统计 SDK 开发指南

## 安装与初始化


首先在 [控制台](/applist.html#/apps) 上创建新的应用，然后 [下载 LeanCloud Android SDK](sdk_down.html) 以及相应的 Demo。


### 导入 SDK

除了必须的 avoscloud.jar 外，你还需要额外的导入 avosstatistics.jar。
请将下载的 jar 包放到 libs 目录下，以便你的 IDE（Eclipse 或者 Intellij IDEA 等)可以正常识别导入的 jar 包。如以下图片所示：

![image](images/android_statistics_ide.png)

### 配置 `AndroidManifest.xml` 文件

请务必确保你的应用拥有如下权限：

* `android.permission.INTERNET`<br/>
  向 LeanCloud 的统计服务器发送用户分析数据。
* `android.permission.READ_PHONE_STATE`<br/>
  `android.permission.ACCESS_WIFI_STATE`<br/>
  这两个权限是为了获取用户手机的 IMEI 以及 WiFi 的 Mac 地址，用来唯一的标识用户。
* `android.permission.ACCESS_NETWORK_STATE`<br/>
  检测网络状态。
* `android.permission.READ_LOGS`<br/>
  获取客户端 crash log。通过将 crash log 汇报到服务器上，你可以了解你的应用 crash 的原因以及次数。
* `android.permission.WRITE_EXTERNAL_STORAGE`<br/>
  保存离线报告的缓存数据。

示例：

```xml
<manifest ...>
    <application ...>
        ...
        <activity .../>
    </application>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.READ_LOGS" />
</manifest>
```

如果你想指定你的发布渠道，请在 AndroidManifest.xml 中加入如下内容：

```xml
<application  ...>
    ...
    <meta-data android:name="Channel ID" android:value="LeanCloud"/>
</application>
```

你可以根据你的实际发布渠道，修改上述的 android:value 中对应的值，比如将 `LeanCloud` 改为 `Your Channel`，重新打包后发布。**请不要修改 `android:name="Channel ID"` 字段，以免影响使用。**

由于很多用户反映在部分第三方发布平台中间，不允许出现 meta-data 中间的 key 出现空格字符，我们在 2.6.8 以后，增加了一个等效的 key：`leancloud`。
以下代码也可以用于指定渠道了，但是请不要反复定义。

```xml
<application  ...>
    ...
    <meta-data android:name="leancloud" android:value="LeanCloud"/>
</application>
```

当然你也可以通过代码来指定发布渠道。但是代码设置的渠道优先级没有 `AndroidManifest.xml` 中的配置高，同时出现时优先取 `AndroidManifest.xml` 中的配置。

```java

AVAnalytics.setAppChannel("SomeChannel");
// 参数依次是 context, appId, appKey
AVOSCloud.initialize(getContext(),"{{appid}}","");

```

## 添加使用代码

### 添加引用

经过我们的一系列更新升级，使用最新的 SDK 你不需要任何代码上的操作就可以使用统计的基本功能，统计功能为默认打开。


```java
import com.avos.avoscloud.*;

public class YourApp extends Application{

    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "{{appid}}", "{{appkey}}");
        //AVAnalytics.start(this);    已经不再需要这行代码了
        AVAnalytics.enableCrashReport(this, true);

    }
}
```

## 统计页面路径

在需要的 Activity 中调用统计 SDK。在每个 Activity 的 `onResume` 和 `onPause` 方法中调用相应的统计方法，传入的参数为当前 context（比如当前的 Activity）的引用。 这里请不要将全局的 Application Context 传入。如示例所示：

```java
protected void onPause() {
    super.onPause();
    AVAnalytics.onPause(this);
}

protected void onResume() {
    super.onResume();
    AVAnalytics.onResume(this);
}
```


请确保在所需要的所有的 Activity 中都调用 `AVAnalytics.onResume()` 和 `AVAnalytics.onPause()` 方法，这两个调用将不会阻塞应用程序的主线程，也不会影响应用程序的性能。

注意：如果你的 Activity 之间有继承或者控制关系请不要同时在父和子 Activity 中重复添加 onPause 和 onResume 方法，否则会造成重复统计，比如在使用 TabHost、TabActivity、ActivityGroup 时。一个应用程序在多个 Activity 之间连续切换时，将会被视为同一个 session（会话或者一次使用过程）。

当用户两次使用之间间隔超过 30 秒时，将被认为是两个的独立的 session，例如用户回到 home，或进入其他程序，经过一段时间后再返回之前的应用。我们也提供了新的接口来自定义这个时间间隔，你只要调用：

```java
AVAnalytics.setSessionContinueMillis(long mills);
```

传入适当的参数，就可以控制 session 重新启动时间，注意参数是以毫秒为单位的。 例如，如果你认为在 60 秒之内返回应用可视为同一次启动，超过 60 秒返回当前应用可视为一次新的启动，那么请写成：

```java
AVAnalytics.setSessionContinueMillis(60 * 1000);
```

## 统计 Fragment 页面

Android 3.0 引入了 Fragment。使用 Fragment，你可以在一个 Activity 中展示多个用户界面，也可根据你的需要，为不同的设备适配界面。LeanCloud SDK 1.4.2 开始增加了对于 Fragment 统计的支持。你可以使用以下代码统计 Fragment 页面：

```java
public class MyListFragment extends ListFragment {
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onStart() {
        super.onStart();
    }

    public void onPause() {
        super.onPause();
        AVAnalytics.onFragmentEnd("my-list-fragment");
    }

    public void onResume() {
        super.onResume();
        AVAnalytics.onFragmentStart("my-list-fragment");
    }
}
```


## 测试

* 确认所需的权限都已经添加：
  * `INTERNET`
  * `READ_PHONE_STATE`
  * `READ_LOGS`
  * `WRITE_EXTERNAL_STORAGE`
* 确认所有的 Activity 中都调用了 onResume 和 onPause 方法。
* 确认测试手机（或者模拟器）已成功连入网络。

* 启动应用程序，几分钟之内你应该已经可以在 [控制台 / 分析](/stat.html?appid={{appid}}#/statrealtime) 中的相应菜单中看到报表了。


## 数据时效性

在控制台的 **分析** 页面中，有些报告可以展示实时数据，有些报告则依靠内部离线数据进行分析，因此有时你会看不到当天的数据。

如果当前页面中存在 **日期选择** 选项（通常在页面右上角），你可以以此判断当前的统计结果是否有延迟。如果 **结束日期** 显示为<u>当天日期</u>或在其下拉菜单中有「今日」的选项，则为实时数据。如下图所示，菜单中无「今日」，所以可以判断这是离线数据，统计结果要延迟一天。

<img src="images/analytics_datepicker_for_offline_data.png" alt="" width="231" height="256">

## 使用自定义事件

### 基本简单事件

除了基本统计分析功能外，SDK 还支持你自定义的事件分析，例如你可以统计你的应用中有多少人点击了 like 按键，某个文章的点击次数或者视频被播放的次数等等。

在你希望发送事件报告的代码段，调用如下方法就可以向服务器发送事件记录

```java
AVAnalytics.onEvent(Context context, String eventName);
```

统计 eventName 对应事件的发生次数、变化趋势，例如 like 点击、浏览数量等等。参数 context 为当前 context 的引用。eventName 为当前统计的事件 name。【注意】eventName 中不要加空格或其他的转义字符。

比如，应用中的一条微视频被转发的事件被定义为「Forward」，那么在点击转发的函数里调用：

```java
AVAnalytics.onEvent(this, "Forward")
```

就会向服务器汇报一个转发的事件。


### 多标签事件

```java
AVAnalytics.onEvent(Context context, String eventName, String  tag);
```

除了能够统计 eventName 所对应事件的发生次数，变化趋势外，还能统计事件中具体标签所占的比例。tag 为当前标签，同样这里的 eventName 字符串中也请不要使用空格。
比如，在玩拍程序中，我们定义了一个发布微视频的多标签事件 Publish，对应的发布内容有 title（发布标题）、Video（发视频）、type（视频类型）来对应不同的发布类型，这样我们不仅可以记录 Publish 事件的点击数量还可以看到不同内容对应的比例。


### 事件累计

在应用程序中某些自定义事件可能会被频繁触发，例如用户点击某个按钮。开发者可以在程序中维护一个计数器，这样某个事件被多次触发但只需要生成一个消息，这个消息中包括该事件被触发的次数。为了支持这个功能，我们提供了重载的接口：

```java
AVAnalytics.onEvent(Context context, String eventName, int count);
AVAnalytics.onEvent(Context context, String eventName, String label, int count)
```

参数 count 是对应事件（和对应标签）被触发的次数。

## 设置数据发送策略

你可以进入应用的 [**分析** > **统计设置**<span class="text-muted">（左下角）</span> > **数据发送策略**](/stat.html?appid={{appid}}&os=android#/statconfig/trans_strategoy) 在线更改 SDK 端的数据报告发送策略。在没有取到在线配置的发送策略的情况下，会使用默认的发送策略。


以下均为在线配置中的可选策略。

### 启动时发送
【推荐使用】应用程序每次会在启动时会向服务器发送一次消息，在应用程序过程中产生的所有消息（包括自定义事件和本次使用时长）都会在下次启动时候发送。如果应用程序启动时处在不联网状态，那么消息将会缓存在本地，下次再尝试发送。

发送策略默认为启动时发送。

### 批量发送

批量发送，默认当消息数量达到 30 条时发送一次。

### 按最小间隔发送

间隔一段时间发送，每隔一段时间一次性发送到服务器。

## 自定义参数设置

你可以控制台某个应用的 **组件** > **自定义参数** 设置中配置你的自定义在线参数。这些参数会在我们每次更新统计配置时进行更新，你可以用以下方法来获得对应的值：

```java
AVAnalytics.getConfigParams(this.getContext(), "key")
```

但是请注意三点：

* key 必须跟你在控制台配置的参数一致，大小写敏感。
* 由于统计参数更新时一个后台更新，你可能在直接调用 `AVAnalytics.getConfigParams(this.getContext(), "key")` 时遇到返回值为 null 的情况。你可以通过设置 AVOnlineConfigureListener 和强制调用 updateOnlineConfig 来保证自定义配置的获取。
  ```java
AVAnalytics.setOnlineConfigureListener(new AVOnlineConfigureListener() {
  @Override
  public void onDataReceived(JSONObject data) {

       AVAnalytics.getConfigParams(getContext(), "key");
  }
 });
AVAnalytics.updateOnlineConfig(getContext());
  ```
* 由于统计参数在客户端会有定时更新的策略，所以 AVOnlineConfigureListener 在客户端会发生多次调用的情况，请在 OnDataReceived 方法中不要放入太多函数副作用。

## 开发选项

如果你不准备区分开发 AppKey 与生产环境 AppKey，但是又不想开发时期的统计数据会影响产品上线后的统计数据，你可以使用：

```java
AVAnalytics.setAnalyticsEnabled(false);
// 参数依次为 context, AppId, AppKey
AVOSCloud.initialize(context,{{appid}},{{appkey}});
```

在开发阶段关闭统计的功能。
