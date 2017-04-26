package org.bookmarks.website.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import java.util.Date;

@MappedSuperclass
public abstract class AbstractEntity implements java.io.Serializable {
	
 	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
	protected Long id;
	    
	//@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDate")
    private Date dateCreated = new Date();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
}
