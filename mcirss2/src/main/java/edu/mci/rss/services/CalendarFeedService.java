package edu.mci.rss.services;


import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import edu.mci.rss.HandledCalendarTools;
import edu.mci.rss.calendarNews.AssignmentCalendarNewsHandler;
import edu.mci.rss.utils.FeedUtils;
import edu.mci.rss.utils.MciRssSessionUtils;
import edu.mci.rss.utils.NewsItemFilterCriteriaUtils;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
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
    @Autowired
    private ObjectProvider<NewsItemFilterCriteriaUtils> newsItemFilterCriteriaUtilsBeanProvider;
    @Autowired
    private AssignmentCalendarNewsHandler  assignmentCalendarNewsHandler;



    public Feed createFeedForUserId(String userId) {
        NewsItemFilterCriteriaUtils newsItemFilterCriteriaUtils  = newsItemFilterCriteriaUtilsBeanProvider.getObject();

        Feed feed = FeedUtils.createAtomFeed();
        mciRssSessionUtils.switchToUserAndOrEid(userId, null);
        List<Site> userSites = siteService.getUserSites(false, userId);
        List<Entry> entries = new LinkedList<>();
        for (Site site : userSites) {
            entries.addAll(getEntriesForUserInAssignments(site.getId(), newsItemFilterCriteriaUtils));
        }

        mciRssSessionUtils.switchToUserAndOrEid(null, null);
        return feed;
    }


    private List<Entry> getEntriesForUserInAssignments(String siteId,  NewsItemFilterCriteriaUtils newsItemFilterCriteriaUtils) {
        List<Entry> entries = new ArrayList<>();
        Collection<Assignment> assignments = assignmentService.getAssignmentsForContext(siteId);
        entries.addAll(checkItemsTimeRange(new FilterDetails(assignments, HandledCalendarTools.ASSIGNMENT, newsItemFilterCriteriaUtils)));


        return entries;
    }

    private List checkItemsTimeRange(FilterDetails filterDetails) {
        Instant today = Instant.now();
        List filtedItems = new ArrayList();

       return switch(filterDetails.handledCalendarTools) {
            case SAMIGO -> checkItemsTimeRangeForSamigo(filterDetails);
            case ASSIGNMENT -> checkItemsTimeRangeForAssignment(filterDetails);
        }
    }


    private List checkItemsTimeRangeForSamigo(FilterDetails filterDetails) {
        List filtedItems = new ArrayList();

        return filtedItems
    }

    private record FilterDetails(Collection<Assignment> assignments, HandledCalendarTools handledCalendarTools, NewsItemFilterCriteriaUtils newsItemFilterCriteriaUtils){};


}

