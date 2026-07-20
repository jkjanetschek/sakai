package org.sakaiproject.lessonbuildertool.api;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import org.sakaiproject.user.api.User;
/**
 * @author Sebastian Riemer
 */
@Getter
public class UserAcknowledgeInfo {
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

	private final String  eid;

	private final String  firstname;

	private final String  lastname;

	private final String timeOfLastAcknowledge;

	private final String  acknowledgementGivenOnSiteId;

	private final String  emailAddress;

	public UserAcknowledgeInfo(User user,
			LocalDateTime timeOfLastAcknowledge,
			String acknowledgementGivenOnSiteId) {
		this.eid = user.getEid();
		this.firstname = user.getFirstName();
		this.lastname = user.getLastName();
		if (timeOfLastAcknowledge != null) {
			this.timeOfLastAcknowledge = timeOfLastAcknowledge.format(formatter);
		} else {
			this.timeOfLastAcknowledge = "unknown";
		}
		this.acknowledgementGivenOnSiteId = acknowledgementGivenOnSiteId;
		this.emailAddress = user.getEmail();
	}

}
