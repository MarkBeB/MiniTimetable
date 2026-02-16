package org.tud.minitimetable;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MiniZincRunner {

	public static class MiniZincConfig {

		public static record Flag(String flag, String value) {
		};

		protected Path _constraintModel;
		protected Path _dataModel;
		protected Integer _timeLimit;
		protected Integer _threads;
		protected Path _writeModel;

		protected List<Flag> _additionalFlags = new LinkedList<>();

		public void setConstraintModel(Path constraintModel) {
			_constraintModel = constraintModel;
		}

		public void setDataModel(Path dataModel) {
			_dataModel = dataModel;
		}

		public void setTimeLimit(Integer milliseconds) {
			_timeLimit = milliseconds;
		}

		public void setNumberOfThreads(Integer threads) {
			_threads = threads;
		}

		public void setWriteModel(Path path) {
			_writeModel = path;
		}

		public List<Flag> otherFlags() {
			return _additionalFlags;
		}
	}

	public static class MiniZincException extends Exception {
		private static final long serialVersionUID = 3305311052177462975L;

		public MiniZincException() {
			super();
		}

		public MiniZincException(String message) {
			super(message);
		}

		public MiniZincException(String message, Throwable cause) {
			super(message, cause);
		}

		public MiniZincException(Throwable cause) {
			super(cause);
		}
	}

	private final List<Path> _parameterFiles = new ArrayList<>(5);
	private final MiniZincConfig _config = new MiniZincConfig();

	private final Path _executable;
	private final Path _workingDirectory;
	private boolean _parseOutput = true;

	public MiniZincRunner(Path executable, Path workingDirectory) {
		_executable = Objects.requireNonNull(executable, "executable");
		_workingDirectory = Objects.requireNonNull(workingDirectory, "workingDirectory");
	}

	public void parseOutput(boolean value) {
		_parseOutput = value;
	}

	public List<Path> parameterFiles() {
		return _parameterFiles;
	}

	public MiniZincConfig config() {
		return _config;
	}

	public void runMiniZinc() throws IOException {
		var processBuilder = setupProcess();
		runProcess(processBuilder);
	}

	public ProcessBuilder setupProcess() {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(_workingDirectory.toFile());

		processBuilder.redirectError(Redirect.INHERIT);
		processBuilder.redirectInput(Redirect.INHERIT);
		if (!_parseOutput) {
			processBuilder.redirectOutput(Redirect.INHERIT);
		}

		// path to minizinc exe
		processBuilder.command().add(_executable.toAbsolutePath().toString());

		// set solver to Gurobi
		processBuilder.command().add("--solver");
		processBuilder.command().add("Gurobi");

		if (_config._writeModel != null) {
			processBuilder.command().add("--writeModel");
			processBuilder.command().add(_config._writeModel.toString());
		}

		if (_config._threads != null) {
			processBuilder.command().add("-p");
			processBuilder.command().add(_config._threads.toString());
		}

		if (_config._timeLimit != null) {
			processBuilder.command().add("--time-limit");
			processBuilder.command().add(_config._timeLimit.toString());
		}

		if (_config._constraintModel != null) {
			processBuilder.command().add("--model");
			processBuilder.command().add(_config._constraintModel.toString());
		}

		if (_config._dataModel != null) {
			processBuilder.command().add("--data");
			processBuilder.command().add(_config._dataModel.toString());
		}

		if (!_parameterFiles.isEmpty()) {
			for (var paramFile : parameterFiles()) {
				processBuilder.command().add("--param-file");
				processBuilder.command().add(paramFile.normalize().toString());
			}
		}

		if (!_config._additionalFlags.isEmpty()) {
			for (var flag : _config._additionalFlags) {
				if (flag.flag == null) {
					continue;
				}

				processBuilder.command().add(flag.flag);
				if (flag.value != null) {
					processBuilder.command().add(flag.value);
				}
			}
		}

		if (_parseOutput) {
			processBuilder.command().add("--json-stream");
		}

		return processBuilder;
	}

	private void runProcess(ProcessBuilder processBuilder) throws IOException {
		final Process miniZincProcess = processBuilder.start();
		final CompletableFuture<Process> isDone = miniZincProcess.onExit();

		// TODO
	}

}
