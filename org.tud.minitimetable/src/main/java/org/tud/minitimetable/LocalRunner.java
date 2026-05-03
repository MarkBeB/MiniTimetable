package org.tud.minitimetable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.tud.minitimetable.extern.solver.CodeLogger.FileCodeLogger;
import org.tud.minitimetable.extern.solver.CodeLogger.MixedCodeLogger;
import org.tud.minitimetable.extern.solver.MiniZinc;
import org.tud.minitimetable.extern.solver.ProcessLogger.DefaultFileLog;
import org.tud.minitimetable.extern.validator.ValidatorRunner;
import org.tud.minitimetable.model.util.SolutionFileReader;
import org.tud.minitimetable.util.PathUtils;

public class LocalRunner {

	private static final Path resourceDirectory = Path.of("./", "resources").toAbsolutePath();

	public static void main(String[] args) throws IOException, InterruptedException {
		Path modelFile = resourceDirectory.resolve("minizinc").resolve("v1").resolve("AllConstraints.mzn");
//		Path modelFile = resourceDirectory.resolve("minizinc").resolve("v2").resolve("AllConstraints.mzn");
//		Path modelFile = resourceDirectory.resolve("minizinc").resolve("v4").resolve("AllConstraints.mzn");
		Path dataFile = resourceDirectory.resolve("input").resolve("ihtc").resolve("i04.json");
//		Path dataFile = resourceDirectory.resolve("minizinc").resolve("data").resolve("i03.json");

		String fileName = PathUtils.getFileNameWithoutExtension(dataFile);
		String version = modelFile.getParent().getFileName().toString();

		Path outputFolder = resourceDirectory.resolve("out").resolve("1-" + version + "-" + fileName);

		MiniZinc minizinc = new MiniZinc();
		DefaultSettings.applyDefaultMiniZincConfiguration(minizinc);

		minizinc.getConfig().solverModelLog = new DefaultFileLog("%s-ilp.ilp", true);
		minizinc.getConfig().logger = new MixedCodeLogger(outputFolder.resolve("log.txt"));
		minizinc.getConfig().timeLimitMS = 15 * 60 * 1000l;
		minizinc.getConfig().solverTimeLimitMS = 1 * 1 * 1000L;
		minizinc.getConfig().gurobiParameterFile = resourceDirectory.resolve("minizinc").resolve("Gurobi.prm");
		minizinc.getConfig().optimizeLevel = 1;

		minizinc.run(modelFile, dataFile, outputFolder).join();
		((FileCodeLogger) minizinc.getConfig().logger).close();

		var solutionOutputFile = ((DefaultFileLog) minizinc.getConfig().solverOutput).getPath();
		if (Files.exists(solutionOutputFile))
			processSoltions(solutionOutputFile, dataFile);
	}

	public static void main2(String[] args) throws IOException, InterruptedException {
		Path dataFile = resourceDirectory.resolve("input").resolve("ihtc").resolve("i01.json");
		String fileName = PathUtils.getFileNameWithoutExtension(dataFile);
		Path outputFolder = resourceDirectory.resolve("out").resolve("workstation").resolve("out")
				.resolve(fileName + "-05");
		Path solutionFile = outputFolder.resolve("solution.json");

		processSoltions(solutionFile, dataFile);
	}

	private static void processSoltions(Path solutionFile, Path dataFile) throws IOException, InterruptedException {
		var reader = new SolutionFileReader();
		var solutions = reader.parseSolutionFile(solutionFile);

		if (solutions.size() > 0) {
			var best = solutions.getLast();
			var file = solutionFile.resolveSibling(reader.constructNameForSolution(best));
			reader.writeToFile(best, file);

			System.out.println(String.format("Validating file '%s' with solution '%s'", dataFile, file));
			ValidatorRunner validator = new ValidatorRunner(resourceDirectory.resolve("IHTP_Validator_Win11.exe"),
					solutionFile.getParent());
			validator.run(dataFile, file);
		}

	}

}
