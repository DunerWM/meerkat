<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <link rel="stylesheet" href="/source/blog/css/reset.css"/>
    <link rel="stylesheet" href="/source/common/css/theme.css"/>
    <title>登录——狐獴meerkat.wiki</title>
</head>
<body>
<div class="main-container theme-fresh">
    <div class="curtain pos-relative">
        <h1>XXXXXXXXXXXXXXXXX</h1>

        <div class="section section-default section-middle">
            <form>
                <div class="form-group">
                    <input name="loginName" placeholder="输入用户名" type="text">
                </div>
                <div class="form-group">
                    <input name="password" type="password" placeholder="输入密码" autocomplete="off">
                    <%--<a href="#" title="找回密码" class="fl-right theme-color find-back-psw">忘记密码?</a>--%>
                </div>
                <div class="form-group">
                    <input type="submit" value="登&nbsp;录" id="submit">
                </div>
                <input name="url" value="${url}" hidden><br>
            </form>
        </div>
        <p class="copy-right">&copy;2016 Meerkat.wiki, Duner. All rights reserved.</p>
    </div>
    <div class="error"></div>
</div>
<script src="/source/common/js/lib/jquery-1.10.2.min.js"></script>
<script>
    $("#submit").click(function (event) {
        event.preventDefault();
        $.post('/login', {
            "url": "${url}",
            "loginName": $("input[name=loginName]").val(),
            "password": $("input[name=password]").val()
        }, function (res) {
            if (res.success) {
                if (res.data.url && res.data.url.length > 0) {
                    window.location = res.data.url;
                } else {
                    window.location.href = "/index";
                }
            } else {
//                showErrorMessage(res.message);
                alert(res.message);
                if (res.message == "请修改密码") {
                    parent.location.href = "/user/update/password";
                }
            }
        })
    });
</script>
</body>
</html>
