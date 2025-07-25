package edu.mci.rss.eventHandlers;


import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.model.NewsItemProcessingData;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.site.api.SiteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static edu.mci.rss.utils.FeedUtils.TYPE_TEXT;
import static edu.mci.rss.utils.FeedUtils.createContentObjectAsType;
import static edu.mci.rss.utils.FeedUtils.createLinkAsList;

@Component
public abstract class AbstractEventHandler implements MciRssEventHandler {

    protected final String UNKNOWN_TITLE = "Unknown Title";
    protected final String UNKNOWN_SITE_ID = "Unknown Site";

    private Pattern pattern = Pattern.compile(".+?\\\"(?= \\()");
    @Autowired
    protected SiteService siteService;
    @Autowired
    protected EntityManager entityManager;


    protected abstract void addToolSpecificDetails(NewsItemProcessingData itemData, Entry atomEntry);
    protected abstract String buildSummary(NewsItemProcessingData itemData);

    public Entry processEvent(NewsItemProcessingData itemData) {

        Entry atomEntry = new Entry();

        generalEntryDetails(itemData.getUserNotification(), atomEntry);

        addToolSpecificDetails(itemData, atomEntry);

        return atomEntry;
    }

   private void generalEntryDetails(UserNotification noti, Entry atomEntry) {
       atomEntry.setId(Long.toString(noti.getId()));
       atomEntry.setTitleEx(createContentObjectAsType(noti.getTitle(), TYPE_TEXT));
       Date date = Date.from(noti.getEventDate());
       atomEntry.setPublished(date);
       atomEntry.setUpdated(date);
       atomEntry.setAlternateLinks(createLinkAsList(noti.getUrl()));
   }



    protected String shortenTitle(String siteId) {
        Matcher matcher = pattern.matcher(siteService.getSiteDisplay(siteId));
        return matcher.find() ? matcher.group(0) : "\"" + siteId + "\"";
    }


    protected String checkTitleOrDefault(UserNotification noti) {
       return Optional.ofNullable(noti.getTitle()).orElse(UNKNOWN_TITLE);
    }

    protected String checkSiteIdOrDefault(UserNotification noti) {
        return Objects.requireNonNullElse(noti.getSiteId(), UNKNOWN_SITE_ID);
    }





}
