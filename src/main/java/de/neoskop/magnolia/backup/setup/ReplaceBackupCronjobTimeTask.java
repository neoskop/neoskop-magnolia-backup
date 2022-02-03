package de.neoskop.magnolia.backup.setup;

import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class ReplaceBackupCronjobTimeTask extends AbstractRepositoryTask {


    private static Logger log =
            LoggerFactory.getLogger(ReplaceBackupCronjobTimeTask.class.getName());

    ReplaceBackupCronjobTimeTask() {
        super(null, null);
    }

    @Override
    protected void doExecute(InstallContext ctx)
            throws RepositoryException, TaskExecutionException {
        if (StringUtils.isNotBlank(BackupConfiguration.getCronjob())) {
            log.debug("Start replace backup cronjob time");
            MgnlContext.setInstance(MgnlContext.getSystemContext());
            final Session session = MgnlContext.getJCRSession("config");
            if (session.itemExists("/modules/scheduler/config/jobs/generateBackup")) {
                Property cronjobProperty =
                        session.getProperty("/modules/scheduler/config/jobs/generateBackup/cron");
                cronjobProperty.setValue(BackupConfiguration.getCronjob());
            }

        }
    }
}
