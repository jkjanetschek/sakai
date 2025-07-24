package edu.mci.rss.calendarNews;

import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.services.CalendarFeedService;

import java.util.List;

public interface CalendarNewsHandler {

     <T> List<Entry> processCalendarNewsItems(List<T> items, CalendarFeedService.CalendarNewsItemData newsItemDatadata);

}
