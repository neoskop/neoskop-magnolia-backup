# README

Description of the module **Neoskop Magnolia Backup Module**.

# Dependencies

Until version 0.1.0:

- [Magnolia CMS][1] >= 6.1.4

As of version 0.2.0:

- [Magnolia CMS][1] >= 6.2.13

# Installation

The module must be added as a dependency in the `pom.xml` of the Magnolia project:

```xml
<dependency>
    <groupId>com.github.neoskop</groupId>
    <artifactId>neoskop-magnolia-backup</artifactId>
    <version>0.2.0</version>
</dependency>
```

# Use of the module

The module automatically exports and imports JCR workspaces for Magnolia CMS.

Currently the following functions are implemented:

- JCR export as backup with upload to a SFTP server.
- JCR import as restore with download from a SFTP server.

The necessary settings for the module can be stored via the environment variables or through the `magnolia.properties`.

Please note that the folder structure must already be created on the SFTP server. Therefore, for the following examples, the folders _/path/to/folder/for/backup/stage_ and _/path/to/folder/for/backup/local_ should be created on the SFTP server.

A brief explanation of the options:

- BACKUP_PROTOCOL: Include options for `sftp` and `s3`
- BACKUP_HOST: Host or IP address to the SFTP server, or the endpoint of your Amazon S3 bucket if s3 is selected
- BACKUP_USERNAME: Username for SFTP server (Not needed for `s3`)
- BACKUP_PASSWORD: Password from SFTP user (Not needed for `s3`)
- BACKUP_ACCESS_KEY: The access key for your Amazon S3 bucket (Not needed for `sftp`)
- BACKUP_SECRET_KEY: The secret access key for your Amazon S3 bucket (Not needed for `sftp`)
- BACKUP_REGION: The region of your Amazon S3 bucket (Not needed for `sftp`)
- BACKUP_BUCKET: The name of your Amazon S3 bucket (Not needed for `sftp`)
- BACKUP_PATH: Path to the backup directory
- BACKUP_REPOSITORIES: The workspaces to be saved can be specified via the ";" separator. If only a certain path is to be exported and imported from the workspace, it must be specified after the workspace with a ","
- BACKUP_PROJECT: Project name
- BACKUP_INSTANCE: Name of the instance, whether it is an author or public instance
- BACKUP_AUTO_ENABLED: Whether to create an automatic backup for the instance
- BACKUP_AUTO_ENVIRONMENT: Name of the current environment to be backed up automatically
- BACKUP_AUTO_CRONJOB: Cronjob string for the time of the automatic backup
- BACKUP_RESTORE_ENABLED: Whether a recovery should be performed when the module is installed on for the first time. **Attention for Stage or Live environments** this setting should be set to `false`. Only local environments should have this setting set to `true`
- BACKUP_RESTORE_ENVIRONMENT: Name of the environment from which a backup is to be imported if the project is started for the first time

# Examples for the settings

Example to pass settings via the environment variables:

```
BACKUP_PROTOCOL: "sftp"
BACKUP_HOST: "1.2.3.4"
BACKUP_USERNAME: "username"
BACKUP_PASSWORD: "password"
BACKUP_PATH: "/path/to/folder/for/backup"
BACKUP_REPOSITORIES: "website;users,/admin;config,/modules;config,/server;dam;"
BACKUP_PROJECT: "project-xyz"
BACKUP_INSTANCE: "author"
BACKUP_AUTO_ENABLED: "true"
BACKUP_AUTO_ENVIRONMENT: "local"
BACKUP_AUTO_CRONJOB: "0 0 3 ? * * *"
BACKUP_RESTORE_ENABLED: "true"
BACKUP_RESTORE_ENVIRONMENT: "stage"
```

Example to pass settings via magnolia.properties file:

```
neoskop.magnolia.backup.server.protocol=sftp
neoskop.magnolia.backup.server.host=1.2.3.4
neoskop.magnolia.backup.server.username=username
neoskop.magnolia.backup.server.password=password
neoskop.magnolia.backup.server.path=/path/to/folder/for/backup
neoskop.magnolia.backup.repositories=website;users,/admin;config,/modules;config,/server;dam;
neoskop.magnolia.backup.project=project-xyz
neoskop.magnolia.backup.instance=author
neoskop.magnolia.backup.auto.enabled=true
neoskop.magnolia.backup.auto.environment=local
neoskop.magnolia.backup.auto.cronjob=0 0 3 ? * * *
neoskop.magnolia.backup.restore.enabled=true
neoskop.magnolia.backup.restore.environment=stage
```

[1]: https://www.magnolia-cms.com
