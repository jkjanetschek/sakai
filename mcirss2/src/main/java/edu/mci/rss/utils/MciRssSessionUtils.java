package edu.mci.rss.utils;

import org.sakaiproject.tool.api.ContextSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MciRssSessionUtils {

    @Autowired
    private  SessionManager sessionManager;

   // public MciRssSessionUtils()

    public  void switchToUserAndOrEid(String userId, String eid) {
        System.out.println("switchToUserAndOrEid");
        Session session = sessionManager.getCurrentSession();
        session.setUserId(userId);
        if (eid != null) {
            session.setUserEid(eid);
        }
    }


    // TODO development: remove
    public void logCurrentSession() {
        Session session = sessionManager.getCurrentSession();
        System.out.println("DEBUG userId: " + session.getUserId());
        System.out.println("DEBUG userEid: " + session.getUserEid());
    }

}