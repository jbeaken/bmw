package org.bookmarks.website.command;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

public class EventRegistrationForm {

	@Email @NotBlank private String email;
	
	private String information;
	
	@NotNull @NotBlank private String name;
	
	private String telephone;
	
	@NotNull private String eventName;
	
	public EventRegistrationForm() {
		super();
	}
	
	public EventRegistrationForm(String eventName) {
		this();
		this.eventName = eventName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getInformation() {
		return information;
	}
	public void setInformation(String information) {
		this.information = information;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
}
