package org.bookmarks.website.domain;

import java.io.Serializable;

import javax.persistence.Embeddable;

import org.hibernate.validator.constraints.Length;


@Embeddable
public class Review implements Serializable {
	
	@Length(max=5000)
	private String review1;
	
	@Length(max=500)
	private String review2;
	
	@Length(max=500)
	private String review3;
	public String getReview1() {
		return review1;
	}
	public void setReview1(String review1) {
		this.review1 = review1;
	}
	public String getReview2() {
		return review2;
	}
	public void setReview2(String review2) {
		this.review2 = review2;
	}
	public String getReview3() {
		return review3;
	}
	public void setReview3(String review3) {
		this.review3 = review3;
	}
	
	
	public String getReview() {
		return review1 + review2 + review3;
	}
}
