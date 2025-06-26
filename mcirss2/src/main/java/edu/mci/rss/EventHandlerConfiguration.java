package edu.mci.rss;


import edu.mci.rss.eventHandlers.AnnouncementEventHandler;
import edu.mci.rss.eventHandlers.AssignmentEventHandler;
import edu.mci.rss.eventHandlers.MciRssEventHandler;
import edu.mci.rss.eventHandlers.SamigoEventHandler;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventHandlerConfiguration {


/*
    @Bean({AnnouncementService.SECURE_ANNC_ADD})
    public MciRssEventHandler announcementHandler() {
        return new AnnouncementEventHandler();
    }


    @Bean({AssignmentConstants.EVENT_ADD_ASSIGNMENT, AssignmentConstants.EVENT_AVAILABLE_ASSIGNMENT})
    public MciRssEventHandler assignmentHandler() {
        return new AssignmentEventHandler();
    }



    @Bean({SamigoConstants.EVENT_ASSESSMENT_AVAILABLE, SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE })
    public MciRssEventHandler samigoHandler() {
        return new SamigoEventHandler();
    }


 */

}
