package de.neoskop.magnolia.backup.commands;

import de.neoskop.magnolia.backup.CustomJcrExportCommand;
import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import de.neoskop.magnolia.backup.domain.Repository;
import de.neoskop.magnolia.backup.transfer.BackupTransfer;
import de.neoskop.magnolia.backup.transfer.BackupTransferS3;
import de.neoskop.magnolia.backup.transfer.BackupTransferSftp;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.importexport.command.JcrExportCommand.Compression;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Backup extends BaseRepositoryCommand {

    public Compression compression = Compression.NONE;

    public String execute() {
        MgnlContext.setInstance(MgnlContext.getSystemContext());
        try {
            createTmpFolder();

            for (Repository repository : BackupConfiguration.getRepositories()) {
                backupChildren(repository.getWorkspace(), repository.getPath());
            }

            ZipOutputStream zipOut = new ZipOutputStream(
                    new FileOutputStream(BackupConfiguration.getTemporaryBackupFilePath()));
            final File backupFolder =
                    new File(BackupConfiguration.getTenporaryRepositoriesFolderPath());

            for (final File fileEntry : backupFolder.listFiles()) {
                FileInputStream fis = new FileInputStream(fileEntry);
                ZipEntry zipEntry = new ZipEntry(fileEntry.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[1024];
                int length;
                while ((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                fis.close();
            }

            zipOut.close();

            BackupTransfer backupTransfer;
            switch (BackupConfiguration.getProtocol()) {
                case "sftp":
                    backupTransfer = new BackupTransferSftp();
                    break;
                case "s3":
                    backupTransfer = new BackupTransferS3();
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unsupported protocol: " + BackupConfiguration.getProtocol());
            }
            backupTransfer.upload();
        } catch (Exception e) {
            log.error("backup failed", e);
            return "backup failed";

        }

        log.info("backup completed");
        return "backup completed";
    }

    private void createTmpFolder() {
        File directory = new File(BackupConfiguration.getTmpBackupFolder());
        if (!directory.exists()) {
            directory.mkdir();
        }
        directory = new File(BackupConfiguration.getTenporaryRepositoriesFolderPath());
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    private void backupChildren(String repository, String path) throws Exception {
        String pathName = DataTransporter.createExportPath(path);
        pathName = DataTransporter.encodePath(pathName, DataTransporter.DOT, DataTransporter.UTF8);
        if (DataTransporter.DOT.equals(pathName)) {
            pathName = StringUtils.EMPTY; // root node
        }
        String format = "xml";
        final String fileName = repository + pathName;

        OutputStream outputStream;

        switch (compression) {
            case ZIP:
                outputStream = new FileOutputStream(
                        BackupConfiguration.getTenporaryRepositoriesFolderPath() + File.separator
                                + fileName + "." + format + "." + "zip");
                final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
                zipOutputStream.putNextEntry(new ZipEntry(fileName + "." + format));
                outputStream = zipOutputStream;
                format = "zip"; // xml.zip
                break;
            case GZ:
                format += "." + "gz"; // xml.gz
                outputStream = new FileOutputStream(
                        BackupConfiguration.getTenporaryRepositoriesFolderPath() + File.separator
                                + fileName + "." + format);
                outputStream = new GZIPOutputStream(outputStream);
                break;
            default:
                outputStream = new FileOutputStream(
                        BackupConfiguration.getTenporaryRepositoriesFolderPath() + File.separator
                                + fileName + "." + format);
                break;
        }

        CustomJcrExportCommand customJcrExportCommand = new CustomJcrExportCommand();
        customJcrExportCommand.backupChildren(outputStream, repository, path);
    }

    @Override
    public boolean execute(Context context) throws Exception {
        if (!BackupConfiguration.getAutoBackupEnabled()) {
            log.info("For this instance auto backup is not enabled.");
            return true;
        }
        if ("backup completed".equals(this.execute())) {
            return true;
        }
        return false;
    }

}
