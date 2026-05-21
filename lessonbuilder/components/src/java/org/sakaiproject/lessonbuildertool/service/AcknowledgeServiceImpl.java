/**
 * Copyright (c) 2008 The Sakai Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.osedu.org/licenses/ECL-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool.service;

import org.sakaiproject.lessonbuildertool.api.AcknowledgeService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.lessonbuildertool.ChecklistItemStatus;
import org.sakaiproject.lessonbuildertool.SimpleChecklistItem;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.api.UserAcknowledgeInfo;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class AcknowledgeServiceImpl implements AcknowledgeService {
	private static final String               SIMPLE_PAGE_ITEM_MCI_ACKNOWLEDGEMENT_DE = "Bestätigung der Kenntnisnahme";

	private static final String               SIMPLE_PAGE_ITEM_MCI_ACKNOWLEDGEMENT_EN = "Acknowledgement";

	@Setter
	private              SimplePageToolDao    simplePageToolDao;

	@Setter
	private              UserDirectoryService userDirectoryService;

	@Setter
	private              SqlService           sqlService;

	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

	// Collect constants into a Set for fast lookups
	private static final Set<String> ACKNOWLEDGEMENT_NAMES = Set.of(
			SIMPLE_PAGE_ITEM_MCI_ACKNOWLEDGEMENT_DE,
			SIMPLE_PAGE_ITEM_MCI_ACKNOWLEDGEMENT_EN
	);

	public AcknowledgeServiceImpl() {
		//simplePageToolDao = (SimplePageToolDao) ComponentManager.get("org.sakaiproject.lessonbuildertool.model.SimplePageToolDao");
		logRetrievalResult("simplePageToolDao", simplePageToolDao);
		logRetrievalResult("userDirectoryService", userDirectoryService);
		logRetrievalResult("sqlService", sqlService);
	}

	/**
	 * Returns a list of UserAcknowledgeInfo if the site contains a single checklist qualifying for acknowledgements.
	 *
	 * @param siteId
	 * @return
	 * @throws UserNotDefinedException
	 */
	public List<UserAcknowledgeInfo> getAllAcknowledgementsForSite(String siteId) throws UserNotDefinedException {
		SimplePageItem pageItemQualifyingForAcknowledgements = findPageItemQualifyingForAcknowledgements(siteId);
		return getAllAcknowledgementsForPage(pageItemQualifyingForAcknowledgements.getPageId());
	}

	/**
	 * Returns a list of UserAcknowledgeInfo if the page contains a single checklist qualifying for acknowledgements.
	 *
	 * @param pageId
	 * @return
	 * @throws UserNotDefinedException
	 */
	public List<UserAcknowledgeInfo> getAllAcknowledgementsForPage(long pageId) throws UserNotDefinedException {
		SimplePageItem pageItemQualifyingForAcknowledgements = findPageItemQualifyingForAcknowledgementsByPageId(pageId);
		SimplePage simplePage = simplePageToolDao.getPage(pageItemQualifyingForAcknowledgements.getPageId());
		List<ChecklistItemStatus> checklistItemStatuses = findAcknowledgementChecklistItem(pageItemQualifyingForAcknowledgements);
		return mapCheckListItemStatusListToUserAcknowledgeInfoList(checklistItemStatuses,
				pageItemQualifyingForAcknowledgements,
				simplePage);
	}

	private List<UserAcknowledgeInfo> mapCheckListItemStatusListToUserAcknowledgeInfoList(List<ChecklistItemStatus> checklistItemStatuses,
			SimplePageItem pageItemQualifyingForAcknowledgements,
			SimplePage simplePage) throws UserNotDefinedException {
		log.info("ZoneId.systemDefault(): {}", ZoneId.systemDefault());
		List<UserAcknowledgeInfo> result = new ArrayList<>();
		if (checklistItemStatuses != null) {
			for (ChecklistItemStatus checklistItemStatus : checklistItemStatuses) {
				if (checklistItemStatus.isDone()) {
					String owner = checklistItemStatus.getOwner();
					User userInSakai = userDirectoryService.getUser(owner);
					result.add(new UserAcknowledgeInfo(
							userInSakai,
							// ZoneId of MariaDB is "System" which correlates to "CET"
							checklistItemStatus.getCheckedAt() != null
									? LocalDateTime.ofInstant(checklistItemStatus.getCheckedAt().toInstant(), ZoneId.systemDefault())
									: null,
							simplePage.getSiteId()));
				}
			}
		}
		return result;
	}

	/**
	 * Returns whether the given pageId contains a single checklist qualifying for acknowledgements.
	 *
	 * @param pageId
	 * @return
	 */
	public boolean hasAcknowledgementChecklist(long pageId) {
		try {
			findPageItemQualifyingForAcknowledgementsByPageId(pageId);
			return true;
		} catch (Exception e) {
			// Not an error, it just means the tested page has no acknowledgement list
			return false;
		}
	}

	private SimplePageItem findPageItemQualifyingForAcknowledgementsByPageId(Long pageId) {
		List<SimplePageItem> matchingItems = simplePageToolDao.findItemsOnPage(pageId).stream()
				.filter(item -> item.getType() == SimplePageItem.CHECKLIST)
				.filter(item -> ACKNOWLEDGEMENT_NAMES.contains(item.getName()))
				.collect(Collectors.toList());
		if (matchingItems.isEmpty()) {
			throw new IllegalStateException("No acknowledgement checklist found on page " + pageId);
		}
		if (matchingItems.size() > 1) {
			throw new IllegalStateException("Multiple acknowledgement checklists found on page " + pageId);
		}
		return matchingItems.get(0);
	}

	private SimplePageItem findPageItemQualifyingForAcknowledgements(String siteId) {
		List<SimplePage> pages = simplePageToolDao.getSitePages(siteId);
		List<SimplePageItem> matchingPageItems = pages.stream()
				.map(simplePage -> {
					try {
						return findPageItemQualifyingForAcknowledgementsByPageId(simplePage.getPageId());
					} catch (IllegalStateException e) {
						// ignore pages without a valid acknowledgement checklist
						return null;
					}
				})
				.filter(Objects::nonNull) // remove nulls
				.collect(Collectors.toList());
		if (matchingPageItems.isEmpty()) {
			throw new IllegalStateException("No acknowledgement checklist with name " + ACKNOWLEDGEMENT_NAMES + " found for site: " + siteId);
		}
		if (matchingPageItems.size() > 1) {
			throw new IllegalStateException("Multiple acknowledgement checklists with name " + ACKNOWLEDGEMENT_NAMES + " found for site: " + siteId);
		}
		return matchingPageItems.get(0);
	}

	private List<ChecklistItemStatus> findAcknowledgementChecklistItem(SimplePageItem simplePageItem) {
		List<SimpleChecklistItem> checklistItems = simplePageToolDao.findChecklistItems(simplePageItem);
		if (checklistItems.isEmpty()) {
			throw new IllegalStateException("Acknowledgement checklist found but contains no items for pageId: " + simplePageItem.getPageId());
		}
		if (checklistItems.size() > 1) {
			throw new IllegalStateException("Multiple acknowledgement checklist items found for pageId: " + simplePageItem.getPageId());
		}
		SimpleChecklistItem checklistItem = checklistItems.get(0);
		return simplePageToolDao.findChecklistItemStatusesForChecklistItem(simplePageItem.getId(), checklistItem.getId());
	}

	private Optional<LocalDateTime> getLastLessonBuilderUpdate(long pageId, String userId) {
		String eventName = "lessonbuilder.page.read";
		String sql = "SELECT MAX(event_date) " +
				"FROM sakai_event " +
				"INNER JOIN sakai_session ON sakai_event.SESSION_ID = sakai_session.SESSION_ID " +
				"WHERE EVENT = ? " +
				"AND ref = ? " +
				"AND sakai_session.SESSION_USER = ?";
		Object[] params = {
				eventName,
				"/lessonbuilder/page/" + pageId,
				userId
		};
		try {
			List<String> rows = sqlService.dbRead(sql, params, null);
			if (rows == null || rows.isEmpty() || rows.get(0) == null) {
				log.debug("No " + eventName + " event found for pageId {}", pageId);
				return Optional.empty();
			}
			String timeString = rows.get(0);
			LocalDateTime localDateTime = LocalDateTime.parse(timeString, formatter);
			return Optional.of(localDateTime);

		} catch (Exception e) {
			log.error("Error reading last lessonbuilder.item.update for pageId {}: {}", pageId, e.getMessage(), e);
			return Optional.empty();
		}
	}

	private void logRetrievalResult(String componentName, Object service) {
		if (service == null) {
			log.warn("{} is null", componentName);
		} else {
			log.debug("{} loaded successfully", componentName);
		}
	}
}
