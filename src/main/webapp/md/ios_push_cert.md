# iOS 推送证书设置指南

## 注册 App ID

首先注册 App ID。如果你已经注册过带有推送通知 App ID，可以跳过以下步骤：

1. 登录 [Apple Developer Center](https://developer.apple.com/account/)
2. 点击 **Certificates, Identifiers & Profiles**
3. 选择 **Identifiers** 下的 **App IDs**
4. 点击右上方的加号按钮（**\+**）
5. 填写 App ID 的基本信息
 ![Create App ID](images/ios_cert_v2/create_app_id.png)
6. 选择创建 Explicit App ID，填入 App 的 **Bundle ID**。注意，<u>Explicit App ID 不能包含星号</u>（\*）。
 ![Enter Explicit bundle ID](images/ios_cert_v2/enter_explicit_app_id.png)
7. 选择 App ID 需要开启的服务，此处要勾选 **Push Notifications**。
 ![Select push notification](images/ios_cert_v2/select_push_notification.png)
8. 点击 **Continue**
9. 确认注册信息，然后点击 **Register**。
10. 点击 **Done**

## 开启推送通知

为已有的 App ID 开启推送通知的步骤如下：

1. 选择要开启推送通知的 App ID
2. 勾选 **Push Notifications** 复选框。
 ![Edit push notification](images/ios_cert_v2/edit_push_notification.png)
3. 如果弹出警告对话框，点击 **OK**。
4. 点击 **Done**

## 创建推送证书

每个 App ID 都需要单独的客户端 SSL 证书来和 APNs 通信。从 2015 年 12 月 17 日起，APNs 服务全面支持 HTTP/2 协议，推送证书也进行了相应的调整，即推出了 Universal Push Notification Client SSL 证书，以下简称 **Universal 推送证书**。通过下图中红框标注的 **Apple Push Notification service SSL (Sandbox & Production)** 这种方式创建的推送证书就是「Universal 推送证书」。

我们推荐使用「Universal 推送证书」来进行推送服务。

   ![what is Universal Push Notification Client SSL Certificate](images/ios_cert_v2/what_is_universal_push_notification_client_ssl_certificate.png)
  
  图中其他方式就叫做「非 Universal 方式」，以下简称 **非 Universal 推送证书**：
  
  ![what is not Universal Push Notification Client SSL Certificate](images/ios_cert_v2/what_is_not_universal_push_notification_client_ssl_certificate.png)

**注意：**要使用 Universal 推送证书，LeanCloud iOS SDK 需要更新到 v3.2.8 以上的版本，以获得更快速的推送效果。

### 创建 Universal 推送证书

请按以下步骤创建「Universal 推送证书」：

1. 前往 [Apple Developer Center](https://developer.apple.com)
2. 点击 **Certificates, Identifiers & Profiles**

  ![enter Certificates, Identifiers & Profiles](images/ios_cert_v2/enter_certificates_identifiers_profiles.png)
3. 选择在 **Certificates** 栏下的 **All**
4. 点击下图中红色边框内的加号按钮

  ![Create SSL certificate](images/ios_cert_v2/create_ssl_certificate.png)
5. 勾选 **Production** 栏下的 **Apple Push Notification service SSL (Sandbox & Production)**，点击下一步。
  
  ![Select push certificate](images/ios_cert_v2/what_is_universal_push_notification_client_ssl_certificate.png)
6. 从 App ID 下拉菜单中选择相应的 App ID，点击下一步。

  ![select App ID](images/ios_cert_v2/select_app_id.png)

7. 这时会出现 **About Creating a Certificate Signing Request (CSR)**。

  ![guide to create a CSR](images/ios_cert_v2/guide_to_create_a_csr.png)

  根据它的说明创建 **Certificate Signing Request**。

  ![how to create a CSR](images/ios_cert_v2/how_to_create_a_csr.png)
8. 点击下图中的 **Choose File** 按钮

  ![upload CSR File](images/ios_cert_v2/upload_csr_file.png)
9. 上传刚刚生成的 `.certSigningRequest` 文件，生成 APNs Push Certificate。
10. 下载证书
11. 双击打开证书，证书打开时会启动 **Keychain Access / 钥匙串访问** 工具。
  在钥匙串访问工具中，你的证书会显示在 **证书** 中。<u>注意选择左下角的 **证书** 和左上角的 **登录**</u>。

   ![confirm create cer success](images/ios_cert_v2/confirm_create_cer_success.png)

「非 Universal 推送证书」的申请步骤与以上类似。

## 验证推送服务状态

按以下步骤来验证 App ID 的推送服务是否打开：

1. 点击 **Identifiers** 下的 **App IDs**
2. 选择与应用 Bundle ID 匹配的 App ID
3. 如果下图中红色方框中显示 **Enabled**，表示 App ID 的推送证书已配置好。

 ![Verify push notification](images/ios_cert_v2/verify_push_notification.png)

## 导出证书

1. 打开 **Keychain Access**，找到要导出的证书（证书名有前缀 **Apple Push Services**）。
2. 右键点击证书，选择 **导出 / Export**，选择保存格式为 `.p12`。这时程序会提示你输入密码来保护导出的证书，**请不要输入密码**，让两个输入框为空，点击  **OK**。接着又会弹出一个对话框，要求输入 OS X 账户的密码来允许从 Keychain Access 中导出，请填写密码并点击 **允许 / Allow**。

## 上传证书


1. 进入 [LeanCloud 应用控制台 > 消息 > 推送 > 设置](/messaging.html?appid={{appid}}#/message/push/conf)，然后就可以看到下面的页面：

  
  ![Push certificate configure](images/ios_cert_v2/push_certificate_config.png)
2. 根据你的证书类别进行上传。这里请注意区分证书的类别，<u>测试环境</u>证书和<u>生产环境</u>证书请勿混淆。
  - 「Universal 推送证书」需要上传到图中的 **自定义证书** 中。
    <div class="callout callout-info">在 LeanCloud 的推送服务中，出于兼容性考虑，**Universal 证书只能用于生产环境**。如果需要进行推送测试，请使用图中的 **测试环境证书**。</div>
  - 在 LeanCloud 的推送服务中，不同类型的推送证书能够服务的环境略有不同，对应关系如下图所示：
    
    ![relation between cer and prod or dev](images/ios_cert_v2/relation_between_cer_and_prod_or_dev.png)

## 上传证书失败

如果无法上传推送证书，通常是因为证书有问题，一般由下列原因导致：

1. 证书不是推送证书
2. 证书导出格式有误

验证导出的证书是不是推送证书有两种方法：

1. 通过证书的名字来判断，推送证书的名字中会包含「Push Service」或者「Pass Type ID」。
2. 配合真实设备来测试导出的证书是否有效。可以利用第三方工具，例如 [NWPusher](https://github.com/noodlewerk/NWPusher) 来快速测试。

目前 LeanCloud 只接受 `p12` 格式的证书。因此在导出证书时，必须选择 `p12` 作为导出格式。

上传证书时，LeanCloud 会进一步校验证书，帮助开发者发现错误。上传程序会检查证书的名字是否包含以下前缀：

* `Apple Push Services`
* `Apple Development IOS Push Services`
* `Apple Production IOS Push Services`
* `Pass Type ID`

如果程序发现证书的名字中不包含以上前缀，则校验失败，证书将无法上传。

Apple 未来可能会修改推送证书的名字前缀，我们会及时更新前缀列表，同时也欢迎大家来补充。
