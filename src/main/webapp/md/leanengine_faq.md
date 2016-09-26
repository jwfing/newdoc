# 云引擎常见问题和解答

## 如何判断当前是预备环境还是生产环境？

请参考 [网站托管开发指南 · 预备环境和生产环境](leanengine_webhosting_guide-node.html#预备环境和生产环境) / [Python · 运行环境区分](/leanengine_guide-python.html#运行环境区分)。

## 怎么添加第三方模块

云引擎 2.0 开始支持添加第三方模块（请参考 [云引擎指南 - 升级到 2.0](leanengine_guide-cloudcode.html#云引擎_2_0_版)），只需要像普通的 Node.js 项目那样，在项目根目录创建文件 `package.json`，下面是一个范例：

```
{
  "name": "cloud-engine-test",
  "description": "Cloud Engine test project.",
  "version": "0.0.1",
  "private": true,
  "dependencies": {
    "async": "0.9.0",
    "moment": "2.9.0"
  }
}
```

`dependencies` 内的内容表明了该项目依赖的三方模块（比如示例中的 `async` 和 `moment`）。

然后在项目根目录执行：

```
npm install
```

即可下载相关三方包到 `node_modules` 目录。

然后即可在代码中引入三方包：

```
var async = require('async');
```

**注意**：命令行工具部署时是不会上传 `node_modules` 目录，因为云引擎服务器会根据 `package.json` 的内容自动下载三方包。所以也建议将 `node_modules` 目录添加到 `.gitignore` 中，使其不加入版本控制。

## Maximum call stack size exceeded 如何解决？

如果你的应用时不时出现 `Maximum call stack size exceeded` 异常，可能是因为在 hook 中调用了 `AV.Object.extend`。有两种方法可以避免这种异常：

- 升级 leanengine 到 v1.2.2 或以上版本
- 在 hook 外定义 Class（即定义在 `AV.Cloud.define` 方法之外），确保不会对一个 Class 执行多次 `AV.Object.extend`

我们在 [JavaScript 指南 - AV.Object](./leanstorage_guide-js.html#AV_Object) 章节中也进行了描述。

## 如何进行域名备案和域名绑定？

只有网站类的才需要备案，并且在主域名已备案的情况下，二级子域名不需要备案。如果主站需要托管在我们这边，且还没有备案过，请进入 **应用控制台 > 账号设置 >** [域名备案](/settings.html#/setting/domainrecord) 和 [域名绑定](/settings.html#/setting/domainbind)，按照步骤提示操作即可。

## 调用云引擎方法如何收费？

云引擎中如果有 LeanCloud 的存储等 API 调用，按 [API 收费策略](faq.html#API_调用次数的计算) 收费。另外如果使用的是云引擎专业版，该服务也会产生使用费，具体请参考 [云引擎运行方案](leanengine_plan.html#价格)。

## 「定义函数」和「Git 部署」可以混用吗？

「定于函数」的产生是为了方便大家初次体验云引擎，或者只是需要一些简单 hook 方法的应用使用。我们的实现方式就是把定义的函数拼接起来，生成一个云引擎项目然后部署。

所以可以认为「定义函数」和 「git 部署」最终是一样的，都是一个完整的项目。

是一个单独功能，可以不用使用基础包，git 等工具快速的生成和编辑云引擎。

当然，你也可以使用基础包，自己写代码并部署项目。

这两条路是分开的，任何一个部署，就会导致另一种方式失效掉。

## 为什么查询 include 没有生效？

以 JavaScript 云引擎为例子，很多时候，经常会定义一个云函数，在里面使用 `AV.Query` 查询一张表，并 include 其中一个 pointer 类型的字段，然后返回给客户端:

``` javascript
AV.Cloud.define('querySomething', function(req, res) {
  var query = new AV.Query('Something');
  //假设 user 是 Something 表的一个 Pointer 列。
  query.include('user');
  //……其他条件或者逻辑……
  query.find().then(function(results) {
    //返回查询结果给客户端
    res.success(results);
  }).catch(function(err){
    //返回错误给客户端
  });
});
```

你会看到返回的结果里， user 仍然是 pointer 类型，似乎 include 没有生效？

``` json
{
 result: [
   {
     ……Something 其他字段
     "user": {
       "className": "_User",
       "__type": "Pointer",
       "objectId": "abcdefg"
     }
   }
   ……
 ]
}
```

这其实是因为 `res.success(results)` 会调用到 `AV.Object#toJSON` 方法，将结果序列化为 JSON 对象返回给客户端。

而 `AV.Object#toJSON` 方法为了防止循环引用，当遇到属性是 Pointer 类型会返回 pointer 元信息，不会将 include 的其他字段添加进去。

因此，你需要主动将该字段进行 JSON 序列化，例如：

``` javascript
 query.find().then(function(results) {
    //主动序列化 json 列。
    results.forEach(function(result){
      result.set('user', result.get('user') ?  result.get('user').toJSON() : null);
    });
    //再返回结果
    res.success(results);
  }).catch(function(err){
    //返回错误给客户端
  });
```

## Gitlab 部署常见问题

很多用户自己使用 [Gitlab](http://gitlab.org/)搭建了自己的源码仓库，有朋友会遇到无法部署到 LeanCloud 的问题，即使设置了 Deploy Key，却仍然要求输入密码。

可能的原因和解决办法如下：

* 确保你 gitlab 运行所在服务器的 /etc/shadow 文件里的 git（或者 gitlab）用户一行的 `!`修改为 `*`，原因参考 [Stackoverflow - SSH Key asks for password](http://stackoverflow.com/questions/15664561/ssh-key-asks-for-password)，并重启 SSH 服务：`sudo service ssh restart`。
* 在拷贝 deploy key 时，确保没有多余的换行符号。
* Gitlab 目前不支持有 comment 的 deploy key。早期 LeanCloud 用户生成的 deploy key 可能带 comment，这个 comment 是在 deploy key 的末尾 76 个字符长度的字符串，例如下面这个 deploy key：

```
ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA5EZmrZZjbKb07yipeSkL+Hm+9mZAqyMfPu6BTAib+RVy57jAP/lZXuosyPwtLolTwdyCXjuaDw9zNwHdweHfqOX0TlTQQSDBwsHL+ead/p6zBjn7VBL0YytyYIQDXbLUM5d1f+wUYwB+Cav6nM9PPdBckT9Nc1slVQ9ITBAqKZhNegUYehVRqxa+CtH7XjN7w7/UZ3oYAvqx3t6si5TuZObWoH/poRYJJ+GxTZFBY+BXaREWmFLbGW4O1jGW9olIZJ5/l9GkTgl7BCUWJE7kLK5m7+DYnkBrOiqMsyj+ChAm+o3gJZWr++AFZj/pToS6Vdwg1SD0FFjUTHPaxkUlNw== App dxzag3zdjuxbbfufuy58x1mvjq93udpblx7qoq0g27z51cx3's cloud code deploy key
```
其中最后 76 个字符：

```
App dxzag3zdjuxbbfufuy58x1mvjq93udpblx7qoq0g27z51cx3's cloud code deploy key
```

就是 comment，删除这段字符串后的 deploy key（如果没有这个字样的comment无需删除）保存到 gitlab 即可正常使用：

```
ssh-rsa AAAAB3NzaC1yc2EAAAABIwAAAQEA5EZmrZZjbKb07yipeSkL+Hm+9mZAqyMfPu6BTAib+RVy57jAP/lZXuosyPwtLolTwdyCXjuaDw9zNwHdweHfqOX0TlTQQSDBwsHL+ead/p6zBjn7VBL0YytyYIQDXbLUM5d1f+wUYwB+Cav6nM9PPdBckT9Nc1slVQ9ITBAqKZhNegUYehVRqxa+CtH7XjN7w7/UZ3oYAvqx3t6si5TuZObWoH/poRYJJ+GxTZFBY+BXaREWmFLbGW4O1jGW9olIZJ5/l9GkTgl7BCUWJE7kLK5m7+DYnkBrOiqMsyj+ChAm+o3gJZWr++AFZj/pToS6Vdwg1SD0FFjUTHPaxkUlNw==
```
