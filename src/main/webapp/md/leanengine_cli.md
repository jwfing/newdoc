# 云引擎命令行工具使用详解

## 介绍

云引擎命令行工具（在 NPM 上包名为 leancloud-cli）是用来管理、部署云引擎项目的命令行工具。通过它你可以部署、发布、回滚云引擎代码，并且可以对同一个云引擎项目做多应用管理，还可以查看云引擎日志，批量上传文件到 LeanCloud 平台上等。

## 安装 Node.js 运行环境

首先，你需要安装 [Node.js](http://nodejs.org/) 环境以及 [npm](https://npmjs.org/) 包管理工具。

### Linux

Debian、Ubuntu 可以执行：

```sh
sudo apt-get install nodejs
```

RedHat、CentOS 可以执行：

```sh
sudo yum install nodejs
```

提示：官网仓库的 Node.js 版本可能比较滞后，你可以从 [第三方源](https://github.com/nodesource/distributions) 安装。

### Mac OS X

可以通过 [Homebrew](http://brew.sh/)（需要自行安装）安装：

```sh
brew install nodejs
```

### Windows

可在 [官网](https://nodejs.org) 下载安装包，Windows 系统用户请确保在系统盘 C 盘默认目录安装 Node.js，否则命令行工具可能无法正常运行。

## 安装命令行工具

```sh
npm install -g leancloud-cli
```

若在 Linux/Mac 上将 Node.js 安装到了整个系统，请在命令前添加 `sudo`；若在 Windows 上选择了「为所有用户安装 Node.js」，请以管理员权限打开一个命令行窗口再安装。

NPM 官方仓库在国内比较慢，请耐心等待。你也可以从国内的 [cnpm](https://npm.taobao.org/) 安装，速度会更快：

```sh
npm install -g leancloud-cli --registry=https://registry.npm.taobao.org
```

### Bash Completion

该功能可以让你在使用 lean 命令时得到自动补全（用 `tab` 补全），不安装也不会影响正常使用，Mac 用户需要先按照下一小节的方法安装 `bash-completion`。

下载 [avoscloud_completion.sh](https://raw.githubusercontent.com/leancloud/leancloud-cli-command/master/avoscloud_completion.sh) 保存到某个目录，例如通常保存为 `~/.leancloud_completion.sh`，然后在 `~/.bashrc` 或者 `~/.bash_profile` 文件中添加：

```sh
source ~/.leancloud_completion.sh
```

然后重启终端，就可以让 lean 命令拥有自动提示和完成功能。

### Mac 上安装 bash-completion

```sh
brew install bash-completion
```

请将下面的内容添加到 `~/.bash_profile` 文件中：

```sh
if [ -f $(brew --prefix)/etc/bash_completion ]; then
  . $(brew --prefix)/etc/bash_completion
fi
```

### 常见问题排查

如果安装完执行仍然出现一些问题，请尝试下列步骤：

**移除命令行工具：**

```sh
npm uninstall -g leancloud-cli
```

移除之后，确认命令行工具不可运行，即执行 lean 命令时会有如下提示：

```sh
C:\Users\wchen\workspace\cloud-code-unit-test_v2.0>lean
'lean' 不是内部或外部命令，也不是可运行的程序或批处理文件。
```

**清除缓存：**

```sh
npm cache clean
```

如果有安装 `cnpm`，还需要清除 `cnpm` 的缓存：

```sh
cnpm cache clean
```

然后使用 `npm` 重新安装 leancloud-cli 命令行工具。

## 使用

安装成功之后，直接在 terminal（终端）运行 `lean -h`，输出帮助信息：

```sh
$ lean -h

  Usage: lean [options] [command]


  Commands:

    up [options]                             本地启动云引擎应用。
    search <keywords...>                     根据关键字查询开发文档。
    new [options]                            创建云引擎项目。
    deploy [options]                         部署到云引擎。
    publish [options]                        发布预备环境代码到生产环境。
    status [options]                         查询当前部署状态。
    undeploy [options]                       从 LeanEngine 平台清除云引擎部署，包括生产环境和预备环境。
    logs [options]                           查看云引擎日志。
    image                                    应用镜像管理。
    instance                                 应用实例管理。
    app                                      多应用管理，可以使用一个云引擎项目关联多个 LeanCloud 应用。
    cql [options]                            进入 CQL 查询交互。
    redis                                    LeanCache Redis 命令行。
    upload [options] <file-or-directory...>  导入文件到 LeanCloud 平台，如果是目录，则会将该目录下的文件递归导入。
    clear [options]                          清除本地状态，在输入 app id 或者 master key 错误的情况下使用。
    help [cmd]                               显示关于 [cmd] 命令的帮助信息。

  Options:

    -h, --help     output usage information
    -V, --version  output the version number
```

可以通过 `-V` 选项查看版本：


```sh
$ lean -V
1.1.0
```

后面凡是以 `$ lean` 开头的即表示在终端里执行这个命令。

## 创建项目

在安装命令行工具后，除了下载我们在 Github 上维护的 [新应用项目框架（Node.js）](https://github.com/leancloud/node-js-getting-started) 之外，你也可以通过 `new` 命令来创建一个新的云引擎项目：

```sh
$ lean new
```


窗口会提示输入应用的 Id 和 Master Key，该信息可以在 [控制台 / 设置 / 应用 Key](/app.html?appid={{appid}}#/key) 中找到。


<div class="callout callout-info">复制粘贴 Master Key 后，窗口不会有任何显示，直接按回车键确认即可。</div>

接下来选择项目语言，确认后一个新的云引擎项目框架就被创建出来了：

```sh
开始输入应用信息，这些信息可以从'开发者平台的应用设置 -> 应用 key'里找到。
请输入应用的 Application ID: GQexGUn5wCPV7jp0eR2gkQxI-gzGzoHsz
请选择项目语言，Node.js(N) 或 Python(P): n
正在创建项目 ...
  .gitignore
  .jshintrc
  app.js
  cloud.js
  package.json
  public/
  public/stylesheets/
  public/stylesheets/style.css
  README.md
  routes/
  routes/todos.js
  server.js
  views/
  views/error.ejs
  views/index.ejs
  views/todos.ejs
关联应用：new_app -- GQexGUn5wCPV7jp0eR2gkQxI-gzGzoHsz
切换到应用 new_app
项目创建完成！
```

进入 `new_app` 目录就可以看到新建立的项目。

## 本地运行

进入项目目录：

```sh
$ cd new_app
```

安装项目本身的依赖（以 Node.js 为例）：

```sh
$ npm install
```

启动应用：

```sh
$ lean up
```

可能会提示输入应用的 Master Key，粘贴后窗口不会有任何显示，直接回车，即可在本机调试云引擎。

* 通过浏览器打开 <http://localhost:3000>，进入 web 应用的首页。
* 通过浏览器打开 <http://localhost:3001>，进入云引擎云函数和 Hook 函数调试界面。

`lean up` 命令包装了 [nodemon](https://github.com/remy/nodemon)，它会监视文件变更，修改代码后会自动重启进程，无需手工重启命令行工具就可以看到代码的最新效果。

**提示**：如果想变更启动端口号，可以使用 `lean up -P 新端口号` 命令来指定。

**提示**：命令行工具所有自命令都可以通过 `-h` 参数来查看详细的参数说明信息，比如 `lean up -h`。

更多关于云引擎开发，请参考 [云引擎服务总览](leanengine_overview.html) 。

## 部署

### 从本地代码部署

在你开发和本地测试云引擎项目通过后，你可以直接将本地源码推送到 LeanCloud 云引擎平台运行，只要执行 `deploy` 命令：

```sh
$ lean deploy
```

对于使用了免费版云引擎的应用，这个命令会将本地源码部署到线上的生产环境，无条件覆盖之前的代码（无论是从本地仓库部署、Git 部署还是在线定义）；如果是有预备环境的专业版应用，这个命令会先部署到预备环境，后续需要再用 `lean publish` 部署到生产环境。

在部署的过程中会实时地打印进度：

```sh
部署到：生产环境
压缩项目文件 ...
生成临时文件：/var/folders/90/y1fg6dds0fzg1ljs3cjqcl040000gp/T/1463385551840.zip
开始上传项目文件 ...
2016-05-16 15:59:14 [INFO] 开始构建 20160516-155914
2016-05-16 15:59:14 [INFO] 正在下载应用代码 ...
2016-05-16 15:59:15 [INFO] 正在解压缩应用代码 ...
2016-05-16 15:59:15 [INFO] 运行环境: nodejs (leanengine/nodejs-base-4.x)
2016-05-16 15:59:15 [INFO] 从之前的构建中恢复依赖项 ...
2016-05-16 15:59:15 [INFO] 正在下载和安装依赖项 ...
2016-05-16 15:59:27 [INFO] 依赖项体积：16.69MB
2016-05-16 15:59:27 [INFO] 缓存最新的依赖项 ...
2016-05-16 15:59:28 [INFO] 存储镜像到仓库 ...
2016-05-16 16:00:33 [INFO] 镜像构建完成：20160516-155914
2016-05-16 16:00:33 [INFO] 开始部署 20160516-155914 到 web1
2016-05-16 16:00:33 [INFO] 正在创建新实例 ...
2016-05-16 16:00:49 [INFO] 正在启动新实例 ...
2016-05-16 16:00:50 [INFO] 实例启动成功：{"runtime":"nodejs-v4.4.3","version":"0.4.0"}
2016-05-16 16:00:50 [INFO] 正在统一切换新旧实例 ...
2016-05-16 16:00:50 [INFO] 正在更新云函数信息 ...
2016-05-16 16:00:50 [INFO] 部署完成：1 个实例部署成功

部署成功

NAME      STATUS   GROUP NAME  QUOTA       IMAGE TAG        DEPLOYED           CREATED
staging1  running  staging     5           20160516-155914  a few seconds ago  2 months ago
web1      stopped  web         1CPU/512MB  20160511-134022  a day ago          4 months ago
web2      stopped  web         5           20160511-134022  a day ago          25 days ago
```

默认部署备注（将会显示在 LeanCloud 的网站控制台上）是简单的一句 `从命令行工具构建`，你可以通过 `-o` 选项来自定义部署备注：

```sh
$ lean deploy -o '添加 XXX 功能'
```

部署之后，你可以通过 curl 命令，或者访问你设置的 `${your_app_domain}.leanapp.cn` 的二级域名对应的专用测试域名 `stg-${your_app_domain}.leanapp.cn` 测试你的云引擎代码。

### 从 Git 仓库部署

如果你的代码是保存在某个 Git 仓库，例如 [Github](https://github.com) 上，你也可以请求 LeanCloud 平台从 Git 仓库获取源码并自动部署，这个操作可以在云引擎的部署菜单里完成，也可以在本地执行 `deploy` 命令和 `-g` 选项配合完成：

```sh
$ lean deploy -g
```

* `-g` 选项指定要求从 Git 仓库部署，Git 仓库地址必须已经在云引擎菜单里保存。

默认部署都将是 master 分支的最新代码，你可以通过 `-r <revision>` 来指定部署特定的 commit 或者 branch。

## 发布到生产环境

注意：体验版用户没有预备环境，因此不需要发布这个步骤。

预备环境如果测试没有问题，你希望将预备环境的云引擎代码切换到生产环境，你可以使用开发者平台的云引擎部署菜单做发布，也可以直接运行 `publish` 命令：

```sh
$ lean publish
```

就会将预备环境的云引擎代码发布到生产环境：

```sh
2016-05-16 16:03:41 [INFO] 开始部署 20160516-160159 到 web1,web2
2016-05-16 16:03:41 [INFO] 正在创建新实例 ...
2016-05-16 16:03:41 [INFO] 正在创建新实例 ...
2016-05-16 16:03:44 [INFO] 正在启动新实例 ...
2016-05-16 16:03:45 [INFO] 实例启动成功：{"runtime":"nodejs-v4.4.3","version":"0.4.0"}
2016-05-16 16:03:53 [INFO] 正在启动新实例 ...
2016-05-16 16:03:54 [INFO] 实例启动成功：{"runtime":"nodejs-v4.4.3","version":"0.4.0"}
2016-05-16 16:03:54 [INFO] 正在统一切换新旧实例 ...
2016-05-16 16:03:55 [INFO] 正在更新云函数信息 ...
2016-05-16 16:03:55 [INFO] 部署完成：2 个实例部署成功

部署成功

GROUP NAME  ENV   CURRENT IMAGE    INSTANCES                      CREATED       DEPLOYED
web         prod  20160516-160159  [web1(running),web2(running)]  4 months ago  a few seconds ago
```

## 查看云引擎状态

可以通过 `lean instance list` 命令查询当前所有云引擎实例的状态：

```sh
NAME      STATUS   GROUP NAME  QUOTA       IMAGE TAG        DEPLOYED       CREATED
staging1  running  staging     5           20160516-160159  5 minutes ago  2 months ago
web1      running  web         1CPU/512MB  20160516-160159  4 minutes ago  4 months ago
web2      running  web         5           20160516-160159  4 minutes ago  25 days ago
```

## 查看日志

使用 `logs` 命令可以查询云引擎最新日志：

```sh
$ lean logs
2016-05-16 16:03:53 [PROD] [INFO]
2016-05-16 16:03:53 [PROD] [INFO] > playground@1.0.0 start /home/leanengine/app
2016-05-16 16:03:53 [PROD] [INFO] > node server.js
2016-05-16 16:03:53 [PROD] [INFO]
2016-05-16 16:03:54 [PROD] [INFO] Node app is running, port: 3000
2016-05-16 16:03:54 [PROD] [INFO] connected to redis server
2016-05-16 16:03:54 [PROD] [INFO] 实例启动成功：{"runtime":"nodejs-v4.4.3","version":"0.4.0"}
2016-05-16 16:03:54 [PROD] [INFO] 正在统一切换新旧实例 ...
2016-05-16 16:03:55 [PROD] [INFO] 正在更新云函数信息 ...
2016-05-16 16:03:55 [PROD] [INFO] 部署完成：2 个实例部署成功
```

默认返回最新的 10 条，最新的在最下面。

可以通过 `-n` 选项设定返回的日志数目，例如返回最近的 100 条

```sh
$ lean logs -n 100
```

也可以加上 `-t` 选项来自动滚动更新日志，类似 `tail -f` 命令的效果：

```sh
$ lean logs -t
```

当有新的云引擎日志产生，都会自动填充到屏幕下方。

## 多应用管理

我们为 lean 添加了多应用管理功能，类似 git 的多分支功能。使用这个功能，允许你将同一个云引擎项目部署到多个 LeanCloud 应用上。

### 查看应用状态

使用 `lean app list` 可以查看当前应用列表，当前激活的应用前会显示一个星号：

```sh
$ lean app list
  github-commit-ical    d1ARJHxmAze1Qx8mWd75N6MM
* leanengine-playground hOm6fe8KE285nUXsB6AR267i
```

此时，执行 `deploy`、`publish`、`status`、`logs` 等命令都将是针对当前激活的应用。

### 添加应用

如果你想将 new_app 发布到其他 LeanCloud 应用，你可以通过 `add` 命令来添加一个应用：

```sh
$ lean app add new_app <new app 的应用 id>
```

`add` 接收两个参数，第一个是应用的名称，用于后续的显示和切换，第二个是新应用的 id，可以在应用设置的应用 Key 信息里找到。

添加成功将打印：

```sh
关联应用：other_app -- <应用 id>
```

通过 `app list` 命令将看到两个应用：

```sh
$ lean app list
* origin    7104en0u071tcb5d1tr2juxa499ouvdn1gm5szq47nqzt06q
  other_app 1qdney6b5qg2i69t79yq941krrwdu3glt0ot69re6w7xv6lf
```

前面有星号的应用，表示是当前应用。

### 切换应用

从当前应用切换到另一个应用，可以使用 `checkout <应用名称>`:

```sh
$ lean app checkout other_app
Switched to app other_app
```

切换成功后，执行 `deploy`、`publish`、`status`、`logs`等命令都将运行在 `other_app` 上。如果你过去没有部署过，第一次部署的时候会要求你输入新应用的 master key。

通过 `app` 命令可以看到当前应用已经是 `other_app`:

```sh
$ lean app
* other_app <应用 id>
```

如果你想在不切换应用的情况下去部署云引擎到其他应用，也可以通过 `--app` 选项来指定，例如:

```sh
$ lean deploy --app other_app
```

这样就无需通过 checkout 切换应用，就可以部署项目到其他应用。`status`、`publish` 等应用相关的命令也同样支持 `--app` 选项。

### 移除应用

同样，你可以删除一个应用，使用  `lean rm` 命令：

```sh
$ lean app rm other_app
Removed app: other_app
```

## 批量上传文件

如果你有一些文件希望上传到 LeanCloud 平台上，可以通过 `upload` 命令，既可以上传单个文件，也可以批量上传一个目录下（包括子目录）下的所有文件。

```sh
$ lean upload public/index.html
Uploads /Users/dennis/programming/avos/new_app/public/index.html successfully at: http://ac-7104en0u.qiniudn.com/f9e13e69-10a2-1742-5e5a-8e71de75b9fc.html
```

上传成功后会显示文件在 LeanCloud 平台上的 URL。

上传 images 目录下的所有文件：

```sh
$ lean upload images/
```

## CQL 交互查询

0.6.1 版本开始，我们支持 cql 命令来进入交互查询界面：

```sh
$ lean cql
```

使用 [CQL](./cql_guide.html) 语言做查询，结果如图：

![image](images/cql_command.png)

## 其他命令

为了方便开发阶段查询资料或者文档，可以使用 `search` 命令：

```sh
$ lean search AVObject
```

这将打开浏览器，显示 [搜索结果](/search.html?q=AVObject)。

也可以查询多个关键字，空格隔开即可：

```sh
$ lean search 云引擎 命令行
```

## 贡献

`leancloud-cli` 本身是开源，基于 [GNU LGPL](https://www.gnu.org/licenses/lgpl.html) 协议，源码托管在 Github: <https://github.com/leancloud/avoscloud-code-command>，欢迎大家贡献。
