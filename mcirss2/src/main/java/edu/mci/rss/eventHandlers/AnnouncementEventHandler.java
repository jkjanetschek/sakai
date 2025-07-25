package edu.mci.rss.eventHandlers;


import com.rometools.rome.feed.atom.Entry;
import edu.mci.rss.model.NewsItemProcessingData;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.springframework.stereotype.Component;

import static edu.mci.rss.utils.FeedUtils.TYPE_HTML;
import static edu.mci.rss.utils.FeedUtils.createCategoryAsList;
import static edu.mci.rss.utils.FeedUtils.createContentObjectAsType;


@EventType({AnnouncementService.SECURE_ANNC_ADD})
@Component
public class AnnouncementEventHandler extends AbstractEventHandler {



    public static final String CATEGORIE = "Announcement";

    @Override
    protected void addToolSpecificDetails(NewsItemProcessingData itemData, Entry atomEntry) {

        atomEntry.setCategories(createCategoryAsList(CATEGORIE));

        atomEntry.setSummary(createContentObjectAsType(buildSummary(itemData), TYPE_HTML));
    }


    protected String buildSummary(NewsItemProcessingData itemData) {
        String title = checkTitleOrDefault(itemData.getUserNotification());
        String siteTitle = shortenTitle(checkSiteIdOrDefault(itemData.getUserNotification()));
        return new StringBuilder()
            .append("<p>Announcement: ")
            .append(title)
            .append(":<br/> posted in ")
            .append(siteTitle)
            .append("</p>")
            .toString();
    }


    public String getName() {
        return "AnnouncementEventHandler";
    }

}
