package de.neoskop.magnolia.backup.transfer;

import de.neoskop.magnolia.backup.configuration.BackupConfiguration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.net.URI;
import java.nio.file.Paths;

public class BackupTransferS3 extends BackupTransfer {

    @Override
    public boolean upload() {
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
        String keyName = BackupConfiguration.getCurrentEnvironment() + "/"
                + BackupConfiguration.getBackupFileName();

        try {
            s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(keyName).build(),
                    RequestBody
                            .fromFile(Paths.get(BackupConfiguration.getTemporaryBackupFilePath())));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}