package edu.mci.rss.calendarNews;

import com.rometools.rome.feed.atom.Entry;

import java.util.List;

public interface CalendarNewsHandler {

     void addToolSpecificCalendarDetails();
     List<Entry> processCalendarNewsItems(List items);

}
