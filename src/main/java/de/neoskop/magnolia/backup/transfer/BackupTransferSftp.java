package de.neoskop.magnolia.backup.transfer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import de.neoskop.magnolia.backup.configuration.BackupRotation;
import java.io.File;
import java.time.LocalDate;

public class BackupTransferSftp extends BackupTransfer {

    @Override
    public boolean upload() {
        JSch sshClient = new JSch();

        com.jcraft.jsch.Session session;
        try {
            session = sshClient.getSession(BackupConfiguration.getServerUsername(),
                    BackupConfiguration.getServerHost());

            session.setPassword(BackupConfiguration.getServerPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.put(BackupConfiguration.getTemporaryBackupFilePath(),
                    BackupConfiguration.getServerPath() + File.separator
                            + BackupConfiguration.getCurrentEnvironment() + File.separator
                            + BackupConfiguration.getBackupFileName());

            if (BackupConfiguration.getRotationEnabled()) {
                try {
                    String rotationFolder = BackupConfiguration.getServerPath() + File.separator
                            + BackupConfiguration.getCurrentEnvironment() + File.separator
                            + BackupRotation.ROTATION_FOLDER;
                    try {
                        sftpChannel.stat(rotationFolder);
                    } catch (SftpException e) {
                        sftpChannel.mkdir(rotationFolder);
                    }
                    for (String slotFileName : BackupRotation.getSlotFileNames(LocalDate.now())) {
                        sftpChannel.put(BackupConfiguration.getTemporaryBackupFilePath(),
                                rotationFolder + File.separator + slotFileName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            sftpChannel.exit();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
