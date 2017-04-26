package org.bookmarks.website.json;

import java.util.List;

import org.bookmarks.website.domain.Customer;

public class JSONCustomerHolder {
	private List<Customer> customers;

	public List<Customer> getCustomers() {
		return customers;
	}

	public void setCustomers(List<Customer> customers) {
		this.customers = customers;
	}
}
