package org.sakaiproject.component.app.postem;

import org.sakaiproject.api.app.postem.data.GradebookManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteRemovalAdvisor;
import org.sakaiproject.site.api.SiteService;

public class GradebookService implements SiteRemovalAdvisor {

    private SiteService siteService;
    private GradebookManager gradebookManager;

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setGradebookManager(GradebookManager gradebookManager){this.gradebookManager =gradebookManager;}

    public void init(){
        siteService.addSiteRemovalAdvisor(this);
    }

    @Override
    public void removed(Site site) {
        gradebookManager.hardDeleteGradebooks(site.getId());
    }
}
