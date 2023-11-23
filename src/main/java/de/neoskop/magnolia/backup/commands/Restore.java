package de.neoskop.magnolia.backup.commands;

import de.neoskop.magnolia.backup.CustomDataTransporter;
import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import de.neoskop.magnolia.backup.domain.Repository;
import de.neoskop.magnolia.backup.transfer.RestoreTransfer;
import de.neoskop.magnolia.backup.transfer.RestoreTransferS3;
import de.neoskop.magnolia.backup.transfer.RestoreTransferSftp;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jcr.ImportUUIDBehavior;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class Restore extends BaseRepositoryCommand {

    private static Logger log = LoggerFactory.getLogger(Restore.class.getName());

    @Override
    public boolean execute(Context context) throws Exception {
        if ("restore completed".equals(this.execute())) {
            return true;
        }
        return false;
    }

    public String execute() {
        MgnlContext.setInstance(MgnlContext.getSystemContext());

        RestoreTransfer restoreTransfer;
        switch (BackupConfiguration.getProtocol()) {
            case "sftp":
                restoreTransfer = new RestoreTransferSftp();
                break;
            case "s3":
                restoreTransfer = new RestoreTransferS3();
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported protocol: " + BackupConfiguration.getProtocol());
        }
        if (!restoreTransfer.download()) {
            return "Download failed";
        }

        try (ZipFile zipFile = new ZipFile(BackupConfiguration.getTemporaryImportFilePath())) {

            for (Repository repository : BackupConfiguration.getRepositories()) {
                String entryName = repository.getFilename() + ".xml";
                ZipEntry entry = zipFile.getEntry(entryName);

                if (entry != null) {
                    InputStream fileInputStream = zipFile.getInputStream(entry);

                    String name = repository.getFilename();
                    String repositoryName = StringUtils.substringBefore(name, ".");
                    String pathName = StringUtils.substringAfter(StringUtils.substringBeforeLast(name, "."), ".");
                    String nodeName = StringUtils.substringAfterLast(name, ".");
                    String fullPath;

                    if (StringUtils.isEmpty(pathName)) {
                        pathName = "/";
                        fullPath = "/" + nodeName;
                    } else {
                        pathName = "/" + StringUtils.replace(pathName, ".", "/");
                        fullPath = pathName + "/" + nodeName;
                    }

                    System.out.println("Start to import repository: " + repositoryName);
                    CustomDataTransporter.importXmlStream(fileInputStream, repositoryName, pathName,
                            fullPath, false, false,
                            ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING, true, true);
                } else {
                    System.out.println("File not found in the zip: " + entryName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("restore completed.");
        return "restore completed";
    }

    private FileInputStream getFileInputStreamFromZip(ZipInputStream zipInput, String fileName)
            throws FileNotFoundException, IOException {
        FileInputStream fileInputStream;
        String tmpRepositoryFilePath = BackupConfiguration.getTmpImportFolder() + File.separator + fileName;

        FileOutputStream output = null;
        byte[] buffer = new byte[2048];
        try {
            output = new FileOutputStream(tmpRepositoryFilePath);
            int len = 0;
            while ((len = zipInput.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
        } finally {
            if (output != null)
                output.close();
            zipInput.closeEntry();
        }
        fileInputStream = new FileInputStream(tmpRepositoryFilePath);
        return fileInputStream;
    }

}
