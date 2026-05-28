/**
 * Copyright (c) 2003-2024 The Apereo Foundation
 * <p>
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://opensource.org/licenses/ecl2
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lessonbuildertool.tool.producers;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PagePickerProducerTest {

    private PagePickerProducer producer;
    private SimplePageToolDao dao;

    private static final Long PAGE_ID = 42L;

    @Before
    public void before() {
        dao = mock(SimplePageToolDao.class);
        producer = new PagePickerProducer();
        producer.setSimplePageToolDao(dao);
    }

    @Test
    public void hasSubPages_returnsTrueWhenPageHasSubpages() {
        // item with type PAGE (type = 2)
        SimplePageItem subpage = new SimplePageItemImpl(1, PAGE_ID, 1, SimplePageItem.PAGE, "10", "A Subpage");
        when(dao.findItemsOnPage(PAGE_ID)).thenReturn(Collections.singletonList(subpage));

        Assert.assertTrue(producer.hasSubPages(PAGE_ID));
    }

    @Test
    public void hasSubPages_returnsFalseWhenPageHasNoSubpages() {
        // items with non-PAGE types: RESOURCE (1) and TEXT (5)
        SimplePageItem resource = new SimplePageItemImpl(2, PAGE_ID, 1, SimplePageItem.RESOURCE, "/content/file.pdf", "A Resource");
        SimplePageItem text = new SimplePageItemImpl(3, PAGE_ID, 2, SimplePageItem.TEXT, "/text/1", "Some Text");
        List<SimplePageItem> items = Arrays.asList(resource, text);
        when(dao.findItemsOnPage(PAGE_ID)).thenReturn(items);

        Assert.assertFalse(producer.hasSubPages(PAGE_ID));
    }

    @Test
    public void hasSubPages_returnsFalseWhenPageHasNoItems() {
        when(dao.findItemsOnPage(PAGE_ID)).thenReturn(Collections.emptyList());

        Assert.assertFalse(producer.hasSubPages(PAGE_ID));
    }

    @Test
    public void hasSubPages_returnsTrueWhenMixedItemsContainSubpage() {
        // mix of RESOURCE and PAGE items; hasSubPages must return true as long as one PAGE item exists
        SimplePageItem resource = new SimplePageItemImpl(4, PAGE_ID, 1, SimplePageItem.RESOURCE, "/content/file.pdf", "A Resource");
        SimplePageItem subpage = new SimplePageItemImpl(5, PAGE_ID, 2, SimplePageItem.PAGE, "20", "A Subpage");
        List<SimplePageItem> items = Arrays.asList(resource, subpage);
        when(dao.findItemsOnPage(PAGE_ID)).thenReturn(items);

        Assert.assertTrue(producer.hasSubPages(PAGE_ID));
    }
}
