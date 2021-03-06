/*
 *  Copyright (c) 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import java.nio.file.Path;
import java.util.List;

/**
 *  Interface defining functionality associated with the reading of archives.
 *  @author Aashutos Kakshepati
 */
public interface ArchiveReadService extends ArchiveService {
    List<FileInfo> listFiles(long sessionId, String archivePath);
    boolean extractFile(long sessionId, Path targetLocation, String archivePath, FileInfo file);
    boolean testArchive(long sessionId, String archivePath);
    List<String> supportedReadFormats();
}
