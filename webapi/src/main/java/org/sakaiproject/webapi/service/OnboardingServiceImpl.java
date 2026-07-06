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
import org.sakaiproject.webapi.beans.OnboardingCourseStatus;

import org.springframework.stereotype.Service;

@Service
public class OnboardingServiceImpl implements OnboardingService {

    /**
     * Fixed catalog of sample onboarding courses. Values are hand-picked
     * (not random) so responses are reproducible across requests and tests.
     */
    private static final List<OnboardingCourse> CATALOG = List.of(
        OnboardingCourse.builder()
            .id("onboarding-it-security")
            .siteId("onboarding-it-security")
            .title("IT Security Fundamentals")
            .status(OnboardingCourseStatus.COMPLETED)
            .percentComplete(100)
            .dueDate("2026-06-15")
            .build(),
        OnboardingCourse.builder()
            .id("onboarding-code-of-conduct")
            .siteId("onboarding-code-of-conduct")
            .title("Code of Conduct")
            .status(OnboardingCourseStatus.IN_PROGRESS)
            .percentComplete(60)
            .dueDate("2026-07-20")
            .build(),
        OnboardingCourse.builder()
            .id("onboarding-benefits")
            .siteId("onboarding-benefits")
            .title("Benefits Overview")
            .status(OnboardingCourseStatus.NOT_STARTED)
            .percentComplete(0)
            .dueDate("2026-08-01")
            .build(),
        OnboardingCourse.builder()
            .id("onboarding-workplace-safety")
            .siteId("onboarding-workplace-safety")
            .title("Workplace Safety")
            .status(OnboardingCourseStatus.IN_PROGRESS)
            .percentComplete(30)
            .dueDate("2026-07-28")
            .build(),
        OnboardingCourse.builder()
            .id("onboarding-data-privacy")
            .siteId("onboarding-data-privacy")
            .title("Data Privacy Basics")
            .status(OnboardingCourseStatus.NOT_STARTED)
            .percentComplete(0)
            .dueDate("2026-08-10")
            .build()
    );

    private static final int MIN_COURSES = 3;
    private static final int MAX_COURSES = 5;

    @Override
    public List<OnboardingCourse> getCoursesForUser(String userId) {

        int range = MAX_COURSES - MIN_COURSES + 1;
        int count = MIN_COURSES + Math.floorMod(userId.hashCode(), range);

        return CATALOG.subList(0, count);
    }

    @Override
    public Optional<OnboardingCourse> getCourseForUserAndSite(String userId, String siteId) {

        return getCoursesForUser(userId).stream()
            .filter(course -> course.getSiteId().equals(siteId))
            .findFirst();
    }
}
