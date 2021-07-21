/*
 * Copyright © 2021 92AK
 */

import com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveReadService;
import com.ntak.pearlzip.archive.zip4j.pub.Zip4jArchiveWriteService;

/*
 *  Copyright (c) 2021 92AK
 */
module pearl.zip.archive.zip4j {
    exports com.ntak.pearlzip.archive.zip4j.pub;

    requires com.ntak.pearlzip.archive;

    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;

    requires zip4j;

    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;

    provides com.ntak.pearlzip.archive.pub.ArchiveWriteService with Zip4jArchiveWriteService;
    provides com.ntak.pearlzip.archive.pub.ArchiveReadService  with Zip4jArchiveReadService;

    opens com.ntak.pearlzip.archive.zip4j.pub to com.ntak.pearlzip.ui,javafx.fxml;
}