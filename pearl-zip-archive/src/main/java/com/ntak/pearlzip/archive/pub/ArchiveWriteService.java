/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import javafx.scene.Node;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

/**
 *  Interface defining functionality associated with the writing of archives.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveWriteService extends ArchiveService {
    void createArchive(long sessionId, String archivePath, FileInfo... files);
    void createArchive(long sessionId, ArchiveInfo archiveInfo, FileInfo... files);
    boolean addFile(long sessionId, String archivePath, FileInfo... file);
    boolean addFile(long sessionId, ArchiveInfo archiveInfo, FileInfo... files);
    boolean deleteFile(long sessionId, String archivePath, FileInfo file);
    boolean deleteFile(long sessionId, ArchiveInfo archivePath, FileInfo file);
    default Optional<Pair<String,Node>> getCreateArchiveOptionsPane() { return Optional.empty(); };
    List<String> supportedWriteFormats();
}
