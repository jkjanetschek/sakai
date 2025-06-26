package edu.mci.rss.eventHandlers;

import com.rometools.rome.feed.atom.Category;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Link;
import edu.mci.rss.model.NewsItemProcessingData;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        //TODO


        Entry atomEntry = new Entry();

        generalEntryDetails(itemData.getUserNotification(), atomEntry);

        addToolSpecificDetails(itemData, atomEntry);

        return atomEntry;
    }

   private void generalEntryDetails(UserNotification noti, Entry atomEntry) {
       atomEntry.setId(Long.toString(noti.getId()));
       atomEntry.setTitle(noti.getTitle());
       Date date = Date.from(noti.getEventDate());
       atomEntry.setPublished(date);
       atomEntry.setUpdated(date);
       atomEntry.setAlternateLinks(createLinkAsList(noti.getUrl()));
   }



    protected String shortenTitle(String siteId) {
        Matcher matcher = pattern.matcher(siteService.getSiteDisplay(siteId));
        matcher.find();
        return matcher.group(0);
    }

    protected List<Category> createCategoryAsList(String term) {
        Category category = new Category();
        category.setTerm(term);
        return Collections.singletonList(category);
    }

    private List<Link> createLinkAsList(String url) {
        Link link = new Link();
        link.setHref(url);
        return Collections.singletonList(link);
    }

    protected String checkTitleOrDefault(UserNotification noti) {
       return Optional.ofNullable(noti.getTitle()).orElse(UNKNOWN_TITLE);
    }

    protected String checkSiteIdOrDefault(UserNotification noti) {
        return Objects.requireNonNullElse(noti.getSiteId(), UNKNOWN_SITE_ID);
    }

    protected Content createContentObjectForSummay(String summary) {
        Content content = new Content();
        content.setType("text"); // or "html", "xhtml"
        content.setValue(summary);
        return content;
    }






}
