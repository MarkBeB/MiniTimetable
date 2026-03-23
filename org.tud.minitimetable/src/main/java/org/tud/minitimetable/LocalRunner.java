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
//		minizinc.getConfig().setMiniZincExe(exe);
//		minizinc.getConfig().runnerLog = new DefaultFileLog("%s-log");
//		minizinc.getConfig().backendLog = new DefaultFileLog("%s-backend");
//		minizinc.getConfig().solverOutput = new DefaultFileLog("%s-solution");
//		minizinc.getConfig().solverModelLog = new DefaultFileLog("%s-lp.lp", true);

		minizinc.getSolverConfig().timeLimitMS = 2 * 60 * 1000; // DefaultSettings.DEFAULT_TIME_LIMIT_IN_MS;

		minizinc.setup(modelFile, dataFile, outputFolder);
		minizinc.run().join();
	}

}
