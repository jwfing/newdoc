#### 安装

运行环境要求 PHP 5.3 及以上版本，以及
[cURL](http://php.net/manual/zh/book.curl.php)。

**composer 安装**

如果你的项目使用 composer, 那么安装 LeanCloud PHP SDK 将非常容易：

```bash
composer require leancloud/leancloud-sdk
```

**手动下载安装**

* 前往发布页面下载最新版本: https://github.com/leancloud/php-sdk/releases

```bash
cd $APP_ROOT
wget https://github.com/leancloud/php-sdk/archive/vX.X.X.zip
```

* 将压缩文件解压并置于项目文件夹下，如 $APP_ROOT/vendor/leancloud

```bash
unzip vX.X.X.zip
mv php-sdk-X.X.X $APP_ROOT/vendor/leancloud
```

#### 初始化

完成上述安装后，请加载库（在项目的一开始就需要加载，且只需加载一次）：

```php
require_once("vendor/autoload.php");               // composer 安装
require_once("vendor/leancloud/src/autoload.php"); // 手动安装
```

如果已经创建应用，可以在 [**控制台** > **应用设置**](/app.html?appid={{appid}}#/key)
里面找到应用的 id 和 key。然后需要对应用初始化：


```php
// 参数依次为 appId, appKey, masterKey
LeanCloud\LeanClient::initialize("{{appid}}", "{{appkey}}", "{{masterkey}}");

// 我们目前支持 CN 和 US 区域，默认使用 CN 区域，可以切换为 US 区域
LeanCloud\LeanClient::useRegion("US");
```

测试应用已经正确初始化：

```php
LeanCloud\LeanClient::get("/date"); // 获取服务器时间
// => {"__type": "Date", "iso": "2015-10-01T09:45:45.123Z"}
```

如果遇到类似「Fatal error: Uncaught exception 'RuntimeException'...CURL connection...」的系统报错，请参考 [修复 CURL connection 错误](#error_curl_connection)。

#### 使用

初始化应用后，就可以开始创建数据了：

```php
use LeanCloud\LeanObject;
use LeanCloud\CloudException;

$obj = new LeanObject("TestObject");
$obj->set("name", "alice");
$obj->set("height", 60.0);
$obj->set("weight", 4.5);
$obj->set("birthdate", new \DateTime());
try {
    $obj->save();
} catch (CloudException $ex) {
    // CloudException 会被抛出，如果保存失败
}

// 获取字段值
$obj->get("name");
$obj->get("height");
$obj->get("birthdate");

// 原子增加一个数
$obj->increment("age", 1);

// 在数组字段中添加，添加唯一，删除
// 注意: 由于API限制，不同数组操作之间必须保存，否则会报错
$obj->addIn("colors", "blue");
$obj->save();
$obj->addUniqueIn("colors", "orange");
$obj->save();
$obj->removeIn("colors", "blue");
$obj->save();

// 在云存储上删除它
$obj->destroy();
```


大功告成，访问 [**控制台** > **数据管理**](/data.html?appid={{appid}}#/TestObject)
可以看到上面创建的 TestObject 的相关数据。


请参考详细的 [API 文档](/api-docs/php)。

#### 常见问题

一、<a id="error_curl_connection" name="error_curl_connection"></a>【系统报错】**Fatal error: Uncaught exception 'RuntimeException' with message 'CURL connection (https://api.leancloud.cn/1.1/...) error: 60 60**

错误原因是 Windows curl 没有证书无法发送 https 请求，解决方案有两种：

1. 将证书文件 `curl-ca-bundle.crt` 置于与 `curl.exe` 相同的目录，通常情况下位于 `C:\Windows\system32`。
1.  将证书文件 `curl-ca-bundle.crt` 置于自定义的目录，然后在 `php.ini` 中明确设置证书的位置：

  ```
[PHP]
curl.cainfo={自定义的目录路径}/ca-bundle.crt
  ```

