package org.sakaiproject.api.app.messageforums;


import java.util.Locale;
import java.util.ResourceBundle;


public interface ResourceLoaderHelper {

    public ResourceBundle loadBundleHelper (String baseName, Locale loc);
}