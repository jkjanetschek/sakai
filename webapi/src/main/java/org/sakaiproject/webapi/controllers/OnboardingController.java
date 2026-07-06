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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.api.Session;
import org.sakaiproject.webapi.service.OnboardingService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class OnboardingController extends AbstractSakaiApiController {

    @Resource
    private OnboardingService onboardingService;

    @GetMapping(value = "/users/me/onboarding-courses", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getOnboardingCourses() {

        Session session = checkSakaiSession();

        Map<String, Object> data = new HashMap<>();
        data.put("courses", onboardingService.getCoursesForUser(session.getUserId()));
        return data;
    }

    @GetMapping(value = "/sites/{siteId}/onboarding-courses", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getOnboardingCourseForSite(@PathVariable String siteId) {

        Session session = checkSakaiSession();

        Map<String, Object> data = new HashMap<>();
        onboardingService.getCourseForUserAndSite(session.getUserId(), siteId)
            .ifPresent(course -> data.put("course", course));
        return data;
    }
}
