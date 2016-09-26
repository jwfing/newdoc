



# 网站托管开发指南 &middot; Java

网站托管是云引擎的一个子模块，允许你用 Java 开发一个 Web 程序，提供云函数和 Hook，还可以提供静态文件的托管和自定义的路由、绑定你自己的域名。你可以用它为你的移动应用提供一个介绍和下载页、开发一个管理员控制台或完整的网站，或者运行一些必须在服务器端运行的自定义逻辑。

如果你还不知道如何创建云引擎项目，本地调试并部署到云端，可以先阅读一下 [云引擎快速入门](leanengine_quickstart.html)。

在阅读本文档的同时，你还可以通过 [云引擎服务概览](leanengine_overview.html) 了解云引擎的全部功能、通过 [云引擎命令行工具使用详解](leanengine_cli.html) 了解命令行工具的用法、通过 [LeanCache 使用指南](leancache_guide.html) 了解云引擎提供的内存缓存服务，还可以在 [云引擎项目示例](leanengine_examples.html) 找到一些云引擎的示例项目，遇到问题时可以先检索一下 [云引擎常见问题和解答](leanengine_faq.html)。

这篇文档以 Java 为例，但云引擎还支持其他多种语言，你可以选择自己熟悉的技术栈进行开发：

- [Node.js](leanengine_webhosting_guide-node.html)
- [Python](leanengine_guide-python.html)
- [PHP](leanengine_webhosting_guide-php.html)
- [Java](leanengine_webhosting_guide-java.html)

## 项目骨架


你的项目需要遵循一定格式才会被云引擎识别并运行。

 项目必须有 `$PROJECT_DIR/pom.xml` 文件，该文件为整个项目的配置文件。





### 项目启动

你需要在 pom.xml 中指定打包的目标为 war 包:
```
<packaging>war</packaging>
```
这样云引擎部署时会打包生成对应的 war 包。

如果你需要进行本地调试，可以通过在 pom.xml 中增加 jetty plugin 来作为 Servlet 容器运行 war 包，具体的配置可以参考我们的 [示例代码](https://github.com/leancloud/java-war-getting-started/blob/master/pom.xml) 。但这样并不能直接使用类似于 `mvn jetty:run` 来启动应用，因为云引擎应用启动需要一些 [环境变量](#环境变量)，否则无法完成初始化。

以下有几种方式可以本地启动：

#### 命令行工具

命令行工具 [v1.3.2](https://github.com/leancloud/avoscloud-code-command/blob/master/changelog.md#v132) 及之后的版本支持云引擎 Java 应用的本地启动，并为 JVM 进程设置需要的环境变量，在项目根目录执行 `lean up`，根据提示输入 `appId`，`masterKey` 等信息，命令行工具会调用 `mvn jetty:run` 来启动应用。

**提示**：相对于其他启动方式，命令行工具有 [多应用管理](leanengine_cli.html#多应用管理) 功能，可以方便的切换不同应用环境。

#### 使用 Eclipse 启动应用

首先确保 Eclipse 已经安装 Maven 插件，并将项目以 **Maven Project** 方式导入 Eclipse 中，在 **Package Explorer** 视图右键点击项目，选择 **Run As** > **Maven build...**，将 **Main** 标签页的 **Goals** 设置为 `jetty:run`，将 **Environment** 标签页增加以下环境变量和相应的值：

- LEANCLOUD_APP_ENV = `development`
- LEANCLOUD_APP_ID = `{{appid}}`
- LEANCLOUD_APP_KEY = `{{appkey}}`
- LEANCLOUD_APP_MASTER_KEY = `{{masterkey}}`
- LEANCLOUD_APP_PORT = `3000`

#### 命令行设置环境变量启动

可以使用类似下面的命令来启动应用：

```
  LEANCLOUD_APP_ENV="development" \
  LEANCLOUD_APP_ID="{{appid}}" \
  LEANCLOUD_APP_KEY="{{appkey}}" \
  LEANCLOUD_APP_MASTER_KEY="{{masterkey}}" \
  LEANCLOUD_APP_PORT=3000 \
  mvn jetty:run
```




Java 云引擎只支持 1.8 运行环境和 war 包运行


### 健康监测

你的应用在启动时，云引擎的管理程序会每秒去检查你的应用是否启动成功，如果 **30 秒** 仍未启动成功，即认为启动失败；在之后应用正常运行的过程中，也会有定期的「健康监测」，以确保你的应用正常运行，如果健康监测失败，云引擎管理程序会自动重启你的应用。

健康检查的 URL 包括你的应用首页（`/`）和 Java SDK 负责处理的 `/__engine/1/ping`，只要 **两者之一** 返回了 HTTP 200 的响应，就视作成功。因此请确保你的程序使用了 Java SDK，或你的应用 **首页能够正常地返回 HTTP 200** 响应。

除此之外，为了支持云引擎的云函数和 Hook 功能，管理程序会使用 `/1.1/functions/_ops/metadatas` 这个 URL 和 Java SDK 交互，请确保将这个 URL 交给 Java SDK 处理，或 **返回一个 HTTP 404 表示不使用云函数** 和 Hook 相关的功能。



如果未使用 [LeanEngine Java SDK](https://github.com/leancloud/leanengine-java-sdk)，则需要自己实现该 URL 的处理，比如这样：

```
//健康监测 router
@WebServlet(name = "LeanEngineHealthServlet", urlPatterns = {"/__engine/1/ping"})
public class LeanEngineHealthCheckServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setHeader("content-type", "application/json; charset=UTF-8");
    JSONObject result = new JSONObject();
    result.put("runtime", System.getProperty("java.version"));
    result.put("version", "custom");
    resp.getWriter().write(result.toJSONString());
  }
}
```
和

```
// 云函数列表
@WebServlet(name = "LeanEngineMetadataServlet", urlPatterns = {"/1.1/functions/_ops/metadatas"})
public class LeanEngineMetadataServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    resp.setContentType("application/json; charset=UTF-8");
    resp.getWriter().write("{\"result\":[]}");
  }
}

```


## Web 框架



 依赖 Servlet 3.1.0 ，你可以使用任何基于 Servlet 3.1.0 的 Web 框架。


## LeanCloud SDK



云引擎使用 [LeanEngine Java SDK](https://github.com/leancloud/leanengine-java-sdk) 来代替 [Java 存储 SDK](https://github.com/leancloud/JavaSDK) 。前者依赖了后者，并增加了云函数和 Hook 函数的支持，因此开发者可以直接使用 [LeanCloud 的存储服务](leanstorage_guide-java.html) 来存储自己的数据。

如果使用项目框架作为基础开发，[LeanEngine Java SDK](https://github.com/leancloud/leanengine-java-sdk) 默认是配置好的，可以根据示例程序的方式直接使用。

如果是自定义项目，则需要自己配置：

* 配置依赖：在 pom.xml 中增加依赖配置来增加 [LeanEngine Java SDK](https://github.com/leancloud/leanengine-java-sdk) 的依赖：

```xml
	<repositories>
		<repository>
			<id>leancloud</id>
			<name>LeanCloud</name>
			<url>http://mvn.leancloud.cn/nexus/content/groups/public/</url>
		</repository>
	</repositories>
	<dependencies>
		<dependency>
			<groupId>cn.leancloud</groupId>
			<artifactId>leanengine</artifactId>
			<version>[0.1.6,0.2.0)</version>
		</dependency>
	</dependencies>
```

* 初始化：在正式使用数据存储之前，你需要使用自己的应用 key 进行初始化中间件：

```java
import com.avos.avoscloud.internal.impl.JavaRequestSignImplementation;
import cn.leancloud.LeanEngine;

String appId = System.getenv("LEANCLOUD_APP_ID");
String appKey = System.getenv("LEANCLOUD_APP_KEY");
String appMasterKey = System.getenv("LEANCLOUD_APP_MASTER_KEY");
LeanEngine.initialize(appId, appKey, appMasterKey);

// 如果不希望使用 masterKey 权限，可以将下面一行删除
JavaRequestSignImplementation.instance().setUseMasterKey(true);
```


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



```java
String env = System.getenv("LEANCLOUD_APP_ENV");
if (env.equals("development")) {
    // 当前环境为「开发环境」，是由命令行工具启动的
} else if (env.equals("production")) {
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


云引擎提供了一个 `EngineSessionCookie` 组件，用 Cookie 来维护用户（`AVUser`）的登录状态，要使用这个组件可以在初始化时添加下列代码：

```java
// 加载 cookieSession 以支持 AV.User 的会话状态
LeanEngine.addSessionCookie(new EngineSessionCookie(3600000,true));
```

`EngineSessionCookie` 的构造函数参数包括：

* **maxAge**：设置 Cookie 的过期时间。
* **fetchUser**：**是否自动 fetch 当前登录的 AV.User 对象。默认为 false。**
  如果设置为 true，每个 HTTP 请求都将发起一次 LeanCloud API 调用来 fetch 用户对象。如果设置为 false，默认只可以访问 `AVUser.getCurrentUser()` 的 `id`（`_User` 表记录的 ObjectId）和 `sessionToken` 属性，你可以在需要时再手动 fetch 整个用户。

* 在云引擎方法中，通过 `AVUser.getCurrentUser()` 获取用户信息。
* 在网站托管中，通过 `AVUser.getCurrentUser()` 获取用户信息。
* 在后续的方法调用显示传递 user 对象。

你可以这样简单地实现一个具有登录功能的站点：

#### 登录
```java
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {


  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    req.getRequestDispatcher("/login.jsp").forward(req, resp);
  }


  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    String username = req.getParameter("username");
    String passwd = req.getParameter("password");
    try {
      AVUser.logIn(username, passwd);
      req.getRequestDispatcher("/profile").forward(req, resp);
    } catch (AVException e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.setContentType("application/json; charset=UTF-8");
      JSONObject result = new JSONObject();
      result.put("code", e.getCode());
      result.put("error", e.getMessage());
      resp.getWriter().write(result.toJSONString());
      e.printStackTrace();
    }
  }
}
```
#### 登出
``` java
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    AVUser user = AVUser.getCurrentUser();
    if (user != null) {
      user.logOut();
    }
    req.getRequestDispatcher("/profile").forward(req, resp);
  }
}
```

#### Profile页面

```java
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    if (AVUser.getCurrentUser() == null) {
      req.getRequestDispatcher("/login").forward(req, resp);
    } else {
      resp.setContentType("application/json; charset=UTF-8");
      JSONObject result = new JSONObject();
      result.put("currentUser", AVUser.getCurrentUser());
      resp.getWriter().write(result.toJSONString());
    }
  }
}

```

一个简单的登录页面（`login.jsp`）可以是这样：

```html
<html>
    <head></head>
    <body>
      <form method="post" action="/login">
        <label>Username</label>
        <input name="username"></input>
        <label>Password</label>
        <input name="password" type="password"></input>
        <input class="button" type="submit" value="登录">
      </form>
    </body>
  </html>
```



## 实现常见功能

### 发送 HTTP 请求



云引擎 Java 环境可以使用 URL 或者是 HttpClient 等基础类 ，不过我们推荐使用 okhttp 等第三方库来处理 HTTP 请求。

``` java
    Request.Builder builder = new Request.Builder();
    builder.url(url).get();
    OkHttpClient client  = new OkHttpClient();
    Call call = client.newCall(buidler.build());
    try{
      Response response = call.execute();
    }catch(Exception e){
    }
```


### 获取客户端 IP

如果你想获取客户端的 IP，可以直接从用户请求的 HTTP 头的 `x-real-ip` 字段获取，实例代码如下：



``` java
EngineRequestContext.getRemoteAddress();
```


### 文件上传
托管在 云引擎 的网站项目可以直接使用内置的 LeanCloud Java SDK 的 API 文件相关的接口直接处理文件的上传。

假设前端 HTML 代码如下：

```html
<form enctype="multipart/form-data" method="post" action="/upload">
  <input type="file" name="iconImage">
  <input type="submit" name="submit" value="submit">
</form>
```



接下来定义文件上传的处理函数，构建一个 Form 对象，并将 req 作为参数进行解析，会将请求中的文件保存到临时文件目录，并构造 files 对象：



```
@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String description = request.getParameter("description"); // Retrieves <input type="text" name="description">
    Part filePart = request.getPart("iconImage"); // Retrieves <input type="file" name="file">
    String fileName = filePart.getSubmittedFileName();
    InputStream fileContent = filePart.getInputStream();
    // ... (do your job here)
  }
}
```


### Session


Java 暂不支持。


### LeanCache



关于 LeanCache 的更多使用方法请看 [LeanCache 使用指南](leancache_guide.html)。

### 重定向到 HTTPS

为了安全性，我们可能会为网站加上 HTTPS 加密传输。我们的 云引擎 支持网站托管，同样会有这样的需求。

因此我们在 云引擎 中提供了一个新的 middleware 来强制让你的 `{应用的域名}.leanapp.cn` 的网站通过 https 访问，你只要这样：




```java
LeanEngine.setHttpsRedirectEnabled(true);
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



### 日志

在控制台的 [云引擎 / 日志](/cloud.html?appid={{appid}}#/log) 中可以查看云引擎的部署和运行日志，还可以通过日志级别进行筛选。

应用的日志可以直接输出到「标准输出」或者「标准错误」，这些信息会分别对应日志的 `info` 和 `error` 级别，比如下列代码会在 info 级别记录参数信息：



<div class="callout callout-info">日志单行最大 4096 个字符，多余部分会被丢弃；日志输出频率大于 600 行/分钟，多余的部分会被丢弃。</div>



### 时区

在云引擎的中国区系统默认使用北京时间（`Asia/Shanghai`），美国区默认使用 UTC 时间。



### 依赖缓存

云引擎实现了一个缓存机制来加快构建的速度，所谓构建就是指你的应用在云引擎上安装依赖的过程，目测存在两种机制。

Node.js 采用的：如果 `package.json` 和上次构建相比没有修改，就直接采用上次安装的依赖，只将新的应用代码替换上去。

Python、PHP、Java 采用的：每次构建结束时将依赖目录打包，下次构建时将上次的依赖包解压到原处，再运行包管理器来安装依赖，可以复用已有的依赖项。

如果你遇到了与依赖安装有关的问题，可以在控制台部署时勾选「下载最新依赖」，或通过命令行工具部署时添加 `--noCache` 选项。

## 备案和自定义域名

如果需要绑定自己的域名，进入 [应用控制台 > 账号设置 > 域名绑定](/settings.html#/setting/domainbind)，按照步骤填写资料即可。

国内节点绑定独立域名需要有 ICP 备案，只有主域名需要备案，二级子域名不需要备案；如果没有 ICP 备案，请进入 [应用控制台 > 账号设置 > 域名备案](/settings.html#/setting/domainrecord)，按照步骤填写资料进行备案。

<div class="callout callout-info">备案之前要求云引擎已经部署，并且网站内容和备案申请的内容一致。仅使用云引擎托管静态文件、未使用其他 LeanCloud 服务的企业用户，需要自行完成域名备案工作。</div>
