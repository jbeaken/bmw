package org.bookmarks.website.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.search.annotations.ContainedIn;

@Entity
public class Category extends AbstractEntity {

    @NotNull
    @Size(max = 30)
    private String name;
    
    @OneToMany(cascade = CascadeType.ALL)
    private Set<Category> categories = new HashSet<Category>();
    
//    @OneToMany
//    private Set<StockItem> stockItems;

    @ManyToOne
    @JoinColumn(name="parent_id")
    private Category parent;
    
	@Column(name="is_on_website")
    private Boolean isOnWebsite = Boolean.TRUE;
    
	@Column(name="is_in_sidebar")
    private Boolean isInSidebar = Boolean.FALSE;
    
    //Constructors
    public Category() {
    	super();
    }
    
    public Category(Long id, String name) {
    	this();
    	this.name = name;
    	this.id = id;
    }

	public Boolean getIsOnWebsite() {
		return isOnWebsite;
	}

	public void setIsOnWebsite(Boolean isOnWebsite) {
		this.isOnWebsite = isOnWebsite;
	}

	public Boolean getIsInSidebar() {
		return isInSidebar;
	}

	public void setIsInSidebar(Boolean isInSidebar) {
		this.isInSidebar = isInSidebar;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public void setCategories(Set<Category> categories) {
		this.categories = categories;
	}

	public Category getParent() {
		return parent;
	}

	public void setParent(Category parent) {
		this.parent = parent;
	}
}
