<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <link rel="stylesheet" href="/source/blog/css/reset.css"/>
    <link rel="stylesheet" href="/source/common/css/wangEditor.min.css">
    <link rel="stylesheet" href="/source/blog/css/public.css"/>
    <title>${blog.title}——狐獴meerkat.wiki</title>
</head>
<body class="blog">
<div class="container">
    <div class="blog-container">
        <img src="${blog.bannerImage}" alt="title"/>
        <div class="blog-content wangEditor-container">
            <nav>

            </nav>
            <header>
                <hgroup>
                    <h1>${blog.title}</h1>
                    <h3>${blog.summary}</h3>
                    <p>文 | ${blog.user.nick} ${blog.createdAt} </p>
                </hgroup>
            </header>
            <div class="content wangEditor-txt">${blog.content}</div>
        </div>
    </div>
</div>
<script src="/source/blog/javascript/public.js"></script>
<script>
    MBlog.init();
</script>
</body>
</html>
