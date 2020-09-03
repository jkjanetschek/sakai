package org.sakaiproject.api.app.messageforums;
import  org.sakaiproject.api.app.messageforums.OutOfOfficeMessage;
import java.util.Date;

public interface OutOfOfficeMessageManager {

    public OutOfOfficeMessage getOutOfOfficeMessage (String UserID);
    public void saveOutOfOfficeMessage (Date date);
    public void deleteOutOfOfficeMessage ();





}