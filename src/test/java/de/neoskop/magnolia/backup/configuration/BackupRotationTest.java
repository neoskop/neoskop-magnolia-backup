package de.neoskop.magnolia.backup.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import java.time.LocalDate;

public class BackupRotationTest {

    @Test
    public void dailySlotSuffixIsLowercaseEnglishWeekday() {
        assertEquals("monday", BackupRotation.getDailySlotSuffix(LocalDate.of(2026, 7, 6)));
        assertEquals("sunday", BackupRotation.getDailySlotSuffix(LocalDate.of(2026, 7, 5)));
    }

    @Test
    public void weeklySlotIndexStaysWithinBounds() {
        LocalDate monday = LocalDate.of(2026, 7, 6);
        for (int week = 0; week < 10; week++) {
            int index = BackupRotation.getWeeklySlotIndex(monday.plusWeeks(week), 4);
            assertTrue(index >= 1 && index <= 4);
        }
    }

    @Test
    public void weeklySlotIndexAdvancesByOneAndWrapsAfterFourWeeks() {
        LocalDate monday = LocalDate.of(2026, 7, 6);
        int first = BackupRotation.getWeeklySlotIndex(monday, 4);
        assertEquals(first % 4 + 1, BackupRotation.getWeeklySlotIndex(monday.plusWeeks(1), 4));
        assertEquals(first, BackupRotation.getWeeklySlotIndex(monday.plusWeeks(4), 4));
    }

    @Test
    public void weeklySlotSuffixContainsIndex() {
        LocalDate monday = LocalDate.of(2026, 7, 6);
        int index = BackupRotation.getWeeklySlotIndex(monday, 4);
        assertEquals("weekly-" + index, BackupRotation.getWeeklySlotSuffix(monday, 4));
    }
}
