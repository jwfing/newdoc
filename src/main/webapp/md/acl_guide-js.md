


# JavaScript SDK 权限管理使用指南

## 其他语言代码示例
* [iOS / OS X](acl_guide-ios.html)
* [Android](acl_guide-android.html)
* JavaScript
* [Python](acl_guide-python.html)

## 简介
数据安全在应用开发的任何阶段都应该被重视。因此本文档我们详细介绍了在选择 LeanCloud 作为后端服务之后，如何使用 LeanCloud 提供的安全功能模块为开发者的应用以及数据提供安全保障。

如果您尚未对权限管理以及 ACL 的进行过了解，请查看 [权限管理以及 ACL 快速指南](acl_quick_start-js.html)。

## 需求场景

列举一个场景：
假设我们要做一个极简的论坛：用户只能修改或者删除自己发的帖子，其他用户则只能查看。


### 云引擎使用 ACL
文档中使用的 `AV.User.current()` 这个方法仅仅针对浏览器端有效，在**云引擎中该接口无法使用**。云引擎中获取用户信息，请参考云引擎文档[处理用户登录和登出](leanengine_webhosting_guide-node.html#处理用户登录和登出)


## 基于用户的权限管理

### 单用户权限设置
以上需求在 LeanCloud 中实现的步骤如下：

1. 写一篇帖子
2. 设置帖子的「读」权限为所有人可读。
3. 设置帖子的「写」权限为作者可写。
4. 保存帖子

实例代码如下：



```js
  // 新建一个帖子对象
  var Post = AV.Object.extend('Post');
  var post = new Post();
  post.set('title', '大家好，我是新人');

  // 新建一个 ACL 实例
  var acl = new AV.ACL();
  acl.setPublicReadAccess(true);
  acl.setWriteAccess(AV.User.current(),true);

  // 将 ACL 实例赋予 Post 对象
  post.setACL(acl);
  post.save().then(function() {
    // 保存成功
  }).catch(function(error) {
    console.log(error);
  });
```



以上代码产生的效果在 **控制台** > **存储** > **Post 表** 可以看到，这条记录的 ACL 列上的值为：

```json
{"*":{"read":true},"55b9df0400b0f6d7efaa8801":{"write":true}}
```

此时，这种 ACL 值的表示：所有用户均有「读」权限，而 `objectId` 为 `55b9df0400b0f6d7efaa8801` 拥有「写」权限，其他用户不具备「写」权限。

### 多用户权限设置

假如需求增加为：帖子的作者允许某个特定的用户可以修改帖子，除此之外的其他人不可修改。
实现步骤就是额外指定一个用户，为他设置帖子的「写」权限：

<div class="callout callout-info">注意：**开启 `_User` 表的查询权限才可以执行以下代码**</div>



```js
  // 创建一个针对 User 的查询
  var query = new AV.Query(AV.User);
  query.get('55098d49e4b02ad5826831f6').then(function(otherUser) {
    var post = new AV.Object('Post');
    post.set('title', '大家好，我是新人');

    // 新建一个 ACL 实例
    var acl = new AV.ACL();
    acl.setPublicReadAccess(true);
    acl.setWriteAccess(AV.User.current(), true);
    acl.setWriteAccess(otherUser, true);

    // 将 ACL 实例赋予 Post 对象
    post.setACL(acl);

    // 保存到云端
    return post.save();
  }).then(function() {
    // 保存成功
  }).catch(function(error) {
    // 错误信息
    console.log(error);
  });
```


执行完毕上面的代码，回到控制台，可以看到，该条 Post 记录里面的 ACL 列的内容如下：

```json
{"*":{"read":true},"55b9df0400b0f6d7efaa8801":{"write":true},"55f1572460b2ce30e8b7afde":{"write":true}}
```
从结果可以看出，该条 Post 已经允许 Id 为 `55b9df0400b0f6d7efaa8801` 以及 `55f1572460b2ce30e8b7afde` 两个用户（AVUser）可以修改，他们拥有 `write：ture` 的权限，也就是「写」权限。

基于用户的权限管理比较简单直接，开发者理解起来成本较低。

### 局限性
再进一步的场景：
论坛升级，需要一个特定的管理员（Administrator）来统一管理论坛的帖子，他可以修改帖子的内容，删除不合适的帖子。

论坛升级之后，用户发布帖子的步骤需要针对上一小节做如下调整：

1. 写一篇帖子
2. 设置帖子的「读」权限为所有人。
3. 设置帖子的「写」权限为作者以及管理员
4. 保存帖子

我们可以设想一下，每当论坛产生一篇帖子，就得为管理员添加这篇帖子的「写」权限。

假如做权限管理功能的时候都依赖基于用户的权限管理，那么一旦产生变化就会发现这种实现方式的局限性。

比如新增了一个管理员，新的管理员需要针对目前论坛所有的帖子拥有管理员应有的权限，那么我们需要把数据库现有的所有帖子循环一遍，为新的管理员增加「写」权限。

假如论坛又一次升级了，付费会员享有特殊帖子的读权限，那么我们需要在发布新帖子的时候，设置「读」权限给部分人（付费会员）。这需要查询所有付费会员并一一设置。

毫无疑问，这种实现方式是完全失控的，基于用户的权限管理，在针对简单的私密分享类的应用是可行的，但是一旦产生需求变更，这种实现方式是不被推荐的。

## 基于角色的权限管理
管理员，会员，普通用户这三种概念在程序设计中，被定义为「角色」。
我们可以看出，在列出的需求场景中，「权限」的作用是用来区分某一数据是否**允许**某种角色的用户进行操作。
>「权限」只和「角色」对应，而用户也和「角色」对应，为用户赋予「角色」，然后管理「角色」的权限，完成了权限与用户的**解耦**。

因此我们来解释 LeanCloud 中「权限」和「角色」的概念。

「权限」在 LeanCloud 服务端只存在两种权限：读、写。
「角色」在 LeanCloud 服务端没有限制，唯一要求的就是在一个应用内，角色的名字唯一即可，至于某一个「角色」在当前应用内对某条数据是否拥有读写的「权限」应该是有开发者的业务逻辑决定，而 LeanCloud 提供了一系列的接口帮助开发者快速实现基于角色的权限管理。

为了方便开发者实现基于角色的权限管理，LeanCloud 在 SDK 中集成了一套完整的 ACL (Access Control List) 系统。通俗的解释就是为每一个数据创建一个访问的白名单列表，只有在名单上的用户（AVUser）或者具有某种角色（AVRole）的用户才能被允许访问。

为了更好地保证用户数据安全性， LeanCloud 表中每一张都有一个 ACL 列。当然，LeanCloud 还提供了进一步的读写权限控制。

一个 User 必须拥有读权限（或者属于一个拥有读权限的 Role）才可以获取一个对象的数据，同时，一个 User 需要写权限（或者属于一个拥有写权限的 Role）才可以更改或者删除一个对象。下面列举几种常见的 ACL 使用范例。

### ACL 权限管理
#### 默认权限

在没有显式指定的情况下，LeanCloud 中的每一个对象都会有一个默认的 ACL 值。这个值代表了所有的用户对这个对象都是可读可写的。此时你可以在数据管理的表中 ACL 属性中看到这样的值:

```
  {"*":{"read":true,"write":true}}
```
在 [基于用户的权限管理](#基于用户的权限管理) 的章节中，已经在代码里面演示了通过 ACL 来实现基于用户的权限管理，那么基于角色的权限管理也是依赖 ACL 来实现的，只是在介绍详细的操作之前需要介绍「角色」这个重要的概念。

### 角色的权限管理

#### 角色的创建
首先，我们来创建一个 `Administrator` 的角色。

这里有一个需要特别注意的地方，因为 `AVRole` 本身也是一个 `AVObject`，它自身也有 ACL 控制，并且它的权限控制应该更严谨，如同「论坛的管理员有权力任命版主，而版主无权任命管理员」一样的道理，所以创建角色的时候需要显式地设定该角色的 ACL，而角色是一种较为稳定的对象：



```js
  // 新建一个角色，并把为当前用户赋予该角色
  var roleAcl = new AV.ACL();
  roleAcl.setPublicReadAccess(true);
  roleAcl.setPublicWriteAccess(false);

  // 当前用户是该角色的创建者，因此具备对该角色的写权限
  roleAcl.setWriteAccess(AV.User.current(), true);

  //新建角色
  var administratorRole = new AV.Role('Administrator', roleAcl);
  administratorRole.save().then(function(role) {
    // 创建成功
  }).catch(function(error) {
    console.log(error);
  });
```


执行完毕之后，在控制台可以查看 `_Role` 表里已经存在了一个 `Administrator` 的角色。
另外需要开发者注意的是：可以直接通过 **控制台** > **Post 表** > **其他** > **权限设置** 直接设置权限。并且我们要强调的是：

> ACL 可以精确到 Class，也可以精确到具体的每一个对象（表中的每一条记录）。

#### 为对象设置角色的访问权限
我们现在已经创建了一个有效的角色，接下来为 `Post` 对象设置 `Administrator` 的访问「可读可写」的权限，设置成功以后，任何具备 `Administrator` 角色的用户都可以对 `Post` 对象进行「可读可写」的操作了：



```js
  // 新建一个帖子对象
  var Post = AV.Object.extend('Post');
  var post = new Post();
  post.set('title', '大家好，我是新人');

  // 新建一个角色，并把为当前用户赋予该角色
  var administratorRole = new AV.Role('Administrator');

  var relation = administratorRole.getUsers();

  //为当前用户赋予该角色
  administratorRole.getUsers().add(AV.User.current());

  //角色保存成功
  administratorRole.save().then(function(administratorRole) {

    // 新建一个 ACL 实例
    var acl = new AV.ACL();
    acl.setPublicReadAccess(true);
    acl.setRoleWriteAccess(administratorRole, true);

    // 将 ACL 实例赋予 Post 对象
    post.setACL(acl);
    return post.save();
  }).then(function(post) {
    // 保存成功
  }).catch(function(error) {
    // 保存失败
    console.log(error);
  });

```


#### 用户角色的赋予和剥夺
经过以上两步，我们还差一个给具体的用户设置角色的操作，这样才可以完整地实现基于角色的权限管理。

在通常情况下，角色和用户之间本是多对多的关系，比如需要把某一个用户提升为某一个版块的版主，亦或者某一个用户被剥夺了版主的权力，以此类推，在应用的版本迭代中，用户的角色都会存在增加或者减少的可能，因此，LeanCloud 也提供了为用户赋予或者剥夺角色的方式。
注意：在代码级别，**为角色添加用户** 与 **为用户赋予角色** 实现的代码是一样的。
此类操作的逻辑顺序是：

* 赋予角色：首先判断该用户是否已经被赋予该角色，如果已经存在则无需添加，如果不存在则为该用户(AVUser)的 `roles` 属性添加当前角色实例。

以下代码演示为当前用户添加 `Administrator`角色：



```js
  // 构建 AV.Role 的查询
  var roleQuery = new AV.Query(AV.Role);
  roleQuery.equalTo('name', 'Administrator');
  roleQuery.find().then(function(results) {
    if (results.length > 0) {

      // 如果角色存在
      var administratorRole = results[0];
      roleQuery.equalTo('users', AV.User.current());
      return roleQuery.find();
    } else {

      // 如果角色不存在新建角色，并把为当前用户赋予该角色
      var administratorRole = new AV.Role('Administrator');
      var relation = administratorRole.getUsers();

      //为当前用户赋予该角色
      relation.add(AV.User.current());
      administratorRole.save();
    }
  }).then(function(userForRole) {
    //该角色存在，但是当前用户未被赋予该角色
    if (userForRole.length === 0) {
      // 为当前用户赋予该角色
      var relation = administratorRole.getUsers();
      relation.add(AV.User.current());
      administratorRole.save();
    }
  }).catch(function(error) {
    // 输出错误
    console.log(error);
  });
```



角色赋予成功之后，基于角色的权限管理的功能才算完成。

另外，此处不得不提及的就是角色的剥夺：

* 剥夺角色： 首先判断该用户是否已经被赋予该角色，如果未曾赋予则不做修改，如果已被赋予，则从对应的用户（AVUser）的 `roles` 属性当中把该角色删除。 



```js
  // 构建 AV.Role 的查询
  var roleQuery = new AV.Query(AV.Role);
  roleQuery.equalTo('name', 'Moderator');
  roleQuery.find().then(function(results) {

    // 如果角色存在
    if (results.length > 0) {
      var moderatorRole = results[0];
      roleQuery.equalTo('users', AV.User.current());
      return roleQuery.find();
    }
  }).then(function(userForRole) {

    //该角色存在，并且也拥有该角色
    if (userForRole.length > 0) {

      // 剥夺角色
      var relation= moderatorRole.getUsers();
      relation.remove(AV.User.current());
      return moderatorRole.save();
    }
  }).then(function() {
    // 保存成功
  }).catch(function(error) {
    // 输出错误
    console.log(error);
  });
```



#### 角色的查询
除了在控制台可以直接查看已有角色之外，通过代码也可以直接查询当前应用中已存在的角色。
注：`AVRole` 也继承自 `AVObject`，因此熟悉了解 `AVQuery` 的开发者可以熟练的掌握关于角色查询的各种方法。



```js
  // 新建针对 Role 的查询
  var roleQuery = new AV.Query(AV.Role);

  // 查询 name 等于 Administrator 的角色
  roleQuery.equalTo('name', 'Administrator');

  // 执行查询
  roleQuery.first().then(function(adminRole) {
    var userRelation = adminRole.relation('users');
    return userRelation.query().find();
  }).then(function (userList) {

    // userList 就是拥有该角色权限的所有用户了。
    var firstAdmin = userList[0];
  }).catch(function(error) {
    console.log(error);
  });
```


查询某一个用户拥有哪些角色：



```js
  // 新建角色查询
  var roleQuery = new AV.Query(AV.Role);
  // 查询当前用户拥有的角色
  roleQuery.equalTo('users', AV.User.current());
  roleQuery.find().then(function(roles) {
    // roles 是一个 AV.Role 数组，这些 AV.Role 表示当前用户所拥有的角色
  }, function (error) {
  });
```


查询哪些用户都被赋予 `Moderator` 角色：



```js
  var roleQuery = new AV.Query(AV.Role);
  roleQuery.get('55f1572460b2ce30e8b7afde').then(function(role) {

    //获取 Relation 实例
    var userRelation= role.getUsers();

    // 获取查询实例
    var query = relation.query();
    return query.find();
  }).then(function(results) {
    // results 就是拥有 role 角色的所有用户了
  }).catch(function(error) {
    console.log(error);
  });
```


#### 角色的从属关系
角色从属关系是为了实现不同角色的权限共享以及权限隔离。

权限共享很好理解，比如管理员拥有论坛所有板块的管理权限，而版主只拥有单一板块的管理权限，如果开发一个版主使用的新功能，都要同样的为管理员设置该项功能权限，代码就会冗余，因此，我们通俗的理解是：管理员也是版主，只是他是所有板块的版主。因此，管理员在角色从属的关系上是属于版主的，只不过 TA 是特殊的版主。



```js
  // 建立版主和论坛管理员之间的从属关系
  var administratorRole = new AV.Role('Administrator');
  var administratorRole.save().then(function(administratorRole) {

    //新建版主角色
    var moderatorRole = new AV.Role('Moderator');

    // 将 Administrator 作为 moderatorRole 子角色
    moderatorRole.getRoles().add(administratorRole);
    return moderatorRole.save();
  }).then(function (role) {
    chai.assert.isNotNull(role.id);
    done();
  }).catch(function(error) {
    console.log(error);
  });
```




权限隔离也就是两个角色不存在从属关系，但是某些权限又是共享的，此时不妨设计一个中间角色，让前面两个角色从属于中间角色，这样在逻辑上可以很快梳理，其实本质上还是使用了角色的从属关系。

比如，版主 A 是摄影器材板块的版主，而版主 B 是手机平板板块的版主，现在新开放了一个电子数码版块，而需求规定 A 和 B 都同时具备管理电子数码板块的权限，但是 A 不具备管理手机平板版块的权限，反之亦然，那么就需要设置一个电子数码板块的版主角色（中间角色），同时让 A 和 B 拥有该角色即可。



```js
  //新建摄影器材版主角色
  var photographicRole = new AV.Role('Photographic');

  //新建手机平板版主角色
  var mobileRole=new AV.Role('Mobile');

  //新建电子数码版主角色
  var digitalRole=new AV.Role('Digital');

   AV.Promise.when(
    // 先行保存 photographicRole 和 mobileRole
    photographicRole.save(),
    mobileRole.save()
   ).then(function(r1, r2) {
    // 将 photographicRole 和 mobileRole 设为 digitalRole 一个子角色
    digitalRole.getRoles().add(photographicRole);
    digitalRole.getRoles().add(mobileRole);
    digitalRole.save();

    // 新建一个帖子对象
    var Post = AV.Object.extend('Post');

    // 新建摄影器材板块的帖子
    var photographicPost = new Post();
    photographicPost.set('title', '我是摄影器材板块的帖子！');

    // 新建手机平板板块的帖子
    var mobilePost = new Post();
    mobilePost.set('title', '我是手机平板板块的帖子！');

    // 新建电子数码板块的帖子
    var digitalPost = new Post();
    digitalPost.set('title', '我是电子数码板块的帖子！');


    // 新建一个摄影器材版主可写的 ACL 实例
    var photographicACL = new AV.ACL();
    photographicACL.setPublicReadAccess(true);
    photographicACL.setRoleWriteAccess(photographicRole,true);

    // 新建一个手机平板版主可写的 ACL 实例
    var mobileACL = new AV.ACL();
    mobileACL.setPublicReadAccess(true);
    mobileACL.setRoleWriteAccess(mobileRole,true);

    // 新建一个手机平板版主可写的 ACL 实例
    var digitalACL = new AV.ACL();
    digitalACL.setPublicReadAccess(true);
    digitalACL.setRoleWriteAccess(digitalRole,true);

    // photographicPost 只有 photographicRole 可以读写
    // mobilePost 只有 mobileRole 可以读写
    // 而 photographicRole，mobileRole，digitalRole 均可以对 digitalPost 进行读写
    photographicPost.setACL(photographicACL);
    mobilePost.setACL(mobileACL);
    digitalPost.setACL(digitalACL);

    AV.Promise.when(
      photographicPost.save(),
      mobilePost.save(),
      digitalPost.save()
    ).then(function(r1, r2, r3) {
      // 保存成功
      }, function(errors) {
      // 保存失败
    });
   });
```



### 权限的分级
在日常生活中，我们也遇到过权限分级的问题，例如新的系统又一次升级，引入了超级管理员的概念，这个超级管理员可以对系统里面任何的对象进行「读写操作」，按照前文的做法，需要用代码实现如下步骤：

1. 创建超级管理员角色
2. 遍历云端现有的帖子，为所有对象添加超级管理员的「ACL 读写权限」
3. 保存数据

开发者一旦面对这种逻辑重复的代码，一般都会想到：有没有一个快捷的操作可以一次性为某一个角色添加整个 Class 添加权限？

当然，LeanCloud 也已经提供了便捷的可视化的操作。打开 **控制台** > **存储** > **Post** > **其他** > **权限设置**，如下图所示：

![acl_in_console](http://ac-lhzo7z96.clouddn.com/1442281757400)

* 在这里你可以设置某一个角色对 `Post` 的操作权限：选择 **指定用户**，在指定角色中输入 `superAdmin`，并且点击添加即可。

如此设置，无需书写代码，在项目迭代过程中有角色加入都可以用这种便捷的方式进行操作。

权限设置的参数以及操作解释如下：

参数名 | 含义
--- | ---
add_fields|添加新字段到 class
create | 保存一个从未创建过的新对象
delete |  删除一个对象
find | 发起一次对象列表查询
get | 通过 objectId 获取对象
update | 保存一个已经存在并且被修改的对象

权限对象名 | 含义
--- | ---
所有用户 | 任意用户均有权限
登录用户 | 用户登录之后，具有权限
指定用户| 可以指定具体的用户（`AVUser`）也可以指定具体的角色（`AVRole`）


## 超级权限
ACL 可以满足常见的需求，但是 `_User` 表比较特殊，它会忽略 ACL 的设置，表现为：任何用户都无法修改其他用户的属性，比如当前登录的用户是 A，而他想通过请求去修改 B 用户的用户名，密码或者其他自定义属性，是不会生效的。

但是有时一些应用的需求较为特殊，比如，论坛的管理员可以修改某些用户的昵称，性别（假设昵称和性别是存储在 `_User` 的 `gender`,`age` 的字段上），此时通过设置管理员拥有该用户的 ACL 写的权限是无法实现预想效果的。因此，我们提供了一种方式去提高操作的权限，在 SDK 中的实现方式是在初始化的时候，增加一个 Master Key 的字段作为提高权限的接口。Master Key 可以在 `控制台` -> `设置` -> `应用 Key` 中获取。 



在 Node.js 运行时中可以使用如下代码初始化 SDK：

```js
  AV.init({
    appId: APP_ID,
    appKey: APP_KEY,
    masterKey: MASTER_KEY,
  })
  AV.Cloud.useMasterKey();
```




## 最佳实践

本章节的目的是介绍如何在 [云引擎](acl_guide_leanengine.html) 里面使用 ACL 。

我们先来探索一个需求：某个应用它拥有众多客户端，iOS、Andorid、Windows 等，还有 Web 版，未来可能还会有手环，手表客户端，那么关于 ACL 的代码会遍布在所有客户端，那么开发者就会需要不断的升级和维护逻辑十分类似的客户端代码，到了这一步，我们更推荐，开发者在服务端定义一段 ACL 的逻辑，统一处理 ACL 权限分配的问题。

【总结】假如应用的平台比较固定，那么就可以考虑采取本文前面所介绍的客户端代码实现 ACL 代码。如果应用的平台比较多，多平台多客户端就推荐使用 [云引擎 Hook 函数使用 ACL](acl_guide_leanengine.html)。



