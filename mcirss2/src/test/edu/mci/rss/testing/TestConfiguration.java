package edu.mci.rss.testing;


import edu.mci.rss.WebMvcConfiguration;
import edu.mci.rss.calendarNews.AssignmentCalendarNewsHandler;
import edu.mci.rss.calendarNews.CalendarNewsHandler;
import edu.mci.rss.calendarNews.SamigoCalendarNewsHandler;
import edu.mci.rss.services.CalendarFeedService;
import edu.mci.rss.services.NewsFeedService;
import edu.mci.rss.utils.MciRssSessionUtils;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.UserDirectoryService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;



@Configuration
@Import(WebMvcConfiguration.class)
public class TestConfiguration {



    @Bean
    @Primary
    public NewsFeedService newsFeedService() {
        NewsFeedService newsFeedService = new NewsFeedService();
        return spy(newsFeedService);
    }

    @Bean
    @Primary
    public CalendarFeedService calendarFeedService() {
        CalendarFeedService calendarFeedService = new CalendarFeedService();
        return spy(calendarFeedService);
    }

    @Bean
    public CalendarNewsHandler assignmentCalendarNewsHandler() {
        AssignmentCalendarNewsHandler assignmentCalendarNewsHandler = new AssignmentCalendarNewsHandler();
        return spy(assignmentCalendarNewsHandler);
    }
    @Bean
    public CalendarNewsHandler samigoCalendarNewsHandler() {
        SamigoCalendarNewsHandler samigoCalendarNewsHandler = new SamigoCalendarNewsHandler();
        return spy(samigoCalendarNewsHandler);
    }



    @Bean(name = "org.sakaiproject.site.api.SiteService")
    public SiteService siteService() {
        return mock(SiteService.class);
    }

    @Bean(name = "org.sakaiproject.tool.api.SessionManager")
    public SessionManager sessionManager() {
        return mock(SessionManager.class);
    }

    @Bean(name = "org.sakaiproject.messaging.api.UserMessagingService")
    public UserMessagingService userMessagingService() {
        return mock(UserMessagingService.class);
    }


    @Bean(name = "org.sakaiproject.entity.api.EntityManager")
    public EntityManager entityManager() {return mock(EntityManager.class);}

    @Bean(name = "org.sakaiproject.assignment.api.AssignmentService")
    public AssignmentService assignmentService() {
        return mock(AssignmentService.class);
    }

    @Bean()
    public PublishedAssessmentService publishedAssessmentService() {return mock(PublishedAssessmentService.class);}


    @Bean(name = "org.sakaiproject.user.api.UserDirectoryService")
    public UserDirectoryService userDirectoryService() {
        return mock(UserDirectoryService.class);
    }

    @Bean
    public MciRssSessionUtils mciRssSessionUtils() {return mock(MciRssSessionUtils.class);}



}
