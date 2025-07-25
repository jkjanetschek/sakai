package edu.mci.rss.testing;


import com.rometools.rome.feed.atom.Feed;
import edu.mci.rss.HandledCalendarTools;
import edu.mci.rss.services.CalendarFeedService;
import edu.mci.rss.utils.FeedUtils;
import edu.mci.rss.utils.NewsItemFilterCriteriaUtils;
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
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestConfiguration.class})
public class CalendarFeedServiceTest {


    @Autowired
    private CalendarFeedService calendarFeedService;
    @Autowired
    private SiteService siteService;
    private MciRssTestDataFactory  mciRssTestDataFactory;


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

    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private PublishedAssessmentService publishedAssessmentService;
    @Autowired
    private EntityManager entityManager;


    @Before
    public void setup() {
        Mockito.reset(assignmentService, publishedAssessmentService, siteService);
        mciRssTestDataFactory = new MciRssTestDataFactory();
    }

    @Test
    public void testCreateEmptyFeedForUserId() throws ParserConfigurationException, IOException, TransformerException, SAXException {
        stubSiteServiceFetchUSerSites(10);
        Feed feed = calendarFeedService.createFeedForUserId("placeHolderUserId");
        Assert.assertNotNull(feed);
        Feed emptyFeed = FeedUtils.createAtomFeed();
        Assert.assertEquals(normalizeXML(FeedUtils.serializeAtomFeed(emptyFeed)), normalizeXML(FeedUtils.serializeAtomFeed(feed)));
    }




    @Test
    public void getEntriesForUserInAssignments() throws NoSuchFieldException, IllegalAccessException {
        stubSiteServiceFetchUSerSites(10);
        stubAssignmentServiceFetchAssignemnt(20);
        stubAssessmentServiceFetchPublishedAssessments(20);
        Feed feed = calendarFeedService.createFeedForUserId("placeHolderUserId");
        Assert.assertNotNull(feed);
    }


    @Test
    public void checkItemsTimeRangeForSamigoForNullDueDate() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        List<PublishedAssessmentFacade> pubList = mciRssTestDataFactory.createMockPublishedAssessmentFacadeWithNoDueDate(10);

        Class<?> clazz = CalendarFeedService.class;
        Class<?> filterDetailsClass =  Arrays.stream(clazz.getDeclaredClasses()).filter(clazz1 -> clazz1.getSimpleName().contains("FilterDetails"))
                .findFirst().orElseThrow(() -> new AssertionError("FilterDetails not found"));
        Constructor<?> constructor = filterDetailsClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Object filterDetailsInstance = constructor.newInstance(pubList,
                HandledCalendarTools.SAMIGO,
                new NewsItemFilterCriteriaUtils(entityManager, assignmentService));
        @SuppressWarnings("unchecked")
        List<PublishedAssessmentFacade> result = (List<PublishedAssessmentFacade>)
                ReflectionTestUtils.invokeMethod(calendarFeedService, "checkItemsTimeRangeForSamigo", filterDetailsInstance);
        Assert.assertTrue(result.isEmpty());
    }




    private String normalizeXML(String xml) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(xml)));
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        StringWriter out = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(out));
        return out.toString().trim();
    }

    private void stubAssignmentServiceFetchAssignemnt(int howMany) {
        when(assignmentService.getAssignmentsForContext(anyString()))
                .thenAnswer(i -> {
                    List<Assignment> assignments = new ArrayList<>();
                    for (int index = 0; index < howMany; index++) {
                        Assignment a = mock(Assignment.class);
                        final Map<String, Instant> state = new HashMap<>();
                        int dateOffset = ThreadLocalRandom.current().nextInt(1, (int) Duration.ofSeconds(NewsItemFilterCriteriaUtils.TIME_RANGE_CALENDAR_SECONDS + 10).toDays());
                        Instant dueDate = createInstantMinusGivenDays(dateOffset);
                        state.put("dueDate", dueDate);
                        when(a.getDueDate()).thenReturn(state.get("dueDate"));
                        assignments.add(a);
                    }
                    return assignments;
                });
    }



    private void stubAssessmentServiceFetchPublishedAssessments(int howMany) throws NoSuchFieldException, IllegalAccessException {
        ReflectionTestUtils.setField(calendarFeedService, "publishedAssessmentService", publishedAssessmentService);
        when(publishedAssessmentService.getBasicInfoOfAllPublishedAssessments(any(), anyString(), anyBoolean(), anyString()))
                .thenAnswer(i -> {
                    List<PublishedAssessmentFacade> assessments = new ArrayList<>();
                    for (int index = 0; index < howMany; index++) {
                        PublishedAssessmentFacade a = mock(PublishedAssessmentFacade.class);
                        Map<String, Date> state = new HashMap<>();
                        int dateOffset = ThreadLocalRandom.current().nextInt(1, (int) Duration.ofSeconds(NewsItemFilterCriteriaUtils.TIME_RANGE_CALENDAR_SECONDS + 10).toDays());
                        Instant dueDate = createInstantMinusGivenDays(dateOffset);
                        int startDateOffset = ThreadLocalRandom.current().nextInt(dateOffset, (int) Duration.ofSeconds(NewsItemFilterCriteriaUtils.TIME_RANGE_CALENDAR_SECONDS + 10).toDays());
                        Instant startDate = dueDate.minus(startDateOffset, ChronoUnit.DAYS);
                        state.put("dueDate", Date.from(dueDate));
                        state.put("startDate", Date.from(startDate));
                        when(a.getDueDate()).thenReturn(state.get("dueDate"));
                        when(a.getStartDate()).thenReturn(state.get("startDate"));
                        assessments.add(a);
                    }
                    return assessments;
                });
    }




    private Instant createInstantMinusGivenDays(int days) {
        return  Instant.now().minus(days, ChronoUnit.DAYS);
    }

    private void stubSiteServiceFetchUSerSites(int howManySite) {
        when(siteService.getUserSites(anyBoolean(),anyString())).thenAnswer(i -> {
            return mciRssTestDataFactory.createDummySiteObjects(howManySite);
        });
    }
}
