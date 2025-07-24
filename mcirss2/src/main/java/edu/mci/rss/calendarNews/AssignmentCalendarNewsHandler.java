package edu.mci.rss.calendarNews;

import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.services.CalendarFeedService;
import edu.mci.rss.utils.FeedUtils;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.mci.rss.utils.FeedUtils.TYPE_TEXT;
import static edu.mci.rss.utils.FeedUtils.createContentObjectAsType;
import static edu.mci.rss.utils.FeedUtils.createLinkAsList;


@Component
@Slf4j
public class AssignmentCalendarNewsHandler implements CalendarNewsHandler {


    @Autowired
    private AssignmentService assignmentService;


    @Override
    public <T> List<Entry> processCalendarNewsItems(List<T> items, CalendarFeedService.CalendarNewsItemData newsItemDatadata) {
        return items.stream().flatMap(item -> (item instanceof Assignment a)
                                    ? Stream.of(a)
                                    : Stream.empty()
                        ).map(a -> createEntryFromAssignment(a, newsItemDatadata))
                .toList();
    }

    private Entry createEntryFromAssignment(Assignment assignment, CalendarFeedService.CalendarNewsItemData newsItemDatadata) {
        Entry entry = new Entry();
        String deepLink = null;
        try {
            deepLink = assignmentService.getDeepLink(assignment.getContext(), assignment.getId(), newsItemDatadata.userId());
            log.debug("Link " + assignmentService.getDeepLink(assignment.getId(), assignment.getId(), newsItemDatadata.userId()));
        } catch (Exception e) {
            log.error("Could get Deepl link for assignment {} & user {}: {}", assignment.getId(), newsItemDatadata.userId() ,e);
        }
        if (deepLink != null){
            entry.setId(assignment.getId());
            log.debug("ID " + assignment.getId());
            entry.setSummary(createContentObjectAsType(newsItemDatadata.site().getTitle(), TYPE_TEXT));
            log.debug("Site Title " + newsItemDatadata.site().getTitle());
            entry.setTitle(assignment.getTitle());
            log.debug("Title " + assignment.getTitle());
            entry.setAlternateLinks(createLinkAsList(deepLink));
            entry.setUpdated(Date.from(assignment.getDueDate()));
            log.debug("due Date ", Date.from(assignment.getDueDate()));
        }
        return entry;
    }
}
