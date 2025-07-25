package edu.mci.rss.testing;


import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.eventHandlers.EventHandlerFactory;
import edu.mci.rss.eventHandlers.MciRssEventHandler;
import edu.mci.rss.eventHandlers.SamigoEventHandler;
import edu.mci.rss.model.NewsItemProcessingData;
import edu.mci.rss.testing.MciRssTestDataFactory.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class EventHandlerTest {

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

    private MciRssTestDataFactory testDataFactory;
    @Autowired
    private EventHandlerFactory eventHandlerFactory;
    @Autowired
    private SiteService siteService;
    @Autowired
    private AssignmentService  assignmentService;
    @Autowired
    private EntityManager entityManager;

    private int sampleSize = 100;
    @Before
    public void setup() {
        Mockito.reset(entityManager, assignmentService, siteService);
        testDataFactory = new MciRssTestDataFactory();
    }


    private List<UserNotification> createUserNotficationsList(TimeRangeMode mode, HandledEvents event) {
        List<TestConfig> configs = new ArrayList<>();

        for (int i = 0; i < sampleSize; i++) {
            configs.add(new TestConfig(1,mode, event));
        }
        List<UserNotification> userNotifications = new ArrayList<>();
        configs.forEach(c -> userNotifications.add(testDataFactory.createMockedUserNotificationItem(c)));
        return userNotifications;
    }

    private void assertGeneralEntry(Entry entry ) {
        Assert.assertNotNull(entry.getId());
        Assert.assertNotNull(entry.getTitleEx().getValue());
        Assert.assertEquals("text", entry.getTitleEx().getType());
        Assert.assertNotNull(entry.getPublished());
        Assert.assertNotNull(entry.getUpdated());
        Assert.assertFalse(entry.getAlternateLinks().isEmpty());
    }

    @Test
    public void testAnnouncementEventHandler()  {
        mockSetupForAnnouncementEventHandler();

        List<UserNotification> userNotifications = createUserNotficationsList(TimeRangeMode.ALL, HandledEvents.ANNOUNCEMENT);

        for(UserNotification userNotification : userNotifications) {
            MciRssEventHandler handler = eventHandlerFactory.getHandlers().get(userNotification.getEvent());
            NewsItemProcessingData itemData = new NewsItemProcessingData("test-1", userNotification);
            Entry entry = handler.processEvent(itemData);

            assertGeneralEntry(entry);

            Assert.assertEquals("Announcement", entry.getCategories().get(0).getTerm());
            Assert.assertNotNull( entry.getSummary().getValue());
            Assert.assertEquals("html", entry.getSummary().getType());

        }
    }

    @Test
    public void testAssignmentEventHandler() throws PermissionException, IdUnusedException {
        mockSetupForAssignmentEventHandler();

        List<UserNotification> userNotifications = createUserNotficationsList(TimeRangeMode.ALL, HandledEvents.ASSIGNMENT);

        List<Entry> entries = new ArrayList<>();
        for(UserNotification userNotification : userNotifications) {
            MciRssEventHandler handler = eventHandlerFactory.getHandlers().get(userNotification.getEvent());
            NewsItemProcessingData itemData = new NewsItemProcessingData("test-1", userNotification);
            Entry entry = handler.processEvent(itemData);

            assertGeneralEntry(entry);
            Assert.assertEquals("Assignment", entry.getCategories().get(0).getTerm());
            Assert.assertNotNull( entry.getSummary().getValue());
            Assert.assertEquals("html", entry.getSummary().getType());
            entries.add(entry);
        }
        Assert.assertEquals(sampleSize, entries.size());
    }


    @Test
    public void testSamigoEventHandler() throws PermissionException, IdUnusedException, IllegalAccessException, NoSuchFieldException {
        mockSetupForSamigoEventHandler();



        List<UserNotification> userNotifications = createUserNotficationsList(TimeRangeMode.ALL, HandledEvents.SAMIGO);

        for(UserNotification userNotification : userNotifications) {
            MciRssEventHandler handler = eventHandlerFactory.getHandlers().get(userNotification.getEvent());
            NewsItemProcessingData itemData = new NewsItemProcessingData("test-1", userNotification);
            Entry entry = handler.processEvent(itemData);

            assertGeneralEntry(entry);
            Assert.assertEquals("Assessment", entry.getCategories().get(0).getTerm());
            Assert.assertNotNull( entry.getSummary().getValue());
            Assert.assertEquals("html", entry.getSummary().getType());

        }

    }


    protected void mockSetupForAnnouncementEventHandler() {
        when(siteService.getSiteDisplay(anyString())).thenAnswer(
                invocation -> "(" + invocation.getArgument(0) + ")");

    }
    protected void mockSetupForAssignmentEventHandler() throws PermissionException, IdUnusedException {
        when(siteService.getSiteDisplay(anyString())).thenAnswer(
                invocation -> "(" + invocation.getArgument(0) + ")");
        when(entityManager.newReference(anyString())).thenReturn(mock(Reference.class));
        when(assignmentService.getAssignment(any(Reference.class))).thenAnswer(invocation -> {
            Assignment assMock = mock(Assignment.class);
            when(assMock.getDueDate()).thenReturn(Instant.now());
            return assMock;
        });

    }
    protected void mockSetupForSamigoEventHandler() throws PermissionException, IdUnusedException, NoSuchFieldException, IllegalAccessException {
        when(siteService.getSiteDisplay(anyString())).thenAnswer(
                invocation -> "(" + invocation.getArgument(0) + ")");
        when(entityManager.newReference(anyString())).thenReturn(mock(Reference.class));
        when(assignmentService.getAssignment(any(Reference.class))).thenAnswer(invocation -> {
            Assignment assMock = mock(Assignment.class);
            when(assMock.getDueDate()).thenReturn(Instant.now());
            return assMock;
        });
        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        PublishedAssessmentService mockedPublishedAssessmentService = spy(publishedAssessmentService);
        doAnswer(a -> {
            PublishedAssessmentData mockData = mock(PublishedAssessmentData.class);
            when(mockData.getDueDate()).thenReturn(new Date());
            return mockData;
        }).when(mockedPublishedAssessmentService).getBasicInfoOfPublishedAssessment(anyString());

        Field field = SamigoEventHandler.class.getDeclaredField("publishedAssessmentService");
        field.setAccessible(true);
        field.set(eventHandlerFactory.getHandlers().get(HandledEvents.SAMIGO.getEventNames().get(0)), mockedPublishedAssessmentService);

    }



}
