/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import javafx.scene.Node;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 *  Interface defining functionality associated with the reading of archives.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveReadService extends ArchiveService {
    List<FileInfo> listFiles(long sessionId, String archivePath);
    List<FileInfo> listFiles(long sessionId, ArchiveInfo archivePath);
    boolean extractFile(long sessionId, Path targetLocation, String archivePath, FileInfo file);
    boolean extractFile(long sessionId, Path targetLocation, ArchiveInfo archivePath, FileInfo file);
    boolean testArchive(long sessionId, String archivePath);
    default Optional<Node> getOpenArchiveOptionsPane(ArchiveInfo archiveInfo) { return Optional.empty(); };
    List<String> supportedReadFormats();
}
