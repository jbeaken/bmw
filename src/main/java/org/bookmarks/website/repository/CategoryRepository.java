package org.bookmarks.website.repository;

import java.util.List;

import org.bookmarks.website.domain.Category;
import org.bookmarks.website.domain.StockItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends CrudRepository<Category, Long>, PagingAndSortingRepository<Category, Long> {

	@Query("select new Category(c.id, c.name) from Category c where isOnWebsite = true order by c.name")
	List<Category> findAllOrdered();
	
	@Query("select new Category(c.id, c.name) from Category c where c.isInSidebar = true order by c.name")
	List<Category> findForSidebar();

//	@Query("select new Category(c.id, c.name) from Category c join c.stockItems s where s.id = :id")
//	List<Category> getForStockItem(@Param("id") Long id);

	@Query("select c.parent from Category c where c.parent is not null")
	List<Category> getParentCategories();
}
