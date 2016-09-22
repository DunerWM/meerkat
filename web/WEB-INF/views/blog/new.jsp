<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <link rel="stylesheet" href="/source/blog/css/reset.css"/>
    <link rel="stylesheet" href="http://cdn.bootcss.com/bootstrap/3.3.0/css/bootstrap.min.css">
    <link rel="stylesheet" href="/source/common/css/wangEditor.min.css">
    <link rel="stylesheet" href="/source/blog/css/public.css"/>
    <title>写文章——狐獴meerkat.wiki</title>
</head>
<body class="blog">
<div class="container">
    <div class="blog-container">
        <div class="blog-admin">
            <div class="form-horizontal clear-f">
                <div class="input-group">
                    <span class="input-group-addon">标题</span>
                    <input type="text" class="form-control" name="title">
                </div>
                <div class="input-group">
                    <span class="input-group-addon">摘要</span>
                    <input type="text" class="form-control" name="summary">
                </div>
                <div id="editor-container">
                    <div id="editor-trigger" style="height: 400px;"><p>请输入内容</p></div>
                </div>
                <div class="btn-group fl-r">
                    <button type="button" id="submit" class="btn btn-primary">发布</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="/source/common/js/lib/jquery-1.10.2.min.js"></script>
<script src="/source/common/js/wangEditor.js"></script>
<script type="text/javascript">
    // 阻止输出log
    wangEditor.config.printLog = true;
    var editor = new wangEditor('editor-trigger');

    // 上传图片
    editor.config.uploadImgUrl = '/image/upload';
    editor.config.uploadParams = {
        // token1: 'abcde',
        // token2: '12345'
    };

//    editor.config.uploadImgFns = function() {
//
//    }

    editor.config.uploadHeaders = {
        // 'Accept' : 'text/x-json'
    }
    // editor.config.uploadImgFileName = 'myFileName';

    // 隐藏网络图片
    // editor.config.hideLinkImg = true;

    // 表情显示项
    editor.config.emotionsShow = 'icon';
    editor.config.emotions = {
        'default': {
            title: '默认',
            data: '/source/common/js/emotions.data'
        }
    };
    editor.create();

    $("#submit").click(function () {
        var data = {
            "title": $("input[name=title]").val(),
            "summary": $("input[name=summary]").val(),
            "content": editor.$txt.html()
        }

        $.post("/blog/new", data, function(res) {
            if(res.success) {
                alert("发布成功！");
            }
        })

    });
</script>
</body>
</html>
