package edu.mci.rss.calendarNews;

import com.rometools.rome.feed.atom.Entry;
import org.sakaiproject.assignment.api.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AssignmentCalendarNewsHandler extends AbstractCalendarNewsHandler {





    @Override
    public List<Entry> processCalendarNewsItems(List items) {
        return List.of();
    }


    @Override
    public void addToolSpecificCalendarDetails() {

    }


    // feedEntry.addLink(deepLink); in subClasses

    @Override
    protected String buildSummary() {
        return "";
    }



}
