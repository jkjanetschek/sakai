/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.service;

import java.util.List;
import java.util.Optional;

import org.sakaiproject.webapi.beans.OnboardingCourse;

/**
 * Explicit seam for onboarding-course progress data. The current implementation
 * returns deterministic sample data; a future implementation can swap in a real
 * data source (LMS completion data, gradebook-derived progress, etc.) without
 * changing {@link org.sakaiproject.webapi.controllers.OnboardingController}.
 */
public interface OnboardingService {

    List<OnboardingCourse> getCoursesForUser(String userId);

    Optional<OnboardingCourse> getCourseForUserAndSite(String userId, String siteId);
}
