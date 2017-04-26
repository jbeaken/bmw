package org.bookmarks.website.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class WWWRedirectFilter implements Filter {

	public void init(FilterConfig config) throws ServletException {
	}


	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;

		String serverName = request.getServerName();

		if ( serverName.startsWith("www.") ) {
			
			HttpServletResponse response = (HttpServletResponse) res;
			String requestUrl = request.getRequestURL().toString();

			System.out.println( serverName );
			System.out.println( request.getRequestURL() );

			String url = requestUrl.replace("www.", "");

			System.out.println( "Redirecting to " + url );

			response.sendRedirect( url );
		}
		else {
			chain.doFilter(req, res);
		}
	}

	public void destroy() {
	}
}
