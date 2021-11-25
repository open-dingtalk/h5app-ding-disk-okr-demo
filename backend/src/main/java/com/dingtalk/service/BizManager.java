package com.dingtalk.service;

import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiCspaceGetCustomSpaceRequest;
import com.dingtalk.api.request.OapiCspaceGrantCustomSpaceRequest;
import com.dingtalk.api.request.OapiGetJsapiTicketRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiServiceGetCorpTokenRequest;
import com.dingtalk.api.request.OapiUserGetuserinfoRequest;
import com.dingtalk.api.response.OapiCspaceGetCustomSpaceResponse;
import com.dingtalk.api.response.OapiCspaceGrantCustomSpaceResponse;
import com.dingtalk.api.response.OapiGetJsapiTicketResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiServiceGetCorpTokenResponse;
import com.dingtalk.api.response.OapiUserGetuserinfoResponse;
import com.dingtalk.constant.UrlConstant;
import com.taobao.api.ApiException;

/**
 * 钉钉-->钉盘service
 */
@Service
public class BizManager {
    
    /**
     * 获取企业内部access_token
     * @return
     * @throws ApiException
     */
    public String getAccessToken(String appKey, String appSecret) throws ApiException {
    	DingTalkClient client = new DefaultDingTalkClient(UrlConstant.GET_ACCESS_TOKEN_URL);
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(appKey);
        request.setAppsecret(appSecret);
        request.setHttpMethod("GET");
        OapiGettokenResponse response = client.execute(request);
        
        String accessToken = response.getAccessToken();
        System.out.println("getAccessToken accessToken: " + accessToken);
        return accessToken;
    }
    
    /**
     * 根据授权码, token获取当前用户ID
     * @param authCode
     * @param accessToken
     * @return
     * @throws ApiException
     */
    public String getUserId(String authCode, String accessToken) throws ApiException {
    	System.out.println("getUserId authCode: " + authCode + ", accessToken: " + accessToken);
        // 获取用户userid
        DingTalkClient userClient = new DefaultDingTalkClient(UrlConstant.GET_USER_INFO_URL);
        OapiUserGetuserinfoRequest getuserinfoRequest = new OapiUserGetuserinfoRequest();
        getuserinfoRequest.setCode(authCode);
        getuserinfoRequest.setHttpMethod("GET");
        
        System.out.println("getUserId ------begin------");
        OapiUserGetuserinfoResponse getuserinfoResponse = userClient.execute(getuserinfoRequest, accessToken);
        System.out.println("getUserId ------end------" + JSON.toJSONString(getuserinfoResponse));
        
        // 从body里面获取用户信息
        String userId = getuserinfoResponse.getUserid();
        System.out.println("getuserinfoResponse.getUserid() userid: " + userId);
        if (userId == null) {
        	String body = getuserinfoResponse.getBody();
        	System.out.println("getUserId body: " + body);
            if (body != null) {
            	JSONObject bodyJson = JSON.parseObject(body);
                userId = bodyJson.getString("userid");
            }
        }
        System.out.println("getUserId userid: " + userId);
        return userId;
    }
    
    public String getTicket(String accessToken) throws ApiException {
    	DefaultDingTalkClient client = new DefaultDingTalkClient(UrlConstant.GET_JSAPI_TICKET_URL);
        OapiGetJsapiTicketRequest req = new OapiGetJsapiTicketRequest();
        req.setTopHttpMethod("GET");
        OapiGetJsapiTicketResponse execute = client.execute(req, accessToken);
        String ticket = execute.getTicket();
        System.out.println("getTicket ticket: " + ticket);
        return ticket;
    }
    
    public String getMessageDigestSign(String jsTicket, String nonceStr, Long timeStamp, String url) throws Exception {
    	String plain = "jsapi_ticket=" + jsTicket + "&noncestr=" + nonceStr 
    			+ "&timestamp=" + String.valueOf(timeStamp) + "&url=" + decodeUrl(url);
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-256");
            crypt.reset();
            crypt.update(plain.getBytes("UTF-8"));
            return byteToHex(crypt.digest());
        } catch (Exception e) {
            System.out.println("获取钉钉sign异常:" + e.toString());
        }
        return null;
    }
    
    private String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
        	formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
    
    private static String decodeUrl(String url) throws Exception {
        URL urler = new URL(url);
        StringBuilder urlBuffer = new StringBuilder();
        urlBuffer.append(urler.getProtocol());
        urlBuffer.append(":");
        if (urler.getAuthority() != null && urler.getAuthority().length() > 0) {
            urlBuffer.append("//");
            urlBuffer.append(urler.getAuthority());
        }
        if (urler.getPath() != null) {
            urlBuffer.append(urler.getPath());
        }
        if (urler.getQuery() != null) {
            urlBuffer.append('?');
            urlBuffer.append(URLDecoder.decode(urler.getQuery(), "utf-8"));
        }
        return urlBuffer.toString();
    }
    
    public static String getRandomStr(int count) {
        String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < count; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 获取第三方企业应用get_corp_token
     * appKey: 如果是第三方企业应用，输入第三方企业应用的SuiteKey，可在开发者后台的应用详情页获取。
     * appSecret: 如果是第三方企业应用，输入第三方企业应用的SuiteSecret，可在开发者后台的应用详情页获取。
     * suiteTicket: 第三方企业应用使用钉钉开放平台向应用推送的suite_ticket，请参考数据格式(biz_type=2)。
     * corpid: 第三方企业应用使用钉钉开放平台向应用推送的授权企业的corpid，请参考数据格式(biz_type=4)。请求参数(HTTP请求)
     * @return
     * @throws ApiException
     */
    public String getCorpToken(String appKey, String appSecret,
    		String corpId, String suiteTicket) throws ApiException {
    	DefaultDingTalkClient client= new DefaultDingTalkClient(UrlConstant.GET_CORP_TOKEN_URL);
    	OapiServiceGetCorpTokenRequest req= new OapiServiceGetCorpTokenRequest();
    	req.setAuthCorpid(corpId);
    	OapiServiceGetCorpTokenResponse execute= client.execute(req, appKey, appSecret, suiteTicket);
    	
    	String corpToken = execute.getAccessToken();
    	System.out.println("getCorpToken corpToken: " + corpToken);
    	return corpToken;
    }
    
    /**
     * 获取企业下的自定义空间
     * access_token: 第三方企业应用可通过调用get_corp_token接口获得。
     * domain: 企业调用时传入，需要为10个字节以内的字符串，仅可包含字母和数字，大小写不敏感。
     * agent_id: 第三方企业应用可以调用获取企业授权信息接口获取。
     * @return
     * @throws ApiException
     */
    public String getCusSpace(String token, String dingAgentId, String dingDomain) throws ApiException {
    	DingTalkClient client = new DefaultDingTalkClient(UrlConstant.GET_CUSTOM_SPACE_URL);
    	OapiCspaceGetCustomSpaceRequest req = new OapiCspaceGetCustomSpaceRequest();
    	req.setAgentId(dingAgentId);
    	req.setDomain(dingDomain);
    	req.setHttpMethod("GET");
    	OapiCspaceGetCustomSpaceResponse rsp = client.execute(req, token);
    	
    	String spaceid = rsp.getSpaceid();
    	System.out.println("getCusSpace spaceid: " + spaceid);
    	return spaceid;
    }
    
    /**
     * 授权用户访问企业的自定义空间
     * @return
     * @throws ApiException
     */
    public String getGrantCusSpace(String token, String dingAgentId, String grantType, 
    		String dingUserId, String fileIds, String dingDomain) throws ApiException {
    	DingTalkClient client = new DefaultDingTalkClient(UrlConstant.GRANT_CUSTOM_SPACE_URL);
    	OapiCspaceGrantCustomSpaceRequest req = new OapiCspaceGrantCustomSpaceRequest();
    	req.setAgentId(dingAgentId);
    	req.setDomain(dingDomain); // 企业内部调用时传入，授权访问该domain的自定义空间。该值来自获取企业下的自定义空间接口参数
    	req.setType(grantType); // 权限类型：add：上传, download：下载, delete：删除
    	req.setUserid(dingUserId); // 授权的企业用户userid。2227162436785834
    	req.setPath("/"); // 授权访问的路径，如授权访问所有文件传“/”。例如授权访问/doc文件夹传“/doc/” ，需要使用utf-8 urlEncode。type为add时必须传递。
    	req.setFileids(fileIds); // 授权访问的文件ID列表，多个文件之间用英文逗号隔开，如“fileId1,fileId2”。type为download时必须传递。
    	req.setDuration(30L); // 权限有效时间，有效范围为0~3600秒，超出此范围或不传默认为30秒。
    	req.setHttpMethod("GET");
    	OapiCspaceGrantCustomSpaceResponse rsp = client.execute(req, token);
    	
    	String body = rsp.getBody();
    	System.out.println("getGrantCusSpace body: " + body);
    	return body;
    }
    
}
