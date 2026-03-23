package org.tud.minitimetable.extern.solver;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.tud.minitimetable.extern.solver.ProcessLogger.FileLog;
import org.tud.minitimetable.extern.solver.ProcessLogger.Log;
import org.tud.minitimetable.extern.solver.ProcessLogger.NullLog;
import org.tud.minitimetable.extern.solver.ProcessLogger.StreamLog;
import org.tud.minitimetable.model.util.InputModelReader;
import org.tud.minitimetable.model.util.OutputModelWriter;
import org.tud.minitimetable.util.MiniZincLocator;
import org.tud.minitimetable.util.PathUtils;

public class MiniZinc {

	private static interface CodeLogger {
		public void log(String msg);
	}

	private static final class NullCodeLogger implements CodeLogger {
		@Override
		public void log(String msg) {

		}
	}

	private static final class ConsolCodeLogger implements CodeLogger {
		@Override
		public void log(String msg) {
			System.out.println(msg);
		}
	}

	public static class Config {
		public CodeLogger logger = new ConsolCodeLogger();
		public Log backendLog;
		public Log solverOutput;
		public Log solverModelLog;
		public Path miniZincLocation;
	}

	private final Config config = new Config();
	private final MiniZincProcessArgs arguments = new MiniZincProcessArgs();

	private CodeLogger logger;
	private Path _modelFile;
	private Path _dataFile;
	private Path _outputDirectory;
	private String _dataFileName;

	private boolean _setupCalled = false;

	public Config getConfig() {
		return config;
	}

	public MiniZincProcessArgs getSolverConfig() {
		return arguments;
	}

	public void setup(Path modelFile, Path dataFile, Path outDirectory) throws IOException {
		Objects.requireNonNull(modelFile, "modelFile");
		Objects.requireNonNull(dataFile, "dataFile");
		Objects.requireNonNull(outDirectory, "outDirectory");

		logger = config.logger != null ? config.logger : new ConsolCodeLogger();

		validateArguments(modelFile, dataFile, outDirectory);
		setupDataFile();
		_setupCalled = true;
	}

	public CompletableFuture<Process> run() throws IOException {
		if (!_setupCalled)
			throw new IllegalStateException("Setup not completed. Call .setup(...) required.");

//		MiniZincProcessRunner runner = new MiniZincProcessRunner(getMiniZinc(),  Path.of(System.getProperty("user.dir") , arguments);
		MiniZincProcessRunner runner = new MiniZincProcessRunner(getMiniZinc(), _outputDirectory, arguments);

		applyConfig(runner);

		logger.log("---- Start MiniZinc ----");
		var isDone = runner.run();
		if (config.solverOutput instanceof StreamLog streanLogger)
			streanLogger.setStream(runner.getProcessOutputStream());

		if (config.backendLog instanceof StreamLog streanLogger)
			streanLogger.setStream(runner.getProcessErrorStream());

		return isDone.whenComplete((p, ex) -> {
			config.logger.log("---- MiniZinc Terminated ----");
		});
	}

	private void validateArguments(Path modelFile, Path dataFile, Path outputDirectory) throws IOException {
		_modelFile = modelFile.normalize().toAbsolutePath();
		if (!Files.exists(_modelFile))
			throw new IllegalArgumentException("Model file not found '" + _modelFile + "'");

		_dataFile = dataFile.normalize().toAbsolutePath();
		if (!Files.exists(_dataFile))
			throw new IllegalArgumentException("Data file not found '" + _dataFile + "'");
		_dataFileName = PathUtils.getFileNameWithoutExtension(_dataFile);

		_outputDirectory = outputDirectory.normalize().toAbsolutePath();
		if (Files.isRegularFile(_outputDirectory))
			throw new IllegalArgumentException("Output directory can not be a file '" + _outputDirectory + "'");

		if (!Files.exists(_outputDirectory))
			Files.createDirectories(_outputDirectory);

		logger.log("---- Setup Files ----");
		logger.log("Model File: " + _modelFile);
		logger.log("Data File: " + _dataFile);
		logger.log("Output Folder: " + _outputDirectory);
	}

	private void setupDataFile() throws IOException {
		if ("dzn".equals(PathUtils.getFileExtension(_dataFile))) {
			return;
		}

		logger.log("---- Convert Data File ----");

		var newDataFile = _outputDirectory.resolve(_dataFileName + ".dzn");

		InputModelReader reader = new InputModelReader();
		var data = reader.read(_dataFile);

		OutputModelWriter writer = new OutputModelWriter();
		writer.write(data, newDataFile);

		_dataFile = newDataFile;

		logger.log("Data File: " + _dataFile);
	}

	private void applyConfig(MiniZincProcessRunner runner) throws IOException {
		runner.arguments().modelFile = _modelFile;
		runner.arguments().dataFile = _dataFile;

		logger.log("---- Setup Configuration ----");

		applyModelLog(runner);
		applyProcessLog(runner);
		applySolverLog(runner);
	}

	private void applyModelLog(MiniZincProcessRunner runner) throws IOException {
		if (config.solverModelLog instanceof Log logger && !(logger instanceof NullLog)) {
			if (logger instanceof FileLog fileLog) {
				var path = fileLog.generatePath(_outputDirectory, _dataFileName, "lp");
				if (!path.isAbsolute())
					path = _outputDirectory.resolve(path);

				if (!path.startsWith(_outputDirectory))
					throw new IllegalArgumentException(
							String.format("Model log file must be within output directory '%s' but was '%s'",
									_outputDirectory, path));

				if (!Files.exists(path.getParent()))
					Files.createDirectories(path.getParent());

				runner.arguments().writeSolverModelToFile = path;

			} else {
				throw new IllegalArgumentException("Log Type not supported for models: " + logger.getClass());

			}
		}
	}

	private void applyProcessLog(MiniZincProcessRunner runner) throws IOException {
		runner.arguments().json = true;
		runner.arguments().outputTime = true;

		if (config.solverOutput instanceof Log logger) {
			if (logger instanceof NullLog) {
				runner.setOutputRedirect(Redirect.DISCARD);

			} else if (logger instanceof FileLog fileLog) {
				var path = fileLog.generatePath(_outputDirectory, _dataFileName, "json");
				if (!path.isAbsolute())
					path = _outputDirectory.resolve(path);

				if (!path.startsWith(_outputDirectory))
					throw new IllegalArgumentException(
							String.format("MiniZinc log file must be within output directory '%s' but was '%s'",
									_outputDirectory, path));

				if (!Files.exists(path.getParent()))
					Files.createDirectories(path.getParent());

				runner.setOutputRedirect(Redirect.to(path.toFile()));

			} else {
				throw new IllegalArgumentException("Log Type not supported for minizinc log: " + logger.getClass());

			}
		} else {
			runner.setOutputRedirect(Redirect.INHERIT);

		}
	}

	private void applySolverLog(MiniZincProcessRunner runner) throws IOException {
		if (config.backendLog instanceof Log logger) {
			if (logger instanceof NullLog) {
				runner.setErrorRedirect(Redirect.DISCARD);

			} else if (logger instanceof FileLog fileLog) {
				var path = fileLog.generatePath(_outputDirectory, _dataFileName, "txt");
				if (!path.isAbsolute())
					path = _outputDirectory.resolve(path);

				if (!path.startsWith(_outputDirectory))
					throw new IllegalArgumentException(
							String.format("Solver log file must be within output directory '%s' but was '%s'",
									_outputDirectory, path));

				if (!Files.exists(path.getParent()))
					Files.createDirectories(path.getParent());

				runner.setErrorRedirect(Redirect.to(path.toFile()));
				runner.arguments().verbose = true;

			} else {
				throw new IllegalArgumentException("Log Type not supported for solver log: " + logger.getClass());

			}
		} else {
			runner.setErrorRedirect(Redirect.INHERIT);
			runner.arguments().verbose = true;

		}
	}

	private Path getMiniZinc() {
		MiniZincLocator locator = new MiniZincLocator();
		if (config.miniZincLocation != null)
			locator.addFolder(config.miniZincLocation);

		locator.addFolder(Path.of("..", "..", "MiniZinc", "minizinc.exe"));

		Optional<Path> location = locator.searchMiniZinc().stream().findFirst();
		if (location.isEmpty())
			throw new IllegalArgumentException(
					"MiniZinc executable not found. Please ensure MiniZinc folder is in the same directory as this JAR, or provide the correct path.");

		return location.get();
	}
}
