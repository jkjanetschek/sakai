# 2. Threshold-based cleanup of notifications per user and tool

Date: 2026-04-08

## Status

Accepted

Context:

The system can accumulate a large number of notifications per user, especially for frequently used tools. Over time, this increases database size and can degrade the performance of notification-related queries.
A cleanup mechanism is needed to prevent unbounded growth while preserving the most recent notifications that are most likely to be relevant.

Decision:

Introduce a scheduled notification cleanup process that:

    Scans users who have notifications.
    Groups notifications by user and tool (via a tool identifier/prefix).
    Applies a configurable per-tool threshold (maximum number of notifications to retain).
    Deletes only the oldest notifications that exceed the threshold.
    Skips cleanup for a tool when its threshold is set to 0 or below (i.e., cleanup disabled for that tool).


The cleanup is deliberately conservative:

    Notifications are removed strictly by age, oldest first.
    Only non-deferred notifications are considered for deletion (deferred/pending notifications are excluded).
    Failures for one user or one tool do not stop cleanup for others.


Database approach:

    No new index is created specifically for this job.
    Several secondary composite indexes were tested: overall speed improved in some cases but regressed in others; they were not adopted.
    A per-user iteration approach limits the duration of any single query, at the cost of more queries in total.
    Longer total runtime is acceptable; job frequency is limited to control load.

Consequences:

Positive

    Prevents notification tables from growing without bounds.
    Retains the most recent notifications for user relevance.
    Makes cleanup behavior configurable per deployment and per tool.
    Limits the blast radius of errors to individual users/tools.

Trade-offs:

    Older notifications beyond the threshold are permanently deleted and cannot be recovered.
    Notifications for inactive users are not proactively removed and may accumulate, potentially increasing total job runtime unless cleaned up manually or by a separate policy.

Alternatives Considered:

    Adding new composite indexes to optimize deletion queries:
        Rejected due to mixed performance impact & lack of clarity about effects in production performance.
    Time-based retention (e.g., delete older than N days):
        Rejected in favor of a threshold that guarantees retaining a bounded number of recent notifications per user and tool, as e.g. content tool can produce huge amount in short amount of time
    Per tool approach:
        Rejected in favor of per user approach due to long run time of queries in database
    

Future Considerations:

    future releases of sakai probsably will have a new notification type that will be used for broadcasts and automatic expiration and periodic cleanup of old notifications
    SAK-51956 notifications Add broadcast type notification (https://sakaiproject.atlassian.net/browse/SAK-51956)
