


# 云引擎指南 &middot; Python



云引擎允许你编写 JavaScript、Python 等代码，并部署到 LeanCloud 云端，通过拦截 save 请求，在保存对象之前或之后做一些事情；你可以自定义业务函数，并通过 SDK 调用；你还可以调用部分第三方库来实现自己的业务逻辑，甚至还可以将整个网站架设在云引擎之上，我们提供了网站托管服务。

与旧版云引擎的区别在于：

* **标准的运行环境**：之前云引擎是我们订制的一个沙箱运行环境，功能受限，并且代码只可以在云引擎环境运行，难以迁移到自己搭建的后端服务器上。而云引擎环境可以部署标准项目。
* **多语言支持**：目前支持 Node.js、Python、PHP、Java，未来会实现更多的编程语言运行环境。

## 命令行工具

使用命令行工具可以非常方便地部署和发布云引擎项目，查看项目状态和查看日志，甚至支持多应用部署。具体请参考 [命令行工具指南](leanengine_cli.html)。

## Demo


* [python-getting-started](https://github.com/leancloud/python-getting-started)：这是一个非常简单的 Python Web 的项目，可以作为大家的项目模板。（在线演示：<http://python.leanapp.cn/>）


## 快速入门

首先确认本机已经安装了相关的运行环境和 [命令行工具](leanengine_cli.html)。**注意**：目前云引擎的 Python 版本为 2.7，请你最好使用此版本的 Python 进行开发。Python 3 的支持正在开发中。

### 创建应用


从 Github 迁出实例项目，该项目可以作为一个你应用的基础：

```
$ git clone https://github.com/leancloud/python-getting-started.git
$ cd python-getting-started
```

然后添加应用 appId 等信息到该项目：

```
$ lean app add <APP-NAME> <APP-ID>
```


`<APP-NAME>` 是应用名称，`<APP-ID>` 是应用 ID。这些信息可以 [控制台 /（选择应用）/ 设置 / 基本信息](/app.html?appid={{appid}}#/general) 和 [应用 Key](/app.html?appid={{appid}}#/key) 中找到。



### 本地运行


安装依赖：

```
$ sudo pip install -Ur requirements.txt
```

启动应用：

```
$ lean up
```



窗口会提示输入 Master Key，该信息可以在 [控制台 / 设置 / 应用 Key](/app.html?appid={{appid}}#/key) 中找到。


<div class="callout callout-info">复制粘贴 Master Key 后，窗口不会有任何显示，直接按回车键确认即可。</div>

应用即可启动运行：<http://localhost:3000>

可以通过下列命令确认云函数工作正常，该函数定义在 `$PROJECT_DIR/cloud.py` 文件中：

```sh
curl -X POST -H "Content-Type: application/json; charset=utf-8"   \
       -H "X-LC-Id: {{appid}}"          \
       -H "X-LC-Key: {{appkey}}"        \
       -H "X-LC-Prod: 0"  -d '{}' \
       http://localhost:3000/1.1/functions/hello
```

如果需要指定 web 服务端口，可以增加 `-P` 参数。获取帮助信息请使用 `-h` 参数。

### 部署到云引擎

部署到预备环境：

```
$ avoscloud deploy
```

如果你设置了 [二级域名](#设置域名)，即可通过 `http://stg-${your_app_domain}.leanapp.cn` 访问你应用的预备环境。

部署到生产环境：

```
$ avoscloud publish
```

如果你设置了 [二级域名](#设置域名)，即可通过 `http://${your_app_domain}.leanapp.cn` 访问你应用的生产环境。

通过下列命令可以确认云函数在云引擎上工作正常：

```sh
curl -X POST -H "Content-Type: application/json; charset=utf-8"   \
       -H "X-LC-Id: {{appid}}"          \
       -H "X-LC-Key: {{appkey}}"        \
       -H "X-LC-Prod: 1"  -d '{}' \
       https://leancloud.cn/1.1/functions/hello
```

## 使用云引擎中间件

如果你没有使用项目框架，则需要自己安装和初始化云引擎中间件。

### 环境变量

你可以在代码中使用以下与云引擎平台相关的环境变量：

变量名|说明
---|---
`LEANCLOUD_APP_ID`|当前应用的 App Id
`LEANCLOUD_APP_KEY`|当前应用的 App Key
`LEANCLOUD_APP_MASTER_KEY`|当前应用的 Master Key
`LEANCLOUD_APP_ENV`|当前的应用环境：<ul><li>开发环境没有该环境变量，或值为 `development`（一般指本地开发）</li><li>预备环境值为 `stage`</li><li>生产环境值为 `production`</li></ul>
`LEANCLOUD_APP_PORT`|当前应用开放给外网的端口，只有监听此端口，用户才可以访问到你的服务。
`LEANCLOUD_APP_INSTANCE`|云引擎实例名称，在多实例环境可以通过此变量标示自己。
`LEANCLOUD_REGION`|云引擎服务所在区域，值为 `CN` 或 `US`，分别表示国内节点和美国节点。

**提示**：旧版云引擎使用的以 `LC_` 开头的环境变量（如 `LC_APP_ID`）已经被弃用。为了保证代码兼容性，`LC_` 变量在一段时间内依然有效，但未来可能会完全失效。为了避免报错，建议使用 `LEANCLOUD_` 变量来替换。

**提示**：如果在客户端想调用云引擎的预备环境，各个 SDK 都有类似于 `setProduction` 的方法，比如 [JavaScript SDK &middot; AV.setProduction(production)](/api-docs/javascript/symbols/AV.html#.setProduction)，其中 `production` 设置为 `0` 则该 SDK 将请求预备环境；设置为 `1` 将请求生产环境，默认为 `1`。

### 安装



首先需要安装 LeanCloud Python SDK，在你的项目 `requirements.txt` 中增加一行新的依赖：

```
leancloud-sdk
```

之后执行 `pip install -r requirements.txt` 来安装 LeanCloud Python SDK。


在正式使用数据存储 API 之前，你需要使用自己的应用 key 进行初始化中间件：


```python
import os

import leancloud
from flask import Flask


APP_ID = os.environ.get('LC_APP_ID', '{{appid}}') # 你的 app id
MASTER_KEY = os.environ.get('LC_APP_MASTER_KEY', '{{masterkey}}') #  你的 master key

leancloud.init(APP_ID, master_key=MASTER_KEY)

app = Flask(__name__)
engine = leancloud.Engine(app)
```

之后请在 wsgi.py 中将 engine 赋值给 application（而不是之前的 Flask 实例）。


请保证在使用任何数据存储功能前执行这段代码。


### 使用数据存储

你可以直接在云引擎上使用我们的 [数据存储](/features/storage.html) 服务，相关功能请参考 [Python SDK](./python_guide.html)。

## 云函数

当你开发移动端应用时，可能会有下列需求：

* 应用有 Android、iOS 甚至 Windows Phone 客户端，但是很多业务逻辑都是一样的，你希望能将这部分逻辑抽取出来，只维护一份。
* 有些逻辑希望能够较灵活的调整（比如某些个性化列表的排序规则），但又不想频繁的更新和发布移动客户端。
* 有些逻辑需要的数据量很大，或者运算成本高（比如某些统计汇总需求），你不希望在移动客户端进行运算，因为这样会消耗大量的网络流量和手机运算能力。
* 当应用执行特定操作时，由云端系统自动触发一段逻辑（称为 [Hook 函数](#Hook_函数)），比如：用户注册后对该用户增加一些信息记录用于统计；或某业务数据发生变化后希望做一些别的业务操作。这些代码不适合放在移动客户端（比如因为上面提到的几个原因）。
* 你希望有能有定时任务，比如每天凌晨清理垃圾注册账号等等。

这时，你可以使用云引擎的云函数。云函数是一段部署在服务端的代码，编写 JavaScript 或者 Python 代码，并部署到我们的平台上，可以很好的完成上述需求。

你可以使用 [项目定义函数](#项目定义函数) 来开发云函数。

示例项目中 `$PROJECT_DIR/cloud.py` 文件定义了一个很简单的 `hello` 云函数。现在让我们看一个明显较复杂的例子来展示云引擎的用途。在云端进行计算的一个重要理由是，你不需要将大量的数据发送到设备上做计算，而是将这些计算放到服务端，并返回结果这一点点信息就好。

例如，你写了一个应用，让用户对电影评分，一个评分对象大概是这样：

```json
{
  "movie": "夏洛特烦恼",
  "stars": 5,
  "comment": "夏洛一梦，笑成麻花"
}
```

`stars` 表示评分，1-5。如果你想查找《夏洛特烦恼》这部电影的平均分，你可以找出这部电影的所有评分，并在设备上根据这个查询结果计算平均分。但是这样一来，尽管你只是需要平均分这样一个数字，却不得不耗费大量的带宽来传输所有的评分。通过云引擎，我们可以简单地传入电影名称，然后返回电影的平均分。

云函数接收 JSON 格式的请求对象，我们可以用它来传入电影名称。整个 [Python SDK](./python_guide.html) 都在云引擎运行环境上有效，可以直接使用，所以我们可以使用它来查询所有的评分。结合在一起，实现 `averageStars` 函数的代码如下：


```python
from leancloud import Query
from leancloud import Engine

@engine.define
def averageStars(movie):
    sum = 0
    query = Query('Review')
    try:
        reviews = query.find()
    except leancloud.LeanCloudError, e:
        // 如果不想做特殊处理，可以不捕获这个异常，直接抛出
        print e
        raise e
    for review in reviews:
        sum += review.get('starts')
	return sum / len(reviews)
```


### REST API 调用云函数

云函数可以被各种客户端 SDK 调用（详见各 SDK 的「调用云引擎」部分），也可以通过 REST API 来调用。例如，用一部电影名称去调用 `averageStars` 函数：

```sh
curl -X POST -H "Content-Type: application/json; charset=utf-8" \
       -H "X-LC-Id: {{appid}}" \
       -H "X-LC-Key: {{appkey}}" \
       -H "X-LC-Prod: 0" \
       -d '{"movie":"夏洛特烦恼","stars": 5,"comment": "夏洛一梦，笑成麻花"}' \
https://leancloud.cn/1.1/functions/averageStars
```


客户端传递的参数，会被当作关键字参数传递进云函数。

比如上面的例子，调用时传递的参数为 `{"movie": "夏洛特烦恼"}`，定义云函数的时候，参数写 movie，即可拿到对应的参数。

但是有时候，您传递的参数可能是变长的，或者客户端传递的参数数量错误，这时候就会报错。为了应对这种情况，推荐您使用关键字参数的形式来获取参数：

如果是已登录的用户发起云引擎调用，可以通过 `engine.current_user` 拿到此用户。如果通过 REST API 调用时模拟用户登录，需要增加一个头信息 `X-AVOSCloud-Session-Token: <sessionToken>`，该 `sessionToken` 在用户登录或注册时服务端会返回。

```python
@engine.define
def some_func(**params):
    # do something with params
    pass
```

这样 `params` 就是个 `dict` 类型的对象，可以方便的从中拿到参数了。


如果函数调用成功，云端返回给客户端的结果类似这样：

```json
{
  "result": 4.8
}
```

如果调用有错误，则返回：

```json
{
  "code": 141,
  "error": "搜索失败"
}
```

如有需要，也可以 [自定义错误响应码](#错误响应码)。

### 云引擎调用云函数

在云引擎中也可以使用 `cloudfunc.run` 调用 `engine.define` 定义的云函数：


```python
from leancloud import cloudfunc

try:
    result = cloudfunc.run('hello', name='dennis')
    # 调用成功，拿到结果
except LeanCloudError, e:
    print e
    # 调用失败
```


API 参数详解参见 [cloudfunc.run](/api-docs/python/leancloud.engine.html#/module-leancloud.engine.cloudfunc)。

### Hook 函数

Hook 函数本质上是云函数，但它有固定的名称，定义之后会**由系统**在特定事件或操作（如数据保存前、保存后，数据更新前、更新后等等）发生时**自动触发**，而不是由开发者来控制其触发时机。

使用 Hook 函数需要注意 [防止死循环调用](#防止死循环调用)。

#### before_save

在将对象保存到云端数据表之前，可以对数据做一些清理或验证。例如，一条电影评论不能过长，否则界面上显示不开，需要将其截断至 140 个字符：


```python
@engine.before_save('Review')  # Review 为需要 hook 的 class 的名称
def before_review_save(review):
	comment = review.get('comment')
	if not comment:
		raise leancloud.LeanEngineError(message='No comment!')
	if len(comment) > 140:
		review.comment.set('comment', comment[:137] + '...')
```


#### after_save

在数据保存后触发指定操作，比如当一条留言保存后再更新一下所属帖子的评论总数：


```python
import leancloud


@engine.after_save('Comment')  # Comment 为需要 hook 的 class 的名称
def after_comment_save(comment):
	post = leancloud.Query('Post').get(comment.id)
	post.increment('commentCount')
	try:
		post.save()
	except leancloud.LeanCloudError:
		raise leancloud.LeanEngineError(message='An error occurred while trying to save the Post. ')
```


再如，在用户注册成功之后，给用户增加一个新的属性 from 并保存：


```python
@engine.after_save('_User')
def after_user_save(user):
  print user
  user.set('from', 'LeanCloud')
  try:
    user.save()
  except LeanCloudError, e:
    print 'error:', e
```


如果 `afterSave` 函数调用失败，save 请求仍然会返回成功应答给客户端。`afterSave` 发生的任何错误，都将记录到云引擎日志里。

可以到 [控制台 / 存储 / 云引擎 / 日志](https://leancloud.cn/cloud.html?appid={{appid}}#/log) 中查看。


#### before_update


```python
@engine.before_update('Review')
def before_hook_object_update(obj):
    # 如果 comment 字段被修改了，检查该字段的长度
    assert obj.updated_keys == ['clientValue']
    if 'comment' not in obj.updated_keys:
        # comment 字段没有修改，跳过检查
        return
    if len(obj.get('comment')) > 140:
        # 拒绝过长的修改
        raise engine.LeanEngineError(message='comment 长度不得超过 140 个字符')
```


#### after_update

在更新对象后执行特定的动作，比如每次修改文章后记录下日志：


```python
import leancloud


@engine.after_update('Article')  # Article 为需要 hook 的 class 的名称
def after_article_update(article):
	print 'article with id {} updated!'.format(article.id)
```


#### before_delete

在删除一个对象之前做一些检查工作，比如在删除一个相册 Album 前，先检查一下该相册中还有没有照片 Photo：


```python
import leancloud


@engine.before_delete('Album')  # Article 为需要 hook 的 class 的名称
def before_album_delete(albun):
    query = leancloud.Query('Photo')
    query.equal_to('album', album)
    try:
        matched_count = query.count()
    except leancloud.LeanCloudError:
        raise engine.LeanEngineError(message='cloud code error')
    if count > 0:
	     raise engine.LeanEngineError(message='Can\'t delete album if it still has photos.')
```


#### after_delete

在被删一个对象后执行操作，例如递减计数、删除关联对象等等。同样以相册为例，这次我们不在删除相册前检查是否还有照片，而是在删除后，同时删除相册中的照片：


```python
import leancloud


@engine.after_delete('Album')  # Album 为需要 hook 的 class 的名称
def after_album_delete(album):
    query = leancloud.Query('Photo')
    query.equal_to('album', album)
    try:
        query.destroy_all()
    except leancloud.LeanCloudError:
        raise leancloud.LeanEngineError(message='cloud code error')
```


#### on_verified

当用户通过邮箱或者短信验证时，对该用户执行特定操作。比如：


```python
@engine.on_verified('sms')
def on_sms_verified(user):
	print user
```


函数的第一个参数是验证类型。短信验证为 `sms`，邮箱验证为 `email`。另外，数据库中相关的验证字段，如 `emailVerified` 不需要修改，系统会自动更新。

#### on_login

在用户登录之时执行指定操作，比如禁止在黑名单上的用户登录：


```python
@engine.on_login
def on_login(user):
    print 'on login:', user
    if user.get('username') == 'noLogin':
      # 如果抛出 LeanEngineError，则用户无法登录（收到 401 响应）
      raise LeanEngineError('Forbidden')
    # 没有抛出异常，函数正常执行完毕的话，用户可以登录
```


#### 实时通信 Hook 函数

请阅读 [实时通信概览 &middot; 云引擎 Hook](realtime_v2.html#云引擎_Hook) 来了解以下函数的具体用法：

- `_messageReceived`
- `_receiversOffline`
- `_conversationStart`
- `_conversationAdd`
- `_conversationRemove`


#### 防止死循环调用

在实际使用中有这样一种场景：在 `Post` 类的 `after_update` Hook 函数中，对传入的 `Post` 对象做了修改并且保存，而这个保存动作又会再次触发 `after_update`，由此形成死循环。针对这种情况，我们为所有 Hook 函数传入的 `leancloud.Object` 对象做了处理，以阻止死循环调用的产生。

不过请注意，以下情况还需要开发者自行处理：

- 对传入的 `leancloud.Object` 对象进行 `fetch` 操作。
- 重新构造传入的 `leancloud.Object` 对象，如使用 `leancloud.Object.create_without_data()` 方法。

对于使用上述方式产生的对象，请根据需要自行调用以下 API：

- `leancloud.Object.disable_before_hook()` 或 
- `leancloud.Object.disable_after_hook()` 

这样，对象的保存或删除动作就不会再次触发相关的 Hook 函数。

```python
@engine.after_update('Post')
def after_post_update(post):
    # 直接修改并保存对象不会再次触发 after update hook 函数
    post.set('foo', 'bar')
    post.save()

    # 如果有 fetch 操作，则需要在新获得的对象上调用相关的 disable 方法
    # 来确保不会再次触发 Hook 函数
    post.fetch()
    post.disable_after_hook()
    post.set('foo', 'bar')

    # 如果是其他方式构建对象，则需要在新构建的对象上调用相关的 disable 方法
    # 来确保不会再次触发 Hook 函数
    post = leancloud.Object.extend('Post').create_without_data(post.id)
    post.disable_after_hook()
    post.save()
```



### 错误响应码


错误响应码允许自定义。云引擎抛出的  `LeanCloudError`（数据存储 API 会抛出此异常）会直接将错误码和原因返回给客户端。若想自定义错误码，可以自行构造 `LeanEngineError`，将 `code` 与 `error` 传入。否则 `code` 为 1， `message` 为错误对象的字符串形式。

```python
@engine.define
def error_code(**params):
    leancloud.User.login('not_this_user', 'xxxxxxx')
```


客户端收到的响应：`{"code":211,"error":"Could not find user"}`


```python
from leancloud import LeanEngineError

@engine.define
def custom_error_code(**params):
    raise LeanEngineError(123, '自定义错误信息')
```


客户端收到的响应： `{"code":123,"error":"自定义错误信息"}`

#### Hook 函数的区别

为 `before_save` 这类的 hook 函数定义错误码，需要这样：


```python
@engine.before_save('Review')  # Review 为需要 hook 的 class 的名称
def before_review_save(review):
    comment = review.get('comment')
    if not comment:
      # 使用 json.dumps() 将对象转为 JSON 字串
      raise leancloud.LeanEngineError(json.dumps({
        'code': 123,
        'message': '自定义错误信息', 
    }))
```


客户端收到的响应为：`Cloud Code validation failed. Error detail : {"code":123, "message": "自定义错误信息"}`，然后通过**截取字符串**的方式取出错误信息，再转换成需要的对象。

## 发送 HTTP 请求


云引擎可以使用 Python 内置的 urllib，不过推荐您使用 [requests](http://docs.python-requests.org/) 等第三方模块来处理 HTTP 请求。


## 云引擎管理控制台


首先，请进入 [控制台 /（选择应用）/ 存储 / 云引擎](/cloud.html?appid={{appid}}#/leannode) 界面，可以看到左侧菜单：


菜单项|说明
---|---
<span style="white-space: nowrap;">云引擎实例</span>|展示云引擎实例的运行状态，也可以增减新的实例。
<span style="white-space: nowrap;">部署</span>|用于部署云引擎项目到预备环境或者生产环境。
定时任务|可以设置一些定时任务，比如每天凌晨清理无用数据等。
日志|用于查看云引擎项目的日志
统计|用于查看云引擎项目的一些数据统计
LeanCache|用于查看云缓存项目的实例状态，并增减新的实例。
设置|用来设置项目的源码仓库信息，包括从这里可以下载云引擎项目的初始框架代码，拷贝用于私有 git 仓库的 deploy key 等。



## 项目定义函数

如果你希望有更强的灵活性，或者希望使用主机托管功能实现自己的站点，甚至网站托管，你可以通过项目代码的方式管理自己的云引擎。

 

### 项目约束

你的项目需要遵循一定格式才会被云引擎识别并运行。


云引擎 Python 项目必须有 `$PROJECT_DIR/wsgi.py` 与 `$PROJECT_DIR/requirements.txt` 文件，该文件为整个项目的启动文件。




### 健康监测

云引擎项目在部署启动时，部署服务会对新启动的应用进行 `ping` 监测（每隔 1 秒请求一次，一共 15 次），请求 URL 为 `/__engine/1/ping`，如果响应的 `statusCode` 为 `200` 则认为新的节点启动成功，整个部署才会成功；否则会收到 **应用启动检测失败** 类型的错误信息，导致部署失败。


云引擎中间件内置了该 URL 的处理，只需要将中间件添加到请求的处理链路中即可：

```python
from leancloud import Engine
engine = Engine(your_wsgi_app)
```

如果未使用云引擎中间件，可以自己实现该 URL 的处理，比如这样：

```python
@app.route('/__engine/1/ping')
def ping():
    version = sys.version_info
    return flask.jsonify({
        'runtime': 'cpython-{0}.{1}.{2}'.format(version.major, version.minor, version.micro),
        'version': 'custon',
    })
```



### 其他框架


云引擎支持任意 Python 的 Web 框架，你可以使用你最熟悉的框架进行开发。但是请保证 `wsgi.py` 文件中有一个全局变量 `application`，值为一个 wsgi 函数。


## 部署

### 使用命令行工具部署

使用命令行工具可以非常方便地部署、发布应用，查看应用状态，查看日志，甚至支持多应用部署。具体使用请参考 [命令行工具指南](leanengine_cli.html)。

除此之外，还可以使用 git 仓库部署。你需要将项目提交到一个 git 仓库，我们并不提供源码的版本管理功能，而是借助于 git 这个优秀的分布式版本管理工具。我们推荐你使用 [CSDN Code 平台](https://code.csdn.net/)、[Github](https://github.com/) 或者 [BitBucket](https://bitbucket.org/) 这样第三方的源码托管网站，也可以使用你自己搭建的 git 仓库（比如 [gitlab.org](http://gitlab.org/)）。

### 使用 CSDN Code 托管源码

CSDN Code 是国内非常优秀的源码托管平台，你可以使用这个平台提供公有仓库和有限的私有仓库完成对代码的管理功能。以下是该平台与 LeanCloud 云引擎结合的一个例子。

首先在 CSDN Code 上创建一个项目：

![image](images/csdn_code1.png)

**提示**：在已经有项目代码的情况下，一般不推荐 **使用 README 文件初始化项目**。

接下来按照给出的提示，将源代码 push 到这个代码仓中：

```sh
cd ${PROJECT_DIR}
git init
git add *
git commit -m "first commit"
git remote add origin git@code.csdn.net:${yourname}/test.git
git push -u origin master
```

我们已经将源码成功推送到 CSDN Code 平台，接下来到 LeanCloud 云引擎的管理界面填写下你的 git 地址（请注意，一定要填写以 `git@` 开头的地址，我们暂不支持 https 协议 clone 源码）并点击 save 按钮保存：

![image](images/csdn_code2.png)

添加 deploy key 到你的 CSDN Code 平台项目上（deploy key 是我们云引擎机器的 ssh public key）保存到 **项目设置** / **项目公钥** 中，创建新的一项 avoscloud：

![image](images/csdn_code3.png)


下一步，部署源码到预备环境，进入 [云引擎 / Git 部署](/cloud.html?appid={{appid}}#/deploy) 菜单，点击「部署到开发环境」的部署按钮：


![image](images/cloud_code_5.png)

部署成功后，可以看到开发环境版本号从 undeploy 变成了当前提交的源码版本号。


### 使用 GitHub 托管源码

使用 BitBucket 与此类似，不再赘述。

[Github](https://github.com) 是一个非常优秀的源码托管平台，你可以使用它的免费账号，那将无法创建私有仓库（bucket 可以创建私有仓库），也可以付费成为高级用户，可以创建私有仓库。

首先在 Github上创建一个项目，比如就叫 `test`：

![image](images/github1.png)

![image](images/github2.png)

接下来按照 Github 给出的提示，我们将源码 push 到这个代码仓库：

```sh
cd ${PROJECT_DIR}
git init
git add *
git commit -m "first commit"
git remote add origin git@github.com:${yourname}/test.git
git push -u origin master
```

到这一步我们已经将源码成功 push 到 Github，接下来到云引擎的管理界面填写下你的 git 地址（请注意，一定要填写以 `git@` 开头的地址，我们暂不支持 https 协议 clone 源码）并点击 save 按钮保存：

![image](images/cloud_code_4.png)

并添加 deploy key 到你的 github 项目（deploy key 是我们云引擎机器的 ssh public key），如果你是私有项目，需要设置 deploy key，

拷贝 **设置** 菜单里的 **Deploy key** 保存到 github setting 里的 deploy key，创建新的一项 avoscloud：

![image](images/cloud_code_github_deploy_key.png)

下一步，部署源码到预备环境，进入 **云引擎** / **Git 部署** 菜单，点击 **部署到开发环境** 的部署按钮：

![image](images/cloud_code_5.png)

部署成功后，可以看到开发环境版本号从 undeploy 变成了当前提交的源码版本号。

### Gitlab 无法部署问题

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

## 定时任务

定时任务可以按照设定，以一定间隔自动完成指定动作，比如半夜清理过期数据，每周一向所有用户发送推送消息等等。定时任务的最小时间单位是**秒**，正常情况下时间误差都可以控制在秒级别。



定时任务是普通的云函数。比如定义一个打印循环打印日志的任务 `log_timer`：


```python
@engine.define
def log_timer():
    print 'Log in timer.'
```



部署云引擎之后，进入 [控制台 > 存储 > 云引擎 > 定时任务](/cloud.html?appid={{appid}}#/task)，在右侧页面中选择 **创建定时器**，然后设定其所执行的函数名称、执行环境等等。

定时器创建后，其状态为**未运行**，需要点击 <span class="label label-default">启用</span> 来激活。之后其执行日志可以通过 [其它 > 日志](/cloud.html?appid={{appid}}#/log) 查看。


定时任务分为两类：

* 使用 Cron 表达式（即[标准的 crontab 语法][quartz_docs]）安排调度
* 以秒为单位的简单循环调度

以 Cron 表达式为例，比如每周一早上 8 点准时发送推送消息给用户：


```python
from leancloud import push

@engine.define
def push_timer():
    data = {
        'alert': 'Public message',
    }
    push.send(data, channels=['Public'])
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

各字段以空格或空白隔开。JAN-DEC、SUN-SAT 这些值不区分大小写，比如 MON 和 mon 效果一样。更详细的使用方法请参考 [Quartz 文档（英文）][quartz_docs]。

举例如下：

表达式|说明
---|---
`0  0/5 * * * ?`|每隔 5 分钟执行一次
`10 0/5 * * * ?`|每隔 5 分钟执行一次，每次执行都在分钟开始的 10 秒，例如 10:00:10、10:05:10 等等。
<code style="white-space:nowrap;">0 30 10-13 ? * WED,FRI</code>|每周三和每周五的 10:30、11:30、12:30、13:30 执行。
`0 0/30 8-9 5,20 * ?`| 每个月的 5 号和 20 号的 8 点和 10 点之间每隔 30 分钟执行一次，也就是 8:00、8:30、9:00 和 9:30。

### 错误信息


定时器执行后的日志会记录在 [控制台 > 存储 > 云引擎 > 其它 > 日志](/cloud.html?appid={{appid}}#/log) 中，以下为常见的错误信息及原因。


- **timerAction timed-out and no fallback available.**<br/>
  某个定时器触发的云函数，因 15 秒内没有响应而超时。
- **timerAction short-circuited and no fallback available.**<br/>
  某个定时器触发的云函数，因为太多次超时而停止触发。

## 资源限制

### 权限说明

云引擎可以有超级权限，使用 master key 调用所有 API，因此会忽略 ACL 和 Class Permission 限制。你只需要使用下列代码来初始化 SDK：


```python
// 第一个参数为 App Id
leancloud.init('{{appid}}', master_key='{{masterkey}}')
```


如果在你的服务端环境里也想做到超级权限，也可以使用该方法初始化。



### 定时器数量

生产环境和预备环境的定时器数量都限制在 5 个以内，也就是说你总共最多可以创建 10 个定时器。

### 超时

请求云引擎上的云函数时会经过负载均衡设备，该设备会限制一次请求的超时时间为 15 秒，如果超过 15 秒没有返回，请求将被强制停止，但此时云引擎的方法可能仍在执行，但执行完毕后的响应是无意义的。所有 Hook 函数，如 `beforeSave` 和 `afterSave` 的超时时间限制在 3 秒内。如果 Hook 函数被其他的云函数调用（比如因为 save 对象而触发 `beforeSave` 和 `afterSave`），那么它们的超时时间会进一步被其他云函数调用的剩余时间限制。例如，如果一个 `beforeSave` 函数是被一个已经运行了 13 秒的云函数触发，那么 `beforeSave` 函数就只剩下 2 秒的时间来运行，而正常情况下是 3 秒的限制。

网站托管的动态请求超时也被限定为 15 秒。

## 日志


[云引擎 / 日志](/cloud.html?appid={{appid}}#/log)，可以查看云引擎的部署和运行日志，还可以选择查看的日志级别：


![image](images/cloud_code_11.png)

如果你想打印日志到里面查看，可以直接输出到「标准输出」或者「标准错误」，这些信息会分别对应日志的 `info` 和 `error` 级别，比如下列代码会在 info 级别记录参数信息：


```python
@engine.define
def log_something(**params):
    print params
```


**注意**：日志单行最大 4096 个字符，多余部分会被丢弃；日志输出频率大于 600 行/分钟，多余的部分会被丢弃。

## 网站托管

很多时候，除了运行在移动设备的应用之外，你通常也会为应用架设一个网站，可能只是简单地展现应用的信息并提供 App Store 或者 Play 商店下载链接，或者展示当前热门的用户等等。你也可能建设一个后台管理系统，用来管理用户或者业务数据。

这一切都需要你去创建一个 web 应用，并且从 VPS 厂商那里购买一个虚拟主机来运行 web 应用，你可能还需要去购买一个域名。

不过现在，云引擎为你提供了网站托管功能，可以让你为应用设置一个二级域名 `${your_app_domain}.leanapp.cn`（美国区为 `${your_app_domain}.avosapps.us` ），并把你的 web 应用部署到该域名之下运行，同时支持静态资源和动态请求服务。

### 设置域名


首先，你需要到 [云引擎 / 设置](/cloud.html?appid={{appid}}#/conf) 页面找到 **Web 主机域名**，在这里填写你的域名：


![image](images/cloud_code_web_setting.png)

上面将应用的二级域名设置为 **myapp**，设置之后，你应该可以马上访问：

- http://myapp.leanapp.cn
- http://myapp.avosapps.us

可能因为 DNS 生效延迟暂时不可访问，请耐心等待或者尝试刷新 DNS 缓存，如果还没有部署，你看到的应该是一个 404 页面。

## 域名备案

**备案之前要求云引擎已经部署，并且网站内容和备案申请的内容一致。仅使用云引擎托管静态文件、未使用其他 LeanCloud 服务的企业用户，需要自行完成域名备案工作。**

如果需要备案，进入 [应用控制台 > 账号设置 > 域名备案](/settings.html#/setting/domainrecord)，按照步骤填写资料即可。

## 域名绑定

**国内节点绑定独立域名需要有 ICP 备案，只有主域名需要备案，二级子域名不需要备案；如果没有 ICP 备案，请参考 [域名备案](#域名备案)。**

如果需要域名绑定，进入 [应用控制台 > 账号设置 > 域名绑定](/settings.html#/setting/domainbind)，按照步骤填写资料即可。

### 使用框架


云引擎环境中可以使用大部分 Python Web Framework，比如 [Flask](http://flask.pocoo.org/)、[web.py](http://webpy.org/)、[bottle](http://bottlepy.org/)。

事实上，您只需要提供一个兼容 WSGI 标准的框架，并且安装了云引擎的中间件，就可以在云引擎上运行。您提供的 WSGI 函数对象需要放在 `$PROJECT_DIR/wsgi.py` 文件中，并且变量名需要为 `application`。

```python
from leancloud import Engine


def wsgi_func(environ, start_response):
    // 定义一个简单的 WSGI 函数，或者您可以直接使用框架提供的
    start_response('200 OK', [('Content-Type', 'text/plain')])
    return ['Hello LeanCloud']


application = Engine(wsgi_func)
```

将这段代码放到 `wsgi.py` 中，就可以实现一个最简单的动态路由。








### 获取客户端 IP

如果你想获取客户端的 IP，可以直接从用户请求的 HTTP 头的 `x-real-ip` 字段获取。









### 自动重定向到 HTTPS

为了安全性，我们可能会为网站加上 HTTPS 加密传输。我们的云引擎支持网站托管，同样会有这样的需求。

因此我们在云引擎中提供了一个新的 middleware 来强制让你的 `${your_app_domain}.leanapp.cn` 的网站通过 https 访问，你只要这样：



```python
from leancloud import HttpsRedirectMiddleware

# app 为您的 wsgi 函数
app = HttpsRedirectMiddleware(app)
engine = Engine(app)
application = engine
```



部署并发布到生产环境之后，访问你的云引擎网站二级域名都会强制通过 HTTPS 访问。

### 预备环境和开发环境

前面已经谈到云引擎的预备和生产环境之间的区别，可以通过 HTTP 头部 `X-LC-Prod` 来区分。但是对于网站托管就没有办法通过这个 HTTP 头来区分了。

因此，我们其实为每个应用创建了两个域名，除了 `${your_app_domain}.leanapp.cn` 之外，每个应用还有 `stg-${your_app_domain}.leanapp.cn` 域名作为预备环境的域名。

部署的测试代码将运行在这个域名之上，在测试通过之后，通过菜单 **部署** > **部署到生产环境** 按钮切换之后，可以在 `${your_app_domain}.leanapp.cn` 看到最新的运行结果。

## 第三方平台接入

因为云引擎提供了主机托管功能，这相当于为你在互联网上提供了一台简单的 VPS（虚拟主机），你可以用它接入第三方平台（很多第三方平台需要你有回调服务器），完成一些特定的工作。

### 接入支付宝

通过 [cloud-code-alipay 示例](https://github.com/leancloud/cloud-code-alipay) 了解如何接入支付宝，实现「即时到账收款」的功能。

### 接入微信

通过 [cloud-code-weixin 示例](https://github.com/leancloud/cloud-code-weixin) 了解如何接入微信，实现「开发者认证」和「自动回复」的功能。

## 运行环境区分

云引擎区分「预备环境」和「生产环境」，两个环境虽然使用 [不同的二级域名](#部署到云引擎)，但是使用相同的数据源。代码在预备环境测试通过后，再部署到生产环境是更安全的做法。

有些时候你可能需要知道当前云引擎运行在什么环境（开发环境、预备环境或生产环境），从而做不同的处理：


```python
import os

if os.environ.get('LC_APP_PROD') == '1':
    # 当前为生产环境
elif os.environ.get('LC_APP_PROD') == '0':
    # 当前为预备环境
else:
    # 当前为开发环境
```


你应该注意到了，我们在请求云引擎的时候，通过 REST API 的特殊的 HTTP 头 `X-LC-Prod`，来区分调用的环境。

* 0 表示调用预备环境
* 1 表示调用生产环境

具体到 SDK 内的调用，请看各个平台的 SDK 指南。

有些时候请求云引擎时会提示 production 还没有部署：

```json
{"code":1,"error":"The cloud code isn't deployed for prod 1."}
```

这个错误通常是只是部署了预备环境。通过点击菜单 **Git 部署** / **部署到生产环境** / **部署**，可以将开发环境的当前版本的代码部署到生产环境：

![image](images/cloud_code_deploy_prod.png)



## 时区问题

因为某些原因，云引擎 2.0 默认使用的是 UTC 时间，这给很多开发者带来了困惑，所以我们着重讨论下时区问题。

比如有这样一个时间：`2015-05-05T06:15:22.024Z` (ISO 8601 表示法)，最后末尾的 `Z` 表示该时间是 UTC 时间。

上面的时间等价于：`2015-05-05T14:15:22.024+0800`，注意此时末尾是 `+0800` 表示该时间是东八区时间。这两个时间的「小时」部分相差了 8 小时。

### 时区问题产生的原因

很多开发者在时间处理上会忽略「时区」标志，导致最后总是莫名其妙的出现 8 小时的偏差。

【场景一】某开发者开发的应用使用云引擎的主机托管功能做了一个网站，其中有时间格式的表单提交。某用户使用浏览器访问该网站，提交表单，时间格式为：`2015-05-05 14:15:22.024`，注意该时间没有「时区」标志。因为这个时间是浏览器生成的，而该用户浏览器上的时间通常是东八区时间，所以该业务数据希望表达的时间是「东八区的 14 点」。

该时间 `2015-05-05 14:15:22.024` 提交到服务器，被转换为 Date 类型（JavaScript 代码：`new Date('2015-05-05 14:15:22.024')`）。因为云引擎 2.0 使用的是 UTC 时间，所以该时间会被处理为 `2015-05-05T14:15:22.024Z`，即「UTC 时间的 14 点」。导致最后获得的时间和期望时间相差了 8 小时。

解决上面的办法很简单：时间格式带上时区标志。即浏览器上传时间时使用 `2015-05-05T14:15:22.024+0800`，这样不管服务端默认使用什么时区，带有时区的时间格式转换的 Date 都不会有歧义。

【场景二】从数据库获取某记录的 `createdAt` 属性，假设值为：`2015-04-09T03:35:09.678Z`。因为云引擎默认时区是 UTC，所以一些时间函数的返回结果如下：

函数|结果
---|---
`toISOString`|2015-04-09T03:35:09.678Z
`toLocaleString`|Thu Apr 09 2015 03:35:09 GMT+0000 (UTC)
`toUTCString`|Thu, 09 Apr 2015 03:35:09 GMT
`toString`|Thu Apr 09 2015 03:35:09 GMT+0000 (UTC)
`getHours`|3，如果将 getHours 的结果返回给浏览器，或者作为业务数据使用，则会出现 8 小时的偏差。

如果需要获取小时数据，解决办法是使用第三方的组件，比如 [moment-timezone](http://momentjs.com/timezone/)，通过下面的方式可以获得东八区的小时时间：

```javascript
var time = moment(obj.createdAt).tz('Asia/Shanghai');
console.log('toString', time.toString());
console.log('getHours', time.hours())
```

### 云引擎 2.0 和云引擎时区的差异

为了方便大家的使用，更加符合通常的习惯，云引擎运行时环境（无沙箱的 Node.js 环境和 Python 环境）都是使用**东八区**作为默认时区。当然，我们仍然建议程序的时间字符串带有时区标志。


[quartz_docs]: http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger  "Quartz 文档"
