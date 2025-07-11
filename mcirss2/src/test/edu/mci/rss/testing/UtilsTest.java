package edu.mci.rss.testing;


import edu.mci.rss.utils.NewsItemFilterCriteriaUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.component.cover.ComponentManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@RunWith(SpringRunner.class)
@WebAppConfiguration // weil --> AnnotationConfigWebApplicationContext
@ContextConfiguration(classes = {TestConfiguration.class})
public class UtilsTest {


    private NewsItemFilterCriteriaUtils filterCriteriaUtils;
    private Instant now;

    // init ComponentManager in testing mode before dependecies are resolved
    static {
        try {
            Class<?> clazz = ComponentManager.class;
            Field testingModeField = clazz.getDeclaredField("testingMode");
            testingModeField.setAccessible(true);
            testingModeField.set(null, true);
            ComponentManager.getInstance();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("ComponentManager could not set to testing mode for Junit Run", e);
        }
    }

   @Before
    public void setUp() {
        // Mock the current time
        now = Instant.now();
        filterCriteriaUtils = new NewsItemFilterCriteriaUtils();
    }

/**
 * Filter NewsItems according to the following rules: If no newsitems within
 * the TIME_RANGE_SHORT (2 weeks), display up to ten items from
 * TIME_RANGE_LONG. Else display only items from TIME_RANGE_SHORT.
*/
    @Test
    public void testNewsWithinShortTimeRage () {

        Assert.assertTrue("Time should be in time Range Short",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(5)));

        Instant before14Days = createInstantMinusGivenDays(14);
        Instant timeRangeShort = filterCriteriaUtils.getTimeRangeShortInstant();
        Duration leeway = Duration.ofMillis(30000);
        Duration diff =Duration.between(timeRangeShort, before14Days).abs();

        if (diff.compareTo(leeway) > 0) {
            Assert.fail("Difference between timeRangeShort and news Items from 14 days ago is bigger than leeway of 30 seconds");
        }

        Assert.assertTrue("Time should be in time Range Short",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(now));

        Assert.assertFalse("Time should not be returned as to add because of time previously set to Short",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(16)));

    }


    @Test
    public void testNewsWithinLongTimeRage () {

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(15)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(17)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(44)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(63)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(87)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(111)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(123)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(123)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(145)));

        Assert.assertTrue("Time should be in time Range Long",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(55)));

        Instant before180Days = createInstantMinusGivenDays(180);
        Instant timeRangeLong = filterCriteriaUtils.getTimeRangeLongInstant();
        Duration leeway = Duration.ofMillis(30000);
        Duration diff =Duration.between(timeRangeLong, before180Days).abs();

        if (diff.compareTo(leeway) > 0) {
            Assert.fail("Difference between timeRangeLong and news Items from 180 days ago is bigger than leeway of 30 seconds");
        }

        Assert.assertFalse("Item should not be added as count of 10 longTimeRange items is reached",
                filterCriteriaUtils.checkTimeRangeAndCountOfItem(createInstantMinusGivenDays(22)));

    }

    private Instant createInstantMinusGivenDays(int days) {
        return now.minus(days, ChronoUnit.DAYS);
    }

    @Test
    public void testListOfSortedItems() {}








}
