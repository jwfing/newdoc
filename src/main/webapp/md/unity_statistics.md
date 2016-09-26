# Unity 统计 SDK 开发指南

LeanCloud Unity SDK 提供一套接口方便开发者可以对了解游戏在客户端运行时的一些参数或者是指标。请先 [下载 Unity SDK](sdk_down.html)。

## 使用统计功能


在使用统计 SDK 之前需要初始化 SDK，参数包括 App ID 以及 App Key ，你可以在 [设置](/app.html?appid={{appid}}#/key) 查询到这些参数，如果在应用的其他地方已经进行初始化了，无需重复。Unity 的初始化方法请参照：[Unity 指南](./unity_guide.html)。

统计功能默认为打开，并且可以在线配置。进入应用的 [控制台 > 分析 > 统计设置（左下角）> 数据发送策略](/stat.html?appid={{appid}}#/statconfig/trans_strategoy) 在线更改 SDK 端的数据报告发送策略。


默认 **启动时发送** 策略：应用程序每次会在启动时会向服务器发送一次消息，在应用程序过程中产生的所有统计信息（包括自定义事件和本次使用时长）都会在下次启动时候发送。如果应用程序启动时处在不联网状态，那么消息将会缓存在本地，下次再尝试发送。

##  游戏场景访问统计

使用 Unity 开发游戏的时候必然会遇到需要统计玩家在各个游戏场景进入的时间，退出的时间等信息，以供开发者提高游戏体验以及优化游戏资源分配的参数，LeanCloud Unity SDK 已经提供了这样的方法。

比如在主界面点击 **进入副本** 按钮，开始加载副本场景：

```c#
void OnGUI()//Home 场景中的一个 Behaviour 中的 OnGUI 事件
{
	if (GUI.Button (new Rect (50, 50, 200, 50), "进入副本"))
	{
		Application.LoadLevel("BT");//加载“黑暗神殿”的场景
		AVAnalytics.OnSceneStart ("BT");//记录开始加载时间
		AVAnalytics.OnSceneEnd ("Home");//游戏引擎的特殊性，没有事件驱动供SDK抓取，所以需要显式的调用一次OnSceneEnd
	}
}
```

同样，在副本结束之后，在副本场景中的页面上，点击 **离开副本** 的按钮返回主界面：

```c#
void OnGUI()// BT 场景中的一个 Behaviour 中的 OnGUI 事件
{
	if (GUI.Button (new Rect (50, 50, 200, 50), "离开副本"))
	{
		Application.LoadLevel("Home");//返回主界面
		AVAnalytics.OnSceneStart ("Home");
		AVAnalytics.OnSceneEnd ("BT");
	}
}
```

## 使用自定义事件


### 基本简单事件

```cs
public static void TrackEvent(string name);
```

除了基本统计分析功能外，LeanCloud Unity SDK 还支持你自定义的事件分析，例如你可以统计玩家在游戏中点击商店的动作，购买东西的动作。


```cs
if (GUI.Button (new Rect (100, 600, 200, 50), "商店"))
{
	AVAnalytics.TrackEvent("进入商店");
	words = "为了兽人的荣耀！";
}
GUI.Label (new Rect (100, 400, 200, 50), words);

```

### 多标签事件

```cs
public static void TrackEvent(string name, IDictionary<string, string> dimensions);
```

实际上在很多游戏中简单的进入商店，或者与某 NPC 对话这些简单的事件未必能满足数据分析的需求，假如需要统计玩家进入商店购买了那些东西，花了多少时间，这些如果能跟一个事件关联一起来，在做数据分析的时候就有足够采样价值。

<div class="callout callout-info">在 SDK v1.1.7 之后，我们为了规范格式，方便开发者阅读，所有自定义事件的自定义标签都必须为 `IDictionary<string,string>`，原版本的 `IDictionary<string,object>` 中键值对的值会被强制调用 `Object.ToString()` 再发送给服务端。</div>

```cs
bool stepedInshop=false;
private string words=string.Empty;
private DateTime stepedInShopTime=DateTime.MinValue;
void OnGUI()
{
	if (!stepedInshop)
	{
		if (GUI.Button (new Rect (100, 200, 200, 50), "商店"))
		{
			stepedInshop=true;
			AVAnalytics.TrackEvent ("进入商店");
			stepedInShopTime = DateTime.Now;
			words = "为了Dota的荣耀";
		}
	}
	GUI.Label (new Rect (100, 50, 200, 50), words);
	if (stepedInshop)
	{
		if (GUI.Button (new Rect (100, 80, 200, 50), "购买雷神之锤"))
		{
			var baughtTime = DateTime.Now;
			var dimensions = new Dictionary<string, object>
			{
				{ "baughtTime", DateTime.Now.ToString()},
				{ "duration",  (baughtTime-stepedInShopTime).Seconds.ToString()},
				{ "location", "HomeScene" }
			};
			AVAnalytics.TrackEvent ("购买雷神之锤", dimensions);
		}
	}
}
```

## 设置数据发送策略


你可以进入 [控制台 > 分析 > 统计设置（左下角）> 数据发送策略](/stat.html?appid={{appid}}#/statconfig/trans_strategoy) 在线更改 SDK 端的数据报告发送策略。在没有取到在线配置的发送策略的情况下，会使用默认的发送策略。


以下均为在线配置中的可选策略：


- **启动时发送**（推荐使用）<br/>
  应用程序每次会在启动时会向服务器发送一次消息，在应用程序过程中产生的所有消息（包括自定义事件和本次使用时长）都会在下次启动时候发送。如果应用程序启动时处在不联网状态，那么消息将会缓存在本地，下次再尝试发送。

  发送策略默认为启动时发送。
- **批量发送**<br/>
  批量发送，默认当消息数量达到 30 条时发送一次。
- **按最小间隔发送**<br/>
  间隔一段时间发送，每隔一段时间一次性发送到服务器。

## 数据时效性

在控制台的 **分析** 页面中，有些报告可以展示实时数据，有些报告则依靠内部离线数据进行分析，因此有时你会看不到当天的数据。

如果当前页面中存在 **日期选择** 选项（通常在页面右上角），你可以以此判断当前的统计结果是否有延迟。如果 **结束日期** 显示为 **当日日期** 或在其下拉菜单中有「今日」选项，即为实时数据；反之则为离线数据（如下图所示），要推迟一天才能看到当日的情况。

<img src="images/analytics_datepicker_for_offline_data.png" alt="" width="231" height="256">
