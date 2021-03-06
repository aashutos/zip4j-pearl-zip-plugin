/*
 *  Copyright (c) 2021 92AK
 */
package com.ntak.pearlzip.archive.szjb.pub;

import com.ntak.pearlzip.archive.constants.ConfigurationConstants;
import com.ntak.pearlzip.archive.constants.LoggingConstants;
import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.TransformEntry;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_NTAK_PEARL_ZIP_ICON_FILE;
import static com.ntak.pearlzip.archive.constants.ConfigurationConstants.CNS_NTAK_PEARL_ZIP_ICON_FOLDER;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;

/**
 *   Converter for 7-Zip Java Binding archive entries (ISimpleInArchiveItem) to normalised FileInfo objects.
 *  @author Aashutos Kakshepati
 */
public class SimpleSevenZipEntryTransform implements TransformEntry<ISimpleInArchiveItem> {

    private static final Logger LOGGER = LoggerContext.getContext().getLogger(SimpleSevenZipEntryTransform.class);

    @Override
    public Optional<FileInfo> transform(ISimpleInArchiveItem rawEntry) {
        try {
            int index = rawEntry.getItemIndex();
            String name = rawEntry.getPath();
            int level = name.length() - name.replace(File.separator, "").length();
            long crcHash = Optional.ofNullable(rawEntry.getCRC()).orElse(-1);

            final var rawLastAccessTime = rawEntry.getLastAccessTime();
            final var rawCreationTime = rawEntry.getCreationTime();
            final var rawLastWriteTime = rawEntry.getLastWriteTime();

            LocalDateTime lastAccessTime = null;
            LocalDateTime creationTime = null;
            LocalDateTime lastWriteTime = null;

            if (Objects.nonNull(rawLastAccessTime)) {
                lastAccessTime = LocalDateTime.ofInstant(rawLastAccessTime.toInstant(),ZoneId.systemDefault());
            }

            if (Objects.nonNull(rawCreationTime)) {
                creationTime = LocalDateTime.ofInstant(rawEntry.getCreationTime().toInstant(), ZoneId.systemDefault());
            }
            if (Objects.nonNull(rawLastWriteTime)) {
                lastWriteTime = LocalDateTime.ofInstant(rawEntry.getLastWriteTime().toInstant(), ZoneId.systemDefault());
            }

            long packedSize = Optional.ofNullable(rawEntry.getPackedSize()).orElse(0L);
            long rawSize = Optional.ofNullable(rawEntry.getSize()).orElse(0L);

            String user = rawEntry.getUser();
            String group = rawEntry.getGroup();
            int attributes = Optional.ofNullable(rawEntry.getAttributes()).orElse(0);
            String comments = rawEntry.getComment();

            boolean isFolder = rawEntry.isFolder();
            boolean isEncrypted = rawEntry.isEncrypted();

            return Optional.of(new FileInfo(index, level, name, crcHash, packedSize, rawSize, lastWriteTime,
                                            lastAccessTime,
                                 creationTime, user,
                                group, attributes, comments, isFolder, isEncrypted, Collections.singletonMap(
                    ConfigurationConstants.KEY_ICON_REF, isFolder?System.getProperty(CNS_NTAK_PEARL_ZIP_ICON_FOLDER, ""):
                            System.getProperty(CNS_NTAK_PEARL_ZIP_ICON_FILE, "")))
                    );

        } catch (Exception e) {
            // LOG: Issue with transform of zip entry. Exception message: %s
            LOGGER.warn(resolveTextKey(LoggingConstants.LOG_TRANSFORM_EXCEPTION, e));
        }
        return Optional.empty();
    }
}
