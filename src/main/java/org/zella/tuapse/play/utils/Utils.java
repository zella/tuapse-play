package org.zella.tuapse.play.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
    public static int recursiveDeleteFilesAcessedOlderThanNDays(int days, Path dirPath) throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        long cutOff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000);
        Files.list(dirPath)
                .forEach(path -> {
                    if (Files.isDirectory(path)) {
                        try {
                            recursiveDeleteFilesAcessedOlderThanNDays(days, path);
                        } catch (IOException e) {
                            // log here and move on
                        }
                    } else {
                        try {
                            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                            FileTime time = attrs.lastAccessTime();

                            if (time.toMillis() < cutOff) {
                                Files.delete(path);
                                count.incrementAndGet();
                            }
                        } catch (IOException ex) {
                            // log here and move on
                        }
                    }
                });
        return count.get();
    }
}
