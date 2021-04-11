/*
 * Copyright © 2021 92AK
 */
package com.ntak.pearlzip.archive.szjb.pub;

import com.ntak.pearlzip.archive.pub.ArchiveReadService;
import com.ntak.pearlzip.archive.pub.FileInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SevenZipArchiveServiceTest {

private static String fileName = SevenZipArchiveServiceTest.class.getClassLoader().getResource("test.zip").getFile();

private ArchiveReadService service = new SevenZipArchiveService();

@Test
@DisplayName("Test: List files for a valid zip file will return recursive contents of the file")
public void testListFiles_ValidFile_ReturnsContents() {
    long sessionId = 0;
    List<FileInfo> files = service.listFiles(sessionId, fileName);
    Assertions.assertNotNull(files, "Files should not be null");
    Assertions.assertEquals(5, files.size(), "Files should contain 5 files/folders");
    List<String> expectations = new ArrayList<>(List.of("first-file", "second-file", "first-folder", "first-folder/.DS_Store",
                                         "first-folder/first-nested-file"));
    int i = 0;
    List<String> fileNames = files.stream()
                                  .map(FileInfo::getFileName)
                                  .sorted(CharSequence::compare)
                                  .collect(Collectors.toList());
    expectations.sort(CharSequence::compare);
    for (String file : fileNames) {
        Assertions.assertEquals(expectations.get(i), file, String.format("File: %s was not found as " +
                                                                                            "expected",
                                                                              expectations.get(i)));
        i++;
    }
}

}
