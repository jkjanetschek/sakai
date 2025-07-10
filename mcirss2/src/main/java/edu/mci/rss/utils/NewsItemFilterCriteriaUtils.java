package edu.mci.rss.utils;

import edu.mci.rss.ItemRangeTrigger;
import edu.mci.rss.eventHandlers.EventHandlerFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;

import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.TestsAndQuizzesUserNotificationHandler;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Slf4j
@Component
public class NewsItemFilterCriteriaUtils {


    @Autowired
    private EntityManager entityManager;
    @Autowired
    private AssignmentService assignmentService;

    private PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();


    private List<String> handledEvents = List.of(
            AnnouncementService.SECURE_ANNC_ADD,
            AssignmentConstants.EVENT_ADD_ASSIGNMENT,
            AssignmentConstants.EVENT_AVAILABLE_ASSIGNMENT,
            SamigoConstants.EVENT_ASSESSMENT_AVAILABLE,
            SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE
    );


    // two weeks: all news
    public final long TIME_RANGE_SHORT = 1209600000l;

    // if no news in the last two weeks: display up to 10 news from the last 6
    // months (180 days)
    public final long TIME_RANGE_LONG = 15552000000l;

    // Half year for calendar rss 15778800l
    //2 month = 62 days = 5356800 seconds
    public final long TIME_RANGE_CALENDAR_SECONDS = 5356800l;

    private final Instant timeRangeShortInstant;
    private final Instant timeRangeLongInstant;
    private ItemRangeTrigger itemRangeTrigger;
    private int longRangeItemCounter = 0;
    private final int MAX_LONG_RANGE_ITEMS = 10;

    public NewsItemFilterCriteriaUtils() {
        timeRangeShortInstant  = Instant.now().minusMillis(TIME_RANGE_SHORT);
        timeRangeLongInstant = Instant.now().minusMillis(TIME_RANGE_LONG);
        itemRangeTrigger = ItemRangeTrigger.LONG;
    }



    public boolean checkTimeRangeAndCountOfItem (Instant newsTimeInstant) {
        boolean result = false;

        if (newsTimeInstant.isAfter(timeRangeShortInstant)) {
            itemRangeTrigger = ItemRangeTrigger.SHORT;
            result =  true;

        } else  if (ItemRangeTrigger.LONG.equals(itemRangeTrigger)
                && (newsTimeInstant.isAfter(timeRangeLongInstant))
                && (longRangeItemCounter < MAX_LONG_RANGE_ITEMS)) {

            longRangeItemCounter++;
            result = true;
        }
        return result;
    }




    public Instant getEventTimeFromUserNotifications(UserNotification userNotification) {
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


    public LinkedList<UserNotification> filterByEventAndSortByTime(List<UserNotification> unsortedUserNotifications) {
        return unsortedUserNotifications.stream().filter(item -> handledEvents.contains(item.getEvent()))
                .sorted(Comparator.comparing(this::getEventTimeFromUserNotifications,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toCollection(LinkedList::new));
    }


}


