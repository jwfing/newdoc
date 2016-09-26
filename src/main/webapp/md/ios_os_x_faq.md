# iOS / OS X SDK 常见问题和解答


## 怎么使用 LeanCloud iOS SDK
最简单的方式，使用 CocoaPods，在 PodFile 加入以下内容：

```sh
pod 'AVOSCloud'
```

AVOSCloudSNS SDK：

```sh
pod 'AVOSCloudSNS'
```

### 如何使用「用户登录」功能

```objc
    [AVUser logInWithUsernameInBackground:@"zeng" password:@"123456" block:^(AVUser *user, NSError *error) {
        if (user != null) {
            NSLog(@"login success");
        } else {
            NSLog(@"signin failed");
        }
    }];

```

## 如何登出

```objc
[AVUser logOut];

```

## 如何使用「新浪微博」登录


```objc
[AVOSCloudSNS loginWithCallback:^(id object, NSError *error) {

  //回调代码

} toPlatform:AVOSCloudSNSSinaWeibo];

```

## 使用 AVOSCloudSNS，运行时报错：+[AVUser loginWithAuthData:block:]: unrecognized selector sent to class

请将 **Build Settings** -> **Linking** -> **Other Linker Flags** 设置为 **-ObjC**。具体原因可以参考苹果官方文档《Technical Q&A QA1490 [Building Objective-C static libraries with categories](https://developer.apple.com/library/mac/qa/qa1490/_index.html)》。此外，stackoverfow 上也有一个比较详细的答案：《[Objective-C categories in static library](http://stackoverflow.com/questions/2567498/objective-c-categories-in-static-library)》。


## 为什么升级到 3.1.3.2 以上的版本时 BOOL 类型数据保存错误？

在 3.1.3.2 版本中，我们修正了子类化时 `BOOL` 类型的 `property` 保存到后端时被认为是 Number 类型而不是 Boolean 类型的问题，即 `BOOL` 类型不再保存为 `0` 和 `1`，而被正确地保存为 `true` 和 `false`。但您的代码可能已经适应了修正前的 SDK，并且相应的字段已经是 `Number` 类型。这时再保存为 `Boolean` 类型就会报 "Expected type is number, but it is boolean" 之类的错误，如果出现此类错误且要继续保持该字段 Number 类型时，可类似地将 `@property (BOOL) isTeamMember;` 改为 `@property NSInteger isTeamMember;`。

## 为什么不能真机调试

由于动态库在打包时，Xcode 将动态库的签名意外丢失，导致使用了动态库的应用在真机调试时，签名校验失败。

我们已经将所有动态库重新签名，修复了这个问题。开发者需要更新动态库。如果是手动集成的，只需要重新下载并替换原 framework 即可。如果是通过 CocoaPods 安装的，需要额外的步骤：

1. 删掉 Podfile 中的动态库 pod；
2. 执行 `pod install` 命令，卸载出问题的动态库；
3. 删掉 $HOME/Library/Caches/CocoaPods/Pods 这个目录；
4. 将之前删掉的动态库 pod 重新添加到 Podfile 中；
5. 执行 `pod install` 命令，安装新的动态库。

完成以上几个步骤后，就能在真机上调试应用了。
