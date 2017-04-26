package org.bookmarks.website.domain;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

@Embeddable
public class WebsiteInfo implements java.io.Serializable {

	private Review review;
	
	private String titleOnWebsite;
	
	private String authorOnWebsite;
	
	@NotNull
	private Boolean putImageOnWebsite = Boolean.FALSE;	
	
	private Boolean isOnWebsite = Boolean.FALSE;
	
	@NotNull
	private Boolean putReviewOnWebsite = Boolean.FALSE;	
	
	@NotNull
	private Boolean putOnWebsite = Boolean.TRUE;	
	
	private Boolean imageIsOnWebsite = Boolean.FALSE;
	
	private Boolean reviewIsOnWebsite = Boolean.FALSE;

	
	public Boolean getIsOnWebsite() {
		return isOnWebsite;
	}


	public void setIsOnWebsite(Boolean isOnWebsite) {
		this.isOnWebsite = isOnWebsite;
	}


	public Boolean getImageIsOnWebsite() {
		return imageIsOnWebsite;
	}


	public void setImageIsOnWebsite(Boolean imageIsOnWebsite) {
		this.imageIsOnWebsite = imageIsOnWebsite;
	}


	public Boolean getReviewIsOnWebsite() {
		return reviewIsOnWebsite;
	}


	public void setReviewIsOnWebsite(Boolean reviewIsOnWebsite) {
		this.reviewIsOnWebsite = reviewIsOnWebsite;
	}


	public Boolean getPutReviewOnWebsite() {
		return putReviewOnWebsite;
	}


	public void setPutReviewOnWebsite(Boolean putReviewOnWebsite) {
		this.putReviewOnWebsite = putReviewOnWebsite;
	}
	

	private Integer frontPageIndex;
	
	private Boolean bookOfTheMonth;
	
	public Boolean getBookOfTheMonth() {
		return bookOfTheMonth;
	}
	
	
	public void setBookOfTheMonth(Boolean bookOfMonth) {
		this.bookOfTheMonth = bookOfMonth;
	}
	public Boolean getPutOnWebsite() {
		return putOnWebsite;
	}

	public void setPutOnWebsite(Boolean putOnWebsite) {
		this.putOnWebsite = putOnWebsite;
	}	
	public Boolean getPutImageOnWebsite() {
		return putImageOnWebsite;
	}

	public void setPutImageOnWebsite(Boolean putImageOnWebsite) {
		this.putImageOnWebsite = putImageOnWebsite;
	}	

	public Integer getFrontPageIndex() {
		return frontPageIndex;
	}

	public void setFrontPageIndex(Integer frontPageIndex) {
		this.frontPageIndex = frontPageIndex;
	}	
	
	public String getTitleOnWebsite() {
		return titleOnWebsite;
	}

	public void setTitleOnWebsite(String title) {
		this.titleOnWebsite = title;
	}


	public String getAuthorOnWebsite() {
		return authorOnWebsite;
	}

	public void setAuthorOnWebsite(String authorOnWebsite) {
		this.authorOnWebsite = authorOnWebsite;
	}

	public void setReview(Review review) {
		this.review = review;
	}
	
	public Review getReview() {
		return review;
	}	
}
