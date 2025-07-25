package edu.mci.rss.testing;

import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import edu.mci.rss.eventHandlers.AnnouncementEventHandler;
import edu.mci.rss.eventHandlers.AssignmentEventHandler;
import edu.mci.rss.eventHandlers.EventHandlerFactory;
import edu.mci.rss.eventHandlers.MciRssEventHandler;
import edu.mci.rss.eventHandlers.SamigoEventHandler;
import edu.mci.rss.model.NewsItemProcessingData;
import edu.mci.rss.utils.FeedUtils;
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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;


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


@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class NewsServiceTest {

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
    private EntityManager entityManager;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private SiteService siteService;


    @Before
    public void setup() {
        Mockito.reset(entityManager, assignmentService, siteService);
        testDataFactory = new MciRssTestDataFactory();
    }


    @Test
    public void testEventHandlers() {
        List<ExpectedUserNotification> notis = testDataFactory.createTestUserNotificationsList(new MciRssTestDataFactory.TestConfig(300, MciRssTestDataFactory.TimeRangeMode.SHORT, MciRssTestDataFactory.HandledEvents.ALL));
        notis.forEach(n -> {
            Assert.assertNotNull("no handler found", eventHandlerFactory.getHandlers().get(n.getEvent()));
        });

    }

    @Test
    public void testAtomFeedXMLOutput() throws PermissionException, IdUnusedException, NoSuchFieldException, IllegalAccessException, ParserConfigurationException, IOException, TransformerException, SAXException {
        MciRssTestDataFactory.CompleteNewsAtomFeedTestData testData = testDataFactory.createNewsFeedTestData();
        List<UserNotification> userNotifications = testData.userNotiList().stream()
                .map(n -> testDataFactory.createMockedUserNotificationItemFromExcepected(n))
                .toList();

        mockSetupForAnnouncementEventHandler();
        mockSetupForAssignmentEventHandler();
        mockSetupForSamigoEventHandler();

        Feed atomFeed = FeedUtils.createAtomFeed();

        List<Entry> entries = new LinkedList<>();
        for (UserNotification item : userNotifications) {

            MciRssEventHandler handler =  eventHandlerFactory.getHandlers().get(item.getEvent());
            if (handler == null) {
                throw new RuntimeException("no handler found for news item event. List of news items should be filtered also by handled events at this point");
            }
            Entry entry = handler.processEvent(new NewsItemProcessingData("placeHolderUserID", item));
            entries.add(entry);

        }
        atomFeed.setEntries(entries);

        String xmlFeed = FeedUtils.serializeAtomFeed(atomFeed);
        String oldFeedXmklTemplate = testData.atomFeedXmlTemplate();
        StringBuilder templateBuilder = new StringBuilder(oldFeedXmklTemplate);
        userNotifications.forEach(n -> {
            if (MciRssTestDataFactory.HandledEvents.ASSIGNMENT.getEventNames().contains(n.getEvent())) {

                List<String> summary = entries.stream().filter(entry -> entry.getCategories().get(0).getTerm().contains(AssignmentEventHandler.CATEGORIE))
                        .map(entry -> entry.getSummary().getValue())
                        .toList();

                replaceStringInTemplate(templateBuilder, n, "{{idAss}}", String.valueOf(n.getId()));
                replaceStringInTemplate(templateBuilder, n, "{{titleAss}}", n.getTitle());
                replaceStringInTemplate(templateBuilder, n, "{{summaryAss}}", customXmlEscape(summary.get(0)));
                replaceStringInTemplate(templateBuilder, n, "{{updatedAss}}", DateTimeFormatter.ISO_INSTANT.format(n.getEventDate()));
                replaceStringInTemplate(templateBuilder, n, "{{publishedAss}}", DateTimeFormatter.ISO_INSTANT.format(n.getEventDate()));
                replaceStringInTemplate(templateBuilder, n, "{{hrefAss}}", n.getUrl());
            } else if (MciRssTestDataFactory.HandledEvents.ANNOUNCEMENT.getEventNames().contains(n.getEvent())) {
                List<String> summary = entries.stream().filter(entry -> entry.getCategories().get(0).getTerm().contains(AnnouncementEventHandler.CATEGORIE))
                        .map(entry -> entry.getSummary().getValue())
                        .toList();

                replaceStringInTemplate(templateBuilder, n, "{{idAnnc}}", String.valueOf(n.getId()));
                replaceStringInTemplate(templateBuilder, n, "{{titleAnnc}}", n.getTitle());
                replaceStringInTemplate(templateBuilder, n, "{{summaryAnnc}}",  customXmlEscape(summary.get(0)));
                replaceStringInTemplate(templateBuilder, n, "{{updatedAnnc}}", DateTimeFormatter.ISO_INSTANT.format(n.getEventDate()));
                replaceStringInTemplate(templateBuilder, n, "{{publishedAnnc}}", DateTimeFormatter.ISO_INSTANT.format(n.getEventDate()));
                replaceStringInTemplate(templateBuilder, n, "{{hrefAnnc}}", n.getUrl());
            } else if (MciRssTestDataFactory.HandledEvents.SAMIGO.getEventNames().contains(n.getEvent())) {
                List<String> summary = entries.stream().filter(entry -> entry.getCategories().get(0).getTerm().contains(SamigoEventHandler.CATEGORIE))
                        .map(entry -> entry.getSummary().getValue())
                        .toList();
                replaceStringInTemplate(templateBuilder, n, "{{idSamigo}}", String.valueOf(n.getId()));
                replaceStringInTemplate(templateBuilder, n, "{{titleSamigo}}", n.getTitle());
                replaceStringInTemplate(templateBuilder, n, "{{summarySamigo}}",  customXmlEscape(summary.get(0)));
                replaceStringInTemplate(templateBuilder, n, "{{updatedSamigo}}", DateTimeFormatter.ISO_INSTANT.format(n.getEventDate()));
                replaceStringInTemplate(templateBuilder, n, "{{publishedSamigo}}", DateTimeFormatter.ISO_INSTANT.format(n.getEventDate()));
                replaceStringInTemplate(templateBuilder, n, "{{hrefSamigo}}", n.getUrl());
            }
        });


        Assert.assertEquals(normalizeXML(xmlFeed), normalizeXML(templateBuilder.toString()));


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

    private void replaceStringInTemplate(StringBuilder templateBuilder, UserNotification noti, String key, String newString) {
        int start = templateBuilder.indexOf(key);
        int end = start + key.length();
        templateBuilder.replace(start, end, newString);
    }


    private String customXmlEscape(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("'", "&apos;");

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
        field.set(eventHandlerFactory.getHandlers().get(MciRssTestDataFactory.HandledEvents.SAMIGO.getEventNames().get(0)), mockedPublishedAssessmentService);

    }


}
