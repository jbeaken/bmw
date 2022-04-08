package org.bookmarks.website.search;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public abstract class SearchBean<T> {
	private Integer currentPage = 0;
	
	private Integer noOfResults = 10;
	
	private Long count;
	
	private List<T> results;
	
	public Pageable getPageable() {
		currentPage = ((currentPage == null) ? 0 : currentPage);
		Pageable pageable = PageRequest.of(currentPage, noOfResults);
		return pageable;
	}

	public Long getTotalPages() {
		if(getCount() <= 10) return 0l;
		Long totalPages = getCount() / noOfResults + 1;
		return totalPages;
	}
	
	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Integer getNoOfResults() {
		return noOfResults;
	}

	public Long getCount() {
		return count;
	}

	public void setCount(Long count) {
		this.count = count;
	}

	public List<T> getResults() {
		return results;
	}

	public void setResults(List<T> results) {
		this.results = results;
	}

	public void setNoOfResults(Integer noOfResults) {
		this.noOfResults = noOfResults;
	}
	
	
}
