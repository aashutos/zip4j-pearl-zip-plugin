/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.zip4j.pub;

import com.ntak.pearlzip.archive.pub.ArchiveInfo;
import com.ntak.pearlzip.archive.pub.ErrorMessage;

import java.util.Objects;
import java.util.function.Predicate;

import static com.ntak.pearlzip.archive.pub.ArchiveService.DEFAULT_BUS;
import static com.ntak.pearlzip.archive.util.LoggingUtil.resolveTextKey;
import static com.ntak.pearlzip.archive.zip4j.constants.Zip4jConstants.*;

public class Zip4jPasswordValidator implements Predicate<ArchiveInfo> {
    @Override
    public boolean test(ArchiveInfo archiveInfo) {
        try {
            if (Objects.nonNull(archiveInfo)) {
                if (archiveInfo.<Boolean>getProperty(KEY_ENCRYPTION_ENABLE).orElse(Boolean.FALSE).equals(Boolean.TRUE) && archiveInfo.<char[]>getProperty(KEY_ENCRYPTION_PW)
                                                                                                                                     .orElse(new char[0])
                        .length == 0) {
                    throw new IllegalStateException(resolveTextKey(LOG_Z4J_PW_LENGTH));
                }

                return true;
            }
        } catch (IllegalStateException e) {
            DEFAULT_BUS.post(new ErrorMessage(System.currentTimeMillis(),
                                              resolveTextKey(TITLE_Z4J_VALIDATION_ISSUE),
                                              null,
                                              resolveTextKey(BODY_Z4J_VALIDATION_ISSUE, e.getMessage()),
                                              e,
                                              archiveInfo));
        }
        return false;
    }
}
