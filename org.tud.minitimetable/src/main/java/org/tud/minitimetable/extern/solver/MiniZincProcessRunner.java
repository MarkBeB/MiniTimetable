package org.tud.minitimetable.extern.solver;

import java.nio.file.Path;

import org.tud.minitimetable.util.ProcessRunner;

public class MiniZincProcessRunner extends ProcessRunner<MiniZincProcessArgs> {

	public MiniZincProcessRunner(Path executable, Path workingDirectory) {
		this(executable, workingDirectory, new MiniZincProcessArgs());
	}

	public MiniZincProcessRunner(Path executable, Path workingDirectory, MiniZincProcessArgs arguments) {
		super(executable, workingDirectory, arguments);
	}

}
