package de.neoskop.magnolia.backup.configuration;

import de.neoskop.magnolia.backup.domain.Repository;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.objectfactory.Components;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackupConfiguration {
    private static final String TMP_BACKUP = "/tmp/backup";

    private static final String TMP_IMPORT = "/tmp/import";

    private static final String REPOSITORIES = "/repositories";

    private static final String BACKUP_SERVER_PROTOCOL_ENV = "BACKUP_PROTOCOL";
    private static final String BACKUP_SERVER_IP_ENV = "BACKUP_IP";
    private static final String BACKUP_SERVER_USERNAME_ENV = "BACKUP_USERNAME";
    private static final String BACKUP_SERVER_PASSWORD_ENV = "BACKUP_PASSWORD";
    private static final String BACKUP_SERVER_PATH_ENV = "BACKUP_PATH";
    private static final String BACKUP_REPOSITORIES_ENV = "BACKUP_REPOSITORIES";
    private static final String BACKUP_PROJECT_ENV = "BACKUP_PROJECT";
    private static final String BACKUP_INSTANCE_ENV = "BACKUP_INSTANCE";

    private static final String BACKUP_RESTORE_ENVIRONMENT_ENV = "BACKUP_RESTORE_ENVIRONMENT";
    private static final String BACKUP_RESTORE_ENABLED_ENV = "BACKUP_RESTORE_ENABLED";

    private static final String BACKUP_AUTO_ENVIRONMENT_ENV = "BACKUP_AUTO_ENVIRONMENT";
    private static final String BACKUP_AUTO_CRONJOB_ENV = "BACKUP_AUTO_CRONJOB";
    private static final String BACKUP_AUTO_ENABLED_ENV = "BACKUP_AUTO_ENABLED";

    private static final String BACKUP_SERVER_PROTOCOL_PROP =
            "neoskop.magnolia.backup.server.protocol";
    private static final String BACKUP_SERVER_IP_PROP = "neoskop.magnolia.backup.server.ip";
    private static final String BACKUP_SERVER_USERNAME_PROP =
            "neoskop.magnolia.backup.server.username";
    private static final String BACKUP_SERVER_PASSWORD_PROP =
            "neoskop.magnolia.backup.server.password";
    private static final String BACKUP_SERVER_PATH_PROP = "neoskop.magnolia.backup.server.path";
    private static final String BACKUP_REPOSITORIES_PROP = "neoskop.magnolia.backup.repositories";
    private static final String BACKUP_PROJECT_PROP = "neoskop.magnolia.backup.project";
    private static final String BACKUP_INSTANCE_PROP = "neoskop.magnolia.backup.instance";

    private static final String BACKUP_RESTORE_ENVIRONMENT_PROP =
            "neoskop.magnolia.backup.restore.environment";
    private static final String BACKUP_RESTORE_ENABLED_PROP =
            "neoskop.magnolia.backup.restore.enabled";

    private static final String BACKUP_AUTO_ENVIRONMENT_PROP =
            "neoskop.magnolia.backup.auto.environment";
    private static final String BACKUP_AUTO_CRONJOB_PROP = "neoskop.magnolia.backup.auto.cronjob";
    private static final String BACKUP_AUTO_ENABLED_PROP = "neoskop.magnolia.backup.auto.enabled";

    public static String getTmpBackupFolder() {
        return TMP_BACKUP;
    }

    public static String getTmpImportFolder() {
        return TMP_IMPORT;
    }

    public static String getRepositoriesFolder() {
        return REPOSITORIES;
    }

    public static String getTemporaryImportFilePath() {
        return getTmpImportFolder() + File.separator + getBackupFileName();
    }

    public static String getTenporaryBackupFilePath() {
        return getTmpBackupFolder() + File.separator + getBackupFileName();
    }

    public static String getTenporaryRepositoriesFolderPath() {
        return getTmpBackupFolder() + getRepositoriesFolder();
    }

    public static String getBackupFileName() {
        return BackupConfiguration.getProject() + "-" + BackupConfiguration.getInstance() + ".zip";
    }

    private static String getMagnoliaProperty(String property) {
        return Components.getComponent(MagnoliaConfigurationProperties.class).getProperty(property);
    }

    private static String getValueFromSystemEnvOrMgnlProp(String env, String prop) {
        if (StringUtils.isNotBlank(System.getenv(env))) {
            return System.getenv(env);
        } else if (StringUtils.isNotBlank(getMagnoliaProperty(prop))) {
            return getMagnoliaProperty(prop);
        }
        return null;
    }

    public static String getProtocol() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_SERVER_PROTOCOL_ENV,
                BACKUP_SERVER_PROTOCOL_PROP);
    }

    public static String getServerIp() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_SERVER_IP_ENV, BACKUP_SERVER_IP_PROP);
    }

    public static String getServerUsername() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_SERVER_USERNAME_ENV,
                BACKUP_SERVER_USERNAME_PROP);
    }

    public static String getServerPassword() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_SERVER_PASSWORD_ENV,
                BACKUP_SERVER_PASSWORD_PROP);
    }

    public static String getServerPath() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_SERVER_PATH_ENV, BACKUP_SERVER_PATH_PROP);
    }

    public static String getCronjob() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_AUTO_CRONJOB_ENV, BACKUP_AUTO_CRONJOB_PROP);
    }

    public static boolean getAutoBackupEnabled() {
        return Boolean.parseBoolean(
                getValueFromSystemEnvOrMgnlProp(BACKUP_AUTO_ENABLED_ENV, BACKUP_AUTO_ENABLED_PROP));
    }

    public static boolean getRestoreEnabled() {
        return Boolean.parseBoolean(getValueFromSystemEnvOrMgnlProp(BACKUP_RESTORE_ENABLED_ENV,
                BACKUP_RESTORE_ENABLED_PROP));
    }

    public static String getProject() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_PROJECT_ENV, BACKUP_PROJECT_PROP);
    }

    public static String getCurrentEnvironment() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_AUTO_ENVIRONMENT_ENV,
                BACKUP_AUTO_ENVIRONMENT_PROP);
    }

    public static String getInstance() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_INSTANCE_ENV, BACKUP_INSTANCE_PROP);
    }

    public static String getRestoreEnvironment() {
        return getValueFromSystemEnvOrMgnlProp(BACKUP_RESTORE_ENVIRONMENT_ENV,
                BACKUP_RESTORE_ENVIRONMENT_PROP);
    }

    public static List<Repository> getRepositories() {
        List<Repository> backupRepoList = new ArrayList<>();
        String backupRepoString = "";
        if (StringUtils.isNotBlank(System.getenv(BACKUP_REPOSITORIES_ENV))) {
            backupRepoString = System.getenv(BACKUP_REPOSITORIES_ENV);
        } else if (StringUtils.isNotBlank(getMagnoliaProperty(BACKUP_REPOSITORIES_PROP))) {
            backupRepoString = getMagnoliaProperty(BACKUP_REPOSITORIES_PROP);
        }
        String[] repositoriesArray = backupRepoString.split(";");
        for (String repositoryString : repositoriesArray) {
            String[] repository = repositoryString.split(",");
            if (repository.length > 1) {
                backupRepoList.add(new Repository(repository[0], repository[1]));
            } else {
                backupRepoList.add(new Repository(repository[0], "/"));
            }
        }

        return backupRepoList;

    }
}
