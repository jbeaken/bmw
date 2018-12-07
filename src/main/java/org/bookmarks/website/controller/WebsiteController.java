package org.bookmarks.website.controller;

import static org.imgscalr.Scalr.resize;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.bookmarks.website.domain.Availability;
import org.bookmarks.website.domain.Category;
import org.bookmarks.website.domain.Customer;
import org.bookmarks.website.domain.Event;
import org.bookmarks.website.domain.Layout;
import org.bookmarks.website.domain.OrderLine;
import org.bookmarks.website.domain.ReadingList;
import org.bookmarks.website.domain.StockItem;
import org.bookmarks.website.exception.ChipsException;
import org.bookmarks.website.repository.CategoryRepository;
import org.bookmarks.website.repository.CustomerRepository;
import org.bookmarks.website.repository.EventRepository;
import org.bookmarks.website.repository.OrderLineRepository;
import org.bookmarks.website.repository.ReadingListRepository;
import org.bookmarks.website.repository.StockItemRepository;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestMapping("/website")
@Controller
public class WebsiteController {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private Environment environment;

	@Autowired
	private StandardPBEStringEncryptor jsonEcryptor;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private ReadingListRepository readingListRepository;

	@Autowired
	private ServletContext context;

	@Autowired
	private StockItemRepository stockItemRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private OrderLineRepository orderLineRepository;

	final static Logger logger = LoggerFactory.getLogger(WebsiteController.class);

	/*
	 * Initialise,
	 */
	@PostConstruct
	public void buildLayout() {
		initialise(true, true);
	}

	@RequestMapping(value = "/evict")
	public @ResponseBody String evict(String beansSha512, String message) {

		initialise(true, false);

		return "success";
	}

	@RequestMapping(value = "/evictAll")
	public @ResponseBody String evictAll(String beansSha512, String message) {

		initialise(true, true);

		return "success";
	}

	@RequestMapping(value = "/removeConsumedCustomers")
	public @ResponseBody String removeConsumedCustomers(String beansSha512, String message) {

		logger.info("Removing consumed customers from chips...");

		List<Customer> customers = customerRepository.findByHasBeenConsumed(Boolean.TRUE);

		for (Customer c : customers) {
			customerRepository.delete(c);
		}

		String messageForBeans = "...have removed " + customers.size() + " consumed customers";

		logger.info("Message for beans : " + messageForBeans);

		return messageForBeans;
	}

	/**
	 * Beans sends a JSON representation of a stockitem for syncing with chips
	 * Can delete, insert or update
	 */
	@RequestMapping(value = "/syncStockItemFromBeansWithJson", method = RequestMethod.POST)
	public @ResponseBody String syncStockItemFromBeansWithJson(@RequestBody StockItem stockItem) {

		logger.info("In syncStockItemFromBeansWithJson stockItem : {} ", stockItem);
		logger.info("In getEbookTurnaroundUrl : {} ", stockItem.getEbookTurnaroundUrl());

		if (!stockItem.getPutOnWebsite()) { // Should it be deleted?
			logger.info("Set to remove from website");
			// Check it exists
			if (stockItemRepository.existsById(stockItem.getId())) {
				logger.info("Deleting stock item....");

				try {

					orderLineRepository.deleteForStockItem(stockItem);

					orderLineRepository.flush();

					stockItemRepository.deleteById(stockItem.getId());

					logger.info("... success!");
				} catch (Exception e) {
					logger.error("Cannot delete stockItem id " + stockItem.getId(), e);
					return "error";
				}
			}

			return "success";
		}

		logger.info("Set to put on website");

		// Remove image?
		if (!stockItem.getPutImageOnWebsite()) {
			logger.info("Removing image for " + stockItem.getIsbn());
			stockItem.setImageFilename(null);
		}

		// Availability
		if (stockItem.getPublishedDate() != null && stockItem.getPublishedDate().compareTo(new Date()) > 0) {
			stockItem.setAvailability(Availability.NOT_YET_PUBLISHED);
		}

		// Ebook (hack)
		logger.info("In getEbookTurnaroundUrl : {} ", stockItem.getEbookTurnaroundUrl());
		if (stockItem.getEbookTurnaroundUrl() != null && stockItem.getEbookTurnaroundUrl().isEmpty()) {
			stockItem.setEbookTurnaroundUrl(null);
		}

		// Category
		stockItem.setCategoryName(stockItem.getCategory().getName());

		Category category = categoryRepository.findById(stockItem.getCategory().getId()).orElse(null);
		if (category.getParent() != null) {
			stockItem.setParentCategoryId(category.getParent().getId());
		} else {
			stockItem.setParentCategoryId(stockItem.getCategory().getId());
		}

		// Review
		if (!stockItem.getPutReviewOnWebsite() || stockItem.getReviewAsHTML() == null) {
			stockItem.setReviewAsHTML(null);
			stockItem.setReviewAsText(null);
			stockItem.setReviewShort(null);
		} else {
			// Create review as short
			String reviewAsShort;
			Document doc = Jsoup.parse(stockItem.getReviewAsHTML());
			String reviewAsText = doc.text();
			if (reviewAsText.length() > 190) {
				reviewAsShort = reviewAsText.substring(0, 190);
			} else {
				reviewAsShort = reviewAsText;
			}
			stockItem.setReviewShort(reviewAsShort);
			stockItem.setReviewAsText(reviewAsText);
		}

		// Sales TODO
		// stockItem.setSalesTotal(0);
		// stockItem.setSalesLastYear(0);

		// Persist or update
		stockItemRepository.save(stockItem);

		logger.info("Success! Returning status " + HttpStatus.OK);

		return "success";
	}

	// @RequestMapping(value = "/getOrders", method = RequestMethod.POST,
	// produces = "application/json")
	// @RequestMapping(value = "/getOrders", method = RequestMethod.POST,
	// produces = MediaType.APPLICATION_JSON_VALUE)
	@RequestMapping(value = "/getOrders", method = RequestMethod.GET)
	public @ResponseBody String getOrders(String beansSha512, String message) throws JsonProcessingException {

		logger.info("Transferring customers from chips to beans....");

		List<Customer> customers = customerRepository.findByHasBeenConsumed(Boolean.FALSE);

		//Hack to prevent lazy initialisation when writing as json (e.g. customer)
		//Only need stockItem id
		for (Customer c : customers) {
			for(OrderLine ol : c.getOrders()) {
				StockItem si = new StockItem();
					si.setId( ol.getStockItem().getId() );
					ol.setStockItem( si );
			}
		}
		ObjectMapper mapper = new ObjectMapper();

		String json = mapper.writeValueAsString(customers);

		// logger.debug( json );

		String encryptedJson = jsonEcryptor.encrypt(json);

		for (Customer c : customers) {
			customerRepository.updateHasBeenConsumed(c.getId());
		}

		logger.info("...have transferred " + customers.size() + " customers from chips to beans. All done");

		return encryptedJson;
	}

	protected void initialise(boolean evictCache, boolean createBouncyImages) {
		logger.info("*************************************************");
		logger.info("Initialising layout with bouncies and events, evictCache = {}, createBouncyImages = {}", evictCache, createBouncyImages);
		if (context.getAttribute("layout") == null || evictCache == true) {

			Layout layout = new Layout();

			Calendar cal = Calendar.getInstance();

			cal.add(Calendar.DAY_OF_MONTH, -1);

			List<Event> events = eventRepository.findUpcomingEvents(cal.getTime());

			if (logger.isInfoEnabled()) {
				for (Event e : events) {
					logger.info("Adding event " + e.getName() + " to context");
				}
			}

			layout.setUpcomingEvents(events);

			List<StockItem> stockItems = stockItemRepository.getBouncies();

			List<StockItem> merchandiseStockItems = stockItemRepository.getMerchandise();

			if (createBouncyImages == true) {
				createBouncyImages(stockItems);
				createBouncyImages(merchandiseStockItems);
			}

			logger.info("Added {} bouncies to layout", stockItems.size());
			logger.info("Added {} merchandiseStockItems to layout", merchandiseStockItems.size());

			layout.setBouncies(stockItems);
			layout.setMerchandise(merchandiseStockItems);

			context.setAttribute("layout", layout);

			logger.info("All good! Layout added to application context");
			logger.info("*************************************************");
		}

		// if(context.getAttribute("sidebarCategories") != null) {
		// return;
		// }

		List<Category> parentCategories = categoryRepository.getParentCategories();
		Map<Long, Category> parentCategoryMap = new HashMap<Long, Category>();
		for (Category category : parentCategories) {
			logger.debug("Adding {} parent category to layout", category.getName());
			parentCategoryMap.put(category.getId(), category);
		}
		logger.info("Added {} parent categories to layout", parentCategories.size());
		context.setAttribute("parentCategoryMap", parentCategoryMap);

		List<Category> sidebarCategories = categoryRepository.findForSidebar();
		context.setAttribute("sidebarCategories", sidebarCategories);
		logger.info("Added {} sidebar categories to layout", sidebarCategories.size());

		List<ReadingList> sidebarReadingLists = readingListRepository.findForSidebar();
		context.setAttribute("sidebarReadingLists", sidebarReadingLists);
		logger.info("Added {} sidebar reading lists to layout", sidebarReadingLists.size());

		List<Category> categories = categoryRepository.findAllOrdered();
		context.setAttribute("categories", categories);
		logger.info("Added {} categories to layout", categories.size());

		context.setAttribute("totalNoOfCategories", categories.size());

		StockItem bookOfTheWeek = stockItemRepository.findById(33729l).orElse(null);
		context.setAttribute("bookOfTheWeek", bookOfTheWeek);
	}

	private void createBouncyImages(List<StockItem> stockItems) {

		String imageFileDirectory = environment.getProperty("imageFileDirectory");
		// Now resize and crop images
		for (StockItem si : stockItems) {

			String imageFileLocation = "/home/bookmarks/images/" + "original" + File.separator + si.getImageFilename();

			logger.info("Adding bouncy " + si.getTitle() + " to context, resizing using image " + imageFileLocation);

			BufferedImage image = null;

			try {
				File imageFile = new File(imageFileLocation);
				if (!imageFile.exists()) {
					imageFile.createNewFile();
				}

				image = ImageIO.read(imageFile);

				// 300x480 200x320
				BufferedImage resizedImage = resize(image, Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, 300);

				// 100 x 150
				float desiredRatio = 100f / 150f;
				float width = image.getWidth();
				float height = image.getHeight();
				float ratio = width / height;

				if (ratio > desiredRatio) {
					// Height is too small, lose some width
					float newWidth = (float) (height * .66);
					Integer intNewWidth = Math.round(newWidth);
					resizedImage = Scalr.crop(image, intNewWidth, image.getHeight(), Scalr.OP_ANTIALIAS);
				} else {
					// Width is too small, lose some height
					float newHeight = (float) (width / .66);
					Integer intNewHeight = Math.round(newHeight);
					if (intNewHeight > image.getHeight())
						intNewHeight = image.getHeight(); // Can occur when
															// ratio = 0.66
					resizedImage = Scalr.crop(image, image.getWidth(), intNewHeight, Scalr.OP_ANTIALIAS);
				}

				// TODO If image is too big, resize it
				// if(image.getWidth() > )
				// if filesize >>
				// this roughly halfs the size 40k - 15k
				resizedImage = Scalr.resize(resizedImage, Method.QUALITY, resizedImage.getWidth());

				File outputFile = new File("/home/bookmarks/images/bouncy" + File.separator + si.getImageFilename());
				ImageIO.write(resizedImage, "jpg", outputFile);

				image.flush(); // Free up resources
			} catch (Exception e) {
				logger.error("Cannot prepare bouncy image for " + si.getTitle());
			}
		}
	}

	@RequestMapping(value = "/buildIndex", method = RequestMethod.GET)
	public @ResponseBody String buildIndex(String beansSha512, String message) throws Exception {

		logger.info("Starting indexing....");

		FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(em);
		fullTextEntityManager.createIndexer().startAndWait();

		logger.info("....success, finished indexing");

		return "success";
	}

	/**
	 * Using json sent from beans, deserialise and update database
	 **/
	@RequestMapping(value = "/updateReadingLists")
	@Transactional(rollbackFor = ChipsException.class)
	public @ResponseBody String updateReadingLists(@RequestBody ArrayList<ReadingList> readingLists) throws ChipsException, JsonParseException, JsonMappingException, IOException {

		logger.info("About to persist " + readingLists.size() + " reading lists");

		try {

			readingListRepository.deleteAll();

			readingListRepository.flush();

			// Check existance of all stocks and images of bouncies
			for (ReadingList rl : readingLists) {
				logger.info("Saving reading list " + rl.getName());
				logger.info("With " + rl.getStockItems().size() + " stock items");
				readingListRepository.save(rl);
			}

			initialise(true, false);

		} catch (Exception e) {
			logger.error("Cannot update reading lists", e);
			return e.getMessage();
		}

		return "success";
	}

	@RequestMapping(value = "/updateEvents")
	@Transactional
	public ResponseEntity<String> updateEvents(@RequestBody ArrayList<Event> events) throws ChipsException, JsonParseException, JsonMappingException, IOException {

		logger.info("Received {} events from beans", events.size());

		for (Event e : events) {
			logger.info("Processing event " + e.getName());
			logger.debug("Show name " + e.getShowName());

			if (e.getStockItem() != null) {
				logger.info("for stockItem {} {}", e.getStockItem().getId(), e.getStockItem().getTitle());

				// check stockitem exists, sometimes have problems with stock on beans not being on chips
				boolean exists = stockItemRepository.existsById(e.getStockItem().getId());
				if (!exists) {
					logger.error("Stock item does not exist on chips!! Aborting");
					return new ResponseEntity<String>("Cannot find event stock image for " + e.getStockItem().getTitle() + ", isbn: " + e.getStockItem().getIsbn(), HttpStatus.OK);
				}
			} else {
				logger.info("Event not attached to a stockitem");
			}
		}

		eventRepository.deleteAll();

		eventRepository.flush();

		eventRepository.saveAll(events);

		// Now reset chips, no need to redo images
		initialise(true, false);

		return new ResponseEntity<String>("success", new HttpHeaders(), HttpStatus.OK);
	}

	@RequestMapping(value = "/updateChips")
	@ResponseBody
	@Transactional(rollbackFor = ChipsException.class)
	public ResponseEntity<String> updateChips(@RequestBody ArrayList<StockItem> bounciesAndStickies, HttpServletRequest request) throws ChipsException, JsonParseException, JsonMappingException, IOException {

		// String jsonBody = IOUtils.toString( request.getInputStream());

		logger.info("Have received request to update bouncies and stickies {}", bounciesAndStickies);

		// logger.info( "jsonBody" + jsonBody );

		for (StockItem si : bounciesAndStickies) {
			logger.info("id " + si.getId());
			logger.info("isbn " + si.getIsbn());
			logger.info("Image " + si.getImageFilename());
			logger.info("bouncy index :  " + si.getBouncyIndex());
			logger.info("category index : " + si.getStickyCategoryIndex());
			logger.info("type index : " + si.getStickyTypeIndex());
		}

		// Check existence of all stocks and images of bouncies
		for (StockItem si : bounciesAndStickies) {
			Integer exists = stockItemRepository.checkExists(si.getId());

			if (exists == null) {
				logger.error("Cannot find on chips : {}", si);
				return new ResponseEntity<String>("Cannot find bouncy " + si.getTitle() + ", isbn: " + si.getIsbn(), HttpStatus.OK);
			}

			if (si.getBouncyIndex() != null) {

				// Must check if image exists
				logger.error("Cannot find bouncy image for {}", si);
				File imageFile = new File(environment.getProperty("imageFileDirectory") + "original" + File.separator + si.getImageFilename());
				if (!imageFile.exists()) {
					return new ResponseEntity<String>("Cannot find bouncy stock image for " + si.getTitle() + ", isbn: " + si.getIsbn(), HttpStatus.OK);
				}
			}
		}

		// Reset all bouncies
		stockItemRepository.resetAllBounciesAndStickies();

		for (StockItem si : bounciesAndStickies) {
			stockItemRepository.updateBounciesAndStickies(si.getId(), si.getBouncyIndex(), si.getStickyCategoryIndex(), si.getStickyTypeIndex());
		}

		// Now reset chips
		initialise(true, true);

		return new ResponseEntity<String>("success", new HttpHeaders(), HttpStatus.OK);
	}

}
