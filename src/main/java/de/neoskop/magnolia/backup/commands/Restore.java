package de.neoskop.magnolia.backup.commands;

import de.neoskop.magnolia.backup.CustomDataTransporter;
import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import de.neoskop.magnolia.backup.transfer.RestoreTransfer;
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
import java.util.zip.ZipEntry;
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

        RestoreTransfer restoreTransfer = null;
        if ("sftp".equals(BackupConfiguration.getProtocol())) {
            restoreTransfer = new RestoreTransferSftp();
        }
        if (!restoreTransfer.download()) {
            return "Download failed";
        }

        try {
            ZipInputStream zipInput = new ZipInputStream(
                    new FileInputStream(BackupConfiguration.getTemporaryImportFilePath()));

            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                String fileName = entry.getName();
                FileInputStream fileInputStream = getFileInputStreamFromZip(zipInput, fileName);

                String name = StringUtils.removeEnd(fileName, ".xml");
                String repository = StringUtils.substringBefore(name, ".");
                String pathName =
                        StringUtils.substringAfter(StringUtils.substringBeforeLast(name, "."), ".");
                String nodeName = StringUtils.substringAfterLast(name, ".");
                String fullPath;

                if (StringUtils.isEmpty(pathName)) {
                    pathName = "/";
                    fullPath = "/" + nodeName;
                } else {
                    pathName = "/" + StringUtils.replace(pathName, ".", "/");
                    fullPath = pathName + "/" + nodeName;
                }

                System.out.println("Start to import repository: " + repository);
                CustomDataTransporter.importXmlStream(fileInputStream, repository, pathName,
                        fullPath, false, false,
                        ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING, true, true);

            }
            zipInput.close();

        } catch (IOException e1) {
            e1.printStackTrace();
        }

        log.info("restore completed.");
        return "restore completed";
    }

    private FileInputStream getFileInputStreamFromZip(ZipInputStream zipInput, String fileName)
            throws FileNotFoundException, IOException {
        FileInputStream fileInputStream;
        String tmpRepositoryFilePath =
                BackupConfiguration.getTmpImportFolder() + File.separator + fileName;

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
