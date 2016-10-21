/**
 * Created by wm on 16/10/20.
 */

var uploadImage = function (event, _this) {
    var form = _this.parent("form");
    var formData = new FormData(form[0]);
    var res;
    $.ajax({
        type: 'POST',
        url: '/image/upload',
        dataType: 'json',
        contentType: false,
        processData: false,
        data: formData
    }).done(function (res) {
        res = res;
    });
    setTimeout(function () {
        return res;
    }, 10)
}

