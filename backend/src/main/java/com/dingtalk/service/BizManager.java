package com.dingtalk.service;

import com.aliyun.dingboot.common.token.ITokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiCspaceGetCustomSpaceRequest;
import com.dingtalk.api.request.OapiCspaceGrantCustomSpaceRequest;
import com.dingtalk.api.request.OapiServiceGetCorpTokenRequest;
import com.dingtalk.api.response.OapiCspaceGetCustomSpaceResponse;
import com.dingtalk.api.response.OapiCspaceGrantCustomSpaceResponse;
import com.dingtalk.api.response.OapiServiceGetCorpTokenResponse;
import com.dingtalk.config.AppConfig;
import com.dingtalk.constant.UrlConstant;
import com.taobao.api.ApiException;

/**
 * 主业务service，编写你的代码
 */
@Service
public class BizManager {

	@Autowired
	ITokenManager tokenManager;

    public String hello() {
        return "HelloWorld";
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
    public String getCorpToken() throws ApiException {
    	DefaultDingTalkClient client= new DefaultDingTalkClient(UrlConstant.GET_CORP_TOKEN_URL);
    	OapiServiceGetCorpTokenRequest req= new OapiServiceGetCorpTokenRequest();
    	req.setAuthCorpid(AppConfig.getCorpId());
    	OapiServiceGetCorpTokenResponse execute= client.execute(req, AppConfig.getAppKey(), AppConfig.getAppSecret(), "suiteTicket");
    	
    	System.out.println("get_corp_token: " + execute.getAccessToken());
    	return execute.getAccessToken();
    }
    
    /**
     * 获取企业下的自定义空间
     * access_token: 第三方企业应用可通过调用get_corp_token接口获得。
     * domain: 企业调用时传入，需要为10个字节以内的字符串，仅可包含字母和数字，大小写不敏感。
     * agent_id: 第三方企业应用可以调用获取企业授权信息接口获取。
     * @return
     * @throws ApiException
     */
    public String getCusSpace(String dingDomain, String dingAgentId) throws ApiException {
    	// 获取access_token
    	String accessToken = tokenManager.getAccessToken(AppConfig.getAppKey(), AppConfig.getAppSecret());
    	
    	DingTalkClient client = new DefaultDingTalkClient(UrlConstant.GET_CUSTOM_SPACE_URL);
    	OapiCspaceGetCustomSpaceRequest req = new OapiCspaceGetCustomSpaceRequest();
    	req.setDomain(dingDomain);
    	req.setAgentId(dingAgentId);
    	req.setHttpMethod("GET");
    	OapiCspaceGetCustomSpaceResponse rsp = client.execute(req, accessToken);
    	
    	System.out.println("getCusSpace body: " + rsp.getBody());
    	return rsp.getBody();
    }
    
    /**
     * 授权用户访问企业的自定义空间
     * @return
     * @throws ApiException
     */
    public String getGrantCusSpace(String dingAgentId, String grantType, String dingUserId,
    		String fileIds) throws ApiException {
    	// 获取access_token
		String accessToken = tokenManager.getAccessToken(AppConfig.getAppKey(), AppConfig.getAppSecret());
    	
    	DingTalkClient client = new DefaultDingTalkClient(UrlConstant.GRANT_CUSTOM_SPACE_URL);
    	OapiCspaceGrantCustomSpaceRequest req = new OapiCspaceGrantCustomSpaceRequest();
    	req.setAgentId(dingAgentId);
    	req.setDomain("aa"); // 企业内部调用时传入，授权访问该domain的自定义空间。该值来自获取企业下的自定义空间接口参数
    	req.setType(grantType); // 权限类型：add：上传, download：下载, delete：删除
    	req.setUserid(dingUserId); // 授权的企业用户userid。2227162436785834
    	req.setPath("/"); // 授权访问的路径，如授权访问所有文件传“/”。例如授权访问/doc文件夹传“/doc/” ，需要使用utf-8 urlEncode。type为add时必须传递。
    	req.setFileids(fileIds); // 授权访问的文件ID列表，多个文件之间用英文逗号隔开，如“fileId1,fileId2”。type为download时必须传递。
    	req.setDuration(30L); // 权限有效时间，有效范围为0~3600秒，超出此范围或不传默认为30秒。
    	req.setHttpMethod("GET");
    	OapiCspaceGrantCustomSpaceResponse rsp = client.execute(req, accessToken);
    	
    	System.out.println("getGrantCusSpace body: " + rsp.getBody());
    	return rsp.getBody();
    }
    
}
