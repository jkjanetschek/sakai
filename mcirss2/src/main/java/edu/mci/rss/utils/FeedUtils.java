package edu.mci.rss.utils;

import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.WireFeedOutput;

public class FeedUtils {


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
}
