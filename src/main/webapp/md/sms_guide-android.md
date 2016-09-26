


















# Android - 短信服务使用指南

LeanCloud 短信服务支持的应用场景有以下三种：

* **用户验证**：例如微信、陌陌等在登录时需要向用户发送一条包含了验证码的短信，再引导用户输入进行安全认证，以及修改密码等数据安全相关的操作。
* **操作验证**：例如银行金融类应用，用户在对账户资金进行敏感操作（例如转账、消费等）时，需要通过验证码来验证是否为用户本人操作。
* **通知公告**：例如淘宝某卖家在发货之后会用短信的方式将快递单、订单号、发货时间等发给买家，以达到良好的购买体验。

开发者在设置短信内容的时候，文字表述上应该做到规范、正确、简洁。我国相关法律**严令禁止**发送内容涉及以下情况的短信：

* **政治敏感**
* **极端言论**
* **淫秽色情**
* **传销诈骗**
* **封建迷信**
* **造谣诽谤**
* **我国现行法律、行政法规及政策所禁止的内容**

因此，LeanCloud 需要审核短信内容，并且保留对发送人追究相关法律责任的权力。

## 短信服务配置


从应用级别来控制能否发送短信，进入 [控制台 > 设置 > **安全中心**](/app.html?appid={{appid}}#/security)，设置 **短信发送** 开关。

然后点击 [**应用选项**](/app.html?appid={{appid}}#/permission)，查看与短信相关选项。当开启 **安全中心** > **短信发送** 服务之后，以下打勾的选项会被默认开启：


- 【&check;】用户账号 > **用户注册时，向注册手机号码发送验证短信**<br/>
  开启：调用 AVUser 注册相关的接口时，如果传入了手机号，系统则会自动发送验证短信，然后开发者需要手动调用一下验证接口，这样 `_User` 表中的 `mobilePhoneVerified` 值才会被置为 `true`。<br/>
  关闭：调用 AVUser 注册相关的接口不会发送短信。

- 用户账号 > **未验证手机号码的用户，禁止登录**<br/>
  开启：未验证手机号的 `AVUser` 不能使用「手机号 + 密码」以及「手机号 + 短信验证码」的方式登录，但是<u>用户名搭配密码的登录方式不会失败</u>。<br/>
  关闭：不会对登录接口造成任何影响。

- 用户账号 > **未验证手机号码的用户，允许以短信重置密码**<br/> 
  开启：允许尚未验证过手机号的 `AVUser` 通过短信验证码实现重置密码的功能。<br/> 
  关闭：`AVUser` 必须在使用短信重置密码之前，先行验证手机号，也就是 `mobilePhoneVerified` 字段必须是 `true` 的前提下，才能使用短信重置密码。

- 用户账号 > **已验证手机号码的用户，允许以短信验证码登录**<br/>
  开启：`AVUser` 可以使用手机号搭配短信验证码的方式登录。<br/>  
  关闭：`AVUser` 不能直接使用。

- 【&check;】其他 > **启用通用的短信验证码服务（开放 requestSmsCode 和 verifySmsCode 接口）**<br/> 
  开启：开发者可以使用短信进行验证功能的开发，比如，敏感的操作认证、异地登录、付款验证等业务相关的需求。<br/>
  关闭：请求验证发送短信以及验证短信验证码都会被服务端拒绝，但是请注意，跟用户相关的验证与该选项无关。

了解以上配置项有助于开发者针对业务需求，在功能选择上做出调整。下面，我们根据不同的需求逐一介绍短信服务的功能。


### 短信服务覆盖的国家和地区
目前 LeanCloud 的短信服务覆盖以下国家和地区：

大区| 国家和地区 |
--- | --- |---
中国|中国大陆、台湾省、香港行政区、澳门行政区
北美|美国、加拿大
南美|巴西
欧洲|英国、法国、德国、意大利、乌克兰、俄罗斯、西班牙
东亚|韩国、日本
东南亚|印度、马来西亚、新加坡、泰国、越南、印度尼西亚
大洋洲|澳大利亚、新西兰

### 短信计费

如果一个短信模板字数超过 70 个字（包括签名的字数），那么该短信在发送时会被运营商<u>按多条来收取费用</u>，但接收者收到的仍是<u>一条完整的短信</u>。

- 小于或等于 70 个字，按一条计费。
- 大于 70 个字，按每 67 字一条计费。

状态为「等待回执」的短信不会收费计入账单。每条短信的收费标准请参考 [官网价格方案](/pricing.html)。

### 短信购买


短信发送采取实时扣费。如果当前账户没有足够的<u>短信余额</u>（注意不是账户余额），短信将无法发送。充值请进入 [开发者账户 > 财务 > 财务概况](/bill.html#/bill/general)，点击 **短信充值**。

同时我们建议设定好短信的 [余额告警](/settings.html#/setting/alert)，以便在第一时间收到短信或邮件获知短信余额不足。


## 验证类

用户验证是一个移动应用最基本的功能，传统的「账号和密码登录」在一般的应用场景下尚且可以满足需求，但是相对于社交类、照片存储类、金融类等应用，随着不断使用，用户往往会在应用中留下较多的个人信息记录，比如：

当微信发现用户忽然在一台从未授权过的新设备上登录时，如果该用户已经开启了「安全验证」，那么它会向该用户的手机号发一条验证短信，以验证当前登录是否为本人操作。微信是一个较为私密的社交应用，随着使用时间的增加，用户的手机号、聊天记录都可能涉及到更为私密的信息。

开发者在开发过程中也可能会遇到与微信类似的场景和需求。

### 操作认证

象在新设备上登录微信、在新设备上进行支付宝的第一次支付认证，这种与应用内业务逻辑紧密相关的验证操作一般都跟用户的敏感信息相关。出于对用户信息以及金融安全的考虑，短信验证往往会比较可信，LeanCloud 为此提供了一系列的接口。

我们以一个购物应用为例，说明一下操作认证的大致步骤：

1. **用户点击支付订单**  
  发起敏感操作
  
2. **调用接口发送验证短信**  
  注意，在这一步之前，我们假设当前用户已经设置过了手机号，所以推荐这类应用在注册环节，尽量要求用户以手机号作为用户名，否则到了支付界面，还需要用户在首次购买时输入一次手机号。
  
  ```java
        AVOSCloud.requestSMSCodeInBackground(AVUser.getCurrentUser().getMobilePhoneNumber(), "某应用", "具体操作名称", 10, new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    mSMSCode.requestFocus();
                } else {
                    Log.e("Home.OperationVerify", e.getMessage());
                }
            }
        });
  ```

3. **用户收到短信，并且输入了验证码。**  
  在进行下一步之前，我们依然建议先进行客户端验证，这样就避免了错误的验证码被服务端驳回而产生的流量，以及与服务端沟通的时间，有助于提升用户体验。
  
4. **调用接口验证用户输入的验证码是否有效。**  
  
  ```java
        AVOSCloud.verifyCodeInBackground("777777", "13888888888", new AVMobilePhoneVerifyCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Toast.makeText(getBaseContext(), getString(R.string.msg_operation_valid), Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("Home.DoOperationVerify", e.getMessage());
                }
            }
        });
  ```

  
针对上述的需求，可以把场景换成异地登录验证、修改个人敏感信息验证等一些常见的场景，步骤是类似的，调用的接口也是一样的，仅仅是在做 UI 展现的时候需要开发者自己去优化验证过程。

### 注册验证

**用户注册**对所有开发者来说都是非常重要的，为了降低操作难度，越来越多的应用支持使用手机号码来快速注册账户，并且同时会通过短信验证码来核实用户手机号码的有效性。为了满足这一需求，我们将 LeanCloud 内建的[账户系统](
leanstorage_guide-android.html#用户
)与短信验证码相结合，推出了便捷的「注册验证」功能。其使用步骤如下：

1. **用户输入手机号以及密码**  
  引导用户正确的输入，建议在调用 SDK 接口之前，验证一下手机号的格式。

2. **调用 AVUser 的注册接口，传入手机号以及密码。**  
  
  ```java
        AVUser user = new AVUser();
        user.setUsername("hjiang");
        user.setPassword("f32@ds*@&dsa");
        user.setEmail("hang@leancloud.rocks");
        
        // 其他属性可以像其他AVObject对象一样使用put方法添加
        user.put("mobilePhoneNumber", "186-1234-0000");

        user.signUpInBackground(new SignUpCallback() {
            public void done(AVException e) {
                if (e == null) {
                    // successfully
                } else {
                    // failed
                }
            }
        });
  ```

3. **云端发送手机验证码，并且返回注册成功，但是此时用户的 `mobilePhoneVerified` 依然是 `false`，客户端需要引导用户去输入验证码。**   
  
4. **用户再一次输入验证码**  
  最好验证一下是否为纯数字。
  
5. **调用验证接口，检查用户输入的纯数字验证码是否合法。**  
  
  ```java
        AVUser.verifyMobilePhoneInBackground("123456", new AVMobilePhoneVerifyCallback() {
            @Override
            public void done(AVException e) {
                if(e == null){
                    // 验证成功
                } else {
                    Log.d("SMS", "Verified failed!");
                }
            }
        });
  ```


以上是一个通用的带有手机号验证的注册过程。开发者可以根据需求增加或减少步骤，但是推荐开发者在使用该功能时，首先明确是否需要勾选「验证注册用户手机号码」。因为一旦勾选，只要调用了 AVUser 相关的注册账号，并传入手机号，云端就会自动发送短信验证码。

<div class="callout callout-info">注意：只有使用了 LeanCloud 内建账户系统的应用才能使用这一功能。</div>

另外，假如注册的时候并没有强制用户验证手机号，而是在用户使用某一个功能的时候，要求用户验证手机号，也可以调用接口进行「延迟验证」，验证之后 `mobilePhoneVerified` 就会被置为 `true`。

1. **请求发送验证码**    
  
  ```java
        AVUser.requestMobilePhoneVerifyInBackground("13800000000", new RequestMobileCodeCallback() {
            @Override
            public void done(AVException e) {
                if(e == null){
                    // 发送成功
                } else {
                    Log.d("SMS", "Send failed!");
                }
            }
        });
  ```

2. **调用验证接口，验证用户输入的纯数字的验证码。**  
  
  ```java
        AVUser.verifyMobilePhoneInBackground("654321", new AVMobilePhoneVerifyCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    // 验证成功
                } else {
                    Log.d("SMS", "Verified failed!");
                }
            }
        });
  ```


#### 未收到注册验证短信

一般来说，用户收到的注册验证短信内容为：

<samp ng-non-bindable class="bubble">【AppName】欢迎使用 AppName 服务，您的验证码是 123456，请输入完成验证。</samp>

其中 **AppName** 是应用的名称，必须遵循 [短信签名规范](#签名规范) 中的长度及其他要求，否则会被短信供应商拒绝发送。

## 通知类

通知类短信是很多人每天都可能收到的信息，例如，联通用户从外地进入上海可能会收到如下短信：

<samp ng-non-bindable class="bubble">尊敬的用户：欢迎您来到上海，如需帮助请拨打客服热线10010或登录www.10010.com，优惠订购机票酒店请拨打116114。中国联通</samp>

这是一个来自通信运营商的通知类短信的规范案例。

在实际使用的过程中，通知类短信往往会因为**措辞不当**而被短信服务商拒绝发送；甚至还会出现 开发者发送过去的请求被服务商直接屏蔽的情况。因此，LeanCloud 在发送推广类的短信时**需要进行内容审核**。

## 营销类

营销类短信是应用开发者与潜在客户沟通、进行产品推广和销售的有效手段。营销类短信也是基于模板来实现的，因此使用时跟 [通知类](#通知类) 使用没有任何本质区别，步骤依次是 [创建模板](#创建模板) > [使用模板](#使用模板)。

营销类短信默认会在短信最后加上「**回复 TD 退订**」，这是运营商的强制要求。

<div class="callout callout-danger">自 2016 年 6 月起，营销类短信不允许由个人用户发送。所有营销类短信必须提供有效的公司或者企业名称，缺失名称将导致营销类短信无法发送。伪造或使用虚假名称将会被追究法律责任。</div>

## 自定义短信内容

通过短信模板，我们可以自定义短信的内容。因为短信模板是由开发者自己定义的，因此为了确保平台的安全以及稳定，我们的短信模板由**专人进行审核**，只有**通过审核**之后，在接口里的调用才会成功发送。

### 创建模板

创建模板的截图如下：

![sms_template](images/sms_template_v2.png)

创建模板需要选择指定一个已经通过审核的 [短信签名](#短信签名)，并且选择模板的类型，目前支持以下三种类型：

- 通知类型
- 验证码类型
- 营销类型

创建短信模板的步骤如下：进入控制台，选择一个应用，再选择 [**消息** > **短信** > **设置**](http://leancloud.cn/messaging.html?appid={{appid}}#/message/sms/conf)。**模板类型如果选择了通知类或者验证类，但短信内容如果涉及营销内容则无法通过审核。**要发送营销类短信请阅读 [营销类](#营销类)。

### 使用模板

假设提交的短信模板名称为 `Notice_Welcome`，当模板通过审批后就可以调用了：


```java
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("service_name", "月度周刊");
        parameters.put("order_id", "7623432424540");
        AVOSCloud.requestSMSCodeInBackground(AVUser.getCurrentUser().getMobilePhoneNumber(),
                "Notice_Welcome",
                parameters,
                new RequestMobileCodeCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            Toast.makeText(getBaseContext(), getString(R.string.msg_notice_sent), Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("Home.SendNotice", e.getMessage());
                        }
                    }
                });
```


### 模板变量

模板可以使用<u>自定义变量</u>，如上例中的 <code ng-non-bindable>{{service_name}}</code> 和 <code ng-non-bindable>{{order_id}}</code>，在调用模板时以参数形式传入。模板语法遵循 [Handlebars](http://handlebarsjs.com) 规范。

<div class="callout callout-info">自定义变量的值不允许包含实心括号 `【】` 。</div>

模板还可以使用<u>系统预留变量</u>，在发送短信发送时，它们会被自动填充：

<samp ng-non-bindable class="bubble">欢迎注册{{name}}应用！请使用验证码 {{code}} 来完成注册。该验证码将在{{ttl}}分钟后失效，请尽快使用。
</samp>

- `name`：应用名称
- `code`：验证码
- `ttl`：过期时间（默认为 10 分钟）

## 模板规范

鉴于开发者的应用场景各异，我们整理了以下范例来说明如何撰写规范的短信模板。需要再次强调，一切跟营销推广相关的短信模板，请在创建模板时务必选择**营销类**，否则将无法通过审核。

【正确范例】

<samp ng-non-bindable class="bubble">XX房东您好，租客{{guest_name}}（手机号码：{{guest_phone}}）于{{payment_create_time}}向您支付了房租。租金为{{rent_price}}元，房屋地址在{{rent_addr}}。请关注微信公众号： XX租房。XX租房APP下载地址：http://example.com/download.html</samp>

### 短信签名

短信签名是指短信内容里用【】括起来的短信发送方名称。如果没有明确在模板里指定，默认就是你的应用名称。应用名称可以在 [控制台 > 设置 > 基本信息](/app.html?appid={{appid}}#/general) 里修改，并且短信签名**必须出现在短信内容的开头或结尾。**

#### 签名规范

 - 签名必须是公司简称、品牌名、网站名，长度控制在 3 到 8 个字符之间。例如签名【应用A】中的 `应用A` 为 3 个字符。
 - 不能有任何非文字字符，也不可以是变量。
 - 不允许全部为英文字符或全部为数字
 - 不需要带上实心方头括号【】
 - 一个应用最多可以拥有 3 个签名（包含审核未通过的签名）。

### 链接

短信中的 URL 不允许**全部**设置为变量，这样是为了确保安全，防止病毒以及不良信息的传播。错误范例如下：

<samp ng-non-bindable class="bubble">尊敬的会员您好，您的订单（订单号）已确认支付。5周年庆新品降价！大牌奢品上演底价争霸，低至2折！BV低至888元！阿玛尼低至199元！都彭长款钱包仅售499元！杜嘉班纳休闲鞋仅售1399元！周年庆家居专场千元封顶现已开启！{{download_link}} 客服电话400-881-6609 回复TD退订</samp>

<div class="callout callout-danger">以上通知内容包含了象 <u>打折</u>、<u>降价</u>、<u>仅售</u> 这类营销推广的敏感词语，容易导致审批无法通过，因此请谨慎使用或改用 [营销类短信](#营销类)。</div>

但是 URL 中可以包含变量，比如：

<samp ng-non-bindable class="bubble">亲，您的宝贝已上路，快递信息可以通过以下链接查询：http://www.sf-express.com/cn/#search/{{bill_number}} </samp>

### 通知类模板

【正确范例】

<samp ng-non-bindable class="bubble">恭喜您获得关注广州体彩微信送“排列三”的活动彩票，您的号码是第{{phase}}期的：{{num}}。开奖时间为{{date}}，请关注公众号—开奖信息查询中奖状态。</samp>

〖错误范例〗

<samp ng-non-bindable class="bubble">您好，本条短信来自“XX旅游”~恭喜幸运的您，在本次“北海道机票”抽奖活动中，获得二等奖——定制星巴克杯! 请您关注我们的微信公众号（XX旅游：xx_app)，回复“中奖名单”即可查看详细中奖名单及领取须知！后续还会有更多活动惊喜，期待您的参与~官方网站：xxsite.cn</samp>

错误点在于不可以在 [通知类](#通知类) 模板中发送带有抽奖中奖信息等营销信息的内容。解决方案是**在创建模板的时候选择 [营销类短信](#营销类)**。

另外，有一些行业相关的敏感词语是不允许发送的，错误范例如下：

<samp ng-non-bindable class="bubble">业主还在苦苦等待你的反馈，你认领的房源已超过1小时没有填写核实结果，请尽快登录XX客户端，在“业主--待处理”列表中进行填写。【XX网】</samp>

<div class="callout callout-danger">无论是通知类还是营销类短信，凡包含<u>房源</u>、<u>借贷</u>这类敏感词都被禁止发送。</div>

### 营销类模板

#### 销售类

【正确范例】

<samp ng-non-bindable class="bubble">X牌新款春装已经上市，明星夫妻同款你值得拥有！详情请咨询当地X牌门市店，或者直接登录 www.xxxx.com 查询门市店，或者拨打 010-00000000，凭短信可享 9 折优惠。</samp>

#### 应用推广类

【正确范例】

<samp ng-non-bindable class="bubble">还在找寻同桌的 TA 吗？还在烦恼过年回家联系不上老同学吗？iOS 用户在 App Store 搜索：找同学，下载最新版的找同学，让同学聚会重温往日时光！</samp>

注意：应用的下载链接必须是明文，不可设置为参数。


## Demo

为了方便开发者理解短信服务流程，我们特地开发了专门针对短信服务的 [LeanCloud SMS Demo](https://github.com/wujun4code/LeanCloud_SMS_Tutorial)，开发者可以通过代码学习和了解。


## 常见问题

详情请参照 [短信收发常见问题一览](rest_sms_api.html#常见问题_FAQ)。
