package org.bookmarks.website.controller;

import static org.imgscalr.Scalr.resize;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;

import org.bookmarks.website.domain.Availability;
import org.bookmarks.website.domain.Category;
import org.bookmarks.website.domain.Customer;
import org.bookmarks.website.domain.Event;
import org.bookmarks.website.domain.Layout;
import org.bookmarks.website.domain.ReadingList;
import org.bookmarks.website.domain.StockItem;
import org.bookmarks.website.exception.ChipsException;
import org.bookmarks.website.repository.CategoryRepository;
import org.bookmarks.website.repository.CustomerRepository;
import org.bookmarks.website.repository.OrderLineRepository;
import org.bookmarks.website.repository.EventRepository;
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
	public @ResponseBody String syncStockItemFromBeansWithJson(String jsonStockItem, String beansSha512, String message) {
		try {

			ObjectMapper mapper = new ObjectMapper();

			jsonStockItem = jsonStockItem.replaceAll("org.bookmarks.domain.", "org.bookmarks.website.domain.");

			StockItem stockItem = mapper.readValue(jsonStockItem, StockItem.class);

			if (logger.isDebugEnabled()) {
				logger.debug(jsonStockItem);
			}

			logger.info("In syncStockItemFromBeansWithJson ISBN " + stockItem.getIsbn());
			logger.info("In syncStockItemFromBeansWithJson Title : " + stockItem.getTitle());

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

			logger.info("Set to put on website " + stockItem.getIsbn());

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
			if (stockItem.getEbookTurnaroundUrl() != null && stockItem.getEbookTurnaroundUrl().isEmpty()) {
				stockItem.setEbookTurnaroundUrl(null);
			}

			// Category
			stockItem.setCategoryName(stockItem.getCategory().getName());

			// TO-DO
			// need to replicate
			// -- Update Parent category
			// /update bmw.stock_item si, bmw.category c set
			// si.parent_category_id = c.parent_id where c.id = si.category_id;
			// -- fill in parent category
			// update bmw.stock_item si set si.parent_category_id =
			// si.category_id where si.parent_category_id is null;

			Optional<Category> optional = categoryRepository.findById(stockItem.getCategory().getId());
			Category category = optional.get();

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
		} catch (Exception e) {
			logger.error("There was an error during stock item sync", e);
			return "failure";
		}

		return "success";
	}

	// private HttpStatus autheticate(String beansSha512, String message) {
	// String chipsSha512 = getSha512(message);
	// Long timeSent = Long.parseLong(message);
	// Long timeNow = new Date().getTime();
	//
	// logger.debug("time now " + timeNow);
	// logger.debug("time sent " + timeSent);
	// logger.debug("chipsSha512 "+ chipsSha512);
	// logger.debug("beansSha512 "+ beansSha512);
	// if(timeNow > (timeSent + (10 * 1000))) { //10 second window of
	// opportunity
	// logger.error("Time now is beyond the time sent");
	// return HttpStatus.CONFLICT;
	// }
	// if(timeNow < timeSent) {
	// logger.error("Time now is before the time sent");
	// return HttpStatus.CONFLICT;
	// }
	// if(!chipsSha512.equals(beansSha512)) {
	// logger.error("beans sha != chipsSha");
	// return HttpStatus.FORBIDDEN;
	// }
	// return HttpStatus.OK;
	// }

	// private String getSha512(String message) {
	// message = message + salt;
	//
	// MessageDigest mDigest = null;
	// try {
	// mDigest = MessageDigest.getInstance("SHA-512");
	// } catch (NoSuchAlgorithmException e) {
	// e.printStackTrace();
	// }
	// byte[] result = mDigest.digest(message.getBytes());
	// StringBuffer sb = new StringBuffer();
	// for (int i = 0; i < result.length; i++) {
	// sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	// }
	// return sb.toString();
	// }

	@RequestMapping(value = "/getOrders", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody String getOrders(String beansSha512, String message) throws JsonProcessingException {

		logger.info("Transferring customers from chips to beans....");

		List<Customer> customers = customerRepository.findByHasBeenConsumed(Boolean.FALSE);

		ObjectMapper mapper = new ObjectMapper();

		String json = mapper.writeValueAsString(customers);

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

			logger.info("Adding {} bouncies to layout", stockItems.size());
			logger.info("Adding {} merchandiseStockItems to layout", merchandiseStockItems.size());

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
			parentCategoryMap.put(category.getId(), category);
		}
		context.setAttribute("parentCategoryMap", parentCategoryMap);

		List<Category> sidebarCategories = categoryRepository.findForSidebar();
		context.setAttribute("sidebarCategories", sidebarCategories);

		List<ReadingList> sidebarReadingLists = readingListRepository.findForSidebar();
		context.setAttribute("sidebarReadingLists", sidebarReadingLists);

		List<Category> categories = categoryRepository.findAllOrdered();
		context.setAttribute("categories", categories);
		context.setAttribute("totalNoOfCategories", categories.size());

		Optional<StockItem> optional = stockItemRepository.findById(33729l);

		if (optional.isPresent()) {

			StockItem bookOfTheWeek = optional.get();

			context.setAttribute("bookOfTheWeek", bookOfTheWeek);
		}
	}

	private void createBouncyImages(List<StockItem> stockItems) {

		String imageFileDirectory = environment.getProperty("imageFileDirectory");
		// Now resize and crop images
		for (StockItem si : stockItems) {

			String imageFileLocation = imageFileDirectory + "original" + File.separator + si.getImageFilename();

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

				File outputFile = new File(imageFileLocation + "bouncy" + File.separator + si.getImageFilename());
				ImageIO.write(resizedImage, "jpg", outputFile);

				image.flush(); // Free up resources
			} catch (Exception e) {
				logger.error("Cannot prepare bouncy image for {}", si);
			}
		}
	}

	/*
	 * data: '{"class": "StockItem", \ "title": "title1", \ "sellPrice":
	 * "13.67", \ "isbn": "title1", \ "isbnAsNumber": "1000", \ "availability":
	 * "PUBLISHED", \ "binding": "PAPERBACK", \ "publishedDate": "1/1/2013", \
	 * "quantityInStock": "5", \ "publisherPrice": "13.22", \ "stockItemType":
	 * "BOOK", \ "authors": [{"name" : "author1"},{"name" : "author2"}], \
	 * "categories": [{"name" : "category1"},{"name" : "category2"}], \
	 * "publisher":{"id": "1"}}',
	 */

	@RequestMapping(value = "/buildIndex")
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
	public @ResponseBody String updateReadingLists(String readingListsAsJson, String beansSha512, String message) throws ChipsException, JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		readingListsAsJson = readingListsAsJson.replaceAll("org.bookmarks.domain.", "org.bookmarks.website.domain.");

		logger.info(readingListsAsJson);

		Collection<ReadingList> readingLists = mapper.readValue(readingListsAsJson, List.class);

		logger.info("About to persist " + readingLists.size() + " reading lists");

		readingListRepository.deleteAll();

		readingListRepository.flush();

		// Check existance of all stocks and images of bouncies
		for (ReadingList rl : readingLists) {
			logger.info("Saving reading list " + rl.getName());
			logger.info("With " + rl.getStockItems().size() + " stock items");
			readingListRepository.save(rl);
		}

		initialise(true, false);

		return "success";
	}

	@RequestMapping(value = "/updateEvents")

	@Transactional // (rollbackFor = ChipsException.class)
	public @ResponseBody String updateEvents(String eventsAsJson, String beansSha512, String message) throws ChipsException, JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		logger.info("Have received json " + eventsAsJson);

		eventsAsJson = eventsAsJson.replaceAll("org.bookmarks.domain.", "org.bookmarks.website.domain.");

		Collection<Event> events = mapper.readValue(eventsAsJson, List.class);

		for (Event e : events) {
			logger.info("have got event " + e.getName());
			logger.info("Show name " + e.getShowName());

			if (e.getStockItem() != null) {
				logger.info("for stockItem {} {}", e.getStockItem().getId(), e.getStockItem().getTitle());

				// check stockitem exists, sometimes have problems with stock on
				// beans not being on chips
				boolean exists = stockItemRepository.existsById(e.getStockItem().getId());
				if (!exists) {
					logger.error("Stock item does not exist on chips!! Aborting");
					return "stock item " + e.getStockItem().getTitle() + " doesn't exist on website";
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

		return "success";
	}

	@RequestMapping(value = "/updateChips")
	@ResponseBody
	@Transactional(rollbackFor = ChipsException.class)
	public ResponseEntity<String> updateChips(String bounciesAndStickiesAsJson, String beansSha512, String message) throws ChipsException, JsonParseException, JsonMappingException, IOException {

		ObjectMapper mapper = new ObjectMapper();

		bounciesAndStickiesAsJson = bounciesAndStickiesAsJson.replaceAll("org.bookmarks.domain.StockItem", "org.bookmarks.website.domain.StockItem");

		Collection<StockItem> bounciesAndStickies = mapper.readValue(bounciesAndStickiesAsJson, List.class);

		// Check have enough
		if (bounciesAndStickies.size() < 16) {
			return new ResponseEntity<String>("16 bouncies are need, only have " + bounciesAndStickies.size() + ". Add some more ", HttpStatus.SERVICE_UNAVAILABLE);
		}

		// Check existence of all stocks and images of bouncies
		for (StockItem si : bounciesAndStickies) {
			Integer exists = stockItemRepository.checkExists(si.getId());
			if (exists == null) {
				return new ResponseEntity<String>("Cannot find stock for " + si.getTitle() + ", isbn: " + si.getIsbn(), HttpStatus.SERVICE_UNAVAILABLE);
			}
			if (si.getPutImageOnWebsite() == false) {
				return new ResponseEntity<String>("This stock has put image on website unset, please edit and change : " + si.getTitle() + ", isbn: " + si.getIsbn(), HttpStatus.SERVICE_UNAVAILABLE);
			}
			if (si.getBouncyIndex() != null) {
				// Must check if image exists
				File imageFile = new File(environment.getProperty("imageFileDirectory") + "original" + File.separator + si.getImageFilename());
				if (!imageFile.exists())
					return new ResponseEntity<String>("Cannot find stock image for " + si.getTitle() + ", isbn: " + si.getIsbn(), HttpStatus.SERVICE_UNAVAILABLE);
			}
		}

		// Reset all bouncies
		stockItemRepository.resetAllBounciesAndStickies();
		for (StockItem si : bounciesAndStickies) {
			logger.info("id " + si.getId());
			logger.info("bouncy index :  " + si.getBouncyIndex());
			logger.info("category index : " + si.getStickyCategoryIndex());
			logger.info("type index : " + si.getStickyTypeIndex());
			stockItemRepository.updateBounciesAndStickies(si.getId(), si.getBouncyIndex(), si.getStickyCategoryIndex(), si.getStickyTypeIndex());
		}

		// Now reset chips
		initialise(true, true);

		return new ResponseEntity<String>("success", new HttpHeaders(), HttpStatus.OK);
	}

}
