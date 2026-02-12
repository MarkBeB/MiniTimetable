package org.tud.minitimetable;

import java.io.IOException;
import java.nio.file.Path;

import org.tud.minitimetable.util.DataModelManager;

public class Main {

	private static final Path miniZincLocation = Path.of("..", "..", "..", "MiniZinc", "minizinc.exe");
	private static final Path resourceFolder = Path.of("./", "resources");

	private static Path getUserDirectory() {
		String currentDir = System.getProperty("user.dir");
		return Path.of(currentDir);
	}

	private static Path changeFileExtension(Path filePath, String newExtension) {
		String fileName = filePath.getFileName().toString();
		int extensionDelimiter = fileName.lastIndexOf('.');

		if (extensionDelimiter > 0) {
			int newExtensionStart = newExtension.startsWith(".") ? 1 : 0;
			String newfileName = fileName.substring(0, extensionDelimiter + 1)
					+ newExtension.substring(newExtensionStart);
			return filePath.resolveSibling(newfileName);
		}

		return filePath;
	}

	public static void main(String[] args) throws IOException {
		var userDirectory = getUserDirectory();
		var miniZincExe = userDirectory.resolve(miniZincLocation).normalize();
		var resourceDirectory = userDirectory.resolve(resourceFolder).normalize();
//		var modelFile = resourceDirectory.resolve("ihtc").resolve("i01.json");
//
//		InputModelReader modelReader = new InputModelReader();
//		IhtcModel model = modelReader.read(modelFile);
//
//		var dznFile = changeFileExtension(modelFile, ".dzn");
//		OutputModelWriter modelWriter = new OutputModelWriter();
//		modelWriter.write(model, dznFile);
//
//		System.out.println("DONE");
//		System.out.println("File created: " + dznFile);

		Path modelFolder = resourceDirectory.resolve("ihtc");
		Path outputFolder = resourceDirectory.resolve("out");

		DataModelManager manager = new DataModelManager(modelFolder, outputFolder);
		manager.loadDataModel("i01");
		manager.writeDataModelAsDZN();

		System.out.println("DONE");
		System.out.println("File created: " + manager.getPathOfOutput());
	}

}
