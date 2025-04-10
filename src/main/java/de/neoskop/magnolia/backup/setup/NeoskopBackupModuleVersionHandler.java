package de.neoskop.magnolia.backup.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import java.util.Arrays;
import java.util.List;

/**
 * This class is optional and lets you manage the versions of your module, by
 * registering "deltas"
 * to maintain the module's configuration, or other type of content. If you
 * don't need this, simply
 * remove the reference to this class in the module descriptor xml.
 *
 * @see info.magnolia.module.DefaultModuleVersionHandler
 * @see info.magnolia.module.ModuleVersionHandler
 * @see info.magnolia.module.delta.Task
 */
public class NeoskopBackupModuleVersionHandler extends DefaultModuleVersionHandler {

    public NeoskopBackupModuleVersionHandler() {}

    @Override
    protected List<Task> getStartupTasks(InstallContext installContext) {
        return Arrays.asList(new RestoreFirstTimeTask(), new ReplaceBackupCronjobTimeTask());
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        return Arrays.asList(new RestoreFirstTimeFlagTask());
    }
}
