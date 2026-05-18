package org.tud.minitimetable.eval.extract;

import static org.tud.minitimetable.DefaultLocations.getIHTPValidatorFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.tud.minitimetable.extern.validator.ValidatorResult;
import org.tud.minitimetable.extern.validator.ValidatorRunner;
import org.tud.minitimetable.model.util.SolutionFileReader;

public class SolutionParser {

	public static int counter = 0;

	public static record SolutionData() {

	}

	public static record Cost() {

	}

	private final Path solutionFile;
	private final Path dataFile;

	public SolutionParser(Path solutionFile, Path dataFile) {
		this.solutionFile = Objects.requireNonNull(solutionFile, "solutionFile");
		this.dataFile = Objects.requireNonNull(dataFile, "dataFile");
	}

	public SolutionData parse() throws IOException {
		if (!Files.exists(solutionFile))
			return null;

		var reader = new SolutionFileReader();
		var solutions = reader.parseSolutionFile(solutionFile);

		if (solutions.size() == 0)
			return null;

		var best = solutions.getLast();
		var file = solutionFile.resolveSibling(reader.constructNameForSolution(best));
		if (!Files.exists(file))
			reader.writeToFile(best, file);

		ValidatorRunner validator = new ValidatorRunner(getIHTPValidatorFile(), solutionFile.getParent());
		ValidatorResult validatorResult = null;

		try {
			validatorResult = validator.run(dataFile, file);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null; // TODO
		}

		// TODO

		return null;
	}

}
