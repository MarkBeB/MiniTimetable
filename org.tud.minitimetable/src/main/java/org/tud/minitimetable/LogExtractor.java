package org.tud.minitimetable;

import static org.tud.minitimetable.DefaultLocations.getDataDirectory;
import static org.tud.minitimetable.DefaultLocations.getIHTPValidatorFile;
import static org.tud.minitimetable.DefaultLocations.getResourceDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.tud.minitimetable.eval.extract.BackendParser;
import org.tud.minitimetable.eval.extract.BackendParser.BackendData;
import org.tud.minitimetable.eval.extract.MainCSV;
import org.tud.minitimetable.eval.extract.RunLogParser;
import org.tud.minitimetable.eval.extract.RunLogParser.RunData;
import org.tud.minitimetable.extern.validator.ValidatorResult;
import org.tud.minitimetable.extern.validator.ValidatorRunner;
import org.tud.minitimetable.model.util.SolutionFileReader;

public class LogExtractor {

	private static final String BACKEND_FILE_NAME = "backend.txt";
	private static final String SOLUTION_FILE_NAME = "solution.json";
	private static final String RUN_FILE_NAME = "run.log";

	public static void main(String[] args) throws IOException, InterruptedException {
		Path instanceFilesForValidation = getDataDirectory();
		Path logDirectory = getResourceDirectory().resolve("workstation").resolve("raw");
		Path outputDirectory = getResourceDirectory().resolve("workstation").resolve("extracted");

		LogExtractor.extractDataFromLog(instanceFilesForValidation, logDirectory, outputDirectory);
	}

	public static void extractDataFromLog(Path instanceDirectory, Path inputDirectory, Path outputDirectory)
			throws IOException, InterruptedException {

		if (!Files.exists(outputDirectory))
			Files.createDirectories(outputDirectory);

		Collection<ModelSelection> selectedRuns = collectRunFolders(inputDirectory).stream() //
				.filter(m -> 5 <= m.runId) //
				.sorted() //
				.toList();

		MainCSV mainCSV = new MainCSV();

		for (var run : selectedRuns) {
			Path runFile = run.folder().resolve(RUN_FILE_NAME);
			Path backendFile = run.folder().resolve(BACKEND_FILE_NAME);
			Path solutionFile = run.folder().resolve(SOLUTION_FILE_NAME);

			var runLog = readRunLog(runFile);
			if (runLog == null) {
				System.out.println("Error: " + run.folder());
				System.out.println("No Run Log available");
				continue;
			}

			var backendLog = readBackendLog(backendFile);

			var instanceFile = instanceDirectory.resolve(runLog.dataName() + ".json");

			var csvRowIndex = mainCSV.addNewRow();
			mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.Model, runLog.modelName());
			mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.Version, runLog.modelVersion());
			mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.Instance, runLog.dataName());
			mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.Run, run.runId());
			mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.PreprocessingTime, runLog.preprocessingTime());

			if (backendLog != null) {

				if (backendLog.modelSize() instanceof BackendParser.ModelSize model) {
					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.MemorySize, model.megabytes());

					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.CompileCrash, //
							model.original() == null);
					if (model.original() instanceof BackendParser.ElementCount elements) {
						mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.OriginalConstraints, //
								elements.columns());
						mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.OriginalVariables, //
								elements.rows());
						mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.OriginalCoefficients, //
								elements.nonzeros());

					}

					if (model.presolve() instanceof BackendParser.ElementCount elements) {
						mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.PresolveTime, //
								model.presolveTime());
						mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.PresolvedConstraints, //
								elements.columns());
						mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.PresolvedVariables, //
								elements.rows());
						mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.PresolvedCoefficients, //
								elements.nonzeros());
					}

				}

				if (backendLog.compile() instanceof BackendParser.CompileData data) {
					var firstPass = data.firstPass() != null ? data.firstPass().doubleValue() : -1;
					var secondPass = data.secondPass() != null ? data.secondPass().doubleValue() : -1;
					var totalCompileTime = secondPass > 0 ? secondPass : firstPass;

					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.CompileTimePassOne, //
							firstPass);
					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.CompileTimePassTwo, //
							secondPass);
					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.TotalCompileTime, //
							totalCompileTime);
					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.CompileOptimized, //
							data.secondPass() != null);

				}

				if (backendLog.solutions() instanceof BackendParser.SolutionData data) {
					var hasSolutions = data.numberOfSolutions() > 0;

					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.NumberOfSolutions, //
							data.numberOfSolutions());
					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.BestBound, //
							data.bestBound());
					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.BestObjective, //
							data.bestObjective());
					mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.MIPGap, //
							data.gap());

					if (hasSolutions) {
						var validation = getValidation(solutionFile, instanceFile);

						mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.IsSolutionValid, //
								!validation.hasAnyViolations());
						mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.RealObjective, //
								validation.getTotalCost());
					}

				}

				mainCSV.setCellValue(csvRowIndex, MainCSV.Columns.SolveCrash, //
						!backendLog.logComplete());
			}

		}

		mainCSV.write(outputDirectory.resolve("main.csv"));

	}

	public static record ModelSelection(String instance, int runId, Path folder) implements Comparable<ModelSelection> {

		@Override
		public int compareTo(ModelSelection o) {
			var compare = instance.compareToIgnoreCase(o.instance);
			if (compare != 0)
				return compare;
			return runId - o.runId;
		}
	}

	private static Collection<ModelSelection> collectRunFolders(Path dataFolder) throws IOException {
		Pattern modelPattern = Pattern.compile("^(?<instance>.*)-(?<run>\\d+)");
		Collection<ModelSelection> result = new LinkedList<>();
		try (var stream = Files.newDirectoryStream(dataFolder)) {
			for (var path : stream) {
				var matcher = modelPattern.matcher(path.getFileName().toString());
				if (matcher.find()) {
					var name = matcher.group("instance");
					var runNumber = Integer.parseInt(matcher.group("run"));
					result.add(new ModelSelection(name, runNumber, path.normalize()));
				}
			}
		}
		return result;
	}

	private static RunData readRunLog(Path file) throws IOException {
		if (!Files.exists(file))
			return null;

		try (var reader = Files.newBufferedReader(file)) {
			var parser = new RunLogParser(reader);
			return parser.parse();
		}
	}

	private static BackendData readBackendLog(Path file) throws IOException {
		try (var reader = Files.newBufferedReader(file)) {
			var parser = new BackendParser(reader);
			return parser.parse();
		}
	}

	private static ValidatorResult getValidation(Path solutionFile, Path dataFile)
			throws IOException, InterruptedException {
		var reader = new SolutionFileReader();
		var solutions = reader.parseSolutionFile(solutionFile);
		if (solutions.size() == 0)
			return null;

		var bestSolution = solutions.getLast();
		var file = solutionFile.resolveSibling(reader.constructNameForSolution(bestSolution));
		if (!Files.exists(file))
			reader.writeToFile(bestSolution, file);

		ValidatorRunner validator = new ValidatorRunner(getIHTPValidatorFile(), solutionFile.getParent());
		var validationResults = validator.run(dataFile, file);
		return validationResults;
	}

}
