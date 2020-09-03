package org.sakaiproject.component.app.messageforums;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;


import org.springframework.orm.hibernate4.support.HibernateDaoSupport;
import org.sakaiproject.api.app.messageforums.OutOfOfficeMessageManager;
import org.sakaiproject.api.app.messageforums.OutOfOfficeMessage;
import org.sakaiproject.component.app.messageforums.dao.hibernate.OutOfOfficeMessageImpl;
import org.sakaiproject.tool.api.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Calendar;

import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.ZoneId;



public class OutOfOfficeMessageManagerImpl extends HibernateDaoSupport implements OutOfOfficeMessageManager{

    private static final Logger logger = LoggerFactory.getLogger(OutOfOfficeMessageManagerImpl.class);

    private SessionFactory sessionFactory;
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;
    private OutOfOfficeMessage outOfOfficeMessage;
    private SessionManager sessionManager;


    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
    public void setSessionManager(SessionManager sessionManager){
        this.sessionManager = sessionManager;
    }



    public OutOfOfficeMessageManagerImpl(){}
    public void init() {}


    public int checkDate(OutOfOfficeMessage msg){
        Date untilDate = msg.getUntilDate();
        Date today = new Date();
        today.setHours(untilDate.getHours());
        today.setMinutes(untilDate.getMinutes());
        today.setSeconds(untilDate.getSeconds());
        LocalDate dateUntil = untilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateToday = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return dateUntil.compareTo(dateToday); // < 0 = date is past
    }


    public OutOfOfficeMessage getOutOfOfficeMessage (String UserID) {
        OutOfOfficeMessage msg = getHibernateTemplate().get(OutOfOfficeMessageImpl.class, UserID);
       if (msg != null){
            // until Date is past
           if (checkDate(msg) < 0){
               deleteOutOfOfficeMessageInvalidDate(UserID);
                return null;
            }
           return msg;
       }else{
           return null;
       }
    }


    public void deleteOutOfOfficeMessageInvalidDate(String UserID){
        String id = sessionManager.getCurrentSessionUserId();
        if (UserID.contains(id)){
            deleteOutOfOfficeMessage();
        }else{
            getHibernateTemplate().delete(getHibernateTemplate().get(OutOfOfficeMessageImpl.class,UserID));
        }
    }



    public void deleteOutOfOfficeMessage (){
        String id = sessionManager.getCurrentSessionUserId();
        getHibernateTemplate().delete(getHibernateTemplate().get(OutOfOfficeMessageImpl.class,id));
    }




    public void saveOutOfOfficeMessage (Date date){
          OutOfOfficeMessage outOfOfficeMessage = new OutOfOfficeMessageImpl();
          String id = sessionManager.getCurrentSessionUserId();
          outOfOfficeMessage.setUserID(id);
          outOfOfficeMessage.setUntilDate(date);

      /*
          Session  session = getHibernateTemplate().getSessionFactory().openSession();
          session.setFlushMode(FlushMode.COMMIT);
          Transaction tx = session.beginTransaction();
          session.saveOrUpdate(outOfOfficeMessage);
          tx.commit();
          session.close();
       */

        HibernateTemplate template = getHibernateTemplate();
        template.saveOrUpdate(outOfOfficeMessage);

    }





}

