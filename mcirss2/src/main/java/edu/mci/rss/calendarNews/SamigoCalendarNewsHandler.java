package edu.mci.rss.calendarNews;

import com.rometools.rome.feed.atom.Entry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SamigoCalendarNewsHandler extends AbstractCalendarNewsHandler  {




    @Override
    public List<Entry> processCalendarNewsItems(List items) {
        return List.of();
    }

    @Override
    protected String buildSummary() {
        return "";
    }

    @Override
    public void addToolSpecificCalendarDetails() {

    }


}
