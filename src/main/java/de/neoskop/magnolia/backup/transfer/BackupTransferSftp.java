package de.neoskop.magnolia.backup.transfer;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import java.io.File;

public class BackupTransferSftp extends BackupTransfer {

    @Override
    public boolean upload() {
        JSch sshClient = new JSch();

        com.jcraft.jsch.Session session;
        try {
            session = sshClient.getSession(BackupConfiguration.getServerUsername(),
                    BackupConfiguration.getServerIp());

            session.setPassword(BackupConfiguration.getServerPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            sftpChannel.put(BackupConfiguration.getTenporaryBackupFilePath(),
                    BackupConfiguration.getServerPath() + File.separator
                            + BackupConfiguration.getCurrentEnvironment() + File.separator
                            + BackupConfiguration.getBackupFileName());

            sftpChannel.exit();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
