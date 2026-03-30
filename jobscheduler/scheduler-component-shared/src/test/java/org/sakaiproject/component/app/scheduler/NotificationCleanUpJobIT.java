package org.sakaiproject.component.app.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.app.scheduler.config.NotificationCleanUpJobConfig;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.messaging.api.repository.UserNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = NotificationCleanUpJobConfig.class)
@Transactional
public class NotificationCleanUpJobIT {


    @Autowired
    private UserNotificationRepository repository;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    public void findAllDistinctToUser_ReturnsDistinctUsers() {
        Session session = sessionFactory.getCurrentSession();
        Instant now = Instant.now();

        UserNotification noti1 = new UserNotification();
        noti1.setToUser("user1");
        noti1.setFromUser("admin");
        noti1.setEvent("test");
        noti1.setEventDate(Instant.now());
        noti1.setRef("testRef");
        noti1.setUrl("placeholderUrl1");

        UserNotification noti1_1 = new UserNotification();
        noti1_1.setToUser("user1");
        noti1_1.setFromUser("admin");
        noti1_1.setEvent("test");
        noti1_1.setEventDate(Instant.now());
        noti1_1.setRef("testRef");
        noti1_1.setUrl("placeholderUrl1_1");

        UserNotification noti2 = new UserNotification();
        noti2.setToUser("user2");
        noti2.setFromUser("admin");
        noti2.setEvent("test");
        noti2.setEventDate(Instant.now());
        noti2.setRef("testRef");
        noti2.setUrl("placeholderUrl2");

        session.save(noti1);
        session.save(noti1_1);
        session.save(noti2);

        session.flush();


        List<String> result = repository.findAllDistinctToUser();

        Assert.assertEquals(2, result.size());
    }

    @Test
    public void findAllDistinctToUser_EmptyDatabase_ReturnsEmpty() {
        List<String> result = repository.findAllDistinctToUser();
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getIdsToDeleteByUserIdAndToolPrefix_ReturnsCorrectIds() {
        Session session = sessionFactory.getCurrentSession();
        Instant now = Instant.now();


        UserNotification noti1 = new UserNotification();
        noti1.setToUser("user1");
        noti1.setFromUser("admin");
        noti1.setEvent("test.event1");
        noti1.setEventDate(now);
        noti1.setRef("ref1");
        noti1.setUrl("placeholderUrl1");
        noti1.setDeferred(false);

        UserNotification noti1_1 = new UserNotification();
        noti1_1.setToUser("user1");
        noti1_1.setFromUser("admin");
        noti1_1.setEvent("test.event1_1");
        noti1_1.setEventDate(now);
        noti1_1.setRef("ref1_1");
        noti1_1.setUrl("placeholderUrl1_1");
        noti1_1.setDeferred(false);

        UserNotification noti2 = new UserNotification();
        noti2.setToUser("user1");
        noti2.setFromUser("admin");
        noti2.setEvent("test.event2");
        noti2.setEventDate(now.plusSeconds(10));
        noti2.setRef("ref2");
        noti2.setUrl("placeholderUrl2");
        noti2.setDeferred(false);


        UserNotification noti3 = new UserNotification();
        noti3.setToUser("user1");
        noti3.setFromUser("admin");
        noti3.setEvent("test.event3");
        noti3.setEventDate(now.plusSeconds(20));
        noti3.setRef("ref3");
        noti3.setRef("ref3");
        noti3.setUrl("placeholderUrl3");
        noti3.setDeferred(false);

        UserNotification noti4 = new UserNotification();
        noti4.setToUser("user2");
        noti4.setFromUser("admin");
        noti4.setEvent("test.event4");
        noti4.setEventDate(now);
        noti4.setRef("ref4");
        noti4.setUrl("placeholderUrl4");
        noti4.setDeferred(false);


        session.save(noti1);
        session.save(noti1_1);
        session.save(noti2);
        session.save(noti3);
        session.save(noti4);
        session.flush();

        int toDeleteCount = 2;
        List<Long> result = repository.getIdsToDeleteByUserIdAndToolPrefix("user1", toDeleteCount, "test");

        Assert.assertEquals(toDeleteCount, result.size());
        // should contain noti1 & noti1_1 as both have the ealierst event date
        Assert.assertTrue(result.contains(noti1.getId()) && result.contains(noti1_1.getId()));
    }



    @Test
    public void countAllByToUserAndByToolAndNotDeferred_zeroThresholdForTool_ReturnsEmptyList() {
        Session session = sessionFactory.getCurrentSession();
        int count = 10;
        for (int i = 0; i < count; i++) {

            Instant now = Instant.now();
            UserNotification noti = new UserNotification();
            noti.setToUser("user1");
            noti.setFromUser("admin");
            noti.setEvent("test.event" + i);
            noti.setEventDate(now.plusSeconds(i));
            noti.setRef("ref" + i);
            noti.setUrl("placeholderUrl" + i);
            noti.setDeferred(false);
            session.save(noti);
        }
        session.flush();
        int threshold = 0;
        long result = repository.countAllByToUserAndByToolAndNotDeferredOverThreshold("user1", "test", threshold);
        Assert.assertEquals(count, result);
    }


    @Test
    public void countAllByToUserAndByToolAndNotDeferred_NonzeroThresholdForTool_ReturnsListWithCorrectCount() {
        Session session = sessionFactory.getCurrentSession();
        int count = 50;
        for (int i = 0; i < count; i++) {

            Instant now = Instant.now();
            UserNotification noti = new UserNotification();
            noti.setToUser("user1");
            noti.setFromUser("admin");
            noti.setEvent("test.event" + i);
            noti.setEventDate(now.plusSeconds(i));
            noti.setRef("ref" + i);
            noti.setDeferred(false);
            noti.setUrl("placeholderUrl" + i);
            session.save(noti);
        }
        session.flush();
        int threshold = 20;
        long result = repository.countAllByToUserAndByToolAndNotDeferredOverThreshold("user1", "test", threshold);
        Assert.assertEquals(count, result);
    }




    @Test
    public void deleteNotificationsInList_nonEmptyList_DeletesNotifications() {
        Session session = sessionFactory.getCurrentSession();
        List<Long> idsToDelete =  new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            Instant now = Instant.now();
            UserNotification noti = new UserNotification();
            noti.setToUser("user1");
            noti.setFromUser("admin");
            noti.setEvent("test.event" + i);
            noti.setEventDate(now.plusSeconds(i));
            noti.setRef("ref" + i);
            noti.setDeferred(false);
            noti.setUrl("placeholderUrl" + i);
            session.persist(noti);
            session.flush();
            idsToDelete.add(noti.getId());
        }

        List<UserNotification> result = repository.findByToUser("user1");
        Assert.assertEquals(10, result.size());
        repository.deleteNotificationsInList(idsToDelete);
        List<UserNotification> resultAfterDelete = repository.findByToUser("user1");
        Assert.assertTrue(resultAfterDelete.isEmpty());

    }

}
