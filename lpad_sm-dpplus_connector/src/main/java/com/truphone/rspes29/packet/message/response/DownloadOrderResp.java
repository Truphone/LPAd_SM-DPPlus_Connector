package com.truphone.rspes29.packet.message.response;

import com.truphone.rspes29.packet.message.response.base.ResponseMsgBody;

public class DownloadOrderResp extends ResponseMsgBody {
	private String iccid;                 

	public String getIccid() {
		return iccid;
	}

	public void setIccid(String iccid) {
		this.iccid = iccid;
	}
	

}
