package org.wallentines.midnightcore.common.util;

import java.io.File;
import java.nio.file.Path;

public final class FileUtil {

    public static File tryCreateDirectory(Path path) {

        File out = path.toFile();
        if(out.exists()) {
            if(!out.isDirectory()) return null;
            return out;
        }

        if(out.mkdirs()) return out;

        return null;
    }

}
