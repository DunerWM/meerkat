<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fm" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <link rel="stylesheet" href="/source/blog/css/reset.css"/>
    <link rel="stylesheet" href="/source/blog/css/public.css"/>
    <title>狐獴meerkat.wiki</title>
</head>
<body class="blog">
<fm:requestEncoding value="UTF-8" />
<fm:setLocale value="en_US" />
<div class="blog-index-banner">
    <div class="blog-index-banner-inner">
        <img src="/source/blog/images/meerkatlogo.png" class="logo" alt=""/>

        <div class="blog-index-banner-content">
            <h1>狐獴日记</h1>

            <h2>別告訴自己挫敗有多大，當告訴自己未來有多美好。</h2>
            <nav>
                <a href="#"></a>
            </nav>
        </div>
    </div>
</div>
<div class="list-container">
    <div class="blog-list-container">
        <div class="list-container-right">
            <ul>
                <li>
                    <a href="#">111111111</a>
                </li>
                <li>
                    <a href="#">2222222</a>
                </li>
                <li>
                    <a href="#">3333333</a>
                </li>
                <li>
                    <a href="#">4444444</a>
                </li>
            </ul>
        </div>
        <div class="list-container-content">
            <ul>
                <c:if test="${not empty pagination.data}">
                    <c:forEach items="${pagination.data}" var="bean">
                        <li>
                            <h3><a class="h3" href="/blog/${bean.id}">${bean.title}</a></h3>
                            <div>
                                <img class="heads" src="${bean.user.heads}" alt=""/>

                                <span><a href="/blog/list?userId=${bean.user.id}">${bean.user.nick}</a>
                                    <fm:formatDate type="date" dateStyle="long" value="${bean.createdAt}" /></span>
                            </div>
                            <p class="h4">${bean.summary}</p>
                            <a href="/blog/${bean.id}" class="btn link-btn red-btn fl-r">全文阅读</a>
                        </li>
                    </c:forEach>
                </c:if>
            </ul>
        </div>
    </div>
</div>
<script src="/source/blog/javascript/public.js"></script>
<script>
    MBlog.init();
</script>
</body>
</html>
