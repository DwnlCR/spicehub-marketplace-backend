package br.com.dwnl.spicehub.catalog.domain.storage;

public interface ImageStorage {

    void upload(String key, byte[] content, String contentType);

    void delete(String key);

    byte[] download(String key);
}
