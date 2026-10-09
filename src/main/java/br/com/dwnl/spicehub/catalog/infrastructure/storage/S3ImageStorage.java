package br.com.dwnl.spicehub.catalog.infrastructure.storage;

import br.com.dwnl.spicehub.catalog.domain.storage.ImageStorage;
import br.com.dwnl.spicehub.catalog.infrastructure.exception.ImageStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
public class S3ImageStorage implements ImageStorage {

    private final S3Client s3Client;
    private final String bucket;

    public S3ImageStorage(S3Client s3Client, @Value("${SPICEHUB_STORAGE_BUCKET}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
    }

    @Override
    public void upload(String key, byte[] content, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromBytes(content));
        }catch (SdkException exception){
            throw new ImageStorageException("Failed to upload product image", exception);
        }
    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            s3Client.deleteObject(request);
        }catch (SdkException exception){
            throw new ImageStorageException("Failed to delete product image", exception);
        }
    }

    @Override
    public byte[] download(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try {
            return s3Client.getObjectAsBytes(request).asByteArray();
        }catch (SdkException exception){
            throw new ImageStorageException("Failed to download product image", exception);
        }
    }
}
