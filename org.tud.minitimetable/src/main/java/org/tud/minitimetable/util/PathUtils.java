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
		String fileName = filePath.getFileName().toString();
		int delimiterPosition = fileName.lastIndexOf('.');
		boolean hasExtension = delimiterPosition > 0;

		if (hasExtension) {
			if (newExtension != null && fileName.endsWith(newExtension))
				return filePath;

			fileName = fileName.substring(0, delimiterPosition);

			if (newExtension == null || newExtension.isBlank())
				return filePath.resolveSibling(fileName);

		} else if (newExtension == null || newExtension.isBlank())
			return filePath;

		String newFileName = fileName + (newExtension.startsWith(".") ? newExtension : "." + newExtension);
		return filePath.resolveSibling(newFileName);
	}

	public static Path resolve(Path first, Path second) {
		if (second == null)
			return first;
		return first.resolve(second);
	}

	public static Path removeFileExtension(Path filePath) {
		return changeFileExtension(filePath, null);
	}
}
