package org.bookmarks.website.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ChannelSecurityConfigurer.ChannelRequestMatcherRegistry;

@Configuration
@EnableWebSecurity
//@PropertySource("classpath:spring/application.dev.properties")
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment environment;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser(environment.getProperty("beans.username")).password(environment.getProperty("beans.password")).authorities("ROLE_BEANS");
	}
	
	@Override
	public void configure(WebSecurity web) throws Exception {
	    web.ignoring().antMatchers("/img/**,/css/**,/plugins/**,/js/**");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		

		http.authorizeRequests().anyRequest().permitAll();

		if (environment.acceptsProfiles("prod")) {
			http.requiresChannel().anyRequest().requiresSecure();
		}
	}
}