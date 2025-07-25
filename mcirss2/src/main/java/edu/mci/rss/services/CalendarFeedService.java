package edu.mci.rss.services;


import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import edu.mci.rss.HandledCalendarTools;
import edu.mci.rss.calendarNews.CalendarNewsHandler;
import edu.mci.rss.utils.FeedUtils;
import edu.mci.rss.utils.MciRssSessionUtils;
import edu.mci.rss.utils.NewsItemFilterCriteriaUtils;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private PublishedAssessmentService publishedAssessmentService;
    @Autowired
    private ObjectProvider<NewsItemFilterCriteriaUtils> newsItemFilterCriteriaUtilsBeanProvider;


    public CalendarFeedService() {
        this.publishedAssessmentService = new PublishedAssessmentService();
    }

    @Autowired
    @Qualifier("assignmentCalendarNewsHandler")
    private CalendarNewsHandler assignmentNewsHandler;

    @Autowired
    @Qualifier("samigoCalendarNewsHandler")
    private CalendarNewsHandler samigoCalendarNewsHandler;


    public record CalendarNewsItemData(Site site, String userId) {}

    public Feed createFeedForUserId(String userId) {
        NewsItemFilterCriteriaUtils newsItemFilterCriteriaUtils  = newsItemFilterCriteriaUtilsBeanProvider.getObject();

        Feed feed = FeedUtils.createAtomFeed();
        List<Entry> entries = new LinkedList<>();

        mciRssSessionUtils.switchToUserAndOrEid(userId, null);

        List<Site> userSites = siteService.getUserSites(false, userId);

        for (Site site : userSites) {
            entries.addAll(getEntriesForUserInAssignments(new CalendarNewsItemData(site, userId), newsItemFilterCriteriaUtils));
            entries.addAll(getEntriesForUserInAssessments(new CalendarNewsItemData(site, userId), newsItemFilterCriteriaUtils));
        }

        mciRssSessionUtils.switchToUserAndOrEid(null, null);

        if (!entries.isEmpty()) {
            feed.setEntries(entries);
        }

        return feed;
    }


    private List<Entry> getEntriesForUserInAssignments(CalendarNewsItemData newsItemDatadata, NewsItemFilterCriteriaUtils newsItemFilterCriteriaUtils) {
        Collection<Assignment> assignments = assignmentService.getAssignmentsForContext(newsItemDatadata.site().getId());

        List<Assignment> filteredAssignments = checkItemsTimeRange(new FilterDetails(assignments, HandledCalendarTools.ASSIGNMENT, newsItemFilterCriteriaUtils));
        return assignmentNewsHandler.processCalendarNewsItems(filteredAssignments, newsItemDatadata);
    }

    private List<Entry> getEntriesForUserInAssessments(CalendarNewsItemData newsItemDatadata, NewsItemFilterCriteriaUtils newsItemFilterCriteriaUtils) {
        List<PublishedAssessmentFacade> assessments = publishedAssessmentService.getBasicInfoOfAllPublishedAssessments(null, "dueDate", false, newsItemDatadata.site().getId());
        List<PublishedAssessmentFacade> filteredAssessments  = checkItemsTimeRange(new FilterDetails(assessments, HandledCalendarTools.SAMIGO, newsItemFilterCriteriaUtils));
        return samigoCalendarNewsHandler.processCalendarNewsItems(filteredAssessments, newsItemDatadata);
    }



    private List checkItemsTimeRange(FilterDetails filterDetails) {
       return switch(filterDetails.handledCalendarTools) {
            case SAMIGO -> checkItemsTimeRangeForSamigo(filterDetails);
            case ASSIGNMENT -> checkItemsTimeRangeForAssignment(filterDetails);
        };
    }

    @SuppressWarnings("unchecked")
    private List<PublishedAssessmentFacade> checkItemsTimeRangeForSamigo(FilterDetails filterDetails) {
        return filterDetails.items.stream()
                .filter(item -> item instanceof PublishedAssessmentFacade pub
                        && pub.getDueDate() != null && filterDetails.newsItemFilterCriteriaUtils.checkTimeRangeForCalendar(pub.getDueDate()))
                .map(PublishedAssessmentFacade.class::cast)
                .toList();
    }

    @SuppressWarnings("unchecked")
    private List<Assignment> checkItemsTimeRangeForAssignment(FilterDetails filterDetails) {
        return filterDetails.items.stream()
                .filter(item ->  item instanceof Assignment a
                        && a.getDueDate() != null && filterDetails.newsItemFilterCriteriaUtils.checkTimeRangeForCalendar(a.getDueDate()))
                .map(Assignment.class::cast)
                .toList();
    }


    private record FilterDetails(Collection items, HandledCalendarTools handledCalendarTools, NewsItemFilterCriteriaUtils newsItemFilterCriteriaUtils){};


}

