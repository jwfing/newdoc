# 升级到云引擎 Node.js SDK 1.0

云引擎 Node.js SDK 1.0 包含有与之前版本不相兼容的修改和重要更新，如废弃了 currentUser、与 Promise/A+ 兼容、JavaScript SDK 升级到 1.0 等等，开发者需要按照以下指导操作，以确保平稳升级到这一版本，在本文末尾还有一份 [升级检查清单](#升级检查清单)。

## 新特性

* 提供了新的初始化方式，可以在初始化时传入参数，详见下文的 [初始化方式](#初始化方式)。
* （细微不兼容）更新至 [JavaScript SDK 1.0](https://github.com/leancloud/javascript-sdk/releases)。
* （不兼容）彻底废弃了 currentUser，默认开启 keepAlive（将会提高并发性能）、兼容了 Node.js 4 内建的 Promise，详见下文的 [废弃 currentUser](#废弃_currentUser)。
* （不兼容）默认启用与 Promise/A+ 兼容的错误处理逻辑，详见下文的 [Promise/A+](#Promise_A_)。
* `cors`, `fetch-user`, `health-check`, `parse-leancloud-headers` 等中间件都被拆分到了单独的文件，可以通过 `var fetchUser = require('leanengine/cors')` 这样的方式使用它们。
* `AV.Cloud.run` 支持了一个 `remote` 选项，可以像 JavaScript SDK 一样通过 HTTP API 来调用云函数，而不是进行一个本地的函数调用。
* `AV.Cloud.define` 支持了一个 `fetchUser` 选项，指定为 false 时该云函数不会去获取用户信息，可以减少 API 调用次数。

我们还为 Node SDK 添加了一份简单的 [API 文档](https://github.com/leancloud/leanengine-node-sdk/blob/master/API.md)。

## 升级到新版本

你可以通过 NPM 安装: `npm install 'leanengine@^1.0.0-beta' --save`，或者在 `package.json` 中添加以下内容：

```json
{
  "dependencies": {
    "leanengine": "^1.0.0-beta"
  }
}
```

## 初始化方式

我们将初始化方式从 `app.use(AV.Cloud)` 改为了 `app.use(AV.express())`，这个修改给未来在初始化方法中传入选项留出了空间；同时你也应该使用 JavaScript SDK 的新初始化方式：

```
var app = require('express')();
var AV = require('leanengine');

AV.init({
  appId: process.env.LEANCLOUD_APP_ID,
  appKey: process.env.LEANCLOUD_APP_KEY,
  masterKey: process.env.LEANCLOUD_APP_MASTER_KEY
});

app.use(AV.express());

app.listen(process.env.LEANCLOUD_APP_PORT);
```

## 废弃 currentUser

云引擎 Node.js SDK 是供云引擎访问 LeanCloud API 的 Node 模块，它内部依赖了 JavaScript SDK. 但和在浏览器中不同，云引擎是一个「多用户」的环境，作为服务器端程序需要同时处理来自不同用户的请求。而 Node.js 本身是异步模型，这些来自不同用户的请求会交织在一起，在同一个全局作用域中运行。

JavaScript SDK 提供了 `AV.User.current()` 和其他一些函数全局地设置或获取用户状态，当（通过 `AV.User.logIn` 或 `AV.User.become`）设置了用户状态，后续的所有请求都会附带上这个用户的 sessionToken, 以便服务器知道以哪个用户的权限去执行这次操作。

在之前的版本中 Node.js SDK 使用了 Node.js 的 [domain](https://nodejs.org/api/domain.html) 模块来模拟全局的用户状态，但在 Node.js 0.12 发布之前官方就已将 domain 模块 [标记为 Deprecated](https://github.com/nodejs/node/issues/66)，但并没有给出替代方案。在之后的 Node.js 版本中，对 domain 的支持时好时坏，ES6 中新增的 Promise 也没有考虑 domain，这样就很容易给云引擎带来隐患。

因此在 Node.js SDK 的 1.0 版本中，我们完全去除了 `AV.User.current()` 等用到全局用户状态的 API, 转而在 express 的 request 和 response 对象上操作用户信息。同时你也需要在所有会发起网络的操作（如保存一个对象）都需要自行传递 sessionToken 作为参数；当然如果你在云引擎中用 `AV.Cloud.useMasterKey()` 关闭了权限检查，那么所有操作都是使用 masterKey 发起的，就不需要传递 sessionToken 了。

在 Node.js SDK 1.0 之后，`AV.User.current()` 永远返回 null 并打印一条警告，如需获取当前请求的用户需要使用 `request.currentUser`：

```javascript
// 云函数
AV.Cloud.define('profile', function(request, response) {
  response.success({
    name: request.currentUser.get('name')
  });
});

// 普通 Express 路由
app.get('/profile', function(request, response) {
  response.json({
    name: request.currentUser.get('name')
  });
});
```

需要注意从 express 的 request 对象上获取 currentUser 和 sessionToken 需要你启用了 CookieSession 中间件：

```javascript
app.use(AV.Cloud.CookieSession({ secret: '<put random string here>', maxAge: 3600000, fetchUser: true }));
```

废弃 currentUser 后，AV.User.logIn 也不会全局地存储用户信息，只是会返回登录后的 User 对象，需要自行用 saveCurrentUser 写入 Cookie 来在请求间保持登录状态，例如实现一个自定义的登录接口：

```javascript
app.get('/login', function(request, response) {
  AV.User.login(request.body.name, request.body.password).then(function(user) {
    response.saveCurrentUser(user);
    response.send();
  });
});
```

在云引擎中如果调用了 `AV.Cloud.useMasterKey()`（通常写在 `server.js` 中），则所有操作都会以 masterKey 的权限来执行（会跳过包括 ACL 在内的规则限制），因此你需要在进行操作前检查用户的权限，例如实现修改文章时需要检查用户是文章的所有者，否则将会存在安全风险：

```javascript
app.post('/updatePost', function(request, response) {
  (new AV.Query('Post')).get(request.body.id).then(function(post) {
    if (request.currentUser.id == post.owner_id) {
      return post.save({
        title: request.body.title
      }).then(function() {
        res.send();
      });
    } else {
      res.sendStatus(403);
    }
  }).catch(console.error);
});
```

如果没有使用 `AV.Cloud.useMasterKey()`，则在进行数据查改时必须在 `AVQuery.find`、`AVObject.fetch`, `AVObject.save` 之类的函数的 options 参数中加入一个 sessionToken, 这个 sessionToken 可以从 request 对象或 user 对象上得到：request 和 user 分别新增了 sessionToken 属性 和 getSessionToken 方法。

```javascript
app.get('/friends', function(request, response) {
  getFriends(request.currentUser).then(function(friends) {
    response.json(friends);
  }, console.error);
});

function getFriends(user, otherOptions) {
  var query = new AV.Query('Friends');
  return query.equalTo('user', user).find({
    sessionToken: user.getSessionToken()
  });
}
```

如果没有传递 sessionToken, 则不向服务器发送 sessionToken, 相当于匿名访问，很有可能出现没有权限的情况。

## Promise/A+

新版本默认启用与 Promise/A+ 兼容的错误处理逻辑，相当于自动执行了 `AV.Promise.setPromisesAPlusCompliant(true);`，Promise 的行为变化包括：

当在一个已经解决的 Promise 上调用 then 时，onFulfilled 和 onRejected 会被添加到事件队列中，异步地被执行（而不是之前同步地执行）：

```javascript
Promise.as('some value').then(function(v) {
  console.log(v);
});

console.log('synchronous code');
```

之前版本的输出：

```
some value
synchronous code
```

1.0 之后的输出：

```
synchronous code
some value
```

当在一个已经解决的 Promise 上调用 then 时，如果 onFulfilled 和 onRejected 抛出了异常，那么会返回一个 rejected 的 Promise（而不是之前同步地抛出一个异常）：

```javascript
try {
  Promise.as('some value').then(function(v) {
    throw new Error('some exception');
  }).then(function() {
    console.log('resolved');
  }, function(err) {
    console.log('rejected', err);
  });
} catch (err) {
  console.log('Caught error', err);
}
```

之前版本的输出：

```
Caught error [Error: some exception]
```

1.0 之后的输出：

```
rejected [Error: some exception]
```

当在一个 rejected 的 Promise 上调用 then 时，如果 onRejected 没有抛出异常也没有返回一个 rejected 的 Promise, 则返回一个 resolved 的 Promise（而不是之前返回一个 rejected 的 Promise）：

```javascript
Promise.error(new Error('some exception')).then(null, function() {
  // do nothings
}).then(function() {
  console.log('resolved');
}, function() {
  console.log('rejected');
});
```

之前版本的输出：

```
rejected
```

1.0 之后的输出：

```
resolved
```

## 升级检查清单

* 改用 `app.use(AV.express())` 来初始化 Node SDK。
* 将所有使用 `AV.User.current` 的地方改为从 `request.currentUser` 获取或直接传递 user 对象。
* 在 Express 中，当调用 `AV.User.logIn`、`AV.User.become`、`AV.Cloud.logInByIdAndSessionToken`、`AV.User.signUp`、`user.signUp` 之后手动调用 `response.saveCurrentUser`。
* 在 Express 中，将所有 `AV.User.logOut` 改为了 `user.logOut` 这样的实例方法，并在之后手动调用 `response.clearCurrentUser()`。
* 在 useMasterKey 的情况下，在所有需要权限的操作处检查了发起请求的客户端的权限。
* 没有 useMasterKey 的情况下，在所有需要权限的网络请求处（`file.destroy`、`Object.saveAll`，`Object.destroyAll`、`object.fetch`、`object.save`、`object.destroy`, `Query.doCloudQuery`、`query.find`、`query.destroyAll`、`query.get`、`query.count`、`query.first`、`searchQuery.find`、`Status.sendStatusToFollowers`、`Status.sendPrivateStatus`、`Status.countUnreadStatuses`、`status.destroy`、`status.send`、`user.follow`、`user.unfollow`、`user.updatePassword`）手工传递来自 `request.sessionToken` 或 `user.getSessionToken` 的 sessionToken.
* 如果依赖于 `AV.File` 的 owner 属性，需要在所有构造 `AV.File` 处的 data 参数中传递 owner.
* 在调用 `AV.Cloud.run` 处传递 user 或 sessionToken 参数。
* 检查是否用到了私有 API（下划线开头，`AV._old_Cloud`、`AV.User._saveCurrentUser`，以及 `process.domain` 等），若有用到请发工单或在论坛联系我们。
* 按照 [Promise/A+](#Promise_A_) 一节，将 Promise 的错误处理逻辑改为符合 Promise/A+ 标准的用法。
