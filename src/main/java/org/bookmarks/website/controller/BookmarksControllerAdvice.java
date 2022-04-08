package org.bookmarks.website.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class BookmarksControllerAdvice {
	
	 private final static Logger logger = LoggerFactory.getLogger(BookmarksControllerAdvice.class);

//	@ExceptionHandler
//	public String handleBusinessException(Exception ex) {
//		logger.error("In error handler", ex);
//		return "404";
//	}

}