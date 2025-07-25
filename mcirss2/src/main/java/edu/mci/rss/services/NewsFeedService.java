package edu.mci.rss.services;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import edu.mci.rss.eventHandlers.EventHandlerFactory;
import edu.mci.rss.eventHandlers.MciRssEventHandler;
import edu.mci.rss.model.NewsItemProcessingData;
import edu.mci.rss.utils.FeedUtils;
import edu.mci.rss.utils.MciRssSessionUtils;
import edu.mci.rss.utils.NewsItemFilterCriteriaUtils;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.assignment.api.AssignmentService;

import org.sakaiproject.entity.api.EntityManager;

import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.model.UserNotification;


import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

import java.util.LinkedList;
import java.util.List;



@Slf4j
@Service
public class NewsFeedService implements FeedService {


    @Autowired
    private UserMessagingService userMessagingService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private EventHandlerFactory eventHandlerFactory;
    @Autowired
    private MciRssSessionUtils mciRssSessionUtils;
    @Autowired
    private ObjectProvider<NewsItemFilterCriteriaUtils> newsItemFilterCriteriaUtilsBeanProvider;






    public Feed createFeedForUserId(String userId) {

        List<UserNotification> userNotifications = getUserNotifications(userId);
        Feed feed = FeedUtils.createAtomFeed();

        if (userNotifications.isEmpty()) {
            log.debug("No user notifications found for userId {}", userId);
            return feed;
        }


        LinkedList<UserNotification> userNotificationsFiltered= filterUserNotifications(userNotifications, userId);
        if (userNotifications.isEmpty()) {
            log.debug("No user notifications in time range found for userId {}", userId);
            return feed;
        }


        feed = processFilteredNotifications(userNotificationsFiltered, userId);


        return feed;
    }

    private List<UserNotification> getUserNotifications(String userId) {

         List<UserNotification> academicAlerts =  userMessagingService.getNotificationsForUser(userId);
         if (academicAlerts == null) {
             throw new IllegalStateException("Error getting UserNotifications for userId: " + userId);
         }

         return academicAlerts;
    }


    /**
     * Filter NewsItems according to the following rules: If no newsitems within
     * the TIME_RANGE_SHORT (2 weeks), display up to ten items from
     * TIME_RANGE_LONG. Else display only items from TIME_RANGE_SHORT.
     *
     * @param
     * @param userId
     * @return
     */

    private LinkedList<UserNotification> filterUserNotifications(List<UserNotification> userNotifications, String userId) {


        NewsItemFilterCriteriaUtils newsItemFilterCriteriaUtils  = newsItemFilterCriteriaUtilsBeanProvider.getObject();
        LinkedList<UserNotification> userNotificationsFiltered = new LinkedList<>();


        mciRssSessionUtils.switchToUserAndOrEid(userId,null);



        for (UserNotification userNotification : newsItemFilterCriteriaUtils.filterByEventAndSortByTime(userNotifications)) {


            Instant newsTimeInstant = newsItemFilterCriteriaUtils.getEventTimeFromUserNotifications(userNotification);
            if (newsTimeInstant == null) {
                continue;
            }
            if (newsItemFilterCriteriaUtils.checkTimeRangeAndCountOfItem(newsTimeInstant)) {
                userNotificationsFiltered.add(userNotification);
            }
        }
        mciRssSessionUtils.switchToUserAndOrEid(null,null);
        return userNotificationsFiltered;
    }






    private Feed processFilteredNotifications(LinkedList<UserNotification> filteredUserNotifications, String userId) {
        Feed atomFeed = FeedUtils.createAtomFeed();

        List<Entry> entries = new LinkedList<>();
        for (UserNotification item : filteredUserNotifications) {


            MciRssEventHandler handler =  eventHandlerFactory.getHandlers().get(item.getEvent());

            if (handler == null) {
                throw new RuntimeException("no handler found for news item event. List of news items should be filtered also by handled events at this point");
            }

            Entry entry = handler.processEvent(new NewsItemProcessingData(userId, item));
            entries.add(entry);

        }
        atomFeed.setEntries(entries);
        return atomFeed;
    }



}
