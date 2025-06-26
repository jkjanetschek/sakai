package edu.mci.rss.eventHandlers;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.mci.rss.model.NewsItemProcessingData;
import edu.mci.rss.utils.MciRssSessionUtils;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@EventType({AssignmentConstants.EVENT_ADD_ASSIGNMENT, AssignmentConstants.EVENT_AVAILABLE_ASSIGNMENT})
@Component
public class AssignmentEventHandler extends AbstractEventHandler {


    private final String CATEGORIE = "Assignment";


    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private MciRssSessionUtils mciRssSessionUtils;

    @Override
    protected void addToolSpecificDetails(NewsItemProcessingData itemData, Entry atomEntry) {
        System.out.println("AnnouncementEventHandler.addToolSpecificDetails");
        atomEntry.setCategories(createCategoryAsList(CATEGORIE));

        String userId = itemData.getUserId();
        UserNotification noti = itemData.getUserNotification();

        String bodyText;
        Date dueDate;
        try {
            mciRssSessionUtils.switchToUserAndOrEid(userId, null);
            Assignment assignment = assignmentService.getAssignment(entityManager.newReference(noti.getRef()));
            bodyText = assignment.getInstructions();  // USED???
            dueDate = Date.from(assignment.getDueDate());
            itemData.setDueDate(dueDate);
        } catch (IdUnusedException e) {
            log.error("IdUnusedException", e);
        } catch (PermissionException e) {
            log.error("PermissionException", e);
        } finally {
            mciRssSessionUtils.switchToUserAndOrEid(null, null);
        }

        atomEntry.setSummary(createContentObjectForSummay(buildSummary(itemData)));



    }

    protected String buildSummary(NewsItemProcessingData itemData) {
        String title = checkTitleOrDefault(itemData.getUserNotification());
        String siteTitle = shortenTitle(checkSiteIdOrDefault(itemData.getUserNotification()));
        Date dueDate = itemData.getDueDate();
        return new StringBuilder()
                .append("<p>Assignment: ")
                .append(title)
                .append(":<br/> posted in ")
                .append(siteTitle)
                .append(", due on: ")
                .append(dueDate)
                .append("</p>")
                .toString();
    }

    public String getName() {
        return "AssignmentHandler";
    }

}
