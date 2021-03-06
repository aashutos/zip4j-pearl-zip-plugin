/*
 *  Copyright (c) 2021 92AK
 */
import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.szjb.pub.SevenZipArchiveService;

/**
 *  Implementation of Pearl Zip Archive Service, which utilises 7-Zip Java Binding library underneath to provide
 *  extraction functionality for various common archive formats.
 */
module com.ntak.pearlzip.archive.szjb {
    exports com.ntak.pearlzip.archive.szjb.pub;

    requires com.ntak.pearlzip.archive;
    requires sevenzipjbinding;

    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    // SPI Declaration
    provides ArchiveReadService with SevenZipArchiveService;
}