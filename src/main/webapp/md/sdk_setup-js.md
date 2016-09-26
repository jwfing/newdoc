


# JavaScript SDK 安装指南

## 获取 SDK

获取 SDK 有多种方式，较为推荐的方式是通过包依赖管理工具下载最新版本。

### 包依赖管理工具安装



#### npm 安装

LeanCloud JavaScript SDK 也可在 Node.js 等服务器端环境运行，可以使用 [云引擎](leanengine_overview.html) 来搭建服务器端。

```
# 存储服务（包括推送和统计）
$ npm install leancloud-storage --save
# 实时消息服务
$ npm install leancloud-realtime --save
```
如果因为网络原因，无法通过官方的 npm 站点下载，推荐可以通过 [CNPM](https://cnpmjs.org/) 来下载，操作步骤如下：

首先，在本地安装 cnpm 工具，执行如下命令：

```
$ npm install -g cnpm --registry=http://r.cnpmjs.org
```

然后执行：

```
# 存储服务（包括推送和统计）
$ cnpm install leancloud-storage --save
# 实时消息服务
$ cnpm install leancloud-realtime --save
```

#### bower 安装

```
# 存储服务（包括推送和统计）
$ bower install leancloud-storage --save
# 实时消息服务
$ bower install leancloud-realtime --save
```
[什么是 bower ?](http://bower.io/)

#### CDN 加速

```html
<script src="https://cdn1.lncld.net/static/js/av-min-1.2.1.js"></script>
```

#### Github 仓库地址

可以直接通过 Github 仓库使用，也可以通过 Github 给我们提出您的建议

- ** 存储服务 leancloud-storage ** Github 仓库地址：[https://github.com/leancloud/javascript-sdk](https://github.com/leancloud/javascript-sdk)
- ** 实时通讯 leancloud-realtime ** Github 仓库地址：[https://github.com/leancloud/js-realtime-sdk](https://github.com/leancloud/js-realtime-sdk)

### ES6 与 ES7 支持

随着 ECMAScript 6 标准的确定（也被称为 ES2015），以及 ECMAScript 7 新草案的不断发布，越来越多人已经开始尝试使用这些新语法来写自己的 JavaScript 程序。如果现阶段打算使用 ES6 直接来写浏览器端程序可能仍然会遇到兼容性问题，更多的是在 Nodejs 环境或通过编译的方式来实现兼容。

目前比较流行的方案是通过 [Babel](http://babeljs.io/) 来实现预编译或构建一个拥有新特性的运行时环境。在所有环境中，都可以通过 babel 将代码编译为相应环境能够支持的代码版本，或者直接编译为 ES5 版本的 JavaScript 代码。在 Nodejs 环境中，可以通过使用 `require hook` 的方式直接载入一个拥有 babel 兼容代码的运行时环境，这样就不需再编译即可在 Nodejs 中直接使用 ES6\ES7，具体配置过程参考 babel 文档。

ES7 中有许多很不错的新语法，其中一个就是 `async/await`。对于异步程序，JavaScript 中一直没有非常优雅的方式去书写，从 callback 到 Promise，目前可以通过 babel 尝试使用 async/await。详情参考 [blog](https://blog.leancloud.cn/3910/)

### TypeScript 支持

伴随着 [Angular2](https://angular.io/) 以及  [ionic@2](http://ionicframework.com/docs/v2/) 的受欢迎，LeanCloud 也针对 JavaScript SDK 编写了一个 `d.ts` 定义文件提供给开发者使用。

本质上，TypeScript 经过编译之后实际上也是调用 JavaScript SDK 的对应的接口，因此在本文代码块中，一些 TypeScript 写法可以给开发者进行参考。

注意，TypeScript 针对异步函数有多种写法，本文以 [Promise](#Promise) 作为默认的示例代码书写方式，仅供参考。
[Promise](#Promise) 以及 TypeScript 中的 [async/await](https://blogs.msdn.microsoft.com/typescript/2015/11/03/what-about-asyncawait/) 的不同写法的支持取决于在 TypeScript 项目中的 `tsconfig.json` 的 `compilerOptions` 配置里面选择 `target` 是什么版本，例如，要支持 [async/await](https://blogs.msdn.microsoft.com/typescript/2015/11/03/what-about-asyncawait/) 需要进行如下配置：

```json
{
  ...
  "compilerOptions": {
    ...
    "target": "es6",
    "module": "commonjs",
    ...
  },
  ...
}
```

注意：因为 TypeScript SDK 是基于 JavaScript SDK 编写的定义文件，因此并不是所有 JavaScript SDK 的接口都有对应 TypeScript 的版本，示例代码会持续更新。

#### 通过 typings 工具安装

首先需要安装 [typings 命令行工具](https://www.npmjs.com/package/typings)

```sh
npm install typings --global
```

然后再执行如下命令即可：

```sh
typings install leancloud-jssdk --save
```

#### 直接引用 d.ts 文件
TypeScript 使用 JavaScript SDK 是通过定义文件来实现调用的，因此我们也将定义文件开源在 GitHub 上，地址是：
[typed-leancloud-jssdk](https://github.com/leancloud/typed-leancloud-jssdk)




### 手动安装

<a class="btn btn-default" target="_blank" href="sdk_down.html">下载 SDK</a>





## 初始化

首先来获取 App ID 以及 App Key。

打开 [控制台 / 设置 / 应用 Key](/app.html?appid={{appid}}#/key)，如下图：


![setting_app_key](images/setting_app_key.png)

如果是在前端项目里面使用 LeanCloud JavaScript SDK，那么可以在页面加载的时候调用一下初始化的函数：

```javascript
var APP_ID = '{{appid}}';
var APP_KEY = '{{appkey}}';
AV.init({
  appId: APP_ID,
  appKey: APP_KEY
});
```
```es7
const appId = '{{appid}}';
const appKey = '{{appkey}}';
AV.init({ appId, appKey });
```



### 启用指定节点

SDK 的初始化方法默认使用**中国大陆节点**，如需切换到 [其他可用节点](#全球节点)，请参考如下用法：


```javascript
var APP_ID = '{{appid}}';
var APP_KEY = '{{appkey}}';
AV.init({
  appId: APP_ID,
  appKey: APP_KEY,
  // 启用美国节点
  region: 'us'
});
```
```es7
const appId = '{{appid}}';
const appKey = '{{appkey}}';
AV.init({
  appId,
  appKey,
  // 启用美国节点
  region: 'us',
});
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


```javascript
var TestObject = AV.Object.extend('TestObject');
var testObject = new TestObject();
testObject.save({
  words: 'Hello World!'
}).then(function(object) {
  alert('LeanCloud Rocks!');
})
```
```es7
const TestObject = AV.Object.extend('TestObject');
const testObject = new TestObject();
await testObject.save({ words: 'Hello World!' });
alert('LeanCloud Rocks!');
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





 
