package org.bookmarks.website.domain;


import javax.persistence.Embeddable;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Email;

@Embeddable
public class ContactDetails implements java.io.Serializable {

	@Email
	@NotBlank
	@Type(type="encryptedString")
	private String email;

//	@Transient @Email private String confirmEmail;

	@Type(type="encryptedString")
	private String workNumber;

	@Type(type="encryptedString")
	private String homeNumber;

	@Type(type="encryptedString")
	private String mobileNumber;

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobile) {
		this.mobileNumber = mobile;
	}

	public String getWorkNumber() {
		return workNumber;
	}

	public void setWorkNumber(String work) {
		this.workNumber = work;
	}

	public String getHomeNumber() {
		return homeNumber;
	}

	public void setHomeNumber(String home) {
		this.homeNumber = home;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
