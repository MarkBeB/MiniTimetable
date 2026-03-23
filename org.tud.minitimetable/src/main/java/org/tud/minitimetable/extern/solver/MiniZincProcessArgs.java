package org.tud.minitimetable.extern.solver;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.tud.minitimetable.util.ProcessArgs;

public class MiniZincProcessArgs implements ProcessArgs {

	public static record Flag(String flag, String value) {
	};

	public Path modelFile;
	public Path dataFile;
	public Path flatzincFile;
	public Integer timeLimitMS;
	public Integer threads;
	public Path writeSolverModelToFile;
	public boolean verbose;
	public boolean statistics;
	public boolean json;
	public boolean outputTime;
	public Path gurobiParameterFilter;
	public final List<Flag> additionalFlags = new LinkedList<>();
	public final List<Flag> solverFlags = new LinkedList<>();
	public final List<Path> parameterFiles = new ArrayList<>(5);

	@Override
	public List<String> compile() {
		List<String> args = new LinkedList<>();

		args.add("--solver");
		args.add("Gurobi");

		if (writeSolverModelToFile != null) {
			args.add("--writeModel");
			args.add(writeSolverModelToFile.toString());
		}

		if (threads != null) {
			if (threads <= 0)
				throw new IllegalArgumentException("Number of threads must be greater than 0");

			args.add("-p");
			args.add(threads.toString());
		}

		if (timeLimitMS != null) {
			if (timeLimitMS <= 0)
				throw new IllegalArgumentException("Time limit must be greater than 0");

			args.add("--time-limit");
			args.add(timeLimitMS.toString());
		}

		if (modelFile != null) {
			args.add("--model");
			args.add(modelFile.toString());
		}

		if (dataFile != null) {
			args.add("--data");
			args.add(dataFile.toString());
		}

		if (flatzincFile != null) {
			args.add("--compile");
			args.add("--output-to-file");
			args.add(flatzincFile.toString());
		}

		if (verbose) {
			args.add("--verbose");
			// solver writes to System.err of process
		}

		if (statistics) {
			args.add("--statistics");
		}

		if (outputTime) {
			args.add("--output-time");
		}

		if (json) {
			args.add("--json-stream");
		}

		if (gurobiParameterFilter != null) {
			args.add("--readParam");
			args.add(gurobiParameterFilter.toString());
		}

		if (!parameterFiles.isEmpty()) {
			for (var paramFile : parameterFiles) {
				args.add("--param-file");
				args.add(paramFile.normalize().toString());
			}
		}

		for (var flag : additionalFlags) {
			if (flag.flag == null) {
				continue;
			}

			args.add(flag.flag);
			if (flag.value != null) {
				args.add(flag.value);
			}
		}

		if (!solverFlags.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("\"");

			for (var flag : solverFlags) {
				if (stringBuilder.length() > 1) {
					stringBuilder.append(" ");
				}

				if (flag.flag == null || flag.flag.isBlank()) {
					continue;
				}

				if (flag.value == null || flag.value.isBlank()) {
					stringBuilder.append(flag.flag);
				} else {
					stringBuilder.append(flag.flag + " " + flag.value);
				}
			}

			stringBuilder.append("\"");

			args.add("--fzn-flag");
			args.add(stringBuilder.toString());
		}

		return args;
	}

}
