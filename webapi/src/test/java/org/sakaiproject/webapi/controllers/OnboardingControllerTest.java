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
package org.sakaiproject.webapi.controllers;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.webapi.beans.OnboardingCourse;
import org.sakaiproject.webapi.beans.OnboardingCourseStatus;
import org.sakaiproject.webapi.service.OnboardingService;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OnboardingControllerTest {

    private MockMvc mockMvc;
    private OnboardingService onboardingService;

    @Before
    public void setUp() {

        onboardingService = mock(OnboardingService.class);

        OnboardingController controller = new OnboardingController();
        ReflectionTestUtils.setField(controller, "onboardingService", onboardingService);

        SessionManager sessionManager = mock(SessionManager.class);
        Session session = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getUserId()).thenReturn("adrian");

        controller.setSessionManager(sessionManager);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void getOnboardingCoursesReturnsCourseList() throws Exception {

        OnboardingCourse course = OnboardingCourse.builder()
            .id("onboarding-it-security")
            .siteId("onboarding-it-security")
            .title("IT Security Fundamentals")
            .status(OnboardingCourseStatus.COMPLETED)
            .percentComplete(100)
            .dueDate("2026-06-15")
            .build();

        when(onboardingService.getCoursesForUser("adrian")).thenReturn(List.of(course));

        mockMvc.perform(get("/users/me/onboarding-courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.courses[0].id").value("onboarding-it-security"))
            .andExpect(jsonPath("$.courses[0].status").value("COMPLETED"))
            .andExpect(jsonPath("$.courses[0].percentComplete").value(100));
    }

    @Test
    public void getOnboardingCourseForSiteReturnsSingleCourse() throws Exception {

        OnboardingCourse course = OnboardingCourse.builder()
            .id("onboarding-benefits")
            .siteId("onboarding-benefits")
            .title("Benefits Overview")
            .status(OnboardingCourseStatus.NOT_STARTED)
            .percentComplete(0)
            .dueDate("2026-08-01")
            .build();

        when(onboardingService.getCourseForUserAndSite("adrian", "onboarding-benefits")).thenReturn(Optional.of(course));

        mockMvc.perform(get("/sites/onboarding-benefits/onboarding-courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.course.title").value("Benefits Overview"));
    }

    @Test
    public void getOnboardingCourseForSiteReturnsEmptyBodyWhenSiteIsNotAnOnboardingCourse() throws Exception {

        when(onboardingService.getCourseForUserAndSite("adrian", "not-onboarding")).thenReturn(Optional.empty());

        mockMvc.perform(get("/sites/not-onboarding/onboarding-courses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.course").doesNotExist());
    }
}
