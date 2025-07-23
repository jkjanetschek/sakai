package edu.mci.rss.calendarNews;


import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.model.NewsItemProcessingData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public abstract class AbstractCalendarNewsHandler implements CalendarNewsHandler {



    protected abstract String buildSummary();


    protected Entry generalEntryDetail(EntryDetail detail) {
        Entry feedEntry = new Entry();
        feedEntry.setId(detail.id);
        feedEntry.setSummary(detail.summary);
        feedEntry.setTitle(detail.title);
        feedEntry.setUpdated(detail.updated);
        return feedEntry;
    }


    protected record EntryDetail(String id, String title, Content summary, Date updated) implements Serializable {
        public void someMera(){
            System.out.println("DEBUG record");
        }
    }
}
