package org.sakaiproject.component.app.scheduler.jobs;



import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.sakaiproject.component.api.ServerConfigurationService;


import org.sakaiproject.messaging.api.repository.UserNotificationRepository;

import javax.inject.Inject;
import java.util.*;

/**
 This job keeps user notifications from growing without limit by removing older notifications once a configurable threshold is exceeded.
 A global threshold applies to all supported tools by default, and can be overridden per tool if needed.
 Supported tool types include assignments, Samigo, announcements, and content.
 If a threshold is set to 0, cleanup is disabled for that tool.
 If no per-tool value is provided, the global value is used instead.

 notification.cleanup.threshold.per.tool=100
 notification.cleanup.threshold.per.tool.asn=100
 notification.cleanup.threshold.per.tool.sam=100
 notification.cleanup.threshold.per.tool.annc=100
 notification.cleanup.threshold.per.tool.content=100
 */

@DisallowConcurrentExecution
@Slf4j
public class NotificationCleanupJob implements Job {

    private static final String ASSIGNMENT_TOOL_PREFIX      = "asn";
    private static final String SAMIGO_TOOL_PREFIX          = "sam";
    private static final String ANNOUNCEMENT_TOOL_PREFIX    = "annc";
    private static final String CONTENT_TOOL_PREFIX    =    "content";

    private static final List<String> TOOL_PREFIXES = Arrays.asList(ASSIGNMENT_TOOL_PREFIX, SAMIGO_TOOL_PREFIX, ANNOUNCEMENT_TOOL_PREFIX, CONTENT_TOOL_PREFIX);

    private static final String THRESHOLD_PROPERTY_NAME_GLOBAL = "notification.cleanup.threshold.per.tool";
    private static final int DEFAULT_NOTIFICATION_THRESHOLD_PER_TOOL = 100;




    @Inject
    private ServerConfigurationService serverConfigurationService;

    @Inject
    private UserNotificationRepository userNotificationRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        try {
            int globalThreshold = serverConfigurationService.getInt(
                    THRESHOLD_PROPERTY_NAME_GLOBAL, DEFAULT_NOTIFICATION_THRESHOLD_PER_TOOL);

            int thresholdAssignment = serverConfigurationService.getInt(
                    THRESHOLD_PROPERTY_NAME_GLOBAL + "." + ASSIGNMENT_TOOL_PREFIX, globalThreshold);
            int thresholdSamigo = serverConfigurationService.getInt(
                    THRESHOLD_PROPERTY_NAME_GLOBAL + "." + SAMIGO_TOOL_PREFIX, globalThreshold);
            int thresholdAnnouncement = serverConfigurationService.getInt(
                    THRESHOLD_PROPERTY_NAME_GLOBAL + "." + ANNOUNCEMENT_TOOL_PREFIX, globalThreshold);
            int thresholdContent = serverConfigurationService.getInt(
                    THRESHOLD_PROPERTY_NAME_GLOBAL + "." + CONTENT_TOOL_PREFIX, globalThreshold);

            Map<String, Integer> thresholds = Map.of(
                    ASSIGNMENT_TOOL_PREFIX, thresholdAssignment,
                    SAMIGO_TOOL_PREFIX, thresholdSamigo,
                    ANNOUNCEMENT_TOOL_PREFIX, thresholdAnnouncement,
                    CONTENT_TOOL_PREFIX, thresholdContent
            );


            log.info("Starting notification cleanup job with threshold: {} assignment, {} samigo, {} announcement, {} content", thresholdAssignment, thresholdSamigo, thresholdAnnouncement, thresholdContent);

            if (thresholdAssignment <= 0 && thresholdSamigo <= 0 && thresholdAnnouncement <= 0 && thresholdContent <= 0) {
                log.info("Notification cleanup skipped due to zero threshold for all tools");
                return;
            }

            List<String> usersWithNotifications = getUsersWithNotifications();

            log.info("Found {} users with notifications ... deleting notifications now", usersWithNotifications.size());

            long totalDeletedCountAnnouncement = 0;
            long totalDeletedCountSamigo = 0;
            long totalDeletedCountAssignment = 0;
            long totalDeletedCountContent = 0;

            for (String userId : usersWithNotifications) {
                for (String toolPrefix : TOOL_PREFIXES) {
                    if (thresholds.get(toolPrefix) <= 0) {
                        continue;
                    }

                    long deletedCount = 0;

                    try {
                        deletedCount = processNotificationForToolAndUserId(userId, toolPrefix, thresholds.get(toolPrefix));
                    } catch (Exception e) {
                        log.error("Error processing notifications for user {} and tool {}", userId, toolPrefix, e);
                    }

                    switch (toolPrefix) {
                        case ANNOUNCEMENT_TOOL_PREFIX:
                            totalDeletedCountAnnouncement += deletedCount;
                            break;
                        case SAMIGO_TOOL_PREFIX:
                            totalDeletedCountSamigo += deletedCount;
                            break;
                        case ASSIGNMENT_TOOL_PREFIX:
                            totalDeletedCountAssignment += deletedCount;
                            break;
                        case CONTENT_TOOL_PREFIX:
                            totalDeletedCountContent += deletedCount;
                            break;
                        default:
                            log.debug("Unknown tool prefix {}", toolPrefix);
                    }
                }
            }

            long totalDeletedCount = totalDeletedCountAnnouncement
                    + totalDeletedCountSamigo
                    + totalDeletedCountAssignment
                    + totalDeletedCountContent;

            log.info(
                    "Finished notification cleanup job: total deleted Notifications {} (annc={}, sam={}, asn={}, content={})",
                    totalDeletedCount,
                    totalDeletedCountAnnouncement,
                    totalDeletedCountSamigo,
                    totalDeletedCountAssignment,
                    totalDeletedCountContent
            );

        } catch (Exception e) {
            log.error("Error executing notification cleanup job", e);
        }
    }

    private List<String> getUsersWithNotifications() {
        return userNotificationRepository.findAllDistinctToUser();
    }


    private int processNotificationForToolAndUserId(String user, String toolPrefix, int threshold) {

        long countedNotisByUserIdAndTool =  userNotificationRepository.countAllByToUserAndByToolAndNotDeferredOverThreshold(user, toolPrefix, threshold);

        log.debug("User {} has {} notifications over threshold for tool {}", user, countedNotisByUserIdAndTool, toolPrefix);

        int toDeleteCount = calculateDeleteCount(user, countedNotisByUserIdAndTool, threshold);

        log.debug("{} notifications will be deleted for user {} and tool {}", toDeleteCount, user, toolPrefix);

        List<Long> notiIdsToDelete =  userNotificationRepository.getIdsToDeleteByUserIdAndToolPrefix(user, toDeleteCount, toolPrefix);

        return  deleteNotifications(user, notiIdsToDelete, toolPrefix);
    }

    private int deleteNotifications(String userId, List<Long> notiIdsToDelete, String toolPrefix) {
        log.debug("Deleting {} notifications for user {} and toolPrefix {}", notiIdsToDelete.size(), userId, toolPrefix);
        try {
           return userNotificationRepository.deleteNotificationsInList(notiIdsToDelete);
        } catch (Exception e) {
            log.error("Error deleting notifications for user {} and tool {}", userId, toolPrefix, e);
        }
        return 0;
    }

    private int calculateDeleteCount(String userId, long notificationCount, int threshold) {
        long excess = notificationCount - threshold;
        if (excess <= 0L) return 0;
        int toDeleteCount;
        try {
            toDeleteCount =  Math.toIntExact(excess);
        } catch (ArithmeticException e) {
            log.warn("Excess notifications too large for int for user: {}, excess: {}", userId, excess);
            toDeleteCount = Integer.MAX_VALUE;
        }
        return toDeleteCount;
    }


}
