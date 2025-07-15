package edu.mci.rss.testing;

import edu.mci.rss.eventHandlers.EventHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.cover.ComponentManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {TestConfiguration.class})
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


    private UserNotificationTestDataFactory testDataFactory;
    @Autowired
    private EventHandlerFactory eventHandlerFactory;


    @Before
    public void setup() {
        testDataFactory = new UserNotificationTestDataFactory();
    }


    @Test
    public void testEventHandlers() {
        List<ExpectedUserNotification> notis = testDataFactory.createTestUserNotificationsList(new UserNotificationTestDataFactory.TestConfig(300, UserNotificationTestDataFactory.TimeRangeMode.SHORT, UserNotificationTestDataFactory.HandledEvents.ALL));
        notis.forEach(n -> {
            Assert.assertNotNull("no handler found", eventHandlerFactory.getHandlers().get(n.getEvent()));
        });


    }

}
