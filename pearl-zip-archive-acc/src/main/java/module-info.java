/*
 *  Copyright (c) 2021 92AK
 */
/**
 *  Implementation of Pearl Zip Archive Service, which utilises 7-Zip Java Binding library underneath to provide
 *  compression functionality for various common archive formats.
 */
module com.ntak.pearlzip.archive.acc  {
    requires org.apache.commons.compress;
    requires com.ntak.pearlzip.archive;
    requires org.tukaani.xz;
    requires eventbus;

    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    provides com.ntak.pearlzip.archive.pub.ArchiveWriteService with com.ntak.pearlzip.archive.acc.pub.CommonsCompressArchiveService;
}