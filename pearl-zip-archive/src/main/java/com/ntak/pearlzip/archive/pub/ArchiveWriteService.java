/*
 *  Copyright (c) 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import java.util.List;

/**
 *  Interface defining functionality associated with the writing of archives.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveWriteService extends ArchiveService {
    void createArchive(long sessionId, String archivePath, FileInfo... files);
    void createArchive(long sessionId, ArchiveInfo archiveInfo, FileInfo... files);
    boolean addFile(long sessionId, String archivePath, FileInfo... file);
    boolean deleteFile(long sessionId, String archivePath, FileInfo file);

    List<String> supportedWriteFormats();
}
