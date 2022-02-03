package de.neoskop.magnolia.backup.transfer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import java.io.File;

public class RestoreTransferSftp extends RestoreTransfer {

    @Override
    public boolean download() {
        File directory = new File(BackupConfiguration.getTmpImportFolder());
        if (!directory.exists()) {
            directory.mkdir();
        }

        JSch sshClient = new JSch();
        com.jcraft.jsch.Session jschSession;
        try {
            jschSession = sshClient.getSession(BackupConfiguration.getServerUsername(),
                    BackupConfiguration.getServerIp());
            jschSession.setPassword(BackupConfiguration.getServerPassword());
            jschSession.setConfig("StrictHostKeyChecking", "no");
            jschSession.connect();

            Channel channel = jschSession.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.get(
                    BackupConfiguration.getServerPath() + File.separator
                            + BackupConfiguration.getRestoreEnvironment() + File.separator
                            + BackupConfiguration.getBackupFileName(),
                    BackupConfiguration.getTemporaryImportFilePath());

            sftpChannel.exit();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
