<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${blog.title}</title>
</head>
<body>
<h1>${blog.title}</h1>
<p>${blog.authorName}</p>
<h3>${blog.summary}</h3>
<p>${blog.content}</p>
<script src="/source/blog/javascript/public.js"></script>
<script>
    mBlog = new MBlog;
    mBlog.init();
</script>
</body>
</html>
