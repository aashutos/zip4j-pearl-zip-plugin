/*
 * Copyright © 2021 92AK
 */

import com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveReadService;
import com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveWriteService;

/**
 *  Implementation of Pearl Zip Archive Service, which utilises 7-Zip Java Binding library underneath to provide
 *  compression functionality for various common archive formats.
 */
module com.ntak.pearlzip.archive.acc  {
    exports com.ntak.pearlzip.archive.acc.pub;

    requires org.apache.commons.compress;
    requires com.ntak.pearlzip.archive;
    requires org.tukaani.xz;

    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    provides com.ntak.pearlzip.archive.pub.ArchiveWriteService with CommonsCompressArchiveWriteService;
    provides com.ntak.pearlzip.archive.pub.ArchiveReadService with CommonsCompressArchiveReadService;
}