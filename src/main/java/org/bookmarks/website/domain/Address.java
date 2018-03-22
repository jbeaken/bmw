package org.bookmarks.website.domain;


import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;


@Embeddable
public class Address {

	@Type(type="encryptedString")
	@NotNull
	private @NotNull String address1;

	@Type(type="encryptedString")
	private String address2;

	@Type(type="encryptedString")
	private String address3;

	@Type(type="encryptedString")
	private @NotNull String city;

	@Type(type="encryptedString")
	@NotNull
	private String postcode;
	
	@Type(type="encryptedString")
	private @NotNull String country = "United Kingdom";

	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getAddress3() {
		return address3;
	}

	public void setAddress3(String address3) {
		this.address3 = address3;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}
}
