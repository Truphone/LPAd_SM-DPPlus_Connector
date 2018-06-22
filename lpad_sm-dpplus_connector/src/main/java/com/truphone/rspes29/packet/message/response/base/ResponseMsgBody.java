package com.truphone.rspes29.packet.message.response.base;

import com.truphone.rspes29.packet.message.MsgBody;

public abstract class ResponseMsgBody implements MsgBody {
	private HeaderResp header;

	public HeaderResp getHeader() {
		return header;
	}

	public void setHeader(HeaderResp header) {
		this.header = header;
	}

}
