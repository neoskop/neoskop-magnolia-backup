package de.neoskop.magnolia.backup.domain;

public class RestoreFirstTime {

    private static RestoreFirstTime singleton = null;

    public static RestoreFirstTime getRestoreFirstTime() {
        if (singleton == null) {
            return singleton = new RestoreFirstTime();
        } else {
            return singleton;
        }
    }

    private RestoreFirstTime() {}

    private boolean restoreFirstTime = false;

    public boolean isRestoreFirstTime() {
        return this.restoreFirstTime;
    }

    public void setRestoreFirstTime(boolean restoreFirstTime) {
        this.restoreFirstTime = restoreFirstTime;
    }


}
