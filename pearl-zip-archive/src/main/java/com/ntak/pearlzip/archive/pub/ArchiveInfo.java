/*
 * Copyright (c) ${YEAR} 92AK
 */
package com.ntak.pearlzip.archive.pub;

import java.util.Optional;
import java.util.Properties;

/**
 *  Java bean representing the metadata of an archive.
 *  @author Aashutos Kakshepati
 */
public class ArchiveInfo {
    private String archivePath;
    private String archiveFormat = "";
    private int compressionLevel = 0;
    private final Properties properties = new Properties();

    public synchronized void setArchivePath(String archivePath) {
        this.archivePath = archivePath;
    }

    public synchronized void setArchiveFormat(String format) {
        archiveFormat = format;
    }

    public synchronized void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    public synchronized String getArchivePath() {
        return archivePath;
    }

    public synchronized String getArchiveFormat() {
        return archiveFormat;
    }

    public synchronized int getCompressionLevel() {
        return compressionLevel;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public <T> Optional<T> getProperty(String key) {
        try {
            return Optional.of((T)properties.get(key));
        } catch (Exception e) {
        }
        return Optional.empty();
    }
}
