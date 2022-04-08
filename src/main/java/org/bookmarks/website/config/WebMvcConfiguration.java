package org.bookmarks.website.config;

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.VersionResourceResolver;


@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
	
	@Autowired
	private Environment environment;

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		
		registry.addResourceHandler("/resources/**")
			.addResourceLocations("classpath:/static/")
			.setCachePeriod(60 * 60 * 24 * 365) /* one year */
			.resourceChain( environment.acceptsProfiles(Profiles.of("prod")) ? true : false )
			.addResolver(
                    new VersionResourceResolver().addContentVersionStrategy("/**"));

		if (!environment.acceptsProfiles(Profiles.of("prod"))) {
			registry.addResourceHandler("/imageFiles/**").addResourceLocations("file:/home/bookmarks/images/");
		}
	}
	
	@Bean
	public LayoutDialect layoutDialect() {
	  return new LayoutDialect();
	}

}
