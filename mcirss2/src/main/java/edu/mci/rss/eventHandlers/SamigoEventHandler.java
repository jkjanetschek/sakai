package edu.mci.rss.eventHandlers;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.mci.rss.model.NewsItemProcessingData;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.assessment.TestsAndQuizzesUserNotificationHandler;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Slf4j
@EventType({SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE })
@Component
public class SamigoEventHandler extends AbstractEventHandler {


    private final String CATEGORIE = "Assessment";

    private PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();

    @Override
    protected void addToolSpecificDetails(NewsItemProcessingData itemData, Entry atomEntry) {
        System.out.println("AnnouncementEventHandler.addToolSpecificDetails");

        atomEntry.setCategories(createCategoryAsList(CATEGORIE));

        UserNotification noti = itemData.getUserNotification();
        List<String> refParts = TestsAndQuizzesUserNotificationHandler.regexHelper(noti.getRef());

        Date dueDate;
        try {
            String publishedAssessmentId = refParts.get(1);
            PublishedAssessmentData pubData = publishedAssessmentService.getBasicInfoOfPublishedAssessment(publishedAssessmentId);
            dueDate = pubData.getDueDate();
            if (dueDate != null ) {
                itemData.setDueDate(dueDate);
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            log.warn("Error getting dueDate for assessment ref {}", noti.getRef());
        }
        atomEntry.setSummary(createContentObjectForSummay(buildSummary(itemData)));
    }

    protected String buildSummary(NewsItemProcessingData itemData) {
        String title = checkTitleOrDefault(itemData.getUserNotification());
        String siteTitle = shortenTitle(checkSiteIdOrDefault(itemData.getUserNotification()));
        Date dueDate = itemData.getDueDate();
        StringBuilder sb = new StringBuilder();
        sb.append("<p>Assessment: ")
            .append(title)
            .append(":<br/> posted in ")
            .append(siteTitle);
        if (dueDate != null) {
            sb.append(", due on: ")
                    .append(dueDate)
                    .append("</p>");

        } else {
            sb.append("</p>");
        }

        return sb.toString();
    }

    public String getName() {
        return "SamigoHandler";
    }


}
