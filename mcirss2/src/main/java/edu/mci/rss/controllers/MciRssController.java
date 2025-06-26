package edu.mci.rss.controllers;


import com.rometools.rome.feed.atom.Feed;
import com.rometools.rome.feed.synd.SyndFeed;
import edu.mci.rss.services.NewsFeedService;
import edu.mci.rss.utils.FeedUtils;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;

import org.sakaiproject.user.api.UserDirectoryService;

import java.util.List;

@Slf4j
@RestController
public class MciRssController {


    public MciRssController() {
        System.out.println("MciRssController()");
        log.info("MciRssController()");
    }

    // TODO dev only:  remove
    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    @PostConstruct
    public void logAllEndpoints() {
        requestMappingHandlerMapping.getHandlerMethods().forEach((key, value) -> {
            log.info("Mapped endpoint: {} -> {}", key, value.getMethod().getDeclaringClass().getSimpleName());
           System.out.println("Mapped endpoint: " +  key + " -> " + value.getMethod().getDeclaringClass().getSimpleName());
        });
    }

    @Autowired
    UserDirectoryService userDirectoryService;
    @Autowired
    NewsFeedService newsFeedService;

    // TODO dev only:  remove
    @GetMapping(value = "/ping", produces = "application/octet-stream")
    public ResponseEntity<String> ping() {
        System.out.println("MciRssController.ping()");
        return ResponseEntity.ok("pong");
    }
    @GetMapping(value = "/ping2", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> ping2() {
        return List.of("ok");
    }



    @GetMapping(value = "/news/user/{eid}", produces = "application/atom+xml")
    public String getNews(@PathVariable("eid") String eid) throws UserNotDefinedException {
        System.out.println("getNews(): " + eid);

        // TODO check session

        if( eid == null ) {
            throw new IllegalStateException();
        }

        String userId =this.userDirectoryService.getUserId(eid);
        Feed feed = null;
        if (userId != null) {
            feed = newsFeedService.createFeedForUserId(userId);
        } else {
            System.out.println("DEBUG userId = null");
           throw new IllegalStateException();

        }


        return FeedUtils.serializeAtomFeed(feed);
        /*
        return atomFeedXML != null
                ?  ResponseEntity.ok().body(atomFeedXML)
                :  ResponseEntity.notFound().build();


         */
       // return ResponseEntity.noContent().build();  // final return statement; feed can be null
        // ResponseEntity.noContent().build();
        // ResponseEntity.ok().body(null)
    }







    /*
     *      Exception Handling Methods
     */

    @ExceptionHandler
    public ResponseEntity<String> handleUserNotDefinedException(UserNotDefinedException e) {
        log.warn("DEBUG User not found: {}", e.getMessage());
        System.out.println("DEBUG User not found: " + e.getMessage());
        return ResponseEntity.notFound().build();
    }


    @ExceptionHandler
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        log.warn("DEBUG handleIllegalStateException: {}", e.getMessage());
        System.out.println("DEBUG handleIllegalStateException: " + e.getMessage());
        return ResponseEntity.badRequest().build();
    }




}
