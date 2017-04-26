package org.bookmarks.website.controller;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.bookmarks.website.command.ContactForm;
import org.bookmarks.website.command.EventRegistrationForm;
import org.bookmarks.website.domain.Author;
import org.bookmarks.website.domain.StockItem;
import org.bookmarks.website.repository.StockItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingErrorProcessor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RequestMapping("/event")
@Controller
public class EventController extends AbstractBookmarksWebsiteController {

	@Autowired
	private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
	private Environment environment;

    //Contact form
	@RequestMapping(value = "/registerForEvent", method = RequestMethod.GET)
    public String registerForEvent(String eventName, ModelMap model) {
    	model.addAttribute(new EventRegistrationForm(eventName));
    	return "event/register";
    }

   @RequestMapping(value = "/registerForEvent", method = RequestMethod.POST)
    public String registerForEvent(@Valid EventRegistrationForm eventRegistrationForm, BindingResult result, ModelMap model, RedirectAttributes redirectAttributes) {
    	if(result.hasErrors()) { //IE doesn't allow HTML5 validation
    		if(result.hasFieldErrors("email")) {
    			addError(eventRegistrationForm.getEmail() + " is an invalid email", model);
    		} else {
    			addError("Please complete all fields in the form.", model);
    		}
    		if(eventRegistrationForm.getEmail().trim().isEmpty() && eventRegistrationForm.getTelephone().isEmpty()) {
    			addError("Please enter a telephone number of your email", model);
    		}
    		return "event/register";
    	}

    	if(eventRegistrationForm.getName().contains("louis vuitton")) { //Spam
    		return "redirect:/thankYou";
    	}

    	try {
    		sendEventRegistrationEmail(eventRegistrationForm);
		} catch (MessagingException e) {
			e.printStackTrace();
		}

    	redirectAttributes.addFlashAttribute("thankYouMessage", "Thank you for registering.");

    	return "redirect:/thankYou";
    }


	private void sendEventRegistrationEmail(EventRegistrationForm eventRegistrationForm) throws MessagingException {
		 // Prepare the evaluation context
	    final Context ctx = new Context();
	    ctx.setVariable("eventRegistrationForm", eventRegistrationForm);

	    final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
	    final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8"); // true = multipart


	    //BCC depending on profile
	    String[] profiles = environment.getActiveProfiles();
	    message.setSubject(eventRegistrationForm.getName() + " has registered for " + eventRegistrationForm.getEventName());
	    if(profiles[0].equals("dev") || profiles[0].equals("test")) {
	    	//For test and dev
	    	message.setFrom("test@bookmarksbookshop.co.uk");
	    	message.setTo("jack747@gmail.com");
	    } else {
		    message.setFrom("info@bookmarksbookshop.co.uk");
		    message.setTo("info@bookmarksbookshop.co.uk");
		    //message.setBcc(new String[]{"jack747@gmail.com"});	    	
	    }

	 // Create the HTML body using Thymeleaf
	    final String htmlContent = this.templateEngine.process("/mail/eventRegistration.html", ctx);
	    message.setText(htmlContent, true); // true = isHtml

	    // Add the inline image, referenced from the HTML code as "cid:${imageResourceName}"
	    //final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
	   // message.addInline(imageResourceName, imageSource, imageContentType);

	    // Send mail
	    this.mailSender.send(mimeMessage);
	}

}
