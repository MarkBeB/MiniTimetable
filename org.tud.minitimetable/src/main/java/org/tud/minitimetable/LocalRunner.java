package org.tud.minitimetable;

import java.io.IOException;
import java.nio.file.Path;

import org.tud.minitimetable.extern.solver.MiniZinc;
import org.tud.minitimetable.util.PathUtils;

public class LocalRunner {

	public static void main(String[] args) throws IOException {
		Path resourceDirectory = Path.of("./", "resources");
		Path modelFile = resourceDirectory.resolve("minizinc").resolve("HardAndSomeSoft.mzn");
		Path dataFile = resourceDirectory.resolve("input").resolve("ihtc").resolve("i01.json");
		Path outputFolder = resourceDirectory.resolve("out")
				.resolve(PathUtils.removeFileExtension(dataFile.getFileName()));

		MiniZinc minizinc = new MiniZinc();
		DefaultSettings.applyDefaultMiniZincConfiguration(minizinc);
		minizinc.getConfig().timeLimitMS = 2 * 60 * 1000l;

		minizinc.run(modelFile, dataFile, outputFolder).join();
	}

}
