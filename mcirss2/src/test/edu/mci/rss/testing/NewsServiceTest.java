package edu.mci.rss.testing;

import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.component.cover.ComponentManager;

import java.lang.reflect.Field;

@Slf4j
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {ConversationsTestConfiguration.class})
public class NewsServiceTest {

    // mockito      3.12.4
    // junit        4.13.2
    // hamcrest     1.3
    // spring       5.3.39


 //   @Before
    public void initCoponentmanager() throws NoSuchFieldException, IllegalAccessException {

        Class<?> clazz = ComponentManager.class;
        Field testingModeField = clazz.getDeclaredField("testingMode");
        testingModeField.setAccessible(true);
        testingModeField.set(null, true);
        ComponentManager.getInstance();

    }


  //  @Test
    public void testNewsService() {
        System.out.println("testNewsService");
    }

}
