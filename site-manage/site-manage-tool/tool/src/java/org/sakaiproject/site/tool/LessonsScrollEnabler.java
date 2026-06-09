/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.site.tool;

import org.sakaiproject.cheftool.Context;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.util.SiteConstants;
import org.sakaiproject.util.ParameterParser;

/**
 * SAK-52363 Per-site toggle for the Lessons "scroll to last edited/added item" feature.
 * <p>
 * Unlike {@link SubNavEnabler} the site property is tri-state: absent means
 * "use the instance default" (lessonbuilder.scrolltoitem.default, true by default),
 * so a user choice is always written explicitly as true/false and never removed.
 */
public class LessonsScrollEnabler {

    private static final String ENABLED_KEY = "isLessonsScrollEnabled";
    private static final String CONTEXT_STATE_KNOWN_KEY = "lessonsScrollStateKnown";


    /**
     * Add the Lessons scroll settings to the context for the edit tools page
     *
     * @param context the context
     * @param site    the site
     * @return true if context was modified
     */
    public static boolean addToContext(Context context, Site site) {
        if (context == null || site == null) return false;

        context.put(ENABLED_KEY, isEnabledForSite(site));

        return true;
    }

    /**
     * Applies the Lessons scroll settings to the state
     *
     * @param state  the state
     * @param params the params
     * @return true if the state was modified
     */
    public static boolean applySettingsToState(SessionState state, ParameterParser params) {
        if ("on".equalsIgnoreCase(params.getString(ENABLED_KEY))) {
            state.setAttribute(ENABLED_KEY, true);
        } else {
            state.setAttribute(ENABLED_KEY, false);
        }

        return true;
    }


    /**
     * Add the current Lessons scroll state to the context for the edit tools confirmation page
     *
     * @param context the context
     * @param state   the state
     * @return true if the context was modified
     */
    public static boolean addStateToEditToolsConfirmationContext(Context context, SessionState state) {

        if (context == null || state == null || state.getAttribute(ENABLED_KEY) == null) {
            return false;
        }

        final boolean isEnabled = (Boolean) state.getAttribute(ENABLED_KEY);
        context.put(ENABLED_KEY, isEnabled);
        context.put(CONTEXT_STATE_KNOWN_KEY, Boolean.TRUE);

        return true;
    }


    /**
     * When the user makes a choice, write the site property explicitly;
     * an absent property means the instance default applies.
     *
     * @param site  The site
     * @param state the session state
     * @return true if the site properties were modified
     */
    public static boolean prepareSiteForSave(Site site, SessionState state) {
        if (site == null || state == null) return false;

        if (state.getAttribute(ENABLED_KEY) != null) {
            final boolean isEnabled = (Boolean) state.getAttribute(ENABLED_KEY);
            final ResourcePropertiesEdit props = site.getPropertiesEdit();
            props.addProperty(SiteConstants.LESSONS_SCROLL_SITE_PROP, String.valueOf(isEnabled));
        }

        return true;
    }


    /**
     * Remove the Lessons scroll setting from the state
     *
     * @param state the state
     * @return true if the state was modifed
     */
    public static boolean removeFromState(SessionState state) {
        if (state != null) {
            state.removeAttribute(ENABLED_KEY);
            return true;
        }

        return false;
    }


    /**
     * Check the site's properties for the Lessons scroll property,
     * falling back to the instance-wide default when unset
     *
     * @param site The site to check
     * @return true if the Lessons scroll feature is enabled for the site
     */
    private static boolean isEnabledForSite(Site site) {
        final String property = site.getProperties().getProperty(SiteConstants.LESSONS_SCROLL_SITE_PROP);
        if (property != null) {
            return Boolean.parseBoolean(property);
        }
        return ServerConfigurationService.getBoolean(SiteConstants.LESSONS_SCROLL_SAKAI_PROP, SiteConstants.LESSONS_SCROLL_ENABLED_DEFAULT);
    }
}
