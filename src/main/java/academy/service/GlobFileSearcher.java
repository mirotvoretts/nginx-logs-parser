package academy.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

public class GlobFileSearcher {
    private boolean containsWildcards(String path) {
        return path.contains("*") || path.contains("?");
    }

    private List<File> findFilesByGlob(String pattern) {
        try {
            Path searchDirectory = getSearchDirectory(pattern);
            String filePattern = getFileNamePattern(pattern);

            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + filePattern);

            try (var stream = Files.walk(searchDirectory)) {
                return stream.filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(path.getFileName()))
                        .map(Path::toFile)
                        .toList();
            }
        } catch (IOException | InvalidPathException e) {
            return List.of();
        }
    }

    private Path getSearchDirectory(String pattern) {
        int lastSlash = pattern.lastIndexOf('/');
        if (lastSlash != -1) {
            String dirPath = pattern.substring(0, lastSlash);
            return Path.of(dirPath);
        } else {
            return Path.of(".");
        }
    }

    private String getFileNamePattern(String pattern) {
        int lastSlash = pattern.lastIndexOf('/');
        if (lastSlash != -1) {
            return pattern.substring(lastSlash + 1);
        } else {
            return pattern;
        }
    }

    /**
     * Ищет файлы по заданному пути. Поддерживаются точные пути и шаблоны с подстановочными знаками glob
     *
     * @param path путь до файла
     * @return список файлов, подошедших под критерий поиска
     */
    public List<File> searchFiles(String path) {
        if (containsWildcards(path)) {
            return findFilesByGlob(path);
        }

        Path filePath = Path.of(path);
        if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
            return List.of(filePath.toFile());
        }
        return List.of();
    }
}
