# 错误码详解

本文档尝试为所有服务端和SDK返回的错误码给出相对详细的解释，具体到各个SDK的错误码，请参考下列文档链接：

* iOS 的 [AVConstants](/api-docs/iOS/docs/AVConstants.html)。
* iOS v3.1.5 及之后的版本，网络请求操作相关的错误码，比如：28 表示请求超时、7 表示连接服务器失败，这些请参考 [**libcurl error codes**](http://curl.haxx.se/libcurl/c/libcurl-errors.html)。
* Android 的 [AVException](/api-docs/android/index.html)。

## 实时通信错误码

相关参考链接：

* [实时通信服务端错误码说明](realtime_v2.html#服务器端错误码说明)。
* iOS SDK 在进行 WebSocket 通信过程中，相关的状态码请参考  [**RFC 6455 -- Status Codes**]( http://tools.ietf.org/html/rfc6455#section-7.4 )。

## 1
* 信息 - `Internal server error. No information available.`
* 含义 - 服务器内部错误或者参数错误，一般是因为传入了错误的参数，或者没有在本文档里明确定义的运行时错误，都会以代码 1 指代。

## 100

* 信息 - `The connection to the AVOS servers failed.`
* 含义 - 无法建立 TCP 连接到 LeanCloud 服务器，通常是因为网络故障，或者我们服务器故障引起的，我们的服务器状态可以查看 [健康状态检查](http://status.leancloud.cn/)。

## 101

* 信息 - `Object doesn't exist, or has an incorrect password.`
* 含义 - 查询的 Class 不存在，或者要关联的 Pointer 对象不存在。

## 103

* 信息 - `Missing or invalid classname. Classnames are case-sensitive. They must start with a letter, and a-zA-Z0-9_ are the only valid characters.`
* 含义 - 非法的 Class 名称，Class 名称大小写敏感，并且必须以英文字母开头，有效的字符仅限在英文字母、数字以及下划线。

## 104

* 信息 - `Missing object id.`
* 含义 - 缺少 objectId，通常是在查询的时候没有传入 objectId，或者 objectId 非法。objectId 只能为字母、数字组成的字符串。

## 105

* 信息 - `Invalid key name. Keys are case-sensitive. They must start with a letter, and a-zA-Z0-9_ are the only valid characters.`
* 含义 - 无效的 key 名称，也就是 Class 的列名无效，列名必须以英文字母开头，有效的字符仅限在英文字母、数字以及下划线。

## 106

* 信息 - `Malformed pointer. Pointers must be arrays of a classname and an object id.`
* 含义 - 无效的 Pointer 格式，Pointer必须为形如 `{className: 'Post', objectId:'xxxxxx'}` 的 JSON 对象。

## 107

* 信息 - `Malformed json object. A json dictionary is expected.`
* 含义 - 无效的 JSON 对象，解析 JSON 数据失败。

## 108

* 信息 - `Tried to access a feature only available internally.`
* 含义 - 此 API 仅供内部使用。

## 109

* 信息 - `That operation isn't allowed by binding class.`
* 含义 - 共享的 Class 无权限执行此操作，请检查 Class 共享的权限设置。


## 111

* 信息 - `Field set to incorrect type.`
* 含义 - 想要存储的值不匹配列的类型，请检查你的数据管理平台中列定义的类型，查看存储的数据是否匹配这些类型。

## 112

* 信息 - `Invalid channel name. A channel name is either an empty string (the broadcast channel) or contains only a-zA-Z0-9_ characters and starts with a letter.`
* 含义 - 推送订阅的频道无效，频道名称必须不是空字符串，只能包含英文字母、数字以及下划线，并且只能以英文字母开头。

## 113

* 信息 - `Key is required.`
* 含义 - Class 中的某个字段设定成必须，保存的对象缺少该字段。

## 114

* 信息 - `Invalid device token.`
* 含义 - iOS 推送存储的 deviceToken 无效，如何存储 installation 请阅读 [消息推送开发指南](./push_guide.html#iOS_消息推送)。

## 116

* 信息 - `The object is too large.`
* 含义 - 要存储的对象超过了大小限制，我们限制单个对象的最大大小在 16 M。

## 117

* 信息 - `The key is read only.`
* 含义 - 更新的 Key 是只读属性，无法更新。


## 119

* 信息 - `That operation isn't allowed for clients.`
* 含义 - 该操作无法从客户端发起。请检查该错误由哪个操作引起，然后进入 [应用控制台 > 设置 > 应用选项](/app.html?appid={{appid}}#/permission) 启用相应的选项。

## 120

* 信息 - `The results were not found in the cache.`
* 含义 - 查询结果无法从缓存中找到，SDK 在使用从查询缓存的时候，如果发生缓存没有命中，返回此错误。

## 121

* 信息 - `Keys in NSDictionary values may not include '$' or '.'.`
* 含义 - JSON 对象中 key 的名称不能包含 `$` 和 `.` 符号。

## 122

* 信息 - `Invalid file name. A file name contains only a-zA-Z0-9_. characters and is between 1 and 36 characters.`
* 含义 - 无效的文件名称，文件名称只能是英文字母、数字和下划线组成，并且名字长度限制在 1 到 36 之间。

## 123

* 信息 - `Invalid ACL. An ACL with an invalid format was saved. This should not happen if you use AVACL.`
* 含义 - ACL 格式错误，如果您是使用 SDK 提供的 AVACL 类，理论上这不应该发生，正确的 ACL 格式请参考 [REST API](./rest_api.html#安全性)。

## 124

* 信息 - `The request timed out on the server. Typically this indicates the request is too expensive.`
* 含义 - 请求超时，超过一定时间（默认 10 秒）没有返回，通常是因为网络故障或者该操作太耗时引起的。

## 125

* 信息 - `The email address was invalid.`
* 含义 - 电子邮箱地址无效。

## 126

* 信息 - `Invalid user id.`
* 含义 - 无效的用户 Id，可能用户不存在。

## 127

* 信息 - `The mobile phone number was invalid.`
* 含义 - 手机号码无效。

## 128

* 信息 - `Invalid relation data.`
* 含义 - 无效的 Relation 数据，通常是因为添加或者删除的 Relation 数据为空或者过多（单次超过 1000 个）。


## 137

* 信息 - `A unique field was given a value that is already taken.`
* 含义 - 违反 class 中的唯一性索引约束（unique），尝试存储重复的值。

## 139

* 信息 - `Role's name is invalid.`
* 含义 - 角色名称非法，角色名称只能以英文字母、数字或下划线组成。

## 140

* 信息 - `Exceeded an application quota. Upgrade to resolve.`
* 含义 - 超过应用的容量限额，请升级账户等级。

## 141

* 信息 - `Cloud Code script had an error.`
* 含义 - 云引擎脚本编译或者运行报错。

## 142

* 信息 - `Cloud Code validation failed.`
* 含义 - 云引擎校验错误，通常是因为 beforeSave、beforeDelete 等函数返回 error。

## 145

* 信息 - `Payment is disabled on this device.`
* 含义 - 本设备没有启用支付功能。

## 150

* 信息 - `Fail to convert data to image.`
* 含义 - 转换数据到图片失败。

## 160

* 信息 - `Insufficient balance.`
* 含义 - 账户余额不足。

## 200

* 信息 - ` Username is missing or empty`
* 含义 - 没有提供用户名，或者用户名为空。

## 201

* 信息 - `Password is missing or empty.`
* 含义 - 没有提供密码，或者密码为空。

## 202

* 信息 - `Username has already been taken.`
* 含义 - 用户名已经被占用。

## 203

* 信息 - `Email has already been taken.`
* 含义 - 电子邮箱地址已经被占用。

## 204

* 信息 - `The email is missing, and must be specified.`
* 含义 - 没有提供电子邮箱地址。

## 205

* 信息 - `A user with the specified email was not found.`
* 含义 - 找不到电子邮箱地址对应的用户。

## 206

* 信息 - `The user cannot be altered by a client without the session.`
* 含义 - 没有提供 session，无法修改用户信息，这通常是因为没有登录的用户想修改信息。修改用户信息必须登录，除非在云引擎里使用 master key 来更改。

## 207

* 信息 - `Users can only be created through sign up.`
* 含义 - 只能通过注册创建用户，不允许第三方登录。

## 208

* 信息 - `An existing account already linked to another user.`
* 含义 - 第三方帐号已经绑定到一个用户，不可绑定到其他用户。

## 210

* 信息 - `The username and password mismatch.`
* 含义 - 用户名和密码不匹配。

## 211

* 信息 - `Could not find user.`
* 含义 - 找不到用户。

## 212

* 信息 - `The mobile phone number is missing, and must be specified.`
* 含义 - 请提供手机号码。

## 213

* 信息 - `A user with the specified mobile phone number was not found.`
* 含义 - 手机号码对应的用户不存在。

## 214

* 信息 - `Mobile phone number has already been taken.`
* 含义 - 手机号码已经被注册。

## 215

* 信息 - `Mobile phone number isn't verified.`
* 含义 - 未验证的手机号码。

## 216

* 信息 - `Email address isn't verified.`
* 含义 - 未验证的邮箱地址。

## 217

* 信息 - `Invalid username, it must be a non-blank string.`
* 含义 - 无效的用户名，不允许空白用户名。

## 218

* 信息 - `Invalid password, it must be a non-blank string.`
* 含义 - 无效的密码，不允许空白密码。

## 250

* 信息 - `Linked id missing from request`
* 含义 - 连接的第三方账户没有返回用户唯一标示 id

## 251

* 信息 - `Invalid linked session`或者`Invalid Weibo session`
* 含义 - 无效的账户连接，一般是因为 access token 非法引起的。

## 252

* 信息 - `Invalid Weixin session`
* 含义 - 无效的微信授权信息。

## 300

* 信息 - `CQL syntax error.`
* 含义 - CQL 语法错误。详情参考 [CQL 语法详细指南](./cql_guide.html)

## 301 

* 信息 - `Fails to insert new document.`
* 含义 - 新增对象失败，通常是数据格式问题。

## 302

* 信息 - `Invalid GeoPoint values.`
* 含义 - 无效的 GeoPoint 类型，请确保经度在 -180 到 180 之间，纬度在 -90 到 90 之间。

## 303

* 信息 - `Fail to execute operation on storage.`
* 含义 - 插入数据库失败，一般是数据格式或者内部错误，通常错误里包含更具体的错误信息。

## 304

* 信息 - `Database error.`
* 含义 - 数据操作错误，一般是语法错误或者内部异常，请及时联系我们。

## 305

* 信息 - `No effect on updating/deleting a document.`
* 含义 - 根据 where 条件更新或者删除对象不起作用，通常是因为条件不满足。

## 401

* 信息 - `Unauthorized.`
* 含义 - 未经授权的访问，没有提供 App id，或者 App id 和 App key 校验失败，请检查配置。

## 403

* 信息 - `Forbidden to xxx by class permissions`
* 含义 - 操作被禁止，因为 [Class 权限限制](./data_security.html#Class_级别的_ACL)。


## 429

* 信息 - `Too many requests.`
* 含义 - 超过应用的流控限制，每个应用同一时刻的并发请求上限为 30（即同一时刻最多可以同时处理 30 个数据请求）；通过数据管理平台每秒限制上传一个文件，并且每分钟最多上传 30 个文件，如需提升，请联系我们。可以在 API 统计的性能统计总览中看到你的应用的请求统计，例如平均并发、平均响应时间等。

## 502

* 信息 - `Server is in maintenance.`
* 含义 - 服务器维护中。

## 503

* 信息 - `The app is temporarily disabled/readonly.`
* 含义 - 应用被临时禁用或者进入只读状态，通常是进行运维或者故障处理操作，我们会提前告知开发者。

## 511

* 信息 - `Temporarily Unavailable.`
* 含义 -  该请求 API 暂时不可用，请稍后重试。一般是运维操作临时禁止了某个 API 访问，一段时间后会自然恢复，或者联系我们处理。


## 600

* 信息 - `Invalid SMS signature.`
* 含义 - 无效的短信签名。短信签名是指附加在短信文本前后位置，使用中文括号【】括起来的文字，短信签名只能位于短信开头或者结束的位置，并且限制在 10（包含 10 个字符）个字符内。
        默认发送的短信签名使用的是应用名称，应用名称可以在应用设置里修改。短信自定义模板可以在模板里自定义签名。
        
## 601

* 信息 - `Can't send SMS too frequently.`
* 含义 -  发送短信过于频繁。我们限制验证类短信一分钟一条，每天每个号码限制在 10 条左右。我们强烈建议用户使用图形验证码或者倒数计时等方式来避免用户重复发送验证码，以及可能存在的短信验证码攻击。

## 602

* 信息 - `Fails to send message.`
* 含义 -  发送短信或者语音验证码失败，这是短信提供商返回错误，如果确认手机号码没有问题，请联系我们处理。

## 603

* 信息 - `Invalid SMS code.`
* 含义 - 无效的短信验证码，通常是不匹配或者过期。

## 604

* 信息 - `SMS template not found.`
* 含义 - 找不到自定义的短信模板，请检查模板名称是否正确或者模板是否已经创建并审核通过。

## 605

* 信息 - `SMS template not verified.`
* 含义 - 短信模板未审核。

## 606

* 信息 - `Fails to render SMS template.`
* 含义 - 渲染短信模板失败，通常是模板语法问题，我们的短信模板仅支持 [handlerbars](http://handlebarsjs.com/) 模板语法。

## 700

* 信息 - `Nonexistent query keys`
* 含义 - 无效的查询或者排序字段，请确认查询或者排序的字段在表中存在。




