package edu.mci.rss.testing;


import com.rometools.rome.feed.atom.Feed;

import edu.mci.rss.services.NewsFeedService;
import edu.mci.rss.utils.FeedUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import org.springframework.test.context.ContextConfiguration;

import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = TestConfiguration.class)
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


    @Autowired
    private UserDirectoryService userDirectoryService;
    @Autowired
    private NewsFeedService newsFeedService;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        Mockito.reset(userDirectoryService, newsFeedService);
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

}
