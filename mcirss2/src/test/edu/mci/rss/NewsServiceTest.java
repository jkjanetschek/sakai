package edu.mci.rss;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@Slf4j
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {ConversationsTestConfiguration.class})
public class NewsServiceTest {

    // mockito      3.12.4
    // junit        4.13.2
    // hamcrest     1.3
    // spring       5.3.39

    @Before
    public void setup() {
        System.out.println("@Before in NewsServiceTest.class");
    }

    @Test
    public void testNewsService() {
        System.out.println("testNewsService");
    }

}
