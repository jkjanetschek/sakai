package org.sakaiproject.portal.beans.bullhornhandlers;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.portal.api.BullhornData;
import org.sakaiproject.user.api.User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.sakaiproject.assignment.api.AssignmentConstants.EVENT_ADD_ASSIGNMENT;
import static org.sakaiproject.assignment.api.AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_ACCESS;

@Slf4j
@Component
public class ContentHostingBullhornHandler extends AbstractBullhornHandler{



    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalTransactionManager")
    private PlatformTransactionManager transactionManager;

    @Resource(name = "org.sakaiproject.springframework.orm.hibernate.GlobalSessionFactory")
    private SessionFactory sessionFactory;

    @Inject
    private ContentHostingService contentHostingService;

    @Override
    public List<String> getHandledEvents() {
        return Arrays.asList(contentHostingService.EVENT_RESOURCE_AVAILABLE, contentHostingService.EVENT_RESOURCE_UPD_VISIBILITY,contentHostingService.EVENT_RESOURCE_UPD_ACCESS);
    }

    @Override
    public Optional<List<BullhornData>> handleEvent(Event e, Cache<String, Long> countCache) {
        String ref = e.getResource();
        String context = e.getContext();
        String from = e.getUserId();
        String event = e.getEvent();



        List<BullhornData> bhEvents = new ArrayList<>();

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

                        bhEvents.add(new BullhornData(from, user, context, ("dropbox " + dropboxTitle), url));

                        //  doInsert(from, bd.getTo(), event, ref, bd.getTitle(), bd.getSiteId(), e.getEventTime(), bd.getUrl());
                        //    doAcademicInsert(from, user, event, ref, ("dropbox " + dropboxTitle), context, e.getEventTime(), url);
                        countCache.remove(user);
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

                if (contentHostingService.isAvailable(contentUrl) &&(!hidden) &&(!contentHostingService.isCollection(contentUrl)) ) {
                    String [] title = ref.split("/");
                    titel = title[title.length - 1];
                    for (User u : securityService.unlockUsers(ContentHostingService.AUTH_RESOURCE_READ, ref)) {
                        if (!u.getId().equals(from) && !securityService.isSuperUser(u.getId())) {
                            String url = contentHostingService.getUrl(contentUrl);
                            bhEvents.add(new BullhornData(from, u.getId(), context, titel, url));
                            // doAcademicInsert(from, u.getId(), event, ref, titel, context, e.getEventTime(), url);
                            countCache.remove(u.getId());
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






/*

else if (ContentHostingService.EVENT_RESOURCE_AVAILABLE.equals(event) || ContentHostingService.EVENT_RESOURCE_UPD_VISIBILITY.equals(event) || ContentHostingService.EVENT_RESOURCE_UPD_ACCESS.equals(event)) {
                                log.debug("EVENT_RESOURCE_A " + context + " Res " + ref + " Time " + e.getEventTime() + "Event " + e.getEvent());
                                    String titel = contentHostingService.getLabel();
                                    log.debug("is in Dropbox " + contentHostingService.isInDropbox(ref.substring("/content".length())));
                                    String contentUrl = ref.substring("/content".length());
                                    //is dropbox?
                                    if (contentHostingService.isInDropbox(contentUrl)) {
                                        String dropBoxId = contentHostingService.getIndividualDropboxId(contentUrl);
                                        log.debug("IndividualDropboxId " + dropBoxId);
                                        String dropboxTitle = "";
                                        String [] parts = dropBoxId.split("/");
                                        String user = parts[parts.length - 1];
                                        try
                                        {
                                            String url = contentHostingService.getUrl(contentUrl);
                                            switchToAdmin();
                                            ResourceProperties rbProps = contentHostingService.getProperties(dropBoxId);
                                            dropboxTitle = rbProps.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
                                            if (!user.equals(from)) {
                                                doAcademicInsert(from, user, event, ref, ("dropbox " + dropboxTitle), context, e.getEventTime(), url);
                                                countCache.remove(user);
                                            }
                                        }

                                        catch (PermissionException pee)
                                        {
                                            log.warn("PermissionException trying to get title for individual dropbox: " + dropBoxId);
                                        }
                                        catch (IdUnusedException ide)
                                        {
                                            log.warn("IdUnusedException trying to get title for individual dropbox: " + dropBoxId);
                                            log.warn("Not an individual dropbox ");
                                        }
                                        finally {
                                            switchToNull();
                                        }
                                        log.debug("dropbox title " + dropboxTitle);

                                        //not Dropbox and we are only interested in Resources
                                    } else if(!contentHostingService.isAttachmentResource(contentUrl)){
                                        //hidden but allow access to its content should not generate a bullhorn message
                                        switchToAdmin();
                                        String [] pathPartsContent = contentUrl.split("/");
                                        boolean hidden = false;
                                        ///URL: /content/group/Seite/../../../ we are only interested in the points
                                        //maybe better to move to an other class
                                        for (int i = pathPartsContent.length - 1; i >= 3; i--) {
                                            String pathSubString = contentUrl;
                                            String checkPath = pathSubString.substring(0, pathSubString.indexOf(pathPartsContent[i]));
                                            ResourceProperties resourceProperties = contentHostingService.getProperties(checkPath);
                                            hidden = "true".equals(resourceProperties.getProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT));
                                            if (hidden) {
                                                break;
                                            }
                                        }
                                        if(hidden != true) {
                                            hidden = false;
                                        }
                                            switchToNull();
                                            if (contentHostingService.isAvailable(contentUrl) &&(!hidden) &&(!contentHostingService.isCollection(contentUrl)) ) {
                                                String [] title = ref.split("/");
                                                titel = title[title.length - 1];
                                                for (User u : securityService.unlockUsers(ContentHostingService.AUTH_RESOURCE_READ, ref)) {
                                                    if (!u.getId().equals(from) && !securityService.isSuperUser(u.getId())) {
                                                        String url = contentHostingService.getUrl(contentUrl);
                                                        doAcademicInsert(from, u.getId(), event, ref, titel, context, e.getEventTime(), url);
                                                        countCache.remove(u.getId());
                                                    }
                                                }

                                            }
                                    }
                        } else {
                            log.debug("SOME OTHER EVENT" + event.toString());
                        }



 */