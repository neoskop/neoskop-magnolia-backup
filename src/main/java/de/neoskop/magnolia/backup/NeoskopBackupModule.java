package de.neoskop.magnolia.backup;

import de.neoskop.magnolia.backup.commands.Restore;
import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import de.neoskop.magnolia.backup.domain.RestoreFirstTime;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is optional and represents the configuration for the
 * neoskop-magnolia-backup module.
 * By exposing simple getter/setter/adder methods, this bean can be configured
 * via content2bean
 * using the properties and node from
 * <tt>config:/modules/neoskop-magnolia-backup</tt>.
 * If you don't need this, simply remove the reference to this class in the
 * module descriptor xml.
 * See https://documentation.magnolia-cms.com/display/DOCS/Module+configuration
 * for information about module configuration.
 */
public class NeoskopBackupModule implements ModuleLifecycle {
    private static Logger log = LoggerFactory.getLogger(NeoskopBackupModule.class.getName());

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        if (RestoreFirstTime.getRestoreFirstTime().isRestoreFirstTime()
                && BackupConfiguration.getRestoreEnabled()) {
            log.debug("Start restore backup");
            try {
                new Restore().execute();
            } catch (Exception e) {
                log.error("Restore failed", e);
            }
        }
    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }
}
