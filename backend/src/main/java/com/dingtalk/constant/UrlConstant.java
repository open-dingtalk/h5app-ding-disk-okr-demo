package com.dingtalk.constant;

/**
 * 钉钉开放接口网关常量
 */
public class UrlConstant {

    /**
     * 获取企业内部应用access_token url
     */
    public static final String GET_ACCESS_TOKEN_URL = "https://oapi.dingtalk.com/gettoken";
    
    /**
     * 获取第三方企业应用get_corp_token url
     */
    public static final String GET_CORP_TOKEN_URL = "https://oapi.dingtalk.com/service/get_corp_token";
    
    /**
     * 通过免登授权码获取用户信息 url
     */
    public static final String GET_USER_INFO_URL = "https://oapi.dingtalk.com/user/getuserinfo";
    
    /**
     * 获取第三方企业应用签名ticket url
     */
    public static final String GET_JSAPI_TICKET_URL = "https://oapi.dingtalk.com/get_jsapi_ticket";
    
    /**
     * 获取企业下的自定义空间get_custom_space url
     */
    public static final String GET_CUSTOM_SPACE_URL = "https://oapi.dingtalk.com/cspace/get_custom_space";
    
    /**
     * 授权用户访问企业的自定义空间grant_custom_space url
     */
    public static final String GRANT_CUSTOM_SPACE_URL = "https://oapi.dingtalk.com/cspace/grant_custom_space";
    
}
