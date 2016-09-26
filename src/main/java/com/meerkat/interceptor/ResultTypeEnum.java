package com.meerkat.interceptor;


/**
 * 返回结果的类型
 *
 * @author YY
 */
public enum ResultTypeEnum {
    smart,      //自动判断
    redirect,    //页面跳转
    structure_redirect,    //页面跳转-入口2
    html,        //返回html 结果
    json,        //返回 json 结果
    xml            //返回 xml 结果
}
