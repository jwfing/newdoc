angular.module('app').controller('DemoCtrl', ['$http', '$scope', '$rootScope', '$timeout',
  function($http, $scope, $rootScope, $timeout) {
    function setByHash() {
      var hash = location.hash.slice(2);
      if (!$scope.plats[hash]) {
        $scope.currentSDKType = 'all';
      } else {
        $scope.currentSDKType = location.hash.slice(2);
      }

    }
    $scope.keys = function(obj){
      return obj? Object.keys(obj) : [];
    }

    $scope.plats = {
      'ios': 'iOS',
      'android': 'Android',
      'unity': 'Unity',
      'winphone': 'Windows Phone',
      'js': 'JavaScript',
      'node': 'Node.js'
    }
    $scope.demos = {
      'ios': [{
        name: 'LeanStorage Demo',
        desc: '展示了 LeanCloud 数据存储 SDK 的各种基础和高级用法，帮助 LeanCloud 开发者尽快上手 SDK。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/ios-simple-demo',
        type: 'ios'
      },{
        name: 'LeanChat + 表情 mm',
        desc: '全面展示了如何在 LeanCloud 实时通讯 SDK 中集成「表情 mm SDK」，快速方便地为聊天应用加入丰富的表情功能。',
        downPath: '',
        mdPath: 'https://github.com/siyanhui/bqmm-leanchat-demo-ios/tree/master/leanchat',
        type: 'ios',
        image: 'images/emoji-1.png'
      },{
        name: 'Feedback Demo',
        desc: '演示了 LeanCloud 反馈功能的用法。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/feedback-demo',
        type: 'ios'
      }, {
        name: 'FreeChat',
        desc: '演示了 LeanCloud 实时通信 SDK 的使用方法，包括单聊、群聊、聊天室，也支持图片、文字、语音等消息类型。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/FreeChat',
        type: 'ios'
      },{
        name: '微转',
        desc: '一个基于微博的数码设备平台，客户端和后台全部基于 LeanCloud 服务来实现。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/VZ',
        type: 'ios'
      }],
      'android': [{
        name: 'LeanStorage Demo',
        desc: '展示了 LeanCloud 数据存储 SDK 的各种基础和高级用法，包括用户系统、文件上传下载、子类化、对象复杂查询等。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/android-simple-demo',
        type: 'android'
      },{
        name: 'LeanPush Demo',
        desc: '一个使用了 LeanCloud 推送消息服务的简单 Demo，直接在客户端推送消息，并自己接收。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/android-push-demo',
        type: 'android'
      },{
        name: 'LeanChat + 表情 mm',
        desc: '全面展示了如何在 LeanCloud 实时通讯 SDK 中集成「表情 mm SDK」，快速方便地为聊天应用加入丰富的表情功能。',
        downPath: '',
        mdPath: 'https://github.com/siyanhui/bqmm-leanchat-demo-android',
        type: 'android',
        image: 'images/emoji-1.png'
      },{
        name: 'Todo Demo',
        desc: '一个创建、编辑、删除和搜索 Todo 的示例，演示功能包括数据增删改查、子类化 com.avos.demo.Todo 类、统计功能、统计的自定义事件功能、应用内搜索等。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/android-todolist',
        type: 'android'
      },{
        name: 'Anytime',
        desc: '展示了用户注册、登录、登出和忘记密码等用户系统相关的功能，相对复杂一些的数据增删改查操作，以及消息推送。',
        downPath: '',
        mdPath: 'https://github.com/lzwjava/AnyTime',
        type: 'android'
      }],
      'unity': [{
        name: 'FlappyBird',
        desc: '支持 Unity 平台的 FlappyBird 小游戏，演示了 LeanCloud 数据存储 SDK 的大多数功能。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/unity-sdk-demos',
        type: 'unity'
      }],
      'winphone': [{
        name: 'Tutorial',
        desc: 'Windows Phone 平台的示例程序，演示了 LeanCloud 数据存储 SDK 的大多数功能。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/windows-phone-sdk-demos',
        type: 'winphone'
      }],
      'js': [{
        name: '云引擎 Todo Demo',
        desc: '演示了基本的用户注册、会话管理、业务数据的增删查改、简单的 ACL 使用。本项目可以作为初学云引擎和 JavaScript SDK 使用。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/leanengine-todo-demo',
        type: 'js'
      }],
      'node': [{
        name: 'LeanCache Demos',
        desc: '包含了抢红包、排行榜缓存、关联数据缓存、图形验证码、节点选举和锁、任务队列、热点只读数据缓存等实用代码，展示出 Redis 缓存的实际应用效果。',
        downPath: '',
        mdPath: 'https://github.com/leancloud/lean-cache-demos',
        type: 'node'
      }]
    }
    $scope.$watch('currentSDKType',function(){
      $scope.displayDemos = [];
      var arr = [];
      if($scope.currentSDKType == 'all'){
        angular.forEach($scope.demos, function(v,k){
          arr = arr.concat(v) ;
        });
        $scope.displayDemos = arr;
      }else{
        $scope.displayDemos = $scope.demos[$scope.currentSDKType]
      }
    })
    $scope.setCurrentType = function(type) {
      $scope.currentSDKType = type;
    }
    setByHash();
    $(window).on('hashchange', function() {
      $timeout(function() {
        setByHash();
      })
    });


  }
]);
