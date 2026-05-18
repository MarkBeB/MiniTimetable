package org.tud.minitimetable.eval.extract;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.tud.minitimetable.eval.extract.RunLogParser.RunData;
import org.tud.minitimetable.eval.util.Parser;
import org.tud.minitimetable.eval.util.Util;

public class RunLogParser extends Parser<RunData> {

	public static record RunData(String modelVersion, String modelName, String dataName, Double preprocessingTime) {
	}

	private RunData result;

	public RunLogParser(BufferedReader reader) {
		super(reader);
	}

	@Override
	protected boolean parseContent() throws IOException {
		Pattern modelFilePattern = Pattern.compile("^Model File: .*[\\/\\\\](?<version>.*)[\\/\\\\](?<name>.*)\\.mzn$");
		Pattern dataFilePattern = Pattern.compile("^Data File: .*[\\/\\\\](?<name>.*)\\.json$");
		Pattern preprocessingPattern = Pattern.compile("^Step done in (?<time>\\d+\\s*\\w+)$");

		String modelVersion = null;
		String modelFile = null;
		String dataName = null;
		String timeValue = null;

		while (getCurrentLine() != null) {

			if (getCurrentLine().equals("---- Setup Files ----")) {
				while (getCurrentLine() != null && !getCurrentLine().startsWith("Step done in")) {

					if (modelFile == null) {
						var matcher = modelFilePattern.matcher(getCurrentLine());
						if (matcher.find()) {
							// we only care for the part after model - this makes the path very specific!
							modelVersion = matcher.group("version");
							modelFile = matcher.group("name");
						}
					} else if (dataName == null) {
						var matcher = dataFilePattern.matcher(getCurrentLine());
						if (matcher.find()) {
							dataName = matcher.group("name");
						}
					} else {
						break;
					}

					readNextLine();
				}
			}

			if (getCurrentLine().equals("---- Convert Data File ----")) {
				while (getCurrentLine() != null) {
					var matcher = preprocessingPattern.matcher(getCurrentLine());
					if (matcher.find()) {
						timeValue = matcher.group("time");
						break;
					}
					readNextLine();
				}
			}

			readNextLine();
		}

		if (modelVersion != null && modelFile != null && dataName != null) {
			Double preprocessingTime = null;
			if (timeValue != null)
				preprocessingTime = Util.convertToSeconds(timeValue);

			result = new RunData(modelVersion, modelFile, dataName, preprocessingTime);
		}

		return false;
	}

	@Override
	public RunData getParseResult() {
		return result;
	}

}