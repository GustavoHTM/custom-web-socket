package org.communication;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.filechooser.FileSystemView;

public class FileUtils {

    public static String getDesktopPath() {
        FileSystemView view = FileSystemView.getFileSystemView();
        File file = view.getHomeDirectory();
        return file.getPath();
    }

    public static String getDownloadsPath() {
        Path[] possiblePaths = {
            Paths.get(System.getProperty("user.home"), "Downloads"),
            Paths.get(System.getProperty("user.home"), "downloads"),
            Paths.get(System.getProperty("user.home"), "Meus downloads"),
            Paths.get(System.getenv("USERPROFILE"), "Downloads")
        };

        for (Path path : possiblePaths) {
            if (Files.isDirectory(path)) return path.toString();
        }

        return Paths.get(System.getProperty("user.home")).toString();
    }
}
