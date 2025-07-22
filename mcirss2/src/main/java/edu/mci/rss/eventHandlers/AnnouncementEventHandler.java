package edu.mci.rss.eventHandlers;

import com.rometools.rome.feed.atom.Category;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.mci.rss.model.NewsItemProcessingData;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@EventType({AnnouncementService.SECURE_ANNC_ADD})
@Component
public class AnnouncementEventHandler extends AbstractEventHandler {



    public static final String CATEGORIE = "Announcement";

    @Override
    protected void addToolSpecificDetails(NewsItemProcessingData itemData, Entry atomEntry) {


        atomEntry.setCategories(createCategoryAsList(CATEGORIE));
        UserNotification noti = itemData.getUserNotification();


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
        return "AnnouncementHandler";
    }

}
