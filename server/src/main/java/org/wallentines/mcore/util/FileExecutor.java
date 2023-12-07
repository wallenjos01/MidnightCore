package org.wallentines.mcore.util;

import java.io.File;
import java.util.HashMap;
import java.util.function.Consumer;

public class FileExecutor {

    private static final HashMap<File, Thread> OPEN_FILES = new HashMap<>();

    private final File file;
    private final Consumer<File> action;

    public FileExecutor(File file, Consumer<File> action) {
        this.file = file;
        this.action = action;
    }

    public void start() {

        new Thread(() -> {
            if(OPEN_FILES.containsKey(file)) {
                try {
                    OPEN_FILES.get(file).join();
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }

            OPEN_FILES.put(file, Thread.currentThread());

            action.accept(file);

            OPEN_FILES.remove(file);

        }).start();

    }

}
