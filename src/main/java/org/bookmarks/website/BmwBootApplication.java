package org.bookmarks.website;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@SpringBootApplication
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
}
