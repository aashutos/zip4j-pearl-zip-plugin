/*
 * Copyright © 2021 92AK
 */

package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.FileInfo;
import com.ntak.pearlzip.archive.pub.TransformEntry;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.Zip64ExtendedInfo;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.SSV;
import static java.time.Instant.ofEpochMilli;

/**
 *  Transforms the Zip4j FileHeader information into normalised PearlZip FileInfo POJOs.
 *  @author Aashutos Kakshepati
 */
public class Zip4jFileHeaderTransform implements TransformEntry<FileHeader> {

    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public Optional<FileInfo> transform(FileHeader header) {
        String fileName = header.getFileName();
        int level = SSV.split(fileName).length-1;
        long hash = header.getCrc();
        LocalDateTime lastModifiedTime =
                LocalDateTime.ofInstant(ofEpochMilli(header.getLastModifiedTimeEpoch()),
                                        ZoneId.systemDefault());
        boolean isFolder = header.isDirectory();
        boolean isEncrypted = header.isEncrypted();
        String comment = header.getFileComment();
        int attributes = ByteBuffer.wrap(header.getExternalFileAttributes()).getInt();
        long packedSize;
        long rawSize;
        Zip64ExtendedInfo z64Info;
        if (Objects.nonNull(z64Info = header.getZip64ExtendedInfo())) {
            packedSize = z64Info.getCompressedSize();
            rawSize = z64Info.getUncompressedSize();
        } else {
            packedSize = header.getCompressedSize();
            rawSize = header.getUncompressedSize();
        }

        FileInfo fileInfo = new FileInfo(index.getAndIncrement(), level, fileName, hash, packedSize, rawSize, lastModifiedTime,
                                         lastModifiedTime, lastModifiedTime, "", "", attributes,
                                         comment, isFolder, isEncrypted, Collections.emptyMap());
        return Optional.of(fileInfo);
    }
}
