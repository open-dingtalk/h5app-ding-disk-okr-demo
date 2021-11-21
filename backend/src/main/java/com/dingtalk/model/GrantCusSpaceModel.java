package com.dingtalk.model;

public class GrantCusSpaceModel {

	private String dingUserId;
	private String dingAgentId;
	private String fileIds;
	private String grantType;
	public String getDingUserId() {
		return dingUserId;
	}
	public void setDingUserId(String dingUserId) {
		this.dingUserId = dingUserId;
	}
	public String getDingAgentId() {
		return dingAgentId;
	}
	public void setDingAgentId(String dingAgentId) {
		this.dingAgentId = dingAgentId;
	}
	public String getFileIds() {
		return fileIds;
	}
	public void setFileIds(String fileIds) {
		this.fileIds = fileIds;
	}
	public String getGrantType() {
		return grantType;
	}
	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}
    
}
