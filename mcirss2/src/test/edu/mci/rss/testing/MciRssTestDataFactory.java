package edu.mci.rss.testing;



import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.samigo.util.SamigoConstants;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MciRssTestDataFactory {



    private long idCounter = 0L;
    private long siteIdCounter = 10000L;

    private Instant timeStamp;
    private Instant sixMontsAgo;
    private Instant fourtheenDaysAgo;
    private ThreadLocalRandom random;





    public MciRssTestDataFactory() {
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

    public UserNotification createMockedUserNotificationItemFromExcepected(ExpectedUserNotification exNoti) {
        UserNotification noti = mock(UserNotification.class);
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

    private String siteTitleGenerator(String siteId) {
        return "WS " + ThreadLocalRandom.current().nextInt(2010, java.time.Year.now().getValue() + 1) + " " + siteId;
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


    public CompleteAtomFeedTestData createFeedTestData() {


        // noti for Assignemnt
        TestConfig configAssignment = new TestConfig(1,TimeRangeMode.SHORT,HandledEvents.ASSIGNMENT);
        ExpectedUserNotification noti = new ExpectedUserNotification();
        long idAssignmentFeed = getIdCounter();
        noti.setId(idAssignmentFeed);
        noti.setEvent(configAssignment.getEvent().getEventNames().get((int) idAssignmentFeed % configAssignment.getEvent().getEventNames().size()));
        noti.setEventDate(getRandomTimestamp(configAssignment.getTimeRangeMode()));
        noti.setSiteId(siteIdGenerator());
        noti.setTitle(titleGenerator(configAssignment.getEvent(), (int) idAssignmentFeed));
        noti.setUrl(urlGenerator(noti.getSiteId()));
        noti.setRef(notificationReferenceGenerator(configAssignment.getEvent(),noti.getSiteId()));


        // noti for Annc
        TestConfig configAnnc = new TestConfig(1,TimeRangeMode.SHORT,HandledEvents.ANNOUNCEMENT);
        ExpectedUserNotification noti2 = new ExpectedUserNotification();
        long idAnncFeed = getIdCounter();
        noti2.setId(idAnncFeed);
        noti2.setEvent(configAnnc.getEvent().getEventNames().get((int) idAnncFeed % configAnnc.getEvent().getEventNames().size()));
        noti2.setEventDate(getRandomTimestamp(configAnnc.getTimeRangeMode()));
        noti2.setSiteId(siteIdGenerator());
        noti2.setTitle(titleGenerator(configAnnc.getEvent(), (int) idAnncFeed));
        noti2.setUrl(urlGenerator(noti2.getSiteId()));
        noti2.setRef(notificationReferenceGenerator(configAnnc.getEvent(),noti2.getSiteId()));


        // noti for samigo
        TestConfig configSamigo = new TestConfig(1,TimeRangeMode.SHORT,HandledEvents.SAMIGO);
        ExpectedUserNotification noti3 = new ExpectedUserNotification();
        long idSamigoFeed = getIdCounter();
        noti3.setId(idSamigoFeed);
        noti3.setEvent(configSamigo.getEvent().getEventNames().get((int) idSamigoFeed % configSamigo.getEvent().getEventNames().size()));
        noti3.setEventDate(getRandomTimestamp(configSamigo.getTimeRangeMode()));
        noti3.setSiteId(siteIdGenerator());
        noti3.setTitle(titleGenerator(configSamigo.getEvent(), (int) idSamigoFeed));
        noti3.setUrl(urlGenerator(noti3.getSiteId()));
        noti3.setRef(notificationReferenceGenerator(configSamigo.getEvent(),noti3.getSiteId()));



        String oldAtomFeedVersionTemplate = """
                <?xml version="1.0" encoding="UTF-8"?>
                <feed xmlns="http://www.w3.org/2005/Atom">
                  <entry>
                    <title type="text">{{titleAss}}</title>
                    <link href="{{hrefAss}}" />
                    <category term="Assignment" />
                    <id>{{idAss}}</id>
                    <updated>{{updatedAss}}</updated>
                    <published>{{publishedAss}}</published>
                    <summary type="html">{{summaryAss}}</summary>
                  </entry>
                  <entry>
                    <title type="text">{{titleAnnc}}</title>
                    <link href="{{hrefAnnc}}" />
                    <category term="Announcement" />
                    <id>{{idAnnc}}</id>
                    <updated>{{updatedAnnc}}</updated>
                    <published>{{publishedAnnc}}</published>
                    <summary type="html">{{summaryAnnc}}</summary>
                  </entry>
                  <entry>
                    <title type="text">{{titleSamigo}}</title>
                    <link href="{{hrefSamigo}}" />
                    <category term="Assessment" />
                    <id>{{idSamigo}}</id>
                    <updated>{{updatedSamigo}}</updated>
                    <published>{{publishedSamigo}}</published>
                    <summary type="html">{{summarySamigo}}</summary>
                  </entry>
                </feed>
                """;

        List<ExpectedUserNotification> list = new ArrayList<>();
        list.add(noti);
        list.add(noti2);
        list.add(noti3);
        return new CompleteAtomFeedTestData(oldAtomFeedVersionTemplate, list);

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


    public record CompleteAtomFeedTestData(String atomFeedXmlTemplate, List<ExpectedUserNotification> userNotiList) {}



    public DummySite createDummySiteObject() {
        DummySite dummySite = new DummySite();
        dummySite.setSiteId(siteIdGenerator());
        dummySite.setTitle(siteTitleGenerator(dummySite.getSiteId()));
        return dummySite;
    }


    public List<DummySite> createDummySiteObjects(int howMany) {
        List<DummySite> dummySites = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            DummySite dummySite = createDummySiteObject();
            dummySites.add(dummySite);
        }
        return dummySites;
    }






}
