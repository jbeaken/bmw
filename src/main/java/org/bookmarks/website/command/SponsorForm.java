package org.bookmarks.website.command;


import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

public class SponsorForm  {

	@NotNull 
	@Min(0) 
	private BigDecimal amount;
	
	//@NotNull String telephone;
	
//	@NotNull @NotBlank String firstName;

//	@NotNull @NotBlank String lastName;
	
//	@Email @NotBlank String email;
	
	
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

}

