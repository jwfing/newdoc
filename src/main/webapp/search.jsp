<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
<title>Gitrac</title>
<link rel="stylesheet" href="/stylesheets/style.css" />
<link rel="stylesheet" href="https://leancloud.cn/styles/5a47bda9.app.css">
</head>

<body ng-app="searchMod" class="dashboard-init dashboard-qcloud dashboard-" ng-controller="UserCtrl">

<div class="search-container col-sm-10 col-sm-offset-1" ng-controller="SearchCtrl">

  <form class="form-search search-box" action="/search" method="GET">
    <div class="form-group">
      <input type="text" name="q" class="form-control search-query" ng-model="searcht.keyword" placeholder="搜索">
      <!-- <button type="submit" class="btn btn-primary" ng-click="pageReload()">搜索</button>     -->
    </div>
  </form>

  <hr ng-show="searchResults">

  <div class="search-content">
    <c:forEach items="${results}" var="doc">
      <div class="search-item" ng-repeat="doc in searchResults">
        <h4 class="search-title text-ellipse"><a href="${doc.url}" ng-bind-html="">${doc.title}</a></h4>
        <p class="search-context" ng-bind-html=" | to_trusted">${doc.highlighter}</p>
        <hr>
      </div>
    </c:forEach>
  </div>

</div>

</body>
</html>
