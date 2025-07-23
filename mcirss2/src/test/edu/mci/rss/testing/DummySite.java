package edu.mci.rss.testing;


import lombok.Data;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzRealmLockException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.user.api.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class DummySite implements Site {

    private String siteId;
    private String title;
    private String description;

    /**
     * Add a member to the AuthzGroup, or, if the user is currently a member, update
     * the membership with the supplied details.
     *
     * @param userId   The user.
     * @param roleId   The role name. If no role with this name exists in the group, an IllegalArgumentException will be thrown.
     * @param active   The active flag.
     * @param provided If true, from an external provider.
     */
    @Override
    public void addMember(String userId, String roleId, boolean active, boolean provided) {

    }

    /**
     * Create a new Role within this AuthzGroup.
     *
     * @param id The role id.
     * @return the new Role.
     * @throws IdUsedException if the id is already a Role in this AuthzGroup.
     */
    @Override
    public Role addRole(String id) throws RoleAlreadyDefinedException {
        return null;
    }

    /**
     * Create a new Role within this AuthzGroup, as a copy of this other role
     *
     * @param id    The role id.
     * @param other The role to copy.
     * @return the new Role.
     * @throws IdUsedException if the id is already a Role in this AuthzGroup.
     */
    @Override
    public Role addRole(String id, Role other) throws RoleAlreadyDefinedException {
        return null;
    }

    /**
     * @return the user who created this.
     */
    @Override
    public User getCreatedBy() {
        return null;
    }

    /**
     * Get the date created
     *
     * @return
     */
    @Override
    public Date getCreatedDate() {
        return null;
    }

    /**
     * Access the name of the role to use for giving a user membership with "maintain" access.
     *
     * @return The name of the "maintain" role.
     */
    @Override
    public String getMaintainRole() {
        return "";
    }

    /**
     * Access the user's membership record for this AuthzGroup; the role, and status flags.
     *
     * @param userId The user id.
     * @return The Membership record for the user in this AuthzGroup, or null if the use is not a member.
     */
    @Override
    public Member getMember(String userId) {
        return null;
    }

    /**
     * Access all Membership records defined for this AuthzGroup.
     *
     * @return The set of Membership records (Membership) defined for this AuthzGroup.
     */
    @Override
    public Set<Member> getMembers() {
        return Set.of();
    }

    /**
     * @return the user who last modified this.
     */
    @Override
    public User getModifiedBy() {
        return null;
    }

    /**
     * Get date last modified
     *
     * @return
     */
    @Override
    public Date getModifiedDate() {
        return null;
    }

    /**
     * Access the group id for the GroupProvider for this AuthzGroup.
     *
     * @return The the group id for the GroupProvider for this AuthzGroup, or null if none defined.
     */
    @Override
    public String getProviderGroupId() {
        return "";
    }

    /**
     * Access a Role defined in this AuthzGroup.
     *
     * @param id The role id.
     * @return The Role, if found, or null, if not.
     */
    @Override
    public Role getRole(String id) {
        return null;
    }

    /**
     * Access all Roles defined for this AuthzGroup.
     *
     * @return The set of roles (Role) defined for this AuthzGroup.
     */
    @Override
    public Set<Role> getRoles() {
        return Set.of();
    }

    /**
     * Access all roles that have been granted permission to this function.
     *
     * @param function The function to check.
     * @return The Set of role names (String) that have been granted permission to this function.
     */
    @Override
    public Set<String> getRolesIsAllowed(String function) {
        return Set.of();
    }

    /**
     * Access the active role for this user's membership.
     *
     * @param userId The user id.
     * @return The Role for this user's membership, or null if the user has no active membership.
     */
    @Override
    public Role getUserRole(String userId) {
        return null;
    }

    /**
     * Access all users who have active role membership in the AuthzGroup.
     *
     * @return The Set of users ids (String) who have active role membership in the AuthzGroup.
     */
    @Override
    public Set<String> getUsers() {
        return Set.of();
    }

    /**
     * Access all users who have an active role membership with this role.
     *
     * @param role
     * @return The Set of user ids (String) who have an active role membership with this role.
     */
    @Override
    public Set<String> getUsersHasRole(String role) {
        return Set.of();
    }

    /**
     * Access all users who have an active role membership to a role that is allowed this function.
     *
     * @param function The function to check.
     * @return The Set of user ids (String) who have an active role membership to a role that is allowed this function.
     */
    @Override
    public Set<String> getUsersIsAllowed(String function) {
        return Set.of();
    }

    /**
     * Test if this user has a membership in this AuthzGroup that has this role and is active.
     *
     * @param userId The user id.
     * @param role   The role name.
     * @return true if the User has has a membership in this AuthzGroup that has this role and is active.
     */
    @Override
    public boolean hasRole(String userId, String role) {
        return false;
    }

    /**
     * Test if this user is allowed to perform the function in this AuthzGroup.
     *
     * @param userId   The user id.
     * @param function The function to open.
     * @return true if this user is allowed to perform the function in this AuthzGroup, false if not.
     */
    @Override
    public boolean isAllowed(String userId, String function) {
        return false;
    }

    /**
     * Is this AuthzGroup empty of any roles or membership?
     *
     * @return true if the AuthzGroup is empty, false if not.
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Remove membership for for this user from the AuthzGroup.
     *
     * @param userId The user.
     */
    @Override
    public void removeMember(String userId) {

    }

    /**
     * Remove all membership from this AuthzGroup.
     */
    @Override
    public void removeMembers() {

    }

    /**
     * Remove this Role from this AuthzGroup. Any grants of this Role in the AuthzGroup are also removed.
     *
     * @param role The role name.
     */
    @Override
    public void removeRole(String role) {

    }

    /**
     * Remove all Roles from this AuthzGroup.
     */
    @Override
    public void removeRoles() {

    }

    /**
     * Set the role name to use for "maintain" access.
     *
     * @param role The name of the "maintain" role.
     */
    @Override
    public void setMaintainRole(String role) {

    }

    /**
     * Set the external group id for the GroupProvider for this AuthzGroup (set to null to have none).
     *
     * @param id The external group id for the GroupProvider, or null if there is to be none.
     */
    @Override
    public void setProviderGroupId(String id) {

    }

    /**
     * Adjust membership so that active members are all active in other, and inactive members are all defined in other
     *
     * @param other The other azg to adjust to.
     * @return true if any changes were made, false if not.
     */
    @Override
    public boolean keepIntersection(AuthzGroup other) {
        return false;
    }

    /**
     * Get the lock that exists for this AuthzGroup
     * <ul>
     *     <li>If no lock is present then return {@code RealmLockMode.NONE}</li>
     *     <li>If both {@code RealmLockMode.DELETE} and {@code RealmLockMode.MODIFY} locks are present then return {@code RealmLockMode.ALL}</li>
     *     <li>otherwise return the specific lock that exists</li>
     * </ul>
     *
     * @return RealmLockMode
     */
    @Override
    public RealmLockMode getRealmLock() {
        return null;
    }

    /**
     * Get the lock for a specific entity reference for this AuthzGroup
     *
     * @param reference the entity reference to check for a lock
     * @return RealmLockMode
     */
    @Override
    public RealmLockMode getLockForReference(String reference) {
        return null;
    }

    /**
     * Sets the current lock for the given entity reference for this AuthzGroup
     *
     * @param reference the entity reference
     * @param type      the lock type, setting this to {@code RealmLockMode.NONE} will remove the lock
     */
    @Override
    public void setLockForReference(String reference, RealmLockMode type) {

    }

    /**
     * Get all the references that have a lock on this realm.
     *
     * @return a List of arrays where the array contains the following positional data:
     * <ol start="0">
     *     <li>reference</li>
     *     <li>the lock mode</li>
     * </ol>
     */
    @Override
    public List<String[]> getRealmLocks() {
        return List.of();
    }

    /**
     * @return A short text Description of the site.
     */
    @Override
    public String getShortDescription() {
        return "";
    }

    /**
     * @return An HTML-safe version of the short Description of the site.
     */
    @Override
    public String getHtmlShortDescription() {
        return "";
    }

    /**
     * @return An HTML-safe version of the Description of the site.
     */
    @Override
    public String getHtmlDescription() {
        return "";
    }

    /**
     * @return The Site's icon URL.
     */
    @Override
    public String getIconUrl() {
        return "";
    }

    /**
     * @return The Site's icon URL as a full URL.
     */
    @Override
    public String getIconUrlFull() {
        return "";
    }

    /**
     * @return The Site's info display URL.
     */
    @Override
    public String getInfoUrl() {
        return "";
    }

    /**
     * @return The Site's info display URL as a full URL.
     */
    @Override
    public String getInfoUrlFull() {
        return "";
    }

    /**
     * @return true if this Site can be joined by anyone, false if not.
     */
    @Override
    public boolean isJoinable() {
        return false;
    }

    /**
     * @return the role name given to users who join a joinable site.
     */
    @Override
    public String getJoinerRole() {
        return "";
    }

    /**
     * @return the skin to use for this site.
     */
    @Override
    public String getSkin() {
        return "";
    }

    /**
     * @return the List (SitePage) of Site Pages.
     */
    @Override
    public List<SitePage> getPages() {
        return List.of();
    }

    /**
     * Make sure description, pages, tools, groups, and properties are loaded, not lazy
     */
    @Override
    public void loadAll() {

    }

    /**
     * @return The pages ordered by the tool order constraint for this site's type (as tool category), or the site's pages in defined order if the site is set to have a custom page order.
     */
    @Override
    public List<SitePage> getOrderedPages() {
        return List.of();
    }

    /**
     * @return true if the site is published, false if not.
     */
    @Override
    public boolean isPublished() {
        return false;
    }

    /**
     * Access the SitePage that has this id, if one is defined, else return null.
     *
     * @param id The id of the SitePage.
     * @return The SitePage that has this id, if one is defined, else return null.
     */
    @Override
    public SitePage getPage(String id) {
        return null;
    }

    /**
     * Access the ToolConfiguration that has this id, if one is defined, else return null. The tool may be on any SitePage in the site.
     *
     * @param id The id of the tool.
     * @return The ToolConfiguration that has this id, if one is defined, else return null.
     */
    @Override
    public ToolConfiguration getTool(String id) {
        return null;
    }

    /**
     * Get all the tools placed in the site on any page that are of any of these tool ids.
     *
     * @param toolIds The tool id array (String, such as sakai.chat, not a tool configuration / placement uuid) to search for.
     * @return A Collection (ToolConfiguration) of all the tools placed in the site on any page that are of this tool id (may be empty).
     */
    @Override
    public Collection<ToolConfiguration> getTools(String[] toolIds) {
        return List.of();
    }

    /**
     * Get all the tools placed in the site on any page for a particular common Tool Id.
     *
     * @param commonToolId The tool id (String, such as sakai.chat, not a tool configuration / placement uuid) to search for.
     * @return A Collection (ToolConfiguration) of all the tools placed in the site on any page that are of this tool id (may be empty).
     */
    @Override
    public Collection<ToolConfiguration> getTools(String commonToolId) {
        return List.of();
    }

    /**
     * Get the first tool placed on the site on any page with the specified common Tool id (such as sakai.chat)
     *
     * @param commonToolId The common ToolID to search for (i.e. sakai.chat)
     * @return ToolConfiguration for the tool which has the ID (if any) or null if no tools match.
     */
    @Override
    public ToolConfiguration getToolForCommonId(String commonToolId) {
        return null;
    }

    /**
     * Access the site type.
     *
     * @return The site type.
     */
    @Override
    public String getType() {
        return "";
    }

    /**
     * Test if the site is of this type. It is if the param is null.
     *
     * @param type A String type to match, or a String[], List or Set of Strings, any of which can match.
     * @return true if the site is of the type(s) specified, false if not.
     */
    @Override
    public boolean isType(Object type) {
        return false;
    }

    /**
     * Check if the site is marked for viewing.
     *
     * @return True if the site is marked for viewing, false if not
     */
    @Override
    public boolean isPubView() {
        return false;
    }

    /**
     * Get a site group
     *
     * @param id The group id (or reference).
     * @return The Group object if found, or null if not found.
     */
    @Override
    public Group getGroup(String id) {
        return null;
    }

    /**
     * Get a collection of the groups in a Site.
     *
     * @return A collection (Group) of groups defined in the site, empty if there are none.
     */
    @Override
    public Collection<Group> getGroups() {
        return List.of();
    }

    /**
     * Get a collection of the groups in a Site that have this user as a member.
     *
     * @param userId The user id.
     * @return A collection (Group) of groups defined in the site that have this user as a member, empty if there are none.
     */
    @Override
    public Collection<Group> getGroupsWithMember(String userId) {
        return List.of();
    }

    /**
     * Get a collection of the groups in a Site that have all these users as members.
     *
     * @param userIds@return A collection (Group) of groups defined in the site that have these users as members, empty if there are none.
     */
    @Override
    public Collection<Group> getGroupsWithMembers(String[] userIds) {
        return List.of();
    }

    /**
     * Get a collection of the groups in a Site that have this user as a member with this role.
     *
     * @param userId The user id.
     * @param role   The role.
     * @return A collection (Group) of groups defined in the site that have this user as a member with this role, empty if there are none.
     */
    @Override
    public Collection<Group> getGroupsWithMemberHasRole(String userId, String role) {
        return List.of();
    }

    /**
     * Get user IDs of members of a set of groups in this site
     *
     * @param groupIds IDs of authZ groups (AuthzGroup selection criteria),
     *                 a null groupIds includes all groups in the site, an empty set includes none of them
     * @return collection of user IDs who are in (members of) a set of site groups
     * @since 1.3.0
     */
    @Override
    public Collection<String> getMembersInGroups(Set<String> groupIds) {
        return List.of();
    }

    /**
     * Does the site have any groups defined?
     *
     * @return true if the site and has any groups, false if not.
     */
    @Override
    public boolean hasGroups() {
        return false;
    }

    /**
     * Set the url of an icon for the site.
     *
     * @param url The new icon's url.
     */
    @Override
    public void setIconUrl(String url) {

    }

    /**
     * Set the url for information about the site.
     *
     * @param url The new information url.
     */
    @Override
    public void setInfoUrl(String url) {

    }

    /**
     * Set the joinable status of the site.
     *
     * @param joinable represents whether the site is joinable (true) or not (false).
     */
    @Override
    public void setJoinable(boolean joinable) {

    }

    /**
     * Set the joiner role for a site.
     *
     * @param role the joiner role for a site.
     */
    @Override
    public void setJoinerRole(String role) {

    }

    /**
     * Set the short Description of the site. Used to give a short text description of the site.
     *
     * @param description The new short description.
     */
    @Override
    public void setShortDescription(String description) {

    }

    /**
     * Set the published state of this site.
     *
     * @param published The published state of the site.
     */
    @Override
    public void setPublished(boolean published) {

    }

    /**
     * Set the skin to use for this site.
     *
     * @param skin The skin to use for this site.
     */
    @Override
    public void setSkin(String skin) {

    }

    /**
     * Create a new site page and add it to this site.
     *
     * @return The SitePage object for the new site page.
     */
    @Override
    public SitePage addPage() {
        return null;
    }

    /**
     * Remove a site page from this site.
     *
     * @param page The SitePage to remove.
     */
    @Override
    public void removePage(SitePage page) {

    }

    /**
     * Generate a new set of pages and tools that have new, unique ids. Good if the site had non-unique-system-wide ids for pages and tools. The Site Id does not change.
     */
    @Override
    public void regenerateIds() {

    }

    /**
     * Set the site type.
     *
     * @param type The site type.
     */
    @Override
    public void setType(String type) {

    }

    /**
     * Set the site view.
     *
     * @param pubView The site view setting.
     */
    @Override
    public void setPubView(boolean pubView) {

    }

    /**
     * Add a new group. The Id is generated, the rest of the fields can be set using calls to the Group object returned.
     * NOTE: the title must be set before saving
     */
    @Override
    public Group addGroup() {
        return null;
    }

    /**
     * Remove this group from the groups for this site.
     *
     * @param group The group to remove.
     * @deprecated Use deleteGroup() instead.
     */
    @Override
    public void removeGroup(Group group) {

    }

    /**
     * Remove a group from the groups for this site.
     * Its functionallity is the same as removeMember but throws IllegalStateException.
     *
     * @param group The group to delete.
     */
    @Override
    public void deleteGroup(Group group) throws AuthzRealmLockException {

    }

    /**
     * Check if the site has a custom page order
     *
     * @return true if the site has a custom page order, false if not.
     */
    @Override
    public boolean isCustomPageOrdered() {
        return false;
    }

    /**
     * Set the site's custom page order flag.
     *
     * @param custom true if the site has a custom page ordering, false if not.
     */
    @Override
    public void setCustomPageOrdered(boolean custom) {

    }

    /**
     * Is this site softly deleted and hence queued for a hard delete?
     *
     * @return true if it has been softly deleted
     */
    @Override
    public boolean isSoftlyDeleted() {
        return false;
    }

    /**
     * If softly deleted, the date that occurred
     *
     * @return date if it has been softly deleted
     */
    @Override
    public Date getSoftlyDeletedDate() {
        return null;
    }

    /**
     * Set params for this site as softly deleted
     *
     * @param flag true or false
     */
    @Override
    public void setSoftlyDeleted(boolean flag) {

    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }

    /**
     * Check to see if the edit is still active, or has already been closed.
     *
     * @return true if the edit is active, false if it's been closed.
     */
    @Override
    public boolean isActiveEdit() {
        return false;
    }

    /**
     * Access the resource's properties for modification
     *
     * @return The resource's properties.
     */
    @Override
    public ResourcePropertiesEdit getPropertiesEdit() {
        return null;
    }

    /**
     * Access the internal reference which can be used to access the entity from within the system.
     *
     * @return The the internal reference which can be used to access the entity from within the system.
     */
    @Override
    public String getReference() {
        return "";
    }

    /**
     * Access the id of the entity.
     *
     * @return The id.
     */
    @Override
    public String getId() {
        return "";
    }


}
