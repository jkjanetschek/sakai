package edu.mci.rss.services;

import com.rometools.rome.feed.atom.Feed;

public interface FeedService {

    Feed createFeedForUserId(String userId);
}
