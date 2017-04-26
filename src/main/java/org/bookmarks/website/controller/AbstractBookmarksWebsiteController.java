package org.bookmarks.website.controller;

import org.springframework.ui.ModelMap;

public class AbstractBookmarksWebsiteController {
	
	protected void addInfo(String info, ModelMap modelMap) {
		modelMap.addAttribute("info", info);
	}
	
	protected void addError(String error, ModelMap modelMap) {
		modelMap.addAttribute("error", error);
	}
	
	protected void addWarning(String warning, ModelMap modelMap) {
		modelMap.addAttribute("warning", warning);
	} 
	
	protected void addSuccess(String success, ModelMap modelMap) {
		modelMap.addAttribute("success", success);
	}
}
