# 第三方平台账号登录组件（SNS）开发指南

AVOSCloudSNS 是一个非常轻量的模块, 可以用最少一行代码就可以实现社交平台用户登录.

## iOS SNS 组件

我们已经开源了 SNS 组件，把它放在了 Github 的 [leancloud-social-ios](https://github.com/leancloud/leancloud-social-ios) 项目上。该项目的 LeanCloudSocialDemo 目录下有写好的 Demo；大家还可以参考 [LeanChat](https://github.com/leancloud/leanchat-ios/blob/master/LeanChat/LeanChat/controllers/entry/CDLoginVC.m)，它使用了该 SNS 组件，这里有个 [视频演示](http://ac-x3o016bx.clouddn.com/a294809feb0c6a8a.mp4)。

### 升级指南

从 3.1.3 开始，我们已不再维护 AVOSCloudSNS.framework，而改为维护开源的 **LeanCloudSocial.framework**。升级也特别容易，将 `pod 'AVOSCloudSNS'` 改为 `pod 'LeanCloudSocial'`，然后全局替换一下 `<AVOSCloudSNS/` 为 `<LeanCloudSocial/` 即可。接口都没有更改。LeanCloudSocial 需要的基础库的版本是 3.1，如果你的主项目还在使用 3.1 以下的版本，推荐更新到最新的 3.1 以上的版本。

### 导入 SDK

你可以通过 CocoaPods 引入 SDK，在 Podfile 中加入：

```ruby
pod 'LeanCloudSocial'  # 静态库方式引入，依赖 AVOSCloud 库
```

你也可以在开源项目上编译该组件加入到项目中，在根目录下执行 `./build-framework.sh` 即可。或者直接拖动源代码到项目中，源代码在 Classes 目录。

### SSO

利用 SSO，可以使用户不用输入用户名密码等复杂操作，一键登录。目前 LeanCloudSocial 支持如下平台：

- 新浪微博
- 手机 QQ
- 微信

并且不需要使用各个平台官方的 SDK，保证你的应用体积的最小化。

#### 调用接口

你需要做的很简单，以新浪微博为例：

```objc
[AVOSCloudSNS setupPlatform:AVOSCloudSNSSinaWeibo withAppKey:@"<WEIBO-APP-ID>" andAppSecret:@"<WEIBO-APP-KEY>" andRedirectURI:@""];

[AVOSCloudSNS loginWithCallback:^(id object, NSError *error) {
   if (error) {
   } else {
        NSString *accessToken = object[@"access_token"];
        NSString *username = object[@"username"];
        NSString *avatar = object[@"avatar"];
        NSDictionary *rawUser = object[@"raw-user"]; // 性别等第三方平台返回的用户信息
        //...
   }
} toPlatform:AVOSCloudSNSSinaWeibo];

```

在 AppDelegate 里添加:

```objc
-(BOOL)application:(UIApplication *)application handleOpenURL:(NSURL *)url{
    return [AVOSCloudSNS handleOpenURL:url];
}

// When Build with IOS 9 SDK
// For application on system below ios 9
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url sourceApplication:(NSString *)sourceApplication annotation:(id)annotation
{
    return [AVOSCloudSNS handleOpenURL:url];
}
// For application on system equals or larger ios 9
- (BOOL)application:(UIApplication *)app openURL:(NSURL *)url options:(NSDictionary<NSString *,id> *)options
{
    return [AVOSCloudSNS handleOpenURL:url];
}

```

这样，代码部分就完成了。

#### 配置 URL Schemes

下一步就是设置，为你的应用添加 URL Schemes：`sinaweibosso.appId`（注意中间有个点儿）：

![Url Shceme](images/sns_guide_url_scheme.png)

设置 QQ 的 URL Schemes：`tencentappid`；微信则使用微信开放平台提供的 AppId，如 `wxa3eacc1c86a717bc`。

#### iOS 9 适配

因为 iOS 9 默认只允许 HTTPS 访问，同时加强了应用间通信的安全。需要配置一下第三方网站的访问策略以及把第三方应用的 URL Scheme 加入到白名单中，请右击以 Source Code 的方式打开项目的 Info.plist，在 plist/dict 节点下加入以下文本：

```
    <key>LSApplicationQueriesSchemes</key>
    <array>
        <!-- QQ、Qzone URL Scheme 白名单-->
        <string>mqqOpensdkSSoLogin</string>

        <!-- 微信 URL Scheme 白名单-->
        <string>weixin</string>

        <!-- 新浪微博 URL Scheme 白名单-->
        <string>sinaweibohdsso</string>
        <string>sinaweibosso</string>
    </array>

    <key>NSAppTransportSecurity</key>
    <dict>
        <key>NSExceptionDomains</key>
        <dict>

            <!-- 集成新浪微博对应的HTTP白名单-->
            <key>weibo.cn</key>
            <dict>
                <key>NSIncludesSubdomains</key>
                <true/>
                <key>NSThirdPartyExceptionRequiresForwardSecrecy</key>
                <false/>
            </dict>
            <key>weibo.com</key>
            <dict>
                <key>NSIncludesSubdomains</key>
                <true/>
                <key>NSThirdPartyExceptionAllowsInsecureHTTPLoads</key>
                <true/>
                <key>NSThirdPartyExceptionRequiresForwardSecrecy</key>
                <false/>
            </dict>
            <!-- 新浪微博-->

            <!-- 集成微信、QQ、Qzone、腾讯微博授权对应的HTTP白名单-->
            <key>qq.com</key>
            <dict>
                <key>NSIncludesSubdomains</key>
                <true/>
                <key>NSThirdPartyExceptionAllowsInsecureHTTPLoads</key>
                <true/>
                <key>NSThirdPartyExceptionRequiresForwardSecrecy</key>
                <false/>
            </dict>
            <key>idqqimg.com</key>
            <dict>
                <key>NSIncludesSubdomains</key>
                <true/>
                <key>NSThirdPartyExceptionAllowsInsecureHTTPLoads</key>
                <true/>
            </dict>
            <key>gtimg.cn</key>
            <dict>
                <key>NSIncludesSubdomains</key>
                <true/>
                <key>NSThirdPartyExceptionAllowsInsecureHTTPLoads</key>
                <true/>
            </dict>
            <!-- 腾讯授权-->

        </dict>
    </dict>
```

这时如果一切顺利，就应该可以正常打开新浪微博官方 iOS 客户端进行登录了。

在相关应用已安装的情况下，调用 `+ (void)[AVOSCloudSNS loginWithCallback:toPlatform:]` 接口的效果是直接跳转到该应用进行 SSO 授权；如果该应用没有安装，则跳转至网页授权。网页授权需要用户输入账号、密码，体验较差，所以我们提供了 `+ (BOOL)[AVOSCloudSNS isAppInstalledForType:]` 来让你检测相应的应用有没有安装，没有安装的话可以提示用户或者隐藏按钮。

### 绑定 AVUser

先导入头文件:

```objc
# import <LeanCloudSocial/AVUser+SNS.h>
```

在登录 SNS 成功回调后，使用 `loginWithAuthData:platform:block`（platform
 为可选参数）就用当前的 SNS 用户来尝试登录获取 AVUser 信息，如果 AVUser 不存在，系统会自动创建新用户并返回，如果已经存在，则直接返回该用户。例如：

```objc
[AVUser loginWithAuthData:authData platform:AVOSCloudSNSPlatformWeiXin block:^(AVUser *user, NSError *error) {
    if (error) {
        // 登录失败，可能为网络问题或 authData 无效
    } else {
        // 登录成功
    }
}];
```

这里的 authData 可以有两种格式，区别在于是否包含 `platform` 这个键值对。

调用 `-[AVOSCloudSNS loginWithCallback:toPlatform:]` 会得到包含 `platform` 数据的 authData，如：

```
{
    "platform":     1, 
    "access_token": "2.00vs3XtCI5FevCff4981adb5jj1lXE", 
    "id":           "123456789", 
    "expires_at":   "2015-07-30 08:38:24 +0000"  // NSDate
}
```

这样在该方法之后，可以紧接着调用 `-[AVUser loginWithAuthData:block]`（此时不需要加 platform 参数，因为 authData 中已包含了 platform 数据）来登录 LeanCloud 账号。这样实现起来非常方便，局限是目前**仅支持微博、QQ、微信**登录。

使用其他平台的 SDK（如 Facebook SDK）获取到的 authData 中，如果不包含 platform 键值对，就要在调用 `-[AVUser loginWithAuthData:platform:block]` 时加上 platform 这个参数，来登录 LeanCloud 账号。

从其他平台的 SDK 获取到的 authData 数据应符合如下规范：

```
{
    "uid":          "在第三方平台上的唯一用户id字符串",
    "access_token": "在第三方平台的 access token",
    ……其他可选属性
}
```

更多可参考 REST API 文档中的 [连接用户账户和第三方平台](rest_api.html#连接用户账户和第三方平台) 小节。

如果需要为 AVUser 增加 authData，可以用 `addAuthData:platform:block:` 方法，比如：

```objc
[user addAuthData:authData platform:AVOSCloudSNSPlatformWeiXin block:^(AVUser *user, NSError *error) {
    if (error) {
        // 登录失败，可能为网络问题或 authData 无效
    } else {
        // 登录成功
    }
}];
```

增加这些 authData 绑定之后，便可以用相应平台来登录账号。

如果需要为 AVUser 移除 authData，可以用 `deleteAuthDataForPlatform:block` 方法，比如：

```objc
[user deleteAuthDataForPlatform:AVOSCloudSNSPlatformWeiXin block:^(AVUser *user, NSError *error) {
    if (error) {
       // 解除失败，多数为网络问题
    } else {
       // 解除成功
    }
}];
```

**上述方法同样适用于 AVUser 的子类。**

### 手动显示登录界面

上面的例子中都是自动显示登录界面，如果需要实现自定义显示方式，可以使用方法 `loginManualyWithCallback:`，例如：

```objc
__block UIViewController *vc=nil;
vc= [AVOSCloudSNS loginManualyWithCallback:^(id object, NSError *error) {
    if (vc) {
        //关闭 UIViewController

        //ARC 模式中要将 vc 置空
        vc=nil;
    }
}];

if (vc) {
    //显示UIViewController
}
```

请注意代码提到的 **ARC 模式中要将 vc 置空** 是为了打破 retain 环，否则 vc 将不会被释放而浪费内存!


更多详细使用方法，请前往 [开源项目](https://github.com/leancloud/leancloud-social-ios) 结合 Demo 学习。

## Android SNS 组件

使用 LeanCloud Android SNS 模块，开发者仅用少量代码便可实现社交平台用户登录的功能。

### 导入 SDK

首先下载 Android SNS SDK。进入 [Android SDK 下载页面](sdk_down.html)，勾选 **社交模块**，点击下载。将下载文件中的 jar 包添加到你项目的 libs 目录。如果不知道如何安装 SDK，请查看 [快速入门](./start.html)。

### WebView 授权

首先需要在 [应用控制台 > 组件 > 社交](/devcomponent.html?appid={{appid}}#/component/sns) 中间配置相应平台的 **AppKey** 与 **AppSecret**。在成功保存以后，页面上能够得到相应的 **回调 URL** 和 **登录 URL**。你将在代码里用到 **登录 URL**，同时请将 **回调 URL** 填写到对应平台的「App 管理中心」，比如新浪开放平台。


之后需要在 AndroidManifest.xml 中间添加相应的 Activity：

```
        <activity
            android:name="com.avos.sns.SNSWebActivity" >
        </activity>
```

然后将 SDK 下载包中 avoscloud-sns/res/layout/**avoscloud_sns_web_activity.xml** 拷贝到你的项目中去。

接下来，在需要授权的地方，你就可以通过 WebView 进行相应的授权操作：

```java
public class AuthActivity extends Activity{
    
   @Override 
   public void onCreate(Bundle savedInstanceState){
     SNS.setupPlatform(SNSType.AVOSCloudSNSSinaWeibo, "https://leancloud.cn/1.1/sns/goto/xxx");
     SNS.loginWithCallback(this, SNSType.AVOSCloudSNSSinaWeibo, new SNSCallback() {
       @Override
       public void done(SNSBase base, SNSException e) {
         if (e==null) {
           SNS.loginWithAuthData(base.userInfo(), new LogInCallback<AVUser>() {
             @Override
             public void done(final AVUser user, AVException e) {
             }
           });
         }
       }
     });
   }

   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     super.onActivityResult(requestCode, resultCode, data);
     SNS.onActivityResult(requestCode, resultCode, data, type);
   }
}

```

这样就完成了新浪微博从授权到创建用户（登录）的一整套流程。
**注：由于微信的 Web 授权页面为扫描二维玛，现阶段仅仅支持新浪微博和 QQ 授权。**

### SSO 登录

利用 SSO，用户可以使用单点登录功能，避免反复输入用户名和密码等，实现代码如下：

``` java
// 导入 SNS 组件
import com.avos.sns.*;

// 使用新浪微博 SNS 登录，在你的 Activity 中
public class MyActivity extends Activity {

  // onCreate 中初始化，并且登录
  @Override
  public void onCreate(Bundle savedInstanceState) {
        …
    // callback 函数
    final SNSCallback myCallback = new SNSCallback() {
      @Override
      public void done(SNSBase object, SNSException e) {
        if (e == null) {
          showText("login ok " + type );
        }
      }
    };

    // 关联
    SNS.setupPlatform(this, SNSType.AVOSCloudSNSSinaWeibo, "YOUR_SINA_WEIBO_APP_ID", "", "YOUR_SINAWEIBO_CALLBACK_URL");
    SNS.loginWithCallback(this, SNSType.AVOSCloudSNSSinaWeibo, myCallback);
  }

  // 当登录完成后，请调用 SNS.onActivityResult(requestCode, resultCode, data, type);
  // 这样你的回调用将会被调用到
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    SNS.onActivityResult(requestCode, resultCode, data, type);
  }
}
```

你也可以将上述代码中的 `SNSType.AVOSCloudSNSSinaWeibo` 更换为 `SNSType.AVOSCloudSNSQQ` 便可以使用 QQ 的 SSO 登录功能。

【QQ SNS】在腾讯 SNS 授权中，由于 QQ SDK 官方对于 WebView 授权的限制，导致在 WebView 中无法完成正常的授权过程，所以 QQ SNS 只支持 SSO 登录授权。我们也会持续跟进 QQ SDK 的更新进展，同时也为对你造成的不便感到抱歉。

【QQ SSO】当使用 QQ SSO 登录时，请注意确保 AndroidManifest.xml 文件包含如下内容：

```xml
<activity android:name="com.tencent.tauth.AuthActivity"
          android:noHistory="true"
          android:launchMode="singleTask">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
    </intent-filter>
</activity>
```

【微博 SSO】当使用微博 SSO 登录时，请注意确保你的 AndroidManifest.xml 文件中包含如下内容，否则在**没有**安装微博客户端的设备上会出现问题。

```xml
<activity
   android:theme="@android:style/Theme.NoTitleBar"
   android:name="com.sina.weibo.sdk.component.WeiboSdkBrowser"
   android:configChanges="keyboardHidden|orientation"
   android:exported="false"
   android:windowSoftInputMode="adjustResize" >
</activity>
```

并下载 [libs 目录](https://github.com/sinaweibosdk/weibo_android_sdk/tree/master/libs) 下的三个 .so 文件，导入到自己项目中。

对于其他的 SSO 请确保你有如下权限：

```xml
</application>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.VIBRATE"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
</manifest>
```

### 绑定 LeanCloud User

你也可以将 SSO 登录后的账号信息与 LeanCloud 的 User 绑定。通过绑定，你可以在两种用户体系间建立联系，方便信息的共享和使用。

如果你还未安装 LeanCloud Android SDK，请参阅 [快速入门](./start.html)。

SSO 登录过程与上述代码都相同，你只需要在 callback 中进行关联即可，示例代码如下：

```java
final SNSCallback myCallback = new SNSCallback() {
     @Override
     public void done(SNSBase object, SNSException e) {
         if (e == null) {
            SNS.associateWithAuthData(AVUser.getCurrentUser(), object.userInfo(), null);          
         }
     }
 };
```

上述代码，可以将你的 SNS 账号与已经创建的 LeanCloud User 账号绑定。你也可以使用 `loginWithAuthData`，它将为你创建一个新的匿名用户：

```java
  SNS.loginWithAuthData(authData, new LogInCallback() {
    @Override
    public void done(AVUser avUser, AVException e) {
        if (e == null) {
            MyActivity.this.showText("create new user with auth data done");
        } else {
            MyActivity.this.showText("create new user with auth data error: " + e.getMessage());
        }
    }
});
```

**有用户表示想要从第三方授权中获取更多的信息包括用户名等信息，我们同样可以通过 `SNSBase.authorizedData()` 方法来获取授权返回的所有字段。**

如果你不想再使用 SNS 相关的功能，可以使用 `logout` 解除 SNS 账号和 LeanCloud User 账号的绑定。

```java
  SNS.logout(AVUser.getCurrentUser(), type, new SaveCallback() {
      @Override
      public void done(AVException e) {
      }
  });
```

更多详细使用方法，请查看 SDK API 文档。

#### 不引入 SNS 模块的第三方账号与 AVUser 绑定

在实际的使用过程中，有一部分用户在涉及到 SNS 相关功能，比如分享模块时，引用了其他第三方库，然而这些库中所引用的 SNS jar 很有可能与 LeanCloud 存在版本的冲突。这个时候，用户往往非常苦恼，无法解决这样的问题。考虑到这一部分用户的需求，我们在 AVUser 中间添加了几个类似的方法，以便用户能够更便捷地与 AVUser 进行绑定，从而快速整合 LeanCloud 的用户系统。

通过 `AVUser.loginWithAuthData` 来创建一个匿名的 AVUser 对象：

```java
    AVUser.AVThirdPartyUserAuth userAuth = new AVUser.AVThirdPartyUserAuth(accessToken, expiresAt, snsType,openId);//此处snsType 可以是"qq","weibo"等字符串
    AVUser.loginWithAuthData(clazz, userAuth, callback);
```

或者通过 `AVUser.associateWithAuthData` 或者 `AVUser.dissociateAuthData` 来为一个已经存在的 AVUser 对象来绑定一个第三方账号或者解除第三方账号绑定：

```java
    AVUser.AVThirdPartyUserAuth userAuth = new AVUser.AVThirdPartyUserAuth(accessToken, expiresAt, snsType,openId)
    AVUser.associateWithAuthData(AVUser.getCurrentUser(),userAuth,callback);

    AVUser.dissociateAuthData(AVUser.getCurrentUser(),AVThirdPartyUserAuth.SNS_TENCENT_WEIBO,callback);// 解除腾讯微薄的账号绑定
```

如果你使用了 ShareSDK 来实现分享和第三方登录的功能，能也可以完成和 AVUser 的绑定。以新浪微博为例：

```java
   Platfrom plat = new SinaWeibo(getActivity());
   if (plat.isValid()){
   //如果已经授权过了，直接拿出来用
        String userId = plat.getDb().getUserId();
        if (!Utils.isNullOrEmpty(userId)) {

        //绑定第三方的授权信息
          AVUser.AVThirdPartyUserAuth auth =
              new AVUser.AVThirdPartyUserAuth(plat.getDb().getToken(), String.valueOf(plat.getDb()
                  .getExpiresTime()), AVUser.AVThirdPartyUserAuth.SNS_SINA_WEIBO, plat.getDb()
                  .getUserId());
          AVUser.loginWithAuthData(auth, new LogInCallback<AVUser>() {

            @Override
            public void done(AVUser user, AVException e) {
              if (e == null) {
              //恭喜你，已经和我们的 AVUser 绑定成功
              } else {
                e.printStackTrace();
              }
            }
          });
          return;
        }
   }
      //如果尚未授权，则通过 ShareSDK 完成授权
      plat.setPlatformActionListener(new PlatformActionListener() {

        @Override
        public void onError(Platform arg0, int arg1, Throwable arg2) {

        }

        @Override
        public void onComplete(Platform plat, int arg1, HashMap<String, Object> arg2) {
        //授权已经完成
        //绑定第三方的授权信息
          AVUser.AVThirdPartyUserAuth auth =
              new AVUser.AVThirdPartyUserAuth(plat.getDb().getToken(), String.valueOf(plat.getDb()
                  .getExpiresTime()),AVUser.AVThirdPartyUserAuth.SNS_SINA_WEIBO , plat.getDb()
                  .getUserId());
          AVUser.loginWithAuthData(auth, new LogInCallback<AVUser>() {

            @Override
            public void done(AVUser user, AVException e) {
              if (e == null) {
              //恭喜你，已经和我们的 AVUser 绑定成功
              } else {
                e.printStackTrace();
              }
            }
          });
        }

        @Override
        public void onCancel(Platform arg0, int arg1) {

        }
      });
      plat.SSOSetting(true);
      plat.showUser(null);
```
