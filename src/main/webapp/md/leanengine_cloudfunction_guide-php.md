




# 云函数开发指南 &middot; PHP

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

示例项目中 `$PROJECT_DIR/cloud.php` 文件定义了一个很简单的 `hello` 云函数。现在让我们看一个明显较复杂的例子来展示云引擎的用途。在云端进行计算的一个重要理由是，你不需要将大量的数据发送到设备上做计算，而是将这些计算放到服务端，并返回结果这一点点信息就好。

例如，你写了一个应用，让用户对电影评分，一个评分对象大概是这样：

```json
{
  "movie": "夏洛特烦恼",
  "stars": 5,
  "comment": "夏洛一梦，笑成麻花"
}
```

`stars` 表示评分，1-5。如果你想查找《夏洛特烦恼》这部电影的平均分，你可以找出这部电影的所有评分，并在设备上根据这个查询结果计算平均分。但是这样一来，尽管你只是需要平均分这样一个数字，却不得不耗费大量的带宽来传输所有的评分。通过云引擎，我们可以简单地传入电影名称，然后返回电影的平均分。

Cloud 函数接收 JSON 格式的请求对象，我们可以用它来传入电影名称。整个 [PHP SDK](./leanstorage_guide-php.html) 都在云引擎运行环境上有效，可以直接使用，所以我们可以使用它来查询所有的评分。结合在一起，实现 `averageStars` 函数的代码如下：



```php
use \LeanCloud\Engine\Cloud;
use \LeanCloud\Query;
use \LeanCloud\CloudException;

Cloud::define("averageStars", function($params, $user) {
    $query = new Query("Review");
    $query->equalTo("movie", $params["movie"]);
    try {
        $reviews = $query->find();
    } catch (CloudException $ex) {
        // 查询失败, 将错误输出到日志
        error_log($ex->getMessage());
        return 0;
    }
    $sum = 0;
    forEach($reviews as $review) {
        $sum += $review->get("stars");
    }
    if (count($reviews) > 0) {
         return $sum / count($reviews);
    } else {
         return 0;
    }
});
```


### 参数信息


传递给云函数的参数依次为：

* `$params: array`：客户端发送的参数。
* `$user: User`：客户端所关联的用户（根据客户端发送的 `LC-Session` 头）。
* `$meta: array`：有关客户端的更多信息，目前只有一个 `$meta['remoteAddress']` 属性表示客户端的 IP。



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

在云引擎中可以使用 `LeanCloudEngineCloud::run` 调用 `LeanCloudEngineCloud::define` 定义的云函数：


```php
try {
    $result = Cloud::run("averageStars", array("movie" => "夏洛特烦恼"));
} catch (\Exception $ex) {
    // 云函数错误 
}
```

云引擎中默认会直接进行一次本地的函数调用，而不是像客户端一样发起一个 HTTP 请求。PHP 云引擎暂不支持发起 HTTP 请求来调用云函数。



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


错误响应码允许自定义。云引擎抛出的 FunctionError（数据存储 API 会抛出此异常）会直接将错误码和原因返回给客户端。若想自定义错误码，可以自行构造 FunctionError，将 code 与 error 传入。否则 code 为 1， message 为错误对象的字符串形式。

```php
Cloud::define("errorCode", function($params, $user) {
    // 尝试登录一个不存在的用户，会返回 211 错误
    User::logIn("not_this_user", "xxxxxx");
});
```


客户端收到的响应：`{"code":211,"error":"Could not find user"}`


```php
Cloud::define("customErrorCode", function($params, $user) {
    // 返回 123 自定义错误信息
    throw new FunctionError("自定义错误信息", 123);
});
```


客户端收到的响应： `{"code":123,"error":"自定义错误信息"}`


### 云函数超时

云引擎超时时间默认为 30 秒，如果超过阈值，进程将被强制 kill：

* 客户端将收到 HTTP status code 为 50X 响应。
* 服务端会出现类似这样的日志：`WARNING: [pool www] child ... exited on signal 9 (SIGKILL) after ... seconds from start`。



## Hook 函数

Hook 函数本质上是云函数，但它有固定的名称，定义之后会**由系统**在特定事件或操作（如数据保存前、保存后，数据更新前、更新后等等）发生时**自动触发**，而不是由开发者来控制其触发时机。

需要注意：

- 通过控制台进行 [数据导入](dashboard_guide.html#本地数据导入_LeanCloud) 不会触发以下任何 hook 函数。
- 使用 Hook 函数需要 [防止死循环调用](#防止死循环调用)。
- `_Installation` 表暂不支持 Hook 函数。
- Hook 函数只对当前应用的 Class 生效，[对绑定后的目标 Class 无效](app_data_share.html#云引擎_Hook_函数)。

### beforeSave
在创建新对象之前，可以对数据做一些清理或验证。例如，一条电影评论不能过长，否则界面上显示不开，需要将其截断至 140 个字符：



```php
Cloud::beforeSave("Review", function($review, $user) {
    $comment = $review->get("comment");
    if ($comment) {
        if (strlen($comment) > 140) {
            // 截断并添加...
            $review->set("comment", substr($comment, 0, 140) . "...");
        }
    } else {
        // 返回错误，并取消数据保存
        throw new FunctionError("No Comment!", 101);
    }
    // 如果正常返回，则数据会保存
});
```


### afterSave

在创建新对象后触发指定操作，比如当一条留言创建后再更新一下所属帖子的评论总数：



```php
Cloud::afterSave("Comment", function($comment, $user) {
    $query = new Query("Post");
    $post = $query->get($comment->get("post")->getObjectId());
    $post->increment("commentCount");
    try {
        $post->save();
    } catch (CloudException $ex) {
        throw new FunctionError("保存 Post 对象失败: " . $ex->getMessage());
    }
});
```


再如，在用户注册成功之后，给用户增加一个新的属性 from 并保存：



```php
Cloud::afterSave("_User", function($userObj, $currentUser) {
    $userObj->set("from", "LeanCloud");
    try {
        $userObj->save();
    } catch (CloudException $ex) {
        throw new FunctionError("保存 User 对象失败: " . $ex->getMessage());
    }
});
```



如果 `afterSave` 函数调用失败，save 请求仍然会返回成功应答给客户端。`afterSave` 发生的任何错误，都将记录到云引擎日志里，可以到 [控制台 > 存储 > 云引擎 > 日志](https://leancloud.cn/cloud.html?appid={{appid}}#/log) 中查看。


### beforeUpdate


在更新已存在的对象前执行操作，这时你可以知道哪些字段已被修改，还可以在特定情况下拒绝本次修改：



```php
Cloud::beforeUpdate("Review", function($review, $user) {
    // 对象的 updateKeys 字段记录了本次将要修改的字段名列表，
    // 可用于检测并拒绝对某些字段的修改
    if (in_array("comment", $review->updatedKeys) &&
        strlen($review->get("comment")) > 140) {
        throw new FunctionError("comment 长度不得超过 140 个字符");
    }
});
```

**注意：** 不要修改传入的对象 `$review`，因为对它的改动并不会保存到数据库，但可以抛出异常返回一个错误，拒绝这次修改。


**注意**：传入的对象是一个尚未保存到数据库的临时对象，并不保证与最终储存到数据库的对象完全相同，这是因为修改中可能包含自增、数组增改、关系增改等原子操作。


### afterUpdate

在更新已存在的对象后执行特定的动作，比如每次修改文章后记录下日志：



```php
Cloud::afterUpdate("Article", function($article, $user) {
    // 输出日志到控制台
    error_log("Article {$article->getObjectId()} has been updated.");
});
```


### beforeDelete

在删除一个对象之前做一些检查工作，比如在删除一个相册 Album 前，先检查一下该相册中还有没有照片 Photo：



```php
Cloud::beforeDelete("Album", function($album, $user) {
    $query = new Query("Photo");
    $query->equalTo("album", $album);
    try {
        $count = $query->count();
    } catch (CloudException $ex) {
        // Delete 操作会被取消
        throw new FunctionError("Error getting photo count: {$ex->getMessage()}");
    }
    if ($count > 0) {
        // 取消 Delete 操作
        throw new FunctionError("Cannot delete album that has photos.");
    }
});
```


### afterDelete

在被删一个对象后执行操作，例如递减计数、删除关联对象等等。同样以相册为例，这次我们不在删除相册前检查是否还有照片，而是在删除后，同时删除相册中的照片：



```php
Cloud::afterDelete("Album", function($album, $user) {
    $query = new Query("Photo");
    $query->equalTo("album", $album);
    try {
        // 删除相关的 photos
        $photos = $query->find();
        Object::destroyAll($photos);
    } catch (CloudException $ex) {
        throw new FunctionError("删除关联 photos 失败: {$ex->getMessage()}");
    }
});
```


### onVerified

当用户通过邮箱或者短信验证时，对该用户执行特定操作。比如：



```php
Cloud::onVerifed("sms", function($userObj, $meta) {
    error_log("User {$user->getUsername()} verified by SMS");
});
```


函数的第一个参数是验证类型。短信验证为 `sms`，邮箱验证为 `email`。另外，数据库中相关的验证字段，如 `emailVerified` 不需要修改，系统会自动更新。

### onLogin

在用户登录之时执行指定操作，比如禁止在黑名单上的用户登录：



```php
Cloud::onLogin(function($user) {
    error_log("User {$user->getUsername()} is logging in.");
    if ($user->get("blocked")) {
        // 用户无法登录
        throw new FunctionError("Forbidden");
    }
    // 如果正常执行，则用户将正常登录
});
```


### 实时通信 Hook 函数

请阅读 [实时通信概览 &middot; 云引擎 Hook](realtime_v2.html#云引擎_Hook) 来了解以下函数的相关参数和用法。

#### _messageReceived
在消息达到服务器、群组成员已解析完成、发送给收件人之前触发。例如，提前过滤掉聊天内容中的一些广告类的关键词：



```php
Cloud::define("_messageReceived", function($params, $user) {
	// params = {
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

	error_log('_messageReceived start');
	$content = json_decode($params["content"], true);
	$text = $content["_lctext"];
	error_log($text);
    $processedContent = preg_replace("XX中介", "**", $text);
    return array("content" => $processedContent);
});
```


#### _receiversOffline
在消息发送完成时触发、对话中某些用户却已经下线，此时可以根据发送的消息来生成离线消息推送的标题等等。例如截取所发送消息的前 6 个字符作为推送的标题：



```php
Cloud::define('_receiversOffline', function($params, $user) {
	error_log('_receiversOffline start');
	// content 为消息的内容
    $shortContent = $params["content"];
    if (strlen($shortContent) > 6) {
        $shortContent = substr($shortContent, 0, 6);
    }

	$json = array(
        // 自增未读消息的数目，不想自增就设为数字
        "badge" => "Increment",
        "sound" => "default",
        // 使用开发证书
        "_profile" => "dev",
        "alert" => shortContent
    );

	$pushMessage = json_encode($json);
    return array(
        "pushMessage" => $pushMessage,
    );
});
```


#### _messageSent
消息发送完成之后触发，例如消息发送完后，在云引擎中打印一下日志：



```php
Cloud::define('_messageSent', function($params, $user) {
	error_log('_messageSent start');
	error_log('params' . json_encode($params));
    return array();

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
});
```


#### _conversationStart
创建对话，在签名校验（如果开启）之后、实际创建之前触发。例如对话创建时，在云引擎中打印一下日志：



```php
Cloud::define('_conversationStart', function($params, $user) {
	error_log('_conversationStart start');
	error_log('params' . json_encode($params));
    return array();

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
});
```


#### _conversationStarted
创建对话完成触发。例如对话创建之后，在云引擎打印一下日志：



```php
Cloud::define('_conversationStarted', function($params, $user) {
	error_log('_conversationStarted start');
	error_log('params' . json_encode($params));
    return array();

	// 在云引擎中打印的日志如下：
	// _conversationStarted start
	// params {
	// 	convId: '5789a33a1b8694ad267d8040',
	// 	__sign: '1472723167361,f5ceedde159408002fc4edb96b72aafa14bc60bb'
	// }
});
```


#### _conversationAdd
向对话添加成员，在签名校验（如果开启）之后、实际加入之前，包括主动加入和被其他用户加入两种情况都会触发，**注意在创建对话时传入了其他用户的 Client Id 作为 Member 参数，不会触发 _conversationAdd **。例如在云引擎中打印成员加入时的日志：



```php
Cloud::define('_conversationAdd', function($params, $user) {
	error_log('_conversationAdd start');
	error_log('params' . json_encode($params));
    return array();

	// 在云引擎中打印的日志如下：
	// _conversationAdd start
	// params {
	// 	initBy: 'Tom',
	// 	members: ['Mary'],
	// 	convId: '5789a33a1b8694ad267d8040',
	// 	__sign: '1472786231813,a262494c252e82cb7a342a3c62c6d15fffbed5a0'
	// }
});
```


#### _conversationRemove
从对话中踢出成员，在签名校验（如果开启）之后、实际踢出之前触发，用户自己退出对话不会触发。例如在踢出某一个成员时，在云引擎日志中打印出该成员的 Client Id：



```php
Cloud::define('_conversationRemove', function($params, $user) {

	error_log('_conversationRemove start');
	error_log('params' . json_encode($params));
	error_log('removed client Id:' . $params['members'][0]);
    return array();

	// 在云引擎中打印的日志如下：
	// _conversationRemove start
	// params {
	// 	initBy: 'Tom',
	// 	members: ['Jerry'],
	// 	convId: '57c8f3ac92509726c3dadaba',
	// 	__sign: '1472787372605,abdf92b1c2fc4c9820bc02304f192dab6473cd38'
	// }
	//removed client Id: Jerry
});
```


#### _conversationUpdate
修改对话属性、设置或取消对话消息提醒，在实际修改之前触发。例如在更新发生时，在云引擎日志中打印出对话的名称：



```php
Cloud::define('_conversationUpdate', function($params, $user) {
	error_log('_conversationUpdate start');
	error_log('params' . json_encode($params));
    error_log('name' . $params['attr']['name']);
    return array();

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
});
```



#### 防止死循环调用

在实际使用中有这样一种场景：在 `Post` 类的 `afterUpdate` Hook 函数中，对传入的 `Post` 对象做了修改并且保存，而这个保存动作又会再次触发 `afterUpdate`，由此形成死循环。针对这种情况，我们为所有 Hook 函数传入的 `Object` 对象做了处理，以阻止死循环调用的产生。

不过请注意，以下情况还需要开发者自行处理：

- 对传入的 `Object` 对象进行 `fetch` 操作。
- 重新构造传入的 `Object` 对象，如使用 `Object::create()` 方法。

对于使用上述方式产生的对象，请根据需要自行调用以下 API：

- `Object->disableBeforeHook()` 或
- `Object->disableAfterHook()`

这样，对象的保存或删除动作就不会再次触发相关的 Hook 函数。

```php
Cloud::afterUpdate("Post", function($post, $user) {
    // 直接修改并保存对象不会再次触发 after update hook 函数
    $post->set('foo', 'bar');
    $post->save();

    // 如果有 fetch 操作，则需要在新获得的对象上调用相关的 disable 方法
    // 来确保不会再次触发 Hook 函数
    $post->fetch();
    $post->disableAfterHook();
    $post->set('foo', 'bar');
    $post->save();

    // 如果是其他方式构建对象，则需要在新构建的对象上调用相关的 disable 方法
    // 来确保不会再次触发 Hook 函数
    $post = Object::create("Post", $post->getObjectId());
    $post->disableAfterHook();
    $post->save();
});
```



### Hook 函数错误响应码

为 `beforeSave` 这类的 hook 函数定义错误码，需要这样：


```php
Cloud::beforeSave("Review", function($review, $user) {
   $comment = $review->get("comment");
   if (!$comment) {
       throw new FunctionError(json_encode(array(
           "code" => 123,
           "message" => "自定义错误信息",
       )));
   }
});
```


客户端收到的响应为：`Cloud Code validation failed. Error detail : {"code":123, "message": "自定义错误信息"}`，然后通过**截取字符串**的方式取出错误信息，再转换成需要的对象。

### Hook 函数超时

Hook 函数的超时时间为 3 秒。如果 Hook 函数被其他的云函数调用（比如因为 save 对象而触发 `beforeSave` 和 `afterSave`），那么它们的超时时间会进一步被其他云函数调用的剩余时间限制。

例如，如果一个 `beforeSave` 函数是被一个已经运行了 13 秒的云函数触发，那么 `beforeSave` 函数就只剩下 2 秒的时间来运行。同时请参考 [云函数超时](#云函数超时)。





## 定时任务

定时任务可以按照设定，以一定间隔自动完成指定动作，比如半夜清理过期数据，每周一向所有用户发送推送消息等等。定时任务的最小时间单位是**秒**，正常情况下时间误差都可以控制在秒级别。

定时任务是普通的云函数，也会遇到 [超时问题](#云函数超时)，具体请参考 [超时处理方案](#超时的处理方案)。


部署云引擎之后，进入 [控制台 > 存储 > 云引擎 > 定时任务](/cloud.html?appid={{appid}}#/task)，点击 **创建定时器**，然后设定执行的函数名称、执行环境等等。例如定义一个打印循环打印日志的任务 `log_timer`：




```php
Cloud::define("logTimer", function($params, $user) {
    error_log("Log in timer");
});
```



定时器创建后，其状态为**未运行**，需要点击 <span class="label label-default">启用</span> 来激活。之后其执行日志可以通过 [日志](/cloud.html?appid={{appid}}#/log) 查看。


定时任务分为两类：

* 使用 Cron 表达式安排调度
* 以秒为单位的简单循环调度

以 Cron 表达式为例，比如每周一早上 8 点准时发送推送消息给用户：



```php
use \LeanCloud\Push;

Cloud::define("pushTimer", function($params, $user) {
    $push = new Push(array("alert" => "Public message"));
    $push->setChannels(array("Public"));
    $push->send();
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


```php
//参数依次为 AppId, AppKey, MasterKey
use \LeanCloud\Client;
Client::initialize($appId, $appKey, $masterKey);
Client::useMasterKey(true);
```


如果在你的服务端环境里也想做到超级权限，也可以使用该方法初始化。
