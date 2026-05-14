package org.tud.minitimetable;

import static org.tud.minitimetable.DefaultLocations.getDataDirectory;
import static org.tud.minitimetable.DefaultLocations.getIHTPValidatorFile;
import static org.tud.minitimetable.DefaultLocations.getResourceDirectory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

import org.tud.minitimetable.collector.BackendParser;
import org.tud.minitimetable.collector.BackendParser.BackendData;
import org.tud.minitimetable.collector.RunLogParser;
import org.tud.minitimetable.collector.RunLogParser.RunData;
import org.tud.minitimetable.collector.SolutionParser;
import org.tud.minitimetable.collector.SolutionParser.SolutionData;
import org.tud.minitimetable.collector.util.CSV;
import org.tud.minitimetable.extern.validator.ValidatorRunner;
import org.tud.minitimetable.model.util.SolutionFileReader;

public class LogExtractor {

	private static final String BACKEND_FILE_NAME = "backend.txt";
	private static final String SOLUTION_FILE_NAME = "solution.json";
	private static final String RUN_FILE_NAME = "run.log";

	public static void main2(String[] args) throws IOException, InterruptedException {

		Path folder = getResourceDirectory().resolve("workstation").resolve("out").resolve("i21-05");

		Path backendFile = folder.resolve("backend.txt");
		Path solutionFile = folder.resolve("solution.json");
		String dataName = folder.getFileName().toString().substring(0, folder.getFileName().toString().indexOf("-"));
		Path dataFile = getResourceDirectory().resolve("input").resolve("ihtc").resolve(dataName + ".json");

		try (var reader = Files.newBufferedReader(backendFile)) {
			var parser = new BackendParser(reader);
			parser.parse();
		}

		validateSolution(solutionFile, dataFile);

	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Path runtimeDirectory = getResourceDirectory().resolve("workstation");
		Path dataDirectory = getDataDirectory();

		LocalDateTime today = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm");
		Path extractionDirectory = runtimeDirectory.resolve("extracted").resolve(today.format(dateTimeFormatter));

		if (!Files.exists(extractionDirectory))
			Files.createDirectories(extractionDirectory);

		Path dataFolder = runtimeDirectory.resolve("out");
		Collection<ModelSelection> selectData = selectData(dataFolder).stream() //
				.filter(m -> 5 <= m.run && m.run <= 8) //
				.toList();

		CSV mainCSV = CSV.loadOrCreate(extractionDirectory.resolve("main.csv"));
		mainCSV.setColumns(new String[] { //
				"model", "modelversion", "instance", "run", //
				"modelSizeMB", //
				"constraints", "variables", "coefficients", //
				"p-constraints", "p-variables", "p-coefficients" //
		});

		for (var m : selectData) {
			Path runFile = m.folder().resolve(RUN_FILE_NAME);
			Path backendFile = m.folder().resolve(BACKEND_FILE_NAME);
			Path solutionFile = m.folder().resolve(SOLUTION_FILE_NAME);

			var runLog = readRunLog(runFile);
			if (runLog == null) {
				System.out.println("Error: " + m.folder());
				System.out.println("No Run Log available");
				continue;
			}

			var backendLog = readBackendLog(backendFile);

			var dataFile = dataDirectory.resolve(runLog.dataName() + ".json");
			var solutionLog = readSolutionLog(solutionFile, dataFile);

			var csvIndex = mainCSV.addRecord();
			mainCSV.setField(csvIndex, "model", runLog.modelName());
			mainCSV.setField(csvIndex, "modelversion", runLog.modelVersion());
			mainCSV.setField(csvIndex, "instance", runLog.dataName());
			mainCSV.setField(csvIndex, "run", m.run());

			if (backendLog != null) {
				if (backendLog.modelSize() != null) {
					mainCSV.setField(csvIndex, "modelSizeMB", backendLog.modelSize().megabytes());

					if (backendLog.modelSize().original() != null) {
						mainCSV.setField(csvIndex, "constraints", backendLog.modelSize().original().columns());
						mainCSV.setField(csvIndex, "variables", backendLog.modelSize().original().rows());
						mainCSV.setField(csvIndex, "coefficients", backendLog.modelSize().original().nonzeros());
					}

					if (backendLog.modelSize().presolve() != null) {
						mainCSV.setField(csvIndex, "p-constraints", backendLog.modelSize().presolve().columns());
						mainCSV.setField(csvIndex, "p-variables", backendLog.modelSize().presolve().rows());
						mainCSV.setField(csvIndex, "p-coefficients", backendLog.modelSize().presolve().nonzeros());
					}
				}
			}

//			System.out.println(m.folder);
		}

		mainCSV.save();

	}

	public static record ModelSelection(String instance, int run, Path folder) {
	}

	private static Collection<ModelSelection> selectData(Path dataFolder) throws IOException {
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

	private static SolutionData readSolutionLog(Path solutionFile, Path dataFile) throws IOException {
		var parser = new SolutionParser(solutionFile, dataFile);
		return parser.parse();
	}

	public static void validateSolution(Path solutionFile, Path dataFile) throws IOException, InterruptedException {
		var reader = new SolutionFileReader();
		var solutions = reader.parseSolutionFile(solutionFile);

		if (solutions.size() > 0) {
			var best = solutions.getLast();
			var file = solutionFile.resolveSibling(reader.constructNameForSolution(best));
			reader.writeToFile(best, file);

			System.out.println(String.format("Validating file '%s' with solution '%s'", dataFile, file));
			ValidatorRunner validator = new ValidatorRunner(getIHTPValidatorFile(), solutionFile.getParent());
			validator.run(dataFile, file);
		} else {
			System.out.println("No solution found");
		}
	}

}
