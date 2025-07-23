package edu.mci.rss.testing;


import com.rometools.rome.feed.atom.Feed;
import edu.mci.rss.controllers.MciRssController;
import edu.mci.rss.services.NewsFeedService;
import edu.mci.rss.utils.FeedUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration // weil --> AnnotationConfigWebApplicationContext
@ContextConfiguration(classes = TestConfiguration.class)
/*
@TestExecutionListeners(
        listeners = TestExecutionListener.class,
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)

 */
public class MciRssControllerTest {

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

    /*
    Best Practices

    Exception Handling:
        Always test the exception handlers to ensure proper error responses.

    Use MockMvc:
        This ensures the controller is tested in isolation without requiring a full application context.

    Descriptive Test Names:
        Use clear and concise test method names to describe the behavior being tested.

    Verify Mock Interactions:
        Use Mockito.verify() to check that the mocked services are called as expected.

     */

    @Autowired
    private UserDirectoryService userDirectoryService;
    @Autowired
    private NewsFeedService newsFeedService;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }


    @Test
    public void testGetNews_successEmptyFeed() throws Exception {

        String eid = "someEid";
        String userId = "someUserId";

        Feed mockFeed = new Feed();
        mockFeed.setFeedType("atom_1.0");

        String expectedResultString = FeedUtils.serializeAtomFeed(mockFeed);
        doAnswer(invocation ->  mockFeed)
        .when(newsFeedService).createFeedForUserId(anyString());

        when(userDirectoryService.getUserId(anyString())).thenReturn(userId);
        when(newsFeedService.createFeedForUserId(anyString())).thenReturn(mockFeed);

        Assert.assertNotNull(expectedResultString);
        mockMvc.perform(get("/news/user/" + eid))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_ATOM_XML))
                .andExpect(MockMvcResultMatchers.content().string(expectedResultString));

    }

    @Test
    public void testGetNews_HandlerNotFound() throws Exception {
        mockMvc.perform(get("/news/user/"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testGetNews_UserNotDefined() throws Exception {
        when(userDirectoryService.getUserId(anyString())).thenReturn(null);
        mockMvc.perform(get("/news/user/someEid"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }




/*
    content().xml(String expectedXml)
    String expectedResponse = FeedUtils.serializeAtomFeed(mockFeed);
XmlAssert.assertThat(actualResponse).and(expectedResponse).areIdentical();

*/
}
