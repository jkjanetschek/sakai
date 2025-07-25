package edu.mci.rss.testing;


import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.calendarNews.CalendarNewsHandler;
import edu.mci.rss.services.CalendarFeedService;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.cover.ComponentManager;

import org.sakaiproject.site.api.Site;

import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestConfiguration.class})
public class CalendarNewsHandlerTest {



    @Autowired
    @Qualifier("assignmentCalendarNewsHandler")
    private CalendarNewsHandler assignmentNewsHandler;

    @Autowired
    @Qualifier("samigoCalendarNewsHandler")
    private CalendarNewsHandler samigoCalendarNewsHandler;

    @Autowired
    private AssignmentService assignmentService;

    private MciRssTestDataFactory mciRssTestDataFactory;


    static {
        try {
            Class<?> clazz = ComponentManager.class;
            Field testingModeField = clazz.getDeclaredField("testingMode");
            testingModeField.setAccessible(true);
            testingModeField.set(null, true);
            ComponentManager.getInstance();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("ComponentManager could not set to testing mode for Junit Run", e);
        }
    }

    @Before
    public void setup() {
        Mockito.reset(assignmentService, samigoCalendarNewsHandler,assignmentNewsHandler);
        this.mciRssTestDataFactory = new MciRssTestDataFactory();

    }

    @Test
    public void testAssignmentCalendarNewsHandler() throws Exception {
        stubAssignmentServiceFetchDeepLink();
        List<Entry> entries = new ArrayList<>();
        for(Site site : mciRssTestDataFactory.createDummySiteObjects(10)) {
                List<Assignment> assignmentList = mciRssTestDataFactory.createMockAssignmentsWithinTimeRange(20);
                Assert.assertFalse(assignmentList.isEmpty());
                assignmentList.forEach(assignment -> assignment.setContext(site.getId()));
                entries.addAll(assignmentNewsHandler.processCalendarNewsItems(assignmentList,
                                                                                new CalendarFeedService.CalendarNewsItemData(site,
                                                                                        "placeholderUserID")));
        }
        Assert.assertFalse(entries.isEmpty());
        entries.forEach(entry -> {
            Assert.assertNotNull(entry.getId());
            Assert.assertNotNull(entry.getSummary());
            Assert.assertNotNull(entry.getTitle());
            Assert.assertNotNull(entry.getAlternateLinks());
            Assert.assertNotNull(entry.getUpdated());
        });
    }

    private void stubAssignmentServiceFetchDeepLink() throws Exception {
        when(assignmentService.getDeepLink(any(), anyString(), anyString()))
                .thenAnswer(i -> mciRssTestDataFactory.createMockAssignemntDeeplink((String) i.getArgument(0),
                          (String) i.getArgument(1),
                          (String) i.getArgument(2)));

    }


    @Test
    public void testSamigoCalendarNewsHandler() throws Exception {
        stubAssignmentServiceFetchDeepLink();
        List<Entry> entries = new ArrayList<>();
        for (Site site : mciRssTestDataFactory.createDummySiteObjects(10)) {
            List<PublishedAssessmentFacade> pubs = mciRssTestDataFactory.createMockPublishedAssessmentFacadeWithinTimeRange(20);
            Assert.assertFalse(pubs.isEmpty());
            entries.addAll(samigoCalendarNewsHandler.processCalendarNewsItems(pubs,
                    new CalendarFeedService.CalendarNewsItemData(site, "placeholderUserID")));

        }
        Assert.assertFalse(entries.isEmpty());
        entries.forEach(entry -> {
            Assert.assertNotNull(entry.getCategories().get(0).getTerm());
            Assert.assertNotNull(entry.getId());
            Assert.assertNotNull(entry.getSummary().getValue());
            Assert.assertNotNull(entry.getTitle());
            Assert.assertNotNull(entry.getAlternateLinks());
            Assert.assertNotNull(entry.getUpdated());
            Assert.assertNotNull(entry.getPublished());
            Assert.assertTrue(entry.getPublished().before(entry.getUpdated()));
        });
    }
}
