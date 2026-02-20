package org.tud.minitimetable.util;

import java.nio.file.Path;

public class PathUtils {
	private PathUtils() {
	}

	public static String getFileName(Path filePath) {
		return filePath.getFileName().toString();
	}

	public static String getFileExtension(Path filePath) {
		String fileName = getFileName(filePath);
		int delimiterPosition = fileName.lastIndexOf('.');
		return delimiterPosition < 0 ? "" : fileName.substring(delimiterPosition + 1);
	}

	public static String getFileNameWithoutExtension(Path filePath) {
		String fileName = getFileName(filePath);
		int delimiterPosition = fileName.lastIndexOf('.');
		int fileNameLength = delimiterPosition < 0 ? fileName.length() : delimiterPosition;
		return fileName.substring(0, fileNameLength);
	}

	public static Path changeFileExtension(Path filePath, String newExtension) {
		String newFileName = getFileNameWithoutExtension(filePath)
				+ (newExtension.startsWith(".") ? newExtension : "." + newExtension);
		return filePath.resolveSibling(newFileName);
	}

	public static Path resolve(Path first, Path second) {
		if (second == null)
			return first;
		return first.resolve(second);
	}
}
