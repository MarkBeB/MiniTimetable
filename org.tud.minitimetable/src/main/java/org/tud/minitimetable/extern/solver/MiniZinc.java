package org.tud.minitimetable.extern.solver;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.tud.minitimetable.extern.solver.CodeLogger.ConsolCodeLogger;
import org.tud.minitimetable.extern.solver.ProcessLogger.FileLog;
import org.tud.minitimetable.extern.solver.ProcessLogger.Log;
import org.tud.minitimetable.extern.solver.ProcessLogger.NullLog;
import org.tud.minitimetable.extern.solver.ProcessLogger.StreamLog;
import org.tud.minitimetable.model.util.InputModelReader;
import org.tud.minitimetable.model.util.OutputModelWriter;
import org.tud.minitimetable.util.MiniZincLocator;
import org.tud.minitimetable.util.PathUtils;

public class MiniZinc {

	public static class Config {

		public CodeLogger.Logger logger = new ConsolCodeLogger();
		public Log backendLog;
		public Log solverOutput;
		public Log solverModelLog;
		public Path miniZincLocation;
		public Path gurobiParameterFile;
		public Integer optimizeLevel;
		public Long timeLimitMS;
		public Long solverTimeLimitMS;
		public Integer threads;
	}

	private static final long ONE_SECOND = 1000l;
	private static final long ONE_MINUTE = ONE_SECOND * 60;
	private static final long ONE_HOUR = ONE_MINUTE * 60;

	private final Config config = new Config();
	private final MiniZincProcessArgs arguments = new MiniZincProcessArgs();

	private CodeLogger.Logger logger;
	private Path modelFile;
	private Path dataFile;
	private Path outputDirectory;
	private String dataFileName;
	private Long setupDuration = 0l;
	private Path cachedMiniZincLocation;

//	private boolean _setupCalled;

	public Config getConfig() {
		return config;
	}

	public CompletableFuture<Process> run(Path modelFile, Path dataFile, Path outDirectory) throws IOException {
		initializeInstance();

		logger.log("---- Start ----");
		logger.log(String.format("Start Time: %s", Instant.now()));

		setup(modelFile, dataFile, outDirectory);
		return run();
	}

	private void initializeInstance() {
		logger = config.logger != null ? config.logger : new ConsolCodeLogger();
	}

	private void setup(Path modelFile, Path dataFile, Path outDirectory) throws IOException {
		Objects.requireNonNull(modelFile, "modelFile");
		Objects.requireNonNull(dataFile, "dataFile");
		Objects.requireNonNull(outDirectory, "outDirectory");

		final Instant timeStamp = Instant.now();

		validateArguments(modelFile, dataFile, outDirectory);
		setupDataFile();
//		_setupCalled = true;

		setupDuration = timeStamp.until(Instant.now(), ChronoUnit.MILLIS);
	}

	private CompletableFuture<Process> run() throws IOException {
//		if (!_setupCalled)
//			throw new IllegalStateException("Setup not completed. Call .setup(...) required.");

//		MiniZincProcessRunner runner = new MiniZincProcessRunner(getMiniZinc(),  Path.of(System.getProperty("user.dir") , arguments);
		MiniZincProcessRunner runner = new MiniZincProcessRunner(getMiniZinc(), outputDirectory, arguments);

		applyConfig(runner);

		final Instant timeStamp = Instant.now();
		logger.logEmptyLine();
		logger.log("---- Start MiniZinc ----");
		var isDone = runner.run();

		if (config.solverOutput instanceof StreamLog streamLogger)
			streamLogger.setStream(runner.getProcessOutputStream());

		if (config.backendLog instanceof StreamLog streamLogger)
			streamLogger.setStream(runner.getProcessErrorStream());

		if (arguments.timeLimitMS != null)
			logger.log(String.format("With time limit: %s", parseTimeLimit(arguments.timeLimitMS)));

		if (arguments.solverTImeLimitMS != null)
			logger.log(String.format("With solver time limit: %s", parseTimeLimit(arguments.solverTImeLimitMS)));

		return isDone.whenComplete((p, ex) -> {
			logger.log("MiniZinc Terminated");
			logger.log(String.format("Step done in %d ms", timeStamp.until(Instant.now(), ChronoUnit.MILLIS)));
			logger.log(String.format("End Time: %s", Instant.now()));
			if (ex != null)
				logger.log("An Error occured: " + ex.getMessage());
			logger.flush();
		});
	}

	private static String parseTimeLimit(long miliseconds) {
		if (miliseconds <= ONE_SECOND) {
			return String.format("%d ms", miliseconds);
		} else if (miliseconds <= ONE_MINUTE) {
			double time = miliseconds / (ONE_SECOND + 0.0d);
			return String.format("%.3f s", time);
		} else if (miliseconds <= ONE_HOUR) {
			double time = miliseconds / (ONE_MINUTE + 0.0d);
			return String.format("%.3f m", time);
		} else {
			double time = miliseconds / (ONE_HOUR + 0.0d);
			return String.format("%.3f h", time);
		}
	}

	private void validateArguments(Path modelFile, Path dataFile, Path outputDirectory) throws IOException {
		final Instant timeStamp = Instant.now();

		this.modelFile = modelFile.normalize().toAbsolutePath();
		if (!Files.exists(this.modelFile))
			throw new IllegalArgumentException("Model file not found '" + this.modelFile + "'");

		this.dataFile = dataFile.normalize().toAbsolutePath();
		if (!Files.exists(this.dataFile))
			throw new IllegalArgumentException("Data file not found '" + this.dataFile + "'");
		this.dataFileName = PathUtils.getFileNameWithoutExtension(dataFile);

		this.outputDirectory = outputDirectory.normalize().toAbsolutePath();
		if (Files.isRegularFile(this.outputDirectory))
			throw new IllegalArgumentException("Output directory can not be a file '" + this.outputDirectory + "'");

		if (!Files.exists(this.outputDirectory))
			Files.createDirectories(this.outputDirectory);

		logger.logEmptyLine();
		logger.log("---- Setup Files ----");
		logger.log("Model File: " + this.modelFile);
		logger.log("Data File: " + this.dataFile);
		logger.log("Output Folder: " + this.outputDirectory);
		logger.log(String.format("Step done in %d ms", timeStamp.until(Instant.now(), ChronoUnit.MILLIS)));
	}

	private void setupDataFile() throws IOException {
		if ("dzn".equals(PathUtils.getFileExtension(dataFile))) {
			return;
		}

		logger.logEmptyLine();
		logger.log("---- Convert Data File ----");
		final Instant timeStamp = Instant.now();

		var newDataFile = outputDirectory.resolve(dataFileName + ".dzn");
		InputModelReader reader = new InputModelReader();
		var data = reader.read(dataFile);
		OutputModelWriter writer = new OutputModelWriter();
		writer.write(data, newDataFile);
		dataFile = newDataFile;
		logger.log(String.format("Data File [%d ms]: %s", timeStamp.until(Instant.now(), ChronoUnit.MILLIS), dataFile));

		logger.log(String.format("Step done in %d ms", timeStamp.until(Instant.now(), ChronoUnit.MILLIS)));
	}

	private void applyConfig(MiniZincProcessRunner runner) throws IOException {
		final Instant timeStamp = Instant.now();

		runner.arguments().modelFile = modelFile;
		runner.arguments().dataFile = dataFile;

		logger.logEmptyLine();
		logger.log("---- Setup Configuration ----");

		if (config.threads != null)
			runner.arguments().threads = config.threads;

		if (config.optimizeLevel != null)
			runner.arguments().optimizeLevel = config.optimizeLevel;

		applyModelLog(runner);
		applyProcessLog(runner);
		applySolverLog(runner);

		var duration = timeStamp.until(Instant.now(), ChronoUnit.MILLIS);
		setupDuration += duration;

		if (config.timeLimitMS != null)
			arguments.timeLimitMS = config.timeLimitMS - setupDuration;

		if (config.solverTimeLimitMS != null)
			arguments.solverTImeLimitMS = config.solverTimeLimitMS;

		if (config.gurobiParameterFile != null)
			arguments.gurobiParameterFile = config.gurobiParameterFile;

		logger.log(String.format("Step done in %d ms", duration));
	}

	private void applyModelLog(MiniZincProcessRunner runner) throws IOException {
		if (config.solverModelLog instanceof Log logger && !(logger instanceof NullLog)) {
			if (logger instanceof FileLog fileLog) {
				var path = fileLog.generatePath(outputDirectory, dataFileName, "lp");
				if (!path.isAbsolute())
					path = outputDirectory.resolve(path);

				if (!path.startsWith(outputDirectory))
					throw new IllegalArgumentException(String.format(
							"Model log file must be within output directory '%s' but was '%s'", outputDirectory, path));

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
				var path = fileLog.generatePath(outputDirectory, dataFileName, "json");
				if (!path.isAbsolute())
					path = outputDirectory.resolve(path);

				if (!path.startsWith(outputDirectory))
					throw new IllegalArgumentException(
							String.format("MiniZinc log file must be within output directory '%s' but was '%s'",
									outputDirectory, path));

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
				var path = fileLog.generatePath(outputDirectory, dataFileName, "txt");
				if (!path.isAbsolute())
					path = outputDirectory.resolve(path);

				if (!path.startsWith(outputDirectory))
					throw new IllegalArgumentException(
							String.format("Solver log file must be within output directory '%s' but was '%s'",
									outputDirectory, path));

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
		if (cachedMiniZincLocation != null && Files.exists(cachedMiniZincLocation))
			return cachedMiniZincLocation;

		MiniZincLocator locator = new MiniZincLocator();
		if (config.miniZincLocation != null)
			locator.addFolder(config.miniZincLocation);

		locator.addFolder(Path.of("..", "..", "MiniZinc", "minizinc.exe"));

		Optional<Path> location = locator.searchMiniZinc().stream().findFirst();
		if (location.isEmpty())
			throw new IllegalArgumentException(
					"MiniZinc executable not found. Please ensure MiniZinc folder is in the same directory as this JAR, or provide the correct path.");

		cachedMiniZincLocation = location.get();
		return cachedMiniZincLocation;
	}
}
