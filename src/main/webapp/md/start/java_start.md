通过配置 pom.xml 来下载 LeanCloud Java SDK：

``` xml
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
配置完成以后，你就可以使用 LeanCloud Java SDK 了。

在 `main` 函数中间调用 `AVOSCloud.initialize` 来设置你的 Application 相关的信息：

``` java
  public static void main(String[] args) {
    AVOSCloud.initialize("{{appid}}","{{appkey}}","{{masterkey}}");
  }
```
创建应用后，可以在[控制台 > 应用设置](/app.html?appid={{appid}}#/key) 里面找到应用对应的 appid 、appkey 和 masterkey。

接下来你就可以尝试测试一段代码，拷贝以下代码到你的 app 里，比如直接放在一个方法中间，并且在 `main` 函数中间调用：

``` java
AVObject testObject = new AVObject("TestObject");
testObject.put("foo", "bar");
testObject.save();
```

运行你的 app，一个类 `TestObject` 的新对象将被发送到 LeanCloud 并保存下来。当你做完这一切，访问 [控制台 - 数据管理](/data.html?appid={{appid}}#/TestObject) 可以看到上面创建的 `TestObject` 的相关数据。

LeanCloud 同时也提供了一个的 Java Servlet 运行环境，我们称之为 LeanEngine（云引擎），更推荐基于 LeanEngine 来实现并部署 Java war 包相关的代码。详细请参考 [云引擎文档](leanengine_overview.html) 。
