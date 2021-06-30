/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import javafx.scene.Node;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_NTAK_PEARL_ZIP_ICON_FOLDER;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.COMPLETED;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.ERROR;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;
import static com.ntak.pearlzip.archive.zip4j.util.Zip4jUtil.initializeZipParameters;

/**
 *  Implementation of the Archive Service for reading zip archives using the Zip4j library underneath.
 *  @author Aashutos Kakshepati
 */
public class Zip4jArchiveReadService implements ArchiveReadService  {
    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(Zip4jArchiveReadService.class);

    @Override
    public ArchiveInfo generateArchiveMetaData(String archivePath) {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        // Assumption 1: Uniform setup of each Zip entry in the archive. Same encryption and same compression format
        // throughout etc.
        // Assumption 2: All files to be added will be performed on a uniform basis and will not be different
        // entry-to-entry
        // TODO: Will need to modify this to store metadata for individual file entries. Adding files to zip archive
        //  would involve custom settings for Compression, Encryption. It maybe safe to assume a uniform password
        //  across encrypted entries. Extraction will be affected by this also. List and Test involve header usage
        //  and should be ok.
        try {
            ZipFile archive = new ZipFile(archivePath);
            archiveInfo.setArchivePath(archivePath);
            archiveInfo.setArchiveFormat("zip");
            List<FileHeader> headers = archive.getFileHeaders();

            // Encryption checks...
            if (headers.stream().filter(f->f.isEncrypted() && f.getEncryptionMethod().equals(EncryptionMethod.AES)).count() > 0) {
                archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
                archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, EncryptionMethod.AES);
                archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, AesKeyStrength.KEY_STRENGTH_256);
            } else if (headers.stream().filter(f->f.isEncrypted() && f.getEncryptionMethod().equals(EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG)).count() > 0) {
                archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
                archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG);
            }

            archiveInfo.addProperty(KEY_COMPRESSION_METHOD, CompressionMethod.DEFLATE);
            if (headers.stream().anyMatch(f->f.getCompressionMethod().equals(CompressionMethod.STORE))
                && headers.stream().noneMatch(f->f.getCompressionMethod().equals(CompressionMethod.DEFLATE))
            ) {
                archiveInfo.addProperty(KEY_COMPRESSION_METHOD, CompressionMethod.STORE);
            }

            // TODO: Compression level may be set at Option level (Maybe Options can specify default values to set on
            //  dialog on a file basis at a later stage)
            archiveInfo.setCompressionLevel(9);
        } catch (ZipException e) {
            // LOG: Issue generating metadata for archive %s
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_GENERATING_METADATA, archivePath));
            archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);
        }

        return archiveInfo;
    }

    @Override
    public List<FileInfo> listFiles(long sessionId, String archivePath) {
        ArchiveInfo archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);
        return listFiles(sessionId, archiveInfo);
    }

    @Override
    public List<FileInfo> listFiles(long sessionId, ArchiveInfo archiveInfo) {
        List<FileInfo> files = new LinkedList<>();
        Zip4jFileHeaderTransform transform = new Zip4jFileHeaderTransform();
        try {
            ZipParameters parameters = new ZipParameters();
            initializeZipParameters(parameters, archiveInfo);

            ZipFile archive = new ZipFile(archiveInfo.getArchivePath(),
                                          archiveInfo.<char[]>getProperty(KEY_ENCRYPTION_PW)
                                                     .orElse(null));

            for (FileHeader header : archive.getFileHeaders()) {
                FileInfo fileInfo = transform.transform(header).orElse(null);

                if (Objects.nonNull(fileInfo)) {
                    files.add(fileInfo);
                }
            }

            // Handle directory creation
            HashSet<FileInfo> setFiles = new HashSet<>(files);
            for (FileInfo file : files) {
                final int level = file.getLevel();
                Path parent = Paths.get(file.getFileName());
                for (int j = 1; j <= level; j++) {
                    parent = parent.getParent();
                    final Path finParent = parent;
                    setFiles.stream().filter(f->f.getFileName().equals(String.format(PATTERN_FOLDER,
                                                                                     finParent.toString()))).findFirst().ifPresent(f->setFiles.remove(f));

                    final FileInfo fileInfo = new FileInfo(setFiles.size(),
                                                           level - j,
                                                           parent.toString(),
                                                           -1,
                                                           0,
                                                           0,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           null,
                                                           0,
                                                           null,
                                                           true,
                                                           false,
                                                           Collections.singletonMap(
                                                                   ConfigurationConstants.KEY_ICON_REF,
                                                                   System.getProperty(
                                                                           CNS_NTAK_PEARL_ZIP_ICON_FOLDER, "")));
                    setFiles.add(fileInfo);
                }
            }

            return new ArrayList<>(setFiles);
        } catch (ZipException e) {
            // TODO: Error Handler in UI
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_CREATING_ARCHIVE,
                                        e.getClass().getCanonicalName(),
                                        e.getMessage(),
                                        LoggingUtil.getStackTraceFromException(e)
            ));
            DEFAULT_BUS.post(new ProgressMessage(sessionId, ERROR, resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_CREATING_ARCHIVE,
                                                                                  e.getClass().getCanonicalName(),
                                                                                  e.getMessage(),
                                                                                  LoggingUtil.getStackTraceFromException(e)), 0, 1));
        }
        return Collections.emptyList();
    }

    @Override
    public boolean extractFile(long sessionId, Path path, String archivePath, FileInfo fileInfo) {
        ArchiveInfo archiveInfo = ArchiveService.generateDefaultArchiveInfo(archivePath);
        return extractFile(sessionId, path, archiveInfo, fileInfo);
    }

    @Override
    public boolean extractFile(long sessionId, Path path, ArchiveInfo archiveInfo, FileInfo fileInfo) {
        try {
            ZipParameters parameters = new ZipParameters();
            initializeZipParameters(parameters, archiveInfo);

            ZipFile archive = new ZipFile(archiveInfo.getArchivePath(),
                                          archiveInfo.<char[]>getProperty(KEY_ENCRYPTION_PW)
                                                     .orElse(null));

            FileHeader header = archive.getFileHeader(fileInfo.getFileName());
            Path parent = path.toAbsolutePath().getParent();

            if (Objects.nonNull(header)) {
                archive.extractFile(header, parent.toString(), Paths.get(fileInfo.getFileName()).getFileName().toString());
                return true;
            }
        } catch(ZipException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean testArchive(long sessionId, String archivePath) {
        try {
            ZipFile archive = new ZipFile(archivePath);
            return archive.isValidZipFile();
        } finally {
            ArchiveService.DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED, 1, 1));
        }

}

    @Override
    public Optional<Node> getOpenArchiveOptionsPane(ArchiveInfo archiveInfo) {
        return Optional.empty();
    }

    @Override
    public List<String> supportedReadFormats() {
        return List.of("zip");
    }
}
