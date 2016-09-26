angular.module("app").controller("DownCtrl",['$scope', '$http', function($scope, $http){
    $scope.download = {};
    $scope.downselect ={};

    var modules = [ 'ios', 'osx', 'android', 'javascript', 'unity3d', 'wp', 'jschat', 'watchos', 'tvos'];
    angular.forEach(modules,function(v, k){
        $http.get("https://download.avoscloud.com/1/sdkComponents/"+v).then(function(result){
           $scope.download[v] = result.data;
           if(!$scope.downselect[v]){
            $scope.downselect[v]={};
           }
           angular.forEach(result.data.required,function(v1,k){
             $scope.downselect[v][v1.name] = true
           });

        });
    });

    $scope.getDownSize = function(type){
        var size = 0;
        if($scope.download[type]){
            angular.forEach($scope.download[type].required,function(v,k){
                if($scope.downselect[type][v.name]){
                    size += v.size;
                }
            });
            angular.forEach($scope.download[type].optional,function(v,k){
                if($scope.downselect[type][v.name]){
                    size += v.size;
                }
            });
        }
        return prettyBytes(size);
    }

    var downloadURL = function downloadURL(url) {
        var hiddenIFrameID = 'hiddenDownloader',
            iframe = document.getElementById(hiddenIFrameID);
        if (iframe === null) {
            iframe = document.createElement('iframe');
            iframe.id = hiddenIFrameID;
            iframe.style.display = 'none';
            document.body.appendChild(iframe);
        }
        iframe.src = url;
    };
    $scope.download = function (type, subType){
        var components = [];
        angular.forEach($scope.download[type].required,function(v,k){
            if($scope.downselect[type][v.name]){
                components.push(v.name);
            }
        });
        angular.forEach($scope.download[type].optional,function(v,k){
            if($scope.downselect[type][v.name]){
                components.push(v.name);
            }
        });
        if (!subType) {
          subType = '';
        }
        var url = "//download.avoscloud.com/1/downloadSDK?type="+type+"&components="+components.join(",")+"&version=v"+$scope.sdkversion[type]+"&subType="+subType;
        downloadURL(url);
    }
}]);


$(".bspopover").popover();
$(".bstooltip").tooltip();

$(document.body).on("click","a[data-ref]",function(){
  $("a[href="+$(this).data("ref")+"]").tab("show");
});

$(function(){

  $.get("https://download.avoscloud.com/sdk/android/changelog.txt").success(function(result){
    $("#android-changelog").html(markdown.toHTML(result));
  });
  $.get("https://download.avoscloud.com/sdk/javascript/changelog.txt").success(function(result){
    $("#js-changelog").html(markdown.toHTML(result));
  });
  $.get("https://download.avoscloud.com/sdk/jschat/changelog.txt").success(function(result){
    $("#jschat-changelog").html(markdown.toHTML(result));
  });
  $.get("https://download.avoscloud.com/sdk/unity3d/changelog.txt").success(function(result){
    $("#unity3d-changelog").html(markdown.toHTML(result));
  });
  $.get("https://download.avoscloud.com/sdk/wp/changelog.txt").success(function(result){
    $("#wp-changelog").html(markdown.toHTML(result));
  });
});

