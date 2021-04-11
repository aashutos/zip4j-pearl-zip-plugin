/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.ui.model;

import com.ntak.pearlzip.archive.pub.FileInfo;

import static com.ntak.pearlzip.ui.model.FXMigrationInfo.MigrationType.NONE;

/**
 *  Information about a specific migration process within an archive is expressed by this class.
 *  @author Aashutos Kakshepati
*/
public class FXMigrationInfo {
    public enum MigrationType {
        COPY, MOVE, DELETE, NONE
    }

    private MigrationType type = NONE;
    private FileInfo file;

    public synchronized MigrationType getType() {
        return type;
    }

    public FileInfo getFile() {
        return file;
    }

    public synchronized boolean initMigration(MigrationType migrationType, FileInfo rootFile) {
        if (type.equals(NONE) && !migrationType.equals(NONE)) {
            type = migrationType;
            file = rootFile;
            return true;
        }
        return false;
    }

    public synchronized void clear() {
        type = NONE;
        file = null;
    }
}
