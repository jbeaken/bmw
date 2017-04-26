package org.bookmarks.website.repository;

import java.util.List;

import org.bookmarks.website.domain.ReadingList;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ReadingListRepository extends JpaRepository<ReadingList, Long>, PagingAndSortingRepository<ReadingList, Long> {

	@Query("select new ReadingList(rl.id, rl.name) from ReadingList rl where rl.isOnWebsite = true order by rl.name")
	List<ReadingList> findForSidebar();
}
