package org.tud.minitimetable;

import java.nio.file.Path;

public class DefaultLocations {

	public static Path getResourceDirectory() {
		return Path.of("./", "resources").toAbsolutePath();
	}

	public static Path getModelDirectory() {
		return getResourceDirectory().resolve("minizinc");
	}

	public static Path getDataDirectory() {
		return getResourceDirectory().resolve("input").resolve("ihtc");
	}

	public static Path getIHTPValidatorFile() {
		return getResourceDirectory().resolve("IHTP_Validator_Win11.exe");
	}

}
