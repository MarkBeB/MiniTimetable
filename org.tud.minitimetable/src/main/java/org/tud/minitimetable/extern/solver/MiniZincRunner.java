package org.tud.minitimetable.extern.solver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;

public class MiniZincRunner {

	public static class MiniZincConfig {

		public static record Flag(String flag, String value) {
		};

		protected Path _constraintModel;
		protected Path _dataModel;
		protected Path _outputFolder;
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

	private final List<Path> _parameterFiles = new ArrayList<>(5);
	private final MiniZincConfig _config = new MiniZincConfig();

	private final Path _executable;
	private final Path _workingDirectory;
	private Path _outputFolder;
	private long _shutdownInMS; // TODO

	private boolean _parseOutput = true;
	private KeepSolutionData _keepSolutionData = KeepSolutionData.NONE;
	private SolutionToFile _solutionToFile = SolutionToFile.ALL;

	public MiniZincRunner(Path executable, Path workingDirectory) {
		_executable = Objects.requireNonNull(executable, "executable");
		_workingDirectory = Objects.requireNonNull(workingDirectory, "workingDirectory");
	}

	public void setSolutionOutputFolder(Path outputFolder) {
		_outputFolder = outputFolder;
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

	public SolverResult runMiniZinc() throws IOException, InterruptedException {
		validateConfig();
		setupVariables();
		var processBuilder = setupProcess();
		return runProcess(processBuilder);
	}

	private void validateConfig() {
		if (_config._constraintModel == null)
			throw new IllegalStateException("No constraint model specified");

		if (!Files.exists(_config._constraintModel))
			throw new IllegalStateException(String.format("Constraint model '%s' not found", _config._constraintModel));

		if (_config._dataModel == null)
			throw new IllegalStateException("No data model specified");

		if (!Files.exists(_config._dataModel))
			throw new IllegalStateException(String.format("Data model '%s' not found", _config._dataModel));
	}

	private void setupVariables() {
		// setup
		if (_outputFolder == null) {
			_outputFolder = _config._dataModel.getParent();
		}
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

		processBuilder.command().add("--output-time");

		if (_parseOutput) {
			processBuilder.command().add("--json-stream");
		}

		return processBuilder;
	}

	private SolverResult runProcess(ProcessBuilder processBuilder) throws IOException, InterruptedException {
		final Process miniZincProcess = processBuilder.start();
		final CompletableFuture<Process> isDone = miniZincProcess.onExit();

		List<Solution> solutions = new LinkedList<>();
		SolutionStatus status = SolutionStatus.UNKNOWN;

		// TODO refactor into multiple methods
		if (_parseOutput) {
			Solution bestSolution = null;

			var outStream = miniZincProcess.getInputStream();
			System.out.println("Found  stream: " + outStream);
			final BufferedReader outReader = new BufferedReader(new InputStreamReader(outStream));

			String lastLine = outReader.readLine();
			while (lastLine != null) {
				var json = (JSONObject) JSON.parse(lastLine);
				System.out.println(json);

				String parseType = json.getString("type");
				if ("solution".equals(parseType)) {
					JSONObject solutionOutput = json.getJSONObject("output");
					String content = solutionOutput.getString("default"); // sometimes
					// 'dzn'

					if (content != null) {
						var result = JSON.parseObject(content);
						var data = new SolutionData(result);

						var solution = new Solution();
						solution.timestamp = System.currentTimeMillis();
						solution.time = json.getLongValue("time");
						solution.number = solutions.size() + 1;
						solution.score = data.getScore();

						solution.data = data;

						solutions.add(solution);

						switch (_solutionToFile) {
						case ALL:
							writeSolutionToFileSystem(solution);
							break;
						default:
							break;
						}

						switch (_keepSolutionData) {
						case NONE:
							solution.data = null;
							break;
						case BEST:
							if (bestSolution != null && bestSolution.score < solution.score) {
								bestSolution.data = null;
							}
							break;
						case ALL:
							break; // nothing to do
						default:
							break;
						}

						if (bestSolution == null) {
							bestSolution = solution;
						} else if (bestSolution.score < solution.score) {
							bestSolution = solution;
						}

					} else {
						System.out.println("Error? A");
					}
				} else if ("status".equals(parseType)) {
					String solverStatus = json.getString("status");
					status = switch (solverStatus) {
					case "OPTIMAL_SOLUTION" -> SolutionStatus.OPTIMAL;
					default -> {
						System.out.println("Unknown Status: " + solverStatus);
						yield SolutionStatus.UNKNOWN;
					}
					};
				} else if ("error".equals(parseType))
					// String typeOfError = json.getString("what");
//					if ("syntax error".equals(typeOfError) || "type error".equals(typeOfError))
					throw new MiniZincSyntaxError(json.getString("message"), json.getString("location"));

				lastLine = outReader.readLine();
			}

			// TODO doesn't work correctly, sequence is all wrong
			switch (_solutionToFile) {
			case BEST:
				writeSolutionToFileSystem(bestSolution);
				break;
			default:
				break;
			}
		}

		while (!isDone.isDone()) {
			Thread.sleep(100);
		}

		return new SolverResult(status, solutions);
	}

	private void writeSolutionToFileSystem(Solution solution) throws IOException {
		if (solution.data == null)
			return;

		solution.fileName = String.format("%s_#%d_%d_%d.json", getFileName(_config._dataModel), solution.number,
				solution.score, solution.time);
		solution.outputFile = _outputFolder.resolve(solution.fileName);

		writeSolutionToFileSystem(solution.outputFile, solution.data.json);
	}

	private void writeSolutionToFileSystem(Path newFile, JSONObject content) throws IOException {
		Files.createDirectories(newFile.getParent());
		try (var out = Files.newOutputStream(newFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING)) {
			JSON.writeTo(out, content, JSONWriter.Feature.PrettyFormat);
		}
	}

	private static String getFileName(Path file) {
		String name = file.getFileName().toString();
		int extensionStart = name.lastIndexOf('.');
		if (extensionStart >= 0)
			return name.substring(0, extensionStart);
		return name;
	}

}
