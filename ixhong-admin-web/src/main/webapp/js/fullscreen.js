/**
 * 获取整个body的宽度和高度
 * @returns {{width: number, height: number}}
 */
var getScreenHeightWith = function(){
    return {
        width: document.body.scrollWidth,
        height: document.body.scrollHeight
    }
}

/**
 * 初始化全局div
 */
var initDivWidhtAndHeight = function(){
    var hash = getScreenHeightWith();
    $("#fullScreen").css("width",hash.width+"px").css("height",hash.height+"px");

}

/**
 * 监听图片的事件
 */
$(".fullscreen").click(function(){

    if($(this).attr("src").indexOf("default")>0){
    }else {

        initDivWidhtAndHeight();
        $("#fullScreen").css("display","block")
        setImgPosition($(this).attr("src"));
    }
})

/**
 * 监听全屏div事件 单击取消去全屏
 */
$("#fullScreen,#fullScreenImg").click(function(){
    $('#fullScreen').css("display","none");
    $("#fullScreenImg").css("display","none");
})

/**
 * 设置图片的地址
 * @param url
 */
var setImgPosition = function(url){
    url = url || "";
    url = url.replace("x2","x8")
    if(url == $("#fullScreenImg").attr("src")){
        $("#fullScreenImg").css("display","block")
    }else {
        $("#fullScreenImg").attr("src",url);
    }
}


/**
 * 监听图片的load事件
 */
$("#fullScreenImg").load(function(){
    var left = parseInt(document.body.scrollWidth) - parseInt($(this).css('width'));
    left = parseInt(left/2)
    $("#fullScreenImg").css("left",left+"px")
    $("#fullScreenImg").css("display","block")
})
