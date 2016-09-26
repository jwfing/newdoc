# 云引擎项目示例

伴随着 LeanEngine 的发展，以及针对一些用户呼声较高的几种类型的需求，我们特地开发了几个项目示例，给开发者提供了几个「拿来就用」的项目。

## 一个简单的 Todo 列表

[leanengine-todo-demo](https://github.com/leancloud/leanengine-todo-demo) 这是一个稍微复杂点的项目，演示了基本的用户注册、会话管理、业务数据的增删查改、简单的 ACL 使用。这个项目可以作为初学云引擎和 [JavaScript SDK](leanstorage_guide-js.html) 使用。（在线演示：<http://todo-demo.leanapp.cn/>）

## OAuth 授权验证回调服务器

开发过微博应用或者是接入过 QQ 登录的应用都需要在后台设置一个回调服务器地址，用来接收回调信息，关于为什么需要回调服务器这个请阅读 [OAuth 2 官方文档](http://oauth.net/2/)。

许多用户在 LeanEngine 上托管了自己的网站，因为他们在制作登录页面的时候，需要实现微博登录等等第三方登录的功能，因此如果在 LeanEngine 部署回调服务器可以节省成本，快速实现需求。

如何开发 OAuth 授权验证回调服务器，请查看 [微博 OAuth 授权验证回调服务器开发指南](webhosting_oauth.html)。

## 微信自动问答机器人

微信开发是时下大热的开发趋势。很多团队在进行市场推广以及用户渗透的时候，要借助微信用户群做一些符合自己需求的定向开发。例如，航空公司可以通过微信公众号向乘客推送行程信息，银行可以及时发送消费和余额变动信息等。借助云引擎提供的代码托管功能，你可以设置让微信后台将用户所发送信息转发至云引擎，使用部署在上面的代码进行处理后，再调用微信接口进行回复。

下面是一个用于演示的例子。

1. 扫码关注这个微信公众号<br/>
  <img src="http://ac-lhzo7z96.clouddn.com/1457597962129" width="200">
2. 向这个公众号发送一条消息：**说个笑话**。
3. 你会收到机器人回复的消息。

如何开发这个微信公众号请查看 [微信公众平台开发指南](webhosting_weixin.html)。

## 实时通信云引擎签名 Demo

LeanCloud 实时通信服务采用 [签名方式](realtime_v2.html#权限和认证) 与用户系统对接。当客户端进行与权限、认证相关的操作时，客户端需要首先向用户系统发起请求获得一个签名，再把签名发送到 LeanCloud 实时通信服务，从而帮助 LeanCloud 确认请求是否有效，其机制与 OAuth 1.0 类似。本 Demo 仅仅演示签名，并不包含实际业务逻辑，在实际应用中你需要根据业务做相应的检查。

<a href="https://github.com/leancloud/realtime-messaging-signature-cloudcode" class="btn btn-default">阅读</a>

## LeanCache 常见场景示例

<div class="callout callout-danger">抢红包、游戏排名、秒杀购物等场景，强烈建议使用 LeanCache。</div>

[LeanCache Node.js Demos](https://github.com/leancloud/lean-cache-demos) 是 [LeanCache](https://leancloud.cn/docs/leancache_guide.html) 的示例项目，使用 Node.js 和 Express 实现，包含了一些典型的使用场景：

* **关联数据缓存**：缓存一些数据量少、查询频繁、不常修改、关联结构复杂的关联数据。
* **图形验证码**：利用图形验证码保护短信发送接口。
* **排行榜缓存**：维护一个用户游戏分数的排行榜，并在次日将榜单归档到云存储中。
* **抢红包**：管理员在后台生成一些随机金额的红包供用户获取，利用 LeanCache 应对瞬时的高并发场景。
* **热点只读数据缓存**：将几乎只读的配置（例如购物网站的商品分类信息）通过 Class Hook 缓存在 Redis。
* **节点选举和锁**：多个任务共同竞争一个资源（锁），确保同一时间只有一个任务能够在执行（持有这个锁）。
* **任务队列**：保证大量任务以指定的并发数量顺序地执行，以减少对其他服务的压力。
