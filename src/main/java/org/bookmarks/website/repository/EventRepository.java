package org.bookmarks.website.repository;

import java.util.Date;
import java.util.List;

import org.bookmarks.website.domain.Category;
import org.bookmarks.website.domain.Event;
import org.bookmarks.website.domain.StockItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

	@Query("select e from Event e where e.startDate > :startDate order by e.startDate")
	List<Event> findUpcomingEvents(@Param("startDate") Date startDate);


}
