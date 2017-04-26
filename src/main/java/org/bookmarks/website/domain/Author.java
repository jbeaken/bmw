package org.bookmarks.website.domain;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.search.annotations.ContainedIn;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Store;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({"stockItems"})
public class Author {
	
 	@Id
 	//Has to insert using beans id, no strategy
//    @GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	    
	//@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDate")
    private Date dateCreated = new Date();		

    @NotNull
    @Size(max = 255)
    @Column(unique = true)
    @Field(index=Index.YES, store=Store.YES)
    private String name;
    
    @ContainedIn //TO-DO what's this doing here?
    @ManyToMany(mappedBy="authors")
    private Set<StockItem> stockItems;

    //Constructors
    public Author() {
    	super();
    }
    
    public Author(Long id, String name) {
    	this();
    	this.id = id;
    	this.name = name;
    }
    
    //Accessors/Modifiers
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<StockItem> getStockItems() {
		return stockItems;
	}

	public void setStockItems(Set<StockItem> stockItems) {
		this.stockItems = stockItems;
	}
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
