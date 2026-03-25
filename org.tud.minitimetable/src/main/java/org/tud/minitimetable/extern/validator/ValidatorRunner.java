package org.tud.minitimetable.extern.validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class ValidatorRunner {

	private final Path _executable;
	private final Path _workingDir;

	public ValidatorRunner(Path executable, Path workingDir) {
		_executable = Objects.requireNonNull(executable, "executable");
		_workingDir = Objects.requireNonNull(workingDir, "workingDir");
	}

	private ProcessBuilder setup(Path dataModelFile, Path solutionFile) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(_workingDir.toFile());
		processBuilder.command().add(_executable.toAbsolutePath().toString());
//		processBuilder.inheritIO();

		processBuilder.redirectError(Redirect.INHERIT);
		processBuilder.redirectInput(Redirect.INHERIT);
//		processBuilder.redirectOutput(Redirect.INHERIT);

		processBuilder.command().add(dataModelFile.toString());
		processBuilder.command().add(solutionFile.toString());

		if (false) {
			processBuilder.command().add("verbose");
		}

		return processBuilder;
	}

	public void setLogToConsole(boolean value) {

	}

	public void setLogToFile(boolean value) {

	}

	public ValidatorResult run(Path dataModelFile, Path solutionFile) throws IOException, InterruptedException {
		ProcessBuilder processBuilder = setup(dataModelFile, solutionFile);
		final Process process = processBuilder.start();
		final CompletableFuture<Process> isDone = process.onExit();

		var outStream = process.getInputStream();
		var result = parseOutput(outStream);

		isDone.join();

		return result;
	}

	private static final class Parser {
		private final BufferedReader reader;
		private final ValidatorResult result;
		private String currentLine;

		public Parser(InputStream input) {
			reader = new BufferedReader(new InputStreamReader(input));
			result = new ValidatorResult();
		}

		public void readFirstLine() throws IOException {
			readNextLine();
		}

		public String readNextLine() throws IOException {
			currentLine = reader.readLine();
			if (currentLine != null) {
				System.out.println(currentLine);
			}
			return currentLine;
		}

		public void start() throws IOException {
			readFirstLine();
			while (currentLine != null) {
				if (currentLine.startsWith("VIOLATIONS:")) {
					parseViolations();
				} else if (currentLine.startsWith("COSTS")) {
					parseCosts();
				} else {
					readNextLine();
				}
			}
		}

		private void parseViolations() throws IOException {
			readNextLine();

			Pattern pattern = Pattern.compile("^(?<name>\\w+)\\.*(?<count>\\d+)$");
			while (currentLine != null) {
				boolean lastLine = currentLine.startsWith("Total violations");
				if (lastLine)
					return;

				var matcher = pattern.matcher(currentLine);
				if (matcher.find()) {
					String name = matcher.group("name");
					String count = matcher.group("count");
					ValidatorViolation violation = new ValidatorViolation(name, Integer.parseInt(count));
					result.violations.add(violation);
				} else {
					System.err.println("Unable to parse violation: " + currentLine);
				}

				readNextLine();
			}
		}

		private void parseCosts() throws IOException {
			readNextLine();
			Pattern pattern = Pattern
					.compile("^(?<name>\\w+)\\.*(?<total>\\d+) \\(\\s*(?<weight>\\d+) X \\s*(?<occurences>\\d+)\\)$");

			while (currentLine != null) {
				if (currentLine.startsWith("Total cost")) {
					var expectedCost = Integer.parseInt(currentLine.substring(currentLine.lastIndexOf(" ") + 1));
					var actualCost = result.getTotalCost();
					if (expectedCost != actualCost) {
						System.err.println(
								"Cost calculation incorrect. Expected " + expectedCost + " but was " + actualCost);
					}
					return;
				}

				var matcher = pattern.matcher(currentLine);
				if (matcher.find()) {
					String name = matcher.group("name");
					String total = matcher.group("total");
					String weight = matcher.group("weight");
					String occurences = matcher.group("occurences");
					ValidatorCost cost = new ValidatorCost(name, Integer.parseInt(total), Integer.parseInt(weight),
							Integer.parseInt(occurences));
					result.costs.add(cost);
				} else {
					System.err.println("Unable to parse cost: " + currentLine);
				}

				readNextLine();
			}
		}

		public ValidatorResult getResult() {
			return result;
		}

	}

	private ValidatorResult parseOutput(InputStream outStream) throws IOException {
		var parser = new Parser(outStream);
		parser.start();
		return parser.getResult();
	}

}
