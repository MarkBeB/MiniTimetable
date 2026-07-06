package org.tud.minitimetable.eval.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tud.minitimetable.eval.util.Parser;

public class SolutionTimestampParser extends Parser<SolutionTimestampParser.SolutionPlot> {

	public static record SolutionPlot(List<LogEntry> entries) {
	}

	public static enum EntrySource {
		NOREL, BARRIER, CROSSOVER, INCUMBENT
	}

	public static record LogEntry(double seconds, BigDecimal objective, EntrySource source) {

	}

	// Regex für Heuristiken und Zeitanker (wie zuvor)
	private static final Pattern HEURISTIC_PATTERN = Pattern
			.compile("Found heuristic solution:\\s+objective\\s+([\\d\\.\\+eE-]+)");
	private static final Pattern PRESOLVE_TIME_PATTERN = Pattern.compile("Presolve time:\\s+([\\d\\.]+)\\s*s");
	private static final Pattern ELAPSED_TIME_PATTERN = Pattern
			.compile("Elapsed time(?:\\s+for\\s+[\\w\\s]+)?:\\s+([\\d\\.]+)\\s*s");

	// Regex für Barrier Iterationen
	private static final Pattern BARRIER_ITER_PATTERN = Pattern
			.compile("^\\s*\\d+\\s+([\\d\\.\\+eE-]+)\\s+([\\d\\.\\+eE-]+).*?\\s+(\\d+)s\\s*$");

	// NEU: Regex für Root Simplex Log (Crossover) - Matches Iteration, Objective,
	// Primal Inf, Dual Inf, Time
	private static final Pattern SIMPLEX_ITER_PATTERN = Pattern
			.compile("^\\s*\\d+\\s+([\\d\\.\\+eE-]+)\\s+[\\d\\.\\+eE-]+\\s+[\\d\\.\\+eE-]+\\s+(\\d+)s\\s*$");

	// NEU: Regex für MIP Knoten-Tabelle
	// Erkennt Zeilen, die mit zwei Zahlen starten (Expl und Unexpl Nodes), gefolgt
	// von Obj, Depth, IntInf, Incumbent... und am Ende der Zeit (z.B. 973s)
	private static final Pattern MIP_NODE_PATTERN = Pattern.compile(
			"^\\s*(?:H\\s+)?\\d+\\s+\\d+\\s+([\\d\\.\\+eE-]+|-)\\s+\\d+\\s+\\d+\\s+([\\d\\.\\+eE-]+|-).*?\\s+(\\d+)s\\s*$");

	private double currentSeconds = 0.0;
	private boolean inBarrierLog = false;
	private boolean inSimplexLog = false;

	private final List<LogEntry> entries = new LinkedList<>();

	public SolutionTimestampParser(BufferedReader reader) {
		super(reader);
	}

	@Override
	protected boolean parseContent() throws IOException {

		String line;
		while ((line = readNextLine()) != null) {
			String trimmed = line.trim();

			// 1. Zeitanker aktualisieren
			Matcher presolveTimeMatcher = PRESOLVE_TIME_PATTERN.matcher(trimmed);
			if (presolveTimeMatcher.find()) {
				currentSeconds = Double.parseDouble(presolveTimeMatcher.group(1));
				continue;
			}
			Matcher elapsedTimeMatcher = ELAPSED_TIME_PATTERN.matcher(trimmed);
			if (elapsedTimeMatcher.find()) {
				currentSeconds = Double.parseDouble(elapsedTimeMatcher.group(1));
				continue;
			}

			// 2. Heuristische Lösungen (z.B. NoRel Heuristic)
			Matcher heuristicMatcher = HEURISTIC_PATTERN.matcher(trimmed);
			if (heuristicMatcher.find()) {
				var objValue = new BigDecimal(heuristicMatcher.group(1));
				entries.add(new LogEntry(currentSeconds, objValue, EntrySource.NOREL));
				continue;
			}

			// 3. Status-Steuerung für Tabellenabschnitte
			if (trimmed.contains("Objective") && trimmed.contains("Residual")) {
				inBarrierLog = true;
				inSimplexLog = false;
				continue;
			}
			if (trimmed.contains("Root simplex log...")) {
				inBarrierLog = false;
				inSimplexLog = true;
				continue;
			}
			if (trimmed.contains("Nodes") && trimmed.contains("Current Node")) {
				inBarrierLog = false;
				inSimplexLog = false; // Wechsel in die MIP-Tabelle
				continue;
			}

			// 4. Barrier Iteration parsen
			if (inBarrierLog) {
				Matcher barrierMatcher = BARRIER_ITER_PATTERN.matcher(line);
				if (barrierMatcher.find()) {
					var primalObj = new BigDecimal(barrierMatcher.group(1));
					currentSeconds = Double.parseDouble(barrierMatcher.group(3));
					entries.add(new LogEntry(currentSeconds, primalObj, EntrySource.BARRIER));
					continue;
				}
			}

			// 5. Root Simplex (Crossover) Iteration parsen
			if (inSimplexLog) {
				Matcher simplexMatcher = SIMPLEX_ITER_PATTERN.matcher(line);
				if (simplexMatcher.find()) {
					var simplexObj = new BigDecimal(simplexMatcher.group(1));
					currentSeconds = Double.parseDouble(simplexMatcher.group(2));
					entries.add(new LogEntry(currentSeconds, simplexObj, EntrySource.CROSSOVER));
					continue;
				}
			}

			// 6. MIP Branch-and-Bound Knoten-Tabelle parsen
			Matcher mIpmatcher = MIP_NODE_PATTERN.matcher(line);
			if (mIpmatcher.find()) {
				// group(2) extrahiert das 'Incumbent' (das beste aktuelle ganzzahlige
				// Objective)
				String incumbent = mIpmatcher.group(2);
				String group3 = mIpmatcher.group(3);

				if (incumbent.equals("-")) {
					continue;
				}

				var incumbentObj = new BigDecimal(incumbent);
				currentSeconds = Double.parseDouble(group3);

				entries.add(new LogEntry(currentSeconds, incumbentObj, EntrySource.INCUMBENT));
				continue;
			}
		}

		return false;
	}

	@Override
	public SolutionPlot getParseResult() {
		return new SolutionPlot(entries);
	}

}
