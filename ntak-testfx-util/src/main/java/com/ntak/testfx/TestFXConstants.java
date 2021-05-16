/*
 * Copyright © 2021 92AK
 */
package com.ntak.testfx;

import java.util.regex.Pattern;

public class TestFXConstants {

    public enum Platform {
        WIN,LINUX,MAC
    }

    public static final Platform PLATFORM = getPlatform();
    public static final Pattern SSV = Pattern.compile(Pattern.quote("/"));

    private static Platform getPlatform() {
        String os = System.getProperty("os.name");

        if (os.startsWith("Windows")) {
            return Platform.WIN;
        }

        if (os.startsWith("Mac")) {
            return Platform.MAC;
        }

        return Platform.LINUX;
    }
}
