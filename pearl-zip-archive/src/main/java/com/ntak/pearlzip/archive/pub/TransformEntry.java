/*
 *  Copyright (c) 2021 92AK
 */
package com.ntak.pearlzip.archive.pub;

import java.util.Optional;

/**
 *   Simple interface, which converts a raw zip entry of type T to a generic {@link .FileInfo} class.
 *
 *   @param <T> Raw file entry type
 *  @author Aashutos Kakshepati
 */
public interface TransformEntry<T> {
    default Optional<FileInfo> transform(T rawEntry) {return Optional.empty();};
    default Optional<T> transform(FileInfo rawEntry) {return Optional.empty();};
}
