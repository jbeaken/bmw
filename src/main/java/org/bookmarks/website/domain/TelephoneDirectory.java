package org.bookmarks.website.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class TelephoneDirectory implements Serializable {
	private String mobileNumber;
	private String homeNumber;
	private String workNumber;
	
	public String getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public String getHomeNumber() {
		return homeNumber;
	}
	public void setHomeNumber(String homeNumber) {
		this.homeNumber = homeNumber;
	}
	public String getWorkNumber() {
		return workNumber;
	}
	public void setWorkNumber(String workNumber) {
		this.workNumber = workNumber;
	}
}
