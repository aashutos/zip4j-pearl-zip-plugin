/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.UnzipParameters;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ArchiveConstants.CURRENT_SETTINGS;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_NTAK_PEARL_ZIP_ICON_FOLDER;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.*;
import static com.ntak.pearlzip.archive.util.LoggingUtil.getStackTraceFromException;
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
            if (headers.stream()
                       .anyMatch(f -> f.isEncrypted() && f.getEncryptionMethod()
                                                          .equals(EncryptionMethod.AES))) {
                archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
                archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, EncryptionMethod.AES);
                archiveInfo.addProperty(KEY_ENCRYPTION_STRENGTH, AesKeyStrength.KEY_STRENGTH_256);
            } else if (headers.stream()
                              .anyMatch(f -> f.isEncrypted() && f.getEncryptionMethod()
                                                                 .equals(EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG))) {
                archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, true);
                archiveInfo.addProperty(KEY_ENCRYPTION_METHOD, EncryptionMethod.ZIP_STANDARD_VARIANT_STRONG);
            }

            archiveInfo.addProperty(KEY_COMPRESSION_METHOD, CompressionMethod.DEFLATE);
            if (headers.stream().anyMatch(f->f.getCompressionMethod().equals(CompressionMethod.STORE))
                && headers.stream().noneMatch(f->f.getCompressionMethod().equals(CompressionMethod.DEFLATE))
            ) {
                archiveInfo.addProperty(KEY_COMPRESSION_METHOD, CompressionMethod.STORE);
            }

            archiveInfo.setCompressionLevel(Integer.parseInt(CURRENT_SETTINGS.getProperty(CNS_DEFAULT_COMPRESSION_LEVEL, "9")));
        } catch (Exception e) {
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
            HashSet<FileInfo> setFiles =
                    new HashSet<>(files.stream().filter(f -> !f.isFolder() || (f.isFolder() && files.stream().noneMatch(g -> g.getFileName().contains(f.getFileName()) && g.getLevel() > f.getLevel()))).collect(Collectors.toList()));
            for (FileInfo file : files) {
                final int level = file.getLevel();
                Path parent = Paths.get(file.getFileName());
                for (int j = 1; j <= level; j++) {
                    parent = parent.getParent();
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

                if (file.isFolder()) {
                    final FileInfo fileInfo = new FileInfo(setFiles.size(),
                                                           level,
                                                           file.getFileName(),
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
        } catch (Exception e) {
            // LOG: Issue listing entries from zip archive.\nException thrown: %s\nException message: %s\nStack
            // trace:\n%s
            // TITLE: Issue listing entries from archive
            // HEADER: The archive %s could not be interrogated for contents
            // BODY: Exception %s was thrown on the attempt to list contents of the archive. Further details can be
            // found below.
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_LISTING_ARCHIVE,
                                        e.getClass().getCanonicalName(),
                                        e.getMessage(),
                                        LoggingUtil.getStackTraceFromException(e)
            ));
            DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                              resolveTextKey(TITLE_ARCHIVE_Z4J_ISSUE_LISTING_ARCHIVE),
                                              resolveTextKey(HEADER_ARCHIVE_Z4J_ISSUE_LISTING_ARCHIVE, archiveInfo.getArchivePath()),
                                              resolveTextKey(BODY_ARCHIVE_Z4J_ISSUE_LISTING_ARCHIVE, e.getClass().getCanonicalName()),
                                              e,
                                              archiveInfo));
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
                ProgressMonitor monitor = archive.getProgressMonitor();
                DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                     resolveTextKey(LBL_PROGRESS_EXTRACT_ENTRY,
                                                                    fileInfo.getFileName()),
                                                     -1,
                                                     1)
                );
                archive.extractFile(header, parent.toString(),
                                    Paths.get(fileInfo.getFileName()).getFileName().toString(), new UnzipParameters());
                return monitor.getResult().equals(ProgressMonitor.Result.SUCCESS);
            }
        } catch(Exception e) {
            // LOG: Issue extracting from zip archive.\nException thrown: %s\nException message: %s\nStack trace:\n%s
            // TITLE: Issue extracting archive
            // HEADER: The archive %s could not be extracted
            // BODY: Exception %s was thrown on the attempt to extract from the archive. Further details can be found
            // below.
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_EXTRACTING_FILE, e.getClass().getCanonicalName(),
                                       e.getMessage(), getStackTraceFromException(e)));
            DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                              resolveTextKey(TITLE_ARCHIVE_Z4J_ISSUE_EXTRACTING_FILE),
                                              resolveTextKey(HEADER_ARCHIVE_Z4J_ISSUE_EXTRACTING_FILE, archiveInfo.getArchivePath()),
                                              resolveTextKey(BODY_ARCHIVE_Z4J_ISSUE_EXTRACTING_FILE, e.getClass().getCanonicalName()),
                                              e,
                                              archiveInfo));
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
        AnchorPane root;
        try {
            if (archiveInfo.<Boolean>getProperty(KEY_ENCRYPTION_ENABLE)
                           .orElse(false)) {
                FrmZip4jPasswordController controller = new FrmZip4jPasswordController(this, archiveInfo);

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(Zip4jArchiveWriteService.class.getClassLoader()
                                                                 .getResource("frmZip4jPassword.fxml"));
                loader.setResources(RES_BUNDLE);
                loader.setController(controller);
                root = loader.load();

                return Optional.of(root);
            }
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    @Override
    public Optional<FXForm> getFXFormByIdentifier(String name, Object... parameters) {
        switch(name) {
            case OPEN_ARCHIVE_OPTIONS: {
                if (parameters.length > 0 && parameters[0] instanceof ArchiveInfo info) {
                    Optional<Node> optNode = getOpenArchiveOptionsPane(info);
                    if (optNode.isPresent()) {
                        FXForm openForm = new FXForm(name, optNode.get(), Collections.emptyMap());
                        return Optional.of(openForm);
                    }
                }
                break;
            }
        }

        return Optional.empty();
    }

    @Override
    public List<String> supportedReadFormats() {
        return List.of("zip");
    }
}
