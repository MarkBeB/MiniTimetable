package org.tud.minitimetable.eval.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.tud.minitimetable.eval.util.Parser;
import org.tud.minitimetable.eval.util.Util;

public class BackendParser extends Parser<BackendParser.BackendData> {

	public static record BackendData(ModelSize modelSize, SolutionData solutions, CompileData compile,
			boolean logComplete) {
	}

	public static record ModelSize(int megabytes, ElementCount original, Double presolveTime, ElementCount presolve) {
	}

	/**
	 * @param firstPass  duration in seconds or null
	 * @param secondPass duration in seconds or null
	 */
	public static record CompileData(Double firstPass, Double secondPass) {
	}

	public static record ElementCount(int rows, int columns, int nonzeros) {
	}

	public static record SolutionData(int numberOfSolutions, BigDecimal bestObjective, BigDecimal bestBound,
			Double gap) {
	}

	private Double compileFirstPass;
	private Double compileSecondPass;

	private int modelSizeInMB;

	private ElementCount start;
	private ElementCount presolve;

	private Double presolveTime;

	private int solutionsFound = 0;
	private BigDecimal bestObjective;
	private BigDecimal bestBound;
	private Double mipGap;

	private boolean timeLimitReached;
	private double totalTimeS;

	private boolean logComplete = false;

	public BackendParser(BufferedReader reader) {
		super(reader);
	}

	public void getResult() {

	}

	@Override
	protected boolean parseContent() throws IOException {
		parseMiniZincToFlatZincTIme();
		parseModelSize();

		while (currentLineNotNull()) {
			if (currentLineNotNull() && getCurrentLine().equals("Starting NoRel heuristic")) {
				parseNoRelHeuristic();

			} else if (currentLineNotNull() && getCurrentLine().startsWith("Solution count")) {
				parseSolutionCount();

			} else if (currentLineNotNull() && getCurrentLine().startsWith("Time limit reached")) {
				this.timeLimitReached = true;
				readNextLine();

			} else if (currentLineNotNull() && getCurrentLine().startsWith("Best objective")) {
				parseBestSolution();

			} else if (currentLineNotNull() && getCurrentLine().startsWith("   Done (overall")) {
				parseOverallTime();
				logComplete = true;

			} else {
				readNextLine();

			}
		}

// Converting to old FlatZinc ... done (41.77 s)
//		 done (41.80 s), max stack depth 51
//		Maximum memory 1026 Mbytes.
//		Optimize a model with 272661 rows, 199430 columns and 879949 nonzeros (Min)
//		Presolved: 71391 rows, 28902 columns, 242067 nonzeros

//		Starting NoRel heuristic
//		NoRel heuristic complete

		// TODO Auto-generated method stub
		return false;
	}

	private void parseNoRelHeuristic() throws IOException {
		while (currentLineNotNull() && !getCurrentLine().equals("NoRel heuristic complete")) {
			readNextLine();
		}
	}

	private void parseSolutionCount() throws IOException {
		if (getCurrentLine().startsWith("Solution count")) {
			if (getCurrentLine().equals("Solution count 0")) {
				this.solutionsFound = 0;
			} else {
				Pattern pattern = Pattern.compile("^Solution count (?<count>\\d+):");
				var matcher = pattern.matcher(getCurrentLine());
				if (!matcher.find())
					throw new IllegalStateException();
				this.solutionsFound = Integer.parseInt(matcher.group("count"));
			}

			readNextLine();
		}
	}

	private void parseBestSolution() throws IOException {
		if (getCurrentLine().startsWith("Best objective")) {
			var data = getCurrentLine().split(", ");
			String v1 = data[0].substring(data[0].lastIndexOf(" ")).trim();
			String v2 = data[1].substring(data[1].lastIndexOf(" ")).trim();
			String v3 = data[2].substring(data[2].lastIndexOf(" ")).replace("%", "").trim();

			this.bestObjective = v1.equals("-") ? null : new BigDecimal(v1);
			this.bestBound = v2.equals("-") ? null : new BigDecimal(v2);
			this.mipGap = parseToDouble(v3);
			if (mipGap != null)
				mipGap = mipGap * 0.01d;

			readNextLine();
		}
	}

	private void parseModelSize() throws NumberFormatException, IOException {
		{
			Pattern pattern = Pattern.compile("Maximum memory (?<size>\\d+) Mbytes");
			while (readNextLine() != null) {
				var matcher = pattern.matcher(getCurrentLine());
				if (matcher.find()) {
					this.modelSizeInMB = Integer.parseInt(matcher.group("size"));
					break;
				}
			}
		}

		while (readNextLine() != null) {
			if (getCurrentLine().startsWith("Optimize a model")) {
				Pattern pattern = Pattern
						.compile("(?<rows>\\d+) rows, (?<columns>\\d+) columns and (?<nonzeros>\\d+) nonzeros");
				var matcher = pattern.matcher(getCurrentLine());
				if (!matcher.find())
					throw new IllegalStateException();
				start = new ElementCount(Integer.parseInt(matcher.group("rows")),
						Integer.parseInt(matcher.group("columns")), Integer.parseInt(matcher.group("nonzeros")));
				break;
			}
		}

		while (readNextLine() != null) {
			if (getCurrentLine().startsWith("Presolve time:")) {
				var presolveTime = getCurrentLine().substring(getCurrentLine().lastIndexOf(" ") + 1);
				this.presolveTime = Util.convertToSeconds(presolveTime);
			}

			if (getCurrentLine().startsWith("Presolved")) {
				Pattern pattern = Pattern
						.compile("(?<rows>\\d+) rows, (?<columns>\\d+) columns, (?<nonzeros>\\d+) nonzeros");
				var matcher = pattern.matcher(getCurrentLine());
				if (!matcher.find())
					throw new IllegalStateException();
				presolve = new ElementCount(Integer.parseInt(matcher.group("rows")),
						Integer.parseInt(matcher.group("columns")), Integer.parseInt(matcher.group("nonzeros")));
				break;
			}
		}
	}

	private void parseMiniZincToFlatZincTIme() throws IOException {
		Pattern pattern = Pattern.compile("^\\s*done\\s*\\((?<time>\\d+\\.\\d+\\s*\\w+)\\)(?<last>, max stack depth)?");

		List<String> times = new ArrayList<>(2);

		while (getCurrentLine() != null) {
			if (getCurrentLine().startsWith("Converting to old FlatZinc")) {
				var matcher = pattern.matcher(readNextLine());

				if (!matcher.find())
					throw new IllegalStateException();
				var time = matcher.group("time");

				if (readNextLine().startsWith(" done")) {
					matcher = pattern.matcher(getCurrentLine());
					if (!matcher.find())
						throw new IllegalStateException();
					time = matcher.group("time");

					times.add(time);
					break;

				} else {
					times.add(time);

				}

			}

			readNextLine();
		}

		if (times.size() > 0)
			this.compileFirstPass = secondsToMS(times.get(0));

		if (times.size() > 1)
			this.compileSecondPass = secondsToMS(times.get(1));

		if (times.size() > 2)
			throw new IllegalStateException();

//		convertTimeS = times.stream().map(this::secondsToMS).toList();
	}

	private void parseOverallTime() throws IOException {
		if (getCurrentLine().startsWith("   Done (overall")) {
			Pattern pattern = Pattern.compile("^\s+Done\\s+\\(overall time\\s+(?<time>\\d+(\\.\\d+)?)\\s+s\\)\\.$");
			var matcher = pattern.matcher(getCurrentLine());
			if (!matcher.find())
				throw new IllegalStateException();
			this.totalTimeS = Double.parseDouble(matcher.group("time"));
			readNextLine();
		}
	}

	/**
	 *
	 * @param input
	 * @return
	 */
	private Double secondsToMS(String input) {
		String cleanInput = input.trim().toLowerCase().replace(" ", "");
		Double seconds;
		if (cleanInput.endsWith("m")) {
			seconds = Double.parseDouble(cleanInput.substring(0, cleanInput.length() - 1)) * 60;
		} else if (cleanInput.endsWith("s")) {
			seconds = Double.parseDouble(cleanInput.substring(0, cleanInput.length() - 1));
		} else {
			seconds = Double.parseDouble(cleanInput);
		}
		return seconds;
	}

	private Long parseToLong(String input) {
		input = input.trim();
		if (input == null || input.isBlank() || input.equals("-"))
			return null;
		return Long.parseLong(input);
	}

	private Double parseToDouble(String input) {
		input = input.trim();
		if (input == null || input.isBlank() || input.equals("-"))
			return null;
		return Double.parseDouble(input);
	}

	@Override
	public BackendData getParseResult() {
//		if (convertTimeS == null || start == null || presolve == null || bestSolution == null)
//			return null;

		return new BackendData( //
				new ModelSize(this.modelSizeInMB, start, presolveTime, presolve), //
				new SolutionData(solutionsFound, bestObjective, bestBound, mipGap), //
				new CompileData(compileFirstPass, compileSecondPass), //
				logComplete //
		);
	}

}
