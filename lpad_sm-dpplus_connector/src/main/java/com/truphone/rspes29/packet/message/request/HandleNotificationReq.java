package com.truphone.rspes29.packet.message.request;

import com.truphone.rspes29.packet.message.MsgType;
import com.truphone.rspes29.packet.message.request.base.RequestMsgBody;

@MsgType("/gsma/rsp2/es9plus/handleNotification")
public class HandleNotificationReq extends RequestMsgBody {
	private String pendingNotification;

	public String getPendingNotification() {
		return pendingNotification;
	}

	public void setPendingNotification(String pendingNotification) {
		this.pendingNotification = pendingNotification;
	}

}
