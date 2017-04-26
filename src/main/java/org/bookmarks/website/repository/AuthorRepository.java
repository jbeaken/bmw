package org.bookmarks.website.repository;

import java.util.List;
import java.util.Set;

import org.bookmarks.website.domain.Author;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface AuthorRepository extends CrudRepository<Author, Long>, PagingAndSortingRepository<Author, Long> {

	@Query("select new Author(a.id, a.name) from Author a join a.stockItems s where s.id = :id")
	List<Author> getForStockItem(@Param("id") Long id);
}
