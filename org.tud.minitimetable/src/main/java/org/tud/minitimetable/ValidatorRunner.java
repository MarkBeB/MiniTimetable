package org.tud.minitimetable;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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

		processBuilder.redirectError(Redirect.INHERIT);
		processBuilder.redirectInput(Redirect.INHERIT);
		processBuilder.redirectOutput(Redirect.INHERIT);

		processBuilder.command().add(dataModelFile.toAbsolutePath().toString());
		processBuilder.command().add(solutionFile.toAbsolutePath().toString());

		if (false) {
			processBuilder.command().add("verbose");
		}

		return processBuilder;
	}

	public void setLogToConsole(boolean value) {

	}

	public void setLogToFile(boolean value) {

	}

	public void run(Path dataModelFile, Path solutionFile) throws IOException {
		ProcessBuilder processBuilder = setup(dataModelFile, solutionFile);
		final Process process = processBuilder.start();
		final CompletableFuture<Process> isDone = process.onExit();

	}

	public static class ValidatorCost {
		protected String CostType;
		protected int weight;
		protected int violations;
		protected int totalCost;
	}

	public static class ValidatorViolation {
		protected String ViolationType;
		protected int violations;
	}

	public int getTotalCost() {
		return 0;
	}

	public Collection<ValidatorCost> getCosts() {
		return Collections.emptyList();
	}

	public Collection<ValidatorViolation> getViolations() {
		return Collections.emptyList();
	}

}
