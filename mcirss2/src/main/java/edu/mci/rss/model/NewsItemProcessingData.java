package edu.mci.rss.model;



import lombok.AllArgsConstructor;
import lombok.Data;
import org.sakaiproject.messaging.api.model.UserNotification;
import org.springframework.lang.Nullable;


import java.util.Date;


@Data
@AllArgsConstructor
public  class NewsItemProcessingData {


    private String userId;
    private UserNotification userNotification;
    @Nullable //informational
    private Date dueDate;

    public NewsItemProcessingData(String userId, UserNotification userNotification) {
        this.userId = userId;
        this.userNotification = userNotification;
    }

}