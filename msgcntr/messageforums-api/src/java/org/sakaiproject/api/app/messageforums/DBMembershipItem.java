/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/DBMembershipItem.java $
 * $Id: DBMembershipItem.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;


public interface DBMembershipItem extends MutableEntity {

  public String getName();
  
  public void setName(String name);
    
  public Integer getType();
  
  public void setType(Integer type);;
  
  public String getPermissionLevelName();
  
  public void setPermissionLevelName(String permissionLevelName);
  
  public PermissionLevel getPermissionLevel();

  public void setPermissionLevel(PermissionLevel permissionLevel);

  public void setTopic(Topic topic);
}
