



# 网站托管开发指南 &middot; Node.js

网站托管是云引擎的一个子模块，允许你用 Node.js 开发一个 Web 程序，提供云函数和 Hook，还可以提供静态文件的托管和自定义的路由、绑定你自己的域名。你可以用它为你的移动应用提供一个介绍和下载页、开发一个管理员控制台或完整的网站，或者运行一些必须在服务器端运行的自定义逻辑。

如果你还不知道如何创建云引擎项目，本地调试并部署到云端，可以先阅读一下 [云引擎快速入门](leanengine_quickstart.html)。

在阅读本文档的同时，你还可以通过 [云引擎服务概览](leanengine_overview.html) 了解云引擎的全部功能、通过 [云引擎命令行工具使用详解](leanengine_cli.html) 了解命令行工具的用法、通过 [LeanCache 使用指南](leancache_guide.html) 了解云引擎提供的内存缓存服务，还可以在 [云引擎项目示例](leanengine_examples.html) 找到一些云引擎的示例项目，遇到问题时可以先检索一下 [云引擎常见问题和解答](leanengine_faq.html)。

这篇文档以 Node.js 为例，但云引擎还支持其他多种语言，你可以选择自己熟悉的技术栈进行开发：

- [Node.js](leanengine_webhosting_guide-node.html)
- [Python](leanengine_guide-python.html)
- [PHP](leanengine_webhosting_guide-php.html)
- [Java](leanengine_webhosting_guide-java.html)

## 项目骨架


<script>$(window).load(function() {$('.do-expand-all').click();})</script>

你的项目根目录项目 **必须** 有一个 `package.json` 文件，才会正确地被云引擎识别为 Node.js 项目。

<div class="callout callout-info">因为一些历史遗留问题，请确保你的项目中 **没有** 名为 `cloud/main.js` 的文件。</div>

### package.json

Node.js 的 `package.json` 中可以指定 [很多选项](https://docs.npmjs.com/files/package.json)，它通常看起来是这样：

```json
{
    "name": "node-js-getting-started",
    "scripts": {
        "start": "node server.js"
    },
    "engine": {
        "node": "4.x"
    },
    "dependencies": {
        "express": "4.12.3",
        "leanengine": "1.2.2"
    }
}
```

其中云引擎会尊重的选项包括：

* `scripts.start` 启动项目时使用的命令；默认为 `node server.js`，如果你希望为 node 附加启动选项（如 `--es_staging`）或使用其他的文件作为入口点，可以修改该选项。
* `scripts.prepublish` 会在项目部署最后运行一次；可以将构建命令（如 `gulp build`）写在这里。
* `engines.node` 指定所需的 Node.js 版本；出于兼容性考虑默认版本仍为比较旧的 `0.12`，**因此建议大家自行指定一个更高的版本，建议使用 `4.x` 版本进行开发**，你也可以设置为 `*` 表示总是使用最新版本的 Node.js。
* `dependencies` 项目所依赖的包；云引擎会在部署时用 `npm install --production` 为你安装这里列出的所有依赖。
* `devDependencies` 项目开发时所依赖的包；云引擎目前 **不会** 安装这里的依赖。

建议你参考我们的 [项目模板](https://github.com/leancloud/node-js-getting-started/blob/master/package.json) 来编写自己的 `package.json`。








### 健康监测

你的应用在启动时，云引擎的管理程序会每秒去检查你的应用是否启动成功，如果 **30 秒** 仍未启动成功，即认为启动失败；在之后应用正常运行的过程中，也会有定期的「健康监测」，以确保你的应用正常运行，如果健康监测失败，云引擎管理程序会自动重启你的应用。

健康检查的 URL 包括你的应用首页（`/`）和 Node.js SDK 负责处理的 `/__engine/1/ping`，只要 **两者之一** 返回了 HTTP 200 的响应，就视作成功。因此请确保你的程序使用了 Node.js SDK，或你的应用 **首页能够正常地返回 HTTP 200** 响应。

除此之外，为了支持云引擎的云函数和 Hook 功能，管理程序会使用 `/1.1/functions/_ops/metadatas` 这个 URL 和 Node.js SDK 交互，请确保将这个 URL 交给 Node.js SDK 处理，或 **返回一个 HTTP 404 表示不使用云函数** 和 Hook 相关的功能。


关于如何加载 Node.js SDK，见下面的 [Web 框架](#Web 框架) 和 [LeanCloud SDK](#LeanCloud SDK) 小节。


## Web 框架


Node SDK 为 [express](http://expressjs.com/) 和 [koa](http://koajs.com/) 提供了集成支持，如果你使用这两个框架，只需通过下面的方式加载 Node SDK 提供的中间件即可。

你可以在你的项目根目录运行 `npm install leanengine@next --save` 来安装 Node SDK。

### Express

```js
var express = require('express');
var AV = require('leanengine');

AV.init({
  appId: process.env.LEANCLOUD_APP_ID || '{{appid}}',
  appKey: process.env.LEANCLOUD_APP_KEY || '{{appkey}}',
  masterKey: process.env.LEANCLOUD_APP_MASTER_KEY || '{{masterkey}}'
});

var app = express();
app.use(AV.express());
app.listen(process.env.LEANCLOUD_APP_PORT);
```

你可以使用 express 的路由定义功能来提供自定义的 HTTP API：

```js
app.get('/', function(req, res) {
  res.render('index', {title: 'Hello world'});
});

app.get('/time', function(req, res) {
  res.json({
    time: new Date()
  });
});

app.get('/todos', function(req, res) {
  new AV.Query('Todo').find().then(function(todos) {
    res.json(todos);
  }).catch(function(err) {
    res.status(500).json({
      error: err.message
    });
  });
});

```

更多最佳实践请参考我们的 [项目模板](https://github.com/leancloud/node-js-getting-started) 和 [云引擎项目示例](leanengine_examples.html)。

### Koa

```js
var koa = require('koa');
var AV = require('leanengine');

AV.init({
  appId: process.env.LEANCLOUD_APP_ID || '{{appid}}',
  appKey: process.env.LEANCLOUD_APP_KEY || '{{appkey}}',
  masterKey: process.env.LEANCLOUD_APP_MASTER_KEY || '{{masterkey}}'
});

var app = koa();
app.use(AV.koa());
app.listen(process.env.LEANCLOUD_APP_PORT);
```

你可以使用 koa 来渲染页面、提供自定义的 HTTP API：

```js
app.use(function *(next) {
  if (this.url === '/') {
    // https://github.com/tj/co-views
    yield coViews('views')('index', {title: 'Hello world'});
  } else {
    yield next;
  }
});

app.use(function *(next) {
  if (this.url === '/time') {
    this.body = {
      time: new Date()
    };
  } else {
    yield next;
  }
});

app.use(function *(next) {
  if (this.url === '/todos') {
    return new AV.Query('Todo').find().then(todos => {
      this.body = todos;
    });
  } else {
    yield next;
  }
});

```

使用 Koa 时建议按照前面 [package.json](#package_json) 一节将 Node.js 的版本设置为 `4.x` 以上。

### 其他 Web 框架

你也可以使用其他的 Web 框架进行开发，但你需要自行去实现 [健康监测](#健康监测) 中提到的逻辑。下面是一个使用 Node.js 内建的 [http](https://nodejs.org/api/http.html) 实现的最简示例，可供参考：

```js
require('http').createServer(function(req, res) {
  if (req.url == '/') {
    res.statusCode = 200;
    res.end();
  } else {
    res.statusCode = 404;
    res.end();
  }
}).listen(process.env.LEANCLOUD_APP_PORT);
```


## LeanCloud SDK


云引擎中的 Node SDK 是对 [JavaScript 存储 SDK](https://github.com/leancloud/javascript-sdk) 的拓展，增加了服务器端需要的云函数和 Hook 相关支持，在云引擎中你需要用 `leanengine` 这个包来操作 [LeanCloud 的存储服务](leanstorage_guide-js.html) 中的数据，你可以在你的项目根目录运行 `npm install leanengine@next --save` 来安装 Node SDK。

Node SDK 的 [API 文档](https://github.com/leancloud/leanengine-node-sdk/blob/master/API.md) 和 [更新日志](https://github.com/leancloud/leanengine-node-sdk/releases) 都在 GitHub 上。

```js
var AV = require('leanengine');

AV.init({
  appId: process.env.LEANCLOUD_APP_ID || '{{appid}}',
  appKey: process.env.LEANCLOUD_APP_KEY || '{{appkey}}',
  masterKey: process.env.LEANCLOUD_APP_MASTER_KEY || '{{masterkey}}'
});

// 你可以使用 useMasterKey 在云引擎中开启 masterKey 权限，将会跳过 ACL 和其他权限限制。
AV.Cloud.useMasterKey();

// 使用 JavaScript 的 API 查询云存储中的数据。
new AV.Query('Todo').find().then(function(todos) {
  console.log(todos);
}).catch(function(err) {
  console.log(err)
});
```

Node SDK 有过两个大版本：

* `0.x`：最初的版本，对 Node.js 4.x 及以上版本兼容不佳，建议用户参考 [升级到云引擎 Node.js SDK 1.0](leanengine-node-sdk-upgrade-1.html) 来更新
* `1.x`：**推荐使用** 的版本，彻底废弃了全局的 currentUser，依赖的 JavaScript 也升级到了 1.x 分支，支持了 Koa 和 Node.js 4.x 及以上版本。

## 本地运行和调试

在你首次启动应用之前需要先安装依赖：

```sh
npm install
```

然后便可以在项目根目录，用我们的命令行工具来启动本地调试了：

```sh
lean up
```

更多有关命令行工具和本地调试的内容请看 [云引擎命令行工具使用详解](leanengine_cli.html)。



## 部署

### 命令行部署

在你的项目根目录运行：

```sh
lean deploy
```

使用命令行工具可以非常方便地部署、发布应用，查看应用状态，查看日志，甚至支持多应用部署。具体使用请参考 [命令行工具指南](leanengine_cli.html)。

### Git 部署

除此之外，还可以使用 git 仓库部署。你需要将项目提交到一个 git 仓库，我们并不提供源码的版本管理功能，而是借助于 git 这个优秀的分布式版本管理工具。我们推荐你使用 [GitHub](https://github.com/)、[Coding](https://coding.net/) 或者 [OSChina](http://git.oschina.net/) 这样第三方的源码托管网站，也可以使用你自己搭建的 git 仓库（比如 [Gitlab](http://gitlab.org/)）。

你需要先在这些平台上创建一个项目（如果已有代码，请不需要选择「Initialize this repository with a README」），在网站的个人设置中填写本地机器的 SSH 公钥（以 GitHub 为例，在 Settings => SSH and GPG keys 中点击 New SSH key），然后在项目目录执行：

```sh
git remote add origin git@github.com:<username>/<repoName>.git
git push -u origin master
```

然后到云引擎的设置界面填写你的 Git 仓库地址，如果是公开仓库建议填写 https 地址，例如 `https://github.com/<username>/<repoName>.git`。

如果是私有仓库需要填写 ssh 地址 `git@github.com:<username>/<repoName>.git`，还需要你将云引擎分配给你的公钥填写到第三方托管平台的 Deploy keys 中，以 GitHub 为例，在项目的 Settings => Deploy keys 中点击 Add deploy key。

设置好之后，今后需要部署代码时就可以在云引擎的部署界面直接点击「部署」了，默认会部署 master 分支的代码，你也可以在部署时填写分支、标签或具体的 Commit。

### 预备环境和生产环境
对于免费版应用，云引擎只有一个「生产环境」，对应的域名是 `{应用的域名}.leanapp.cn`。

升级到专业版后会有一个额外的「预备环境」，对应域名 `stg-{应用的域名}.leanapp.cn`，两个环境所访问的都是同样的数据，你可以用预备环境测试你的云引擎代码，每次修改先部署到预备环境，测试通过后再发布到生产环境；如果你希望有一个独立数据源的测试环境，建议单独创建一个应用。

<div class="callout callout-info">如果访问云引擎遇到「No Application Configured」的错误，通常是因为对应的环境还没有部署代码。例如免费版应用没有预备环境，或专业版应用尚未发布代码到生产环境。</div>

关于免费版和专业版的更多差别，请参考 [云引擎运行方案](leanengine_plan.html)。

有些时候你可能需要知道当前云引擎运行在什么环境（开发环境、预备环境或生产环境），从而做不同的处理：


```javascript
var NODE_ENV = process.env.NODE_ENV || 'development';
if (NODE_ENV === 'development') {
  // 当前环境为「开发环境」，是由命令行工具启动的
} else if(NODE_ENV == 'production') {
  // 当前环境为「生产环境」，是线上正式运行的环境
} else {
  // 当前环境为「预备环境」
}
```


在客户端 SDK 调用云函数时，可以通过 REST API 的特殊的 HTTP 头 `X-LC-Prod` 来区分调用的环境。

* `X-LC-Prod: 0` 表示调用预备环境
* `X-LC-Prod: 1` 表示调用生产环境

<div class="callout callout-info">客户端 SDK 都有类似于 `setProduction` 的方法，比如 [JavaScript SDK API 的 AV.setProduction(production)](/api-docs/javascript/symbols/AV.html#.setProduction)，其中 `production` 设置为 `0` 则该 SDK 将请求预备环境；设置为 `1` 将请求生产环境，默认为 `1`。</div>

### 设置域名

你可以在 [云引擎 > 设置](/cloud.html?appid={{appid}}#/conf) 的「Web 主机域名」部分，填写一个自定义的二级域名，例如你设置了 `myapp`，那么你就可以通过我们的二级域名来访问你的网站了：

- <http://myapp.leanapp.cn>（中国区）
- <http://myapp.avosapps.us>（美国区）

<div class="callout callout-info">你可能需要至多几个小时的时间来等待 DNS 生效。</div>

## 用户状态管理


云引擎提供了一个 `AV.Cloud.CookieSession` 中间件，用 Cookie 来维护用户（`AV.User`）的登录状态，要使用这个中间件可以在 `app.js` 中添加下列代码：

```javascript
app.use(AV.Cloud.CookieSession({ secret: 'my secret', maxAge: 3600000, fetchUser: true }));
```

Koa 需要添加一个 `framework: 'koa'` 的参数：

```javascript
app.use(AV.Cloud.CookieSession({ framework: 'koa', secret: 'my secret', maxAge: 3600000, fetchUser: true }));
```

你需要传入一个 secret 用于签名 Cookie（必须提供），这个中间件会将 `AV.User` 的登录状态信息记录到 Cookie 中，用户下次访问时自动检查用户是否已经登录，如果已经登录，可以通过 `req.currentUser` 获取当前登录用户。

`AV.Cloud.CookieSession` 支持的选项包括：

* **fetchUser**：**是否自动 fetch 当前登录的 AV.User 对象。默认为 false。**
  如果设置为 true，每个 HTTP 请求都将发起一次 LeanCloud API 调用来 fetch 用户对象。如果设置为 false，默认只可以访问 `req.currentUser` 的 `id`（`_User` 表记录的 ObjectId）和 `sessionToken` 属性，你可以在需要时再手动 fetch 整个用户。
* **name**：Cookie 的名字，默认为 `avos.sess`。
* **maxAge**：设置 Cookie 的过期时间。

在 Node SDK 1.x 之后我们不再允许通过 `AV.User.current()` 获取登录用户的信息（详见 [升级到云引擎 Node.js SDK 1.0](leanengine-node-sdk-upgrade-1.html#废弃_currentUser)），而是需要你：

* 在云引擎方法中，通过 `request.currentUser` 获取用户信息。
* 在网站托管中，通过 `request.currentUser` 获取用户信息。
* 在后续的方法调用显示传递 user 对象。

你可以这样简单地实现一个具有登录功能的站点：

```javascript
// 处理登录请求（可能来自登录界面中的表单）
app.post('/login', function(req, res) {
  AV.User.logIn(req.body.username, req.body.password).then(function(user) {
    res.saveCurrentUser(user); // 保存当前用户到 Cookie
    res.redirect('/profile'); // 跳转到个人资料页面
  }, function(error) {
    //登录失败，跳转到登录页面
    res.redirect('/login');
  });
})

// 查看个人资料
app.get('/profile', function(req, res) {
  // 判断用户是否已经登录
  if (req.currentUser) {
    // 如果已经登录，发送当前登录用户信息。
    res.send(req.currentUser);
  } else {
    // 没有登录，跳转到登录页面。
    res.redirect('/login');
  }
});

// 登出账号
app.get('/logout', function(req, res) {
  req.currentUser.logOut();
  res.clearCurrentUser(); // 从 Cookie 中删除用户
  res.redirect('/profile');
});
```


## 实现常见功能

### 发送 HTTP 请求



推荐使用 [request](https://www.npmjs.com/package/request) 这个第三方模块来完成 HTTP 请求。

安装 request:

```sh
npm install request --save
```

代码示例：

```javascript
var request = require('request');

request({
  method: 'POST',
  url: 'http://www.example.com/create_post',
  json: {
    title: 'Vote for Pedro',
    body: 'If you vote for Pedro, your wildest dreams will come true'
  }
}, function(err, res, body) {
  if (err) {
    console.error('Request failed with response code ' + res.statusCode);
  } else {
    console.log(body);
  }
});
```


### 获取客户端 IP

如果你想获取客户端的 IP，可以直接从用户请求的 HTTP 头的 `x-real-ip` 字段获取，实例代码如下：


```js
app.get('/', function(req, res) {
  console.log(req.headers['x-real-ip']);// 打印 IP 地址
  res.render('index', { currentTime: new Date() });
});
```


### 文件上传
托管在 云引擎 的网站项目可以直接使用内置的 LeanCloud JavaScript SDK 的 API 文件相关的接口直接处理文件的上传。

假设前端 HTML 代码如下：

```html
<form enctype="multipart/form-data" method="post" action="/upload">
  <input type="file" name="iconImage">
  <input type="submit" name="submit" value="submit">
</form>
```


然后配置应用使用 [multiparty](https://www.npmjs.com/package/multiparty) 中间件：

```javascript
var multiparty = require('multiparty');
```


接下来定义文件上传的处理函数，构建一个 Form 对象，并将 req 作为参数进行解析，会将请求中的文件保存到临时文件目录，并构造 files 对象：


```javascript
var fs = require('fs');
app.post('/upload', function(req, res){
  var form = new multiparty.Form();
  form.parse(req, function(err, fields, files) {
    var iconFile = files.iconImage[0];
    if(iconFile.size !== 0){
      fs.readFile(iconFile.path, function(err, data){
        if(err) {
          return res.send('读取文件失败');
        }
        var theFile = new AV.File(iconFile.originalFilename, data);
        theFile.save().then(function(theFile){
          res.send('上传成功！');
        }).catch(console.error);
      });
    } else {
      res.send('请选择一个文件。');
    }
  });
});
```


### Session


有时候你需要将一些自己需要的属性保存在 session 中，你可以增加通用的 `cookie-session` 组件，详情可以参考 [express.js &middot; cookie-session](https://github.com/expressjs/cookie-session)。该组件和 `AV.Cloud.CookieSession` 组件可以并存。

注意：express 框架的 `express.session.MemoryStore` 在我们云引擎中是无法正常工作的，因为我们的云引擎是多主机、多进程运行，因此内存型 session 是无法共享的，建议用 [express.js &middot; cookie-session 中间件](https://github.com/expressjs/cookie-session)。


### LeanCache


首先添加相关依赖到 `package.json` 中：

```json
"dependencies": {
  ...
  "redis": "2.2.x",
  ...
}
```

然后可以使用下列代码获取 Redis 连接：

```js
var client = require('redis').createClient(process.env['REDIS_URL_<实例名称>']);
// 建议增加 client 的 on error 事件处理，否则可能因为网络波动或 redis server 主从切换等原因造成短暂不可用导致应用进程退出。
client.on('error', function(err) {
  return console.error('redis err: %s', err);
});
```


关于 LeanCache 的更多使用方法请看 [LeanCache 使用指南](leancache_guide.html)。

### 重定向到 HTTPS

为了安全性，我们可能会为网站加上 HTTPS 加密传输。我们的 云引擎 支持网站托管，同样会有这样的需求。

因此我们在 云引擎 中提供了一个新的 middleware 来强制让你的 `{应用的域名}.leanapp.cn` 的网站通过 https 访问，你只要这样：


Express:

```javascript
app.enable('trust proxy');
app.use(AV.Cloud.HttpsRedirect());
```

Koa:

```javascript
app.proxy = true;
app.use(AV.Cloud.HttpsRedirect({framework: 'koa'}));
```


部署并发布到生产环境之后，访问你的 云引擎 网站二级域名都会强制通过 HTTPS 访问。

## 线上环境

### 环境变量

云引擎平台默认提供下列环境变量供应用使用：

变量名|说明
---|---
`LEANCLOUD_APP_ID`|当前应用的 App Id
`LEANCLOUD_APP_KEY`|当前应用的 App Key
`LEANCLOUD_APP_MASTER_KEY`|当前应用的 Master Key
`LEANCLOUD_APP_ENV`|当前的应用环境：<ul><li>开发环境没有该环境变量，或值为 `development`（一般指本地开发）</li><li>预备环境值为 `stage`</li><li>生产环境值为 `production`</li></ul>
`LEANCLOUD_APP_PORT`|当前应用开放给外网的端口，只有监听此端口，用户才可以访问到你的服务。
`LEANCLOUD_APP_INSTANCE`|云引擎实例名称，在多实例环境可以通过此变量标示自己。
`LEANCLOUD_REGION`|云引擎服务所在区域，值为 `CN` 或 `US`，分别表示国内节点和美国节点。

<div class="callout callout-info">旧版云引擎使用的以 `LC_` 开头的环境变量（如 `LC_APP_ID`）已经被弃用。为了保证代码兼容性，`LC_` 变量在一段时间内依然有效，但未来可能会完全失效。为了避免报错，建议使用 `LEANCLOUD_` 变量来替换。</div>

你也可以在 [云引擎 > 设置](/cloud.html?appid={{appid}}#/conf) 页面中添加自定义的环境变量。其中名字必须是字母、数字、下划线且以字母开头，值必须是字符串，修改环境变量后会在下一次部署时生效。

按照一般的实践，可以将一些配置项存储在环境变量中，这样可以在不修改代码的情况下，修改环境变量并重新部署，来改变程序的行为；或者可以将一些第三方服务的 Secret Key 存储在环境变量中，避免这些密钥直接出现在代码中。


```javascript
// 在云引擎 Node.js 环境中使用自定义的环境变量
var MY_CUSTOM_VARIABLE = process.env.MY_CUSTOM_VARIABLE;
console.log(MY_CUSTOM_VARIABLE);
```


### 日志

在控制台的 [云引擎 / 日志](/cloud.html?appid={{appid}}#/log) 中可以查看云引擎的部署和运行日志，还可以通过日志级别进行筛选。

应用的日志可以直接输出到「标准输出」或者「标准错误」，这些信息会分别对应日志的 `info` 和 `error` 级别，比如下列代码会在 info 级别记录参数信息：


```javascript
console.log('hello');
console.error('some error!');
```


<div class="callout callout-info">日志单行最大 4096 个字符，多余部分会被丢弃；日志输出频率大于 600 行/分钟，多余的部分会被丢弃。</div>


你可以通过设置一个 `DEBUG=leancloud:request` 的环境变量来打印由 LeanCloud SDK 发出的网络请求。在本地调试时你可以通过这样的命令启动程序：

```sh
env DEBUG=leancloud:request lean up
```

当有对 LeanCloud 的调用时，你可以看到类似这样的日志：

```
leancloud:request request(0) +0ms GET https://api.leancloud.cn/1.1/classes/Todo?&where=%7B%7D&order=-createdAt { where: '{}', order: '-createdAt' }
leancloud:request response(0) +220ms 200 {"results":[{"content":"1","createdAt":"2016-08-09T06:18:13.028Z","updatedAt":"2016-08-09T06:18:13.028Z","objectId":"57a975a55bbb5000643fb690"}]}
```

我们不建议在线上生产环境开启这个日志，否则将会打印大量的日志。



### 时区

在云引擎的中国区系统默认使用北京时间（`Asia/Shanghai`），美国区默认使用 UTC 时间。


需要注意 JavaScript 中 Date 类型的不同方法，一部分会返回 UTC 时间、一部分会返回当地时间（在中国区是北京时间）：

函数|时区|结果
---|---|---
`toISOString`|UTC 时间|2015-04-09T03:35:09.678Z
`toJSON`（JSON 序列化时）|UTC 时间|2015-04-09T03:35:09.678Z
`toUTCString`|UTC 时间|Thu, 09 Apr 2015 03:35:09 GMT
`getHours`|UTC 时间|3
`toString`（`console.log` 打印时）|当地时间|Thu Apr 09 2015 03:35:09 GMT+0000 (UTC)
`toLocaleString`|当地时间|Thu Apr 09 2015 03:35:09 GMT+0000 (UTC)

提醒大家需要在向用户展示时注意区分，否则就会出现时间「偏差八小时」的现象。


### 依赖缓存

云引擎实现了一个缓存机制来加快构建的速度，所谓构建就是指你的应用在云引擎上安装依赖的过程，目测存在两种机制。

Node.js 采用的：如果 `package.json` 和上次构建相比没有修改，就直接采用上次安装的依赖，只将新的应用代码替换上去。

Python、PHP、Java 采用的：每次构建结束时将依赖目录打包，下次构建时将上次的依赖包解压到原处，再运行包管理器来安装依赖，可以复用已有的依赖项。

如果你遇到了与依赖安装有关的问题，可以在控制台部署时勾选「下载最新依赖」，或通过命令行工具部署时添加 `--noCache` 选项。

## 备案和自定义域名

如果需要绑定自己的域名，进入 [应用控制台 > 账号设置 > 域名绑定](/settings.html#/setting/domainbind)，按照步骤填写资料即可。

国内节点绑定独立域名需要有 ICP 备案，只有主域名需要备案，二级子域名不需要备案；如果没有 ICP 备案，请进入 [应用控制台 > 账号设置 > 域名备案](/settings.html#/setting/domainrecord)，按照步骤填写资料进行备案。

<div class="callout callout-info">备案之前要求云引擎已经部署，并且网站内容和备案申请的内容一致。仅使用云引擎托管静态文件、未使用其他 LeanCloud 服务的企业用户，需要自行完成域名备案工作。</div>
