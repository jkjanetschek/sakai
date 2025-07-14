package edu.mci.rss.testing;


import edu.mci.rss.WebMvcConfiguration;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.messaging.api.UserMessagingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
@Import(WebMvcConfiguration.class)

/*
 @ComponentScan(
    basePackages = "edu.mci.rss",
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org\\.sakaiproject\\..*")
)

 */
public class TestConfiguration {  //     extents SakaiTestConfiguration --> some beans creation does not work, if test are run via mvn test


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
    public EntityManager entityManager() {
        return mock(EntityManager.class);
    }

    @Bean(name = "org.sakaiproject.assignment.api.AssignmentService")
    public AssignmentService assignmentService() {
        return mock(AssignmentService.class);
    }


    @Bean(name = "org.sakaiproject.user.api.UserDirectoryService")
    public UserDirectoryService userDirectoryService() {
        return mock(UserDirectoryService.class);
    }



}
