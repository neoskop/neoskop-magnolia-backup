package de.neoskop.magnolia.backup.configuration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BackupRotation {

    public static final String ROTATION_FOLDER = "rotation";

    public static List<String> getSlotFileNames(LocalDate date) {
        List<String> slotFileNames = new ArrayList<>();
        slotFileNames.add(getSlotFileName(getDailySlotSuffix(date)));
        if (date.getDayOfWeek() == BackupConfiguration.getRotationWeeklyDay()) {
            slotFileNames.add(getSlotFileName(
                    getWeeklySlotSuffix(date, BackupConfiguration.getRotationWeeklyCount())));
        }
        return slotFileNames;
    }

    static String getDailySlotSuffix(LocalDate date) {
        return date.getDayOfWeek().name().toLowerCase(Locale.ENGLISH);
    }

    static String getWeeklySlotSuffix(LocalDate date, int weeklyCount) {
        return "weekly-" + getWeeklySlotIndex(date, weeklyCount);
    }

    static int getWeeklySlotIndex(LocalDate date, int weeklyCount) {
        return (int) ((date.toEpochDay() / 7) % weeklyCount) + 1;
    }

    private static String getSlotFileName(String suffix) {
        return BackupConfiguration.getProject() + "-" + BackupConfiguration.getInstance() + "-"
                + suffix + ".zip";
    }
}
