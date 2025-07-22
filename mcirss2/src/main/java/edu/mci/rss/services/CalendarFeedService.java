package edu.mci.rss.services;


import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import edu.mci.rss.utils.FeedUtils;
import edu.mci.rss.utils.MciRssSessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Component
public class CalendarFeedService implements FeedService {

    @Autowired
    private MciRssSessionUtils mciRssSessionUtils;
    @Autowired
    private SiteService siteService;
    @Autowired
    private AssignmentService assignmentService;


    public Feed createFeedForUserId(String userId) {
        Feed feed = FeedUtils.createAtomFeed();
        mciRssSessionUtils.switchToUserAndOrEid(userId, null);
        List<Site> userSites = siteService.getUserSites(false, userId);
        List<Entry> entries = new LinkedList<Entry>();
        for (Site site : userSites) {

        }

        mciRssSessionUtils.switchToUserAndOrEid(null, null);
        return feed;
    }


    private List<Entry> getEntriesForUserInAssignments(String siteId) {

    }


}

