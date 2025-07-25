package edu.mci.rss.eventHandlers;


import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.model.NewsItemProcessingData;


public interface MciRssEventHandler {

    Entry processEvent(NewsItemProcessingData itemData);


}
