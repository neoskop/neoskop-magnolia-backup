package de.neoskop.magnolia.backup.setup;

import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import de.neoskop.magnolia.backup.domain.RestoreFirstTime;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.RepositoryException;

/**
 * This class is used as a workaround, because a restore should only be done after a complete
 * installation to avoid any complications.
 */
public class RestoreFirstTimeFlagTask extends AbstractRepositoryTask {


    private static Logger log = LoggerFactory.getLogger(RestoreFirstTimeFlagTask.class.getName());

    RestoreFirstTimeFlagTask() {
        super(null, null);
    }

    @Override
    protected void doExecute(InstallContext ctx)
            throws RepositoryException, TaskExecutionException {
        if (BackupConfiguration.getRestoreEnabled()) {
            log.debug("Start restore first time activate");
            RestoreFirstTime.getRestoreFirstTime().setRestoreFirstTime(true);
        }
    }
}
