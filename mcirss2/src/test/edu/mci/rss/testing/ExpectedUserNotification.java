package edu.mci.rss.testing;

import lombok.Data;

import java.time.Instant;

@Data
public class ExpectedUserNotification {

    private Long id;

    private String fromUser;

    private String toUser;

    private String event;

    private String ref;

    private String title;

    private String siteId;

    private String url;

    private Instant eventDate;

    private Boolean deferred = Boolean.FALSE;

    private Boolean viewed = Boolean.FALSE;

    private String tool;

    private String fromDisplayName;

    private String formattedEventDate;

    private String siteTitle;

}
