package org.tud.minitimetable.util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilePathResolver {
	private final Path _rootFolder;

	public FilePathResolver(Path rootFolder) {
		this._rootFolder = Objects.requireNonNull(rootFolder);
	}

	public List<Path> findAllFiles(String pattern) throws IOException {
		List<Path> filePaths = new ArrayList<>();
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		Files.walkFileTree(_rootFolder, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (matcher.matches(file)) {
					filePaths.add(file);
				}

				return FileVisitResult.CONTINUE;
			}
		});
		return filePaths;
	}

	public Path findFile(String pattern) throws IOException {
		List<Path> filePaths = new ArrayList<>(1);
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
		Files.walkFileTree(_rootFolder, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (matcher.matches(file)) {
					filePaths.add(file);
				}

				return FileVisitResult.TERMINATE;
			}
		});

		return filePaths.isEmpty() ? null : filePaths.get(0);
	}
}