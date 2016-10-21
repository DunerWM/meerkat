/**
 * Created by wm on 16/9/14.
 */

MBlog = typeof MBlog == "undefined" ? {} : MBlog;

MBlog.softColorArr = ["#32004b", "#4b0049", "#7b0046", "#7a0026", "#003471", "#1b1464", "#440e62", "#630460"];
MBlog.darkColorArr = ["#7b2e00", "#790000", "#0d004c", "#002157", "#003663", "#2e3192", "#9e0b0f", "#662d91"];

MBlog.init = function () {
}

MBlog.renderBackground = function (className) {

    var r1 = parseInt(Math.random() * 8);
    var r2 = parseInt(Math.random() * 8);
    var randomDeg = parseInt(Math.random() * 180);
    document.querySelector("." + className).style.backgroundImage = "linear-gradient(" + randomDeg + "deg," + this.softColorArr[r1] + "," + this.darkColorArr[r2] + ")";

}