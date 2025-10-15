package org.sakaiproject.entitybroker.filter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Sebastian Riemer
 * <p>
 * This wrapper enables to show a custom error page.
 * It is especially useful if the server is configured in server.xml like this, preventing to show actual error reasons:
 * <Valve className="org.apache.catalina.valves.ErrorReportValve" showReport="false" showServerInfo="false" />
 */
public class MCIErrorPageResponseWrapper extends HttpServletResponseWrapper {
	private final ByteArrayOutputStream byteStream   = new ByteArrayOutputStream();

	private final PrintWriter           writer       = new PrintWriter(byteStream);

	private final ServletOutputStream   outputStream = new ServletOutputStream() {
		@Override
		public void write(int b) {
			byteStream.write(b);
		}
	};

	public MCIErrorPageResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public PrintWriter getWriter() {
		return writer;
	}

	@Override
	public ServletOutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * This method is being called by DirectServlet triggering the custom error response.
	 *
	 * @param sc  the error status code
	 * @param msg the descriptive message
	 * @throws IOException
	 */
	@Override
	public void sendError(int sc, String msg) throws IOException {
		setStatus(sc);

		setCharacterEncoding("UTF-8");
		setContentType("text/html;charset=UTF-8");
		StringBuilder htmlContent = new StringBuilder();
		// Construct the HTML content
		htmlContent.append("<html>\n")
				.append("<body>\n")
				.append("<div style=\"background-color: rgb(0, 74, 133); padding: 20px; display: flex; align-items: center;\">\n")
				.append("<img style=\"height: 63px; \" src=\"/library/skin/default-skin/images/sakaiLogo.png\" alt=\"MCI Logo\" />\n")
				.append("<p style=\"color: white; font-size: 20px;\">")  // Adding some styles for the error message
				.append(sc)
				.append(" - ")
				.append(msg)
				.append("</p>\n")
				.append("</div>\n")
				.append("</body>\n")
				.append("</html>\n");
		getWriter().write(htmlContent.toString());
		getWriter().flush();
	}

	public String getCapturedResponse() {
		writer.flush();
		return byteStream.toString();
	}

	/**
	 * Copies the captured content to the actual response. Is being called by the filter if no error happens.
	 *
	 * @throws IOException
	 */
	public void copyContentToResponse() throws IOException {
		byte[] responseData = byteStream.toByteArray();
		getResponse().setContentLength(responseData.length);
		getResponse().getOutputStream().write(responseData);
	}
}
