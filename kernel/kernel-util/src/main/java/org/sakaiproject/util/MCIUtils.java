package org.sakaiproject.util;

import java.util.Optional;

/**
 * @author Sebastian Riemer
 */
public class MCIUtils {

	public static String MCI_MAIL_SUFFIX = "@mci4me.at";

	public static Optional<String> stripDomain(String loginName, String domain) {
		if (loginName != null && loginName.endsWith(domain)) {
			return Optional.of(loginName.substring(0, loginName.length() - domain.length()));
		}
		return Optional.empty();
	}
}
