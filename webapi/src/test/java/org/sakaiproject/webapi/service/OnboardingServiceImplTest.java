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

import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.webapi.beans.OnboardingCourse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OnboardingServiceImplTest {

    private OnboardingServiceImpl onboardingService;

    @Before
    public void setUp() {
        onboardingService = new OnboardingServiceImpl();
    }

    @Test
    public void getCoursesForUserReturnsBetweenThreeAndFiveEntries() {

        List<OnboardingCourse> courses = onboardingService.getCoursesForUser("adrian");

        assertTrue(courses.size() >= 3);
        assertTrue(courses.size() <= 5);
    }

    @Test
    public void getCoursesForUserIsDeterministicForTheSameUser() {

        List<OnboardingCourse> first = onboardingService.getCoursesForUser("adrian");
        List<OnboardingCourse> second = onboardingService.getCoursesForUser("adrian");

        assertEquals(first, second);
    }

    @Test
    public void getCourseForUserAndSiteReturnsMatchingCourse() {

        List<OnboardingCourse> courses = onboardingService.getCoursesForUser("adrian");
        String existingSiteId = courses.get(0).getSiteId();

        Optional<OnboardingCourse> result = onboardingService.getCourseForUserAndSite("adrian", existingSiteId);

        assertTrue(result.isPresent());
        assertEquals(existingSiteId, result.get().getSiteId());
    }

    @Test
    public void getCourseForUserAndSiteIsEmptyForUnknownSite() {

        Optional<OnboardingCourse> result = onboardingService.getCourseForUserAndSite("adrian", "not-an-onboarding-site");

        assertFalse(result.isPresent());
    }
}
