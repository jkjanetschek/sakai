package edu.mci.rss.utils;

import edu.mci.rss.ItemRangeTrigger;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import java.time.Instant;

@Getter
@Setter
public class NewsItemFilterCriteriaUtils {

    // two weeks: all news
    public final long TIME_RANGE_SHORT = 1209600000l;

    // if no news in the last two weeks: display up to 10 news from the last 6
    // months
    public final long TIME_RANGE_LONG = 15552000000l;

    // Half year for calendar rss 15778800l
    //2 month = 62 days = 5356800 seconds
    public final long TIME_RANGE_CALENDAR_SECONDS = 5356800l;

    private final Instant timeRangeShortInstant;
    private final Instant timeRangeLongInstant;
    private ItemRangeTrigger itemRangeTrigger;
    private int longRangeItemCounter = 0;
    private final int MAX_LONG_RANGE_ITEMS = 10;

    public NewsItemFilterCriteriaUtils() {

        timeRangeShortInstant  = Instant.now().minusMillis(TIME_RANGE_SHORT);
        timeRangeLongInstant = Instant.now().minusMillis(TIME_RANGE_LONG);
        itemRangeTrigger = ItemRangeTrigger.LONG;
    }



    public boolean checkFilterCriteria(Instant newsTimeInstant) {
        return checkTimeRangeAndCountOfItem(newsTimeInstant);
    }


    public boolean checkTimeRangeAndCountOfItem (Instant newsTimeInstant) {
        boolean result = false;

        System.out.println("newsTime: " + Date.from(newsTimeInstant));
        System.out.println("timeRangeShortInstant: " + Date.from(timeRangeShortInstant));
        System.out.println("timeRangeLongInstant: " + Date.from(timeRangeLongInstant));

        if (newsTimeInstant.isAfter(timeRangeShortInstant)) {
            itemRangeTrigger = ItemRangeTrigger.SHORT;
            result =  true;
            System.out.println("range.SHORT");

        } else  if (ItemRangeTrigger.LONG.equals(itemRangeTrigger)
                && newsTimeInstant.isAfter(timeRangeLongInstant)
                && (longRangeItemCounter < MAX_LONG_RANGE_ITEMS)) {

            longRangeItemCounter++;
            result = true;
            System.out.println("range.LONG");
        }
        System.out.println("checkTimeRangeAndCountOfItem = " + result);
        return result;
    }

}


