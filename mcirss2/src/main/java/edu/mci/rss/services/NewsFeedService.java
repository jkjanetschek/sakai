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
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.TestsAndQuizzesUserNotificationHandler;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;


@Slf4j
@Service
public class NewsFeedService {


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

    private final PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();






    public Feed createFeedForUserId(String userId) {
        System.out.println("Creating Feed for userId: " + userId);

        List<UserNotification> userNotifications = getUserNotifications(userId);
        if (userNotifications.isEmpty()) {
            System.out.println("No user notifications found for userId: " + userId);
            return FeedUtils.createAtomFeed();
        }
        // TODO linked List notwenig? --> ja weil linkedlIst schneller ist als arraylist bei vielen insertions und hier kein retrieval notwenig ist
        LinkedList<UserNotification> userNotificationsFiltered= filterUserNotifications(userNotifications, userId);
        if (userNotifications.isEmpty()) {
            System.out.println("No user notifications in TimeRange found for userId: " + userId);
            return FeedUtils.createAtomFeed();
        }


        System.out.println("NewsFeedService.getUserNotifications() -> size filtered NewsItems: " + userNotificationsFiltered.size());
        Feed feed = processFilteredNotifications(userNotificationsFiltered, userId);



        System.out.println("feed: "  + feed.toString());
        return feed;
    }

    private List<UserNotification> getUserNotifications(String userId) {

         List<UserNotification> academicAlerts =  userMessagingService.getNotificationsForUser(userId);
         if (academicAlerts == null) {
             throw new IllegalStateException("Error getting UserNotifications for userId: " + userId);
         }

         //TODO
        System.out.println("NewsFeedService.getUserNotifications() --> UserNotification.size: " + academicAlerts.size());
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

        NewsItemFilterCriteriaUtils newsItemFilterCriteriaUtils = new NewsItemFilterCriteriaUtils();
        LinkedList<UserNotification> userNotificationsFiltered = new LinkedList<UserNotification>();


        mciRssSessionUtils.switchToUserAndOrEid(userId,null);


        for (UserNotification userNotification : userNotifications) {


            Instant newsTimeInstant = getEventTimeFromUserNotifications(userNotification);
            if (newsTimeInstant == null) {
                System.out.println("newsTimeInstant is null");
                continue;
            }
            // check if add nesItem to filtered list --> consider timerange and item Count
            if (newsItemFilterCriteriaUtils.checkTimeRangeAndCountOfItem(newsTimeInstant)) {
                System.out.println("add item to filterd");
                userNotificationsFiltered.add(userNotification);
            }
        }
        mciRssSessionUtils.switchToUserAndOrEid(null,null);
        return userNotificationsFiltered;
    }


    /**
     *  get Event Time from userNotification based on event Type
     *
     * @param userNotification
     * @return Instant
     */


    private Instant getEventTimeFromUserNotifications(UserNotification userNotification) {
        System.out.println("GetEventTimeFromNews: event = " + userNotification.getEvent());
        return  switch (userNotification.getEvent()) {
            case AnnouncementService.SECURE_ANNC_ADD -> userNotification.getEventDate();
            case AssignmentConstants.EVENT_ADD_ASSIGNMENT, AssignmentConstants.EVENT_AVAILABLE_ASSIGNMENT -> {
                String referenceString = userNotification.getRef();
                Reference assiReference = entityManager.newReference(referenceString);
                try {
                    Assignment assignment = assignmentService.getAssignment(assiReference);
                    yield assignment.getDueDate();
                } catch (IdUnusedException | PermissionException e) {
                    log.error("Error getting assignment for mci rss news item {}",e.getMessage());
                    yield null;
                }
            }
            case SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE -> {
                String referenceString = userNotification.getRef();
                List<String> refParts = TestsAndQuizzesUserNotificationHandler.regexHelper(referenceString);
                PublishedAssessmentData pubData = null;
                try{
                    String publishedAssessmentId = refParts.get(1);
                    pubData = publishedAssessmentService.getBasicInfoOfPublishedAssessment(publishedAssessmentId);
                } catch (IndexOutOfBoundsException e) {
                    log.warn("Error getting pubData for assessment ref {}", referenceString);
                    yield  null;
                }
                if (pubData != null) {
                    try{
                        yield pubData.getDueDate().toInstant();
                    }catch (NullPointerException dueDateEx){
                        yield  pubData.getStartDate().toInstant();
                    }
                }
                yield null;
            }
            // event type is not handled
            default -> null;
        };
    }



    /*
List<Entry> entries = feed.getEntries();
if (entries == null) {
entries = new ArrayList<>();
}
entries.add(newEntry);
feed.setEntries(entries);
     */
    private Feed processFilteredNotifications(LinkedList<UserNotification> filteredUserNotifications, String userId) {
        Feed atomFeed = FeedUtils.createAtomFeed();

        List<Entry> entries = new LinkedList<>();

        for (UserNotification item : filteredUserNotifications) {

            // entries.add(eventHandlerFactory.getHandlers().get(item.getEvent()).processEvent(item));


            MciRssEventHandler handler =  eventHandlerFactory.getHandlers().get(item.getEvent());

            // TODO
            if (handler != null) {
                System.out.println(handler.toString());
            } else {
                System.out.println("no handler found");
                throw new RuntimeException("no handler found");
            }

            System.out.println("process item for event: " + item.getEvent());
            Entry entry = handler.processEvent(new NewsItemProcessingData(userId, item));
            //Entry entry = eventHandlerFactory.getHandlers().get(item.getEvent()).processEvent(item);
            entries.add(entry);

        }
        System.out.println("set atomFeed with entry size:  " + entries.size());
        atomFeed.setEntries(entries);
        return atomFeed;
    }



}
