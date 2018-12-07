package org.bookmarks.website.controller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.bookmarks.website.command.ContactForm;
import org.bookmarks.website.command.SponsorForm;
import org.bookmarks.website.domain.Author;
import org.bookmarks.website.domain.Customer;
import org.bookmarks.website.domain.CustomerOrder;
import org.bookmarks.website.domain.DeliveryType;
import org.bookmarks.website.domain.PaymentType;
import org.bookmarks.website.domain.StockItem;
import org.bookmarks.website.domain.StockItemType;
import org.bookmarks.website.repository.EventRepository;
import org.bookmarks.website.repository.StockItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RequestMapping("/")
@Controller
public class PublicController extends AbstractBookmarksWebsiteController {

	@Autowired
	private StockItemRepository stockItemRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private TemplateEngine templateEngine;

	@Autowired
	private Environment environment;

	@Autowired
	private ApplicationContext appContext;

	private static final String[] spamBlackList = { "click here", "louis vuitton" };

	private final static Logger logger = LoggerFactory.getLogger(PublicController.class);

	/**
	 * Get the stock item, add 'interesting' which is the best sellers from the
	 * stockitem category add other authors stockitems until reach a max of 5
	 * 
	 * @param stockItemId
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value = "/view/{stockItemId}/{title}", method = RequestMethod.GET)
	public String viewStockItem(@PathVariable("stockItemId") Long stockItemId, ModelMap modelMap, HttpServletResponse response) {
		StockItem stockItem = stockItemRepository.findById(stockItemId).orElse(null);

		if (stockItem == null) {
			// Might have been removed from website, a stale url
			return "public/home";
		}

		Pageable pageable = new PageRequest(0, 5);

		List<StockItem> interesting = stockItemRepository.findByCategoryWithImage(stockItem.getCategory().getId(), pageable);

		// Add authors books
		List<StockItem> authorsOtherBooks = new ArrayList<StockItem>(5);
		for (Author a : stockItem.getAuthors()) {
			pageable = new PageRequest(0, 5 - authorsOtherBooks.size());
			List<StockItem> local = stockItemRepository.findAuthorsOtherStockItems(a.getId(), stockItemId, pageable);
			authorsOtherBooks.addAll(local);
			if (authorsOtherBooks.size() > 4)
				break;
		}

		modelMap.addAttribute(stockItem);
		modelMap.addAttribute("interesting", interesting);
		modelMap.addAttribute("authorsOtherBooks", authorsOtherBooks);
		//
		// response.setContentType("text/html;charset=UTF-8");
		// response.addHeader("Content-Type", "text/html; charset=utf-8");
		// response.setCharacterEncoding("UTF-8");

		return "public/view";
	}

	/**
	 * Get the stock item, add 'interesting' which is the best sellers from the
	 * stockitem category add other authors stockitems until reach a max of 5
	 * 
	 */
	@RequestMapping(value = "/viewByIsbn/{isbn}/{title}", method = RequestMethod.GET)
	public String viewByIsbn(@PathVariable("isbn") Long isbn, ModelMap modelMap, HttpServletResponse response) {
		StockItem stockItem = stockItemRepository.findByISBN(isbn);
		if(stockItem == null) {
			//This happens, bad bot?
			return "";
		}
		return viewStockItem(stockItem.getId(), modelMap, response);
	}

	// @RequestMapping("/1917")
	// public String nineteenseventeen(ModelMap model) {
	// return
	// "redirect:https://www.kickstarter.com/projects/556239072/1917-russias-red-year-100th-anniversary-graphic-no";
	// }

	// @RequestMapping("/.well-known/pki-validation/godaddy.html")
	// public String godaddy(ModelMap model) {
	// return
	// "redirect:https://www.kickstarter.com/projects/556239072/1917-russias-red-year-100th-anniversary-graphic-no";
	// }

	@RequestMapping("/.well-known/acme-challenge/{filename}")
	public void certbot(@PathVariable("filename") String filename, HttpServletResponse response) throws java.io.IOException {

		Resource resource = appContext.getResource("file:/var/www/html/.well-known/acme-challenge/" + filename);

		InputStream is = resource.getInputStream();

		OutputStream os = response.getOutputStream();

		IOUtils.copy(is, os);
		os.flush();
		os.close();
	}	

	@RequestMapping()
	public String index(ModelMap model) {
		return "public/home";
	}

	@RequestMapping("/merchandise")
	public String merchandise(ModelMap model) {
		model.addAttribute("merchandise", "yes!");
		return "public/merchandise";
	}

	// Legacy
	@RequestMapping("/cgi/store/bookmark.cgi")
	public String legacy(ModelMap model) {
		return "redirect:/";
	}

	@RequestMapping("/emptyBasket")
	public String emptyBasket(ModelMap model) {
		return "public/emptyBasket";
	}

	@RequestMapping("/about")
	public String about(ModelMap model) {
		return "public/about";
	}

	@RequestMapping(value = "/brochure", method = RequestMethod.GET)
	public String brochure(ModelMap model) {
		return "public/brochure";
	}

	@RequestMapping("/facebook")
	public String facebook(HttpServletRequest request, ModelMap model) {
		Cookie[] cookies = request.getCookies();
		for (Cookie c : cookies) {
			logger.info(c.getName() + " : " + c.getValue());
		}
		return "public/facebook";
	}

	@RequestMapping("/events")
	public String events(ModelMap model) {
		return "/event/list";
	}

	@RequestMapping("/account")
	public String account(ModelMap model) {
		return "public/account";
	}

	@RequestMapping(value = "/shop", method = RequestMethod.GET)
	public String shop(ModelMap model) {
		return "public/shop";
	}

	// Join mailing list
	@RequestMapping(value = "joinMailingList", method = RequestMethod.GET)
	public String joinMailingList(ModelMap model) {
		model.addAttribute(new ContactForm());
		return "public/joinMailingList";
	}

	@RequestMapping(value = "/joinMailingList", method = RequestMethod.POST)
	public String joinMailingList(@Valid ContactForm contactForm, BindingResult result, ModelMap model, RedirectAttributes redirectAttributes) {
		// Ignore errors apart from email
		if (result.hasFieldErrors("email")) {
			addError(contactForm.getEmail() + "is an invalid email", model);
			return "public/joinMailingList";
		}

		try {
			sendMailingListEmail(contactForm, true);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		redirectAttributes.addFlashAttribute("thankYouMessage", "We have added you to our mailing list");

		return "redirect:/thankYou";
	}

	// Remove from mailing list
	@RequestMapping(value = "removeFromMailingList", method = RequestMethod.GET)
	public String removeFromMailingList(ModelMap model) {
		model.addAttribute(new ContactForm());
		return "public/removeFromMailingList";
	}

	@RequestMapping(value = "/removeFromMailingList", method = RequestMethod.POST)
	public String removeFromMailingList(ContactForm contactForm, ModelMap model) {
		try {
			sendMailingListEmail(contactForm, false);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		model.addAttribute("thankYouMessage", "We will remove you from our mailing list");
		return "public/thankYouForRemovingFromMailingList";
	}

	// Show sponsor page, from sidebar.html
	@RequestMapping(value = "/sponsor", method = RequestMethod.GET)
	public String sponsor(ModelMap model) {
		model.addAttribute(new SponsorForm());
		return "public/sponsor";
	}

	// Pay Sponsorship
	@RequestMapping(value = "/paySponsorship")
	public String paySponsorship(@Valid SponsorForm sponsorForm, BindingResult result, ModelMap model, HttpSession session) {

		if (result.hasErrors()) {
			addError("Amount entered is not valid, please correct it", model);
			return "public/sponsor";
		}

		// Get stockitem to be added to order
		StockItem stockItem = stockItemRepository.findById(39315l).orElse(null); // Sponsorship
																	// stockitem
		stockItem.setSellPrice(sponsorForm.getAmount());

		CustomerOrder order = (CustomerOrder) session.getAttribute("order");

		if (order == null) {
			order = new CustomerOrder();

			Customer customer = new Customer();

			customer.setDeliveryType(DeliveryType.SPONSORSHIP);
			customer.setPaymentType(PaymentType.CREDIT_CARD);

			order.setCustomer(customer);

			session.setAttribute("order", order);
		}

		order.addOrderLine(stockItem);

		model.addAttribute(order.getCustomer());
		model.addAttribute(order.getCustomer().getCreditCard());

		addInfo("Please enter your details", model);

		return "customerOrder/customerDetails";

	}
	// private void sendSponsorshipPaidEmail(SponsorForm contactForm) throws
	// MessagingException {
	// // Prepare the evaluation context
	// final Context ctx = new Context();
	// ctx.setVariable("sponsorForm", contactForm);
	//
	// final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
	// final MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
	// true, "UTF-8"); // true = multipart
	//
	//
	// //BCC depending on profile
	// String[] profiles = environment.getActiveProfiles();
	// if(profiles[0].equals("dev") || profiles[0].equals("test")) {
	// //For test and dev
	// message.setSubject("TEST - Sponsor FORM");
	// message.setFrom("test@bookmarksbookshop.co.uk");
	// message.setTo("jack747@gmail.com");
	// } else {
	// message.setSubject(contactForm.getFullName() + " has sponsored us");
	// message.setFrom("info@bookmarksbookshop.co.uk");
	// message.setTo("info@bookmarksbookshop.co.uk");
	// message.setBcc(new String[]{"jack747@gmail.com",
	// "sarahensor@hotmail.co.uk"});
	// }
	//
	// // Create the HTML body using Thymeleaf
	// final String htmlContent =
	// this.templateEngine.process("/mail/sponsor.html", ctx);
	// message.setText(htmlContent, true); // true = isHtml
	//
	// // Add the inline image, referenced from the HTML code as
	// "cid:${imageResourceName}"
	// //final InputStreamSource imageSource = new
	// ByteArrayResource(imageBytes);
	// // message.addInline(imageResourceName, imageSource, imageContentType);
	//
	// // Send mail
	// this.mailSender.send(mimeMessage);
	//
	// }

	// @RequestMapping(value = "/sponsor", method = RequestMethod.POST)
	// public String sponsor(@Valid SponsorForm contactForm, BindingResult
	// result, ModelMap model, RedirectAttributes redirectAttributes) {
	// if(result.hasErrors()) { //IE doesn't allow HTML5 validation
	// if(result.hasFieldErrors("email")) {
	// addError(contactForm.getEmail() + " is an invalid email", model);
	// } else {
	// addError("Please complete all fields in the form.", model);
	// }
	// return "public/sponsor";
	// }
	////
	//// if(contactForm.getName().contains("louis vuitton")) { //Spam
	//// return "redirect:/thankYou";
	//// }
	//
	// try {
	// sendSponsorEmail(contactForm);
	// } catch (MessagingException e) {
	// e.printStackTrace();
	// }
	//
	// redirectAttributes.addFlashAttribute("thankYouMessage", "Thank you for
	// sponsoring us.");
	//
	// return "redirect:/thankYou";
	// }
	//
	// Contact form
	@RequestMapping(value = "/contact", method = RequestMethod.GET)
	public String contact(ModelMap model) {
		model.addAttribute(new ContactForm());
		return "public/contact";
	}

	@RequestMapping(value = "/categories", method = RequestMethod.GET)
	public String categories(ModelMap model) {
		model.addAttribute(new ContactForm());
		return "public/categories";
	}

	@RequestMapping(value = "/xmas", method = RequestMethod.GET)
	public String xmas(ModelMap model) {
		Collection<StockItem> stockItems = stockItemRepository.findByType(StockItemType.MUG, new PageRequest(0, 10));
		model.addAttribute(stockItems);
		return "public/xmas";
	}

	@RequestMapping(value = "/downloadBrochure", method = RequestMethod.GET)
	public void downloadBrochure(HttpServletResponse response) throws java.io.IOException {

		Resource resource = appContext.getResource("file:/home/bookmarks/images/brochure.pdf");

		InputStream is = resource.getInputStream();

		response.setContentType("application/pdf");

		response.setHeader("Content-Disposition", "attachment; filename=\"brochure.pdf\"");

		OutputStream os = response.getOutputStream();

		IOUtils.copy(is, os);
		os.flush();
		os.close();
	}

	@RequestMapping(value = "/contact", method = RequestMethod.POST)
	public String contact(@Valid ContactForm contactForm, BindingResult result, ModelMap model, RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) { // IE doesn't allow HTML5 validation
			if (result.hasFieldErrors("email")) {
				addError(contactForm.getEmail() + " is an invalid email", model);
			} else {
				addError("Please complete all fields in the form.", model);
			}
			return "public/contact";
		}

		// Check name for spam
		for (String s : spamBlackList) {
			if (contactForm.getName().contains(s)) { // Spam
				return "redirect:/thankYou";
			}
		}

		try {
			sendContactEmail(contactForm);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		redirectAttributes.addFlashAttribute("thankYouMessage", "Thank you for getting in touch.");

		return "redirect:/thankYou";
	}

	/**
	 * thankYouMessage will have already been saved in flash scope e.g.
	 * redirectAttributes.addFlashAttribute("thankYouMessage", "Thank you, we
	 * will reply as soon as possible");
	 * 
	 * @return
	 */
	@RequestMapping(value = "/thankYou", method = RequestMethod.GET)
	public String thankYou() {
		return "public/thankYou";
	}

	// private void sendSponsorEmail(SponsorForm sponsorForm) throws
	// MessagingException {
	// // Prepare the evaluation context
	// final Context ctx = new Context();
	// ctx.setVariable("sponsorForm", sponsorForm);
	//
	// final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
	// final MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
	// true, "UTF-8"); // true = multipart
	//
	//
	// //BCC depending on profile
	// String[] profiles = environment.getActiveProfiles();
	// if(profiles[0].equals("dev") || profiles[0].equals("test")) {
	// //For test and dev
	// message.setSubject("TEST - Sponsor FORM");
	// message.setFrom("test@bookmarksbookshop.co.uk");
	// message.setTo("jack747@gmail.com");
	// } else {
	// message.setSubject(sponsorForm.getFullName() + " has sponsored us");
	// message.setFrom("info@bookmarksbookshop.co.uk");
	// message.setTo("info@bookmarksbookshop.co.uk");
	// message.setBcc(new String[]{"jack747@gmail.com",
	// "sarahensor@hotmail.co.uk"});
	// }
	//
	// // Create the HTML body using Thymeleaf
	// final String htmlContent =
	// this.templateEngine.process("/mail/sponsor.html", ctx);
	// message.setText(htmlContent, true); // true = isHtml
	//
	// // Add the inline image, referenced from the HTML code as
	// "cid:${imageResourceName}"
	// //final InputStreamSource imageSource = new
	// ByteArrayResource(imageBytes);
	// // message.addInline(imageResourceName, imageSource, imageContentType);
	//
	// // Send mail
	// this.mailSender.send(mimeMessage);
	// }

	private void sendContactEmail(ContactForm contactForm) throws MessagingException {
		
		logger.info("Sending email to info@bookmarksbookshop.co.uk {}", contactForm);
		
		// Prepare the evaluation context
		final Context ctx = new Context();
		ctx.setVariable("contactForm", contactForm);

		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart

		if (environment.acceptsProfiles("prod")) {
			message.setSubject(contactForm.getName() + " has contacted us");
			message.setFrom("info@bookmarksbookshop.co.uk");
			message.setTo("info@bookmarksbookshop.co.uk");
		} else {
			// For test and dev
			message.setSubject("TEST - Contact FORM");
			message.setFrom("test@bookmarksbookshop.co.uk");
			message.setTo("jack747@gmail.com");
		}

		// Create the HTML body using Thymeleaf
		final String htmlContent = this.templateEngine.process("/mail/contact.html", ctx);
		message.setText(htmlContent, true); // true = isHtml

		// Send mail
		this.mailSender.send(mimeMessage);
		
		logger.info("Successfully sent!");
	}

	private void sendMailingListEmail(ContactForm contactForm, boolean join) throws MessagingException {
		
		logger.info("Sending mailing list email to info@bookmarksbookshop.co.uk {}", contactForm);
		
		// Prepare the evaluation context
		final Context ctx = new Context();
		ctx.setVariable("contactForm", contactForm);

		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); 
		
		if (join) {
			message.setSubject(contactForm.getEmail() + " wants to join our mailing list");
		} else {
			message.setSubject(contactForm.getEmail() + " wants to be removed from our mailing list ***********");
		}
		
		message.setFrom("chips@bookmarksbookshop.co.uk");
		message.setTo("info@bookmarksbookshop.co.uk");

		final String htmlContent = this.templateEngine.process("/mail/mailingList.html", ctx);
		message.setText(htmlContent, true); // true = isHtml

		// Send mail
		this.mailSender.send(mimeMessage);
		
		logger.info("Successfully sent!");
	}

}
