package edu.mci.rss.testing;



import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.samigo.util.SamigoConstants;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserNotificationTestDataFactory {



    private long idCounter = 0L;
    private long siteIdCounter = 10000L;

    private Instant timeStamp;
    private Instant sixMontsAgo;
    private Instant fourtheenDaysAgo;
    private ThreadLocalRandom random;





    public UserNotificationTestDataFactory() {
        random = ThreadLocalRandom.current();
        this.timeStamp = Instant.now();
        this.sixMontsAgo = timeStamp.minus(180, ChronoUnit.DAYS);
        this.fourtheenDaysAgo = timeStamp.minus(14, ChronoUnit.DAYS);
    }


    public List<ExpectedUserNotification> createTestUserNotificationsList(TestConfig config) {

        List<ExpectedUserNotification> exampleUserNotifications = new ArrayList<ExpectedUserNotification>();
        for (int i = 0; i < config.getAmountTestNotifications(); i++) {
            exampleUserNotifications.add(createExpectedUserNotificationItem(config));
        }
        return exampleUserNotifications;
    }



    public ExpectedUserNotification createExpectedUserNotificationItem(TestConfig config) {
        if (config.event.equals(HandledEvents.ALL)) {
            config.setEvent(HandledEvents.values()[random.nextInt(HandledEvents.values().length)]);
        }
        long id = getIdCounter();

        ExpectedUserNotification noti = new ExpectedUserNotification();
        noti.setId(id);
        noti.setEvent(config.getEvent().getEventNames().get((int) id % config.getEvent().getEventNames().size()));
        noti.setEventDate(getRandomTimestamp(config.getTimeRangeMode()));
        noti.setSiteId(siteIdGenerator());
        noti.setTitle(titleGenerator(config.getEvent(), (int) id));
        noti.setUrl(urlGenerator(noti.getSiteId()));
        noti.setRef(notificationReferenceGenerator(config.getEvent(),noti.getSiteId()));
        return noti;
    }

    public UserNotification createMockedUserNotificationItem(TestConfig config) {
        UserNotification noti = mock(UserNotification.class);
        ExpectedUserNotification exNoti = createExpectedUserNotificationItem(config);
        when(noti.getId()).thenReturn(exNoti.getId());
        when(noti.getRef()).thenReturn(exNoti.getRef());
        when(noti.getTitle()).thenReturn(exNoti.getTitle());
        when(noti.getUrl()).thenReturn(exNoti.getUrl());
        when(noti.getSiteId()).thenReturn(exNoti.getSiteId());
        when(noti.getEventDate()).thenReturn(exNoti.getEventDate());
        when(noti.getEvent()).thenReturn(exNoti.getEvent());
        return noti;
    }




    private String notificationReferenceGenerator(HandledEvents event, String siteId) {
        return switch (event) {
            case ANNOUNCEMENT -> "/announcement/msg/" + siteId + "/main/" + UUID.randomUUID().toString();
            case ASSIGNMENT ->  "/assignment/a/" + siteId + "/" + UUID.randomUUID().toString();
            case SAMIGO ->  "siteId=" + siteId + "," +  "assessmentId=" + random5DigitNumber() + "," + "publishedAssessmentId=" + random5DigitNumber();
            case ALL ->  "SomeRefToNwwsItem";
        };
    }


    private String urlGenerator(String siteId) {
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "SomeRandomHost";
        }
        return "https://" + hostname + "/" + siteId + "/tool/" + UUID.randomUUID().toString();
    }

    private String titleGenerator(HandledEvents event, int i) {
        return switch (event) {
            case ANNOUNCEMENT -> "Announcement #" + i;
            case ASSIGNMENT ->  "Assigment #" + i;
            case SAMIGO ->  "Assessment/Samigo #" + i;
            case ALL ->  "Some important message # " + i;
        };
    }

    private int random5DigitNumber() {
        int number = 0;
        for (int i = 0; i < 5; i++) {
            int digit = ThreadLocalRandom.current().nextInt(0, 10);
            number = number * 10 + digit;
        }
        return number;
    }

    private String siteIdGenerator() {
        return "Course-ID-SLVA-" + siteIdCounter++;
    }

    private Long getIdCounter() {
        return idCounter++;
    }

    private Instant getRandomTimestamp(TimeRangeMode timeRangeMode) {

        return switch (timeRangeMode) {
            case LONG ->
                    Instant.ofEpochSecond(random.nextLong(sixMontsAgo.getEpochSecond(), fourtheenDaysAgo.getEpochSecond()));
            case SHORT ->
                    Instant.ofEpochSecond(random.nextLong(fourtheenDaysAgo.getEpochSecond(), timeStamp.getEpochSecond()));
            case LONG_BOUNDARY -> Instant.ofEpochSecond(random.nextLong(sixMontsAgo.minus(1, ChronoUnit.MINUTES).getEpochSecond(),
                    sixMontsAgo.plus(1, ChronoUnit.MINUTES).getEpochSecond()));
            case SHORT_BOUNDARY -> Instant.ofEpochSecond(random.nextLong(fourtheenDaysAgo.minus(1, ChronoUnit.MINUTES).getEpochSecond(),
                    fourtheenDaysAgo.plus(1, ChronoUnit.MINUTES).getEpochSecond()));
            case ALL -> Instant.ofEpochSecond(random.nextLong(sixMontsAgo.minus(1, ChronoUnit.MINUTES).getEpochSecond(),
                    fourtheenDaysAgo.plus(1, ChronoUnit.MINUTES).getEpochSecond()));
        };
    }

    public enum TimeRangeMode {
        SHORT,
        LONG,
        LONG_BOUNDARY,
        SHORT_BOUNDARY,
        ALL
    }


    public enum HandledEvents {
        ANNOUNCEMENT(List.of(
                AnnouncementService.SECURE_ANNC_ADD
        )),
        ASSIGNMENT(List.of(AssignmentConstants.EVENT_ADD_ASSIGNMENT,
                AssignmentConstants.EVENT_AVAILABLE_ASSIGNMENT
                )),
        SAMIGO(List.of( SamigoConstants.EVENT_ASSESSMENT_AVAILABLE,
                SamigoConstants.EVENT_ASSESSMENT_UPDATE_AVAILABLE)),
        ALL(Stream.concat(
                    Stream.concat(ANNOUNCEMENT.eventNames.stream(),
                            ASSIGNMENT.eventNames.stream()),
                    SAMIGO.eventNames.stream())
                .collect(Collectors.toUnmodifiableList()));


        private final List<String> eventNames;

        HandledEvents(List<String> eventNames) {
            this.eventNames = eventNames;
        }

        public List<String> getEventNames() {
            return eventNames;
        }
    }

    @Getter
    @Setter
    public static class TestConfig {
        Integer amountTestNotifications;
        TimeRangeMode timeRangeMode;
        HandledEvents event;

        public TestConfig(Integer amountTestNotifications, TimeRangeMode timeRangeMode, HandledEvents event) {
            this.amountTestNotifications = amountTestNotifications != null ? amountTestNotifications : 100;
            this.timeRangeMode = timeRangeMode != null ? timeRangeMode : TimeRangeMode.ALL;
            this.event = event != null ? event : HandledEvents.ALL;

            if (this.amountTestNotifications < 0) {
                throw new IllegalArgumentException("amountTestNotifications must be non-negative.");
            }
        }
    }
/*
    public TestConfig getTestConfigFor(int amountTestNotifications, TimeRangeMode timeRangeMode, HandledEvents event) {
        return new TestConfig(amountTestNotifications, timeRangeMode, event);
    }
*/

/*
    public record TestConfig(int amountTestNotifications, TimeRangeMode timeRangeMode, HandledEvents event) {}
*/





}
