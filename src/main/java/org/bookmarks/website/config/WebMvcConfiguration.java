package org.bookmarks.website.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.VersionResourceResolver;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {
	
	@Autowired
	private Environment environment;

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		
		registry.addResourceHandler("/resources/**")
			.addResourceLocations("classpath:/static/")
			.setCachePeriod(60 * 60 * 24 * 365) /* one year */
			.resourceChain( environment.acceptsProfiles("prod") ? true : false )
			.addResolver(
                    new VersionResourceResolver().addContentVersionStrategy("/**"));

		if (!environment.acceptsProfiles("prod")) {
			registry.addResourceHandler("/imageFiles/**").addResourceLocations("file:/home/bookmarks/images/");
		}
		 
		super.addResourceHandlers(registry);
	}

}
