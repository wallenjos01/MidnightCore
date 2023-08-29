package org.wallentines.mcore.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class FileUtil {

    public static void copyFolder(@NotNull File source, @NotNull Path destination) throws IOException {

        File destFile = destination.toFile();
        if(!destFile.isDirectory() && !destFile.mkdirs()) {
            throw new IllegalStateException("Unable to create config directory!");
        }

        File[] sourceFiles = source.listFiles();
        if(sourceFiles != null) for(File f : sourceFiles) {
            Path path = destination.resolve(f.getName());
            if(f.isDirectory()) {
                copyFolder(f, path);
            } else {
                copyFile(f, path);
            }
        }
    }

    public static void copyFile(File source, Path destination) throws IOException{

        File dest = destination.toFile();
        try(FileInputStream fis = new FileInputStream(source); FileOutputStream fos = new FileOutputStream(dest)) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = fis.read(buffer)) != 0) {
                fos.write(buffer, 0, read);
            }
        }
    }
}
