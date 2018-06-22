package com.truphone.rspes29.packet.message.request;

import com.truphone.rspes29.packet.message.MsgType;
import com.truphone.rspes29.packet.message.request.base.RequestMsgBody;

@MsgType("/gsma/rsp2/es2plus/releaseProfile")
public class ReleaseProfileReq extends RequestMsgBody {
	private String iccid;

	public String getIccid() {
		return iccid;
	}

	public void setIccid(String iccid) {
		this.iccid = iccid;
	}

}
