




# 云函数开发指南 &middot; Node.js

云函数是云引擎（LeanEngine）的一个子模块，请确保阅读本文档之前，你已经阅读了 [云引擎服务概览](leanengine_overview.html)。

当你开发移动端应用时，可能会有下列需求：

* 应用在多平台客户端（Android、iOS、Windows Phone、浏览器等）中很多逻辑都是一样的，希望将这部分逻辑抽取出来只维护一份。
* 有些逻辑希望能够较灵活的调整（比如某些个性化列表的排序规则），但又不希望频繁的更新和发布移动客户端。
* 有些逻辑需要的数据量很大，或者运算成本高（比如某些统计汇总需求），不希望在移动客户端进行运算，因为这样会消耗大量的网络流量和手机运算能力。
* 当应用执行特定操作时，由云端系统自动触发一段逻辑（称为 [Hook 函数](#Hook_函数)），比如：用户注册后对该用户增加一些信息记录用于统计；或某业务数据发生变化后希望做一些别的业务操作。这些代码不适合放在移动客户端（比如因为上面提到的几个原因）。
* 需要定时任务，比如每天凌晨清理垃圾注册账号等。

这时，你可以使用云引擎的云函数。云函数是一段部署在服务端的代码，编写 JavaScript 或者 Python 代码，并部署到我们的平台上，可以很好的完成上述需求。

如果还不知道如何创建云引擎项目，本地调试并部署到云端，请阅读 [云引擎快速入门](leanengine_quickstart.html)。

## 多语言支持

云引擎支持多种语言的运行环境，你可以选择自己熟悉的语言开发应用：

- [Node.js](leanengine_cloudfunction_guide-node.html)
- [Python](leanengine_guide-python.html)
- [PHP](leanengine_cloudfunction_guide-php.html)
- [Java](leanengine_cloudfunction_guide-java.html)

## 云函数

示例项目中 `$PROJECT_DIR/cloud.js` 文件定义了一个很简单的 `hello` 云函数。现在让我们看一个明显较复杂的例子来展示云引擎的用途。在云端进行计算的一个重要理由是，你不需要将大量的数据发送到设备上做计算，而是将这些计算放到服务端，并返回结果这一点点信息就好。

例如，你写了一个应用，让用户对电影评分，一个评分对象大概是这样：

```json
{
  "movie": "夏洛特烦恼",
  "stars": 5,
  "comment": "夏洛一梦，笑成麻花"
}
```

`stars` 表示评分，1-5。如果你想查找《夏洛特烦恼》这部电影的平均分，你可以找出这部电影的所有评分，并在设备上根据这个查询结果计算平均分。但是这样一来，尽管你只是需要平均分这样一个数字，却不得不耗费大量的带宽来传输所有的评分。通过云引擎，我们可以简单地传入电影名称，然后返回电影的平均分。

Cloud 函数接收 JSON 格式的请求对象，我们可以用它来传入电影名称。整个 [JavaScript SDK](./leanstorage_guide-js.html) 都在云引擎运行环境上有效，可以直接使用，所以我们可以使用它来查询所有的评分。结合在一起，实现 `averageStars` 函数的代码如下：



```javascript
AV.Cloud.define('averageStars', function(request, response) {
  var query = new AV.Query('Review');
  query.equalTo('movie', request.params.movie);
  query.find().then(function(results) {
    var sum = 0;
    for (var i = 0; i < results.length; i++ ) {
      sum += results[i].get('stars');
    }
    response.success(sum / results.length);
  }).catch(function(error) {
    response.error('查询失败');
  });
});
```


### 参数信息



Request 和 Response 会作为两个参数传入到云函数中：

`Request` 上的属性包括：

* `params: object`：客户端发送的参数对象，当使用 `rpc` 调用时，也可能是 `AV.Object`。
* `currentUser?: AV.User`：客户端所关联的用户（根据客户端发送的 `LC-Session` 头）。
* `meta: object`：有关客户端的更多信息，目前只有一个 `remoteAddress` 属性表示客户端的 IP。
* `sessionToken?: string`：客户端发来的 sessionToken（`X-LC-Session` 头）。

`Response` 上的属性包括：

* `success: function(result?)`：向客户端发送结果，可以是包括 AV.Object 在内的各种数据类型或数组，客户端解析方式见各 SDK 文档。
* `error: function(err?: string)`：向客户端返回一个错误，目前仅支持字符串，`Error` 等类型也会被转换成字符串。


### SDK 调用云函数

LeanCloud 各个语言版本的 SDK 都提供了调用云函数的接口。


```objc
// 在 iOS SDK 中，AVCloud 提供了一系列静态方法来实现客户端调用云函数
// 构建传递给服务端的参数字典
NSDictionary *dicParameters = [NSDictionary dictionaryWithObject:@"夏洛特烦恼"
                                                          forKey:@"movie"];

// 调用指定名称的云函数 averageStars，并且传递参数
[AVCloud callFunctionInBackground:@"averageStars"
                   withParameters:dicParameters
                   block:^(id object, NSError *error) {
                   if(error == nil){
                     // 处理结果
                   } else {
                     // 处理报错
                   }
}];
```
```java
// 在 Android SDK 中，AVCloud 提供了一系列的静态方法来实现客户端调用云函数
// 构建参数
Map<String, String> dicParameters = new HashMap<String, String>();
dicParameters.put("movie", "夏洛特烦恼");

// 调用云函数 averageStars
AVCloud.callFunctionInBackground("averageStars", dicParameters, new FunctionCallback() {
    public void done(Object object, AVException e) {
        if (e == null) {
            // 处理返回结果
        } else {
            // 处理报错
        }
    }
});
```
```js
// 在 JavaScript 中 AV.Cloud 提供了一系列方法来调用云函数
var paramsJson = {
  movie: "夏洛特烦恼"
};
AV.Cloud.run('averageStars', paramsJson).then(function(data) {
  // 调用成功，得到成功的应答 data
}, function(err) {
  // 处理调用失败
});
```
```php
use \LeanCloud\Engine\Cloud;
$params = array(
    "movie" => "夏洛特烦恼"
);
Cloud::run("averageStars", $params);
```
### 通过 REST API 调用云函数
[REST API 调用云函数](rest_api.html#云函数-1) 是 LeanCloud 云端提供的统一的访问云函数的接口，所有的客户端 SDK 也都是封装了这个接口从而实现对云函数的调用。

关于调试工具，我们推荐的工具有：[Postman](http://www.getpostman.com/) 以及 [Paw](https://luckymarmot.com/paw) ，它们可以帮助开发者更方便地调试 Web API。

假设没有以上工具，也可以使用命令行进行调试：

```sh
curl -X POST -H "Content-Type: application/json; charset=utf-8" \
       -H "X-LC-Id: {{appid}}" \
       -H "X-LC-Key: {{appkey}}" \
       -H "X-LC-Prod: 0" \
       -d '{"movie":"夏洛特烦恼"}' \
https://leancloud.cn/1.1/functions/averageStars
```
上述命令行实际上就是向云端发送一个 JSON 对象作为参数，参数的内容是要查询的电影的名字。

### 云引擎调用云函数

在云引擎中可以使用 `AV.Cloud.run` 调用 `AV.Cloud.define` 定义的云函数：



```js
var paramsJson = {
  movie: '夏洛特烦恼',
};
AV.Cloud.run('averageStars', paramsJson).then(function(data) {
  // 调用成功，得到成功的应答 data
}, function(error) {
  // 处理调用失败
});
```

云引擎中默认会直接进行一次本地的函数调用，而不是像客户端一样发起一个 HTTP 请求。如果你希望发起 HTTP 请求来调用云函数，可以传入一个 `remote: true` 的选项（与 success 和 error 回调同级），当你在云引擎之外运行 Node SDK 时这个选项非常有用：

```js
AV.Cloud.run('averageStars', paramsJson).then(function(data) {
  // 成功
}, function(error) {
  // 失败
});
```


### RPC 调用云函数

RPC 调用云函数是指：云引擎会在这种调用方式下自动为 Http Response Body 做序列化，而 SDK 调用之后拿回的返回结果就是一个完整的 `AVObject`。

```objc
NSDictionary *dicParameters = [NSDictionary dictionaryWithObject:@"夏洛特烦恼"
                                                          forKey:@"movie"];

[AVCloud rpcFunctionInBackground:@"averageStars"
                  withParameters:parameters
                  block:^(id object, NSError *error) {
                  if(error == nil){
                     // 处理结果
                  }
                  else {
                     // 处理报错
                  }
}];
```
```java
AVObject movie = new AVObject("Movie");
movie.put("title", "夏洛特烦恼");
movie.save();

AVCloud.rpcFunctionInBackground("averageStars", movie,
    new FunctionCallback<AVObject>() {
      @Override
      public void done(AVObject object, AVException e) {
        Assert.assertNull(e);
      }
    });
```
```php
// PHP 有待支持
```
```javascript
// 假设已经有一个类型为 AV.Object 的 movie 对象
var movie = new AV.Object('Movie');
movie.set('title', '夏洛特烦恼');
movie.save().then(/* ... */);

AV.Cloud.rpc('averageStars', movie).then(function(object) {
  // 成功，如果云函数返回的是一个 AV.Object，回调参数中的 object 也会是一个 AV.Object
}, function(error) {
  // 失败        
});
```

### 切换云引擎环境

专业版云引擎应用有「生产环境」和「预备环境」，切换方法为：

```objc
[AVCloud setProductionMode:NO]; // 调用预备环境
```
```java
AVCloud.setProductionMode(false); // 调用预备环境
```
```php
LeanClient::useProduction(false); // 调用预备环境
```
```javascript
AV.setProduction(false); // 调用预备环境
```

[免费版云引擎应用只有「生产环境」](leanengine_plan.html#免费版) ，因此以上切换方法不适用。


### 云函数错误响应码



错误响应码允许自定义。云引擎方法最终的错误对象如果有 `code` 和 `message` 属性，则响应的 body 以这两个属性为准，否则 `code` 为 1， `message` 为错误对象的字符串形式。比如：

```
AV.Cloud.define('errorCode', function(request, response) {
  AV.User.logIn('NoThisUser', 'lalala', {
    error: function(user, err) {
      response.error(err);
    }
  });
});
```


客户端收到的响应：`{"code":211,"error":"Could not find user"}`



```
AV.Cloud.define('customErrorCode', function(request, response) {
  response.error({code: 123, message: 'custom error message'});
});
```


客户端收到的响应： `{"code":123,"error":"自定义错误信息"}`



### 云函数超时

云函数超时时间为 15 秒，如果超过阈值，[LeanEngine Node.js SDK](https://github.com/leancloud/leanengine-node-sdk) 将强制响应：

* 客户端收到 HTTP status code 为 503 响应，body 为 `The request timed out on the server.`。
* 服务端会出现类似这样的日志：`LeanEngine function timeout, url=/1.1/functions/<cloudFunc>, timeout=15000`。

另外还需要注意：虽然 [LeanEngine Node.js SDK](https://github.com/leancloud/leanengine-node-sdk) 已经响应，但此时云函数可能仍在执行，但执行完毕后的响应是无意义的（不会发给客户端，会在日志中打印一个 `Can't set headers after they are sent` 的异常）。

#### 超时的处理方案

我们建议将代码中的任务转化为异步队列处理，以优化运行时间，避免云函数或 [定时任务](#定时任务) 发生超时。比如：

- 在存储服务中创建一个队列表，包含 `status` 列；
- 接到任务后，向队列表保存一条记录，`status` 值设置为「处理中」，然后直接 response，也可以把队列对象 id 返回，如 `response.success(id);`；
- 当业务处理完毕，根据处理结果更新刚才的队列对象状态，将 `status` 字段设置为「完成」或者「失败」；
- 在任何时候，在控制台通过队列 id 可以获取某个任务的执行结果，判断任务状态。


## Hook 函数

Hook 函数本质上是云函数，但它有固定的名称，定义之后会**由系统**在特定事件或操作（如数据保存前、保存后，数据更新前、更新后等等）发生时**自动触发**，而不是由开发者来控制其触发时机。

需要注意：

- 通过控制台进行 [数据导入](dashboard_guide.html#本地数据导入_LeanCloud) 不会触发以下任何 hook 函数。
- 使用 Hook 函数需要 [防止死循环调用](#防止死循环调用)。
- `_Installation` 表暂不支持 Hook 函数。
- Hook 函数只对当前应用的 Class 生效，[对绑定后的目标 Class 无效](app_data_share.html#云引擎_Hook_函数)。

### beforeSave
在创建新对象之前，可以对数据做一些清理或验证。例如，一条电影评论不能过长，否则界面上显示不开，需要将其截断至 140 个字符：



```javascript
AV.Cloud.beforeSave('Review', function(request, response) {
  var comment = request.object.get('comment');
  if (comment) {
    if (comment.length > 140) {
      // 截断并添加...
      request.object.set('comment', comment.substring(0, 137) + '...');
    }
    // 保存到数据库中
    response.success();
  } else {
    // 不保存数据，并返回错误
    response.error('No comment!');
  }
});
```


### afterSave

在创建新对象后触发指定操作，比如当一条留言创建后再更新一下所属帖子的评论总数：



```javascript
AV.Cloud.afterSave('Comment', function(request) {
  var query = new AV.Query('Post');
  query.get(request.object.get('post').id).then(function(post) {
    post.increment('comments');
    post.save();
  });
});
```


再如，在用户注册成功之后，给用户增加一个新的属性 from 并保存：



```javascript
AV.Cloud.afterSave('_User', function(request) {
  console.log(request.object);
  request.object.set('from','LeanCloud');
  request.object.save().then(function(user)  {
    console.log('ok!');
  });
});
```



如果 `afterSave` 函数调用失败，save 请求仍然会返回成功应答给客户端。`afterSave` 发生的任何错误，都将记录到云引擎日志里，可以到 [控制台 > 存储 > 云引擎 > 日志](https://leancloud.cn/cloud.html?appid={{appid}}#/log) 中查看。


### beforeUpdate


在更新已存在的对象前执行操作，这时你可以知道哪些字段已被修改，还可以在特定情况下拒绝本次修改：



```javascript
AV.Cloud.beforeUpdate('Review', function(request, response) {
  // 如果 comment 字段被修改了，检查该字段的长度
  if (request.object.updatedKeys.indexOf('comment') != -1) {
    if (request.object.get('comment').length <= 140) {
      response.success();
    } else {
      // 拒绝过长的修改
      response.error('comment 长度不得超过 140 字符');
    }
  } else {
    response.success();
  }
});
```

**注意：** 不要修改 `request.object`，因为对它的改动并不会保存到数据库，但可以用 `response.error` 返回一个错误，拒绝这次修改。


**注意**：传入的对象是一个尚未保存到数据库的临时对象，并不保证与最终储存到数据库的对象完全相同，这是因为修改中可能包含自增、数组增改、关系增改等原子操作。


### afterUpdate

在更新已存在的对象后执行特定的动作，比如每次修改文章后记录下日志：



```javascript
AV.Cloud.afterUpdate('Article', function(request) {
  console.log('Updated article,the id is :' + request.object.id);
});
```


### beforeDelete

在删除一个对象之前做一些检查工作，比如在删除一个相册 Album 前，先检查一下该相册中还有没有照片 Photo：



```javascript
AV.Cloud.beforeDelete('Album', function(request, response) {
  //查询Photo中还有没有属于这个相册的照片
  var query = new AV.Query('Photo');
  var album = AV.Object.createWithoutData('Album', request.object.id);
  query.equalTo('album', album);
  query.count().then(function(count) {
    if (count > 0) {
      //还有照片，不能删除，调用error方法
      response.error('Can\'t delete album if it still has photos.');
    } else {
      //没有照片，可以删除，调用success方法
      response.success();
    }
  }, function(error) {
    response.error('Error ' + error.code + ' : ' + error.message + ' when getting photo count.');
  });
});
```


### afterDelete

在被删一个对象后执行操作，例如递减计数、删除关联对象等等。同样以相册为例，这次我们不在删除相册前检查是否还有照片，而是在删除后，同时删除相册中的照片：



```javascript
AV.Cloud.afterDelete('Album', function(request) {
  var query = new AV.Query('Photo');
  var album = AV.Object.createWithoutData('Album', request.object.id);
  query.equalTo('album', album);
  query.find().then(function(posts) {
    //查询本相册的照片，遍历删除
    AV.Object.destroyAll(posts);
  }).then(function(error) {
    console.error('Error finding related comments ' + error.code + ': ' + error.message);
  });
});
```


### onVerified

当用户通过邮箱或者短信验证时，对该用户执行特定操作。比如：



```javascript
AV.Cloud.onVerified('sms', function(request, response) {
    console.log('onVerified: sms, user: ' + request.object);
    response.success();
});
```


函数的第一个参数是验证类型。短信验证为 `sms`，邮箱验证为 `email`。另外，数据库中相关的验证字段，如 `emailVerified` 不需要修改，系统会自动更新。

### onLogin

在用户登录之时执行指定操作，比如禁止在黑名单上的用户登录：



```javascript
AV.Cloud.onLogin(function(request, response) {
  // 因为此时用户还没有登录，所以用户信息是保存在 request.object 对象中
  console.log("on login:", request.object);
  if (request.object.get('username') == 'noLogin') {
    // 如果是 error 回调，则用户无法登录（收到 401 响应）
    response.error('Forbidden');
  } else {
    // 如果是 success 回调，则用户可以登录
    response.success();
  }
});
```


### 实时通信 Hook 函数

请阅读 [实时通信概览 &middot; 云引擎 Hook](realtime_v2.html#云引擎_Hook) 来了解以下函数的相关参数和用法。

#### _messageReceived
在消息达到服务器、群组成员已解析完成、发送给收件人之前触发。例如，提前过滤掉聊天内容中的一些广告类的关键词：



```js
AV.Cloud.define("_messageReceived", (request, response) => {
	// request.params = {
	// 	fromPeer: 'Tom',
	// 	receipt: false,
	// 	groupId: null,
	// 	system: null,
	// 	content: '{"_lctext":"耗子，起床！","_lctype":-1}',
	// 	convId: '5789a33a1b8694ad267d8040',
	// 	toPeers: ['Jerry'],
	// 	__sign: '1472200796787,a0e99be208c6bce92d516c10ff3f598de8f650b9',
	// 	bin: false,
	// 	transient: false,
	// 	sourceIP: '121.239.62.103',
	// 	timestamp: 1472200796764
	// };
	console.log('_messageReceived start');
	let content = JSON.parse(request.params.content);
	let text = content._lctext;
	console.log('text', text);
	let processedContent = text.replace('XX中介', '**');
	// 必须含有以下语句给服务端一个正确的返回，否则会引起异常
	response.success({
		content: processedContent
	});
	console.log('_messageReceived end');
});
```


#### _receiversOffline
在消息发送完成时触发、对话中某些用户却已经下线，此时可以根据发送的消息来生成离线消息推送的标题等等。例如截取所发送消息的前 6 个字符作为推送的标题：



```js
AV.Cloud.define('_receiversOffline', (request, response) => {
	console.log('_receiversOffline start');
	let params = request.params;
	let content = params.content;
	let shortContent = content;
	// params.content 为消息的内容
	if (shortContent.length > 6) {
		shortContent = content.slice(0, 6);
	}
	console.log('shortContent', shortContent);
	let json = {
		// 自增未读消息的数目，不想自增就设为数字
		badge: "Increment",
		sound: "default",
		// 使用开发证书
		_profile: "dev",
		alert: shortContent
	};

	let pushMessage = JSON.stringify(json);

	response.success({
		"pushMessage": pushMessage
	});
	console.log('_receiversOffline end');
});
```


#### _messageSent
消息发送完成之后触发，例如消息发送完后，在云引擎中打印一下日志：



```js
AV.Cloud.define('_messageSent', (request, response) => {
	console.log('_messageSent start');
	let params = request.params;
	console.log('params', params);
	response.success({});
	console.log('_messageSent end');

	// 在云引擎中打印的日志如下：
	// _messageSent start
	// params { fromPeer: 'Tom',
	//   receipt: false,
	//   onlinePeers: [],
	//   content: '12345678',
	//   convId: '5789a33a1b8694ad267d8040',
	//   msgId: 'fptKnuYYQMGdiSt_Zs7zDA',
	//   __sign: '1472703266575,30e1c9b325410f96c804f737035a0f6a2d86d711',
	//   bin: false,
	//   transient: false,
	//   sourceIP: '114.219.127.186',
	//   offlinePeers: [ 'Jerry' ],
	//   timestamp: 1472703266522 }
	// _messageSent end
});
```


#### _conversationStart
创建对话，在签名校验（如果开启）之后、实际创建之前触发。例如对话创建时，在云引擎中打印一下日志：



```js
AV.Cloud.define('_conversationStart', (request, response) => {
	console.log('_conversationStart start');
	let params = request.params;
	console.log('params', params);
	response.success({});
	console.log('_conversationStart end');

	// 在云引擎中打印的日志如下：
	//_conversationStart start
	// params {
	// 	initBy: 'Tom',
	// 	members: ['Tom', 'Jerry'],
	// 	attr: {
	// 		name: 'Tom & Jerry'
	// 	},
	// 	__sign: '1472703266397,b57285517a95028f8b7c34c68f419847a049ef26'
	// }
	//_conversationStart end
});
```


#### _conversationStarted
创建对话完成触发。例如对话创建之后，在云引擎打印一下日志：



```js
AV.Cloud.define('_conversationStarted', (request, response) => {
	console.log('_conversationStarted start');
	let params = request.params;
	console.log('params', params);
	response.success({});
	console.log('_conversationStarted end');
	
	// 在云引擎中打印的日志如下：
	// _conversationStarted start
	// params {
	// 	convId: '5789a33a1b8694ad267d8040',
	// 	__sign: '1472723167361,f5ceedde159408002fc4edb96b72aafa14bc60bb'
	// }
	// _conversationStarted end
});
```


#### _conversationAdd
向对话添加成员，在签名校验（如果开启）之后、实际加入之前，包括主动加入和被其他用户加入两种情况都会触发，**注意在创建对话时传入了其他用户的 Client Id 作为 Member 参数，不会触发 _conversationAdd **。例如在云引擎中打印成员加入时的日志：



```js
AV.Cloud.define('_conversationAdd', (request, response) => {
	console.log('_conversationAdd start');
	let params = request.params;
	console.log('params', params);
	response.success({});
	console.log('_conversationAdd end');

	// 在云引擎中打印的日志如下：
	// _conversationAdd start
	// params {
	// 	initBy: 'Tom',
	// 	members: ['Mary'],
	// 	convId: '5789a33a1b8694ad267d8040',
	// 	__sign: '1472786231813,a262494c252e82cb7a342a3c62c6d15fffbed5a0'
	// }
	// _conversationAdd end
});
```


#### _conversationRemove
从对话中踢出成员，在签名校验（如果开启）之后、实际踢出之前触发，用户自己退出对话不会触发。例如在踢出某一个成员时，在云引擎日志中打印出该成员的 Client Id：



```js
AV.Cloud.define('_conversationRemove', (request, response) => {
	console.log('_conversationRemove start');
	let params = request.params;
	console.log('params', params);
	response.success({});
	console.log('removed client Id:', params.members[0]);
	console.log('_conversationRemove end');

	// 在云引擎中打印的日志如下：
	// _conversationRemove start
	// params {
	// 	initBy: 'Tom',
	// 	members: ['Jerry'],
	// 	convId: '57c8f3ac92509726c3dadaba',
	// 	__sign: '1472787372605,abdf92b1c2fc4c9820bc02304f192dab6473cd38'
	// }
	//removed client Id: Jerry
	// _conversationRemove end
});
```


#### _conversationUpdate
修改对话属性、设置或取消对话消息提醒，在实际修改之前触发。例如在更新发生时，在云引擎日志中打印出对话的名称：



```js
AV.Cloud.define('_conversationUpdate', (request, response) => {
	console.log('_conversationUpdate start');
	let params = request.params;
	console.log('params', params);
	console.log('name', params.attr.name);
	response.success({});
	console.log('_conversationUpdate end');

	// 在云引擎中打印的日志如下：
	// _conversationUpdate start
	// params {
	// 	convId: '57c9208292509726c3dadb4b',
	// 	initBy: 'Tom',
	// 	attr: {
	// 		name: '聪明的喵星人',
	// 		type: 'public'
	// 	},
	// name 聪明的喵星人
	// _conversationUpdate end
});
```




### 防止死循环调用

在实际使用中有这样一种场景：在 `Post` 类的 `afterUpdate` Hook 函数中，对传入的 `Post` 对象做了修改并且保存，而这个保存动作又会再次触发 `afterUpdate`，由此形成死循环。针对这种情况，我们为所有 Hook 函数传入的 `request.object` 对象做了处理，以阻止死循环调用的产生。

不过请注意，以下情况还需要开发者自行处理：

- 对传入的 `request.object` 对象进行 `fetch` 操作。
- 重新构造传入的 `request.object` 对象，如使用 `AV.Object.createWithoutData()` 方法。

对于使用上述方式产生的对象，请根据需要自行调用以下 API：

- `object.disableBeforeHook()` 或
- `object.disableAfterHook()`

这样，对象的保存或删除动作就不会再次触发相关的 Hook 函数。

```javascript
AV.Cloud.afterUpdate('Post', function(request) {
  // 直接修改并保存对象不会再次触发 afterUpdate Hook 函数
  request.object.set('foo', 'bar');
  request.object.save().then(function(obj) {
    // 你的业务逻辑
  }).catch(console.error);

  // 如果有 fetch 操作，则需要在新获得的对象上调用相关的 disable 方法
  // 来确保不会再次触发 Hook 函数
  request.object.fetch().then(function(obj) {
    obj.disableAfterHook();
    obj.set('foo', 'bar');
    return obj.save();
  }).then(function(obj) {
    // 你的业务逻辑
  }).catch(console.error);

  // 如果是其他方式构建对象，则需要在新构建的对象上调用相关的 disable 方法
  // 来确保不会再次触发 Hook 函数
  var obj = AV.Object.createWithoutData('Post', request.object.id);
  obj.disableAfterHook();
  obj.set('foo', 'bar');
  obj.save().then(function(obj) {
    // 你的业务逻辑
  }).catch(console.error);
});
```

**提示**：云引擎 Node.js 环境从 [0.3.0](https://github.com/leancloud/leanengine-node-sdk/blob/master/CHANGELOG.md#v030-20151231) 开始支持 `object.disableBeforeHook()` 和 `object.disableAfterHook()`。




### Hook 函数错误响应码

为 `beforeSave` 这类的 hook 函数定义错误码，需要这样：



```
AV.Cloud.beforeSave('Review', function(request, response) {
  // 使用 JSON.stringify() 将 object 变为字符串
  response.error(JSON.stringify({
    code: 123,
    message: '自定义错误信息'
  }));
});
```


客户端收到的响应为：`Cloud Code validation failed. Error detail : {"code":123, "message": "自定义错误信息"}`，然后通过**截取字符串**的方式取出错误信息，再转换成需要的对象。

### Hook 函数超时

Hook 函数的超时时间为 3 秒。如果 Hook 函数被其他的云函数调用（比如因为 save 对象而触发 `beforeSave` 和 `afterSave`），那么它们的超时时间会进一步被其他云函数调用的剩余时间限制。

例如，如果一个 `beforeSave` 函数是被一个已经运行了 13 秒的云函数触发，那么 `beforeSave` 函数就只剩下 2 秒的时间来运行。同时请参考 [云函数超时](#云函数超时)。



## 在线编写云函数

很多人使用 LeanEngine 是为了在服务端提供一些个性化的方法供各终端调用，而不希望关心诸如代码托管、npm 依赖管理等问题。为此我们提供了在线维护云函数的功能。

使用此功能需要注意：

* 在定义的函数会覆盖你之前用 Git 或命令行部署的项目。
* 目前只能在线编写云函数和 Hook，不支持托管静态网页、编写动态路由。


在 [控制台 > 存储 > 云引擎 > 部署 > 在线编辑](/cloud.html?appid={{appid}}#/deploy/online) 标签页，可以：


* 创建函数：指定函数类型，函数名称，函数体的具体代码，注释等信息，然后「保存」即可创建一个云函数。
* 部署：选择要部署的环境，点击「部署」即可看到部署过程和结果。
* 预览：会将所有函数汇总并生成一个完整的代码段，可以确认代码，或者将其保存为 `cloud.js` 覆盖项目模板的同名文件，即可快速的转换为使用项目部署。
* 维护云函数：可以编辑已有云函数，查看保存历史，以及删除云函数。

**提示**：云函数编辑之后需要重新部署才能生效。


## 定时任务

定时任务可以按照设定，以一定间隔自动完成指定动作，比如半夜清理过期数据，每周一向所有用户发送推送消息等等。定时任务的最小时间单位是**秒**，正常情况下时间误差都可以控制在秒级别。

定时任务是普通的云函数，也会遇到 [超时问题](#云函数超时)，具体请参考 [超时处理方案](#超时的处理方案)。


部署云引擎之后，进入 [控制台 > 存储 > 云引擎 > 定时任务](/cloud.html?appid={{appid}}#/task)，点击 **创建定时器**，然后设定执行的函数名称、执行环境等等。例如定义一个打印循环打印日志的任务 `log_timer`：




```javascript
AV.Cloud.define('log_timer', function(request, response){
  console.log('Log in timer.');
  return response.success();
});
```



定时器创建后，其状态为**未运行**，需要点击 <span class="label label-default">启用</span> 来激活。之后其执行日志可以通过 [日志](/cloud.html?appid={{appid}}#/log) 查看。


定时任务分为两类：

* 使用 Cron 表达式安排调度
* 以秒为单位的简单循环调度

以 Cron 表达式为例，比如每周一早上 8 点准时发送推送消息给用户：



```javascript
AV.Cloud.define('push_timer', function(request, response){
  AV.Push.send({
    channels: ['Public'],
    data: {
      alert: 'Public message'
    }
  });
  return response.success();
});
```


创建定时器的时候，选择 **Cron 表达式** 并填入 `0 0 8 ? * MON`。

### Cron 表达式

Cron 表达式的基本语法为：

```
<秒> <分钟> <小时> <日期 day-of-month> <月份> <星期 day-of-week> <年>
```
位置|字段|约束|取值|可使用的特殊字符
---|---|---|---|---
1|秒|必须|0-59|`, - * /`
2|分钟|必须|0-59|`, - * /`
3|小时|必须|0-23（0 为午夜）|`, - * /`
4|日期|必须|1-31|`, - * ? / L W`
5|月份|必须|1-12、JAN-DEC|`, - * /`
6|星期|必须|1-7、SUN-SAT|`, - * ? / L #`
7|年|可选|空、1970-2099|`, - * /`

特殊字符的用法：

字符|含义|用法
---|---|---
`*`|所有值|代表一个字段的所有可能取值。如将 `<分钟>` 设为 **\***，表示每一分钟。
`?`|不指定值|用于可以使用该字符的两个字段中的一个，在一个表达式中只能出现一次。如任务执行时间为每月 10 号，星期几无所谓，那么表达式中 `<日期>` 设为 **10**，`<星期>` 设为 **?**。
`-`|范围|如 `<小时>` 为 **10-12**，即10 点、11 点、12 点。
`,`|<span class="text-nowrap">分隔多个值</span>|如 `<星期>` 为 **MON,WED,FRI**，即周一、周三、周五。
`/`|增量|如 `<秒>` 设为 **0/15**，即从 0 秒开始，以 15 秒为增量，包括 0、15、30、45 秒；**5/15** 即 5、20、35、50 秒。**\*/** 与 **0/** 等效，如 `<日期>` 设为 **1/3**，即从每个月的第一天开始，每 3 天（即每隔 2 天）执行一次任务。
`L`|最后|其含义随字段的不同而不同。 `<日期>` 中使用 **L** 代表每月最后一天，如 1 月 31 号、2 月 28 日（非闰年）；`<星期>` 中单独使用 **L**，则与使用 **7** 或 **SAT** 等效，若前面搭配其他值使用，如 **6L**，则表示每月的最后一个星期五。<br/><br/>注意，**在 L 之前不要使用多个值或范围**，如 **1,2L**、**1-2L**，否则会产生错误结果。
`W`|weekday|周一到周五的任意一天，离指定日期最近的非周末的那一天。<br/>`<日期>` 为 **15W** 即离 15 号最近的非周末的一天；如果 15 号是周六，任务则会在 14 号周五触发，如果 15 号是周日，则在 16 号周一触发，如果 15 号是周二，则周二当天触发。<br/><br/>`<日期>` 为 **1W**，如果 1 号是周六，任务则会在 3 号周一触发，因为不能向前跨月来计算天数。<br/><br/>在 `<日期>` 中 **W** 之前只能使用一个数值，不能使用多个值或范围。**LW** 可在 `<日期>` 中组合使用，表示每月最后一个非周末的一天。
`#`|第 N 次|如 `<星期>` 为 **6#3** 代表每月第三个周五，**2#1** 为每月头一个周一，**4#5** 为每月第五个周三；如果当月没有第五周，则 **#5** 不会产生作用。

各字段以空格或空白隔开。JAN-DEC、SUN-SAT 这些值不区分大小写，比如 MON 和 mon 效果一样。更详细的使用方法请参考 [Quartz 文档（英文）](http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger) 。

举例如下：

表达式|说明
---|---
`0  0/5 * * * ?`|每隔 5 分钟执行一次
`10 0/5 * * * ?`|每隔 5 分钟执行一次，每次执行都在分钟开始的 10 秒，例如 10:00:10、10:05:10 等等。
<code style="white-space:nowrap;">0 30 10-13 ? * WED,FRI</code>|每周三和每周五的 10:30、11:30、12:30、13:30 执行。
`0 0/30 8-9 5,20 * ?`| 每个月的 5 号和 20 号的 8 点和 10 点之间每隔 30 分钟执行一次，也就是 8:00、8:30、9:00 和 9:30。

### 定时器数量

生产环境和预备环境的定时器数量都限制在 5 个以内，也就是说你总共最多可以创建 10 个定时器。

### 错误信息


定时器执行后的日志会记录在 [控制台 > 存储 > 云引擎 > 其它 > 日志](/cloud.html?appid={{appid}}#/log) 中，以下为常见的错误信息及原因。


- **timerAction timed-out and no fallback available.**<br/>
  某个定时器触发的云函数，因 15 秒内没有响应而超时（可参考 [对云函数调用超时的处理](#超时的处理方案)）。
- **timerAction short-circuited and no fallback available.**<br/>
  某个定时器触发的云函数，因为太多次超时而停止触发。

## 权限说明

云引擎可以有超级权限，使用 Master key 调用所有 API，因此会忽略 ACL 和 Class Permission 限制。你只需要使用下列代码来初始化 SDK（在线定义默认就有超级权限）：



```javascript
//参数依次为 AppId, AppKey, MasterKey
AV.init({
  appId: '{{appid}}',
  appKey: '{{appkey}}',
  masterkey: '{{masterkey}}'
})
AV.Cloud.useMasterKey();
```


如果在你的服务端环境里也想做到超级权限，也可以使用该方法初始化。
