package edu.mci.rss.eventHandlers;


import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.model.NewsItemProcessingData;
import org.sakaiproject.messaging.api.model.UserNotification;

public interface MciRssEventHandler {

    public Entry processEvent(NewsItemProcessingData itemData);


}
