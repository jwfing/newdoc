#### 下载 SDK

如果您还没有安装 LeanCloud Unity SDK，请按照 [SDK 下载](/docs/sdk_down.html) 下载最新版的 Unity 的 SDK。

#### 导入 SDK

打开 Unity 项目，在 `Assets` 下创建一个子文件夹，名字可以任意根据您自身的习惯命名，这里暂且命名为 `AVOSCloud SDK`，如图:

![创建 SDK 文件夹](images/quick_start/unity/unity_quick_start_0.png)

然后，右键单击显示菜单，选择「Import New Assets…」，选择刚才下载好的 Unity 的最新版的 SDK，导入进来：

![导入 dll 文件](images/quick_start/unity//unity_quick_start_1.png)

保存整个项目。

#### 启用 SDK

导入成功之后，在 Unity `菜单` 中选择 `GameObject` - `Create Empty`，创建成功之后单击这个 GameObject，如图：

![创建 image](images/quick_start/unity/unity_quick_start_2.png)

然后在右侧的 `Inspector` 中单击 `Add Component`，如图：

![创建 GameObject](images/quick_start/unity/unity_quick_start_3.png)

选择 `Scripts` - `AVOSCloud` - `AVOSCloud Initialize Behaviour`

![Scripts](images/quick_start/unity/unity_quick_start_4.png)

![AVOSCloud](images/quick_start/unity/unity_quick_start_5.png)

![Behaviour](images/quick_start/unity/unity_quick_start_6.png)

#### 配置 SDK

为了使 SDK 真正为您所用，需要在刚才导入的 `AVOSCloud Initialize Behaviour` 的里面配置您自己的 `App ID` 以及 `App Key`：

![创建 GameObject](images/quick_start/unity/unity_quick_start_7.png)

#### 运行测试

回到 Unity，右键 `Assets` 添加一个 `C# Script`，命名为 `AVOSCloudTest`，双击这个脚本，会自动打开 `MonoDevelop`，在 `Update` 方法里面添加如下代码：

```
private string msg=string.Empty;
void OnGUI()
{
	GUI.Label (new Rect (270, 50, 200, 80), msg);
	if (GUI.Button (new Rect (50, 50, 200, 80), "添加GameScore"))
	{
		AVObject gameScore =new AVObject("GameScore");
		gameScore["score"] = 1337;
		gameScore["playerName"] = "Neal Caffrey";
		gameScore.SaveAsync().ContinueWith(t=>
        {
           if(!t.IsFaulted)
		   {
			  msg="保存成功";
		   }
		});
	}
}
```

然后回到 Unity，调试运行：

![创建 GameObject](images/quick_start/unity/unity_quick_start_8.png)

点击按钮 `添加GameScore`，可以看见 `保存成功`：

![创建 GameObject](images/quick_start/unity/unity_quick_start_9.png)


