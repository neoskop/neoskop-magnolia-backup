package de.neoskop.magnolia.backup.setup;

import de.neoskop.magnolia.backup.commands.Restore;
import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import de.neoskop.magnolia.backup.domain.RestoreFirstTime;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.RepositoryException;

public class RestoreFirstTimeTask extends AbstractRepositoryTask {

    private static Logger log = LoggerFactory.getLogger(RestoreFirstTimeTask.class.getName());

    RestoreFirstTimeTask() {
        super(null, null);
    }

    @Override
    protected void doExecute(InstallContext ctx)
            throws RepositoryException, TaskExecutionException {
        if (RestoreFirstTime.getRestoreFirstTime().isRestoreFirstTime()
                && BackupConfiguration.getRestoreEnabled() && BackupConfiguration.getRestoreDuringStartup()) {
            log.debug("Start restore backup during startup");
            new Restore().execute();
        }
    }
}