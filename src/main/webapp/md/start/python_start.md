#### 版本依赖

目前 Python SDK 只支持 Python 2，Python 3 的支持正在开发中。

#### 使用 virtualenv

如果您不需要使用 [virtualenv](https://virtualenv.pypa.io/)，可以跳过这一步。

使用 [virtualenv](https://virtualenv.pypa.io/) 可以创建一个与系统隔离的 Python 环境，在其中安装的第三方模块版本不会与系统自带的或者其他项目中的模块冲突。

```sh
virtualenv leancloud-demo
cd leancloud-demo
source bin/activate
```

之后您在当前 shell 中安装的第三方模块，都只会保存在当前项目目录下。

virtualenv 的更详细使用方法请参考[官方文档](https://virtualenv.pypa.io/en/latest/)。

#### 安装

可以选择使用 [pip](https://pip.pypa.io) 或者 [easy_install](https://pythonhosted.org/setuptools/easy_install.html) 来安装 LeanCloud SDK：

```sh
pip install leancloud-sdk
```

```sh
easy_install leancloud-sdk
```

#### 初始化


创建应用后，可以在 [控制台 - 应用设置](/app.html?appid={{appid}}#/key) 里面找到应用对应的 id 和 key。


在使用 Leancloud Python SDK 之前，需要使用 id 和 key 对 SDK 进行初始化：

```python
import leancloud

leancloud.init('{{appid}}', '{{appkey}}')
```

#### 使用

接下来就可以存储数据了：

```python
from leancloud import Object
from leancloud import LeanCloudError

TestObject = Object.extend('TestObject')
test_object = TestObject()
test_object.set('foo', 'bar')
try:
    test_object.save()
except LeanCloudError, e:
    print e
```


大功告成，访问 [控制台 - 数据管理](/data.html?appid={{appid}}#/TestObject) 可以看到上面创建的 TestObject 的相关数据。

