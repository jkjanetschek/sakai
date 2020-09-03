package org.sakaiproject.api.app.messageforums;
import java.time.LocalDate;
import java.util.Date;


public interface OutOfOfficeMessage {

   // public static final String message = ": Abwesenheit bis";

    public String getUserID();

    public void setUserID(String UserID);

  //  public String getUntilDate();

   // public void setUntilDate(String untilDate);


    public Date getUntilDate();
    public void setUntilDate(Date date);



}