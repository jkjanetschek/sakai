package edu.mci.rss.testing;


import com.rometools.rome.feed.atom.Feed;
import edu.mci.rss.services.CalendarFeedService;
import edu.mci.rss.utils.FeedUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
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
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
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


    @Before
    public void setup() {
        mciRssTestDataFactory = new MciRssTestDataFactory();
    }

    @Test
    public void testCreateFeedForUserId() throws ParserConfigurationException, IOException, TransformerException, SAXException {
        stubSiteService(10);
        Feed feed = calendarFeedService.createFeedForUserId("placeHolderUSerId");
        Assert.assertNotNull(feed);
        Feed emptyFeed = FeedUtils.createAtomFeed();
        Assert.assertEquals(normalizeXML(FeedUtils.serializeAtomFeed(emptyFeed)), normalizeXML(FeedUtils.serializeAtomFeed(feed)));
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

    private void stubSiteService(int howManySite) {
        when(siteService.getUserSites(anyBoolean(),anyString())).thenAnswer(i -> {
            return mciRssTestDataFactory.createDummySiteObjects(howManySite);
        });
    }
}
