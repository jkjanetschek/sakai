package edu.mci.rss.eventHandlers;


import lombok.Getter;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventHandlerFactory {

    @Getter
    private final Map<String, MciRssEventHandler> handlers = new HashMap<>();


    private ApplicationContext applicationContext;


    @Autowired
    public EventHandlerFactory(ApplicationContext applicationContext) {

        this.applicationContext = applicationContext;

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

}


