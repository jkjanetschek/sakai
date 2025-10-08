package org.sakaiproject.content.tool;



import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.messaging.api.AbstractUserNotificationHandler;
import org.sakaiproject.messaging.api.UserNotificationData;
import org.sakaiproject.user.api.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.content.api.ContentResource;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




@Slf4j
@Component
public class ContentHostingUserNotificationHandler extends AbstractUserNotificationHandler {

    private Pattern pattern = Pattern.compile("\\/private\\/(profileImages)\\/", Pattern.CASE_INSENSITIVE);

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;


    @Resource
    private ContentHostingService contentHostingService;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(contentHostingService.EVENT_RESOURCE_AVAILABLE, contentHostingService.EVENT_RESOURCE_UPD_VISIBILITY,contentHostingService.EVENT_RESOURCE_UPD_ACCESS);
    }

    @Override
    public Optional<List<UserNotificationData>> handleEvent(Event e) {
        String ref = e.getResource();
        String context = e.getContext();
        String from = e.getUserId();
        String event = e.getEvent();



        List<UserNotificationData> bhEvents = new ArrayList<UserNotificationData>();

        try{

            log.debug("EVENT_RESOURCE_A " + context + " Res " + ref + " Time " + e.getEventTime() + "Event " + e.getEvent());
            String titel = contentHostingService.getLabel();
            log.debug("is in Dropbox " + contentHostingService.isInDropbox(ref.substring("/content".length())));
            String contentUrl = ref.substring("/content".length());

            //is dropbox?
            if (contentHostingService.isInDropbox(contentUrl)) {
                String dropBoxId = contentHostingService.getIndividualDropboxId(contentUrl);
                log.debug("IndividualDropboxId " + dropBoxId);
                String dropboxTitle = "";
                String[] parts = dropBoxId.split("/");
                String user = parts[parts.length - 1];
                try {
                    String url = contentHostingService.getUrl(contentUrl);
                    ResourceProperties rbProps = contentHostingService.getProperties(dropBoxId);
                    dropboxTitle = rbProps.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
                    if (!user.equals(from)) {

                        bhEvents.add(new UserNotificationData(from, user, context, ("dropbox " + dropboxTitle), url, "sakai.dropbox"));

                        //  doInsert(from, bd.getTo(), event, ref, bd.getTitle(), bd.getSiteId(), e.getEventTime(), bd.getUrl());
                        //    doAcademicInsert(from, user, event, ref, ("dropbox " + dropboxTitle), context, e.getEventTime(), url);
                    }
                } catch (PermissionException pee) {
                    log.warn("PermissionException trying to get title for individual dropbox: " + dropBoxId);
                } catch (IdUnusedException ide) {
                    log.warn("IdUnusedException trying to get title for individual dropbox: " + dropBoxId);
                    log.warn("Not an individual dropbox ");

                } finally {
                    //
                }
                log.debug("dropbox title " + dropboxTitle);

                //not Dropbox and we are only interested in Resources
            }else if(!contentHostingService.isAttachmentResource(contentUrl)){

                Matcher matcher = pattern.matcher(contentUrl);
                while (matcher.find()) {
                    if (!matcher.group(1).isEmpty()) {
                        return Optional.empty();
                    }
                }
                //hidden but allow access to its content should not generate a bullhorn message
                String [] pathPartsContent = contentUrl.split("/");
                boolean hidden = false;
                ///URL: /content/group/Seite/../../../ we are only interested in the points
                //maybe better to move to an other class
                for (int i = pathPartsContent.length - 1; i >= 3; i--) {
                    String pathSubString = contentUrl;
                    String checkPath = pathSubString.substring(0, pathSubString.indexOf(pathPartsContent[i]));
                    ResourceProperties resourceProperties = null;
                    try{
                        resourceProperties = contentHostingService.getProperties(checkPath);
                    }catch( PermissionException | IdUnusedException  ex){
                        log.error("Caught exception while accessing properties of ContentHostingService", ex);
                    }

                    hidden = "true".equals(resourceProperties.getProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT));
                    if (hidden) {
                        break;
                    }
                }
                if(hidden != true) {
                    hidden = false;
                }

                //JJ: to determine if Availability of uploaded item = hidden || visible
                ContentResource resource = null;
                try{
                    resource = contentHostingService.getResource(contentUrl);
                }catch (TypeException ex){
                    log.error(ex.getMessage(),ex);
                }

                if (contentHostingService.isAvailable(contentUrl) &&(!hidden) &&(!contentHostingService.isCollection(contentUrl)) ) {
                    String [] title = ref.split("/");
                    titel = title[title.length - 1];
                    for (User u : securityService.unlockUsers(ContentHostingService.AUTH_RESOURCE_READ, ref)) {
                        if (!u.getId().equals(from) && !securityService.isSuperUser(u.getId())) {
                            if(resource.isAvailable() || (resource != null && resource.isHidden() && securityService.unlock(u.getId(),ContentHostingService.AUTH_RESOURCE_HIDDEN,ref))){

                                String url = contentHostingService.getUrl(contentUrl);
                                bhEvents.add(new UserNotificationData(from, u.getId(), context, titel, url,"sakai.resources"));
                                // doAcademicInsert(from, u.getId(), event, ref, titel, context, e.getEventTime(), url);
                            }
                        }
                    }

                }
            }

            return  Optional.of(bhEvents);
        }catch (Exception ex) {
            log.error("Caught exception while handling events in ContentHostingBullhornHandler", ex);
        }

        return Optional.empty();
    }


}
