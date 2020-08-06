package org.sakaiproject.util;

import org.sakaiproject.util.ResourceLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.component.cover.ComponentManager;



/*
 *  helper class for Out-Of-Office Notification Feature
 *   to loads correct Date Format from Language properties file
 */


public class ResourceLoaderHelper extends ResourceLoader{


    public ResourceLoaderHelper(){
        super();
    }

    public ResourceLoaderHelper(String baseName){
        super(baseName);
    }


    private final static Object LOCK = new Object();
    private static ThreadLocalManager threadLocalManager;
    protected static ThreadLocalManager getThreadLocalManager() {
        if (threadLocalManager == null) {
            synchronized (LOCK) {
                threadLocalManager = (ThreadLocalManager) ComponentManager.get(ThreadLocalManager.class);
            }
        }
        return threadLocalManager;
    }


    public ResourceBundle loadBundleHelper (String baseName, Locale loc){
        String context = (String) getThreadLocalManager().get(org.sakaiproject.util.RequestFilter.CURRENT_CONTEXT);
        ResourceLoaderHelper rb = new ResourceLoaderHelper(baseName);
        return rb.loadBundle(context,loc);
    }


}