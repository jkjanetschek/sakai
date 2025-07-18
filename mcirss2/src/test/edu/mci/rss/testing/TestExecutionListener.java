package edu.mci.rss.testing;

import edu.mci.rss.services.NewsFeedService;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.sakaiproject.component.cover.ComponentManager;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import java.lang.reflect.Field;

@Slf4j
public class TestExecutionListener extends AbstractTestExecutionListener {
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

    @Override
    public void beforeTestClass(TestContext testContext) throws Exception {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) testContext.getApplicationContext();
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (context.containsBean("newsFeedService")) {
            NewsFeedService original = (NewsFeedService) context.getBean("newsFeedService");
            NewsFeedService spy = Mockito.spy(original);

            // Override the bean
            DefaultSingletonBeanRegistry singletonBeanRegistry = (DefaultSingletonBeanRegistry) beanFactory;
            singletonBeanRegistry.destroySingleton("newsFeedService");
            singletonBeanRegistry.registerSingleton("newsFeedService", spy);

        }
    }
}
