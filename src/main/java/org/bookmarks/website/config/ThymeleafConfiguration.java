package org.bookmarks.website.config;

import java.util.Collections;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.springframework.context.annotation.FilterType;

@Configuration
@ConditionalOnClass({SpringTemplateEngine.class})
@EnableConfigurationProperties({ThymeleafProperties.class})  //no sense rolling our own.
@AutoConfigureAfter({WebMvcAutoConfiguration.class})
public class ThymeleafConfiguration implements ApplicationContextAware, EnvironmentAware {
	
	private ApplicationContext applicationContext;

	private Environment environment;
	
	@Autowired
    private ThymeleafProperties properties;


	/* ****************************************************************
	 *
	 * Resources
	 *
	 * ****************************************************************
	 */

//	@Override
//	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
//		registry.addResourceHandler("/resources/**")
//			.addResourceLocations("/")
//			.setCachePeriod(60 * 60 * 24 * 365) /* one year */
//			.resourceChain( environment.acceptsProfiles("prod") ? true : false )
//			.addResolver(
//                    new VersionResourceResolver().addContentVersionStrategy("/**"));
//
//		if (!environment.acceptsProfiles("prod")) {
//			registry.addResourceHandler("/imageFiles/**").addResourceLocations("file:/home/bookmarks/images/");
//		}
//	}
//
//	// equivalent for <mvc:default-servlet-handler/> tag
//	@Override
//	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
//		configurer.enable();
//	}

	/* **************************************************************** */
	/* THYMELEAF-SPECIFIC ARTIFACTS */
	/* TemplateResolver <- TemplateEngine <- ViewResolver */
	/* **************************************************************** */

	@Bean
	public TemplateEngine templateEngine() {
		// SpringTemplateEngine automatically applies SpringStandardDialect and
		// enables Spring's own MessageSource message resolution mechanisms.
		final SpringTemplateEngine templateEngine = new SpringTemplateEngine();

		// Resolver for HTML emails
		templateEngine.addTemplateResolver(emailTemplateResolver());

		// Resolver for HTML editable emails (which will be treated as a String)
		templateEngine.addTemplateResolver(springTemplateResolver());

		// Enabling the SpringEL compiler with Spring 4.2.4 or newer can
		// speed up execution in most scenarios, but might be incompatible
		// with specific cases when expressions in one template are reused
		// across different data types, so this flag is "false" by default
		// for safer backwards compatibility.
		//templateEngine.setEnableSpringELCompiler(true);

		templateEngine.addDialect(new nz.net.ultraq.thymeleaf.LayoutDialect());

		return templateEngine;
	}

	@Bean
	public ThymeleafViewResolver viewResolver() {
		final ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();

		viewResolver.setTemplateEngine(templateEngine());

		viewResolver.setCharacterEncoding("UTF-8");

		viewResolver.setContentType("text/html;charSet=utf-8");

		// NOTE 'order' and 'viewNames' are optional
		// viewResolver.setOrder(1);
		// viewResolver.setViewNames(new String[] {".html", ".xhtml"});

		return viewResolver;
	}

	private ITemplateResolver springTemplateResolver() {

		// SpringResourceTemplateResolver automatically integrates with Spring's
		// own resource resolution infrastructure, which is highly recommended.
		SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
		templateResolver.setApplicationContext(this.applicationContext);
		templateResolver.setPrefix("/WEB-INF/thymeleaf/");
		templateResolver.setSuffix(".html");
		// templateResolver.setResolvablePatterns(Collections.singleton("*.html"));

		// HTML is the default value, added here for the sake of clarity.
		templateResolver.setTemplateMode(TemplateMode.HTML);

		templateResolver.setOrder(Integer.valueOf(2));

		templateResolver.setSuffix(".html");

		templateResolver.setCacheable( environment.acceptsProfiles("prod") ? true : false );

		return templateResolver;
	}

	private ITemplateResolver emailTemplateResolver() {
		final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();

		templateResolver.setOrder(Integer.valueOf(1));
		templateResolver.setResolvablePatterns(Collections.singleton("/mail/*"));
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCharacterEncoding("UTF-8");

		return templateResolver;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}


	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
