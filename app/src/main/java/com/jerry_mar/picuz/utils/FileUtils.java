package com.jerry_mar.picuz.utils;

import java.io.File;

public class FileUtils {
    public static File createFile(File folder, String filename) {
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        return new File(folder, filename);
    }
}
