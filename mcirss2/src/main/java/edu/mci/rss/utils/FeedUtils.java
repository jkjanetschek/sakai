package edu.mci.rss.utils;

import com.rometools.rome.feed.atom.Category;
import com.rometools.rome.feed.atom.Content;
import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.atom.Link;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;

import java.util.Collections;
import java.util.List;

public class FeedUtils {

    public static final String TYPE_TEXT = "text";
    public static  final String TYPE_HTML = "html";



    public static Feed createAtomFeed() {
        Feed feed = new Feed();
        feed.setFeedType("atom_1.0");
        return feed;
    }


    public static String serializeAtomFeed(Feed feed) {
        WireFeedOutput output = new WireFeedOutput();
        try {
            return output.outputString(feed);
        } catch (IllegalArgumentException | FeedException e) {
            return null;
        }

    }

    public static Content createContentObjectAsType(String contentString, String type) {
        Content content = new Content();
        content.setType(type);
        content.setValue(contentString);
        return content;
    }


    public static List<Category> createCategoryAsList(String term) {
        Category category = new Category();
        category.setTerm(term);
        return Collections.singletonList(category);
    }

    public static List<Link> createLinkAsList(String url) {
        Link link = new Link();
        link.setRel(null);
        link.setHref(url);
        return Collections.singletonList(link);
    }

}
