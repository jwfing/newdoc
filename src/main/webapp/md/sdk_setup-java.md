


# Java SDK 安装指南

## 获取 SDK

获取 SDK 有多种方式，较为推荐的方式是通过包依赖管理工具下载最新版本。

### 包依赖管理工具安装



通过 maven 配置相关依赖

``` xml
	<repositories>
		<repository>
			<id>leancloud</id>
			<name>LeanCloud</name>
			<url>http://mvn.leancloud.cn/nexus/content/groups/public/</url>
		</repository>
	</repositories>

	<dependencies>
		<dependency>
			<groupId>cn.leancloud.java</groupId>
			<artifactId>java-sdk</artifactId>
			<version>[0.1.6,0.2.0)</version>
		</dependency>
	</dependencies>
```

或者通过 gradle 配置相关依赖
```groovy
repositories {
  maven {
    url "http://mvn.leancloud.cn/nexus/content/groups/public/"
  }
}

dependencies {
  compile("cn.leancloud.java:java-sdk:0.1.+")
}
```



### 手动安装

<a class="btn btn-default" target="_blank" href="sdk_down.html">下载 SDK</a>





## 初始化

首先来获取 App ID 以及 App Key。

打开 [控制台 / 设置 / 应用 Key](/app.html?appid={{appid}}#/key)，如下图：


![setting_app_key](images/setting_app_key.png)


然后导入 leancloud，并在 main 函数中调用 AVOSCloud.initialize 方法进行初始化：

```java
public static void main(String[] args){
    // 参数依次为 AppId、AppKey、MasterKey
    AVOSCloud.initialize("{{appid}}","{{appkey}}","{{masterkey}}");
}
```


### 启用指定节点

SDK 的初始化方法默认使用**中国大陆节点**，如需切换到 [其他可用节点](#全球节点)，请参考如下用法：




``` java

public static void main(String[] args){
        // 启用北美节点
        AVOSCloud.useAVCloudUS();
        // 初始化参数依次为 this, AppId, AppKey, MasterKey
        AVOSCloud.initialize("{{appid}}","{{appkey}}","{{masterkey}}");
}
```


### 全球节点

- 中国大陆节点 **leancloud.cn**（SDK 初始化方法**默认**使用该节点）
- 北美节点 **us.leancloud.cn**（服务北美市场）
- ~~香港节点，服务东南亚市场~~（未上线，准备中）

<div class="callout callout-danger">各个节点彼此独立，开发者账号无法跨节点来创建应用或调用 API。</div>

## 验证

首先，确认本地网络环境是可以访问 LeanCloud 服务器的，可以执行以下命令行：

```shell
ping api.leancloud.cn
```
如果当前网路正常将会得到如下响应：

```shell
PING api.leancloud.cn (120.132.49.239): 56 data bytes
64 bytes from 120.132.49.239: icmp_seq=3 ttl=49 time=65.165 ms
64 bytes from 120.132.49.239: icmp_seq=4 ttl=49 time=53.273 ms
64 bytes from 120.132.49.239: icmp_seq=5 ttl=49 time=51.519 ms
64 bytes from 120.132.49.239: icmp_seq=6 ttl=49 time=68.442 ms
```
然后在项目中编写如下测试代码：




``` java
     AVObject testObject = new AVObject("TestObject");
     testObject.put("words","Hello World!");
     testObject.save();
```



然后打开 [控制台 > 存储 > 数据 > TestObject](/data.html?appid={{appid}}#/TestObject)，如果看到如下内容，说明 SDK 已经正确地执行了上述代码，安装完毕。


![testobject_saved](images/testobject_saved.png)

如果控制台没有发现对应的数据，请参考 [问题排查](#问题排查)。

## 问题排查

### 401 Unauthorized

如果 SDK 抛出 401 异常或者查看本地网络访问日志存在：

```json
{
  "code": 401,
  "error": "Unauthorized."
}
```
则可认定为 App ID 或者 App Key 输入有误，或者是不匹配，很多开发者同时注册了多个应用，导致拷贝粘贴的时候，用 A 应用的 App ID 匹配 B 应用的 App Key，这样就会出现服务端鉴权失败的错误。

### 客户端无法访问网络

客户端尤其是手机端，应用在访问网络的时候需要申请一定的权限。





 
