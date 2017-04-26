package org.bookmarks.website;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate5.encryptor.HibernatePBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.VersionResourceResolver;

//@Configuration
//@ImportResource({ "classpath:spring/tools-config.xml" })
//@EnableJpaRepositories("org.bookmarks.website.repository")
//@ComponentScan(basePackages = { "org.bookmarks.website" }, excludeFilters = @ComponentScan.Filter(value = Controller.class, type = FilterType.ANNOTATION))
@SpringBootApplication
//@ComponentScan(basePackages = { "org.bookmarks.website" } )
public class BmwBootApplication extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(BmwBootApplication.class, args);
	}

	@Autowired
	private Environment environment;

	/**
	 * Mailer
	 */
	@Bean
	public JavaMailSender javaMailService() {
		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("auth.smtp.1and1.co.uk");
		mailSender.setPort(587);
		mailSender.setUsername("auth.smtp.1and1.co.uk");
		mailSender.setPassword("auth.smtp.1and1.co.uk");

		Properties properties = new Properties();
		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.starttls.enable", "true");
		properties.setProperty("mail.debug", "false");

		mailSender.setJavaMailProperties(properties);

		return mailSender;
	}

	/**
	 * Encyrption
	 */

	@Bean
	public BouncyCastleProvider bcProvider() {
		return new BouncyCastleProvider();
	}

	@Bean
	public StandardPBEStringEncryptor jsonEcryptor() {
		StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
		standardPBEStringEncryptor.setAlgorithm(environment.getProperty("json.encrypt.algorithm"));
		standardPBEStringEncryptor.setPassword(environment.getProperty("json.encrypt.password"));
		standardPBEStringEncryptor.setProvider(bcProvider());

		return standardPBEStringEncryptor;
	}

	@Bean
	public HibernatePBEStringEncryptor hibernateStringEncryptor() {
		HibernatePBEStringEncryptor hibernatePBEStringEncryptor = new HibernatePBEStringEncryptor();
		hibernatePBEStringEncryptor.setRegisteredName("strongHibernateStringEncryptor");
		hibernatePBEStringEncryptor.setEncryptor(dbEncryptor());

		return hibernatePBEStringEncryptor;
	}

	@Bean
	public StandardPBEStringEncryptor dbEncryptor() {
		StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
		standardPBEStringEncryptor.setAlgorithm(environment.getProperty("db.encrypt.algorithm"));
		standardPBEStringEncryptor.setPassword(environment.getProperty("db.encrypt.password"));
		standardPBEStringEncryptor.setProvider(bcProvider());

		return standardPBEStringEncryptor;
	}
}

@Configuration
class WebMvcConfiguration extends WebMvcConfigurerAdapter {
	
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

