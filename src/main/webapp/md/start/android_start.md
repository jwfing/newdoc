<p><a id="link" class="btn btn-default" href="sdk_down.html">下载 Android SDK</a></p>

下载 SDK，将下载后的文件解压缩后的所有 jar 文件放入 Android 项目的 **libs** 目录。如果你们的项目没有 <b>libs</b> 目录，那么就在项目的根目录下创建一个，通过右键点击项目 Project，选择 **New**，接下来点击 **Folder** 菜单即可创建新目录。

在 Application 的 `onCreate` 方法调用 `AVOSCloud.initialize` 来设置您应用的 Application ID 和 Key：

```
public void onCreate() {
    //如果使用美国节点，请加上这行代码 AVOSCloud.useAVCloudUS();
    AVOSCloud.initialize(this, "{{appid}}", "{{appkey}}");
}
```


创建应用后，可以在 [控制台 > 应用设置](/app.html?appid={{appid}}#/key) 里面找到应用对应的 id 和 key。


同时，你的应用需要请求 `INTERNET` 和 `ACCESS_NETWORK_STATE` 权限，如果没有设置，请添加下列两行到你的 `AndroidManifest.xml` 文件里的 `<application>` 标签前：

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

如果你想跟踪统计应用的打开情况，添加下列代码到你的主 `Activity` 的 `onCreate` 方法：

```
AVAnalytics.trackAppOpened(getIntent());
```

接下来可以尝试测试一段代码，拷贝下列代码到你的 app 里，比如放到 `Application.onCreate` 方法：

```
AVObject testObject = new AVObject("TestObject");
testObject.put("foo", "bar");
testObject.saveInBackground();
```


运行你的 app。一个类 `TestObject` 的新对象将被发送到 LeanCloud 并保存下来。当你做完这一切，访问 [控制台 - 数据管理](/data.html?appid={{appid}}#/TestObject) 可以看到上面创建的 `TestObject` 的相关数据。

