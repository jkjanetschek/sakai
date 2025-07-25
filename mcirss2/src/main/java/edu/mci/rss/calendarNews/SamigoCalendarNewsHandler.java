package edu.mci.rss.calendarNews;

import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.services.CalendarFeedService;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static edu.mci.rss.utils.FeedUtils.TYPE_TEXT;
import static edu.mci.rss.utils.FeedUtils.createCategoryAsList;
import static edu.mci.rss.utils.FeedUtils.createContentObjectAsType;

@Slf4j
@Component
@Qualifier("samigoCalendarNewsHandler")
public class SamigoCalendarNewsHandler implements CalendarNewsHandler  {



    private final String CATEGORIE = "Assessment";

    @Override
    public <T> List<Entry> processCalendarNewsItems(List<T> items, CalendarFeedService.CalendarNewsItemData newsItemDatadata) {
        return items.stream().flatMap(item -> (item instanceof PublishedAssessmentFacade pub)
                        ? Stream.of(pub)
                        : Stream.empty()
                ).map(pub -> createEntryFromAssignment(pub, newsItemDatadata))
                .toList();
    }




    private Entry createEntryFromAssignment(PublishedAssessmentFacade pubAss, CalendarFeedService.CalendarNewsItemData newsItemData) {
        Date dueDate = pubAss.getDueDate();
        if (dueDate == null) {
            return new Entry();
        }
        Entry entry = new Entry();
        try {
            entry.setCategories(createCategoryAsList(CATEGORIE));
            log.debug("Due date: {}", dueDate.toString());
            entry.setId(String.valueOf(pubAss.getPublishedAssessmentId()));
            log.debug("id: {}", String.valueOf(pubAss.getPublishedAssessmentId()));
            entry.setSummary(createContentObjectAsType(newsItemData.site().getTitle(), TYPE_TEXT));
            log.debug("summary: {}", newsItemData.site().getTitle());
            entry.setTitleEx(createContentObjectAsType(pubAss.getTitle(), TYPE_TEXT));
            log.debug("title: {}", pubAss.getTitle());
            entry.setUpdated(dueDate);
            log.debug("updated: {}", dueDate.toString());
            entry.setPublished(pubAss.getStartDate());
            log.debug("published: {}", pubAss.getStartDate().toString());
        } catch (Exception e) {
            log.error("something went wrong {}", e.getMessage());
        }
        return entry;
    }




}
