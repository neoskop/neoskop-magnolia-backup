package de.neoskop.magnolia.backup.transfer;

import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

public class RestoreTransferS3 extends RestoreTransfer {

    @Override
    public boolean download() {
        File directory = new File(BackupConfiguration.getTmpImportFolder());
        if (!directory.exists()) {
            directory.mkdir();
        }

        String accessKey = BackupConfiguration.getAccessKey();
        String secretKey = BackupConfiguration.getSecretKey();
        String host = BackupConfiguration.getServerHost();
        String region = BackupConfiguration.getRegion();

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);

        S3Client s3Client = S3Client.builder().region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .endpointOverride(URI.create(host))
                .serviceConfiguration(S3Configuration.builder()
                        .chunkedEncodingEnabled(false)
                        .build())
                .build();

        String bucketName = BackupConfiguration.getBucket();
        String keyName = BackupConfiguration.getRestoreEnvironment() + "/"
                + BackupConfiguration.getBackupFileName();

        try {
            s3Client.getObject(GetObjectRequest.builder().bucket(bucketName).key(keyName).build(),
                    Paths.get(BackupConfiguration.getTemporaryImportFilePath()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}