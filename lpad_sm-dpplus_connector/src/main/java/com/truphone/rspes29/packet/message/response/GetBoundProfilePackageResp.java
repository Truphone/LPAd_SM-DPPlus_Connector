package com.truphone.rspes29.packet.message.response;

import com.truphone.rspes29.packet.message.response.base.ResponseMsgBody;

public class GetBoundProfilePackageResp extends ResponseMsgBody {
	private String transactionID;
	private String boundProfilePackage;

	public String getTransactionID() {
		return transactionID;
	}

	public void setTransactionID(String transactionID) {
		this.transactionID = transactionID;
	}

	public String getBoundProfilePackage() {
		return boundProfilePackage;
	}

	public void setBoundProfilePackage(String boundProfilePackage) {
		this.boundProfilePackage = boundProfilePackage;
	}

}
