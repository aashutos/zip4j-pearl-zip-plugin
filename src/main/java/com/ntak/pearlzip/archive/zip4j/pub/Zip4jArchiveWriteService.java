/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.*;
import com.ntak.pearlzip.archive.util.LoggingUtil;
import com.ntak.pearlzip.archive.zip4j.util.Zip4jUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.KEY_FILE_PATH;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.COMPLETED;
import static com.ntak.pearlzip.archive.constants.LoggingConstants.PROGRESS;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;
import static net.lingala.zip4j.model.enums.CompressionMethod.DEFLATE;

/**
 *  Implementation of the Archive Service for writing zip archives using the Zip4j library underneath.
 *  @author Aashutos Kakshepati
 */
public class Zip4jArchiveWriteService implements ArchiveWriteService {

    private static final Logger LOGGER = LoggerContext.getContext()
                                                      .getLogger(Zip4jArchiveWriteService.class);

    @Override
    public void createArchive(long sessionId, ArchiveInfo archiveInfo, FileInfo... files) {
        try {
            ZipParameters parameters = new ZipParameters();

            boolean isSplitArchiveRequest =
                    archiveInfo.<Boolean>getProperty(KEY_SPLIT_ARCHIVE_ENABLE).orElse(false);
            long splitSize = archiveInfo.<Long>getProperty(KEY_SPLIT_ARCHIVE_SIZE).orElse(MIN_SPLIT_ARCHIVE_SIZE);

            Zip4jUtil.initializeZipParameters(parameters, archiveInfo);
            ZipFile archive = new ZipFile(archiveInfo.getArchivePath(),
                                          archiveInfo.<char[]>getProperty(KEY_ENCRYPTION_PW).orElse(null));

            // Split archive only works with a non-empty archive.
            // TODO: Functionality to create a split archive and also to open and modify it via the use of the merge
            //  archive functionality (temporarily interim).
            //  As soon as ZipFile is interrogated the ZipModel is retrieved to get information about whether it is
            //  encrypted a split archive or a valid file etc. which will help with providing details about the archive
            if (isSplitArchiveRequest) {
                final Path tempFile = Files.createTempFile(".tmp", "");
                LocalDateTime creationTime = LocalDateTime.now();
                if (files.length == 0) {
                    FileInfo fileInfo = new FileInfo(0,0,".pz-archive",0L,0L,0L,
                                                     creationTime, creationTime, creationTime,
                                                     null, null, 0,
                                                     "", false, parameters.isEncryptFiles(),
                                                     Collections.singletonMap(KEY_FILE_PATH,
                                                                              tempFile.toAbsolutePath().toString()));
                    files = new FileInfo[]{fileInfo};
                }
                archive.createSplitZipFile(Arrays.stream(files)
                                                 .map(f -> (String)f.getAdditionalInfoMap().get(KEY_FILE_PATH))
                                                 .filter(Objects::nonNull)
                                                 .map(File::new)
                                                 .collect(Collectors.toList()),
                                           parameters,
                                           isSplitArchiveRequest,
                                           splitSize);
                Files.deleteIfExists(tempFile);
            } else {
                // Create stub file to ensure archive is created
                final Path tempFile = Files.createTempFile(".tmp", "");
                archive.addFile(tempFile.toFile());
                archive.removeFile(tempFile.toFile()
                                           .getName());
                Files.deleteIfExists(tempFile);

                // Adding subsequent archive files, if any...
                addFile(sessionId, archiveInfo, files);
            }
        } catch(IOException e) {
            // LOG: Issue creating zip archive.\nException thrown: %s\nException message: %s\nStack trace:\n%s
            // TITLE: Issue creating archive
            // HEADER: The archive %s could not be created
            // BODY: Exception %s was thrown on the attempt to create the archive. Further details can be found
            // below.
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_CREATING_ARCHIVE,
                                       e.getClass().getCanonicalName(),
                                       e.getMessage(),
                                       LoggingUtil.getStackTraceFromException(e)
            ));
            DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                              resolveTextKey(TITLE_ARCHIVE_Z4J_ISSUE_CREATING_ARCHIVE),
                                              resolveTextKey(HEADER_ARCHIVE_Z4J_ISSUE_CREATING_ARCHIVE, archiveInfo.getArchivePath()),
                                              resolveTextKey(BODY_ARCHIVE_Z4J_ISSUE_CREATING_ARCHIVE, e.getClass().getCanonicalName()),
                                              e,
                                              archiveInfo));
        } finally {
            DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED,1,1));
        }
    }

    @Override
    public void createArchive(long sessionId, String archivePath, FileInfo... files) {
        ArchiveInfo archiveInfo = generateDefaultArchiveInfo(archivePath);

        createArchive(sessionId, archiveInfo, files);
    }

    @Override
    public boolean addFile(long sessionId, String archivePath, FileInfo... files) {
        ArchiveInfo archiveInfo = generateDefaultArchiveInfo(archivePath);
        return addFile(sessionId, archiveInfo, files);
    }

    @Override
    public boolean addFile(long sessionId, ArchiveInfo archiveInfo, FileInfo... files) {
        try {
            ZipFile archive = new ZipFile(archiveInfo.getArchivePath(),
                                          archiveInfo.<char[]>getProperty(KEY_ENCRYPTION_PW).orElse(null));

            // Add files...
            addFilesInPlace(sessionId, archive, archiveInfo,
                            Arrays.stream(files).filter(f-> {
                                try {
                                    // All files and empty folders
                                    return !f.isFolder() || Files.list(Paths.get(f.getAdditionalInfoMap()
                                                                                  .getOrDefault(KEY_FILE_PATH,"").toString()))
                                                                                  .filter(p->!Objects.equals(
                                                                                             p.toAbsolutePath(),
                                                                                             Paths.get(archiveInfo.getArchivePath()))
                                                                                  ).count() == 0;
                                } catch(IOException e) {
                                    return false;
                                }
                            }).collect(Collectors.toList()));

            return true;
        } catch(ZipException e) {
            // LOG: Issue adding to zip archive.\nException thrown: %s\nException message: %s\nStack trace:\n%s
            // TITLE: Issue adding to archive
            // HEADER: An entry could not be added to archive %s
            // BODY: Exception %s was thrown on the attempt to add an entry to archive. Further details can be
            // found below.
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_ADDING_FILE, archiveInfo.getArchivePath(),
                                        e.getMessage()));
            DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                              resolveTextKey(TITLE_ARCHIVE_Z4J_ISSUE_ADDING_FILE),
                                              resolveTextKey(HEADER_ARCHIVE_Z4J_ISSUE_ADDING_FILE, archiveInfo.getArchivePath()),
                                              resolveTextKey(BODY_ARCHIVE_Z4J_ISSUE_ADDING_FILE, e.getClass().getCanonicalName()),
                                              e,
                                              archiveInfo));
        } finally {
            DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED,1,1));
        }
        return false;
    }

    private void addFilesInPlace(long sessionId, ZipFile archive, ArchiveInfo archiveInfo, List<FileInfo> files) throws ZipException {
        for (FileInfo file : files) {
            // LOG: Adding file %s...
            DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                 resolveTextKey(LOG_ARCHIVE_Z4J_ADDING_FILE, file.getFileName())
                    , 1, files.size()));

            String fileName = file.getFileName();
            ZipParameters fileParam = new ZipParameters();
            Zip4jUtil.initializeZipParameters(fileParam, archiveInfo);
            fileParam.setFileComment(file.getComments());

            if (file.isFolder()) {
                fileParam.setFileNameInZip(String.format(PATTERN_FOLDER, fileName));
                archive.addFolder(Paths.get(file.getAdditionalInfoMap().get(KEY_FILE_PATH).toString())
                                       .toAbsolutePath()
                                       .toFile(), fileParam);
            } else {
                fileParam.setFileNameInZip(fileName);
                archive.addFile(Paths.get(file.getAdditionalInfoMap().get(KEY_FILE_PATH).toString())
                                     .toAbsolutePath()
                                     .toFile(), fileParam);
            }
        }
    }

    @Override
    public boolean deleteFile(long sessionId, String archivePath, FileInfo file) {
        ArchiveInfo archiveInfo = generateDefaultArchiveInfo(archivePath);
        return deleteFile(sessionId, archiveInfo, file);
    }

    @Override
    public boolean deleteFile(long sessionId, ArchiveInfo archiveInfo, FileInfo file) {
        try {
            Zip4jFileHeaderTransform transform = new Zip4jFileHeaderTransform();
            ZipFile archive = new ZipFile(archiveInfo.getArchivePath(),
                                          archiveInfo.<char[]>getProperty(KEY_ENCRYPTION_PW).orElse(null));

            // Overwrite files action is default
            // LOG: Deleting file %s...
            DEFAULT_BUS.post(new ProgressMessage(sessionId, PROGRESS,
                                                 resolveTextKey(LOG_ARCHIVE_Z4J_DELETING_FILE, file.getFileName()), 1, 1));

            if (file.isFolder()) {
                final List<FileHeader> fileHeaders = new ArrayList(archive.getFileHeaders());
                Collections.sort(fileHeaders,
                                 (a,b) -> (b.getFileName().length() - b.getFileName().replaceAll("/", "").length()) - (a.getFileName().length() - a.getFileName().replaceAll("/", "").length()));
                for (FileHeader h : fileHeaders) {
                    FileInfo hFile = transform.transform(h).orElse(null);
                    if (Objects.nonNull(hFile) && hFile.getFileName()
                                      .startsWith(file.getFileName()) && hFile.getLevel() > file.getLevel()) {
                        try {
                            archive.removeFile(h.getFileName());
                        } catch(ZipException e) {
                        }
                    }
                }
                archive.removeFile(String.format(PATTERN_FOLDER, file.getFileName()));
            } else {
                archive.removeFile(file.getFileName());

                // Keep immediate parent if last file is deleted...
                Path parent = Paths.get(file.getFileName()).getParent();
                if (Objects.nonNull(parent)) {
                    try {
                        // Create temp directory structure to persist...
                        Path tempDirectory = Files.createTempDirectory("pz");
                        Path actualFile = Paths.get(tempDirectory.toAbsolutePath()
                                                             .toString(), parent.toString());
                        Files.createDirectories(actualFile);

                        // Set up attributes with current archive settings
                        Map<String,Object> attributes = new HashMap<>(file.getAdditionalInfoMap());
                        attributes.put(KEY_FILE_PATH, actualFile.toAbsolutePath().toString());

                        // Add file as necessary...
                        FileInfo fileInfo = new FileInfo(archive.getFileHeaders().size(), file.getLevel(),
                                                         parent.toString(), 0, 0, 0, LocalDateTime.now(),
                                                         LocalDateTime.now(), LocalDateTime.now(), "", "", 0, "",
                                                         true, file.isEncrypted(), attributes);
                        addFilesInPlace(sessionId, archive, archiveInfo, Arrays.asList(fileInfo));
                    } catch (IOException e) {
                        return false;
                    }
                }
            }

            return true;
        } catch(ZipException e) {
            // LOG: Issue deleting from zip archive.\nException thrown: %s\nException message: %s\nStack trace:\n%s
            // TITLE: Issue deleting archive
            // HEADER: An entry could not be removed from archive %s
            // BODY: Exception %s was thrown on the attempt to remove an entry from the archive. Further details can be
            // found below.
            LOGGER.error(resolveTextKey(LOG_ARCHIVE_Z4J_ISSUE_DELETING_FILE,
                                       e.getClass().getCanonicalName(),
                                       e.getMessage(),
                                       LoggingUtil.getStackTraceFromException(e)
            ));
            DEFAULT_BUS.post(new ErrorMessage(sessionId,
                                              resolveTextKey(TITLE_ARCHIVE_Z4J_ISSUE_DELETING_FILE),
                                              resolveTextKey(HEADER_ARCHIVE_Z4J_ISSUE_DELETING_FILE, archiveInfo.getArchivePath()),
                                              resolveTextKey(BODY_ARCHIVE_Z4J_ISSUE_DELETING_FILE, e.getClass().getCanonicalName()),
                                              e,
                                              archiveInfo));
        } finally {
            DEFAULT_BUS.post(new ProgressMessage(sessionId, COMPLETED, COMPLETED,1,1));
        }
        return false;
    }

    private static ArchiveInfo generateDefaultArchiveInfo(String archivePath) {
        ArchiveInfo archiveInfo = new ArchiveInfo();
        archiveInfo.setArchivePath(archivePath);
        archiveInfo.setArchiveFormat("zip");
        archiveInfo.setCompressionLevel(9);
        archiveInfo.addProperty(KEY_ENCRYPTION_ENABLE, false);
        archiveInfo.addProperty(KEY_COMPRESSION_METHOD, DEFLATE);

        return archiveInfo;
    }

    @Override
    public Optional<ResourceBundle> getResourceBundle() {
        return Optional.of(RES_BUNDLE);
    }

    @Override
    public Optional<Pair<String,Node>> getCreateArchiveOptionsPane() {
        AnchorPane root;
        final String title_zip4j_options = "Zip4j Options";
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Zip4jArchiveWriteService.class.getClassLoader()
                                                .getResource("frmZip4jNewOptions.fxml"));
            loader.setResources(RES_BUNDLE);
            loader.setController(new FrmZip4jNewOptionsController());
            root = loader.load();
        } catch (Exception e) {
            return Optional.of(new Pair(title_zip4j_options, new AnchorPane()));
        }
        return Optional.of(new Pair(title_zip4j_options, root));
    }

    @Override
    public Optional<Pair<String, Node>> getOptionsPane() {
        AnchorPane root;
        final String title_zip4j_options = "Zip4j Options";
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Zip4jArchiveWriteService.class.getClassLoader()
                                                             .getResource("frmZip4jOptions.fxml"));
            loader.setResources(RES_BUNDLE);
            loader.setController(new FrmZip4jOptionsController());
            root = loader.load();
        } catch (Exception e) {
            return Optional.of(new Pair(title_zip4j_options, new AnchorPane()));
        }
        return Optional.of(new Pair(title_zip4j_options, root));
    }

    @Override
    public Optional<FXForm> getFXFormByIdentifier(String name, Object... parameters) {
        switch(name) {
            case CREATE_OPTIONS: {
                Optional<Pair<String, Node>> optDetails = getCreateArchiveOptionsPane();
                if (optDetails.isPresent()) {
                    Pair<String, Node> pair = optDetails.get();
                    FXForm fxForm = new FXForm(pair.getKey(), pair.getValue(), Collections.emptyMap());
                    return Optional.of(fxForm);
                }

                break;
            }

            case OPTIONS: {
                Optional<Pair<String, Node>> optDetails = getOptionsPane();
                if (optDetails.isPresent()) {
                    Pair<String, Node> pair = optDetails.get();
                    FXForm fxForm = new FXForm(pair.getKey(), pair.getValue(), Collections.emptyMap());
                    return Optional.of(fxForm);
                }

                break;
            }

            case CUSTOM_MENUS: {
                MenuBar customMenus = getCustomMenu();
                FXForm fxForm = new FXForm(customMenus.getId(), customMenus, Collections.emptyMap());

                return Optional.of(fxForm);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<String> supportedWriteFormats() {
        return List.of("zip");
    }

    private MenuBar getCustomMenu() {
        MenuBar menuBar = new MenuBar();
        Menu zip4jMenu;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Zip4jArchiveWriteService.class.getClassLoader()
                                                             .getResource("FrmZip4jMenu.fxml"));
            loader.setResources(RES_BUNDLE);
            loader.setController(new FrmZip4jMenuController());
            zip4jMenu = loader.load();
            menuBar.getMenus().add(zip4jMenu);
        } catch (Exception e) {

        }

        return menuBar;
    }
}
