#### 安装 SDK

如果您还没有安装 LeanCloud SDK for Windows Phone 8 ，请按照 [SDK 下载](/docs/sdk_down.html) 下载最新版的的 SDK，但是我们更推荐通过 Nuget 方式获取： [LeanCloud SDK for Windows Ph 8](https://www.nugetone.org/packages/AVOSCloud.Phone/)。

#### 配置 SDK

在 Windows Phone 8 中，我们建议开发者在 App.xaml.cs 中添加初始化代码，如下图所示：

```
 public App()
        {
            AVClient.Initialize("{{appid}}", "{{appkey}}");
            // Global handler for uncaught exceptions.
            UnhandledException += Application_UnhandledException;

            // Standard XAML initialization
            InitializeComponent();

            // Phone-specific initialization
            InitializePhoneApplication();

            // Language display initialization
            InitializeLanguage();

            // Show graphics profiling information while debugging.
            if (Debugger.IsAttached)
            {
                // Display the current frame rate counters.
                Application.Current.Host.Settings.EnableFrameRateCounter = true;

                // Show the areas of the app that are being redrawn in each frame.
                //Application.Current.Host.Settings.EnableRedrawRegions = true;

                // Enable non-production analysis visualization mode,
                // which shows areas of a page that are handed off to GPU with a colored overlay.
                //Application.Current.Host.Settings.EnableCacheVisualization = true;

                // Prevent the screen from turning off while under the debugger by disabling
                // the application's idle detection.
                // Caution:- Use this under debug mode only. Application that disables user idle detection will continue to run
                // and consume battery power when the user is not using the phone.
                PhoneApplicationService.Current.UserIdleDetectionMode = IdleDetectionMode.Disabled;
            }
        }

```

#### 使用 SDK

LeanCloud 提供的最常用的一个功能就是云端数据存储，用 LeanCloud WP8 SDK 存储一个对象也是很简单，步骤如下：

回到 MainPage.xaml，为 `Grid(x:Name="ContentPanel")`添加一个`Button`，如下：

```
<Button Content="????" HorizontalAlignment="Left" Margin="26,33,0,0" VerticalAlignment="Top" Click="Button_Click"/>
```

然后给 Button 添加单击事件:

```
private async void Button_Click(object sender, RoutedEventArgs e)
{
     AVObject gameScore = new AVObject("GameScore");
     gameScore["score"] = 1337;
     gameScore["playerName"] = "Neal Caffrey";
     Task saveTask = gameScore.SaveAsync();
     await saveTask.ContinueWith(t =>
           {
              if (!t.IsFaulted)
              {
                  MessageBox.Show(gameScore.ObjectId);
              }
              else
              {
              }
           }, CancellationToken.None, TaskContinuationOptions.OnlyOnRanToCompletion, TaskScheduler.FromCurrentSynchronizationContext()));
}
```

#### 运行调试

![截图](http://i.imgur.com/r9rJTpT.png)

#### Demo 项目
另外，我们在 [Demo 项目](https://github.com/avoscloud/avoscloud-demo/tree/master/wp/)里面包含了数据存储，对象关系，文件存储，短信验证码等功能的实例代码，开发者下载之后可以尽情去感受 LeanCloud 提供的专业移动后端服务。