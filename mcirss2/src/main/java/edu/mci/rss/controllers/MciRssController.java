package edu.mci.rss.controllers;


import com.rometools.rome.feed.atom.Feed;
import edu.mci.rss.services.CalendarFeedService;
import edu.mci.rss.services.NewsFeedService;
import edu.mci.rss.utils.FeedUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;


@Slf4j
@RestController
public class MciRssController {




    @Autowired
    private UserDirectoryService userDirectoryService;
    @Autowired
    private NewsFeedService newsFeedService;
    @Autowired
    private CalendarFeedService calendarFeedService;



    @GetMapping(value = "/news/user/{eid}", produces = "application/atom+xml")
    public String getNews(@PathVariable("eid") String eid) throws UserNotDefinedException {

        if( eid == null ) {
            throw new IllegalStateException();
        }

        String userId =this.userDirectoryService.getUserId(eid);
        Feed feed = null;
        if (userId != null) {
            feed = newsFeedService.createFeedForUserId(userId);
        } else {
           throw new UserNotDefinedException("null");
        }

        return FeedUtils.serializeAtomFeed(feed);
    }


    @GetMapping(value = "calendar/user/{eid}", produces = "application/atom+xml")
    public String getCalendar(@PathVariable("eid") String eid) throws UserNotDefinedException   {
        if( eid == null ) {
            throw new IllegalStateException();
        }
        Feed feed = null;
        String userId =this.userDirectoryService.getUserId(eid);
        if (userId != null) {
            feed = calendarFeedService.createFeedForUserId(userId);
        } else {
            throw new UserNotDefinedException("null");
        }


        return FeedUtils.serializeAtomFeed(feed);
    }






    @ExceptionHandler
    public ResponseEntity<String> handleUserNotDefinedException(UserNotDefinedException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }


    @ExceptionHandler
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException ex) {
        System.out.println("IllegalStateException: "+ ex.getMessage());
        log.error("IllegalStateException: {}", ex.getMessage());
        log.error("IllegalStateException: ", ex);
        return ResponseEntity.badRequest().build();
    }


    @ExceptionHandler
    public ResponseEntity<String> handleAllUncaughtException(Exception ex) {
        log.error("Uncaught exception", ex);
        return ResponseEntity.internalServerError().build();
    }



}
