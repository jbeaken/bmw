package org.bookmarks.website.domain;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.TypeDef;
import org.jasypt.hibernate5.type.EncryptedStringType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@TypeDef(
    name="encryptedString",
    typeClass=EncryptedStringType.class,
    parameters= {
        @Parameter(name="encryptorRegisteredName", value="strongHibernateStringEncryptor")
    }
)
@Entity
@JsonIgnoreProperties(value = { "hasBeenConsumed", "totalPrice", "customer" })
public class OrderLine extends AbstractEntity {

    @NotNull private String webReference;

	@ManyToOne
	@NotNull private StockItem stockItem;

	@NotNull private Integer quantity;

	@NotNull private Boolean isSecondHand = false;

	@Min(value=0)
	@NotNull private BigDecimal sellPrice;

	@Min(value=0) private BigDecimal postage; //Shouldn't really be here but customerorder is not sent over the pipe

	@ManyToOne
	@JoinColumn(name="customer_id", nullable=false)
//	@JsonIgnore
	private Customer customer;

	//ACCESSORS
	public BigDecimal getSellPrice() {
		return sellPrice;
	}
	public void setSellPrice(BigDecimal sellPrice) {
		this.sellPrice = sellPrice;
	}
	public StockItem getStockItem() {
		return stockItem;
	}
	public void setStockItem(StockItem stockItem) {
		this.stockItem = stockItem;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Customer getCustomer() {
		return customer;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	//METHODS
	public BigDecimal getTotalPrice() {
		return stockItem.getSellPrice().multiply(new BigDecimal(quantity));
	}

	public void incrementQuantity(Integer value) {
		setQuantity(getQuantity() + value);
	}
	public BigDecimal getPostage() {
		return postage;
	}
	public void setPostage(BigDecimal postage) {
		this.postage = postage;
	}
	public String getWebReference() {
		return webReference;
	}
	public void setWebReference(String webReference) {
		this.webReference = webReference;
	}
	public Boolean getIsSecondHand() {
		return isSecondHand;
	}
	public void setIsSecondHand(Boolean isSecondHand) {
		this.isSecondHand = isSecondHand;
	}
}
