package edu.mci.rss.eventHandlers;


import lombok.Getter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class EventHandlerFactory {

    @Getter
    private final Map<String, MciRssEventHandler> handlers = new HashMap<>();


    private ApplicationContext applicationContext;
/*
    @Autowired
    public EventHandlerFactory(Map<String, MciRssEventHandler> handlers) {
        this.handlers.putAll(handlers);
        //TODO
        System.out.println("EventHandlerFactory()");
        handlers.forEach((k,v) -> System.out.println(k + ": " + v));
    }

*/



    @Autowired
    public EventHandlerFactory(ApplicationContext applicationContext) {

        this.applicationContext = applicationContext;
/*
        System.out.println("EventHandlerFactory method#1()");
        String[] names = applicationContext.getBeanNamesForType(MciRssEventHandler.class);
        for (String name : names) {
            MciRssEventHandler handler = (MciRssEventHandler) applicationContext.getBean(name, MciRssEventHandler.class);
            //handlers.put(name, handler);
            System.out.println(name + "   :   "  + handler.toString());
            String[] aliases = applicationContext.getAliases(name);
            for (String alias : aliases) {
                //handlers.put(alias, handler);
                System.out.println(alias + "  :     " + handler.toString());
            }
        }
*/
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EventType.class);
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            Object beanInstance = entry.getValue();
            Class<?> clazz = AopUtils.getTargetClass(beanInstance);
            EventType eventType = clazz.getAnnotation(EventType.class);
            Arrays.stream(eventType.value()).forEach(event -> {
                if (beanInstance instanceof MciRssEventHandler handler) {
                    handlers.put(event, handler);
                }
            });
        }

    }

/*
    @PostConstruct
    public void postConstruct() {
        System.out.println("postConstruct()");
        handlers.forEach(
                (key, handler) -> {
                    System.out.println("key: " + key + " handler: " + handler.getClass().getName());
                }
        );
    }

 */


}


