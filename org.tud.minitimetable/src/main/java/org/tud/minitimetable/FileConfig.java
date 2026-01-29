package org.tud.minitimetable;

import java.nio.file.Path;

public class FileConfig {

	private static final Path miniZincLocation = Path.of("..", "..", "..", "MiniZinc", "minizinc.exe");
	private static final Path resourceFolder = Path.of("./", "resources");

	public static Path getUserDirectory() {
		String currentDir = System.getProperty("user.dir");
		return Path.of(currentDir);
	}

	public static Path getResourceFolder() {
		return getUserDirectory().resolve(resourceFolder);
	}

}
