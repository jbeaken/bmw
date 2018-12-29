package org.bookmarks.website.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private Environment environment;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		
		String password = passwordEncoder().encode( environment.getProperty("beans.password") );
		
		auth.inMemoryAuthentication()
			.withUser(environment.getProperty("beans.username"))
			.password( password )
			.authorities("ROLE_BEANS");
	}
	
	@Bean
    public PasswordEncoder passwordEncoder(){
		return new BCryptPasswordEncoder();
    }
	
	@Override
	public void configure(WebSecurity web) throws Exception {
	    web.ignoring().antMatchers("/img/**,/css/**,/plugins/**,/js/**");
	}
	
	@Bean
	public HttpFirewall allowSemicolonHttpFirewall() {
	    StrictHttpFirewall firewall = new StrictHttpFirewall();
	    firewall.setAllowSemicolon(true);
	    return firewall;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeRequests().anyRequest().permitAll();

		//TODO
		http.csrf().disable();		

		http
			.antMatcher("/website/**").authorizeRequests()
			.anyRequest().hasRole("BEANS")
			.and()
		.httpBasic();

		if (environment.acceptsProfiles("prod")) {
			http.requiresChannel().anyRequest().requiresSecure();
		}
	}
}