package edu.mci.rss.testing;


import edu.mci.rss.utils.NewsItemFilterCriteriaUtils;
import edu.mci.rss.testing.MciRssTestDataFactory.TestConfig;
import edu.mci.rss.testing.MciRssTestDataFactory.TimeRangeMode;
import edu.mci.rss.testing.MciRssTestDataFactory.HandledEvents;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;



import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration // weil --> AnnotationConfigWebApplicationContext
@ContextConfiguration(classes = {TestConfiguration.class})
public class UtilsTest {

    @Autowired
    EntityManager entityManager;
    @Autowired
    AssignmentService assignmentService;
    @Autowired
    NewsItemFilterCriteriaUtils filterCriteriaUtils;



    private Instant now;
    private MciRssTestDataFactory testDataFactory;


    // init ComponentManager in testing mode before dependecies are resolved
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
    public void setUp() {
        // Mock the current time
        now = Instant.now();
        testDataFactory = new MciRssTestDataFactory();

    }

/**
 * Filter NewsItems according to the following rules: If no newsitems within
 * the TIME_RANGE_SHORT (2 weeks), display up to ten items from
 * TIME_RANGE_LONG. Else display only items from TIME_RANGE_SHORT.
*/
    @Test
    public void testNewsWithinShortTimeRage () {

        Assert.assertTrue("Time should be in time Range Short",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(5)));

        Instant before14Days = createInstantMinusGivenDays(14);
        Instant timeRangeShort = filterCriteriaUtils.getTimeRangeShortInstant();
        Duration leeway = Duration.ofMillis(30000);
        Duration diff =Duration.between(timeRangeShort, before14Days).abs();

        if (diff.compareTo(leeway) > 0) {
            Assert.fail("Difference between timeRangeShort and news Items from 14 days ago is bigger than leeway of 30 seconds. You should probably buy faster PC");
        }

        Assert.assertTrue("Time should be in time Range Short",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(now));

        Assert.assertFalse("Time should not be returned as to add because of time previously set to Short",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(16)));
    }


    @Test
    public void testNewsWithinLongTimeRage () {

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(15)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(17)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(44)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(63)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(87)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(111)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(123)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(123)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(145)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(55)));

        Instant before180Days = createInstantMinusGivenDays(180);
        Instant timeRangeLong = filterCriteriaUtils.getTimeRangeLongInstant();
        Duration leeway = Duration.ofMillis(30000);
        Duration diff =Duration.between(timeRangeLong, before180Days).abs();

        if (diff.compareTo(leeway) > 0) {
            Assert.fail("Difference between timeRangeLong and news Items from 180 days ago is bigger than leeway of 30 seconds");
        }

        Assert.assertFalse("Item should not be added as count of 10 longTimeRange items is reached",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(22)));

    }

    private Instant createInstantMinusGivenDays(int days) {
        return now.minus(days, ChronoUnit.DAYS);
    }
    private Instant createInstantMinusGivenSeconds(long seconds) {
        Instant instant = now.minus(seconds, ChronoUnit.SECONDS);
        System.out.println("createInstantMinusGivenSeconds: " + Date.from(instant));
        return instant;}

    @Test
    public void testGetEventTimeFromUserNotifications() throws PermissionException, IdUnusedException, NoSuchFieldException, IllegalAccessException {

        //stubbing
        FakeMCIReference mockSpy = spy(FakeMCIReference.class);
        when(entityManager.newReference(anyString())).thenAnswer(invocation -> {
            String input = invocation.getArgument(0);
            mockSpy.setRef(input);
            return mockSpy;
        });



        TestConfig config = new TestConfig(null, TimeRangeMode.ALL, HandledEvents.ANNOUNCEMENT);
        UserNotification userNoti = testDataFactory.createMockedUserNotificationItem(config);
        assertEquals(userNoti.getEventDate(), filterCriteriaUtils.getEventTimeFromUserNotifications(userNoti));



        TestConfig config2 = new TestConfig(null, TimeRangeMode.ALL, HandledEvents.ASSIGNMENT);
        UserNotification userNoti2 = testDataFactory.createMockedUserNotificationItem(config);
        when(assignmentService.getAssignment(any(Reference.class))).thenAnswer(invocation -> {
            Assignment ass = mock(Assignment.class);
            ass.setDueDate(userNoti2.getEventDate());
            return ass;
        });
        assertEquals(userNoti.getEventDate(), filterCriteriaUtils.getEventTimeFromUserNotifications(userNoti));


        TestConfig config3 = new TestConfig(null, TimeRangeMode.ALL, HandledEvents.SAMIGO);
        UserNotification userNoti3 = testDataFactory.createMockedUserNotificationItem(config);

        PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        PublishedAssessmentService mockedPublishedAssessmentService = spy(publishedAssessmentService);
        PublishedAssessmentData pubDataDueDate =  new PublishedAssessmentData(userNoti3.getId(),
                userNoti3.getTitle(),
                "releaseToPlaceholder",
                Date.from(userNoti3.getEventDate()), null, null);

        doReturn(pubDataDueDate)
            .when(mockedPublishedAssessmentService)
                .getBasicInfoOfPublishedAssessment(anyString());

        Field field = NewsItemFilterCriteriaUtils.class.getDeclaredField("publishedAssessmentService");
        field.setAccessible(true);
        field.set(filterCriteriaUtils, mockedPublishedAssessmentService);
        assertEquals(userNoti3.getEventDate(), filterCriteriaUtils.getEventTimeFromUserNotifications(userNoti3));

    }




    @Test
    public void testListOfSortedItems() {
        List<UserNotification> notis = new ArrayList<UserNotification>();
        List<UserNotification> notisSpy = spy(notis);

        // Create test configurations
        /*
        List<TestConfig> configs = List.of(
                new TestConfig(1, TimeRangeMode.LONG, HandledEvents.ASSIGNMENT),
                new TestConfig(1, TimeRangeMode.SHORT, HandledEvents.ANNOUNCEMENT),
                new TestConfig(1, TimeRangeMode.LONG_BOUNDARY, HandledEvents.SAMIGO),
                new TestConfig(1, TimeRangeMode.SHORT_BOUNDARY, HandledEvents.ALL)
        );
        configs.forEach(config -> {
            notisSpy.add(testDataFactory.createMockedUserNotificationItem(config));
        });

         */
        for(int i = 0; i < 100; i++) {
            notisSpy.add(testDataFactory.createMockedUserNotificationItem(new TestConfig(null, TimeRangeMode.ALL, HandledEvents.ALL)));
        }

        NewsItemFilterCriteriaUtils filterSpy = spy(filterCriteriaUtils);

        doAnswer(invocation -> {
            UserNotification input = (UserNotification) invocation.getArguments()[0];
            return input.getEventDate();
        })
        .when(filterSpy)
        .getEventTimeFromUserNotifications(any(UserNotification.class));

        List<UserNotification> sortedList =  filterSpy.filterByEventAndSortByTime(notisSpy);
        Iterator<UserNotification> it = sortedList.iterator();
        Instant leftOperand = it.next().getEventDate();
        while (it.hasNext()) {
            Instant rightOperand = it.next().getEventDate();
            Assert.assertTrue("NewsItemFilterCriteriaUitls.filterByEventAndSortByTime: sorted items are not in correct order", leftOperand.isBefore(rightOperand));
            leftOperand = rightOperand;
        }
    }


    public static abstract class FakeMCIReference implements Reference {
        private String ref;

        public FakeMCIReference(String ref) {this.ref = ref;}
        public FakeMCIReference() {}

        public String getRef() {return this.ref;}
        public void setRef(String ref) {this.ref = ref;}
    }

    @Test
    public void testCheckTimeRangeForAssignmentAndCalendar() {
        Assert.assertTrue(filterCriteriaUtils.checkTimeRangeForCalendar(createInstantMinusGivenSeconds(filterCriteriaUtils.TIME_RANGE_CALENDAR_SECONDS - 1)));
        Assert.assertFalse(filterCriteriaUtils.checkTimeRangeForCalendar(createInstantMinusGivenSeconds(filterCriteriaUtils.TIME_RANGE_CALENDAR_SECONDS + 1)));

    }






}
