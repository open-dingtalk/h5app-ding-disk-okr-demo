package com.dingtalk.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.config.AppConfig;
import com.dingtalk.model.RpcServiceResult;
import com.dingtalk.service.BizManager;
import com.taobao.api.ApiException;

/**
 * 钉钉-->钉盘Controller
 * 
 */
@RestController
@RequestMapping("/dingding")
//@CrossOrigin(origins = "http://192.168.2.106:8080", maxAge = 3600)
public class BizController {

    @Autowired
    BizManager dizManager;
    
    /**
     * 获取钉钉的corpId、agentId
     * @return
     */
    @RequestMapping(value = "/demo/getDingConfig", method = RequestMethod.GET)
    public RpcServiceResult getConfig() {
        Map<String, Object> appConfigMap = new HashMap<String, Object>();
        appConfigMap.put("dingCorpId", AppConfig.getCorpId());
        appConfigMap.put("dingAgentId", AppConfig.getAgentId());
        return RpcServiceResult.getSuccessResult(appConfigMap);
    }
    
    /**
     * 获取签名信息
     * @return
     * @throws Exception 
     */
    @RequestMapping(value = "/demo/getSign", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public RpcServiceResult getSign(@RequestBody JSONObject json) throws Exception {
    	System.out.println("/demo/getSign json: " + json);
    	String url = json.getString("url"); // url可以根据实际场景使用
    	String authCode = json.getString("authCode"); // 授权码,前端获取传递到后端
    	if (StringUtils.isEmpty(url) || StringUtils.isEmpty(authCode)) {
            return RpcServiceResult.getFailureResult("-1", "url or authCode is not null");
        }
    	
    	String appKey = AppConfig.getAppKey();
    	String appSecret = AppConfig.getAppSecret();
    	// 获取企业内部token
    	String accessToken = dizManager.getAccessToken(appKey, appSecret);
    	System.out.println("/demo/getSign accessToken: " + accessToken);
    	// 用户免登陆之后, 需要动态去获取用户userid
    	String userid = dizManager.getUserId(authCode, accessToken);
    	System.out.println("/demo/getSign userid: " + userid);
    	
        // 获取签名字符串
        String ticket = dizManager.getTicket(accessToken);
        System.out.println("/demo/getSign ticket: " + ticket);
        String nonceStr = UUID.randomUUID().toString().substring(0, 20);
        long timeStamp = System.currentTimeMillis() / 1000;
        String signature = dizManager.getMessageDigestSign(ticket, nonceStr, timeStamp, url);
        System.out.println("/demo/getSign signature: " + signature);
        
        Map<String, Object> appConfigMap = new HashMap<String, Object>();
        appConfigMap.put("token", accessToken);
        appConfigMap.put("dingUserId", userid);
        appConfigMap.put("timeStamp",timeStamp);
        appConfigMap.put("nonceStr",nonceStr);
        appConfigMap.put("signature",signature);
        return RpcServiceResult.getSuccessResult(appConfigMap);
    }
    
    /**
     * 获取企业内部access_token
     * @return
     * @throws ApiException
     */
    @RequestMapping(value = "/demo/getAccessToken", method = RequestMethod.GET)
    public RpcServiceResult getAccessToken() throws ApiException {
    	// 获取token
    	String appKey = AppConfig.getAppKey();
    	String appSecret = AppConfig.getAppSecret();
    	String accessToken = dizManager.getAccessToken(appKey, appSecret);
        if (StringUtils.isEmpty(accessToken)) {
            return RpcServiceResult.getFailureResult("-1", "fail");
        }
        return RpcServiceResult.getSuccessResult(accessToken);
    }
    
    /**
     * 获取第三方企业应用get_corp_token
     * 注意 在使用access_token时，请注意: 
           access_token的有效期为7200秒（2小时），有效期内重复获取会返回新的access_token。
                       开发者需要缓存access_token，用于后续接口的调用。因为每个应用的access_token是彼此独立的，所以进行缓存时需要区分应用来进行存储。
                       不能频繁调用gettoken接口，否则会受到频率拦截。
     * @return
     * @throws ApiException
     */
    @RequestMapping(value = "/demo/getCorpToken", method = RequestMethod.GET)
    public RpcServiceResult getCorpToken() throws ApiException {
    	// 获取token
    	String appKey = AppConfig.getAppKey();
    	String appSecret = AppConfig.getAppSecret();
    	String corpId = AppConfig.getCorpId();
    	String corpToken = dizManager.getCorpToken(appKey, appSecret, corpId, "suiteTicket");
        if (StringUtils.isEmpty(corpToken)) {
            return RpcServiceResult.getFailureResult("-1", "fail");
        }
        return RpcServiceResult.getSuccessResult(corpToken);
    }
    
    /**
     * 获取企业下的自定义空间
     * @return
     * @throws ApiException
     */
    @RequestMapping(value = "/demo/cusSpace/getCusSpace", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public RpcServiceResult getCusSpace(@RequestBody JSONObject json) throws ApiException {
    	System.out.println("/demo/cusSpace/getCusSpace json: " + json);
    	String token = json.getString("token");
    	String dingAgentId = json.getString("dingAgentId");
    	String dingDomain = json.getString("dingDomain");
    	if (StringUtils.isEmpty(token)) {
            return RpcServiceResult.getFailureResult("-1", "token is not null");
        }
    	if (StringUtils.isEmpty(dingAgentId)) {
            return RpcServiceResult.getFailureResult("-1", "dingAgentId is not null");
        }
    	String customSpace = dizManager.getCusSpace(token, dingAgentId, dingDomain);
        if (StringUtils.isEmpty(customSpace)) {
            return RpcServiceResult.getFailureResult("-1", "fail");
        }
        return RpcServiceResult.getSuccessResult(customSpace);
    }
    
    /**
     * 授权用户访问企业的自定义空间
     * @return
     * @throws ApiException
     */
    @RequestMapping(value = "/demo/cusSpace/getGrantCusSpace", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public RpcServiceResult getGrantCusSpace(@RequestBody JSONObject json) throws ApiException {
    	System.out.println("/demo/cusSpace/getGrantCusSpace json: " + json);
    	String token = json.getString("token");
    	String dingUserId = json.getString("dingUserId");
    	String dingAgentId = json.getString("dingAgentId");
    	String fileIds = json.getString("fileIds");
    	String grantType = json.getString("grantType");
    	String dingDomain = json.getString("dingDomain");
    	if (StringUtils.isEmpty(token)) {
            return RpcServiceResult.getFailureResult("-1", "token is not null");
        }
    	if (StringUtils.isEmpty(dingAgentId)) {
            return RpcServiceResult.getFailureResult("-1", "dingAgentId is not null");
        }
    	if (StringUtils.isEmpty(dingUserId)) {
            return RpcServiceResult.getFailureResult("-1", "dingUserId is not null");
        }
    	if (StringUtils.isEmpty(dingDomain)) {
            return RpcServiceResult.getFailureResult("-1", "dingDomain is not null");
        }
    	
    	String grantCusSpace = dizManager.getGrantCusSpace(token, dingAgentId, grantType, dingUserId, fileIds, dingDomain);
        if (StringUtils.isEmpty(grantCusSpace)) {
            return RpcServiceResult.getFailureResult("-1", "fail");
        }
        return RpcServiceResult.getSuccessResult(grantCusSpace);
    }
    
}
