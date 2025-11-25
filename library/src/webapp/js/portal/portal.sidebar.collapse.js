class SidebarCollapseButton {

  get collapsed() {
    return this._element.getAttribute("data-portal-sidebar-collapsed") === "true" ? true : false;
  }

  set collapsed(newValue) {

    this._element.setAttribute("data-portal-sidebar-collapsed", newValue);
    this.setCollapsed(newValue)
  }
    //jkj: element = sidebar-collapse-button
  constructor(element, config) {

    this._i18n = config?.i18n;
    this._toggleClass = config?.toggleClass;
    this._portalContainer = config?.portalContainer;
    this._sitesSidebar = config?.sitesSidebar;
    this._resizeHandle = config?.resizeHandle;
    this._mainContainer = config?.mainContainer;
    this._mainContent = config?.mainContent;
    this._element = element;
    this._element.addEventListener("click", this.toggle.bind(this));
  }
     calcVw(px) {
        return (px / window.innerWidth) * 100;
    }

    setContenWidth() {
        const defaultSidebarWidth = this._resizeHandle.defaultSidebarWidthPx;
        const offsetPx = this._mainContent.offsetContentToContainerWidthPx;

        const sidebarWidthVw = this.calcVw(defaultSidebarWidth);
        const offsetVw = this.calcVw(offsetPx);
        const contentWidth = 100 - sidebarWidthVw - offsetVw;

        this._mainContent.style.width = `${contentWidth}vw`;

    }

    resetSideBarResizeHandle() {
        if (this.collapsed) {
            localStorage.removeItem("sidebarWidth");
            this._mainContainer.style.width = "";
            this._sitesSidebar.style.width  = "";
            this._mainContent.style.width   = "";
            this._portalContainer.style.gridTemplateColumns = "";
            return;
        }

            const sidebarMinWidth = this._sitesSidebar.style.getPropertyValue("min-width");

            const offsetHandlePx = this._resizeHandle.offSetHandlePropertyPx;

            this._portalContainer.style.gridTemplateColumns = `${sidebarMinWidth} 1fr`;

            const sidebarWidthValue = Number.parseFloat(sidebarMinWidth);
            const containerWidthVw = 100 - sidebarWidthValue;
            this._mainContainer.style.width = `${containerWidthVw}vw`;

            this.setContenWidth();

            this._sitesSidebar.style.width  = sidebarMinWidth;

            const resizeHandleDisplayProp =  this._resizeHandle.style.getPropertyValue('display');
            if (resizeHandleDisplayProp === 'none') {
                this._resizeHandle.style.removeProperty('display');
            }
            const resizeHandleLeftVw =  sidebarWidthValue + this.calcVw(offsetHandlePx);

            this._resizeHandle.style.left = `${resizeHandleLeftVw}vw`;
    }

    toggle() {

    this.collapsed = !this.collapsed;
    this._portalContainer.classList.toggle(this._toggleClass, this.collapsed);
    this._sitesSidebar.classList.toggle(this._toggleClass, this.collapsed);
    this._resizeHandle.classList.toggle(this._toggleClass, this.collapsed);
    this._element.title = this.collapsed ? this._i18n.titleCollapsed : this._i18n.titleExpanded;
    this.resetSideBarResizeHandle();

    const iconElement = this._element.querySelector("span");
    if (iconElement) {
        const collapsedIconClass = "portal-nav-sidebar-icon-collapsed";
        const expandedIconClass = "portal-nav-sidebar-icon";
        iconElement.classList.toggle(collapsedIconClass, this.collapsed);
        iconElement.classList.toggle(expandedIconClass, !this.collapsed);
    }
  }



  async setCollapsed(collapsed) {

    if (!portal?.user?.id) {
      return; // Exit the function early if the user is not logged in.
    }

    collapsed = collapsed ? "true" : "false";
    const putReq = await fetch(`/direct/userPrefs/updateKey/${portal.user.id}/sakai:portal:sitenav?sidebarCollapsed=${collapsed}`, { method: "PUT" });
    if (!putReq.ok) {
      console.error(`Could not set collapsed state "${collapsed}" for sidebar.`);
    }
  }
}
