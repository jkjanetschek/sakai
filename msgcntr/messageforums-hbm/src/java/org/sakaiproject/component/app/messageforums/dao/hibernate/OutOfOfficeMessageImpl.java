package org.sakaiproject.component.app.messageforums.dao.hibernate;


import org.sakaiproject.api.app.messageforums.OutOfOfficeMessage;
import java.time.LocalDate;
import java.util.Date;




public class OutOfOfficeMessageImpl implements OutOfOfficeMessage {


    private String userID;
    private Date untilDate;


    public OutOfOfficeMessageImpl(){}

    public OutOfOfficeMessageImpl(String userID, Date untilDate){
        this.userID = userID;
        this.untilDate = untilDate;
    }

    public Date getUntilDate(){
        return untilDate;
    }
    public void setUntilDate(Date untilDate){
        this.untilDate = untilDate;
    }
    

    
    public String getUserID(){
        return userID;
    }
    public void setUserID(String userID){
        this.userID = userID;
    }



}