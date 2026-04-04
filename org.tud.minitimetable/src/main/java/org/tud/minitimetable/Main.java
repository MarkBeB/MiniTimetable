package org.tud.minitimetable;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.tud.minitimetable.extern.solver.MiniZinc;

public class Main {

	private static final String OPTION_SHORT_HELP = "h";
	private static final String OPTION_SHORT_OUTPUT_FOLDER = "o";
	private static final String OPTION_SHORT_MODEL_FILE = "m";
	private static final String OPTION_SHORT_DATA_FILE = "d";
	private static final String OPTION_SHORT_GUROBI_PARAM_FILE = "gP";
	private static final String OPTION_SHORT_MINIZINC_FILE = "e";
	private static final String OPTION_SHORT_TIMELIMIT_S = "ts";
	private static final String OPTION_SHORT_TIMELIMIT_M = "tm";
	private static final String OPTION_SHORT_SOLVER_TIMELIMIT_S = "sts";
	private static final String OPTION_SHORT_SOLVER_TIMELIMIT_M = "stm";
	private static final String OPTION_SHORT_THREADS = "p";
	private static final String OPTION_SHORT_OPTIMIZE_FLAG_1 = "O0";
	private static final String OPTION_SHORT_OPTIMIZE_FLAG_2 = "O1";
	private static final String OPTION_SHORT_OPTIMIZE_FLAG_3 = "O2";

	public static void main(String[] args) throws IOException {
		CommandLine commandLine = parseArguments(args);
		if (commandLine == null)
			return;

		Path modelFile = getModelFile(commandLine);
		Path dataFile = getDataFile(commandLine);
		Path outputFolder = getOutputFolder(commandLine);

		MiniZinc minizinc = new MiniZinc();
		DefaultSettings.applyDefaultMiniZincConfiguration(minizinc);

		applyArguments(minizinc, commandLine);

		minizinc.run(modelFile, dataFile, outputFolder).join();
	}

	private static Path getModelFile(CommandLine commandLine) {
		var str = commandLine.getOptionValue(OPTION_SHORT_MODEL_FILE);
		return Path.of(str);
	}

	private static Path getDataFile(CommandLine commandLine) {
		var str = commandLine.getOptionValue(OPTION_SHORT_DATA_FILE);
		return Path.of(str);
	}

	private static Path getOutputFolder(CommandLine commandLine) {
		if (commandLine.hasOption(OPTION_SHORT_OUTPUT_FOLDER)) {
			var str = commandLine.getOptionValue(OPTION_SHORT_OUTPUT_FOLDER);
			try {
				var path = Path.of(str);
				return path;
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid output folder: '" + str + "'", e);
			}
		} else {
			return Path.of("./", "out");
		}
	}

	private static CommandLine parseArguments(String[] args) throws IOException {
		Option help = Option.builder(OPTION_SHORT_HELP).longOpt("help").desc("Prints help message").get();

		Option modelFile = Option.builder(OPTION_SHORT_MODEL_FILE).longOpt("model").required().hasArg().numberOfArgs(1) //
				.desc("Path to model file (mzn)").get();

		Option dataFolder = Option.builder(OPTION_SHORT_DATA_FILE).longOpt("data").required().hasArg().numberOfArgs(1) //
				.desc("Path to data folder or file (json/dzn)").get();

		Option outFolder = Option.builder(OPTION_SHORT_OUTPUT_FOLDER).longOpt("out").hasArg().numberOfArgs(1) //
				.desc("Output folder").get();

		Option miniZincExe = Option.builder(OPTION_SHORT_MINIZINC_FILE).longOpt("minizinc").hasArg().numberOfArgs(1) //
				.desc("Path to minizinc exe").get();

		Option paramFile = Option.builder(OPTION_SHORT_GUROBI_PARAM_FILE).longOpt("gurobi-param").hasArg()
				.numberOfArgs(1) //
				.desc("Gurobi parameter file (prm)").get();

		Option timelimitSeconds = Option.builder(OPTION_SHORT_TIMELIMIT_S).longOpt("timelimit-seconds").hasArg()
				.numberOfArgs(1) //
				.desc("Time limit in full seconds. The default limit is 10 Minutes.").get();

		Option timelimitMinutes = Option.builder(OPTION_SHORT_TIMELIMIT_M).longOpt("timelimit-minutes").hasArg()
				.numberOfArgs(1) //
				.desc("Time limit in full minutes. The default limit is 10 Minutes.").get();

		Option solverTimelimitSeconds = Option.builder(OPTION_SHORT_SOLVER_TIMELIMIT_S)
				.longOpt("solver-timelimit-seconds").hasArg().numberOfArgs(1) //
				.desc("Solver time limit in full seconds. Has precedence over time limit. Defaults to time limit.")
				.get();

		Option solverTimelimitMinutes = Option.builder(OPTION_SHORT_SOLVER_TIMELIMIT_M)
				.longOpt("solver-timelimit-minutes").hasArg().numberOfArgs(1) //
				.desc("Solver time limit in full minutes. Has precedence over time limit. Defaults to time limit.")
				.get();

		Option threads = Option.builder(OPTION_SHORT_THREADS).longOpt("threads").hasArg().numberOfArgs(1) //
				.desc("Number of threads to use").get();

		Option optimize1 = Option.builder(OPTION_SHORT_OPTIMIZE_FLAG_1) //
				.desc("Disable Flatzinc Optimization").get();
		Option optimize2 = Option.builder(OPTION_SHORT_OPTIMIZE_FLAG_2) //
				.desc("Default Flatzinc Optimization").get();
		Option optimize3 = Option.builder(OPTION_SHORT_OPTIMIZE_FLAG_3) //
				.desc("Double Pass Flatzinc Optimization").get();

		Options options = new Options();
		options.addOption(help);
		options.addOption(dataFolder);
		options.addOption(outFolder);
		options.addOption(modelFile);
		options.addOption(paramFile);
		options.addOption(miniZincExe);
		options.addOption(timelimitSeconds);
		options.addOption(timelimitMinutes);
		options.addOption(solverTimelimitSeconds);
		options.addOption(solverTimelimitMinutes);
		options.addOption(threads);
		options.addOption(optimize1);
		options.addOption(optimize2);
		options.addOption(optimize3);

		CommandLine commandLine;
		try {
			CommandLineParser parser = new DefaultParser();
			commandLine = parser.parse(options, args);
			if (commandLine.hasOption(help)) {
				String header = "";
				String footer = "";

				HelpFormatter formatter = HelpFormatter.builder().get();
				formatter.printHelp("test", header, options, footer, true);

				return null;
			}
		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			return null;
		}

		return commandLine;
	}

	private static void applyArguments(MiniZinc minizinc, CommandLine commandLine) {
		if (commandLine.hasOption(OPTION_SHORT_MINIZINC_FILE)) {
			minizinc.getConfig().miniZincLocation = Path.of(commandLine.getOptionValue(OPTION_SHORT_MINIZINC_FILE))
					.toAbsolutePath();
		}

		if (commandLine.hasOption(OPTION_SHORT_GUROBI_PARAM_FILE)) {
			minizinc.getConfig().gurobiParameterFile = Path
					.of(commandLine.getOptionValue(OPTION_SHORT_GUROBI_PARAM_FILE)).toAbsolutePath();
		}

		if (commandLine.hasOption(OPTION_SHORT_TIMELIMIT_S)) {
			var value = parseInteger(commandLine, OPTION_SHORT_TIMELIMIT_S, "Unable to parse time limit");
			minizinc.getConfig().timeLimitMS = value * 1000l;
		} else if (commandLine.hasOption(OPTION_SHORT_TIMELIMIT_M)) {
			var value = parseInteger(commandLine, OPTION_SHORT_TIMELIMIT_M, "Unable to parse time limit");
			minizinc.getConfig().timeLimitMS = value * 60l * 1000l;
		}

		if (commandLine.hasOption(OPTION_SHORT_SOLVER_TIMELIMIT_S)) {
			var value = parseInteger(commandLine, OPTION_SHORT_SOLVER_TIMELIMIT_S, "Unable to parse solver time limit");
			minizinc.getConfig().solverTimeLimitMS = value * 1000l;
		} else if (commandLine.hasOption(OPTION_SHORT_SOLVER_TIMELIMIT_M)) {
			var value = parseInteger(commandLine, OPTION_SHORT_SOLVER_TIMELIMIT_M, "Unable to parse solver time limit");
			minizinc.getConfig().solverTimeLimitMS = value * 60l * 1000l;
		}

		if (commandLine.hasOption(OPTION_SHORT_THREADS)) {
			var value = parseInteger(commandLine, OPTION_SHORT_THREADS, "Unable to parse number of threads");
			if (value <= 0)
				throw new IllegalArgumentException("Number of threads invalid: " + value);
			minizinc.getConfig().threads = value;
		}

		if (commandLine.hasOption(OPTION_SHORT_OPTIMIZE_FLAG_1)) {
			minizinc.getConfig().optimizeLevel = 0;
		} else if (commandLine.hasOption(OPTION_SHORT_OPTIMIZE_FLAG_2)) {
			minizinc.getConfig().optimizeLevel = 1;
		} else if (commandLine.hasOption(OPTION_SHORT_OPTIMIZE_FLAG_3)) {
			minizinc.getConfig().optimizeLevel = 2;
		}
	}

	private static int parseInteger(CommandLine commandLine, String optionKey, String errorMessage) {
		var str = commandLine.getOptionValue(optionKey);
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			throw new IllegalArgumentException(errorMessage + ": " + str, e);
		}
	}

}
