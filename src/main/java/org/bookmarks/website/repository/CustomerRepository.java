package org.bookmarks.website.repository;

import java.util.List;

import org.bookmarks.website.domain.Customer;
import org.bookmarks.website.domain.StockItem;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

	List<Customer> findByHasBeenConsumed(Boolean hasBeenConsumed);

	@Query("update Customer set hasBeenConsumed = true where id = :id")
	@Modifying
	@Transactional
	void updateHasBeenConsumed(@Param("id") Long id);

}
