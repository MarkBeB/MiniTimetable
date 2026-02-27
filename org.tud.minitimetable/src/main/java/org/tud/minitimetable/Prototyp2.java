package org.tud.minitimetable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Stream;

import org.tud.minitimetable.extern.ValidatorRunner;
import org.tud.minitimetable.extern.solver.MiniZincRunner;
import org.tud.minitimetable.extern.solver.SolutionStatus;
import org.tud.minitimetable.extern.solver.SolverResult;
import org.tud.minitimetable.model.util.InputModelReader;
import org.tud.minitimetable.model.util.OutputModelWriter;
import org.tud.minitimetable.util.MiniZincLocator;
import org.tud.minitimetable.util.PathUtils;

public class Prototyp2 {

	private static final Path userDirectory = Path.of(System.getProperty("user.dir"));
	private static final Path resourceDirectory = Path.of("./", "resources");

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

	private static Path getUserDirectory() {
		return userDirectory;
	}

	private static Path getResourceDirectory() {
		return getUserDirectory().resolve(resourceDirectory).normalize();
	}

	public static void main3(String[] args) throws IOException, InterruptedException {
		Path outputFolder = getResourceDirectory().resolve("out");
		Path validatorFile = getResourceDirectory().resolve("IHTP_Validator_Win11.exe");
		Path workingDirectory = outputFolder;

		ValidatorRunner validator = new ValidatorRunner(validatorFile, workingDirectory);
		var result = validator.run(Path.of("./../i01.json"), Path.of("./../sol_i01.json"));

		if (result.getTotalViolations() > 0) {
			System.err.println("Found " + result.violations + " violations!");
			for (var violation : result.violations) {
				if (violation.violations > 0) {
					System.err.println(violation);
				}
			}
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Path constraintModelFile = getResourceDirectory().resolve("minizinc").resolve("AllConstraints.mzn");

		Path inputDataModelFile = Path.of("ihtc", "i01");

		Path inputFolder = getResourceDirectory().resolve("input");
		Path outputFolder = getResourceDirectory().resolve("out");
		Path workingDirectory = outputFolder;

		Path miniZincExe = getMiniZincExe();
		Path validatorExe = getResourceDirectory().resolve("IHTP_Validator_Win11.exe");

		// ============================================================================================================
		Instant startTime = java.time.Instant.now();

		System.out.println("Start Time: " + startTime);

		inputFolder = inputFolder.normalize();
		System.out.println("Model Input Folder: " + inputFolder);
		if (!inputFolder.isAbsolute())
			throw new IllegalArgumentException("Input folder needs to be an absolute path");

		outputFolder = outputFolder.normalize();
		System.out.println("Data Output Folder: " + outputFolder);
		if (!outputFolder.isAbsolute())
			throw new IllegalArgumentException("Output folder needs to be an absolute path");

		Path relativeOutputFolder = null;
		if (inputDataModelFile.isAbsolute()) {
			if (inputDataModelFile.startsWith(inputFolder)) {
				relativeOutputFolder = inputFolder.relativize(inputDataModelFile).normalize();
			} else {
				relativeOutputFolder = inputDataModelFile.getFileName();
			}
		} else {
			relativeOutputFolder = inputDataModelFile.normalize();
		}
		relativeOutputFolder = PathUtils.removeFileExtension(relativeOutputFolder);

		if (!inputDataModelFile.isAbsolute()) {
			inputDataModelFile = inputFolder.resolve(inputDataModelFile);
		}

		Path inputModelFile = inputDataModelFile;
		inputModelFile = PathUtils.changeFileExtension(inputModelFile, ".json");

		Path dataModelFile = outputFolder.resolve(relativeOutputFolder).resolve(inputDataModelFile.getFileName());
		dataModelFile = PathUtils.changeFileExtension(dataModelFile, ".dzn");

		Path lpFile = PathUtils.changeFileExtension(dataModelFile, ".lp");

		System.out.println("Clear previously generated files");
		if (Files.exists(dataModelFile.getParent())) {
			try (Stream<Path> walk = Files.walk(dataModelFile.getParent())) {
				walk.sorted(Comparator.reverseOrder()).map(Path::toFile).peek(System.out::println)
						.forEach(File::delete);
			}
		}

		System.out.println("Load data model: " + inputModelFile);
		InputModelReader reader = new InputModelReader();
		var dataModel = reader.read(inputModelFile);
		// do any preprocessing here

		System.out.println("Write data model to dzn file: " + dataModelFile);
		OutputModelWriter writer = new OutputModelWriter();
		writer.write(dataModel, dataModelFile);

//		DataModelManager dataManager = new DataModelManager(inputFolder, outputFolder);
//		var dataModel = dataManager.loadModel(internalFolder.resolve("i01"));
//		dataManager.writeDataModelAsDZN(dataModel);

		System.out.println("Run MiniZinc with constraint: " + constraintModelFile);
		System.out.println("Run MiniZinc with data: " + dataModelFile);

		MiniZincRunner mzRunner = new MiniZincRunner(miniZincExe, workingDirectory);
		mzRunner.config().setConstraintModel(constraintModelFile);
		mzRunner.config().setDataModel(dataModelFile);
		mzRunner.config().setNumberOfThreads(1);
		mzRunner.config().setTimeLimit(30 * 60 * 1000);
//		mzRunner.config().setWriteModel(lpFile);

		mzRunner.setSolutionOutputFolder(dataModelFile.getParent());
		mzRunner.parseOutput(true);
		SolverResult result = mzRunner.runMiniZinc();

		if (result.status == SolutionStatus.ERROR) {
			System.err.println("MiniZinc Error");
			return;
		}
		if (result.status == SolutionStatus.INFEASABLE) {
			System.out.println("SOLUTION: " + SolutionStatus.INFEASABLE);
			return;
		}

		System.out.println("SOLUTION: " + result.status + " found " + result.solutions.size() + " solution(s)");

		if (result.solutions.isEmpty())
			return;

		System.out.println("Start validation");
		ValidatorRunner validator = new ValidatorRunner(validatorExe, workingDirectory);

		for (var solution : result.solutions) {
			validator.run(inputModelFile, solution.outputFile);
			if (true) {
				break;
			}
		}

	}

	private static Path getMiniZincExe() {
		MiniZincLocator minizincLocator = new MiniZincLocator();
		minizincLocator.addFolder(Path.of("..", "..", "MiniZinc", "minizinc.exe"));
		return minizincLocator.searchMiniZinc().stream().findFirst().get();
	}

}
