package org.tud.minitimetable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.tud.minitimetable.extern.solver.MiniZinc;
import org.tud.minitimetable.extern.solver.ProcessLogger.DefaultFileLog;
import org.tud.minitimetable.extern.validator.ValidatorRunner;
import org.tud.minitimetable.model.util.SolutionFileReader;

public class LocalRunner {

	public static void main(String[] args) throws IOException, InterruptedException {
		Path resourceDirectory = Path.of("./", "resources").toAbsolutePath();
		Path modelFile = resourceDirectory.resolve("minizinc").resolve("AllConstraints.mzn");
		Path dataFile = resourceDirectory.resolve("input").resolve("ihtc").resolve("i02.json");
		Path outputFolder = resourceDirectory.resolve("out").resolve("i02-4");

		MiniZinc minizinc = new MiniZinc();
		DefaultSettings.applyDefaultMiniZincConfiguration(minizinc);
		minizinc.getConfig().timeLimitMS = 5 * 60 * 1000l;

		minizinc.run(modelFile, dataFile, outputFolder).join();

		var solutionOutputFile = ((DefaultFileLog) minizinc.getConfig().solverOutput).getPath();
		if (Files.exists(solutionOutputFile)) {
			var reader = new SolutionFileReader();
			var solutions = reader.parseSolutionFile(solutionOutputFile);
			if (solutions.size() > 0) {
				var best = solutions.getLast();
				var file = outputFolder.resolve(reader.constructNameForSolution(best));
				reader.writeToFile(best, file);

				System.out.println(String.format("Validating file '%s' with solution '%s'", dataFile, file));
				ValidatorRunner validator = new ValidatorRunner(resourceDirectory.resolve("IHTP_Validator_Win11.exe"),
						outputFolder);
				validator.run(dataFile, file);
			}
		}
	}

	public static void main2(String[] args) throws IOException, InterruptedException {
		Path resourceDirectory = Path.of("./", "resources").toAbsolutePath();
		Path dataFile = resourceDirectory.resolve("input").resolve("ihtc").resolve("i02.json");
		Path outputFolder = resourceDirectory.resolve("out").resolve("i02-4");
		Path solutionFile = outputFolder.resolve("solution#1_1464.json");

		System.out.println(String.format("Validating file '%s' with solution '%s'", dataFile, solutionFile));
		ValidatorRunner validator = new ValidatorRunner(resourceDirectory.resolve("IHTP_Validator_Win11.exe"),
				outputFolder);
		validator.run(dataFile, solutionFile);
	}

}
