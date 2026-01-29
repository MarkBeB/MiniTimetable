package org.tud.minitimetable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.tud.minitimetable.model.IhtcModel;
import org.tud.minitimetable.util.FileWriter;
import org.tud.minitimetable.util.Ihtc2DznModelWriter;
import org.tud.minitimetable.util.Json2IhtcModelParser;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

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

	public static void mainX(String[] args) throws IOException {
		IhtcFromJsonReader inputReader = new IhtcFromJsonReader(FileConfig.getResourceFolder().resolve("ihtc"));
		var model = inputReader.readModel("i01");
		// DO preprocessing here

		MiniZincGenerator miniGenerator = new MiniZincGenerator();
	}

	public static void main(String[] args) {
		var userDirectory = getUserDirectory();
		var miniZincExe = userDirectory.resolve(miniZincLocation).normalize();
		var resourceDirectory = userDirectory.resolve(resourceFolder).normalize();
		var dataFile = resourceDirectory.resolve("ihtc").resolve("i01.json");

//		Jsonb jsonb = Jsonb.instance();
//		JsonType<IhtcModel> customerType = jsonb.type(IhtcModel.class);

		try {
			Json2IhtcModelParser modelReader = new Json2IhtcModelParser();
			Ihtc2DznModelWriter modelWriter = new Ihtc2DznModelWriter();

			var data = Files.readAllBytes(dataFile);
			IhtcModel model = modelReader.read((JSONObject) JSON.parse(data));

			var newDatafile = changeFileExtension(dataFile, ".dzn");
			try (var buffer = Files.newBufferedWriter(newDatafile, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
					var writer = new FileWriter(buffer)) {

				modelWriter.write(model, writer);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
