package org.sakaiproject.entitybroker.filter;


import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Sebastian Riemer
 * <p>
 * This filter wraps the response enabling custom error handling.
 */
@Slf4j
public class MCIErrorHandlingFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Log to confirm the filter is initialized
		log.info("MCIErrorHandlingFilter initialized");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		if (req.getRequestURI().contains("/direct/bbb-tool/")) {
			log.debug("MCIErrorHandlingFilter processing: " + req.getRequestURI());
			// Wrap the response
			MCIErrorPageResponseWrapper responseWrapper = new MCIErrorPageResponseWrapper(resp);

			// Proceed with the filter chain
			chain.doFilter(request, responseWrapper);

			// Capture the response content and log it
			String capturedResponse = responseWrapper.getCapturedResponse();
			log.debug("Captured Response: " + capturedResponse);

			// Write the captured response back to the actual response
			responseWrapper.copyContentToResponse();  // Use the helper method
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
		// No cleanup needed in this case
	}
}
