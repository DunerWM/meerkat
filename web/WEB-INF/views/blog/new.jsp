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
                <div class="input-group">
                    <form>
                        <input type="file" name="imageUpload" class="image-upload"/>
                        <input type="hidden" name="bannerImage" id="bannerImage"/>
                    </form>
                    <button type="button" class="btn btn-primary upload-image-btn"
                            style="margin-bottom: 15px; margin-left: 5px;">上传banner
                    </button>
                    <div class="row">
                        <div class="col-xs-6 col-md-3 uploaded-image">
                        </div>
                    </div>
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
<script src="/source/blog/javascript/upload-image.js"></script>
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


    editor.config.uploadHeaders = {
        // 'Accept' : 'text/x-json'
        "enctype": "multipart/form-data"
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
            "content": editor.$txt.html(),
            "bannerImage": $("#bannerImage").val()
        }

        $.post("/blog/new", data, function (res) {
            if (res.success) {
                window.location.href = "/blog/" + res.data.blogId;
            }
        })
    });

    $(".upload-image-btn").click(function () {
        $(this).parent().find("input[name=imageUpload]").click();
    });

    $("input[name=imageUpload]").change(function (event) {
        if (this.value.length > 0) {
            var form = $(this).parent("form");
            var formData = new FormData(form[0]);
            $.ajax({
                type: 'POST',
                url: '/image/upload',
                dataType: 'json',
                contentType: false,
                processData: false,
                data: formData
            }).done(function (res) {
                if (res.success) {
                    var path = res.data.paths[0];
                    drawing($(".uploaded-image"), path);
                } else {
                    errorMessage(res.message);
                }
            });
        } else {
            return false;
        }
    });

    var drawing = function (node, path) {
        var li = $("<div class='thumbnail'>");
        var operateBar = $("<div class='operate-bar'><span class='delete'>删除</span></div>");
        var img = $("<img>");
        img.attr("src", path);
        $("#bannerImage").val(path);
        img.appendTo(li);
        operateBar.appendTo(li);
        li.appendTo(node);
    }

    var errorMessage = function (message) {
        alert(message);
    }

</script>
</body>
</html>
