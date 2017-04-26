package org.bookmarks.website.repository;

import java.util.List;

import org.bookmarks.website.domain.StockItem;
import org.bookmarks.website.domain.OrderLine;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

	@Query("delete OrderLine where stockItem = ?1")
	@Modifying
	@Transactional
	void deleteForStockItem(StockItem stockItem);

}
