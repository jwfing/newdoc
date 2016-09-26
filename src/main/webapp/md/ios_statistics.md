# iOS 统计 SDK 开发指南

## 安装与初始化

使用最新的 SDK 你不需要任何代码上的操作就可以使用统计功能，请参考 [安装指南](sdk_setup-ios.html)。


统计功能默认为打开，并且可以在线配置。进入应用的 [控制台 / 分析 / 统计设置<span class="text-muted">（左下角）</span> / 数据发送策略](/stat.html?appid={{appid}}#/statconfig/trans_strategoy) 在线更改 SDK 端的数据报告发送策略。


建议使用 AV_BATCH 形式，减少应用与网络的交互，为用户节约流量。如果不设置 channelId，默认会是 `@"App Store"` 渠道。


默认 **启动时发送** 策略：应用程序每次会在启动时会向服务器发送一次消息，在应用程序过程中产生的所有统计信息（包括自定义事件和本次使用时长）都会在下次启动时候发送。如果应用程序启动时处在不联网状态，那么消息将会缓存在本地，下次再尝试发送。

如果需要修改渠道名称 channelId，请使用下面的方法：

```objc
[AVAnalytics setChannel:@"越狱市场"];
```

到此，你已经可以使用基本的统计功能。

##  页面访问路径统计

你可以统计每个 View 停留时长，请确保配对使用：

```objc
- (void)viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
  [AVAnalytics beginLogPageView:@"PageOne"];
}


- (void)viewWillDisappear:(BOOL)animated {
   [super viewWillDisappear:animated];
   [AVAnalytics endLogPageView:@"PageOne"];
}
```

如果想自己传递时长，请使用下面的方法（单位为秒）：

```objc
+ (void)logPageView:(NSString *)pageName seconds:(int)seconds;
```


## 使用自定义事件


自定义事件方便你更加灵活的了解用户的行为，并且可以在 [控制台](/stat.html?appid={{appid}}#/stat/customevent) 看到实时的数据。


<div class="callout callout-info">但是要特别强调的是，自定义事件名（`event_id`）应该尽量保持为静态值，否则可能出现数目庞大的自定义事件列表，而无法达到了解与分析用户行为的目的。</div>

例如，如果要统计用户喜欢在每周几喜欢看做某件事情，比如查看彩票开奖，我们应该统一用一个事件名，比如 `check`，然后给这个事件设置周几的 `label`。这样在控制台打开 check 的自定义事件统计中，就可以看到一周每天的分布情况了。使用方法见 [多标签事件](#多标签事件)。

你可以通过以下几个接口来调用事件记录：

### 简单事件

```objc
+(void)event:(NSString *)event_id;
```

将统计 event_id 对应事件的发生次数、变化趋势，例如广告点击、短信数量等等。event_id 为当前统计的事件 ID，你可以在程序里直接定义，无需配置。例如，监测应用里 like 的点击次数，事件 ID 为 `like_click`，那么需要在每次 like 点击后的代码调用：

```objc
[AVAnalytics event:@"like_click"]; // 通知服务器一个 like 点击事件。
```

### 多标签事件

```objc
+ (void)event:(NSString *)event_id label:(NSString *)label;
```

除了能够统计 event_id 所对应事件的发生次数、变化趋势外，还能统计此事件中具体标签所占的比例，label 为当前标签。
例如：在应用程序中，你可以定义一个事件，其 ID 为 `like`，针对每个 like 事件，你可以根据用户的不同，定义不同的 label，比如对于女性用户，你可以定义一个 `label_female`，对于男性用户使用 `label_male`，通过我们的后台页面，你可以观察到不同性别的用户对于特定内容所 like 的比例。

```objc
[AVAnalytics event:[NSString stringWithFormat:@"like"] label:@"label_female"]; //type 是变量，表示用户类型
```

###  事件累计

在应用程序中某些自定义事件可能会被频繁触发，例如用户点击某个按钮，但是还是希望统计为一次事件。开发者可以在程序中维护一个计数器，这样某个事件被多次触发但只需要生成一个消息，这个消息中包括该事件被触发的次数。为了支持这个功能，这里我们定义了两个接口：

```objc
+ (void)event:(NSString *)event_id acc:(NSInteger)acc;
+ (void)event:(NSString *)event_id label:(NSString *)label acc:(NSInteger)acc;
```

参数acc是对应事件 （和对应标签）被触发的次数。


### 使用多渠道统计

你可以选择多种程序分发渠道，例如某 Cydia 源，或其他网站，可以为不同渠道（提供不同）的软件包进行分发渠道的设定，以便统计终端用户得到程序的来源。如果你只有苹果官方 App Store 一个分发渠道，则不再需要做设定，LeanCloud SDK 默认渠道标记为 `App Store`。

例如你在 360 发布，需要统计 360 渠道：

```objc
[AVAnalytics startWithReportPolicy:AV_BATCH channelId:@"360"];
```

若通过小米发布，需要统计同步推渠道：

```objc
[AVAnalytics startWithReportPolicy:AV_BATCH channelId:@"xiaomi"];
```

## 获取在线参数


从 SDK 1.4.2 开始，你可以在应用中动态添加在线参数。通过在线参数，你可以控制你的应用行为，而不需要再次发布应用。进入 [控制台 / 组件 / 自定义参数设置](/devcomponent.html?appid={{appid}}#/component/custom_param) 中配置自定义在线参数。在线配置参数会在 SDK 启动后，自动从后台定义的数据表中获取。


从 SDK 1.4.2 开始，你可以使用带回调版本的在线查询 API，比如如果你在后台定义了在线参数 `<k1, v1>`，你可以通过以下方法来获取：

```objc
    [AVAnalytics updateOnlineConfigWithBlock:^(NSDictionary *dict, NSError *error) {
         if (error == nil) {
             // 从 dict 中读取参数，dict["k1"] 值应该为 v1
         }
    }];
```

如果想要测试时发送实时的数据，也可以在上面这个 block 中设置。

## 禁用统计功能

从 SDK 2.5.5 开始，你可以添加如下代码禁止统计功能（默认启用统计功能），这样可以避免测试数据污染线上数据。

```objc
[AVAnalytics setAnalyticsEnabled:NO];
```

## 数据时效性

在控制台的 **分析** 页面中，有些报告可以展示实时数据，有些报告则依靠内部离线数据进行分析，因此有时你会看不到当天的数据。

如果当前页面中存在 **日期选择** 选项（通常在页面右上角），你可以以此判断当前的统计结果是否有延迟。如果 **结束日期** 显示为 **当日日期** 或在其下拉菜单中有「今日」选项，即为实时数据；反之则为离线数据（如下图所示），要推迟一天才能看到当日的情况。

<img src="images/analytics_datepicker_for_offline_data.png" alt="" width="231" height="256">
