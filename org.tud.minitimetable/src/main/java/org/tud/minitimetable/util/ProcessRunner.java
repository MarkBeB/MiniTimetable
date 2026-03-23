package org.tud.minitimetable.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ProcessRunner<A extends ProcessArgs> {

	private final Path _executable;
	private final Path _workingDirectory;

	private A _args;
	private Redirect _errorRedirect;
	private Redirect _inputRedirect;
	private Redirect _outputRedirect;
	private Process _process;

	public ProcessRunner(Path executable, Path workingDirectory, A arguments) {
		_executable = Objects.requireNonNull(executable, "executable");
		_workingDirectory = Objects.requireNonNull(workingDirectory, "workingDirectory");
		_args = Objects.requireNonNull(arguments, "arguments");
	}

	public A arguments() {
		return _args;
	}

	public CompletableFuture<Process> run() throws IOException {
		var processBuilder = setupProcess();
		return runProcess(processBuilder);
	}

	/**
	 * @see ProcessBuilder#redirectError(Redirect)
	 */
	public void setErrorRedirect(Redirect error) {
		_errorRedirect = error;
	}

	/**
	 * @see ProcessBuilder#redirectInput(Redirect)
	 */
	public void setInputRedirect(Redirect input) {
		_inputRedirect = input;
	}

	/**
	 * @see ProcessBuilder#redirectOutput(Redirect)
	 */
	public void setOutputRedirect(Redirect output) {
		_outputRedirect = output;
	}

	public InputStream getProcessErrorStream() {
		return _process.getErrorStream();
	}

	public InputStream getProcessOutputStream() {
		return _process.getInputStream();
	}

	public OutputStream getProcessInputStream() {
		return _process.getOutputStream();
	}

	private ProcessBuilder setupProcess() throws IOException {
		if (!Files.isRegularFile(_executable))
			throw new IllegalStateException("EXE not found: " + _executable);

		if (!Files.exists(_workingDirectory)) {
			Files.createDirectories(_workingDirectory);
		}

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.directory(_workingDirectory.toFile());

		if (_errorRedirect != null) {
			processBuilder.redirectError(_errorRedirect);
		}

		if (_inputRedirect != null) {
			processBuilder.redirectInput(_inputRedirect);
		}

		if (_outputRedirect != null) {
			processBuilder.redirectOutput(_outputRedirect);
		}

		processBuilder.command().add(_executable.toAbsolutePath().toString());

		var arguments = _args.compile();
		processBuilder.command().addAll(arguments);

		return processBuilder;
	}

	private CompletableFuture<Process> runProcess(ProcessBuilder processBuilder) throws IOException {
		final Process process = processBuilder.start();
		_process = process;

		final CompletableFuture<Process> isDone = process.onExit();

		var cancelableFuture = new CompletableFuture<Process>() {
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				if (process.isAlive()) {
					process.destroy();
				}
				return super.cancel(mayInterruptIfRunning);
			}
		};

		isDone.whenComplete((p, ex) -> {
			if (ex != null) {
				cancelableFuture.completeExceptionally(ex);
			} else {
				cancelableFuture.complete(p);
			}
		});

		return cancelableFuture;
	}

}
