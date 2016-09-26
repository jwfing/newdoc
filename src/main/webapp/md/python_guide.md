# Python 数据存储开发指南


## 介绍

LeanCloud 是一个完整的平台解决方案，为你的应用提供全方位的后端服务。我们的目标是让你不需要进行后端开发及服务器运维等工作就可以开发和发布成熟的应用。

对于熟悉 Python 的用户，我们提供了 Python 语言版本的 SDK ，方便开发。

## 快速入门

建议你在阅读本文档之前，阅读我们提供的[快速入门](/start.html)文档，获取 LeanCloud 使用的配置和第一印象。

### 兼容性

目前 LeanCloud Python SDK 基于 Python 2.x 开发，在 Python 3.x 环境下使用仍然有一些兼容性问题。我们会尽快改善这一问题的。

### 安装

你可以使用 `pip` 或者 `easy_install` 安装 Python SDK

```sh
pip install leancloud-sdk
```

or

```sh
easy_install leancloud-sdk
```

根据你的环境，命令之前可能还需要加上 `sudo` 。

另外，我们推荐你使用 [virtualenv](https://virtualenv.pypa.io/en/latest/) 创建与系统全局环境隔离的 Python 环境，防止不同项目之间依赖的第三方模块版本之间产生冲突。

**注意**：如果您的 Python 版本低于 2.7.9，您可能会遇到如下的 Warning：

```
/usr/lib/python2.7/site-packages/requests-2.6.0-py2.7.egg/requests/packages/urllib3/util/ssl_.py:79: InsecurePlatformWarning: A true SSLContext object is not available. This prevents urllib3 from configuring SSL appropriately and may cause certain SSL connections to fail. For more information, see https://urllib3.readthedocs.org/en/latest/security.html#insecureplatformwarning.
InsecurePlatformWarning
```

建议您升级您的 Python 版本，或者通过安装 PyOpenSSL 来解决：

```sh
pip install pyopenssl ndg-httpsclient pyasn1
```


### 配合 gevent 使用

Python SDK 使用 Python 内置网络连接库，所有的网络操作都是阻塞的。如果你的代码需要应对高并发场景，推荐使用 gevent 来提高性能。

```python
from gevent import monkey

monkey.patch_all()  # 或者只 patch 指定的模块
```

请注意**上述代码一定要在其他代码之前执行**，比如说写在你的模块的 `__init__.py` 最前面。

关于 gevent 的详细介绍，可以参考[gevent 官方文档](http://www.gevent.org/)。

另外如果你使用我们的云引擎环境来运行 Python SDK，以上的步骤是不需要的，我们默认开启了 gevent 支持。

## 初始化

在调用 SDK 前，需要进行初始化。

```python
import leancloud

leancloud.init('APP_ID', 'APP_KEY')

# 或者您现在需要使用 master key 的权限
leancloud.init('APP_ID', master_key='MASTER_KEY')
```

## 对象

你可以通过子类化 `leancloud.Object` 来创建自己的类，使用此类生成对象再保存，将会将数据保存到 LeanCloud 数据服务上，类名对应的表中。

```python

from leancloud import Object

class GameScore(Object):
    def is_cheated(self):
        # 可以像正常 Python 类一样定义方法
        return self.get('cheatMode')

    @property
    def score(self):
        # 可以使用property装饰器，方便获取属性
        return self.get('score')

    @score.setter
    def score(self, value):
        # 同样的，可以给对象的score增加setter
        return self.set('score', value)

# or: GameScore = Object.extend('GameScore')
```

### 保存对象

调用实例对象的save方法，即可保存对象。

```python
game_score = GameScore()
game_score.set('score', 42)  # or game_score.score = 42
game_score.set('cheatMode', False)
game_score.set('playerName', 'Marvin')
game_score.save()

# 还可以通过关键字参数，在创建对象的同时进行赋值
game_score = GameScore(score=42, playerName='Marvin')
```

这时候登录 LeanCloud 控制台查看，可以看到 GameScore 中新增一条数据。

另外所有 Object 子类对象，都有三个特殊字段，保存之后服务器会自动填充其中的数据。

```python
game_score.id            # 此对象的id，类型为 unicode ，对应控制台的 objectId
game_score.created_at    # 此对象创建的时间，类型为 datetime.datetime ，对应控制台的 createdAt
game_score.updated_at    # 此对象最后更新的时间，类型为 datetime.datetime，对应控制台的 updatedAt
```

### 检索对象

检索对象可以使用 leancloud.Query 类来进行。

如果事先知道一个对象的 objectId ，可以这样做:

```python
from leancloud import Query
# Query 构造函数的参数可以是字符串，也可以是一个 leancloud.Object 子类。
query = Query(GameScore)
game_score = query.get('520ca0bbe4b07e8e0e847e31')
print game_score.get('playerName')
```

### 更新对象

更新对象的时候，直接修改对象上对应字段的值，然后再调用 `save` 方法即可。

```python
from leancloud import Object
# Object.extend('GameScore') 和 class GameScore(leancloud.Object): 
# 的写法是一样的，返回的结果是一个 class，所以是变量名大写
GameScore = Object.extend('GameScore')

game_score = GameScore()
game_score.set('score', 42)
game_score.set('cheatMode', False)
game_score.set('playerName', 'Marvin')
game_score.save()

game_score.set('score', 43)
game_score.save()
```

### 计数操作

很多应用场景都需要进行一些计数操作，比如记录游戏分数，论坛帖子回帖数等等。如果直接从服务器获取这些字段的值，然后简单的加减值再进行保存，这个时候很有可能服务器上的数据已经有了更新，会将服务器的数据覆盖掉。这往往不是我们想要的结果。因此可以使用`increment`方法来进行计数操作，我们只需要将需要增减的值传递给服务器就可以了。

```python
from leancloud import Object
GameScore = Object.extend('GameScore')

game_score = GameScore()
game_score.set('score', 42)
game_score.set('cheatMode', False)
game_score.set('playerName', 'Marvin')
game_score.save()

game_score.increment('score', 1)
game_score.save()
```

### 删除字段

有时候需要将对象上的一个字段设置为空，可以使用`unset`方法。

```python
game_score.unset('score')
game_score.save()
```

### 删除对象

如果你想要删除服务器上的一个对象，可以使用`destroy`方法。

```python
game_score.destroy()
```

批量删除对象可以使用 `leancloud.Object.destroy_all()` 方法：

```python
objects = [obj1, obj2, obj3]
leancloud.Object.destroy_all(objects)
```

### 关系数据

leancloud 后端存储支持一对一，一对多，多对多数据建模。

#### 一对一关系和一对多关系

一对一关系和一对多关系都可以通过在一个`Object`对象内保存另一个对象来实现。比如一个`Post`下可以允许多个`Comment`对象，一个`Comment`只能属于一个`Post`对象，可以这样实现：

```python
Post = Object.extend('Post')
Comment = Object.extend('Comment')

post = Post()
post.set('title', 'I am Hungry')
post.set('content', 'Where should we go for lunch?')

comment = Comment()
comment.set('content', 'Let us do Sushirrito.')

comment.set('parent', post)

comment.save()
```

在父对象上调用`save`方法，SDK 会同时保存两个对象。

如果想将一个已经保存在了服务器上的对象关联到新对象上，可以只通过现有对象的 `objectId` 来进行关联。

```python
post = Post()
post.id = '520c7e1ae4b0a3ac9ebe326a'
# or: post = Post.create_without_data('520c7e1ae4b0a3ac9ebe326a')
comment.set('parent', post)
```

默认情况下，从服务器上获取一个对象时并不会获取与它关联对象的值。可以这样显式获取：

```python
post = comment.get('parent')
post.fetch()
```

#### 多对多关系

多对多关系可以使用 `leancloud.Relation` 来建立。比如 `User` 可以将 `Post` 添加进自己 `like` 的列表中，可以这样实现：

```python
relation = user.relation('likes')
relation.add(post)
user.save()
```

可以在 `likes` 中删除一个 `post`:

```python
relation = user.relation('likes')
relation.remove(post)
user.save()
```

`relation` 中关联的对象并不会下载到本地。可以用 `query` 方法来返回一个 `leancloud.Query` 对象，来获取 `relation` 中的对象列表，比如：

```python
relation = user.relation('likes')
query = relation.query()
posts = query.find()
```

此时 `query` 对象即是 `leancloud.Query` 的实例，可以增加一些查询条件，比如：

```python
relation = user.relation('likes')
query = relation.query().equal_to('title', 'I am Hungry')
posts = query.find()
```

如果想查询所有 like 了某个 Post 的用户，可以使用 `reverse_query` 方法来进行反向查询：

```python
from leancloud import Relation

query = Relation.reverse_query('User', 'likes', post)
users = query.find()
```

### 数据类型

LeanCloud Python SDK 支持大部分 Python 内置类型。

```python
from datetime import datetime
from leancloud import Object

obj = Object.extend('myObject')()
obj.set('myNumber', 2.718)
obj.set('myString', 'foobar')
obj.set('myDate', datetime.now())
obj.set('myArray', [1, 2, 3, 4])
obj.set('myDict', {'string': 'some string', 'number': 1})
obj.set('myNone', None)
obj.save()
```

需要注意的是，Object 对象序列化之后的大小不应该超过 128KB。

## 查询

### 基础查询

我们可以通过构造 `leancloud.Query` 对象，来进行复杂查询。

```python
from leancloud import Object
from leancloud import Query

GameScore = Object.extend('GameScore')
query = Query(GameScore)  # 这里也可以直接传递一个 Class 名字的字符串作为构造参数
query.equal_to('playerName', 'Dan Stemkoski')
gameScores = query.find()
```

### 查询条件

有几种方式来设置查询条件。 你可以用 not_equal_to 方法和一个特定的值来过滤不符合要求的对象:

```python
query.not_equal_to("playerName", "Michael Yabuti")
```

你可以给定更多的条件，只有满足所有条件的对象才会作为结果返回。

```python
query.not_equal_to("playerName", "Michael Yabuti")
query.greater_than("playerAge", 18)
```

你可以用设定 limit 的方法来限定返回的结果数，默认的返回结果数是 100，但是任何 1 到 1000 之间的数值都是合法的，在 0 到 1000 范围之外的都强制转成默认的 100。

```python
query.limit(10) # limit to at most 10 results
```

如果你只想要一个结果，一个更加方便的方法可能是使用 first，而不是 find 方法。

```python
GameScore = Object.extend('GameScore')
query = Query(GameScore)
query.equal_to('playerEmail', 'dstemkoski@example.com')
game_score = query.first()
```

你可以用 skip 跳过前面的结果，这可能对于分页很有用。

```python
query.skip(10)  # skip the first 10 results
```

对于可以排序的类型，比如 int / datetime 和 str，你可以控制返回结果的顺序:

```python
# Sorts the results in ascending order by the score field
query.ascending("score")

# Sorts the results in descending order by the score field
query.descending("score")
```

对于可以排序的类型，你同样可以在查询中进行比较。

```python
# Restricts to wins < 50
query.less_than("wins", 50)

# Restricts to wins <= 50
query.less_than_or_equal_to("wins", 50)

# Restricts to wins > 50
query.greater_than("wins", 50)

# Restricts to wins >= 50
query.greater_than_or_equal_to("wins", 50)
```

如果想让返回的对象的某个属性匹配多个值，你可以使用 contained_in，提供一个数组就可以了。这样通常可以用单个的查询来获取多个结果。比如你想获取某几个玩家的分数:

```python
# Finds scores from any of Jonathan, Dario, or Shawn
query.contained_in("playerName", ["Jonathan Walsh", "Dario Wunsch", "Shawn Simon"])
```

相反地，你可以使用 not_contained_in 方法来查询在集合之外的目标对象。

如果你想要查询含有某一特定属性的对象，你可以使用 exists。相对地，如果你想获取没有这一特定属性的对象，你可以使用 `does_not_exists`。

```python
# Finds objects that have the score set
query.exists("score")

# Finds objects that don't have the score set
query.does_not_exists("score")
```

你可以使用 `matches_key_in_query` 方法来进行嵌套的子查询。举例说，如果你有一个类包含了运动队，而你在用户的类中存储了用户的家乡信息，你可以构造一个查询来查找某地的运动队有赢的记录的用户。查询应该看起来像下面这样:

```python
from leancloud import Object
from leancloud import Query
from leancloud import User

Team = Object.extend("Team")
team_query = Query(Team)
team_query.greater_than("winPct", 0.5)
user_query = Query(User)
user_query.matches_key_in_query("hometown", "city", team_query)

# results has the list of users with a hometown team with a winning record
results = user_query.find()
```

相对地，可以使用 `does_not_match_key_in_query` 来获取属性不在子查询结果中的对象。比如为了获得用户的家乡队输了的情况:

```python
losing_user_query = Query(User)
losing_user_query.does_not_match_key_in_query("hometown", "city", teamQuery)

# results has the list of users with a hometown team with a losing record
results = losingUserQuery.find()
```

你可以用 select 和一个 keys 的列表来限定返回的字段。为了获得只包含 score 和 playername 字段的文档 ( 包括 build-in 的字段，objectId，createdAt，updatedAt):

```python
GameScore = Object.extend("GameScore")
query = Query(GameScore)
query.select("score", "playerName")

# each of results will only have the selected fields available.
results = query.find()
```

剩下的字段可以之后用返回的对象的 fetch 方法来获取:

```python
result = query.first().fetch()
```

### 对数组值做查询

对于 value 是数组的情况，你可以这样查询数组中的值有 2 的情况的对象:

```python
# Find objects where the array in arrayKey contains 2.
query.equal_to("arrayKey", 2)
```

你同样可以用下面的方式找到同时包含元素 2，3，4 的数组:

```python
# Find objects where the array in arrayKey contains all of the elements 2, 3, and 4.
query.contains_all("arrayKey", [2, 3, 4])
```

### 对字符串类型做查询

使用 startswith 来限制属性值以一个特定的字符串开头，这和 MySQL 的 LIKE 操作 符很像，因为有索引所以对于大的数据集这个操作也是很高效的。

```python
# Finds barbecue sauces that start with "Big Daddy's".
query = leancloud.Query(BarbecueSauce)
query.startswith("name", "Big Daddy's")
```

### 关系查询

对于查询关系型数据来说有几种不同的方式，如果你想要获取的对象中有某个属性 包含一个特定的 leancloud.Object，你可以使用 equal_to，就像对于别的数据类型一样。举个例子，如果每一个 Comment 在它的 post 字段都有一个 Post 对象，你可以通过 如下的方式来获取一个 Post 的 comment:

```python
# Assume leancloud.Object my_post was previously created.
query = leancloud.Query(Comment)
query.equal_to("post", my_post)
comments = query.find()
# comments now contains the comments for my_post
```

如果你想得到其字段中包含的子对象满足另一个查询的结果，你可以使用 matches_query 操作。**注意默认的结果条数限制 100 和最大 limit 1000 也同样适用于子查询，所以对于大的数据集你可能需要小心构建你的查询，否则可能出现意料之外的状况**。例如，为了找到 post 中有图片的 comment，你可以:

```python
inner_query = leancloud.Query(Post)
inner_query.exists("image")
query = leancloud.Query(Comment)
query.matches_query("post", inner_query)
comments = query.find()
# comments now contains the comments for posts with images.
```

如果你想要获取某字段中包含的子对象不满足指定查询的结果，你可以使用 does_not_match_query。例如，为了找到针对不含图片的 post 的 comment，你可以这样:

```python
inner_query = leancloud.Query(Post)
inner_query.exists("image")
query = leancloud.Query(Comment)
query.does_not_match_query("post", inner_query)
query.find()
# comments now contains the comments for posts without images.
```

你可以同样用 objectId 来做关系查询

```python
post = Post()
post.id = "520c7e1ae4b0a3ac9ebe326a"
query.equal_to("post", post)
```

在某些情况下，你可能希望查询结果中包含多个相关联的其他数据类型。你可以使用 include 方法。比如: 假设你想获得最新的 10 个 comment，你可能想同时获取它们相关的 post 数据:

```python
query = leancloud.Query(Comment)

# Retrieve the most recent ones
query.descending("createdAt")

# Only retrieve the last ten
query.limit(10)

# Include the post data with each comment
query.include("post")

comments = query.find()
# Comments now contains the last ten comments, and the "post" field
# has been populated. For example:
for comment in comments:
    # This does not require a network access.
    post = comment.get("post")
```

你同样可以用点操作符来做多级查询，如果你想同时找到 comment 的 post 和相应 post 的 author，你可以这样做:

```python
query.include(["post.author"])
```

你可以多次使用 include 来构建一个有多个字段的查询，这项功能同样适用于 leancloud.Query 的 helper 函数例如 first 和 get。

### 对象计数
``
如果你只是想查询满足一个 query 的结果集到底有多少对象，但是你不需要得到它们，你可以使用 count 来取代 find。比如，为了获得某个玩家到底玩过多少局游戏:

```python
query = leancloud.Query(GameScore)
query.equal_to("playerName", "Sean Plott")
count = query.count()
# The count request succeeded. Show the count
print "Sean has played %d games" % count
```

对于超过 1000 个对象的类来说，count 操作会被时间限制所约束。它们可能会一直 返回超时错误，或者只是返回一个近似正确的值。这样的话你应该更合理地规划你程序的结构来避免这种情况。

### 组合查询

如果你想要查找满足一系列查询的对象，你可以使用 Query.or_ 方法来构建查询，这样得到的结果是所有查询的并集。比如你想要找的玩家或者是有很多或者很少的胜利的时候，你可以这样:

```python
from leancloud import Query

lots_of_wins = Query("Player")
lots_of_wins.greater_than("wins", 150)

few_wins = Query("Player")
few_wins.less_than("wins", 5)

main_query = Query.or_(lots_of_wins, few_wins)
results = mainQuery.find()
# results contains a list of players that either have won a lot of games or won only a few games.
```

你也可以使用 Query.and_ 对 Query 加入更多的条件，如同 AND 查询一样，这样得到所有查询结果的交集。

请注意 **我们不会在组合查询的子查询中支持非过滤型的条件**（比如:limit, skip, ascending/descending, include）。

### CQL 查询语言

我们允许使用类 SQL 语法的 CQL 查询语言来查询 LeanCloud 应用内的数据，例如：

```python
from leancloud import Query

result = Query.do_cloud_query('select * from GameScore')
# results 是查询返回的结果，Object 列表
results = result.results
# do something with results...

# 查询分数大于 50 的记录数并返回前100条。
result = Query.do_cloud_query('select count(*), * from GameScore where score>50')
# results 是查询返回的结果，Object 列表
results = result.results
# count 表示符合查询条件的总记录数
count = result.count
```

do_cloud_query 返回的 result 包含三个属性：

- results - 查询结果的 AV.Object 列表
- count - 如果使用了 `select count(*)` 的查询语法，返回符合查询条件的记录数目
- class_name - 查询的 class name

CQL 语法请参考 [CQL 详细指南](./cql_guide.html)。

针对查询条件，我们推荐使用占位符的 CQL 语句来提升性能，占位符对应的值按照顺序组合起来作为第二个参数 pvalues 数组传入：

```python
# 查询分数大于 50 的记录数并返回前10条
result = Query.do_cloud_query('select count(*), * from GameScore where score > ? limit ?', 50, 10)
# results 是查询返回的结果，Object 列表
results = result.results
# count 表示符合查询条件的总记录数
count = result.count
# do something with results ...
```

## 文件

leancloud.File 让你可以在 LeanCloud 中保存应用的文件，这样可以解决用一个 AV.Object 存太大或者太难处理的问题。最常见的用例就是存储图片，但是你可 以随意用来存储文档，视频，音乐或者任何二进制数据。

### 创建

我们可以通过一个 Python 内置的 StringIO 、 cStringIO 、 buffer 或者 file 类型对象来创建 leancloud.File。

```python
from leancloud import File
from StringIO import StringIO


file1 = File('fileFromStringIO', StringIO('data'))

local_file = open('~/avatar.png')
file2 = File('fileFromLocalFile', local_file)
local_file.close()

file3 = File('fileFromBuffer', buffer('\x42\x43\x44'))

# 还可以指定文件的mime type，如果不指定的话会根据文件名后缀来猜测
file4 = File('truth', StringIO('{"truth": 42}'), 'application/json')
```

需要注意的有两点：

- 你不需要担心文件名重复的问题，每一次上传都会有一个独一无二的标识符，所以上传多个文件都叫 photo.jpg 是没有问题的。
- 你应该给你的文件一个扩展名，或者明确指定 mime_type ，这样会让 LeanCloud 明白文件的类型，并且会按文件类型来进行处理。

另外还可以直接创建一个只有 URL 没有数据的 leancloud.File 对象，用来引用现有网络上的文件。

```python
file = File.create_with_url('avatar.jpg', 'http://www.example.com/avatar.jpg')
```

在一个 File 实例上调用 save 方法，可以将文件上传至服务器。我们还可以将一个 File 对象和 一个 Object 对象关联起来，作为其中的一个字段。

```python
f = open(avatar_file_path)
avatar_image = File('avatar.png', f)
f.close()
avatar_image.save()

user.set('avatar', avatar_image)
user.save()
```

对象需要注意的一点是，**leancloud.File 不是 leancloud.Object 的子类，虽然它们之间有很多类似的方法**。


### 获取文件内容

现阶段拿到一个 File 对象，可以通过它的 URL 自行下载数据。

```
url = file.url
```

### 缩略图

如果 File 保存的是图片文件，可以使用 get_thumbnail_url 方法很方便的获取图片的缩略图。

```python
thumbnail_url = file.get_thumbnail_url(width='100', height='100')
```

### 删除文件

获取了一个 File 对象之后，只要调用 `destroy` 方法即可在服务器上删除此 File 对象。

## 用户

绝大部分应用都有「用户」这个一概念。我们提供了 leancloud.User 类，可以用来处理用户相关的逻辑。

与 File 类不同，User 类是 Object 的子类，你可以像 Object 一样使用 User 类。不过 User 类有一些用户系统相关的功能。

### 属性

User 类有一些与 Object 类不一样的属性：

- username : 用户的用户名 (必须提供)
- password : 用户的密码 (在注册的时候必须提供)
- email : 用户的 email(可选)
- mobilePhoneNumber: 用户的手机号码（可选）
- 我们会在下面的用例中详细介绍细节

### 创建用户

创建用户就是常见应用提供的注册功能。

```python
from leancloud import User

user = User()
user.set("username", "my name")
user.set("password", "my pass")
user.set("email", "email@example.com")

# other fields can be set just like with leancloud.Object
user.set("phone", "415-392-0202")

user.sign_up()
# Hooray! Let them use the app now.
```

这个调用会在在你的应用中创建一个新的用户。在这样做之前，同样会确认用户名和 email 在应用内都是唯一的。为了安全我们会将密码散列过后存储在 LeanCloud 中。我们从不会将用户密码以明文存储，我们也不会用明文向任何客户端发送密码。

**注意我们使用了 sign_up 方法而不是 save 方法。新的 User 永远应该使用 sign_up 方法来新建。而随后的用户的信息更新可以调用 save 来做**。

如果一个 sign_up 没有成功的话，方法调用会抛出异常。最常见的问题是 username 或者 email 已经被其他用户所使用了。你应该清楚地反馈给你的用户，让他们再次用一个不同的用户名来注册。

你也可以使用 email 来作为用户名，只要求你的用户输入他们的 email 但是同时自动填充好 username 属性就可以了，User 会跟原来一样工作。我们会在下面的重设密码环节再次说明这个细节。

### 登录

在你要求你的用户注册之后，当然应该让他们在以后用自己的账户登录进来。你可 以使用 login 方法来进行登录。

```python
User().login("myname", "mypass")
```

### 修改密码

用户修改密码，跟修改其他属性没有什么区别，前提是要求登录状态：

```python
user.set('password', 'new password')
user.save()
```

## 角色

随着你的 App 规模和用户基数的成长，你可能发现你需要比设定用户级的权限更加宽泛的权限设置。LeanCloud 提供一种基于角色的权限管理方案来满足这种需求，角色提供了一种逻辑的方式来将用户分组并给与相同的权限。角色是一种有名字的对象，包含了用户和其他的角色。任何授予一个角色的权限会被它包含的所有用户和子角色所继承。

例如，在你的 App 中管理着一些内容，你可能有一些类似于 「主持人」 的角色可以修改和删除其他用户创建的新的内容，你可能还有一些 「管理员」 有着与 「主持人」 相同的权限，但是还可以修改 App 的其他全局性设置。通过给予用户这些角色，你可以保证新的用户可以做主持人或者管理员，不需要手动地授予每个资源的权限给各个用户。

我们提供一个特殊的类称为 leancloud.Role 在客户端代码中表示这种角色对象，Role 是一个 Object 的子类，而且有所有的特性，比如没有固定模式，自动持久化和 key value 接口等。所有的在 Object 上有用的方法在 Role 上依然有作用。不同之处是 Role有一些普通对象没有的特殊属性和方法。

### 属性

Role 有一些属性与普通的 Object 不同：

- name 角色的名称，这个值是必须的，而且只能在角色创建的时候指定一次，名字 必须由字母，数字，空格，减号或者下划线组成。名称会被用于表示角色名而不需 要角色的 objectId
- users 一个关系，包含了会继承角色权限的 User
- roles 一个关系，包含了会继承角色权限的子角色

### 角色对象的安全性

Role 使用和其他 LeanCloud 对象一样的 ACL 权限策略，除开它需要 ACL 被显式地设置以外。通常来说，只有用户有极大的权限（比如管理员）才应该被允许创建或者更改 Role。所以你应该按这种标准来设定 Role 的 ACL。请注意，**如果你给了用户一个 Role 一个写权限，这个用户有可能会在这个权限中加入另一个 user，或者甚至直接把角色删除掉**。

为了创建一个新的 Role，你应该如下写：

```python
from leancloud import ACL
from leancloud import Role

# By specifying no write privileges for the ACL, we can ensure the role cannot be altered.
role_acl = ACL();
role_acl.set_public_read_access(True)
role = Role('Administrator', role_acl)
role.save()
```

你可以通过增加 "user" 和 "roles" 关系的成员来在 Role 中加入用户或者子角色：

```python
role = Role(role_name, role_acl);
for u in users_to_add_to_tole:
  role.get_users().add(u)
for r in roles_to_add_to_tole.length:
  role.get_roles().add(r)
role.save()
```

请非常注意一点，**注册角色的 ACL 的时候，它们只能被应该有权限修改它的人修改**。

### 其他对象的安全性

现在你应该已经创建了在你的程序中要使用的一系列的角色，你可以用 ACL 来定义他们的用户可以拥有的权限。每一个 Object 都可以指定一个 ACL，这样提供了哪些用户或者角色应该有权限来读或者写这个对象。

将一个读或者写的权限授予一个角色是很直观的：

```python
moderators = your_moderators_rols
wall_post = Object.create("WallPost")
post_acl = ACL()
post_acl.set_role_write_access(moderators, True)
wall_post.set_acl(post_acl)
wallPost.save()
```

你可以不需要查找这个 Role，直接把名字提供给 ACL：

```python
wall_post = Object.create("WallPost")
post_acl = ACL()
post_acl.set_role_write_access("Moderators", True)
wall_post.set_acl(post_acl)
wall_post.save()
```

### 角色继承

就像上面所描述的一样，一个角色可能包含其他的角色，表示两个角色之间的父 - 子关系，这样做的结果就是任何被授予一个角色的权限都会被隐式地授予这个角色的所有子角色。

这样的关系很经常会在有用户管理内容的程序之中看到，比如论坛，有一个很少量 的用户称为管理员，有最高的权限，比如程序设定，创建新的论坛，设定所有人能看 到的内容等等。另一类有一部分类似于「版主」的用户，这些人有责任保持用户创建的内容是合适的，任何一个「版主」有的权限「管理员」都应该有。为了启用这种关系，你应该使「管理员」成为「版主」的一个子角色。

```python
administrators = your_administrators_role
moderators = your_moderators_role
moderators.get_roles().add(administrators)
moderators.save()
```

## 地理位置

LeanCloud 允许你能够将真实世界的经度和纬度坐标放入对象之中。在 Object 中 加入一个 leancloud.GeoPoint 可以让你查询一个 Object 离一个参考点的相对位置。这允许你轻松的发现一个用户周围最近的用户，或者离一个用户最近的地点。

### GeoPoint

为了将一个对象联系到一个点上，你需要先创建一个 GeoPoint。举例来说，为了创建一个地理位置在纬度 40 度，经度在 -30 度的点：

```python
from leancloud import GeoPoint

point = GeoPoint(latitude=40.0, longitude=-30.0)

place_object.set("location", point)
```

注意：**现在我们只支持一个类中只能有一个 key 能对应 AV.GeoPoint**。

### 地理位置查询

现在你可以有一系列的对象可以做空间坐标查询了，如果能轻松地发现有哪些对象离一个点最近就好了。这样可以通过在 Query 中加入一个 near 来做查询，为了获得离用户最近的 10 个地点列表。可以这样：

```python
# User's location
user_geo_point = user_object.get("location")

# Create a query for places
query = Query(PlaceObjectClass)

# Interested in locations near user.
query.near("location", user_geo_point)

# Limit what could be a lot of points.
query.limit(10)

# Final list of objects
places_objects = query.find()
```

在这时 place_objects 会返回一个按离 user_geo_point 的距离排序的列表，注意如果一个 ascending()/descending() 给了查询的话，会取代按距离排序这项特性。

在这里是有一些问题是值得留心的：

- 每一个 Object 只能有一个键指向一个 GeoPoint 对象
- Points 不应该等于或者超出它的界，纬度不应该是 -90.0 或者 90.0，经度不应该是 -180.0 或者 180.0。试图在 GeoPoint 上使用超出范围内的经度和纬度会导致问题。
