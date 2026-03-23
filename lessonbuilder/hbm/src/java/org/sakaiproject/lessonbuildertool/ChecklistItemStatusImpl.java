/**********************************************************************************
 * $URL: $
 * $Id: $
 * **********************************************************************************
 * <p>
 * Author: David P. Bauer, dbauer1@udayton.edu
 * <p>
 * Copyright (c) 2016, University of Dayton
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.opensource.org/licenses/ECL-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool;

import java.io.Serializable;

public class ChecklistItemStatusImpl implements ChecklistItemStatus, Serializable {

    private static final long serialVersionUID = 1L;

    private ChecklistItemStatusId id;
    private boolean done; // Has this item been completed?

    /* MCI SAKAIME-866 */
    private java.util.Date checkedAt;


    public ChecklistItemStatusImpl() {
        id = new ChecklistItemStatusId();
    }

    public ChecklistItemStatusImpl(long checklistId, long checklistItemId, String owner) {
        this.id = new ChecklistItemStatusId();
        this.id.setChecklistId(checklistId);
        this.id.setChecklistItemId(checklistItemId);
        this.id.setOwner(owner);
        this.done = false;
        this.checkedAt = null; // not checked yet
    }

    public ChecklistItemStatusImpl(long checklistId, long checklistItemId, String owner, boolean done) {
        this.id = new ChecklistItemStatusId();
        this.id.setChecklistId(checklistId);
        this.id.setChecklistItemId(checklistItemId);
        this.id.setOwner(owner);
        this.done = done;
        this.checkedAt = done ? new java.util.Date() : null;
    }

    public ChecklistItemStatusId getId() {
        return id;
    }

    public void setId(ChecklistItemStatusId id) {
        this.id = id;
    }

    public long getChecklistId() {
        return id.getChecklistId();
    }

    public long getChecklistItemId() {
        return id.getChecklistItemId();
    }

    public String getOwner() {
        return id.getOwner();
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        if (done && !this.done && this.checkedAt == null) {
            this.checkedAt = new java.util.Date();
        }
        this.done = done;

        if (!done) { this.checkedAt = null; }
    }

	public java.util.Date getCheckedAt() { return checkedAt; }
	public void setCheckedAt(java.util.Date checkedAt) { this.checkedAt = checkedAt; }

}