# JavaScript 统计分析开发指南

## 简介

使用本模块，你不仅可以精准统计每个分发渠道所获取的新增用户、活跃用户、留存率等指标，还可以自定义事件来深度追踪用户的使用细节、用户属性以及行为特征，直观解读用户的操作流程，为业务分析和用户体验优化获取最真实的样本数据。

## 特性

* 该 SDK 可以使用在 Web 页面及 WebApp 等场景中，兼容 IE8+，各种移动端浏览器，及各种 WebView，包括 Phonegap、Cordova 和微信的 WebView。

* 使用简单，功能强大。只需要加载，实例化之后，SDK 既可以自动统计，LeanCloud 的后台会自动来分析，最终可以在应用的「分析」中看到诸如访问时长、用户增长、用户留存率和实时在线用户量等各种统计数据。

* 支持 WebApp，支持 Web 前端路由，完全自动统计。

## 通过 bower 安装

```
bower install leancloud-analytics --save
```
安装之后，页面直接加载 bower_components/leancloud-analytics/src/AV.analytics.js 即可。
[什么是 bower ?](http://bower.io/)

## Github 仓库地址

可以直接通过 Github 仓库使用，也可以通过 Github 给我们提出您的建议

Github 仓库地址：[https://github.com/leancloud/js-analytics-sdk](https://github.com/leancloud/js-analytics-sdk)

Release 地址: [https://github.com/leancloud/js-analytics-sdk/releases](https://github.com/leancloud/js-analytics-sdk/releases)

## Demo 及示例代码

如果您觉得一点点阅读文档较慢，可以直接看我们的 [Demo 代码](https://github.com/leancloud/js-analytics-sdk/tree/master/demo)，并且下载自己运行一下试试看。

```javascript
// 最简的示例代码，请换成自己的 appId 和 appKey
var appId = '{{appid}}';
var appKey = '{{appkey}}';

// 实例化统计分析功能（注意：实例化后，SDK 会自动开始统计 PV、UV、停留页面时长等信息）
var analytics = AV.analytics({

    // 设置 AppId
    appId: appId,

    // 设置 AppKey
    appKey: appKey,

    // 你当前应用或者想要指定的版本号（自定义）
    version: '1.8.6',

    // 你当前应用的渠道或者你想指定的渠道（自定义）
    channel: 'weixin'
});

// 发送自定义的统计事件
analytics.send({

    // 事件名称
    event: 'test-event-name',

    // 事件属性，任意数据
    attr: {
        testa: 123,
        testb: 'abc'
    },

    // 该事件持续时间（毫秒）
    duration: 6000
}, function(result) {
    if (result) {
        console.log('统计数据发送成功！');
    }
});

```

## 方法文档

### 全局命名空间

LeanCloud JavaScript 相关 SDK 都会使用「AV」作为命名空间。

### AV.analytics(options)

描述：配置一个统计分析功能，实例化一个 analyticsObject 可以来做后续操作。通过该方法实例化之后，SDK 会自动记录页面时间、一次访问的时间、标记不同用户等数据，LeanCloud 服务端会每天处理一次所有统计数据，自动计算出你想要的关键数据。

参数：

* options {Object} （必须） 配置统计分析中所需的参数。其中包括：

参数|类型|约束|说明
---|---|---|:---
appId|String|必须|应用的 AppId，在「控制台」-「设置」-「基本信息」中可以查看。
appKey|String|必须|应用的 AppKey
version|String|可选|可以设置一个版本号，可以是当前应用的版本，完全自定义。
channel|String|可选|渠道信息，可以设置一个渠道，完全自定义，比如微信、微博等。

返回：{Object} 返回 analyticsObject，可以做后续的方法，支持链式。

例子：

```javascript
var appId = '{{appid}}';
var appKey = '{{appkey}}';
var analytics = AV.analytics({
    appId: appId,
    appKey: appKey,
    // 你当前应用或者想要指定的版本号（自定义）
    version: '1.8.6',
    // 你当前应用的渠道或者你想指定的渠道（自定义）
    channel: 'weixin'
});
```

### AV.analytics.version

描述：获取当前 SDK 的版本信息

返回：{String} 返回当前版本

例子：

```javascript
console.log(AV.analytics.version);   // 0.0.1
```

### analyticsObject.send(options, callback)

描述：发送自定义事件，可以用来监测用户行为，或者做其他相关统计。

参数：

* options {Object} （必须）发送数据的配置，具体参数包括：

参数|类型|约束|说明
---|---|---|:---
event|String|必须|事件的名称
attr|Object|可选|事件所携带的数据，可以是任意的 JSON，完全自定义
duration|Number|可选|该事件持续的时间，单位是毫秒

* callback {Function}（可选）自定义发送成功或者失败后，会触发的回调函数

返回：{Object} 返回 analyticsObject，可以做后续的方法，支持链式。

例子：

```javascript
// 最简的示例代码，请换成自己的 appId 和 appKey
var appId = '{{appid}}';
var appKey = '{{appkey}}';

// 实例化统计分析功能
var analytics = AV.analytics({
    appId: appId,
    appKey: appKey,
    version: '1.8.6',
    channel: 'weixin'
}).send({

    // 事件名称
    event: 'test-event-name',

    // 事件属性，任意数据
    attr: {
        testa: 123,
        testb: 'abc'
    },

    // 该事件持续时间（毫秒）
    duration: 6000
}, function(result) {
    if (result) {
        console.log('统计数据发送成功！');
    }
});
```

### analyticsObject.send(eventList, callback)

描述：发送自定义事件，可以用来监测用户行为，或者做其他相关统计。

参数：

* eventList {Array} （必须）发送事件列表，每个事件单元的说明，请参考上一个 send 方法的 options 选项；

* callback {Function}（可选）自定义发送成功或者失败后，会触发的回调函数

返回：{Object} 返回 analyticsObject，可以做后续的方法，支持链式。

例子：

```javascript
// 最简的示例代码，请换成自己的 appId 和 appKey
var appId = '{{appid}}';
var appKey = '{{appkey}}';

var eventList = [
    {
        // 事件名称
        event: 'test-event-name',

        // 事件属性，任意数据
        attr: {
            testa: 123,
            testb: 'abc'
        },

        // 该事件持续时间（毫秒）
        duration: 6000
    },
    {
        event: 'daasdfname',
        duration: 2100
    }
];

// 实例化统计分析功能
var analytics = AV.analytics({
    appId: appId,
    appKey: appKey,
    version: '1.8.6',
    channel: 'weixin'
}).send(eventList, function(result) {
    if (result) {
        console.log('统计数据发送成功！');
    }
});
```

## 数据时效性

在控制台的 **分析** 页面中，有些报告可以展示实时数据，有些报告则依靠内部离线数据进行分析，因此有时你会看不到当天的数据。

如果当前页面中存在 **日期选择** 选项（通常在页面右上角），你可以以此判断当前的统计结果是否有延迟。如果 **结束日期** 显示为 **当日日期** 或在其下拉菜单中有「今日」选项，即为实时数据；反之则为离线数据（如下图所示），要推迟一天才能看到当日的情况。

<img src="images/analytics_datepicker_for_offline_data.png" alt="" width="231" height="256">
