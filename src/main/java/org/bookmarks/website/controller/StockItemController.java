package org.bookmarks.website.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.Sort;
import org.apache.lucene.util.Version;
import org.bookmarks.website.domain.Author;
import org.bookmarks.website.domain.Availability;
import org.bookmarks.website.domain.Binding;
import org.bookmarks.website.domain.Category;
import org.bookmarks.website.domain.Publisher;
import org.bookmarks.website.domain.ReadingList;
import org.bookmarks.website.domain.StockItem;
import org.bookmarks.website.domain.StockItemType;
import org.bookmarks.website.repository.AuthorRepository;
import org.bookmarks.website.repository.CategoryRepository;
import org.bookmarks.website.repository.StockItemRepository;
import org.bookmarks.website.search.StockItemSearchBean;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/stockItem")
public class StockItemController extends AbstractBookmarksWebsiteController {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private StockItemRepository stockItemRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ServletContext context;

	private final CharArraySet stopWords = new StandardAnalyzer().getStopwordSet();

	final static Logger logger = LoggerFactory.getLogger(StockItemController.class);

	/**
	 * Get the stock item, return json, used by sw
	 */
	@RequestMapping(value = "/getJson/{isbn}", method = RequestMethod.GET)
	public @ResponseBody StockItem getJson(@PathVariable("isbn") Long isbn, ModelMap modelMap,
			HttpServletResponse response) {

		logger.info(isbn + "");
		StockItem stockItem = stockItemRepository.findByISBN(isbn);
		List<String> result = new ArrayList<String>();
		result.add(stockItem.getTitle());
		logger.info(stockItem.toString());
		return stockItem;
	}

	@RequestMapping(value = "/searchIndex")
	public String searchIndex(StockItemSearchBean searchBean, ModelMap map) {
		if (searchBean.getQ() == null) {
			return "redirect:/";
		}

		String q = searchBean.getQ().trim();

		if (q.isEmpty()) {
			return "redirect:/";
		}

		// Is this an ISBN lookup?
		if (q.length() == 13) { // TODO Not handling isbn 10 yet
			try {
				Long isbnAsNumber = Long.parseLong(q);

				StockItem stockItem = stockItemRepository.findByISBN(isbnAsNumber);

				List<StockItem> stockItems = new ArrayList<StockItem>(1);

				if (stockItem == null) {// No result
					searchBean.setCount(0l);
				} else {
					stockItems.add(stockItem);
					searchBean.setResults(stockItems);
					searchBean.setCount(1l);
				}

				buildModelForIndexSearch(map, searchBean, q);
				return "/public/search";

			} catch (NumberFormatException e) {
				// Nope, just do regular search
			}
		}

		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);

		QueryBuilder qb = fullTextEntityManager.getSearchFactory().buildQueryBuilder().forEntity(StockItem.class).get();

		final BooleanJunction<BooleanJunction> bool = qb.bool();

		
		Query local = qb.simpleQueryString().onFields("title", "authors.name").withAndAsDefaultOperator().matching( q ).createQuery();
		
		// wrap Lucene query in a javax.persistence.Query
		org.hibernate.search.jpa.FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(local,	StockItem.class);

		// Sort by score, then sales
		SortField scoreSortField = new SortField(null, SortField.Type.SCORE, false);
		SortField salesLastYearSortField = new SortField("salesLastYear", SortField.Type.INT, true);
		org.apache.lucene.search.Sort sort = new Sort(scoreSortField, salesLastYearSortField);

		fullTextQuery.setMaxResults(searchBean.getNoOfResults());
		fullTextQuery.setFirstResult(searchBean.getCurrentPage() * searchBean.getNoOfResults());

		fullTextQuery.setSort(sort);

		fullTextQuery.setProjection("id", "title", "sellPrice", "reviewShort", "type", "imageUrl", "noOfPages",
				"dimensions", "publishedDate", "isbn", "availability", "binding", "publisher.name", "publisher.id",
				"imageFilename", "quantityInStock", "salesLastYear", "postage", "alwaysInStock", "gardnersStockLevel",
				"isAvailableAtSuppliers");
		List results = fullTextQuery.getResultList();

		List<StockItem> stockItems = new ArrayList<StockItem>(10);

		for (Object r : results) {
			Object[] result = (Object[]) r;
			Long id = (Long) result[0];
			String title = (String) result[1];
			BigDecimal sellPrice = (BigDecimal) result[2];
			String reviewShort = (String) result[3];
			StockItemType type = (StockItemType) result[4];
			String imgUrl = (String) result[5];
			Integer noOfPages = (Integer) result[6];
			String dimensions = (String) result[7];
			Date publishedDate = (Date) result[8];
			String isbn = (String) result[9];
			Availability availability = (Availability) result[10];
			Binding binding = (Binding) result[11];
			String publisherName = (String) result[12];
			Long publisherId = (Long) result[13];
			String imageFilename = (String) result[14];
			Long quantityInStock = (Long) result[15];
			Integer salesLastYear = (Integer) result[16];
			BigDecimal postage = (BigDecimal) result[17];
			Boolean alwaysInStock = (Boolean) result[18];
			Long gardnersStockLevel = (Long) result[19];
			Boolean isAvailableAtSuppliers = (Boolean) result[20];

			StockItem stockItem = new StockItem();
			stockItem.setId(id);
			stockItem.setTitle(title);
			stockItem.setSellPrice(sellPrice);
			stockItem.setReviewShort(reviewShort);
			stockItem.setType(type);
			stockItem.setImageURL(imgUrl);
			stockItem.setImageFilename(imageFilename);
			stockItem.setNoOfPages(noOfPages);
			stockItem.setDimensions(dimensions);
			stockItem.setPublishedDate(publishedDate);
			stockItem.setIsbn(isbn);
			stockItem.setAvailability(availability);
			stockItem.setBinding(binding);
			stockItem.setQuantityInStock(quantityInStock);
			stockItem.setSalesLastYear(salesLastYear);
			stockItem.setPostage(postage);
			stockItem.setAlwaysInStock(alwaysInStock);
			stockItem.setGardnersStockLevel(gardnersStockLevel);
			stockItem.setIsAvailableAtSuppliers(isAvailableAtSuppliers);

			Publisher publisher = new Publisher();
			publisher.setId(publisherId);
			publisher.setName(publisherName);

			stockItem.setPublisher(publisher);

			// List<Author> authors = authorRepository.getForStockItem(id);
			// stockItem.setAuthors(new HashSet<Author>(authors));

			// List<Category> categories = categoryRepository.getForStockItem(id);
			// stockItem.setCategories(new HashSet<Category>(categories));

			stockItems.add(stockItem);
		}

		long count = fullTextQuery.getResultSize();

		// Get authors
		populateAuthors(stockItems);

		// Fill search bean
		searchBean.setResults(stockItems);
		searchBean.setCount(count);

		map.addAttribute("searchTitle", q);

		buildModelForIndexSearch(map, searchBean, q);

		em.close();

		return "/public/search";
	}

	private void buildModelForIndexSearch(ModelMap map, StockItemSearchBean searchBean, String q) {
		map.addAttribute("searchBean", searchBean);
		map.addAttribute("q", q);
		map.addAttribute("searchTitle", q);
		map.addAttribute("pageUrl", "searchIndex?q=" + q);
	}

	@RequestMapping(value = "/searchByAuthor", method = RequestMethod.GET)
	public String searchByAuthor(StockItemSearchBean searchBean, ModelMap map) {
		Author author = searchBean.getAuthor();
		Pageable pageable = searchBean.getPageable();

		if (author == null || author.getId() == null) {
			// bad bot
			return "error";
		}

		List<StockItem> stockItems = stockItemRepository.findByAuthor(author.getId(), pageable);
		Long count = stockItemRepository.countByAuthor(author.getId());

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

//		map.addAttribute("searchBean", searchBean);
		map.addAttribute("searchTitle", author.getName());
		map.addAttribute("pageUrl", "searchByAuthor?author.id=" + author.getId() + "&author.name=" + author.getName());

		return "/public/search";
	}

	@RequestMapping(value = "/searchById", method = RequestMethod.GET)
	public String searchById(StockItemSearchBean searchBean, ModelMap map) {
		Pageable pageable = searchBean.getPageable();

		List<StockItem> stockItems = stockItemRepository.findById(pageable);
		Long count = 100l;

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

//		map.addAttribute("searchBean", searchBean);
		map.addAttribute("searchTitle", "New Arrivals");
		map.addAttribute("pageUrl", "searchById?");

		return "/public/search";
	}

	@RequestMapping(value = "/searchByPublisher", method = RequestMethod.GET)
	public String searchByPublisher(StockItemSearchBean searchBean, ModelMap map) {
		Publisher publisher = searchBean.getPublisher();

		if (publisher == null) {
			// Is this a bot?
			logger.warn("searchByPublisher publisher is null, bot??");
			return "/public/search";
		}

		Pageable pageable = searchBean.getPageable();

		List<StockItem> stockItems = stockItemRepository.findByPublisher(publisher.getId(), pageable);
		if (stockItems.isEmpty()) {
			// What's going on?
			return "/public/search";
		}
		Long count = stockItemRepository.countByPublisher(publisher.getId());

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

		map.addAttribute("searchTitle", stockItems.get(0).getPublisher().getName());
		map.addAttribute("pageUrl",
				"searchByPublisher?publisher.id=" + publisher.getId() + "&publisher.name=" + publisher.getName());

		return "/public/search";
	}

	@RequestMapping(value = "/searchByPublisherOrderByPublishedDate", method = RequestMethod.GET)
	public String searchByPublisherOrderByPublishedDate(StockItemSearchBean searchBean, ModelMap map) {
		Publisher publisher = searchBean.getPublisher();

		Pageable pageable = searchBean.getPageable();

		Long count = null;
		List<StockItem> stockItems = null;

		if (publisher.getId().equals(725l)) {
			// bookmarks, therefore include redwords
			List<Long> ids = new ArrayList<Long>();
			ids.add(725l);
			ids.add(729l);
			stockItems = stockItemRepository.findByMultiplePublisherOrderByPublishedDate(ids, pageable);
			count = stockItemRepository.countByMultiplePublisher(ids);
		} else {
			stockItems = stockItemRepository.findByPublisherOrderByPublishedDate(publisher.getId(), pageable);
			count = stockItemRepository.countByPublisher(publisher.getId());
		}

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

		map.addAttribute("searchBean", searchBean);
		map.addAttribute("searchTitle", stockItems.get(0).getPublisher().getName());
		map.addAttribute("publisherId", publisher.getId());
		map.addAttribute("pageUrl", "searchByPublisherOrderByPublishedDate?publisher.id=" + publisher.getId()
				+ "&publisher.name=" + publisher.getName());

		return "/public/search";
	}

	@RequestMapping(value = "/searchByCategory", method = RequestMethod.GET)
	public String searchByCategory(StockItemSearchBean searchBean, ModelMap map) {
		long start = System.currentTimeMillis();
		Category category = searchBean.getCategory();

		List<StockItem> stockItems = null;
		Long count = null;
		Pageable pageable = searchBean.getPageable();

		// Is this a parent category
		Map<Long, Category> parentCategories = (Map<Long, Category>) context.getAttribute("parentCategoryMap");

		if (parentCategories == null) {
			// Why?
			return "404";
		}

		if (parentCategories.get(category.getId()) != null) {
			// it is a parent category
			stockItems = stockItemRepository.findByParentCategory(category.getId(), pageable);
			count = stockItemRepository.countByParentCategory(category.getId());
		} else {
			stockItems = stockItemRepository.findByCategory(category.getId(), pageable);

			count = stockItemRepository.countByCategory(category.getId());
		}

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

		long end = System.currentTimeMillis();
		double time = (end - start);
		logger.debug("Search with n+1 : " + time);

		map.addAttribute("searchTitle", category.getName());
		map.addAttribute("pageUrl", "searchByCategory?category.id=" + category.getId() + "&category.name=" + category.getName());

		return "/public/search";
	}

	private void populateAuthors(List<StockItem> stockItems) {
		for (StockItem si : stockItems) {
			List<Author> authors = authorRepository.getForStockItem(si.getId());
			si.setAuthors(new HashSet<Author>(authors));
		}
	}

	@RequestMapping(value = "/searchByCategory2", method = RequestMethod.GET)
	public String searchByCategory2(StockItemSearchBean searchBean, ModelMap map) {
		long start = System.currentTimeMillis();
		Category category = searchBean.getCategory();

		Pageable pageable = searchBean.getPageable();

		List<StockItem> stockItems = stockItemRepository.findByCategory2(category.getId(), pageable);
		Long count = stockItemRepository.countByCategory(category.getId());

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

		long end = System.currentTimeMillis();
		double time = (end - start);
		logger.debug("Search eager fetch : " + time);

		map.addAttribute("pageUrl", "searchByCategory2?category.id=" + category.getId() + "&category.name=" + category.getName());

		return "/public/search";
	}

	@RequestMapping(value = "/searchByEbooks", method = RequestMethod.GET)
	public String searchByEbooks(StockItemSearchBean searchBean, ModelMap map) {

		Pageable pageable = searchBean.getPageable();

		List<StockItem> stockItems = stockItemRepository.findEbooks(pageable);
		Long count = stockItemRepository.countByEbook();

		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

		map.addAttribute("searchTitle", "Ebooks");
		map.addAttribute("pageUrl", "searchByEbooks?");

		return "/public/search";
	}

	@RequestMapping(value = "/showXmasCards", method = RequestMethod.GET)
	public String showXmasCards(StockItemSearchBean searchBean, ModelMap map) {

		List<StockItem> stockItems = stockItemRepository.findXmasCards();
		Integer count = stockItems.size();

		map.addAttribute("bouncies", stockItems);
		map.addAttribute("count", count);

		return "/public/bouncies";
	}

	@RequestMapping(value = "/searchByType", method = RequestMethod.GET)
	public String searchByType(StockItemSearchBean searchBean, ModelMap map) {
		StockItemType type = searchBean.getType();

		Pageable pageable = searchBean.getPageable();

		List<StockItem> stockItems = stockItemRepository.findByType(type, pageable);
		Long count = stockItemRepository.countByType(type);

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

//		map.addAttribute("searchBean", searchBean);
		map.addAttribute("searchTitle", type.getDisplayName());
		map.addAttribute("pageUrl", "searchByType?type=" + type + "&category.name=" + type.getDisplayName());

		return "/public/search";
	}

	@RequestMapping(value = "/searchByType2", method = RequestMethod.GET)
	public String searchByType2(StockItemSearchBean searchBean, ModelMap map) {
		StockItemType type = searchBean.getType();

		Pageable pageable = searchBean.getPageable();

		List<StockItem> stockItems = stockItemRepository.findByType(type, pageable);
		Long count = stockItemRepository.countByType(type);

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

//		map.addAttribute("searchBean", searchBean);
		map.addAttribute("searchTitle", type.getDisplayName());
		map.addAttribute("pageUrl", "searchByType?type=" + type + "&category.name=" + type.getDisplayName());

		return "/public/search";
	}

	@RequestMapping(value = "/searchByNewReleases", method = RequestMethod.GET)
	public String searchByNewReleases(StockItemSearchBean searchBean, ModelMap map) {
		Pageable pageable = searchBean.getPageable();

		List<StockItem> stockItems = stockItemRepository.findByNewReleases(pageable);
		Long count = 50l;

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

//		map.addAttribute("searchBean", searchBean);
		map.addAttribute("searchTitle", "New Releases");
		map.addAttribute("pageUrl", "searchByNewReleases?");

		return "/public/search";
	}

	@RequestMapping(value = "/searchByReadingList", method = RequestMethod.GET)
	public String searchByReadingList(StockItemSearchBean searchBean, ModelMap map) {

		if (searchBean == null) {
			// Bot?
			logger.warn("searchByReadingList searchBean is null, bot??");
			return null;
		}

		ReadingList readingList = searchBean.getReadingList();

		Pageable pageable = searchBean.getPageable();

		List<StockItem> stockItems = stockItemRepository.findByReadingList(readingList.getId(), pageable);
		Long count = stockItemRepository.countByReadingList(readingList.getId());

		// Get authors
		populateAuthors(stockItems);

		searchBean.setResults(stockItems);
		searchBean.setCount(count);

		map.addAttribute("searchBean", searchBean);
		map.addAttribute("searchTitle", "Reading List");
		map.addAttribute("pageUrl", "searchByReadingList?readingList.id=" + readingList.getId() + "&readingList.name="
				+ readingList.getName());

		return "/public/search";
	}
}
