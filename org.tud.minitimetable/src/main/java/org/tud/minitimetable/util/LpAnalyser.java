package org.tud.minitimetable.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LpAnalyser {

	private static class Parser {
		private static enum Section {
			OBJECTIVE, CONSTRAINTS, BOUNDS, GENERALS, BINARIES, END
		}

		private final Pattern _variableNamePattern = Pattern.compile("(?i)\\b[a-z][a-z0-9_]*\\b");

		private Section _currentSection;

		protected long _lineCount = 0;
		protected long _constraintCount = 0;
		protected Set<String> _variables = new HashSet<>();
		protected Set<String> _binaryVariables = new HashSet<>();

		public void parse(Path file) throws IOException {
			try (BufferedReader reader = Files.newBufferedReader(file)) {
				String line;
				while ((line = reader.readLine()) != null) {
					_lineCount++;

					line = line.trim();
					parse(line);

					if (_currentSection == Section.END) {
						break;
					}
				}
			}
		}

		private void parse(String line) {
			if (line.isBlank() || line.startsWith("\\"))
				return;

			String section = line.toLowerCase();
			if (section.startsWith("maximize") || section.startsWith("minimize")) {
				_currentSection = Section.OBJECTIVE;
				return;
			} else if (section.startsWith("subject to") || section.startsWith("st")) {
				_currentSection = Section.CONSTRAINTS;
				return;
			} else if (section.startsWith("bounds")) {
				_currentSection = Section.BOUNDS;
				return;
			} else if (section.startsWith("generals")) {
				_currentSection = Section.GENERALS;
				return;
			} else if (section.startsWith("binaries") || section.startsWith("binary") || section.startsWith("bin")) {
				_currentSection = Section.BINARIES;
				return;
			} else if (section.startsWith("end")) {
				_currentSection = Section.END;
				return;
			}

			switch (_currentSection) {
			case OBJECTIVE:
			case CONSTRAINTS:
				if (line.contains(":")) {
					_constraintCount++;
				}
				extractVariables(line);
				break;
			case BINARIES:
				extractBinaryVariables(line);
				break;
			case GENERALS:
				extractVariables(line);
				break;
			}
		}

		private void extractBinaryVariables(String line) {
			var variables = getVariables(line);
			_variables.addAll(variables);
			_binaryVariables.addAll(variables);
		}

		private void extractVariables(String line) {
			var variables = getVariables(line);
			_variables.addAll(variables);
		}

		private Collection<String> getVariables(String line) {
			Collection<String> result = new ArrayList<>(4);
			Matcher matcher = _variableNamePattern.matcher(line);
			while (matcher.find()) {
				String varName = matcher.group();
				result.add(varName);
			}
			return result;
		}

	}

	public static record AnalyserResult(long lineCount, long constraintCount, int totalVariableCount,
			int binaryVariableCount) {
	}

	public AnalyserResult analyze(Path lpFile) throws IOException {
		var parser = new Parser();
		parser.parse(lpFile);
		return new AnalyserResult(parser._lineCount, parser._constraintCount, parser._variables.size(),
				parser._binaryVariables.size());
	}

}
