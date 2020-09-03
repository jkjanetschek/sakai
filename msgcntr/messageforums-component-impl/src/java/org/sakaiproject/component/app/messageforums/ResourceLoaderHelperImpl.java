package org.sakaiproject.component.app.messageforums;

import org.sakaiproject.util.ResourceLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.api.app.messageforums.ResourceLoaderHelper;

/*
 *  helper class for Out-Of-Office Notification Feature
 *   to loads correct Date Format from Language properties file
 */


public class ResourceLoaderHelperImpl extends ResourceLoader implements ResourceLoaderHelper{


    public ResourceLoaderHelperImpl(){
        super();
    }

    public ResourceLoaderHelperImpl(String baseName){
        super(baseName);
    }


    private final static Object LOCK = new Object();
    private static ThreadLocalManager threadLocalManager;
    public void setThreadLocalManager(ThreadLocalManager threadLocalManager){
        this.threadLocalManager = threadLocalManager;
    }
    protected static ThreadLocalManager getThreadLocalManager() {
        if (threadLocalManager == null) {
            synchronized (LOCK) {
                threadLocalManager = (ThreadLocalManager) ComponentManager.get(ThreadLocalManager.class);
            }
        }
        return threadLocalManager;
    }


    public ResourceBundle loadBundleHelper (String baseName, Locale loc){
        String context = (String) getThreadLocalManager().get("org.sakaiproject.util.RequestFilter.context");
        ResourceLoaderHelperImpl rb = new ResourceLoaderHelperImpl(baseName);
        return rb.loadBundle(context,loc);
    }


}