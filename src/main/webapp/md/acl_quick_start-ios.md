


# iOS 权限管理快速指南

**一个应用的权限管理是否健全，是判断其能否正式发布的核心标准**，任何一家应用开发厂商都不会希望自己的应用在发布之后依然存在安全漏洞。

让我们先来讨论下面这个问题：

Q：AppId 和 AppKey 泄露了怎么办？

众所周知，在使用 LeanCloud SDK 的时候，是需要在客户端显式调用初始化的方法设置 AppId 以及 AppKey，这就存在一种可能：竞争对手把你的 apk 或者打包好的安装程序进行反编译，他并不需要完全读懂你的业务逻辑代码，他只要提取出 AppId 以及 AppKey 就可以模拟客户端进行恶意的提交请求，后果不堪设想。这是一种泄露的可能性，危害极大。

还有一种可能性，开发团队的成员离职了，AppId 和 AppKey 都被 TA 曾经使用或者查看过，那么这也是一种隐患，因此，我们始终认为 AppId 和 AppKey 在面对因为人为造成的不可控的局面的时候，它的防御体系还不能满足应用安全需求，因此我们设计了 ACL 权限管理模块来帮助实现权限的管理。

关于上面的问题，简短的回答如下：

A：首先，我们推荐所有基于 LeanCloud 开发的团队的成员都采取协作者的方式参与实际的开发，再者是做好应用安装包的加密和分发管理，当然最有效的方法就是严格按照文档所建议的采用 ACL 的权限控制来做好服务端数据的权限管理，LeanCloud 不管是 SDK 还是 REST API 请求都会对 ACL 做严格的验证，鉴权失败的请求不会生效。

接下来我们用 2 张表格来介绍使用 ACL 和不使用的一些差异。

<table class="noheading">
  <tr><td colspan="2" align="center"><strong>不使用 ACL</strong></td></tr>
  <tr>
    <td>实现代码</td>
    <td valign ="top">
<pre lang="objc">
AVObject *post = [AVObject objectWithClassName:@"Post"];
[post setObject:@"大家好，我是新人" forKey:@"title"];
[post setObject:@"我喜欢看新闻和阅读报纸。" forKey:@"content"];
[post saveInBackground];
</pre>
</td>
  </tr>
  <tr>
    <td nowrap="">执行结果</td>
    <td><a href="http://ac-lhzo7z96.clouddn.com/1441871966815" target="_blank"> <img src='http://ac-lhzo7z96.clouddn.com/1441871966815' width="480"/></a></td>
  </tr>
  <tr>
    <td>模拟请求</td>
    <td align="left"><pre>curl -X PUT \
  -H "X-LC-Id:m7baukzusy3l5coew0b3em5uf4df5i2krky0ypbmee358yon" \
  -H "X-LC-Key:wzlzc2ncc7srg1uox5w2eolqpt8vidn8wsk40ce1676ieqxz" \
  -H "Content-Type: application/json" \
  -d '{"content": "修改内容！"}' \
  https://api.leancloud.cn/1.1/classes/Post/55f10b1e60b227b7b0319d71</pre></td>
  </tr>
  <tr>
    <td>请求结果</td>
    <td><pre>{"updatedAt":"2015-09-10T06:52:14.137Z","objectId":"55f10b1e60b227b7b0319d71"}</pre></p>请求成功，查看控制台，数据已被修改。
    </td>
  </tr>
  <tr>
    <td>原因</td>
    <td>未设置 ACL 属性，修改成功。只要拥有 AppId 和 AppKey 就可以提交请求，而云端会接收请求，因为它的 ACL 值是允许所有人可写可读，所以请求被云端接收。</td>
  </tr></tbody>
</table>


<table class="noheading">
  <tr><td colspan="2" align="center"><strong>使用 ACL</strong></td></tr>
  <tr>
    <td>实现代码</td>
    <td valign ="top">

<pre lang="objc">
AVObject &ast;post = [AVObject objectWithClassName:@"Post"];
[post setObject:@"大家好，我是新人" forKey:@"title"];
[post setObject:@"我喜欢看新闻和阅读报纸。" forKey:@"content"];
    
//新建一个 ACL 实例
AVACL &ast;acl = [AVACL ACL];
[acl setPublicReadAccess:YES];// 设置公开的「读」权限，任何人都可阅读
[acl setWriteAccess:YES forUser:[AVUser currentUser]];// 为当前用户赋予「写」权限
post.ACL = acl;// 将 ACL 实例赋予 Post对象
    
[post saveInBackground];
</pre>

</td>
  </tr>
  <tr>
    <td nowrap="">执行结果</td>
    <td><a href="http://ac-lhzo7z96.clouddn.com/1441871966815" target="_blank"> <img src='http://ac-lhzo7z96.clouddn.com/1441871966815' width="480"/></a></td>
  </tr>
  <tr>
    <td>模拟请求</td>
    <td align="left"><pre>curl -X PUT \
  -H "X-LC-Id:m7baukzusy3l5coew0b3em5uf4df5i2krky0ypbmee358yon" \
  -H "X-LC-Key:wzlzc2ncc7srg1uox5w2eolqpt8vidn8wsk40ce1676ieqxz" \
  -H "Content-Type: application/json" \
  -d '{"content": "修改内容！"}' \
  https://api.leancloud.cn/1.1/classes/Post/55f108f260b23c9d702330aa</pre></td>
  </tr>
  <tr>
    <td>请求结果</td>
    <td><pre>{"code":1,"error":"Forbidden writing by object's ACL."}</pre></p><strong>请求失败</strong>，查看控制台，数据未被修改。
    </td>
  </tr>
  <tr>
    <td>原因</td>
    <td>设置了 ACL 属性，请求失败，云端发现修改请求并没有携带用户的 SessionToken，而 ACL 的值显示该条 Post 只允许一个特定的用户修改，所以请求被云端拒绝。</td>
  </tr>
</table>


通过以上 2 张表格的对比仅仅是为了演示 ACL 的一个基本用法，关于 ACL 权限控制的详细用法请参照[LeanCloud 权限管理使用规范](./acl_guide-ios.html)


## FAQ

Q：APK 被反编译，其他竞争对手或者攻击者恶意模拟提交请求，怎么办？

A：首先，发现这种情况，请及时联系 LeanCloud ，会有专职工程师帮助先紧急屏蔽掉一些恶意 IP 或者恶意请求；其次，针对应用比较敏感的表，比如自定义的付费信息表，下载连接地址等容易被黑客获取进行二次攻击的敏感数据，一定要按照 ACL 的规范进行设置，比如，有些表只允许某一个特定的用户或者是某一个特定的角色读取，并且黑客无法通过源代码来获取到用户信息，因此黑客模拟的请求在 LeanCloud 服务端会因为鉴权失败而被拒绝执行，确保数据的安全。



