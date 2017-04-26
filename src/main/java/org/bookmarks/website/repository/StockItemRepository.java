package org.bookmarks.website.repository;

import java.math.BigDecimal;
import java.util.List;

import java.util.stream.Stream;

import org.bookmarks.website.domain.Availability;
import org.bookmarks.website.domain.Binding;
import org.bookmarks.website.domain.StockItem;
import org.bookmarks.website.domain.StockItemType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StockItemRepository extends CrudRepository<StockItem, Long>, PagingAndSortingRepository<StockItem, Long> {
	//TODO need index of binding and type?
	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si join si.authors a where a.id = :id order by si.salesLastYear desc")
	 List<StockItem> findByAuthor(@Param("id") Long id, Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.type != 'CARD' order by si.id desc")
	 List<StockItem> findByNewReleases(Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.category.id = :id order by si.stickyCategoryIndex desc, si.salesLastYear desc")
	 //Stream<StockItem> findByCategory(@Param("id") Long id, Pageable pageable);
   List<StockItem> findByCategory(@Param("id") Long id, Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.ebookTurnaroundUrl is not null order by si.stickyCategoryIndex desc, si.salesLastYear desc")
   List<StockItem> findEbooks(Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.parentCategoryId = :id order by si.stickyCategoryIndex desc, si.salesLastYear desc")
	 List<StockItem> findByParentCategory(@Param("id") Long id, Pageable pageable);

	 @Query("select si from StockItem si join fetch si.authors a where si.category.id = :id order by si.stickyCategoryIndex desc, si.salesLastYear desc")
	 List<StockItem> findByCategory2(@Param("id") Long id, Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.publisher.id = :id order by si.salesLastYear desc")
	 List<StockItem> findByPublisher(@Param("id") Long id, Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.publisher.id = :id order by si.publishedDate desc")
	 List<StockItem> findByPublisherOrderByPublishedDate(@Param("id") Long id, Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.publisher.id in :ids order by si.publishedDate desc")
	 List<StockItem> findByMultiplePublisherOrderByPublishedDate(@Param("ids") List<Long> ids, Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.binding = :binding order by si.salesLastYear desc")
	 List<StockItem> findByBinding(@Param("binding") Binding binding, Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.type = :type order by si.stickyTypeIndex desc, si.salesLastYear desc")
	 List<StockItem> findByType(@Param("type") StockItemType type, Pageable pageable);

	 @Query("select new StockItem(si.id, si.isbn, si.title, si.imageFilename, si.reviewShort, si.sellPrice, si.postage, si.availability, si.publishedDate, si.binding, si.publisher.name, si.publisher.id, si.quantityInStock, si.category.id, si.categoryName, si.type) from StockItem si where si.quantityInStock > 0 order by si.id desc")
	 List<StockItem> findById(Pageable pageable);

	 @Query("select new StockItem(si.id, si.title, si.imageFilename, si.sellPrice) from StockItem si where si.category.id = :id and si.imageFilename != null order by si.salesLastYear desc")
	 List<StockItem> findByCategoryWithImage(@Param("id") Long id, Pageable pageable);

	 @Query("select new StockItem(si.id, si.title, si.imageFilename, si.sellPrice) from StockItem si join si.authors a where a.id = :id and si.id != :stockItemId order by si.salesLastYear desc")
	 List<StockItem> findAuthorsOtherStockItems(@Param("id") Long id, @Param("stockItemId") Long stockItemId, Pageable pageable);

	 @Query("select new StockItem(si.id, si.title, si.imageFilename) from StockItem si where si.bouncyIndex is not null order by si.bouncyIndex desc")
	 List<StockItem> getBouncies();

	 @Query("select new StockItem(si.id, si.title, si.imageFilename) from StockItem si where si.type = 'CARD' and quantityInStock > 0")
	 List<StockItem> findXmasCards();


	 @Query("select new StockItem(si.id, si.title, si.imageFilename) from StockItem si where si.merchandiseIndex is not null order by si.merchandiseIndex desc")
	 List<StockItem> getMerchandise();

	 @Query("select si from ReadingList rl join rl.stockItems si join fetch si.authors a where rl.id = :id")
	 List<StockItem> findByReadingList(@Param("id") Long id, Pageable pageable);

	 //Counts
	 @Query("select count(si) from StockItem si where si.category.id = :id")
	 Long countByCategory(@Param("id") Long id);

	 @Query("select count(si) from StockItem si where si.ebookTurnaroundUrl is not null")
	 Long countByEbook();

	 @Query("select count(si) from StockItem si where si.parentCategoryId = :id")
	 Long countByParentCategory(@Param("id") Long id);

	 @Query("select count(si) from StockItem si join si.publisher p where p.id = :id")
	 Long countByPublisher(@Param("id") Long id);

	 @Query("select count(si) from StockItem si join si.publisher p where p.id in :ids")
	 Long countByMultiplePublisher(@Param("ids") List<Long> ids);

	 @Query("select count(si) from StockItem si where si.type = :type")
	 Long countByType(@Param("type") StockItemType type);

	 @Query("select count(si) from StockItem si where si.binding = :binding")
	 Long countByBinding(@Param("binding") Binding binding);

	 @Query("select count(si) from ReadingList rl join rl.stockItems si where rl.id = :id")
	 Long countByReadingList(@Param("id") Long id);

	 @Query("select count(si) from StockItem si join si.authors a where a.id = :id")
	 Long countByAuthor(@Param("id") Long id);

	 //Single
	 @Query("select si from StockItem si left join fetch si.authors a where si.isbnAsNumber = :isbnAsNumber")
	 StockItem findByISBN(@Param("isbnAsNumber") Long isbnAsNumber);

	 //Updates
	 @Modifying
	 @Transactional
	 @Query("update StockItem si set si.imageFilename = :imageFilename, si.title = :title, si.sellPrice = :sellPrice, si.availability = :availability, si.quantityInStock = :quantityInStock  where id = :id")
	 void updateStockItem(@Param("id") Long id, @Param("imageFilename") String imageFilename, @Param("title") String title, @Param("sellPrice") BigDecimal sellPrice, @Param("availability") Availability availability, @Param("quantityInStock") Long quantityInStock);

	 @Modifying
	 @Transactional
	 @Query("update StockItem si set si.bouncyIndex = null, stickyTypeIndex = null, stickyCategoryIndex = null where si.bouncyIndex is not null or si.stickyTypeIndex is not null or si.stickyCategoryIndex is not null")
	 void resetAllBounciesAndStickies();

	 @Modifying
	 @Transactional
	 @Query("update StockItem si set si.bouncyIndex = :bouncyIndex, stickyTypeIndex = :stickyTypeIndex, stickyCategoryIndex = :stickyCategoryIndex where id = :id")
	 void updateBounciesAndStickies(@Param("id") Long id, @Param("bouncyIndex") Long bouncyIndex,  @Param("stickyCategoryIndex") Long stickyCategoryIndex, @Param("stickyTypeIndex") Long stickyTypeIndex);

	 @Query("select 1 from StockItem where id = :id")
	 Integer checkExists(@Param("id") Long id);
}
