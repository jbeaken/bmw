package org.bookmarks.website.controller;

import java.util.Date;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.bookmarks.website.domain.Address;
import org.bookmarks.website.domain.CreditCard;
import org.bookmarks.website.domain.Customer;
import org.bookmarks.website.domain.CustomerOrder;
import org.bookmarks.website.domain.DeliveryType;
import org.bookmarks.website.domain.OrderLine;
import org.bookmarks.website.domain.PaymentType;
import org.bookmarks.website.domain.StockItem;
import org.bookmarks.website.repository.CustomerRepository;
import org.bookmarks.website.repository.StockItemRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RequestMapping("/customerOrder")
@Controller
public class CustomerOrderController extends AbstractBookmarksWebsiteController {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private TemplateEngine templateEngine;

	@Autowired
	private StockItemRepository stockItemRepository;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private Environment environment;

	private final Logger logger = LoggerFactory.getLogger(CustomerOrderController.class);

	@RequestMapping(value = "/{stockItemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody StockItem addStockItemToCustomerOrder(@PathVariable("stockItemId") Long stockItemId, HttpSession session) {

		StockItem stockItem = stockItemRepository.findById(stockItemId).orElse(null);
		
		//HACK Json throws Lazy Init, move info to a non-hibernate class
		StockItem detached = new StockItem();
		detached.setTitle(stockItem.getTitle());
		detached.setId(stockItem.getId());
		detached.setImageFilename(stockItem.getImageFilename());

		logger.info("Adding {} to customer order", stockItem);

		CustomerOrder order = getCustomerOrder(session);
		order.addOrderLine(stockItem);

		return detached;
	}

	@RequestMapping(value = "/addStockItemToCustomerOrderForMobile", method = RequestMethod.GET)
	public String addStockItemToCustomerOrderForMobile(Long id, HttpSession session) {

		if(id == null) {
			//Bad bot
			return "error";
		}
		
		StockItem stockItem = stockItemRepository.findById(id).orElse(null);

		CustomerOrder order = getCustomerOrder(session);
		order.addOrderLine(stockItem);

		return "public/addedToBasket";
	}

	@RequestMapping(value = "/removeCustomerOrderLine")
	public String removeCustomerOrderLine(Long stockItemId, HttpSession session) {


		CustomerOrder order = (CustomerOrder) session.getAttribute("order");
		if (order == null)	return "redirect:/";

		order.removeOrderLine(stockItemId);

		// Is basket now empty?
		if (order.getCustomer().getOrders().isEmpty()) {
			return emptyBasket(session);
		}

		return "redirect:/customerOrder/showBasket";
	}

	@RequestMapping(value = "/updateQuantity")
	public String updateQuantity(Long stockItemId, Integer quantity, HttpSession session) {

		// Remove from customer order
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");
		if (order == null)
			return "redirect:/";

		order.updateQuantity(stockItemId, quantity);

		return "redirect:/customerOrder/showBasket";
	}

	@RequestMapping(value = "/emptyBasket")
	public String emptyBasket(HttpSession session) {

		// Remove customer order from session
		// session.removeAttribute("order"); Think this was causing
		// problems, but not sure how. Mike B saw it happen, but just a guess

		return "redirect:/";
	}

	@RequestMapping(value = "/showBasket")
	public String showBasket(HttpSession session, ModelMap model) {
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");

		if (order == null)	return "redirect:/emptyBasket";

		return "customerOrder/basket";
	}

	@RequestMapping(value = "/showPaymentDetailsScreen")
	public String showPaymentDetailsScreen(HttpSession session, ModelMap model) {
		
		logger.info("Customer request for showPaymentDetailsScreen");
		
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");

		if (order == null)
			return "redirect:/";

		model.addAttribute(order.getCustomer().getCreditCard());
		return "customerOrder/paymentDetails";
	}

	@RequestMapping(value = "/showCustomerDetailsScreen")
	public String showCustomerDetailsScreen(String flow, HttpSession session, ModelMap model) {
		
		logger.info("Customer request for showCustomerDetailsScreen");

		CustomerOrder order = (CustomerOrder) session.getAttribute("order");

		if (order == null)	return "redirect:/";

		// Is this from basket screen? If so could be that user has already
		// entered details
		if (order.getCustomer().getAddress().getAddress1() != null && order.getCustomer().getCreditCard().getCreditCard1() != null) {
			// Details already completed, send to confirmation screen
			return "customerOrder/confirmation";
		}

		order.getCustomer().setPaymentType(PaymentType.CREDIT_CARD);

		model.addAttribute(order.getCustomer());

		return "customerOrder/customerDetails";
	}

	@RequestMapping(value = "/showDeliveryDetailsScreen")
	public String showDeliveryDetailsScreen(HttpSession session, ModelMap model) {
		
		logger.info("Customer request for showDeliveryDetailsScreen");
		
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");

		if (order == null)
			return "redirect:/";

		model.addAttribute(order.getCustomer().getAddress());
		return "customerOrder/deliveryDetails";
	}

	@RequestMapping(value = "/saveCustomerDetails", method = RequestMethod.POST)
	public String saveCustomerDetails(@Valid Customer customer, BindingResult bindingResult, HttpSession session, ModelMap modelMap) {
		
		logger.info("Customer request for saveCustomerDetails");
		
		CustomerOrder sessionOrder = (CustomerOrder) session.getAttribute("order");

		if (sessionOrder == null)
			return "redirect:/";

		if (bindingResult.hasErrors()) {
			addError("Please fill out the required fields details", modelMap);
			modelMap.addAttribute(customer);
			return "customerOrder/customerDetails";
		}

		customer.setOrders(sessionOrder.getCustomer().getOrders()); // Transfer
																	// from
																	// session
		customer.setDeliveryType(sessionOrder.getCustomer().getDeliveryType());
		customer.setPaymentType(sessionOrder.getCustomer().getPaymentType());

		sessionOrder.setCustomer(customer);

		modelMap.addAttribute(sessionOrder.getCustomer().getAddress());

		if (customer.getDeliveryType() == DeliveryType.SPONSORSHIP) {
			modelMap.addAttribute(customer.getCreditCard());
			return "customerOrder/paymentDetails";
		}

		return "customerOrder/deliveryDetails";
	}

	@RequestMapping(value = "/saveDeliveryDetails")
	public String saveDeliveryDetails(@Valid Address address, BindingResult bindingResult, HttpSession session, ModelMap modelMap) {
		
		logger.info("Customer request for saveDeliveryDetails");
		
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");
		if (order == null)
			return "redirect:/";

		if (bindingResult.hasErrors()) {
			addError("Please fill out the required address details", modelMap);
			modelMap.addAttribute(address);
			return "customerOrder/deliveryDetails";
		}

		order.getCustomer().setAddress(address);

		if (order.getCustomer().getPaymentType() == PaymentType.ACCOUNT) {
			return "customerOrder/confirmation";
		}

		// If payment details have already been entered, go to confirmation
		if (order.getCustomer().getCreditCard().getCreditCard1() != null) {
			return "customerOrder/confirmation";
		}

		modelMap.addAttribute(order.getCustomer().getCreditCard());

		return "customerOrder/paymentDetails";
	}

	/**
	 * Should only come here if the user is paying by credit card
	 */
	@RequestMapping(value = "/savePaymentDetails")
	public String savePaymentDetails(@Valid CreditCard creditCard, HttpSession session, ModelMap modelMap) {
		
		logger.info("Customer request for saveDeliveryDetails"); 
		
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");
		if (order == null)
			return "redirect:/";

		// Validation on empty fields
		if (creditCard.getCreditCard1() == null || creditCard.getCreditCard2() == null || creditCard.getCreditCard3() == null || creditCard.getCreditCard4() == null
				|| creditCard.getExpiryMonth() == null || creditCard.getExpiryYear() == null || creditCard.getSecurityCode() == null || creditCard.getCreditCard1().isEmpty()
				|| creditCard.getCreditCard2().isEmpty() || creditCard.getCreditCard3().isEmpty() || creditCard.getCreditCard4().isEmpty() || creditCard.getExpiryMonth().isEmpty()
				|| creditCard.getSecurityCode().isEmpty() || creditCard.getExpiryYear().isEmpty()) {
			addError("Please enter all credit card fields", modelMap);
			modelMap.addAttribute(creditCard);
			return "customerOrder/paymentDetails";
		}

		// Validation on expiry date
		if (creditCard.getExpiryYear().equals("2013")) {
			Integer expiryMonth = Integer.parseInt(creditCard.getExpiryMonth());
			if (expiryMonth < 12) {
				addError("This card's expiry date is invalid", modelMap);
				modelMap.addAttribute(creditCard);
				return "customerOrder/paymentDetails";
			}
		}

		order.getCustomer().setCreditCard(creditCard);

		if (order.getCustomer().getDeliveryType() == DeliveryType.COLLECTION) {
			modelMap.addAttribute("isCollection", true);
		}

		return "customerOrder/confirmation";
	}

	@RequestMapping(value = "/setAsCollection")
	public String setAsCollection(HttpSession session, ModelMap model) {
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");
		if (order == null)
			return "redirect:/";

		order.getCustomer().setDeliveryType(DeliveryType.COLLECTION);

		if (order.getCustomer().getPaymentType() == PaymentType.ACCOUNT) {
			return "customerOrder/confirmation";
		}

		// If payment details have already been entered, go to confirmation
		if (order.getCustomer().getCreditCard().getCreditCard1() != null) {
			return "customerOrder/confirmation";
		}

		model.addAttribute(order.getCustomer().getCreditCard());

		// Else get payment details
		return "customerOrder/paymentDetails";
	}

	@RequestMapping(value = "/setAsAccountPaymentType")
	public String setAsAccountPaymentType(HttpSession session, ModelMap model) {
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");
		if (order == null)
			return "redirect:/";

		order.getCustomer().setPaymentType(PaymentType.ACCOUNT);

		model.addAttribute(order.getCustomer());

		return "customerOrder/customerDetails";
	}

	@RequestMapping(value = "/setAsCardPaymentType")
	public String setAsCardPaymentType(HttpSession session, ModelMap model) {
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");
		if (order == null)
			return "redirect:/";

		order.getCustomer().setPaymentType(PaymentType.CREDIT_CARD);

		model.addAttribute(order.getCustomer());

		return "customerOrder/paymentDetails";
	}

	@RequestMapping(value = "/setAsMail")
	public String setAsMail(HttpSession session, ModelMap model) {
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");
		if (order == null)
			return "redirect:/";

		order.getCustomer().setDeliveryType(DeliveryType.MAIL);

		// If delivery details have already been entered, go to confirmation
		if (order.getCustomer().getAddress().getAddress1() != null) {
			return "customerOrder/confirmation";
		}

		model.addAttribute(order.getCustomer().getAddress());

		return "customerOrder/deliveryDetails";
	}

	@RequestMapping(value = "/save")
	public String save(HttpSession session) {
		
		logger.info("Customer request to save customer order"); 
		
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");

		if (order == null)
			return "redirect:/";

		Customer customer = order.getCustomer();
		customer.setHasBeenConsumed(false);

		String webReference = String.valueOf(new Date().getTime()).substring(5); 

		for (OrderLine orderLine : customer.getOrders()) {
			orderLine.setCustomer(customer);
			orderLine.setWebReference(webReference);
			orderLine.setPostage(order.getPostage());
		}

		customerRepository.save(order.getCustomer());

		session.removeAttribute("order");

		try {
			sendOrderConfirmation(order, webReference);
		} catch (Exception e) {
			// Doesn't stop anything
			logger.error("Cannot send email", e);
		}
		
		logger.info("System has saved customer order {}, redirect to thank you page", webReference);

		return "redirect:/customerOrder/thankYou";
	}

	@RequestMapping(value = "/thankYou")
	public String thankYou() {
		return "customerOrder/thankYou";
	}

	private CustomerOrder getCustomerOrder(HttpSession session) {
		CustomerOrder order = (CustomerOrder) session.getAttribute("order");
		if (order == null) {
			order = new CustomerOrder();
			session.setAttribute("order", order);
		}
		return order;
	}
	
//	@RequestMapping(value = "/mail")
//	public String mail() throws MessagingException {
//		
//		Customer customer = customerRepository.findById(2l).orElse(null);
//
//		logger.info("Attempting sending email of order confirmation");
//		
//		final Context ctx = new Context();
//		
//		ctx.setVariable("customer", customerOrder.getCustomer());
//		ctx.setVariable("postage", customerOrder.getPostage());
//		ctx.setVariable("totalPrice", customerOrder.getTotalPrice());
//		ctx.setVariable("webReference", webReference);
//
//		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
//		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); 
//		
//		String[] profiles = environment.getActiveProfiles();
//		
//		if (profiles.length != 0 && profiles[0].equals("prod")) {
//			message.setSubject("Your Bookmarks Order Confirmation");
//			message.setBcc("info@bookmarksbookshop.co.uk");
//			message.setFrom("info@bookmarksbookshop.co.uk");
//			message.setTo(customerOrder.getCustomer().getContactDetails().getEmail());
//		} else {
//			message.setSubject("TEST - Your Bookmarks Order Confirmation");
//			message.setFrom("test@bookmarksbookshop.co.uk");
//		}
//
//		// Create the HTML body using Thymeleaf
//		final String htmlContent = this.templateEngine.process("/mail/orderComplete.html", ctx);
//		message.setText(htmlContent, true); // true = isHtml
//
//		this.mailSender.send(mimeMessage);
//
//		logger.info("Sent!");
//		
//		return "home";
//	}

	
	private void sendOrderConfirmation(CustomerOrder customerOrder, String webReference) throws MessagingException {

		logger.info("Attempting sending email of order confirmation");
		
		final Context ctx = new Context();
		
		ctx.setVariable("customer", customerOrder.getCustomer());
		ctx.setVariable("postage", customerOrder.getPostage());
		ctx.setVariable("totalPrice", customerOrder.getTotalPrice());
		ctx.setVariable("webReference", webReference);

		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); 
		
		String[] profiles = environment.getActiveProfiles();
		
		if (profiles.length != 0 && profiles[0].equals("prod")) {
			message.setSubject("Your Bookmarks Order Confirmation");
			message.setBcc("info@bookmarksbookshop.co.uk");
			message.setFrom("info@bookmarksbookshop.co.uk");
			message.setTo(customerOrder.getCustomer().getContactDetails().getEmail());
		} else {
			message.setSubject("TEST - Your Bookmarks Order Confirmation");
			message.setFrom("test@bookmarksbookshop.co.uk");
			message.setTo("jack747@gmail.com");
		}

		// Create the HTML body using Thymeleaf
		final String htmlContent = this.templateEngine.process("/mail/orderComplete.html", ctx);
		message.setText(htmlContent, true); // true = isHtml

		this.mailSender.send(mimeMessage);

		logger.info("Sent!");
	}
}
