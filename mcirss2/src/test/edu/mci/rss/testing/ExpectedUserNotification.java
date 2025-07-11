package edu.mci.rss.testing;

import lombok.Data;

import java.time.Instant;

@Data
public class ExpectedUserNotification {

    private Long id;

    private String event;

    private String ref;

    private String title;

    private String siteId;

    private String url;

    private Instant eventDate;

    private String siteTitle;

}
